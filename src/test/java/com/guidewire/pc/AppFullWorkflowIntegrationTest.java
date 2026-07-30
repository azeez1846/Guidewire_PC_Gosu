package com.guidewire.pc;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppFullWorkflowIntegrationTest {

    private static final String BASE_URL = "http://localhost:8085";

    @Test
    public void testFullApplicationWorkflowEndToEnd() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new java.net.CookieManager())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        // 1. Test Unauthenticated Access -> Redirect to Login Page
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop"))
                .GET()
                .build();
        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp1.statusCode());
        assertTrue(resp1.headers().firstValue("Location").orElse("").contains("page=login"));

        // 2. Test Invalid Login Credentials -> Redirect with Error
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=wrong&password=invalid"))
                .build();
        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp2.statusCode());
        assertTrue(resp2.headers().firstValue("Location").orElse("").contains("error=invalid"));

        // 3. Test Valid Login Credentials -> Receives Secure Session Cookie & Redirect to Desktop
        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=su&password=gw"))
                .build();
        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp3.statusCode());
        assertTrue(resp3.headers().firstValue("Location").orElse("").contains("page=desktop"));

        List<String> cookies = resp3.headers().allValues("Set-Cookie");
        assertFalse(cookies.isEmpty());
        String sessionCookie = cookies.stream().filter(c -> c.startsWith("SESSIONID=")).findFirst().orElse("");
        assertFalse(sessionCookie.isEmpty());
        assertTrue(sessionCookie.contains("HttpOnly"));

        String sessionToken = sessionCookie.split(";")[0];

        // 4. Test Desktop Navigation Tabs
        // 4a. Desktop - Submissions Tab
        HttpRequest reqSub = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop&tab=submissions"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respSub = client.send(reqSub, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respSub.statusCode());
        assertTrue(respSub.body().contains("Submissions &amp; Policy Transactions"));

        // 4b. Desktop - Activities Tab
        HttpRequest reqAct = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop&tab=activities"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respAct = client.send(reqAct, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respAct.statusCode());
        assertTrue(respAct.body().contains("Pending Underwriting Activities"));

        // 4c. Desktop - Accounts Tab
        HttpRequest reqAcc = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop&tab=accounts"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respAcc = client.send(reqAcc, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respAcc.statusCode());
        assertTrue(respAcc.body().contains("Accounts Directory"));

        // 5. Test QuickJump Search & XSS Vector Protection
        HttpRequest reqXss = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop&q=%3Cscript%3Ealert(1)%3C%2Fscript%3E"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respXss = client.send(reqXss, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respXss.statusCode());
        assertTrue(respXss.body().contains("&lt;script&gt;alert(1)&lt;&#x2F;script&gt;"));
        assertFalse(respXss.body().contains("<script>alert(1)</script>"));

        // 6. Test New Account Creation Workflow
        HttpRequest reqCreateAcc = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=new-account"))
                .header("Cookie", sessionToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "action=create&accountHolderName=Cybernetic+Labs+Inc&accountHolderType=Company&fein=88-9900112&producerCode=PR-10928&addressLine1=700+Cyber+Way&city=Austin&state=TX&postalCode=78701"))
                .build();
        HttpResponse<String> respCreateAcc = client.send(reqCreateAcc, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, respCreateAcc.statusCode());
        String accRedirect = respCreateAcc.headers().firstValue("Location").orElse("");
        assertTrue(accRedirect.contains("page=account-detail"));

        // Follow redirect to view newly created account detail
        String accTarget = accRedirect.startsWith("http") ? accRedirect : BASE_URL + accRedirect;
        HttpRequest reqAccDetail = HttpRequest.newBuilder()
                .uri(URI.create(accTarget))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respAccDetail = client.send(reqAccDetail, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respAccDetail.statusCode());
        assertTrue(respAccDetail.body().contains("Cybernetic Labs Inc"));

        // 7. Test New Policy Submission & Rating Engine Workflow
        HttpRequest reqCreateSub = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=new-submission"))
                .header("Cookie", sessionToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("action=create&accNum=A0001001&productCode=CommercialAuto&effectiveDate=2026-09-01&baseState=CA"))
                .build();
        HttpResponse<String> respCreateSub = client.send(reqCreateSub, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, respCreateSub.statusCode());
        String wizardRedirect = respCreateSub.headers().firstValue("Location").orElse("");
        assertTrue(wizardRedirect.contains("page=submission-wizard"));

        // Step 1 -> Step 2
        String wizardTarget = wizardRedirect.startsWith("http") ? wizardRedirect : BASE_URL + wizardRedirect;
        HttpRequest reqStep1 = HttpRequest.newBuilder()
                .uri(URI.create(wizardTarget))
                .header("Cookie", sessionToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("action=updateStep1&effectiveDate=2026-09-01&expirationDate=2027-09-01&termMonths=12&baseState=CA&producerCode=PR-10928"))
                .build();
        HttpResponse<String> respStep1 = client.send(reqStep1, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respStep1.statusCode());
        assertTrue(respStep1.body().contains("Step 2: Line Coverages &amp; Deductibles"));

        // Step 2 -> Rating Engine & Quote Generation
        String jobNum = wizardRedirect.replaceAll(".*jobNum=([^&]+).*", "$1");
        HttpRequest reqQuote = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=submission-wizard&jobNum=" + jobNum))
                .header("Cookie", sessionToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("action=quote&bodilyInjuryLimit=$500k/$500k&propertyDamageLimit=$250k&comprehensiveDeductible=$500&collisionDeductible=$1000"))
                .build();
        HttpResponse<String> respQuote = client.send(reqQuote, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respQuote.statusCode());
        assertTrue(respQuote.body().contains("Step 3: Rating Breakdown &amp; Financial Summary"));
        assertTrue(respQuote.body().contains("Quoted"));

        // Step 3 -> Bind & Issue Policy
        HttpRequest reqBind = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=submission-wizard&jobNum=" + jobNum))
                .header("Cookie", sessionToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("action=bind"))
                .build();
        HttpResponse<String> respBind = client.send(reqBind, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respBind.statusCode());
        assertTrue(respBind.body().contains("Issued"));

        // 8. Test Logout Flow -> Invalidates Session & Clears Cookie
        HttpRequest reqLogout = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/logout"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respLogout = client.send(reqLogout, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, respLogout.statusCode());
        assertTrue(respLogout.headers().firstValue("Location").orElse("").contains("page=login"));

        // Verify session token is no longer valid after logout
        HttpRequest reqPostLogoutAccess = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?page=desktop"))
                .header("Cookie", sessionToken)
                .GET()
                .build();
        HttpResponse<String> respPostLogout = client.send(reqPostLogoutAccess, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, respPostLogout.statusCode());
        assertTrue(respPostLogout.headers().firstValue("Location").orElse("").contains("page=login"));
    }
}
