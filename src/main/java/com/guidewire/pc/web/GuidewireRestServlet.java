package com.guidewire.pc.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.PolicyLifecycleService;
import com.guidewire.pc.service.RatingEngine;
import com.guidewire.pc.service.SearchService;
import com.guidewire.pc.security.SecurityUtils;
import com.guidewire.pc.security.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuidewireRestServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DataStoreService dataStore = DataStoreService.getInstance();

    private boolean isAuthenticated(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            if (SessionManager.getInstance().validateSession(token) != null) {
                return true;
            }
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) {
                    if (SessionManager.getInstance().validateSession(c.getValue()) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SecurityUtils.addSecurityHeaders(resp);
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (path == null || path.equals("/") || path.equals("/openapi.json")) {
            serveOpenApiJson(resp);
            return;
        }

        if (!isAuthenticated(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(), Map.of("error", "Unauthorized: Invalid or missing API session token"));
            return;
        }

        if (path.equals("/accounts")) {
            List<Account> accounts = dataStore.getAccounts();
            objectMapper.writeValue(resp.getWriter(), accounts);
            return;
        }

        if (path.equals("/jobs") || path.equals("/submissions")) {
            List<PolicyPeriod> submissions = dataStore.getSubmissions();
            objectMapper.writeValue(resp.getWriter(), submissions);
            return;
        }

        if (path.equals("/search")) {
            String q = req.getParameter("q");
            SearchService.SearchResult sr = SearchService.getInstance().executeSearch(q);
            Map<String, Object> result = new HashMap<>();
            result.put("query", sr.getQuery());
            result.put("resultType", sr.getResultType().name());
            result.put("targetUrl", sr.getTargetUrl());
            result.put("matchingAccountsCount", sr.getMatchingAccounts().size());
            result.put("matchingSubmissionsCount", sr.getMatchingSubmissions().size());
            if (sr.getDirectAccount() != null) {
                result.put("directAccount", sr.getDirectAccount());
            }
            if (sr.getDirectSubmission() != null) {
                result.put("directSubmission", sr.getDirectSubmission());
            }
            result.put("matchingAccounts", sr.getMatchingAccounts());
            result.put("matchingSubmissions", sr.getMatchingSubmissions());
            objectMapper.writeValue(resp.getWriter(), result);
            return;
        }

        if (path.startsWith("/documents/policy/")) {
            String jobNumber = path.substring("/documents/policy/".length()).replace("/binder.pdf", "");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period != null) {
                try {
                    byte[] pdfBytes = com.guidewire.pc.service.DocumentGenerator.getInstance().generatePolicyBinderPdf(period);
                    resp.setContentType("application/pdf");
                    resp.setHeader("Content-Disposition", "inline; filename=\"Policy_Binder_" + jobNumber + ".pdf\"");
                    resp.setContentLength(pdfBytes.length);
                    resp.getOutputStream().write(pdfBytes);
                    return;
                } catch (IOException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    objectMapper.writeValue(resp.getWriter(), Map.of("error", "Failed to generate PDF document: " + e.getMessage()));
                    return;
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Job not found for PDF generation: " + jobNumber));
                return;
            }
        }

        if (path.startsWith("/vin/decode/")) {
            String vin = path.substring("/vin/decode/".length());
            Map<String, Object> decoded = com.guidewire.pc.service.VinLookupService.getInstance().decodeVin(vin);
            objectMapper.writeValue(resp.getWriter(), decoded);
            return;
        }

        if (path.startsWith("/payments/schedule/")) {
            String jobNumber = path.substring("/payments/schedule/".length());
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal totalPrem = period != null && period.getTotalPremium() != null ? period.getTotalPremium() : new BigDecimal("2889.00");
            BigDecimal downPayment = totalPrem.multiply(new BigDecimal("0.20")).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal remaining = totalPrem.subtract(downPayment);
            BigDecimal monthly = remaining.divide(new BigDecimal("11"), 2, java.math.RoundingMode.HALF_UP);

            objectMapper.writeValue(resp.getWriter(), Map.of(
                    "jobNumber", jobNumber,
                    "totalPremium", totalPrem,
                    "downPayment", downPayment,
                    "monthlyInstallment", monthly,
                    "numberOfInstallments", 12
            ));
            return;
        }

        if (path.startsWith("/claims/")) {
            String policyNumber = path.substring("/claims/".length());
            var claims = com.guidewire.pc.service.ClaimCenterService.getInstance().getClaimsForPolicy(policyNumber);
            objectMapper.writeValue(resp.getWriter(), claims);
            return;
        }

        if (path.startsWith("/policies/") && path.endsWith("/invoice")) {
            String policyNumber = path.replace("/policies/", "").replace("/invoice", "");
            PolicyPeriod period = dataStore.findPolicyByPolicyNumber(policyNumber);
            var schedule = com.guidewire.pc.service.BillingCenterService.getInstance().generateSchedule(period, "FourPay");
            if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("text/html")) {
                resp.setContentType("text/html");
                resp.getWriter().write(com.guidewire.pc.service.BillingCenterService.getInstance().generateInvoiceHtml(period, schedule));
            } else {
                objectMapper.writeValue(resp.getWriter(), schedule);
            }
            return;
        }

        if (path.startsWith("/policies/") && path.endsWith("/diff")) {
            String policyNumber = path.replace("/policies/", "").replace("/diff", "");
            PolicyPeriod current = dataStore.findPolicyByPolicyNumber(policyNumber);
            String compareJob = req.getParameter("compareJob");
            PolicyPeriod base = compareJob != null ? dataStore.findSubmission(compareJob) : current;
            if (current != null && base != null) {
                var diffReport = com.guidewire.pc.service.PolicyDiffService.getInstance().compareRevisions(base, current);
                objectMapper.writeValue(resp.getWriter(), diffReport);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Policy revision not found for comparison"));
            }
            return;
        }

        if (path.equals("/dashboard/kpis")) {
            var kpis = com.guidewire.pc.service.UnderwritingDashboardService.getInstance().computeKpis();
            objectMapper.writeValue(resp.getWriter(), kpis);
            return;
        }

        if (path.equals("/system/java-diagnostics")) {
            var diagnostics = com.guidewire.pc.service.JVMDiagnosticsService.getInstance().getJVMDiagnostics();
            objectMapper.writeValue(resp.getWriter(), diagnostics);
            return;
        }

        if (path.equals("/webhooks")) {
            var events = com.guidewire.pc.service.WebhookPublisherService.getInstance().getEventLog();
            objectMapper.writeValue(resp.getWriter(), events);
            return;
        }

        if (path.startsWith("/jobs/")) {
            String jobNumber = path.substring("/jobs/".length());
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period != null) {
                objectMapper.writeValue(resp.getWriter(), period);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Job not found: " + jobNumber));
            }
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", "Endpoint not found: " + path));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SecurityUtils.addSecurityHeaders(resp);
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!isAuthenticated(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(), Map.of("error", "Unauthorized: Invalid or missing API session token"));
            return;
        }

        if ("/accounts".equals(path)) {
            Account newAccount = objectMapper.readValue(req.getInputStream(), Account.class);
            Account created = dataStore.createAccount(newAccount);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), created);
            return;
        }

        if ("/jobs".equals(path)) {
            PolicyPeriod newPeriod = objectMapper.readValue(req.getInputStream(), PolicyPeriod.class);
            PolicyPeriod created = dataStore.createSubmission(newPeriod);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), created);
            return;
        }

        if (path != null && path.endsWith("/quote")) {
            String jobNumber = path.replace("/jobs/", "").replace("/quote", "");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Job not found"));
                return;
            }

            RuleContext ruleCtx = RulesEngine.getInstance().evaluatePreQuoteRules(period);
            if (ruleCtx.hasErrors()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(), Map.of("status", "Failed", "errors", ruleCtx.getErrorMessages()));
                return;
            }

            RatingEngine.getInstance().rate(period);
            period.setStatus(com.guidewire.pc.constants.PCConstants.STATUS_QUOTED);
            objectMapper.writeValue(resp.getWriter(), Map.of(
                    "jobNumber", period.getJobNumber(),
                    "status", period.getStatus(),
                    "basePremium", period.getBasePremium(),
                    "totalPremium", period.getTotalPremium(),
                    "underwritingHold", ruleCtx.isUnderwritingHoldRequired(),
                    "warnings", ruleCtx.getWarningMessages()
            ));
            return;
        }

        if (path != null && path.endsWith("/issue")) {
            String jobNumber = path.replace("/jobs/", "").replace("/issue", "");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Job not found"));
                return;
            }

            RuleContext ruleCtx = RulesEngine.getInstance().evaluatePreBindRules(period);
            if (ruleCtx.hasErrors()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(), Map.of("status", "Failed", "errors", ruleCtx.getErrorMessages()));
                return;
            }

            if (period.getPolicyNumber() == null) {
                period.setPolicyNumber("POL-" + (System.currentTimeMillis() % 1000000));
            }
            period.setStatus("Issued");
            objectMapper.writeValue(resp.getWriter(), Map.of(
                    "jobNumber", period.getJobNumber(),
                    "policyNumber", period.getPolicyNumber(),
                    "status", period.getStatus(),
                    "formattedStatus", period.getFormattedStatus()
            ));
            return;
        }

        if (path != null && path.contains("/policies/") && path.endsWith("/change")) {
            String policyNumber = path.replace("/policies/", "").replace("/change", "");
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String editEff = (String) reqMap.get("editEffectiveDate");
            String biLimit = (String) reqMap.get("bodilyInjuryLimit");
            String collDeduct = (String) reqMap.get("collisionDeductible");
            PolicyPeriod changeJob = PolicyLifecycleService.getInstance().startPolicyChange(policyNumber, editEff, biLimit, collDeduct);
            objectMapper.writeValue(resp.getWriter(), changeJob);
            return;
        }

        if (path != null && path.contains("/policies/") && path.endsWith("/cancel")) {
            String policyNumber = path.replace("/policies/", "").replace("/cancel", "");
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String reason = (String) reqMap.getOrDefault("reason", "Underwriting Risk");
            String calcMethod = (String) reqMap.getOrDefault("calcMethod", "ProRata");
            String cancelEffDate = (String) reqMap.getOrDefault("effectiveDate", "");
            PolicyPeriod cancelled = PolicyLifecycleService.getInstance().cancelPolicy(policyNumber, reason, calcMethod, cancelEffDate);
            objectMapper.writeValue(resp.getWriter(), cancelled);
            return;
        }

        if (path != null && path.contains("/policies/") && path.endsWith("/reinstate")) {
            String policyNumber = path.replace("/policies/", "").replace("/reinstate", "");
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String reason = (String) reqMap.getOrDefault("reason", "Payment Resolved");
            PolicyPeriod reinstated = PolicyLifecycleService.getInstance().reinstatePolicy(policyNumber, reason);
            objectMapper.writeValue(resp.getWriter(), reinstated);
            return;
        }

        if (path != null && path.contains("/policies/") && path.endsWith("/renew")) {
            String policyNumber = path.replace("/policies/", "").replace("/renew", "");
            PolicyPeriod renewal = PolicyLifecycleService.getInstance().renewPolicy(policyNumber);
            objectMapper.writeValue(resp.getWriter(), renewal);
            return;
        }

        if (path != null && path.contains("/policies/") && path.endsWith("/copy")) {
            String jobNum = path.replace("/policies/", "").replace("/copy", "");
            PolicyPeriod copied = PolicyLifecycleService.getInstance().copySubmission(jobNum);
            objectMapper.writeValue(resp.getWriter(), copied);
            return;
        }

        if (path != null && path.equals("/gosu/eval")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String expression = (String) reqMap.get("expression");
            Object evalResult = com.guidewire.pc.gosu.GosuBridge.eval(expression);
            objectMapper.writeValue(resp.getWriter(), Map.of("expression", expression != null ? expression : "", "result", evalResult != null ? evalResult.toString() : "null"));
            return;
        }

        if (path != null && path.equals("/gosu/reload")) {
            com.guidewire.pc.gosu.GosuBridge.reloadScripts();
            objectMapper.writeValue(resp.getWriter(), Map.of("status", "Success", "message", "Gosu script directory hot-reloaded successfully."));
            return;
        }

        if (path != null && path.equals("/admin/reset-db")) {
            dataStore.resetToSeedData();
            objectMapper.writeValue(resp.getWriter(), Map.of("status", "Success", "message", "Database reset to clean sample seed data."));
        }
    }

    private void serveOpenApiJson(HttpServletResponse resp) throws IOException {
        Map<String, Object> openApi = new HashMap<>();
        openApi.put("openapi", "3.0.1");
        openApi.put("info", Map.of(
                "title", "Guidewire PolicyCenter Cloud REST API",
                "version", "1.0.0",
                "description", "OOTB Guidewire PolicyCenter Digital & REST Integration Framework"
        ));
        openApi.put("paths", Map.of(
                "/rest/v1/accounts", Map.of("get", Map.of("summary", "List all active accounts"), "post", Map.of("summary", "Create new account")),
                "/rest/v1/jobs", Map.of("get", Map.of("summary", "List all policy submissions/jobs"), "post", Map.of("summary", "Create new submission")),
                "/rest/v1/jobs/{jobNumber}/quote", Map.of("post", Map.of("summary", "Rate and Quote policy job")),
                "/rest/v1/jobs/{jobNumber}/issue", Map.of("post", Map.of("summary", "Bind and Issue policy job"))
        ));
        objectMapper.writeValue(resp.getWriter(), openApi);
    }
}
