package com.guidewire.pc.web;

import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.pcf.PCFParser;
import com.guidewire.pc.service.DataStoreService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuidewireServer {
    private final int port;
    private final PCFParser pcfParser;
    private final DataStoreService dataStore;

    public GuidewireServer(int port, File rootDir) {
        this.port = port;
        this.pcfParser = new PCFParser(rootDir);
        this.dataStore = DataStoreService.getInstance();
        System.out.println("[PCF Engine] Loaded " + pcfParser.getPcfFiles().size() + " Guidewire PCF layout definitions.");
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MainHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("===============================================================");
        System.out.println("  Guidewire PolicyCenter Application running on JDK & Gosu!");
        System.out.println("  Access URL: http://localhost:" + port);
        System.out.println("  Default Credentials: Username = su | Password = gw");
        System.out.println("===============================================================");
    }

    private class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            Map<String, String> params = new HashMap<>();
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                parseFormData(query, params);
            }
            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                parseFormData(body, params);
            }

            // Session check
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            boolean loggedIn = cookie != null && cookie.contains("SESSIONID=gw_su_session");

            if (path.startsWith("/api/login") && "POST".equalsIgnoreCase(method)) {
                String u = params.get("username");
                String p = params.get("password");
                if ("su".equalsIgnoreCase(u) && "gw".equals(p)) {
                    exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=gw_su_session; Path=/; HttpOnly");
                    redirect(exchange, "/?page=desktop");
                    return;
                } else {
                    redirect(exchange, "/?page=login&error=invalid");
                    return;
                }
            }

            if (path.startsWith("/api/logout")) {
                exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=; Path=/; Max-Age=0");
                redirect(exchange, "/?page=login");
                return;
            }

            if (!loggedIn && !path.startsWith("/login") && !params.getOrDefault("page", "").equals("login")) {
                redirect(exchange, "/?page=login");
                return;
            }

            String page = params.getOrDefault("page", loggedIn ? "desktop" : "login");

            String html = switch (page) {
                case "login" -> renderLoginPage(params.containsKey("error"));
                case "desktop" -> renderDesktopPage(params.getOrDefault("tab", "submissions"), params.get("q"));
                case "new-account" -> renderNewAccountPage(params);
                case "account-detail" -> renderAccountDetailPage(params.get("accNum"));
                case "new-submission" -> renderNewSubmissionPage(params);
                case "submission-wizard" -> renderSubmissionWizard(params.get("jobNum"), params.getOrDefault("step", "step1"), params);
                case "policy-change" -> renderPolicyChangePage(params.get("jobNum"), params);
                case "cancellation" -> renderCancellationPage(params.get("jobNum"), params);
                default -> renderDesktopPage("submissions", null);
            };

            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private void parseFormData(String body, Map<String, String> map) {
        if (body == null || body.isEmpty()) return;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    // HTML RENDERERS

    private String getHeaderCSS() {
        return "<style>" +
                ":root { --gw-navy: #1C2B39; --gw-blue: #0A66C2; --gw-header: #1E2D3D; --gw-bg: #F4F6F9; --gw-sidebar: #273849; --gw-accent: #0073B1; }" +
                "* { box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 0; }" +
                "body { background-color: var(--gw-bg); color: #333; display: flex; flex-direction: column; min-height: 100vh; font-size: 13px; }" +
                ".gw-header { background: var(--gw-header); color: white; display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; border-bottom: 2px solid #0D161F; }" +
                ".gw-brand { display: flex; align-items: center; gap: 12px; font-weight: 700; font-size: 16px; letter-spacing: 0.5px; }" +
                ".gw-brand-logo { background: #0088CC; color: white; border-radius: 4px; padding: 4px 8px; font-size: 12px; font-weight: 800; }" +
                ".gw-search-box { display: flex; gap: 6px; align-items: center; }" +
                ".gw-search-input { padding: 5px 10px; border-radius: 4px; border: 1px solid #4A5D70; background: #2A3C4E; color: white; font-size: 12px; width: 220px; }" +
                ".gw-search-input::placeholder { color: #A0B0C0; }" +
                ".gw-user-menu { display: flex; align-items: center; gap: 14px; font-size: 12px; }" +
                ".gw-user-badge { background: #34495E; padding: 4px 10px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 6px; }" +
                ".gw-nav-tabs { background: #243444; display: flex; padding-left: 16px; border-bottom: 1px solid #14212D; }" +
                ".gw-tab { color: #C2D1E0; padding: 10px 18px; text-decoration: none; font-weight: 600; font-size: 13px; border-top-left-radius: 4px; border-top-right-radius: 4px; display: inline-block; transition: all 0.15s; }" +
                ".gw-tab:hover { color: white; background: rgba(255,255,255,0.08); }" +
                ".gw-tab.active { background: #F4F6F9; color: var(--gw-navy); border-top: 3px solid #0088CC; font-weight: 700; }" +
                ".gw-main-container { display: flex; flex: 1; }" +
                ".gw-sidebar { width: 220px; background: #2C3E50; color: white; padding: 16px 0; font-size: 13px; border-right: 1px solid #1A252F; }" +
                ".gw-sidebar-title { padding: 6px 16px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #8C9CAE; font-weight: 700; margin-top: 10px; }" +
                ".gw-sidebar-item { display: block; padding: 8px 16px; color: #D5E1ED; text-decoration: none; font-weight: 500; border-left: 3px solid transparent; }" +
                ".gw-sidebar-item:hover { background: #34495E; color: white; }" +
                ".gw-sidebar-item.active { background: #1C2B39; color: #38B6FF; border-left-color: #38B6FF; font-weight: 700; }" +
                ".gw-content { flex: 1; padding: 20px; overflow-y: auto; }" +
                ".gw-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid #DCE3EA; }" +
                ".gw-page-title { font-size: 20px; font-weight: 700; color: #1C2B39; display: flex; align-items: center; gap: 8px; }" +
                ".gw-pcf-tag { font-size: 10px; background: #E1E8ED; color: #556677; padding: 2px 6px; border-radius: 3px; font-family: monospace; font-weight: normal; }" +
                ".gw-toolbar { display: flex; gap: 8px; margin-bottom: 16px; background: white; padding: 10px 14px; border: 1px solid #D0D7DE; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }" +
                ".gw-btn { background: #0073B1; color: white; border: none; padding: 7px 16px; border-radius: 4px; font-weight: 600; font-size: 12px; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; transition: background 0.15s; }" +
                ".gw-btn:hover { background: #005580; }" +
                ".gw-btn-secondary { background: #EBEFF3; color: #334455; border: 1px solid #CBD5E1; }" +
                ".gw-btn-secondary:hover { background: #DDE4EC; }" +
                ".gw-btn-success { background: #28A745; }" +
                ".gw-btn-success:hover { background: #218838; }" +
                ".gw-card { background: white; border: 1px solid #D0D7DE; border-radius: 6px; padding: 18px; margin-bottom: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }" +
                ".gw-card-title { font-size: 14px; font-weight: 700; color: #1C2B39; margin-bottom: 14px; border-bottom: 1px solid #EDF2F7; padding-bottom: 8px; display: flex; justify-content: space-between; align-items: center; }" +
                ".gw-table { width: 100%; border-collapse: collapse; font-size: 12px; text-align: left; }" +
                ".gw-table th { background: #F0F4F8; color: #334455; padding: 9px 12px; font-weight: 700; border-bottom: 2px solid #CBD5E1; }" +
                ".gw-table td { padding: 9px 12px; border-bottom: 1px solid #E2E8F0; vertical-align: middle; }" +
                ".gw-table tr:hover { background: #F8FAFC; }" +
                ".gw-status-badge { padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 700; display: inline-block; }" +
                ".status-Issued { background: #D4EDDA; color: #155724; }" +
                ".status-Bound { background: #CCE5FF; color: #004085; }" +
                ".status-Quoted { background: #FFF3CD; color: #856404; }" +
                ".status-Draft { background: #E2E3E5; color: #383D41; }" +
                ".status-Active { background: #D4EDDA; color: #155724; }" +
                ".gw-form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; }" +
                ".gw-field { display: flex; flex-direction: column; gap: 5px; margin-bottom: 12px; }" +
                ".gw-field label { font-weight: 600; font-size: 12px; color: #4A5568; }" +
                ".gw-field label .required { color: #E53E3E; margin-left: 2px; }" +
                ".gw-field input, .gw-field select { padding: 8px 10px; border: 1px solid #CBD5E1; border-radius: 4px; font-size: 12px; outline: none; transition: border 0.15s; }" +
                ".gw-field input:focus, .gw-field select:focus { border-color: #0073B1; box-shadow: 0 0 0 2px rgba(0,115,177,0.15); }" +
                ".gw-wizard-steps { display: flex; border-bottom: 2px solid #E2E8F0; margin-bottom: 20px; background: white; border-radius: 6px; overflow: hidden; }" +
                ".gw-step { flex: 1; padding: 12px 16px; text-align: center; font-weight: 600; color: #718096; background: #F7FAFC; border-right: 1px solid #EDF2F7; text-decoration: none; font-size: 12px; }" +
                ".gw-step.active { background: #0073B1; color: white; font-weight: 700; }" +
                ".gw-step.completed { background: #EBF8FF; color: #2B6CB0; }" +
                ".gw-footer { text-align: center; padding: 12px; font-size: 11px; color: #718096; background: white; border-top: 1px solid #E2E8F0; margin-top: auto; }" +
                "</style>";
    }

    private String renderLoginPage(boolean hasError) {
        return "<!DOCTYPE html><html><head><title>Login - Guidewire PolicyCenter</title>" + getHeaderCSS() + "</head>" +
                "<body style='justify-content: center; align-items: center; background: #1C2B39;'>" +
                "<div style='width: 380px; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.3); text-align: center;'>" +
                "<div style='display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 10px;'>" +
                "<span style='background:#0088CC; color:white; padding:6px 12px; border-radius:4px; font-weight:800; font-size:16px;'>GW</span>" +
                "<h2 style='font-size:20px; font-weight:700; color:#1C2B39;'>PolicyCenter</h2>" +
                "</div>" +
                "<p style='color:#718096; font-size:12px; margin-bottom:20px;'>Guidewire PolicyCenter 10.0 Evaluation</p>" +
                (hasError ? "<div style='background:#FFF5F5; border:1px solid #FEB2B2; color:#C53030; padding:8px; border-radius:4px; font-size:12px; margin-bottom:15px;'>Invalid credentials! Try <b>su</b> / <b>gw</b></div>" : "") +
                "<form action='/api/login' method='POST'>" +
                "<div class='gw-field' style='text-align:left;'><label>Username</label><input type='text' name='username' value='su' required></div>" +
                "<div class='gw-field' style='text-align:left;'><label>Password</label><input type='password' name='password' value='gw' required></div>" +
                "<button type='submit' class='gw-btn' style='width:100%; justify-content:center; padding:10px; margin-top:10px;'>Log In</button>" +
                "</form>" +
                "<div style='margin-top:20px; font-size:11px; color:#A0AEC0;'>PCF Definition: <code>Login.pcf</code></div>" +
                "</div></body></html>";
    }

    private String renderHeader(String activeTab) {
        return "<div class='gw-header'>" +
                "<div class='gw-brand'><span class='gw-brand-logo'>GW</span> Guidewire PolicyCenter <span style='font-size:11px; font-weight:normal; opacity:0.8;'>v10.0</span></div>" +
                "<div class='gw-search-box'>" +
                "<form action='/' method='GET' style='display:flex; gap:6px;'>" +
                "<input type='hidden' name='page' value='desktop'>" +
                "<input type='text' name='q' class='gw-search-input' placeholder='QuickJump / Search Account or Submission...'>" +
                "<button type='submit' class='gw-btn gw-btn-secondary' style='padding:5px 10px;'>Search</button>" +
                "</form>" +
                "</div>" +
                "<div class='gw-user-menu'>" +
                "<div class='gw-user-badge'>👤 Super User (su)</div>" +
                "<a href='/api/logout' style='color:#A0B0C0; text-decoration:none; font-weight:600;'>Log Out</a>" +
                "</div>" +
                "</div>" +
                "<div class='gw-nav-tabs'>" +
                "<a href='/?page=desktop&tab=submissions' class='gw-tab " + ("desktop".equals(activeTab) ? "active" : "") + "'>Desktop</a>" +
                "<a href='/?page=desktop&tab=accounts' class='gw-tab " + ("accounts".equals(activeTab) ? "active" : "") + "'>Accounts</a>" +
                "<a href='/?page=desktop&tab=submissions' class='gw-tab " + ("submissions".equals(activeTab) ? "active" : "") + "'>Policies &amp; Submissions</a>" +
                "<a href='/?page=new-account' class='gw-tab'>+ New Account</a>" +
                "<a href='/?page=new-submission' class='gw-tab'>+ New Submission</a>" +
                "</div>";
    }

    private String renderDesktopPage(String tab, String searchQuery) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Desktop - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'>");
        // Sidebar
        sb.append("<div class='gw-sidebar'>");
        sb.append("<div class='gw-sidebar-title'>My Views</div>");
        sb.append("<a href='/?page=desktop&tab=submissions' class='gw-sidebar-item ").append("submissions".equals(tab) ? "active" : "").append("'>📋 My Submissions</a>");
        sb.append("<a href='/?page=desktop&tab=activities' class='gw-sidebar-item ").append("activities".equals(tab) ? "active" : "").append("'>⚡ My Activities</a>");
        sb.append("<a href='/?page=desktop&tab=accounts' class='gw-sidebar-item ").append("accounts".equals(tab) ? "active" : "").append("'>🏢 My Accounts</a>");
        sb.append("<div class='gw-sidebar-title'>Quick Actions</div>");
        sb.append("<a href='/?page=new-account' class='gw-sidebar-item'>+ Create New Account</a>");
        sb.append("<a href='/?page=new-submission' class='gw-sidebar-item'>+ New Submission</a>");
        sb.append("</div>");

        // Main Content
        sb.append("<div class='gw-content'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Desktop <span class='gw-pcf-tag'>Desktop.pcf</span></div>");
        sb.append("<div>");
        sb.append("<a href='/?page=new-account' class='gw-btn'>+ New Account</a> ");
        sb.append("<a href='/?page=new-submission' class='gw-btn gw-btn-secondary'>+ New Submission</a>");
        sb.append("</div>");
        sb.append("</div>");

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sb.append("<div class='gw-card' style='background:#EBF8FF; border-color:#90CDF4; color:#2B6CB0;'>");
            sb.append("Search Filter Active: <b>\"").append(searchQuery).append("\"</b>. <a href='/?page=desktop' style='color:#2B6CB0; font-weight:bold;'>Clear Filter</a>");
            sb.append("</div>");
        }

        // Subtabs Toolbar
        sb.append("<div class='gw-toolbar'>");
        sb.append("<a href='/?page=desktop&tab=submissions' class='gw-btn ").append("submissions".equals(tab) ? "" : "gw-btn-secondary").append("'>My Submissions (").append(dataStore.getSubmissions().size()).append(")</a> ");
        sb.append("<a href='/?page=desktop&tab=activities' class='gw-btn ").append("activities".equals(tab) ? "" : "gw-btn-secondary").append("'>My Activities (").append(dataStore.getActivities().size()).append(")</a> ");
        sb.append("<a href='/?page=desktop&tab=accounts' class='gw-btn ").append("accounts".equals(tab) ? "" : "gw-btn-secondary").append("'>My Accounts (").append(dataStore.getAccounts().size()).append(")</a>");
        sb.append("</div>");

        switch (tab) {
            case "submissions" -> {
                sb.append("<div class='gw-card'>");
                sb.append("<div class='gw-card-title'>Submissions &amp; Policy Transactions <span class='gw-pcf-tag'>DesktopSubmissionsLV.pcf</span></div>");
                sb.append("<table class='gw-table'>");
                sb.append("<thead><tr><th>Transaction #</th><th>Account Holder</th><th>Policy Line</th><th>Effective Date</th><th>Status</th><th>Total Premium</th><th>Producer Code</th><th>Action</th></tr></thead><tbody>");

                List<PolicyPeriod> subs = dataStore.getSubmissions();
                for (PolicyPeriod s : subs) {
                    if (searchQuery != null && !searchQuery.isEmpty()) {
                        boolean match = s.getJobNumber().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                (s.getAccount() != null && s.getAccount().getAccountHolderName().toLowerCase().contains(searchQuery.toLowerCase()));
                        if (!match) continue;
                    }
                    sb.append("<tr>");
                    sb.append("<td><b><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' style='color:#0073B1; text-decoration:none;'>").append(s.getJobNumber()).append("</a></b></td>");
                    sb.append("<td>").append(s.getAccount() != null ? s.getAccount().getAccountHolderName() : "N/A").append("</td>");
                    sb.append("<td><b>").append(s.getProductCode()).append("</b></td>");
                    sb.append("<td>").append(s.getEffectiveDate()).append("</td>");
                    sb.append("<td><span class='gw-status-badge status-").append(s.getStatus()).append("'>").append(s.getFormattedStatus()).append("</span></td>");
                    sb.append("<td><b>$").append(s.getTotalPremium()).append("</b></td>");
                    sb.append("<td>").append(s.getProducerCode()).append("</td>");
                    sb.append("<td><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' class='gw-btn gw-btn-secondary' style='padding:4px 8px; font-size:11px;'>Open Wizard &gt;</a></td>");
                    sb.append("</tr>");
                }
                sb.append("</tbody></table></div>");
            }
            case "activities" -> {
                sb.append("<div class='gw-card'>");
                sb.append("<div class='gw-card-title'>Pending Underwriting Activities <span class='gw-pcf-tag'>DesktopActivitiesLV.pcf</span></div>");
                sb.append("<table class='gw-table'>");
                sb.append("<thead><tr><th>Due Date</th><th>Priority</th><th>Subject</th><th>Target Job / Account</th><th>Status</th><th>Assigned User</th></tr></thead><tbody>");
                for (Activity a : dataStore.getActivities()) {
                    sb.append("<tr>");
                    sb.append("<td>").append(a.getDueDate()).append("</td>");
                    sb.append("<td><b style='color:").append("High".equals(a.getPriority()) ? "#C53030" : "#2B6CB0").append(";'>").append(a.getPriority()).append("</b></td>");
                    sb.append("<td><b>").append(a.getSubject()).append("</b><br><span style='font-size:11px; color:#718096;'>").append(a.getDescription()).append("</span></td>");
                    sb.append("<td>").append(a.getRelatedJobNumber() != null ? a.getRelatedJobNumber() : a.getRelatedAccountId()).append("</td>");
                    sb.append("<td><span class='gw-status-badge status-Active'>").append(a.getStatus()).append("</span></td>");
                    sb.append("<td>").append(a.getAssignedUser()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</tbody></table></div>");
            }
            case "accounts" -> {
                sb.append("<div class='gw-card'>");
                sb.append("<div class='gw-card-title'>Accounts Directory <span class='gw-pcf-tag'>DesktopAccountsLV.pcf</span></div>");
                sb.append("<table class='gw-table'>");
                sb.append("<thead><tr><th>Account #</th><th>Account Holder Name</th><th>Type</th><th>Address</th><th>Status</th><th>Producer Code</th><th>Action</th></tr></thead><tbody>");
                for (Account a : dataStore.getAccounts()) {
                    if (searchQuery != null && !searchQuery.isEmpty()) {
                        boolean match = a.getAccountNumber().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                a.getAccountHolderName().toLowerCase().contains(searchQuery.toLowerCase());
                        if (!match) continue;
                    }
                    sb.append("<tr>");
                    sb.append("<td><b><a href='/?page=account-detail&accNum=").append(a.getAccountNumber()).append("' style='color:#0073B1; text-decoration:none;'>").append(a.getAccountNumber()).append("</a></b></td>");
                    sb.append("<td><b>").append(a.getAccountHolderName()).append("</b></td>");
                    sb.append("<td>").append(a.getAccountHolderType()).append("</td>");
                    sb.append("<td>").append(a.getFormattedAddress()).append("</td>");
                    sb.append("<td><span class='gw-status-badge status-Active'>").append(a.getAccountStatus()).append("</span></td>");
                    sb.append("<td>").append(a.getProducerCode()).append("</td>");
                    sb.append("<td><a href='/?page=new-submission&accNum=").append(a.getAccountNumber()).append("' class='gw-btn' style='padding:4px 8px; font-size:11px;'>+ New Submission</a></td>");
                    sb.append("</tr>");
                }
                sb.append("</tbody></table></div>");
            }
            default -> {}
        }

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderNewAccountPage(Map<String, String> params) {
        if (params.containsKey("action") && "create".equals(params.get("action"))) {
            Account acc = new Account();
            acc.setAccountHolderName(params.get("accountHolderName"));
            acc.setAccountHolderType(params.getOrDefault("accountHolderType", "Company"));
            acc.setFein(params.get("fein"));
            acc.setAddressLine1(params.get("addressLine1"));
            acc.setAddressLine2(params.get("addressLine2"));
            acc.setCity(params.get("city"));
            acc.setState(params.get("state"));
            acc.setPostalCode(params.get("postalCode"));
            acc.setPhone(params.get("phone"));
            acc.setEmail(params.get("email"));
            acc.setProducerCode(params.get("producerCode"));
            acc.setIndustryCode(params.get("industryCode"));
            acc.setOrgType(params.get("orgType"));

            Account created = dataStore.createAccount(acc);
            return "<!DOCTYPE html><html><head><script>window.location.href='/?page=account-detail&accNum=" + created.getAccountNumber() + "';</script></head></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Create New Account - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("accounts"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:960px; margin:0 auto;'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Create New Account <span class='gw-pcf-tag'>NewAccount.pcf</span></div>");
        sb.append("</div>");

        sb.append("<form action='/?page=new-account' method='POST'>");
        sb.append("<input type='hidden' name='action' value='create'>");

        sb.append("<div class='gw-toolbar'>");
        sb.append("<button type='submit' class='gw-btn gw-btn-success'>💾 Update &amp; Create Account</button> ");
        sb.append("<a href='/?page=desktop&tab=accounts' class='gw-btn gw-btn-secondary'>Cancel</a>");
        sb.append("</div>");

        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Account Information &amp; Primary Details <span class='gw-pcf-tag'>AccountDetailDV.pcf</span></div>");
        sb.append("<div class='gw-form-grid'>");

        sb.append("<div>");
        sb.append("<div class='gw-field'><label>Account Holder Name <span class='required'>*</span></label><input type='text' name='accountHolderName' placeholder='e.g. Apex Industrial Solutions' required></div>");
        sb.append("<div class='gw-field'><label>Account Holder Type <span class='required'>*</span></label><select name='accountHolderType'><option value='Company'>Company</option><option value='Individual'>Individual</option></select></div>");
        sb.append("<div class='gw-field'><label>Tax ID / FEIN or SSN</label><input type='text' name='fein' placeholder='e.g. 98-7654321'></div>");
        sb.append("<div class='gw-field'><label>Producer Code <span class='required'>*</span></label><input type='text' name='producerCode' value='PR-10928' required></div>");
        sb.append("<div class='gw-field'><label>Industry Code (SIC/NAICS)</label><input type='text' name='industryCode' placeholder='e.g. 541511 - Custom Software'></div>");
        sb.append("<div class='gw-field'><label>Organization Type</label><select name='orgType'><option value='Corporation'>Corporation</option><option value='LLC'>LLC</option><option value='Partnership'>Partnership</option><option value='Individual'>Individual</option></select></div>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<div class='gw-field'><label>Street Address Line 1 <span class='required'>*</span></label><input type='text' name='addressLine1' placeholder='123 Commercial Blvd' required></div>");
        sb.append("<div class='gw-field'><label>Address Line 2</label><input type='text' name='addressLine2' placeholder='Suite 200'></div>");
        sb.append("<div class='gw-field'><label>City <span class='required'>*</span></label><input type='text' name='city' placeholder='San Francisco' required></div>");
        sb.append("<div class='gw-field'><label>State <span class='required'>*</span></label><input type='text' name='state' placeholder='CA' required></div>");
        sb.append("<div class='gw-field'><label>ZIP / Postal Code <span class='required'>*</span></label><input type='text' name='postalCode' placeholder='94105' required></div>");
        sb.append("<div class='gw-field'><label>Phone Number</label><input type='text' name='phone' placeholder='(415) 555-0100'></div>");
        sb.append("<div class='gw-field'><label>Email Address</label><input type='text' name='email' placeholder='billing@apex.com'></div>");
        sb.append("</div>");

        sb.append("</div></div>");
        sb.append("</form>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderAccountDetailPage(String accNum) {
        Account acc = dataStore.findAccount(accNum);
        if (acc == null) return "Account Not Found";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Account ").append(acc.getAccountNumber()).append(" - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("accounts"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:960px; margin:0 auto;'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Account Summary: ").append(acc.getAccountNumber()).append(" - ").append(acc.getAccountHolderName()).append(" <span class='gw-pcf-tag'>AccountDetailDV.pcf</span></div>");
        sb.append("<div><a href='/?page=new-submission&accNum=").append(acc.getAccountNumber()).append("' class='gw-btn'>+ Create New Submission</a></div>");
        sb.append("</div>");

        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Account Overview</div>");
        sb.append("<div class='gw-form-grid'>");
        sb.append("<div>");
        sb.append("<p style='margin-bottom:8px;'><b>Account Holder:</b> ").append(acc.getAccountHolderName()).append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>Type:</b> ").append(acc.getAccountHolderType()).append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>FEIN / Tax ID:</b> ").append(acc.getFein() != null ? acc.getFein() : "N/A").append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>Producer Code:</b> ").append(acc.getProducerCode()).append("</p>");
        sb.append("</div>");
        sb.append("<div>");
        sb.append("<p style='margin-bottom:8px;'><b>Address:</b> ").append(acc.getFormattedAddress()).append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>Phone:</b> ").append(acc.getPhone() != null ? acc.getPhone() : "N/A").append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>Email:</b> ").append(acc.getEmail() != null ? acc.getEmail() : "N/A").append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>Status:</b> <span class='gw-status-badge status-Active'>").append(acc.getAccountStatus()).append("</span></p>");
        sb.append("</div>");
        sb.append("</div></div>");

        // Associated Submissions
        List<PolicyPeriod> subs = dataStore.getSubmissions();
        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Submissions &amp; Policies under Account</div>");
        sb.append("<table class='gw-table'>");
        sb.append("<thead><tr><th>Transaction #</th><th>Policy Line</th><th>Effective Date</th><th>Status</th><th>Total Premium</th><th>Action</th></tr></thead><tbody>");
        boolean count = false;
        for (PolicyPeriod s : subs) {
            if (s.getAccount() != null && s.getAccount().getAccountNumber().equalsIgnoreCase(accNum)) {
                count = true;
                sb.append("<tr>");
                sb.append("<td><b><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' style='color:#0073B1;'>").append(s.getJobNumber()).append("</a></b></td>");
                sb.append("<td><b>").append(s.getProductCode()).append("</b></td>");
                sb.append("<td>").append(s.getEffectiveDate()).append("</td>");
                sb.append("<td><span class='gw-status-badge status-").append(s.getStatus()).append("'>").append(s.getFormattedStatus()).append("</span></td>");
                sb.append("<td><b>$").append(s.getTotalPremium()).append("</b></td>");
                sb.append("<td><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' class='gw-btn gw-btn-secondary' style='padding:3px 8px; font-size:11px;'>View Wizard</a></td>");
                sb.append("</tr>");
            }
        }
        if (!count) {
            sb.append("<tr><td colspan='6' style='text-align:center; color:#718096; padding:16px;'>No active submissions found for this account. <a href='/?page=new-submission&accNum=").append(accNum).append("'>Create One Now</a></td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderNewSubmissionPage(Map<String, String> params) {
        if (params.containsKey("action") && "create".equals(params.get("action"))) {
            String accNum = params.get("accNum");
            Account acc = dataStore.findAccount(accNum);

            PolicyPeriod sub = new PolicyPeriod();
            sub.setAccount(acc);
            sub.setProductCode(params.get("productCode"));
            sub.setEffectiveDate(params.getOrDefault("effectiveDate", "2026-08-01"));
            sub.setExpirationDate(params.getOrDefault("expirationDate", "2027-08-01"));
            sub.setBaseState(params.getOrDefault("baseState", "CA"));
            sub.setProducerCode(acc != null ? acc.getProducerCode() : "PR-10928");

            PolicyPeriod created = dataStore.createSubmission(sub);
            return "<!DOCTYPE html><html><head><script>window.location.href='/?page=submission-wizard&jobNum=" + created.getJobNumber() + "&step=step1';</script></head></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Create New Submission - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("submissions"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:720px; margin:0 auto;'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>New Policy Submission <span class='gw-pcf-tag'>NewSubmission.pcf</span></div>");
        sb.append("</div>");

        sb.append("<form action='/?page=new-submission' method='POST'>");
        sb.append("<input type='hidden' name='action' value='create'>");

        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Select Account &amp; Product Line</div>");
        sb.append("<div class='gw-field'><label>Select Account <span class='required'>*</span></label><select name='accNum' required>");

        String selectedAccNum = params.get("accNum");
        for (Account a : dataStore.getAccounts()) {
            boolean sel = a.getAccountNumber().equalsIgnoreCase(selectedAccNum);
            sb.append("<option value='").append(a.getAccountNumber()).append("' ").append(sel ? "selected" : "").append(">")
                    .append(a.getAccountNumber()).append(" - ").append(a.getAccountHolderName()).append("</option>");
        }
        sb.append("</select></div>");

        sb.append("<div class='gw-field'><label>Product Line of Business <span class='required'>*</span></label><select name='productCode' required>");
        sb.append("<option value='CommercialAuto'>Commercial Auto</option>");
        sb.append("<option value='GeneralLiability'>General Liability</option>");
        sb.append("<option value='CommercialProperty'>Commercial Property</option>");
        sb.append("<option value='PersonalAuto'>Personal Auto</option>");
        sb.append("</select></div>");

        sb.append("<div class='gw-field'><label>Effective Date</label><input type='date' name='effectiveDate' value='2026-08-01'></div>");
        sb.append("<div class='gw-field'><label>Base State</label><input type='text' name='baseState' value='CA'></div>");

        sb.append("<div style='margin-top:20px; display:flex; gap:10px;'>");
        sb.append("<button type='submit' class='gw-btn gw-btn-success'>Next &gt; Enter Policy Wizard</button> ");
        sb.append("<a href='/?page=desktop' class='gw-btn gw-btn-secondary'>Cancel</a>");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</form>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderSubmissionWizard(String jobNum, String step, Map<String, String> params) {
        PolicyPeriod sub = dataStore.findSubmission(jobNum);
        if (sub == null) return "Submission Not Found";

        // Handle step updates
        if (params.containsKey("action")) {
            String act = params.get("action");
            switch (act) {
                case "updateStep1" -> {
                    sub.setEffectiveDate(params.get("effectiveDate"));
                    sub.setExpirationDate(params.get("expirationDate"));
                    sub.setTermMonths(Integer.parseInt(params.getOrDefault("termMonths", "12")));
                    sub.setBaseState(params.get("baseState"));
                    sub.setProducerCode(params.get("producerCode"));
                    step = "step2";
                }
                case "quote" -> {
                    sub.setBodilyInjuryLimit(params.get("bodilyInjuryLimit"));
                    sub.setPropertyDamageLimit(params.get("propertyDamageLimit"));
                    sub.setComprehensiveDeductible(params.get("comprehensiveDeductible"));
                    sub.setCollisionDeductible(params.get("collisionDeductible"));

                    sub.calculatePremium();
                    sub.setStatus("Quoted");
                    step = "step3";
                }
                case "bind" -> {
                    if (sub.getPolicyNumber() == null || sub.getPolicyNumber().trim().isEmpty()) {
                        sub.setPolicyNumber("POL-" + (int)(Math.random() * 900000 + 100000));
                    }
                    sub.setStatus("Issued");
                    step = "step3";
                }
                default -> {}
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Submission ").append(sub.getJobNumber()).append(" - Wizard</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("submissions"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:980px; margin:0 auto;'>");

        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Submission Wizard: ").append(sub.getJobNumber()).append(" (").append(sub.getProductCode()).append(") <span class='gw-pcf-tag'>SubmissionWizard.pcf</span></div>");
        sb.append("<div>Status: <span class='gw-status-badge status-").append(sub.getStatus()).append("'>").append(sub.getFormattedStatus()).append("</span></div>");
        sb.append("</div>");

        // Stepper Header
        sb.append("<div class='gw-wizard-steps'>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step1' class='gw-step ").append("step1".equals(step) ? "active" : "completed").append("'>1. Policy Info</a>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step2' class='gw-step ").append("step2".equals(step) ? "active" : ("step3".equals(step) ? "completed" : "")).append("'>2. Coverages &amp; Limits</a>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step3' class='gw-step ").append("step3".equals(step) ? "active" : "").append("'>3. Rating &amp; Quote Summary</a>");
        sb.append("</div>");

        if ("step1".equals(step)) {
            sb.append("<form action='/?page=submission-wizard&jobNum=").append(jobNum).append("' method='POST'>");
            sb.append("<input type='hidden' name='action' value='updateStep1'>");

            sb.append("<div class='gw-toolbar'>");
            sb.append("<button type='submit' class='gw-btn'>Next: Coverages &gt;</button>");
            sb.append("</div>");

            sb.append("<div class='gw-card'>");
            sb.append("<div class='gw-card-title'>Step 1: General Policy Information</div>");
            sb.append("<div class='gw-form-grid'>");
            sb.append("<div>");
            sb.append("<div class='gw-field'><label>Transaction / Job #</label><input type='text' value='").append(sub.getJobNumber()).append("' disabled></div>");
            sb.append("<div class='gw-field'><label>Account Holder</label><input type='text' value='").append(sub.getAccount() != null ? sub.getAccount().getAccountHolderName() : "N/A").append("' disabled></div>");
            sb.append("<div class='gw-field'><label>Policy Line</label><input type='text' value='").append(sub.getProductCode()).append("' disabled></div>");
            sb.append("</div>");
            sb.append("<div>");
            sb.append("<div class='gw-field'><label>Effective Date</label><input type='date' name='effectiveDate' value='").append(sub.getEffectiveDate()).append("' required></div>");
            sb.append("<div class='gw-field'><label>Expiration Date</label><input type='date' name='expirationDate' value='").append(sub.getExpirationDate()).append("' required></div>");
            sb.append("<div class='gw-field'><label>Term (Months)</label><select name='termMonths'><option value='12' ").append(sub.getTermMonths() == 12 ? "selected" : "").append(">12 Months</option><option value='6' ").append(sub.getTermMonths() == 6 ? "selected" : "").append(">6 Months</option></select></div>");
            sb.append("<div class='gw-field'><label>Base State</label><input type='text' name='baseState' value='").append(sub.getBaseState()).append("' required></div>");
            sb.append("<div class='gw-field'><label>Producer Code</label><input type='text' name='producerCode' value='").append(sub.getProducerCode()).append("' required></div>");
            sb.append("</div>");
            sb.append("</div></div>");
            sb.append("</form>");
        } else if ("step2".equals(step)) {
            sb.append("<form action='/?page=submission-wizard&jobNum=").append(jobNum).append("' method='POST'>");
            sb.append("<input type='hidden' name='action' value='quote'>");

            sb.append("<div class='gw-toolbar'>");
            sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step1' class='gw-btn gw-btn-secondary'>&lt; Back</a> ");
            sb.append("<button type='submit' class='gw-btn gw-btn-success'>⚡ Run Gosu Rating Engine &amp; Generate Quote &gt;</button>");
            sb.append("</div>");

            sb.append("<div class='gw-card'>");
            sb.append("<div class='gw-card-title'>Step 2: Line Coverages &amp; Deductibles</div>");
            sb.append("<div class='gw-form-grid'>");
            sb.append("<div>");
            sb.append("<h4 style='margin-bottom:10px; color:#1C2B39;'>Liability Limits</h4>");
            sb.append("<div class='gw-field'><label>Bodily Injury Limit</label><select name='bodilyInjuryLimit'>");
            sb.append("<option value='$250k/$500k' ").append("$250k/$500k".equals(sub.getBodilyInjuryLimit()) ? "selected" : "").append(">$250,000 / $500,000</option>");
            sb.append("<option value='$500k/$500k' ").append("$500k/$500k".equals(sub.getBodilyInjuryLimit()) ? "selected" : "").append(">$500,000 / $500,000</option>");
            sb.append("<option value='$1M/$1M' ").append("$1M/$1M".equals(sub.getBodilyInjuryLimit()) ? "selected" : "").append(">$1,000,000 / $1,000,000</option>");
            sb.append("</select></div>");

            sb.append("<div class='gw-field'><label>Property Damage Limit</label><select name='propertyDamageLimit'>");
            sb.append("<option value='$100k' ").append("$100k".equals(sub.getPropertyDamageLimit()) ? "selected" : "").append(">$100,000</option>");
            sb.append("<option value='$250k' ").append("$250k".equals(sub.getPropertyDamageLimit()) ? "selected" : "").append(">$250,000</option>");
            sb.append("<option value='$500k' ").append("$500k".equals(sub.getPropertyDamageLimit()) ? "selected" : "").append(">$500,000</option>");
            sb.append("</select></div>");
            sb.append("</div>");

            sb.append("<div>");
            sb.append("<h4 style='margin-bottom:10px; color:#1C2B39;'>Physical Damage Deductibles</h4>");
            sb.append("<div class='gw-field'><label>Comprehensive Deductible</label><select name='comprehensiveDeductible'>");
            sb.append("<option value='$250' ").append("$250".equals(sub.getComprehensiveDeductible()) ? "selected" : "").append(">$250</option>");
            sb.append("<option value='$500' ").append("$500".equals(sub.getComprehensiveDeductible()) ? "selected" : "").append(">$500</option>");
            sb.append("<option value='$1000' ").append("$1000".equals(sub.getComprehensiveDeductible()) ? "selected" : "").append(">$1,000</option>");
            sb.append("</select></div>");

            sb.append("<div class='gw-field'><label>Collision Deductible</label><select name='collisionDeductible'>");
            sb.append("<option value='$500' ").append("$500".equals(sub.getCollisionDeductible()) ? "selected" : "").append(">$500</option>");
            sb.append("<option value='$1000' ").append("$1000".equals(sub.getCollisionDeductible()) ? "selected" : "").append(">$1,000</option>");
            sb.append("<option value='$2500' ").append("$2500".equals(sub.getCollisionDeductible()) ? "selected" : "").append(">$2,500</option>");
            sb.append("</select></div>");
            sb.append("</div>");

            sb.append("</div></div>");
            sb.append("</form>");
        } else if ("step3".equals(step)) {
            sb.append("<form action='/?page=submission-wizard&jobNum=").append(jobNum).append("' method='POST'>");
            sb.append("<input type='hidden' name='action' value='bind'>");

            sb.append("<div class='gw-toolbar'>");
            sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step2' class='gw-btn gw-btn-secondary'>&lt; Edit Coverages</a> ");
            if (!"Issued".equalsIgnoreCase(sub.getStatus())) {
                sb.append("<button type='submit' class='gw-btn gw-btn-success'>📜 Bind &amp; Issue Policy</button>");
            } else {
                sb.append("<span style='color:#28A745; font-weight:700; display:inline-flex; align-items:center; gap:5px; margin-right:10px;'>✅ Policy In Force</span>");
                sb.append("<a href='/?page=policy-change&jobNum=").append(jobNum).append("' class='gw-btn gw-btn-secondary'>⚡ Policy Change</a> ");
                sb.append("<a href='/?page=cancellation&jobNum=").append(jobNum).append("' class='gw-btn' style='background:#C53030;'>❌ Cancel Policy</a>");
            }
            sb.append("</div>");

            sb.append("<div class='gw-card'>");
            sb.append("<div class='gw-card-title'>Step 3: Rating Breakdown &amp; Financial Summary</div>");
            sb.append("<div class='gw-form-grid'>");
            sb.append("<div>");
            sb.append("<p style='margin-bottom:8px;'><b>Transaction Status:</b> <span class='gw-status-badge status-").append(sub.getStatus()).append("'>").append(sub.getFormattedStatus()).append("</span></p>");
            if (sub.getPolicyNumber() != null) {
                sb.append("<p style='margin-bottom:8px; font-size:14px;'><b>Issued Policy #:</b> <b style='color:#0073B1;'>").append(sub.getPolicyNumber()).append("</b></p>");
            }
            sb.append("<p style='margin-bottom:8px;'><b>Job Type:</b> <span class='gw-pcf-tag'>").append(sub.getJobType()).append("</span></p>");
            sb.append("<p style='margin-bottom:8px;'><b>PolicyPeriod FixedID:</b> <code>").append(sub.getPolicyPeriodFixedId()).append("</code></p>");
            sb.append("<p style='margin-bottom:8px;'><b>Line of Business:</b> ").append(sub.getProductCode()).append("</p>");
            sb.append("<p style='margin-bottom:8px;'><b>Effective Period:</b> ").append(sub.getEffectiveDate()).append(" to ").append(sub.getExpirationDate()).append("</p>");
            sb.append("</div>");

            sb.append("<div style='background:#F7FAFC; padding:16px; border-radius:6px; border:1px solid #E2E8F0;'>");
            sb.append("<h4 style='color:#1C2B39; margin-bottom:12px;'>Gosu Rating Calculation Results</h4>");
            sb.append("<p style='display:flex; justify-content:space-between; margin-bottom:6px;'><span>Base Written Premium:</span> <b>$").append(sub.getBasePremium()).append("</b></p>");
            sb.append("<p style='display:flex; justify-content:space-between; margin-bottom:6px;'><span>Taxes &amp; Surcharges (8%):</span> <b>$").append(sub.getTaxesAndFees()).append("</b></p>");
            sb.append("<hr style='margin:8px 0; border:none; border-top:1px solid #CBD5E1;'>");
            sb.append("<p style='display:flex; justify-content:space-between; font-size:15px; color:#0073B1;'><span>Total Cost / Premium:</span> <b>$").append(sub.getTotalPremium()).append("</b></p>");
            sb.append("</div>");

            sb.append("</div></div>");
            sb.append("</form>");
        }

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderPolicyChangePage(String origJobNum, Map<String, String> params) {
        PolicyPeriod orig = dataStore.findSubmission(origJobNum);
        if (orig == null) return "Original Policy Period Not Found";

        if (params.containsKey("action") && "executeChange".equals(params.get("action"))) {
            String editEffDateStr = params.getOrDefault("editEffectiveDate", "2026-10-01");
            try {
                java.util.Date editEffDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(editEffDateStr);
                String newJobNum = "C000" + (int)(Math.random() * 9000 + 1000);
                PolicyPeriod changeBranch = orig.createPolicyChangeBranch(editEffDate, newJobNum);
                changeBranch.calculatePremium();
                changeBranch.setStatus("Issued");

                dataStore.createSubmission(changeBranch);
                return "<!DOCTYPE html><html><head><script>window.location.href='/?page=submission-wizard&jobNum=" + changeBranch.getJobNumber() + "&step=step3';</script></head></html>";
            } catch (java.text.ParseException e) {
                System.err.println("Failed to parse policy change effective date: " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Policy Change Wizard - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("submissions"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:720px; margin:0 auto;'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Start Policy Change Transaction <span class='gw-pcf-tag'>PolicyChangeWizard.pcf</span></div>");
        sb.append("</div>");

        sb.append("<form action='/?page=policy-change&jobNum=").append(origJobNum).append("' method='POST'>");
        sb.append("<input type='hidden' name='action' value='executeChange'>");

        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Policy Change Transaction Info</div>");
        sb.append("<p style='margin-bottom:8px;'><b>Parent Policy #:</b> ").append(orig.getPolicyNumber()).append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>PolicyPeriod FixedID:</b> <code>").append(orig.getPolicyPeriodFixedId()).append("</code></p>");
        sb.append("<p style='margin-bottom:8px;'><b>Account Holder:</b> ").append(orig.getAccount() != null ? orig.getAccount().getAccountHolderName() : "N/A").append("</p>");

        sb.append("<div class='gw-field' style='margin-top:16px;'><label>Change Effective Date <span class='required'>*</span></label><input type='date' name='editEffectiveDate' value='2026-10-01' required></div>");

        sb.append("<div style='margin-top:20px; display:flex; gap:10px;'>");
        sb.append("<button type='submit' class='gw-btn gw-btn-success'>⚡ Process Mid-Term Policy Change</button> ");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(origJobNum).append("' class='gw-btn gw-btn-secondary'>Cancel</a>");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</form>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderCancellationPage(String origJobNum, Map<String, String> params) {
        PolicyPeriod orig = dataStore.findSubmission(origJobNum);
        if (orig == null) return "Original Policy Period Not Found";

        if (params.containsKey("action") && "executeCancellation".equals(params.get("action"))) {
            String cancelEffDateStr = params.getOrDefault("cancelEffectiveDate", "2026-11-01");
            try {
                java.util.Date cancelEffDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(cancelEffDateStr);
                String newJobNum = "X000" + (int)(Math.random() * 9000 + 1000);
                PolicyPeriod cancelBranch = orig.createCancellationBranch(cancelEffDate, newJobNum);
                cancelBranch.setStatus("Issued");

                dataStore.createSubmission(cancelBranch);
                return "<!DOCTYPE html><html><head><script>window.location.href='/?page=submission-wizard&jobNum=" + cancelBranch.getJobNumber() + "&step=step3';</script></head></html>";
            } catch (java.text.ParseException e) {
                System.err.println("Failed to parse cancellation effective date: " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Cancellation Wizard - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("submissions"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:720px; margin:0 auto;'>");
        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Start Cancellation Transaction <span class='gw-pcf-tag'>CancellationWizard.pcf</span></div>");
        sb.append("</div>");

        sb.append("<form action='/?page=cancellation&jobNum=").append(origJobNum).append("' method='POST'>");
        sb.append("<input type='hidden' name='action' value='executeCancellation'>");

        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Cancellation Transaction Info</div>");
        sb.append("<p style='margin-bottom:8px;'><b>Parent Policy #:</b> ").append(orig.getPolicyNumber()).append("</p>");
        sb.append("<p style='margin-bottom:8px;'><b>PolicyPeriod FixedID:</b> <code>").append(orig.getPolicyPeriodFixedId()).append("</code></p>");
        sb.append("<p style='margin-bottom:8px;'><b>Account Holder:</b> ").append(orig.getAccount() != null ? orig.getAccount().getAccountHolderName() : "N/A").append("</p>");

        sb.append("<div class='gw-field' style='margin-top:16px;'><label>Cancellation Effective Date <span class='required'>*</span></label><input type='date' name='cancelEffectiveDate' value='2026-11-01' required></div>");

        sb.append("<div style='margin-top:20px; display:flex; gap:10px;'>");
        sb.append("<button type='submit' class='gw-btn' style='background:#C53030;'>❌ Confirm Policy Cancellation</button> ");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(origJobNum).append("' class='gw-btn gw-btn-secondary'>Cancel</a>");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</form>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }
}
