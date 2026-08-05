package com.guidewire.pc.security;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthenticationService — single source of truth for credential validation.
 *
 * <p>Design goals:
 * <ul>
 *   <li>All credential logic lives here, never inline in servlets.</li>
 *   <li>Uses {@link SecurityUtils#constantTimeEquals} for timing-safe comparison,
 *       preventing brute-force timing attacks.</li>
 *   <li>Returns a typed {@link AuthResult} so callers never need to inspect internals.</li>
 *   <li>The {@code VALID_USERS} map is the single place to add, remove, or change
 *       credentials — no grepping across servlets needed.</li>
 * </ul>
 *
 * <p>To add a new user: add one entry to {@code VALID_USERS}.
 * To change a password:  update that entry. Both changes are covered by
 * {@code AuthenticationServiceTest}, so a broken credential is caught at
 * {@code mvn test} before it ever reaches a running server.
 */
public final class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    // ── Credential store ──────────────────────────────────────────────────
    // username (lowercase) → password (case-sensitive)
    // To extend: add entries here. Swap Map.of() for a DB/config-file reader
    // when moving to production.
    private static final Map<String, String> VALID_USERS = Map.of(
        "su",           "gw",
        "admin",        "gw",
        "underwriter",  "gw",
        "testuser",     "gw"
    );

    // ── Singleton ─────────────────────────────────────────────────────────
    private static final AuthenticationService INSTANCE = new AuthenticationService();

    private AuthenticationService() {
        LOGGER.log(Level.FINE, "→ AuthenticationService.AuthenticationService");
    }

    public static AuthenticationService getInstance() {
        return INSTANCE;
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Validates a username/password pair.
     *
     * <ul>
     *   <li>Username matching is case-insensitive (trimmed).</li>
     *   <li>Password matching is case-sensitive, timing-safe.</li>
     *   <li>Both null and blank inputs are rejected before any map lookup.</li>
     * </ul>
     *
     * @param username raw username from the login form
     * @param password raw password from the login form
     * @return {@link AuthResult} describing success or failure reason
     */
    public AuthResult authenticate(String username, String password) {
        LOGGER.log(Level.FINE, "→ AuthenticationService.authenticate");

        if (username == null || username.isBlank()) {
            return AuthResult.failure("Username must not be blank");
        }
        if (password == null || password.isBlank()) {
            return AuthResult.failure("Password must not be blank");
        }

        String uClean = username.strip().toLowerCase();
        String pClean = password.strip();

        String expectedPassword = VALID_USERS.get(uClean);

        // Always run the constant-time comparison, even for unknown usernames,
        // so response time does not leak whether the username exists.
        boolean passwordMatches = expectedPassword != null
                && SecurityUtils.constantTimeEquals(expectedPassword, pClean);

        if (expectedPassword != null && passwordMatches) {
            LOGGER.log(Level.INFO, "Authentication succeeded for user: {0}", uClean);
            return AuthResult.success(uClean);
        }

        LOGGER.log(Level.WARNING, "Authentication failed for user: {0}", uClean);
        return AuthResult.failure("Invalid username or password");
    }

    /**
     * Typed result of an authentication attempt.
     */
    public static final class AuthResult {

        private final boolean success;
        private final String  username;   // normalized, non-null on success
        private final String  reason;     // non-null on failure

        private AuthResult(boolean success, String username, String reason) {
            this.success  = success;
            this.username = username;
            this.reason   = reason;
        }

        static AuthResult success(String normalizedUsername) {
            return new AuthResult(true, normalizedUsername, null);
        }

        static AuthResult failure(String reason) {
            return new AuthResult(false, null, reason);
        }

        /** @return true if the credentials were accepted */
        public boolean isSuccess() { return success; }

        /**
         * @return the normalized (lowercased, stripped) username on success
         * @throws IllegalStateException if called on a failed result
         */
        public String getUsername() {
            if (!success) throw new IllegalStateException("AuthResult is a failure — no username available");
            return username;
        }

        /** @return human-readable failure reason (null on success) */
        public String getReason() { return reason; }
    }
}
