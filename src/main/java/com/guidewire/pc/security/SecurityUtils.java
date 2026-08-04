package com.guidewire.pc.security;

import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SecurityUtils {
    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());


    /**
     * Sanitizes user input string for safe HTML context rendering (XSS Protection).
     */
    public static String escapeHtml(String input) {
        LOGGER.log(Level.FINE, "→ SecurityUtils.escapeHtml");
        if (input == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '&' -> builder.append("&amp;");
                case '"' -> builder.append("&quot;");
                case '\'' -> builder.append("&#x27;");
                case '/' -> builder.append("&#x2F;");
                default -> builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * Masks sensitive PII Tax ID / FEIN / SSN numbers for secure display.
     * e.g., "12-3456789" -> "XX-XXX6789"
     */
    public static String maskFein(String fein) {
        LOGGER.log(Level.FINE, "→ SecurityUtils.maskFein");
        if (fein == null || fein.trim().isEmpty()) {
            return "N/A";
        }
        String clean = fein.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        int visibleDigits = 4;
        StringBuilder masked = new StringBuilder();
        int unmaskedCount = 0;

        for (int i = clean.length() - 1; i >= 0; i--) {
            char c = clean.charAt(i);
            if (Character.isDigit(c)) {
                if (unmaskedCount < visibleDigits) {
                    masked.insert(0, c);
                    unmaskedCount++;
                } else {
                    masked.insert(0, 'X');
                }
            } else {
                masked.insert(0, c);
            }
        }
        return masked.toString();
    }

    /**
     * Performs a constant-time string comparison to prevent timing attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        LOGGER.log(Level.FINE, "→ SecurityUtils.constantTimeEquals");
        if (a == null || b == null) {
            return java.util.Objects.equals(a, b);
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    /**
     * Generates SHA-256 hash of a password string with salt.
     */
    public static String hashPassword(String password, String salt) {
        LOGGER.log(Level.FINE, "→ SecurityUtils.hashPassword");
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = (salt != null ? salt : "") + password;
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    /**
     * Applies standard HTTP security headers to outgoing responses.
     */
    public static void addSecurityHeaders(HttpServletResponse response) {
        LOGGER.log(Level.FINE, "→ SecurityUtils.addSecurityHeaders");
        if (response == null) return;
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' https://unpkg.com; style-src 'self' 'unsafe-inline' https://unpkg.com; img-src 'self' data:;");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}
