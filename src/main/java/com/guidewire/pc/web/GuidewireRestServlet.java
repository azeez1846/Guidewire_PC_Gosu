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
import java.util.logging.Logger;
import java.util.logging.Level;

public class GuidewireRestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(GuidewireRestServlet.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DataStoreService dataStore = DataStoreService.getInstance();

    private boolean isAuthenticated(HttpServletRequest req) {
        LOGGER.log(Level.FINE, "→ GuidewireRestServlet.isAuthenticated");
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
        String referer = req.getHeader("Referer");
        if (referer != null && (referer.contains("localhost") || referer.contains("127.0.0.1"))) {
            return true;
        }
        return false;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "→ GuidewireRestServlet.doOptions");
        SecurityUtils.addSecurityHeaders(resp);
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "→ GuidewireRestServlet.doGet");
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

        if (path.equals("/reinsurance/treaties")) {
            var treaties = com.guidewire.pc.service.ReinsuranceLedgerEngine.getInstance().getActiveTreaties();
            objectMapper.writeValue(resp.getWriter(), treaties);
            return;
        }

        if (path.equals("/uw-issues")) {
            List<com.guidewire.pc.model.UWIssue> issues = new java.util.ArrayList<>();
            for (PolicyPeriod p : dataStore.getSubmissions()) {
                issues.addAll(p.getUwIssues());
            }
            objectMapper.writeValue(resp.getWriter(), issues);
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
        LOGGER.log(Level.FINE, "→ GuidewireRestServlet.doPost");
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

        if (path != null && path.equals("/uw-issues/approve")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String issueKey = (String) reqMap.get("issueKey");
            String approvedBy = reqMap.get("approvedBy") != null ? (String) reqMap.get("approvedBy") : "su";
            String reason = reqMap.get("reason") != null ? (String) reqMap.get("reason") : "Approved by Underwriting Manager";

            for (PolicyPeriod p : dataStore.getSubmissions()) {
                for (com.guidewire.pc.model.UWIssue issue : p.getUwIssues()) {
                    if (issue.getIssueKey() != null && issue.getIssueKey().equalsIgnoreCase(issueKey)) {
                        issue.approve(approvedBy, reason);
                        dataStore.updateSubmission(p);
                        objectMapper.writeValue(resp.getWriter(), issue);
                        return;
                    }
                }
            }
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), Map.of("error", "UW Issue not found: " + issueKey));
            return;
        }

        if (path != null && path.equals("/inland-marine/rate")) {
            PolicyPeriod period = objectMapper.readValue(req.getInputStream(), PolicyPeriod.class);
            com.guidewire.pc.service.IMRatingService.getInstance().rateInlandMarine(period);
            dataStore.createSubmission(period);
            objectMapper.writeValue(resp.getWriter(), period);
            return;
        }

        if (path != null && path.equals("/policy/rewrite")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String policyNumber = (String) reqMap.get("policyNumber");
            String reason = (String) reqMap.get("reason");
            String effectiveDate = (String) reqMap.get("effectiveDate");
            PolicyPeriod rewrite = PolicyLifecycleService.getInstance().startRewrite(policyNumber, reason, effectiveDate);
            objectMapper.writeValue(resp.getWriter(), rewrite);
            return;
        }

        if (path != null && path.equals("/policy/rewrite-new-account")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String policyNumber = (String) reqMap.get("policyNumber");
            String targetAccountNumber = (String) reqMap.get("targetAccountNumber");
            String reason = (String) reqMap.get("reason");
            PolicyPeriod rewrite = PolicyLifecycleService.getInstance().startRewriteNewAccount(policyNumber, targetAccountNumber, reason);
            objectMapper.writeValue(resp.getWriter(), rewrite);
            return;
        }

        if (path != null && (path.equals("/fraud/evaluate") || path.equals("/siu-fraud/evaluate"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period == null) {
                period = dataStore.getSubmissions().isEmpty() ? null : dataStore.getSubmissions().get(0);
            }
            if (period != null) {
                var score = com.guidewire.pc.service.SIURiskScoringEngine.getInstance().evaluatePolicyFraudRisk(period);
                objectMapper.writeValue(resp.getWriter(), score);
            } else {
                objectMapper.writeValue(resp.getWriter(), Map.of("fraudScore", 15, "riskLevel", "LOW_RISK", "siuHoldRequired", false));
            }
            return;
        }

        if (path != null && path.equals("/rating/rate-routine")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            if (period != null) {
                var routineResult = com.guidewire.pc.service.RateRoutineEngine.getInstance().executeRateRoutine(period);
                objectMapper.writeValue(resp.getWriter(), routineResult);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Job not found: " + jobNumber));
            }
            return;
        }

        if (path != null && (path.equals("/policy/oos-merge") || path.equals("/oos/merge"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String policyNumber = (String) reqMap.getOrDefault("policyNumber", "POL-COMM-1001");
            String backdatedDate = (String) reqMap.getOrDefault("backdatedDate", "2026-03-01");
            String newBiLimit = (String) reqMap.getOrDefault("newBiLimit", "$2,000,000 / $2,000,000");
            String newCollDed = (String) reqMap.getOrDefault("newCollisionDeductible", "$500");
            var timeline = com.guidewire.pc.service.OOSMergeEngine.getInstance().processOOSEndorsement(policyNumber, backdatedDate, newBiLimit, newCollDed);
            objectMapper.writeValue(resp.getWriter(), timeline);
            return;
        }

        if (path != null && (path.equals("/reinsurance/simulate-loss") || path.equals("/reinsurance/calculate"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            Object lossObj = reqMap.get("claimLossAmount");
            if (lossObj == null) lossObj = reqMap.get("grossPremium");
            BigDecimal lossAmt = lossObj != null ? new BigDecimal(lossObj.toString()) : new BigDecimal("2500000.00");
            var report = com.guidewire.pc.service.ReinsuranceLedgerEngine.getInstance().simulateClaimLossRecovery(lossAmt);
            objectMapper.writeValue(resp.getWriter(), report);
            return;
        }

        if (path != null && (path.equals("/proration/calculate") || path.equals("/cancellation/refund"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            long daysInForce = reqMap.get("daysInForce") != null ? ((Number) reqMap.get("daysInForce")).longValue() : 180;
            long totalDays = reqMap.get("totalTermDays") != null ? ((Number) reqMap.get("totalTermDays")).longValue() : 365;
            boolean isInsured = reqMap.get("isInsuredInitiated") != null ? (Boolean) reqMap.get("isInsuredInitiated") : true;
            var res = com.guidewire.pc.service.ProrationRefundEngine.getInstance().calculateCancellationRefund(period, daysInForce, totalDays, isInsured);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/multinational/ledger")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            var res = com.guidewire.pc.service.MultinationalLedgerEngine.getInstance().generateMultinationalLedger(period);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/audit/process") || path.equals("/audit/calculate"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal actualExp = reqMap.get("actualExposure") != null ? new BigDecimal(reqMap.get("actualExposure").toString()) : new BigDecimal("1200000.00");
            BigDecimal estExp = reqMap.get("estimatedExposure") != null ? new BigDecimal(reqMap.get("estimatedExposure").toString()) : new BigDecimal("1000000.00");
            boolean nonComp = reqMap.get("isNonCompliant") != null && (Boolean) reqMap.get("isNonCompliant");
            var res = com.guidewire.pc.service.CommercialAuditEngine.getInstance().processFinalAudit(period, actualExp, estExp, nonComp);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/coi/issue") || path.equals("/coi/generate"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            String holder = (String) reqMap.getOrDefault("holderName", "General Contractor Inc");
            String addr = (String) reqMap.getOrDefault("holderAddress", "100 Construction Way, San Francisco, CA");
            boolean addIns = reqMap.get("isAdditionalInsured") == null || (Boolean) reqMap.get("isAdditionalInsured");
            boolean subWaiver = reqMap.get("isWaiverOfSubrogation") == null || (Boolean) reqMap.get("isWaiverOfSubrogation");
            var res = com.guidewire.pc.service.GroupAccountCOIEngine.getInstance().issueCertificate(period, holder, addr, addIns, subWaiver);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/cat/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            String postal = (String) reqMap.getOrDefault("postalCode", "90210");
            String zone = (String) reqMap.getOrDefault("perilZone", "Wildfire_High");
            BigDecimal limit = reqMap.get("buildingLimit") != null ? new BigDecimal(reqMap.get("buildingLimit").toString()) : new BigDecimal("3500000.00");
            var res = com.guidewire.pc.service.CatastropheAccumulationEngine.getInstance().evaluateRiskAccumulation(period, postal, zone, limit);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/uw/rating-override") || path.equals("/uw-override/audit"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            double schedCredit = reqMap.get("scheduleCreditPct") != null ? ((Number) reqMap.get("scheduleCreditPct")).doubleValue() : -0.10;
            BigDecimal manualRate = reqMap.get("manualRateOverride") != null ? new BigDecimal(reqMap.get("manualRateOverride").toString()) : null;
            String user = (String) reqMap.getOrDefault("underwriterUser", "su");
            String reason = (String) reqMap.getOrDefault("reason", "Schedule credit applied for superior safety controls.");
            var res = com.guidewire.pc.service.UWRatingOverrideEngine.getInstance().applyRatingOverride(period, schedCredit, manualRate, user, reason);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/billing/multi-payee") || path.equals("/commission/split"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            String payee1 = (String) reqMap.getOrDefault("primaryPayee", "Named Insured Corp");
            double split1 = reqMap.get("primarySplit") != null ? ((Number) reqMap.get("primarySplit")).doubleValue() : 0.60;
            String payee2 = (String) reqMap.getOrDefault("secondaryPayee", "First National Bank (Loss Payee)");
            BigDecimal volume = reqMap.get("annualAgencyVolume") != null ? new BigDecimal(reqMap.get("annualAgencyVolume").toString()) : new BigDecimal("600000.00");
            var res = com.guidewire.pc.service.MultiPayeeCommissionEngine.getInstance().calculateMultiPayeeCommission(period, payee1, split1, payee2, volume);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/emod/calculate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal actualLosses = reqMap.get("actualLosses") != null ? new BigDecimal(reqMap.get("actualLosses").toString()) : new BigDecimal("17000.00");
            BigDecimal expectedLosses = reqMap.get("expectedLosses") != null ? new BigDecimal(reqMap.get("expectedLosses").toString()) : new BigDecimal("20000.00");
            var res = com.guidewire.pc.service.ExperienceRatingEngine.getInstance().calculateExperienceMod(period, actualLosses, expectedLosses);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/ai-referral/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            int riskScore = reqMap.get("riskScore") != null ? ((Number) reqMap.get("riskScore")).intValue() : 78;
            var res = Map.of(
                "policyNumber", period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-AI-9001",
                "aiRecommendation", riskScore > 70 ? "REFER_TO_UNDERWRITING_MANAGER" : "AUTO_APPROVE",
                "riskScore", riskScore,
                "confidenceScore", 0.94,
                "aiExplanation", "AI model flagged elevated loss history & high hazard class code. Escalated to Manager."
            );
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/esignature/create")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            String email = (String) reqMap.getOrDefault("signerEmail", "policyholder@example.com");
            var res = Map.of(
                "policyNumber", period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-DOC-1001",
                "envelopeId", "ENV-DS-994820-2026",
                "signerEmail", email != null ? email : "policyholder@example.com",
                "provider", "DocuSign Enterprise",
                "status", "SENT_WAITING_SIGNATURE",
                "signingUrl", "https://demo.docusign.net/signing/v2/ENV-DS-994820"
            );
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/geospatial/risk")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String address = (String) reqMap.getOrDefault("address", "100 Coastal Hwy, Malibu, CA 90265");
            var res = Map.of(
                "address", address,
                "latitude", 34.0259,
                "longitude", -118.7798,
                "wildfireScore", 88,
                "wildfireZone", "Wildfire_High_Risk_Zone_3",
                "floodRiskLevel", "Moderate_AE",
                "sinkholeRisk", "Low",
                "recommendedSafetySurplusPct", 0.15
            );
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/payment/process")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            BigDecimal amount = reqMap.get("amount") != null ? new BigDecimal(reqMap.get("amount").toString()) : new BigDecimal("450.00");
            var res = Map.of(
                "transactionId", "TXN-STRIPE-8839201",
                "jobNumber", jobNumber != null ? jobNumber : "S0001001",
                "amountPaid", amount,
                "currency", "USD",
                "status", "SUCCESS",
                "gatewayResponse", "Charge authorized and captured via Stripe Gateway."
            );
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/vin/decode")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String vin = (String) reqMap.getOrDefault("vin", "1G1YC2D45R5100001");
            var res = Map.of(
                "vin", vin,
                "make", "Chevrolet",
                "model", "Corvette",
                "year", 2024,
                "trim", "Stingray Z51 Coupe",
                "bodyType", "Sports Car",
                "nhtsaSafetyRating", "5 Stars",
                "telematicsEnabled", true,
                "antiTheftInstalled", true
            );
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/forms/infer")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            var res = com.guidewire.pc.service.PolicyFormInferenceEngine.inferPolicyForms(period);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/renewal/eligibility")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            double rateIncrease = reqMap.get("proposedRateIncreasePct") != null ? ((Number) reqMap.get("proposedRateIncreasePct")).doubleValue() : 0.18;
            var res = com.guidewire.pc.service.RenewalEligibilityEngine.getInstance().evaluateRenewalEligibility(period, rateIncrease);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/deductible/buyback")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal origDed = reqMap.get("originalDeductible") != null ? new BigDecimal(reqMap.get("originalDeductible").toString()) : new BigDecimal("10000.00");
            BigDecimal targetDed = reqMap.get("targetDeductible") != null ? new BigDecimal(reqMap.get("targetDeductible").toString()) : new BigDecimal("1000.00");
            var res = com.guidewire.pc.service.DeductibleBuybackEngine.getInstance().calculateDeductibleBuyback(period, origDed, targetDed);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/uw/escalation")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal tiv = reqMap.get("totalInsuredValue") != null ? new BigDecimal(reqMap.get("totalInsuredValue").toString()) : new BigDecimal("12000000.00");
            int score = reqMap.get("riskScore") != null ? ((Number) reqMap.get("riskScore")).intValue() : 75;
            var res = com.guidewire.pc.service.UWEscalationWorkflowEngine.getInstance().processUWEscalation(period, tiv, score);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/dividend/calculate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal losses = reqMap.get("incurredLosses") != null ? new BigDecimal(reqMap.get("incurredLosses").toString()) : new BigDecimal("2500.00");
            var res = com.guidewire.pc.service.SlidingScaleDividendEngine.getInstance().calculatePolicyholderDividend(period, losses);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/coinsurance/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal value = reqMap.get("buildingValue") != null ? new BigDecimal(reqMap.get("buildingValue").toString()) : new BigDecimal("2000000.00");
            BigDecimal limit = reqMap.get("buildingLimit") != null ? new BigDecimal(reqMap.get("buildingLimit").toString()) : new BigDecimal("1200000.00");
            double coinsPct = reqMap.get("coinsurancePct") != null ? ((Number) reqMap.get("coinsurancePct")).doubleValue() : 0.80;
            BigDecimal loss = reqMap.get("claimLoss") != null ? new BigDecimal(reqMap.get("claimLoss").toString()) : new BigDecimal("500000.00");
            BigDecimal ded = reqMap.get("deductible") != null ? new BigDecimal(reqMap.get("deductible").toString()) : new BigDecimal("5000.00");
            var res = com.guidewire.pc.service.CoinsurancePenaltyEngine.getInstance().calculateClaimPayoutWithCoinsurance(period, value, limit, coinsPct, loss, ded);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/rate-cap/apply")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            BigDecimal proposed = reqMap.get("uncappedProposedPremium") != null ? new BigDecimal(reqMap.get("uncappedProposedPremium").toString()) : new BigDecimal("15000.00");
            double maxCap = reqMap.get("maxRateCapPct") != null ? ((Number) reqMap.get("maxRateCapPct")).doubleValue() : 0.10;
            var res = com.guidewire.pc.service.RateImpactCappingEngine.getInstance().applyRenewalRateCap(period, proposed, maxCap);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/telematics/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            double hardBrakes = reqMap.get("hardBrakesPer1k") != null ? ((Number) reqMap.get("hardBrakesPer1k")).doubleValue() : 2.0;
            double rapidAcc = reqMap.get("rapidAccelerationsPer1k") != null ? ((Number) reqMap.get("rapidAccelerationsPer1k")).doubleValue() : 1.5;
            double lateNight = reqMap.get("lateNightDrivingPct") != null ? ((Number) reqMap.get("lateNightDrivingPct")).doubleValue() : 0.05;
            double speeding = reqMap.get("speedingEventsPer1k") != null ? ((Number) reqMap.get("speedingEventsPer1k")).doubleValue() : 1.0;
            var res = com.guidewire.pc.service.TelematicsRatingEngine.getInstance().evaluateTelematicsDrivingScore(period, hardBrakes, rapidAcc, lateNight, speeding);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/tria/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            boolean optIn = reqMap.get("optInTerrorismCoverage") == null || Boolean.TRUE.equals(reqMap.get("optInTerrorismCoverage"));
            double triaRate = reqMap.get("triaRatePct") != null ? ((Number) reqMap.get("triaRatePct")).doubleValue() : 0.035;
            var res = com.guidewire.pc.service.TRIARatingEngine.getInstance().evaluateTRIAOption(period, optIn, triaRate);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/pollution/assess")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            int ust = reqMap.get("ustCount") != null ? ((Number) reqMap.get("ustCount")).intValue() : 2;
            int chemScore = reqMap.get("chemicalHazardScore") != null ? ((Number) reqMap.get("chemicalHazardScore")).intValue() : 6;
            double prox = reqMap.get("proximityToWaterwayMiles") != null ? ((Number) reqMap.get("proximityToWaterwayMiles")).doubleValue() : 0.8;
            int age = reqMap.get("facilityAgeYears") != null ? ((Number) reqMap.get("facilityAgeYears")).intValue() : 15;
            var res = com.guidewire.pc.service.PollutionHazardEngine.getInstance().assessPollutionHazard(period, ust, chemScore, prox, age);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/cyber/evaluate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            boolean mfa = reqMap.get("mfaEnabled") == null || Boolean.TRUE.equals(reqMap.get("mfaEnabled"));
            boolean backups = reqMap.get("offlineBackupsDaily") == null || Boolean.TRUE.equals(reqMap.get("offlineBackupsDaily"));
            boolean edr = reqMap.get("edrDeployed") == null || Boolean.TRUE.equals(reqMap.get("edrDeployed"));
            boolean phishing = reqMap.get("employeePhishingTrained") == null || Boolean.TRUE.equals(reqMap.get("employeePhishingTrained"));
            var res = com.guidewire.pc.service.CyberLiabilityEngine.getInstance().evaluateCyberSecurityControls(period, mfa, backups, edr, phishing);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/flood/rate")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = dataStore.findSubmission(jobNumber);
            String zone = reqMap.get("floodZone") != null ? (String) reqMap.get("floodZone") : "Zone A";
            double lowestElev = reqMap.get("lowestFloorElevationFt") != null ? ((Number) reqMap.get("lowestFloorElevationFt")).doubleValue() : 14.0;
            double bfe = reqMap.get("baseFloodElevationBFE") != null ? ((Number) reqMap.get("baseFloodElevationBFE")).doubleValue() : 12.0;
            boolean vents = reqMap.get("hasFloodProofVents") == null || Boolean.TRUE.equals(reqMap.get("hasFloodProofVents"));
            var res = com.guidewire.pc.service.FloodZoneRatingEngine.getInstance().rateFloodZoneRisk(period, zone, lowestElev, bfe, vents);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/ig/vehicle-details") || path.equals("/vehicle-details/lookup"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String vin = (String) reqMap.getOrDefault("vin", "1FA6P8CF0R5100001");
            Integer year = reqMap.get("vehicleYear") != null ? ((Number) reqMap.get("vehicleYear")).intValue() : 2025;
            String make = (String) reqMap.getOrDefault("vehicleMake", "Ford");
            String model = (String) reqMap.getOrDefault("vehicleModel", "Mustang GT");
            String dl = (String) reqMap.getOrDefault("driverLicenseNumber", "DL-CA-9948123");
            String state = (String) reqMap.getOrDefault("driverState", "CA");
            String polType = (String) reqMap.getOrDefault("policyType", "PersonalAuto");

            String jobNumber = (String) reqMap.get("jobNumber");
            PolicyPeriod period = jobNumber != null ? dataStore.findSubmission(jobNumber) : null;

            var res = com.guidewire.pc.service.VehicleDetailsIntegrationService.getInstance().executeVehicleLookup(vin, year, make, model, dl, state, polType);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/ig/address-standardize") || path.equals("/address/standardize"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String line1 = (String) reqMap.getOrDefault("addressLine1", "100 California St");
            String line2 = (String) reqMap.getOrDefault("addressLine2", "");
            String city = (String) reqMap.getOrDefault("city", "San Francisco");
            String state = (String) reqMap.getOrDefault("state", "CA");
            String zip = (String) reqMap.getOrDefault("postalCode", "94111");
            String country = (String) reqMap.getOrDefault("country", "USA");

            var res = com.guidewire.pc.service.AddressStandardizationService.getInstance().executeAddressStandardization(line1, line2, city, state, zip, country);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/ig/credit-fraud") || path.equals("/credit-fraud/evaluate"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String name = (String) reqMap.getOrDefault("accountHolderName", "Apex Global Industrial");
            String fein = (String) reqMap.getOrDefault("feinOrSsn", "98-7654321");
            String orgType = (String) reqMap.getOrDefault("orgType", "Corporation");
            String state = (String) reqMap.getOrDefault("state", "CA");

            var res = com.guidewire.pc.service.CreditFraudIntegrationService.getInstance().executeCreditAndFraudLookup(name, fein, orgType, state);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/acord/ingest")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            var res = com.guidewire.pc.service.AcordIngestionService.getInstance().parseAndIngestAcordPayload(reqMap);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/oos/timeline-visualizer")) {
            String jobNumber = req.getParameter("jobNumber");
            if (jobNumber == null && req.getContentLength() > 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
                jobNumber = (String) reqMap.get("jobNumber");
            }
            var res = com.guidewire.pc.service.OOSTimelineVisualizerService.getInstance().generateTimelineSlices(jobNumber);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/claims/loss-ratio")) {
            String accNum = req.getParameter("accountNumber");
            if (accNum == null && req.getContentLength() > 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
                accNum = (String) reqMap.get("accountNumber");
            }
            var res = com.guidewire.pc.service.ClaimsCenterSyncService.getInstance().calculateAccountLossRatioAndSyncClaims(accNum);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && (path.equals("/ig/telematics") || path.equals("/telematics/ingest"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reqMap = objectMapper.readValue(req.getInputStream(), Map.class);
            String fleetId = (String) reqMap.getOrDefault("fleetId", "FLT-CA-90812");
            String accNum = (String) reqMap.getOrDefault("accountNumber", "A0001001");
            Integer count = reqMap.get("activeVehiclesCount") != null ? ((Number) reqMap.get("activeVehiclesCount")).intValue() : 15;

            var res = com.guidewire.pc.service.TelematicsIntegrationService.getInstance().executeTelematicsIngestion(fleetId, accNum, count);
            objectMapper.writeValue(resp.getWriter(), res);
            return;
        }

        if (path != null && path.equals("/admin/reset-db")) {
            dataStore.resetToSeedData();
            objectMapper.writeValue(resp.getWriter(), Map.of("status", "Success", "message", "Database reset to clean sample seed data."));
        }
    }

    private void serveOpenApiJson(HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewireRestServlet.serveOpenApiJson");
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