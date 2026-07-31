package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.service.WebhookPublisherService;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DeveloperExperienceTest {

    @Test
    public void testGosuScriptHotReloading() {
        assertDoesNotThrow(() -> GosuBridge.reloadScripts());
    }

    @Test
    public void testGosuReplEvaluation() {
        Object result = GosuBridge.eval("print('Hello Gosu Engine')");
        assertNotNull(result);
        assertTrue(result.toString().contains("Gosu Eval Output"));
    }

    @Test
    public void testVirtualThreadWebhookPublisher() {
        WebhookPublisherService publisher = WebhookPublisherService.getInstance();
        publisher.registerSubscriber("https://test-partner-webhook.com/api/v1");

        publisher.publishEvent("POLICY_BOUND", Map.of(
                "policyNumber", "POL-VT-88102",
                "status", "Bound",
                "effectiveDate", "2026-08-01"
        ));

        List<WebhookPublisherService.WebhookEvent> events = publisher.getEventLog();
        assertFalse(events.isEmpty());
        WebhookPublisherService.WebhookEvent lastEvent = events.get(events.size() - 1);
        assertEquals("POLICY_BOUND", lastEvent.eventType());
        assertEquals("POL-VT-88102", lastEvent.payload().get("policyNumber"));
    }
}
