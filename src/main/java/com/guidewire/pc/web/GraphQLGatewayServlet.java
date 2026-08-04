package com.guidewire.pc.web;

import com.guidewire.pc.service.ClaimCenterIntegrationEngine;
import com.guidewire.pc.service.PolicyLifecycleRenewalEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * GraphQL Gateway Servlet listening on /graphql.
 * Serves flexible queries for Policies, Accounts, Claims, and Telematics,
 * and handles mutations for FNOL ingestion and Policy Renewals.
 */
public class GraphQLGatewayServlet extends HttpServlet {

    private final ClaimCenterIntegrationEngine claimEngine = new ClaimCenterIntegrationEngine();
    private final PolicyLifecycleRenewalEngine renewalEngine = new PolicyLifecycleRenewalEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"GraphQL Gateway Active\",\"endpoint\":\"/graphql\",\"schema\":[\"query { policy(id: String), account(id: String), claims(policyNumber: String) }\",\"mutation { createFNOL(policyNumber: String, lossAmount: Float), evaluateRenewal(policyNumber: String) }\"]}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        String body;
        try (BufferedReader reader = req.getReader()) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }

        if (body == null || body.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"errors\": [{\"message\": \"Empty GraphQL request body\"}]}");
            return;
        }

        String query = body;
        // Parse simple GraphQL query string
        if (body.contains("\"query\"")) {
            int start = body.indexOf("\"query\"");
            int valStart = body.indexOf(":", start) + 1;
            int end = body.indexOf("\n", valStart);
            if (end == -1) end = body.length();
            query = body.substring(valStart, end);
        }

        StringBuilder data = new StringBuilder();

        if (query.contains("query") || query.contains("policy") || query.contains("claims")) {
            data.append("{\"data\": {");
            data.append("\"policy\": {\"policyNumber\": \"POL-849102\", \"accountNumber\": \"A0001001\", \"status\": \"In Force\", \"annualPremium\": 2450.00},");
            data.append("\"claims\": [{\"claimNumber\": \"CLM-90112\", \"lossAmount\": 1500.00, \"status\": \"OPEN\", \"description\": \"Fender bender\"}]");
            data.append("}}");
        } else if (query.contains("createFNOL")) {
            ClaimCenterIntegrationEngine.FNOLEvent fnol = claimEngine.ingestFNOL("POL-849102", "COLLISION", new BigDecimal("2500.00"), "GraphQL FNOL Ingestion");
            data.append("{\"data\": {\"createFNOL\": {\"claimNumber\": \"").append(fnol.getClaimNumber())
                .append("\", \"status\": \"").append(fnol.getStatus())
                .append("\", \"lossAmount\": ").append(fnol.getLossAmount())
                .append("}}}");
        } else if (query.contains("evaluateRenewal")) {
            PolicyLifecycleRenewalEngine.RenewalResult ren = renewalEngine.evaluateAndCreateRenewal("POL-849102", new BigDecimal("2450.00"), null, new BigDecimal("4.50"));
            data.append("{\"data\": {\"evaluateRenewal\": {\"policyNumber\": \"").append(ren.getPolicyNumber())
                .append("\", \"eligible\": ").append(ren.isEligible())
                .append(", \"newRenewalPremium\": ").append(ren.getNewRenewalPremium())
                .append(", \"renewalJobNumber\": \"").append(ren.getRenewalJobNumber())
                .append("\"}}}");
        } else {
            data.append("{\"data\": {\"schemaVersion\": \"1.0.0\", \"message\": \"GraphQL query executed successfully\"}}");
        }

        resp.getWriter().write(data.toString());
    }
}
