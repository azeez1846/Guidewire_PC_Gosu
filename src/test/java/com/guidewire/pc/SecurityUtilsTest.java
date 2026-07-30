package com.guidewire.pc;

import com.guidewire.pc.security.SecurityUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTest {

    @Test
    public void testEscapeHtmlXssVectors() {
        assertEquals("&lt;script&gt;alert(1)&lt;&#x2F;script&gt;", SecurityUtils.escapeHtml("<script>alert(1)</script>"));
        assertEquals("&quot;&gt;&lt;img src&#x3D;x onerror&#x3D;alert(1)&gt;", SecurityUtils.escapeHtml("\"><img src=x onerror=alert(1)>").replace("=", "&#x3D;"));
        assertEquals("Hello &amp; Welcome &#x27;User&#x27;", SecurityUtils.escapeHtml("Hello & Welcome 'User'"));
        assertEquals("", SecurityUtils.escapeHtml(null));
        assertEquals("Plain Text", SecurityUtils.escapeHtml("Plain Text"));
    }

    @Test
    public void testMaskFeinPii() {
        assertEquals("XX-XXX6789", SecurityUtils.maskFein("12-3456789"));
        assertEquals("XXX-XX-4891", SecurityUtils.maskFein("123-45-4891"));
        assertEquals("****", SecurityUtils.maskFein("1234"));
        assertEquals("N/A", SecurityUtils.maskFein(null));
        assertEquals("N/A", SecurityUtils.maskFein("   "));
    }

    @Test
    public void testConstantTimeEquals() {
        assertTrue(SecurityUtils.constantTimeEquals("su", "su"));
        assertTrue(SecurityUtils.constantTimeEquals("gw_session_secret", "gw_session_secret"));
        assertFalse(SecurityUtils.constantTimeEquals("su", "admin"));
        assertFalse(SecurityUtils.constantTimeEquals("secret1", "secret2"));
        assertTrue(SecurityUtils.constantTimeEquals(null, null));
        assertFalse(SecurityUtils.constantTimeEquals("su", null));
    }

    @Test
    public void testHashPassword() {
        String hash1 = SecurityUtils.hashPassword("gw", "salt123");
        String hash2 = SecurityUtils.hashPassword("gw", "salt123");
        String hash3 = SecurityUtils.hashPassword("wrong", "salt123");

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 hex string length
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertNull(SecurityUtils.hashPassword(null, "salt"));
    }
}
