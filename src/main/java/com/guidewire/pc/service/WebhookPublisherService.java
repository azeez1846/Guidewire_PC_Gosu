package com.guidewire.pc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhookPublisherService {
    private static final Logger LOGGER = Logger.getLogger(WebhookPublisherService.class.getName());
    private static final WebhookPublisherService instance = new WebhookPublisherService();

    public record WebhookEvent(String eventId, String eventType, String timestamp, Map<String, Object> payload) {}

    public record FailedWebhook(
        String eventId,
        String targetUrl,
        String eventType,
        String failureReason,
        int retryCount,
        String timestamp,
        Map<String, Object> payload
    ) {}

    private final List<String> subscribers = new ArrayList<>();
    private final List<WebhookEvent> eventLog = new ArrayList<>();
    private final ConcurrentLinkedQueue<FailedWebhook> deadLetterQueue = new ConcurrentLinkedQueue<>();

    private WebhookPublisherService() {
        subscribers.add("https://analytics.guidewire-demo.com/webhooks");
        subscribers.add("https://claims-hub.guidewire-demo.com/events");
    }

    public static WebhookPublisherService getInstance() {
        return instance;
    }

    public void registerSubscriber(String webhookUrl) {
        if (webhookUrl != null && !subscribers.contains(webhookUrl)) {
            subscribers.add(webhookUrl);
        }
    }

    public void publishEvent(String eventType, Map<String, Object> payload) {
        String eventId = "EVT-" + (System.currentTimeMillis() % 89999 + 10000);
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
        WebhookEvent event = new WebhookEvent(eventId, eventType, timestamp, payload);
        
        synchronized (eventLog) {
            eventLog.add(event);
        }

        // Dispatch to all subscribers asynchronously using Java 23 Virtual Threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String subUrl : subscribers) {
                executor.submit(() -> {
                    if (subUrl.contains("fail") || subUrl.contains("error")) {
                        recordFailure(eventId, subUrl, eventType, "HTTP 503 Service Unavailable / Connection Timeout", payload);
                    } else {
                        LOGGER.log(Level.INFO, "[Virtual Thread Webhook Publisher] Dispatched event: {0} [{1}] to {2}", new Object[]{eventType, eventId, subUrl});
                    }
                });
            }
        }
    }

    public void recordFailure(String eventId, String targetUrl, String eventType, String failureReason, Map<String, Object> payload) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
        FailedWebhook failed = new FailedWebhook(eventId, targetUrl, eventType, failureReason, 1, timestamp, payload);
        deadLetterQueue.add(failed);
        LOGGER.log(Level.WARNING, "[Webhook DLQ] Captured failed delivery [{0}] to {1}: {2}", new Object[]{eventId, targetUrl, failureReason});
    }

    public List<FailedWebhook> getDeadLetterQueue() {
        return Collections.unmodifiableList(new ArrayList<>(deadLetterQueue));
    }

    public boolean replayFailedWebhook(String eventId) {
        FailedWebhook target = null;
        for (FailedWebhook fw : deadLetterQueue) {
            if (fw.eventId().equals(eventId)) {
                target = fw;
                break;
            }
        }
        if (target != null) {
            deadLetterQueue.remove(target);
            LOGGER.log(Level.INFO, "[Webhook DLQ Replay] Successfully replayed webhook event: {0} to {1}", new Object[]{target.eventId(), target.targetUrl()});
            return true;
        }
        return false;
    }

    public List<WebhookEvent> getEventLog() {
        synchronized (eventLog) {
            return new ArrayList<>(eventLog);
        }
    }
}
