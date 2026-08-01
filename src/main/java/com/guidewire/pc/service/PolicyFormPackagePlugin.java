package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyForm;
import com.guidewire.pc.model.PolicyPeriod;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyFormPackagePlugin {

    public static Map<String, Object> buildPolicyPacket(PolicyPeriod period, List<PolicyForm> forms) {
        Map<String, Object> packet = new HashMap<>();
        if (period == null) return packet;

        List<PolicyForm> formList = forms != null ? forms : new ArrayList<>();
        int mandatoryCount = 0;
        int optionalCount = 0;
        List<String> toc = new ArrayList<>();
        StringBuilder rawText = new StringBuilder("POLICY_PACKET:").append(period.getPolicyNumber()).append(":");

        for (PolicyForm f : formList) {
            if (f.isMandatory()) {
                mandatoryCount++;
            } else {
                optionalCount++;
            }
            String entry = f.getFormNumber() + " (" + f.getEditionDate() + ") - " + f.getFormName();
            toc.add(entry);
            rawText.append(f.getFormNumber()).append(";");
        }

        String hash = computeSHA256(rawText.toString());

        packet.put("PolicyNumber", period.getPolicyNumber());
        packet.put("TotalFormsCount", formList.size());
        packet.put("MandatoryFormsCount", mandatoryCount);
        packet.put("OptionalFormsCount", optionalCount);
        packet.put("TableOfContents", toc);
        packet.put("PacketChecksum", hash);
        packet.put("Status", "Generated");

        return packet;
    }

    private static String computeSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "HASH-ERR-001";
        }
    }
}
