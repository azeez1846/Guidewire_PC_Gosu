package com.guidewire.pc;

import com.guidewire.pc.security.SessionManager;
import com.guidewire.pc.web.JettyPolicyCenterServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class RestApiSecurityTest {

    private static JettyPolicyCenterServer server;
    private static final int PORT = 9099;
    private static HttpClient client;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new JettyPolicyCenterServer(PORT, new File("."));
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testUnauthenticatedRestApiAccessDenied() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/rest/v1/accounts"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
        assertTrue(resp.body().contains("Unauthorized"));
    }

    @Test
    public void testAuthenticatedRestApiAccessGranted() throws Exception {
        String token = SessionManager.getInstance().createSession("su");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/rest/v1/accounts"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("accountNumber"));

        // Verify security response headers
        assertTrue(resp.headers().firstValue("X-Content-Type-Options").isPresent());
        assertEquals("nosniff", resp.headers().firstValue("X-Content-Type-Options").get());
        assertTrue(resp.headers().firstValue("X-Frame-Options").isPresent());
        assertEquals("SAMEORIGIN", resp.headers().firstValue("X-Frame-Options").get());
    }

    @Test
    public void testOpenApiJsonUnauthenticatedAccessAllowed() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/rest/v1/openapi.json"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("openapi"));
    }

    @Test
    public void testInvalidTokenRestApiAccessDenied() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/rest/v1/accounts"))
                .header("Authorization", "Bearer invalid-token-xyz")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, resp.statusCode());
    }
}
