package com.guidewire.pc;

import com.guidewire.pc.web.JettyPolicyCenterServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoginFlowTest {

    private static JettyPolicyCenterServer server;
    private static final int PORT = 9199;
    private static HttpClient client;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new JettyPolicyCenterServer(PORT, new File("."));
        server.start();
        client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFormUrlencodedLoginSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=su&password=gw"))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp.statusCode());
        assertTrue(resp.headers().firstValue("Location").orElse("").contains("page=desktop"));
        List<String> cookies = resp.headers().allValues("Set-Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.contains("SESSIONID=")));
    }

    @Test
    public void testJsonLoginSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"su\",\"password\":\"gw\"}"))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("SUCCESS"));
        assertTrue(resp.body().contains("token"));
    }

    @Test
    public void testRestApiLoginEndpointSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/rest/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"admin\",\"password\":\"gw\"}"))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("SUCCESS"));
        assertTrue(resp.body().contains("token"));
    }

    @Test
    public void testInvalidLoginCredentials() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/api/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=su&password=wrongpassword"))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp.statusCode());
        assertTrue(resp.headers().firstValue("Location").orElse("").contains("error=invalid"));
    }
}
