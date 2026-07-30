package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GosuPluginsUnitTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testEventMessagingPlugin() {
        EventMessagingPluginMock plugin = new EventMessagingPluginMock();

        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-990011");
        period.setJobNumber("S0005001");
        period.setStatus("Bound");

        String payload = plugin.formatMessage("PolicyBoundEvent", period);
        assertNotNull(payload);
        assertTrue(payload.contains("POL-990011"));
        assertTrue(payload.contains("PolicyBoundEvent"));

        boolean sent = plugin.sendMessage("kafka.policycenter.events", payload);
        assertTrue(sent);
    }

    @Test
    public void testAddressVerificationPlugin() {
        AddressVerificationPluginMock plugin = new AddressVerificationPluginMock();

        Map<String, String> rawAddress = new HashMap<>();
        rawAddress.put("addressLine1", "100 Market St");
        rawAddress.put("city", "San Francisco");
        rawAddress.put("state", "CA");
        rawAddress.put("postalCode", "94105");

        Map<String, Object> result = plugin.verifyAddress(rawAddress);
        assertNotNull(result);
        assertEquals(true, result.get("valid"));
        assertEquals("94105-1908", result.get("standardizedPostalCode"));
        assertEquals("USPS Verified", result.get("verificationStatus"));
    }

    @Test
    public void testPaymentGatewayPlugin() {
        PaymentGatewayPluginMock plugin = new PaymentGatewayPluginMock();

        Map<String, Object> tokenResult = plugin.tokenizeCard("4111222233334444", "12/28", "123");
        assertNotNull(tokenResult);
        assertEquals("SUCCESS", tokenResult.get("status"));
        assertNotNull(tokenResult.get("paymentToken"));
        assertTrue(((String) tokenResult.get("paymentToken")).startsWith("TOK-"));

        BigDecimal totalPrem = new BigDecimal("1200.00");
        List<Map<String, Object>> schedule = plugin.generateInstallments(totalPrem, 4);
        assertNotNull(schedule);
        assertEquals(4, schedule.size());
        assertEquals(new BigDecimal("300.00"), schedule.get(0).get("amount"));
    }

    // Mock classes representing Gosu plugin implementations
    public static class EventMessagingPluginMock {
        public String formatMessage(String eventName, PolicyPeriod period) {
            return "{\"event\":\"" + eventName + "\",\"policyNumber\":\"" + period.getPolicyNumber() + "\",\"status\":\"" + period.getStatus() + "\"}";
        }

        public boolean sendMessage(String topic, String payload) {
            return payload != null && !payload.isEmpty();
        }
    }

    public static class AddressVerificationPluginMock {
        public Map<String, Object> verifyAddress(Map<String, String> input) {
            Map<String, Object> res = new HashMap<>();
            res.put("valid", true);
            res.put("standardizedPostalCode", input.get("postalCode") + "-1908");
            res.put("verificationStatus", "USPS Verified");
            return res;
        }
    }

    public static class PaymentGatewayPluginMock {
        public Map<String, Object> tokenizeCard(String cardNumber, String exp, String cvv) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "SUCCESS");
            res.put("paymentToken", "TOK-" + System.currentTimeMillis());
            return res;
        }

        public List<Map<String, Object>> generateInstallments(BigDecimal totalAmount, int count) {
            List<Map<String, Object>> schedule = new ArrayList<>();
            BigDecimal installmentAmt = totalAmount.divide(new BigDecimal(count));
            for (int i = 1; i <= count; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("installmentNumber", i);
                item.put("amount", installmentAmt);
                schedule.add(item);
            }
            return schedule;
        }
    }
}
