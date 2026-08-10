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

public class PageNavigationIntegrationTest {

    private static JettyPolicyCenterServer server;
    private static final int PORT = 9299;
    private static HttpClient client;
    private static String sessionToken;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new JettyPolicyCenterServer(PORT, new File("."));
        server.start();
        client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        sessionToken = SessionManager.getInstance().createSession("su");
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private HttpResponse<String> fetchPage(String pathOrQuery) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + pathOrQuery))
                .header("Cookie", "SESSIONID=" + sessionToken)
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void testFeaturesPageQueryParam() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=features");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("EnterpriseFeaturesSuite.pcf"));
        assertTrue(resp.body().contains("FEATURES"));
    }

    @Test
    public void testFeaturesPageDirectPath() throws Exception {
        HttpResponse<String> resp = fetchPage("/features");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("EnterpriseFeaturesSuite.pcf"));
    }

    @Test
    public void testDesktopPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=desktop");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Guidewire PolicyCenter"));
    }

    @Test
    public void testUWIssuesPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=uw-issues");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Underwriting Issues"));
    }

    @Test
    public void testFraudDashboardPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=fraud-dashboard");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Fraud"));
    }

    @Test
    public void testReinsuranceLedgerPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=reinsurance-ledger");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Reinsurance"));
    }

    @Test
    public void testInlandMarinePage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=inland-marine");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Inland Marine"));
    }

    @Test
    public void testDashboardPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/?page=dashboard");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("PolicyCenter Intelligence & Analytics"));
    }

    @Test
    public void testSmartPortalStaticPage() throws Exception {
        HttpResponse<String> resp = fetchPage("/portal.html");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("PolicyCenter Portal"));
    }
}
