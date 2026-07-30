# Guidewire PolicyCenter Application Security Hardening Guide

## Executive Summary
This document serves as the architectural reference for the security hardening implemented in the **Guidewire PolicyCenter Gosu Application**. The application now features dynamic cryptographic session token management, REST API token authentication, localhost-only H2 Web Console isolation, Reflected/Stored XSS prevention, HTTP security response headers, and PII masking.

---

## 1. Authentication & Session Management (`SessionManager`)

### Key Architecture
- **Class Location**: [`com.guidewire.pc.security.SessionManager`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/security/SessionManager.java)
- **Token Generation**: Cryptographically secure 128-bit UUID strings (`UUID.randomUUID()`).
- **Session Store**: Thread-safe concurrent map (`ConcurrentHashMap`) maintaining active session metadata.
- **Session Expiration**: Automatic 30-minute idle session timeout. Expired sessions are automatically purged during validation.
- **Cookie Security**:
  - Cookie Name: `SESSIONID`
  - Attributes: `HttpOnly=true`, `Path=/`, `SameSite=Strict`
  - MaxAge: Cleared on `/api/logout` and invalidated server-side.

### Usage Example
```java
// Create a new session on login
String token = SessionManager.getInstance().createSession("su");

// Validate session from request
UserSession session = SessionManager.getInstance().validateSession(token);

// Explicit logout / invalidation
SessionManager.getInstance().invalidateSession(token);
```

---

## 2. REST API Protection (`GuidewireRestServlet`)

### Key Features
- **File Location**: [`com.guidewire.pc.web.GuidewireRestServlet`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/web/GuidewireRestServlet.java)
- **Protected Endpoints**: `/rest/v1/accounts`, `/rest/v1/jobs`, `/rest/v1/documents/policy/*`, `/rest/v1/jobs/{jobNumber}/quote`, `/rest/v1/jobs/{jobNumber}/issue`.
- **Public Metadata**: `/rest/v1/openapi.json` remains accessible for API discovery.
- **Authentication Credentials**:
  1. HTTP Header: `Authorization: Bearer <session_token>`
  2. Browser Cookie: `SESSIONID=<session_token>`
- **Unauthenticated Response**: Returns HTTP `401 Unauthorized` with JSON error payload:
  ```json
  {
    "error": "Unauthorized: Invalid or missing API session token"
  }
  ```

---

## 3. Defense-in-Depth & Security Utilities (`SecurityUtils`)

### Key Features
- **File Location**: [`com.guidewire.pc.security.SecurityUtils`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/security/SecurityUtils.java)
- **XSS Sanitization**: `SecurityUtils.escapeHtml(input)` escapes HTML/XML special characters (`<`, `>`, `&`, `"`, `'`, `/`) before rendering dynamic content into HTML templates.
- **PII Data Masking**: `SecurityUtils.maskFein(fein)` masks FEIN / SSN numbers (e.g. `12-3456789` -> `XX-XXX6789`).
- **Timing Attack Defense**: `SecurityUtils.constantTimeEquals(a, b)` compares sensitive tokens and passwords using `MessageDigest.isEqual` to prevent side-channel timing attacks.
- **HTTP Security Response Headers**:
  - `Content-Security-Policy`: Restricts resource loading to trusted origins.
  - `X-Content-Type-Options: nosniff`: Prevents MIME-sniffing attacks.
  - `X-Frame-Options: SAMEORIGIN`: Prevents Clickjacking attacks.
  - `X-XSS-Protection: 1; mode=block`: Enables browser XSS filters.
  - `Referrer-Policy: strict-origin-when-cross-origin`: Controls referrer headers.

---

## 4. H2 Database Web Console Hardening

### Key Changes
- **File Location**: [`com.guidewire.pc.web.JettyPolicyCenterServer`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/main/java/com/guidewire/pc/web/JettyPolicyCenterServer.java)
- **Security Fix**: Removed the `-webAllowOthers` flag when initializing the H2 console server.
- **Impact**: The database console on port 8082 is strictly bound to `127.0.0.1` (localhost), eliminating remote network access and Remote Code Execution (RCE) exposure.

---

## 5. Automated Test Suite

| Test Suite | Target | Coverage |
| :--- | :--- | :--- |
| [`SecuritySessionTest`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/SecuritySessionTest.java) | `SessionManager` | Session creation, expiration, invalidation, multi-session tracking, null/invalid token handling. |
| [`SecurityUtilsTest`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/SecurityUtilsTest.java) | `SecurityUtils` | HTML XSS vector escaping, PII FEIN masking, constant-time equality, password hashing. |
| [`RestApiSecurityTest`](file:///Users/azeezmohiuddin/Downloads/Guidewire_PC_Gosu/src/test/java/com/guidewire/pc/RestApiSecurityTest.java) | `GuidewireRestServlet` & Jetty | Unauthenticated 401 response verification, valid Bearer token 200 response, security header presence, public OpenAPI endpoint. |

### Running Tests
Execute via Maven:
```bash
mvn test
```
All 36 unit and integration tests pass cleanly with 0 failures and 0 errors.
