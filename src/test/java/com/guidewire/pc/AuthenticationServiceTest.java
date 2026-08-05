package com.guidewire.pc;

import com.guidewire.pc.security.AuthenticationService;
import com.guidewire.pc.security.AuthenticationService.AuthResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthenticationServiceTest — regression guard for the authentication layer.
 *
 * <p>Every test here corresponds to a previously-broken or historically-risky
 * behaviour. If any test here turns red, the login system is broken — do NOT
 * ship until it is green.
 *
 * <p>Adding a new credential to AuthenticationService.VALID_USERS? Add a test here.
 */
public class AuthenticationServiceTest {

    private final AuthenticationService auth = AuthenticationService.getInstance();

    // ── Valid credential tests ────────────────────────────────────────────

    @Test
    void testSuWithCorrectPasswordSucceeds() {
        AuthResult r = auth.authenticate("su", "gw");
        assertTrue(r.isSuccess(), "su/gw must be accepted");
        assertEquals("su", r.getUsername());
    }

    @Test
    void testAdminWithCorrectPasswordSucceeds() {
        AuthResult r = auth.authenticate("admin", "gw");
        assertTrue(r.isSuccess(), "admin/gw must be accepted");
        assertEquals("admin", r.getUsername());
    }

    @Test
    void testUnderwriterWithCorrectPasswordSucceeds() {
        AuthResult r = auth.authenticate("underwriter", "gw");
        assertTrue(r.isSuccess(), "underwriter/gw must be accepted");
        assertEquals("underwriter", r.getUsername());
    }

    @Test
    void testTestuserWithCorrectPasswordSucceeds() {
        AuthResult r = auth.authenticate("testuser", "gw");
        assertTrue(r.isSuccess(), "testuser/gw must be accepted");
        assertEquals("testuser", r.getUsername());
    }

    // ── Username normalisation ────────────────────────────────────────────

    @Test
    void testUsernameIsCaseInsensitive() {
        assertTrue(auth.authenticate("SU", "gw").isSuccess(), "Uppercase SU should be normalised");
        assertTrue(auth.authenticate("Admin", "gw").isSuccess(), "Mixed-case Admin should be normalised");
        assertEquals("su", auth.authenticate("SU", "gw").getUsername(), "getUsername() should return lowercase");
    }

    @Test
    void testLeadingAndTrailingWhitespaceIsStripped() {
        assertTrue(auth.authenticate("  su  ", "gw").isSuccess(), "Whitespace around username should be stripped");
        assertTrue(auth.authenticate("su", "  gw  ").isSuccess(), "Whitespace around password should be stripped");
    }

    // ── Wrong-password tests ──────────────────────────────────────────────

    @Test
    void testWrongPasswordFails() {
        // Root cause of the original bug: password was only checked for !isEmpty()
        // This test permanently guards against that regression.
        AuthResult r = auth.authenticate("su", "wrongpassword");
        assertFalse(r.isSuccess(), "su with wrong password MUST be rejected — this was the original bug");
        assertNotNull(r.getReason());
    }

    @Test
    void testEmptyPasswordFails() {
        assertFalse(auth.authenticate("su", "").isSuccess(), "Empty password must be rejected");
        assertFalse(auth.authenticate("su", "   ").isSuccess(), "Blank password must be rejected");
    }

    @Test
    void testAnyNonEmptyPasswordNoLongerAccepted() {
        // Regression: old code was `!pClean.isEmpty()` — any password worked.
        assertFalse(auth.authenticate("su", "a").isSuccess());
        assertFalse(auth.authenticate("su", "password").isSuccess());
        assertFalse(auth.authenticate("su", "GW").isSuccess(),  "Password check is case-sensitive");
        assertFalse(auth.authenticate("su", "Gw").isSuccess());
    }

    // ── Unknown username tests ────────────────────────────────────────────

    @Test
    void testUnknownUsernameFails() {
        assertFalse(auth.authenticate("root", "gw").isSuccess());
        assertFalse(auth.authenticate("administrator", "gw").isSuccess());
        assertFalse(auth.authenticate("system", "gw").isSuccess());
    }

    // ── Null / blank input tests ──────────────────────────────────────────

    @Test
    void testNullInputsAreRejected() {
        assertFalse(auth.authenticate(null, "gw").isSuccess(),   "null username must be rejected");
        assertFalse(auth.authenticate("su", null).isSuccess(),   "null password must be rejected");
        assertFalse(auth.authenticate(null, null).isSuccess(),   "both null must be rejected");
    }

    @Test
    void testBlankInputsAreRejected() {
        assertFalse(auth.authenticate("", "gw").isSuccess());
        assertFalse(auth.authenticate("   ", "gw").isSuccess());
    }

    // ── Injection / adversarial input tests ──────────────────────────────

    @Test
    void testSqlInjectionAttemptFails() {
        assertFalse(auth.authenticate("su' OR '1'='1", "gw").isSuccess());
        assertFalse(auth.authenticate("su", "gw' OR '1'='1").isSuccess());
        assertFalse(auth.authenticate("' OR 1=1 --", "anything").isSuccess());
    }

    @Test
    void testXssAttemptFails() {
        assertFalse(auth.authenticate("<script>alert(1)</script>", "gw").isSuccess());
        assertFalse(auth.authenticate("su", "<img src=x onerror=alert(1)>").isSuccess());
    }

    // ── Backdoor regression test ──────────────────────────────────────────

    @Test
    void testHardcodedBackdoorSessionIdsDoNotAuthenticate() {
        // The old SessionManager had a backdoor: validateSession("su") auto-created a session.
        // AuthenticationService must not grant access for these magic strings as passwords.
        assertFalse(auth.authenticate("su", "su").isSuccess(),             "Magic session ID 'su' must not be a valid password");
        assertFalse(auth.authenticate("su", "gw_su_session").isSuccess(), "Magic session ID must not be a valid password");
    }

    // ── AuthResult API contract ───────────────────────────────────────────

    @Test
    void testAuthResultThrowsOnGetUsernameWhenFailed() {
        AuthResult r = auth.authenticate("unknown", "badpass");
        assertFalse(r.isSuccess());
        IllegalStateException ex = assertThrows(IllegalStateException.class, r::getUsername,
                "getUsername() must throw on a failed AuthResult");
        assertNotNull(ex);
    }

    @Test
    void testAuthResultHasReasonOnFailure() {
        AuthResult r = auth.authenticate("su", "wrongpassword");
        assertFalse(r.isSuccess());
        assertNotNull(r.getReason(), "Failed AuthResult must carry a reason string");
        assertFalse(r.getReason().isBlank());
    }

    @Test
    void testAuthResultHasNullReasonOnSuccess() {
        AuthResult r = auth.authenticate("su", "gw");
        assertTrue(r.isSuccess());
        assertNull(r.getReason(), "Successful AuthResult must have null reason");
    }
}
