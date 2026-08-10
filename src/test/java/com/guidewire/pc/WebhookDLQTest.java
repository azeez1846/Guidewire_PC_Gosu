package com.guidewire.pc;

import com.guidewire.pc.service.WebhookPublisherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookDLQTest {

    @Test
    @DisplayName("Test Webhook Dead-Letter Queue (DLQ) & Replay Engine")
    public void testWebhookDLQAndReplay() {
        WebhookPublisherService service = WebhookPublisherService.getInstance();
        assertNotNull(service);

        // Manually record a failed webhook delivery
        String eventId = "EVT-TEST-FAIL-909";
        service.recordFailure(eventId, "https://failing-partner.com/webhook", "POLICY_BOUND", "503 Service Unavailable", Map.of("policyNumber", "POL-849102"));

        var dlq = service.getDeadLetterQueue();
        assertFalse(dlq.isEmpty(), "DLQ should contain recorded failed webhook entries");

        boolean replayed = service.replayFailedWebhook(eventId);
        assertTrue(replayed, "Replaying valid eventId from DLQ should return true");
    }
}
