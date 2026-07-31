package com.guidewire.pc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhookPublisherService {
    private static final Logger LOGGER = Logger.getLogger(WebhookPublisherService.class.getName());
    private static final WebhookPublisherService instance = new WebhookPublisherService();

    public record WebhookEvent(String eventId, String eventType, String timestamp, Map<String, Object> payload) {}

    private final List<String> subscribers = new ArrayList<>();
    private final List<WebhookEvent> eventLog = new ArrayList<>();

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
                    LOGGER.log(Level.INFO, "[Virtual Thread Webhook Publisher] Dispatched event: {0} [{1}] to {2}", new Object[]{eventType, eventId, subUrl});
                });
            }
        }
    }

    public List<WebhookEvent> getEventLog() {
        synchronized (eventLog) {
            return new ArrayList<>(eventLog);
        }
    }
}
