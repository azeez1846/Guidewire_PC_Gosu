package com.guidewire.pc.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.rules.RuleContext;
import com.guidewire.pc.rules.RulesEngine;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.RatingEngine;
import com.guidewire.pc.security.SecurityUtils;
import com.guidewire.pc.security.SessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
            period.setStatus("Quoted");
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

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", "Action endpoint not found"));
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
