package com.guidewire.pc;

import com.guidewire.pc.security.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecuritySessionTest {

    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearAllSessions();
        sessionManager.setSessionTimeoutMs(30 * 60 * 1000L);
    }

    @Test
    public void testCreateAndValidateSession() {
        String token = sessionManager.createSession("su");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        SessionManager.UserSession session = sessionManager.validateSession(token);
        assertNotNull(session);
        assertEquals("su", session.getUsername());
        assertEquals(1, sessionManager.getActiveSessionCount());
    }

    @Test
    public void testInvalidateSession() {
        String token = sessionManager.createSession("su");
        assertTrue(sessionManager.invalidateSession(token));
        assertNull(sessionManager.validateSession(token));
        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    public void testSessionExpiration() throws InterruptedException {
        sessionManager.setSessionTimeoutMs(100L); // 100ms timeout for fast testing
        String token = sessionManager.createSession("su");

        assertNotNull(sessionManager.validateSession(token));
        Thread.sleep(150L);

        assertNull(sessionManager.validateSession(token));
        assertEquals(0, sessionManager.getActiveSessionCount());
    }

    @Test
    public void testInvalidTokenHandling() {
        assertNull(sessionManager.validateSession(null));
        assertNull(sessionManager.validateSession(""));
        assertNull(sessionManager.validateSession("   "));
        assertNull(sessionManager.validateSession("invalid-token-12345"));
        assertFalse(sessionManager.invalidateSession("non-existent-token"));
    }

    @Test
    public void testNullOrEmptyUsernameRejection() {
        assertThrows(IllegalArgumentException.class, () -> sessionManager.createSession(null));
        assertThrows(IllegalArgumentException.class, () -> sessionManager.createSession("   "));
    }
}
