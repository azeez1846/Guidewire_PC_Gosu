package com.guidewire.pc.web;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.Activity;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.pcf.PCFParser;
import com.guidewire.pc.security.AuthenticationService;
import com.guidewire.pc.security.SecurityUtils;
import com.guidewire.pc.security.SessionManager;
import com.guidewire.pc.service.DataStoreService;
import com.guidewire.pc.service.SearchService;
import com.guidewire.pc.service.SearchService.SearchResult;
import com.guidewire.pc.service.SearchService.SearchResultType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GuidewirePolicyCenterServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(GuidewirePolicyCenterServlet.class.getName());
    private final PCFParser pcfParser;
    private final DataStoreService dataStore;

    public GuidewirePolicyCenterServlet(File rootDir) {
        this.pcfParser = new PCFParser(rootDir);
        this.dataStore = DataStoreService.getInstance();
        System.out.println("[Embedded Jetty PCF Engine] Loaded " + pcfParser.getPcfFiles().size() + " Guidewire PCF layout definitions.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.doGet");
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.doPost");
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.processRequest");
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        SecurityUtils.addSecurityHeaders(resp);

        String path = req.getRequestURI();
        String method = req.getMethod();

        if (path != null && path.startsWith("/pcf-studio")) {
            new PcfStudioServlet(new File(".")).service(req, resp);
            return;
        }

        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((key, vals) -> {
            if (vals != null && vals.length > 0) {
                params.put(key, vals[0]);
            }
        });

        // Session check via SessionManager
        boolean loggedIn = false;
        String currentSessionId = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("SESSIONID".equals(c.getName())) {
                    currentSessionId = c.getValue();
                    if (SessionManager.getInstance().validateSession(currentSessionId) != null) {
                        loggedIn = true;
                    }
                    break;
                }
            }
        }

        boolean isLoginPost = "POST".equalsIgnoreCase(method) &&
                ("/api/login".equalsIgnoreCase(path) || "/login".equalsIgnoreCase(path) ||
                "login".equalsIgnoreCase(params.get("page")) || "login".equalsIgnoreCase(req.getParameter("page")) ||
                (req.getParameter("username") != null && req.getParameter("password") != null));

        if (isLoginPost) {
            String u = req.getParameter("username");
            if (u == null || u.trim().isEmpty()) {
                u = params.get("username");
            }
            String p = req.getParameter("password");
            if (p == null) {
                p = params.get("password");
            }

            // Delegate entirely to AuthenticationService — never inline credentials here.
            AuthenticationService.AuthResult authResult =
                    AuthenticationService.getInstance().authenticate(u, p);

            if (authResult.isSuccess()) {
                String token = SessionManager.getInstance().createSession(authResult.getUsername());
                Cookie sessionCookie = new Cookie("SESSIONID", token);
                sessionCookie.setPath("/");
                sessionCookie.setHttpOnly(true);
                resp.addCookie(sessionCookie);
                resp.sendRedirect("/?page=desktop");
            } else {
                resp.sendRedirect("/?page=login&error=invalid");
            }
            return;
        }

        if ("/api/logout".equalsIgnoreCase(path)) {
            if (currentSessionId != null) {
                SessionManager.getInstance().invalidateSession(currentSessionId);
            }
            Cookie sessionCookie = new Cookie("SESSIONID", "");
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            resp.addCookie(sessionCookie);
            resp.sendRedirect("/?page=login");
            return;
        }

        if (path != null && path.startsWith("/pcf-studio")) {
            new PcfStudioServlet(new File(".")).service(req, resp);
            return;
        }

        if (!loggedIn && !params.getOrDefault("page", "").equals("login")) {
            resp.sendRedirect("/?page=login");
            return;
        }

        String page = params.getOrDefault("page", loggedIn ? "desktop" : "login");

        if ("search".equalsIgnoreCase(page)) {
            String q = params.get("q");
            if (q != null && !q.trim().isEmpty()) {
                SearchResult sr = SearchService.getInstance().executeSearch(q);
                if (sr.getResultType() == SearchResultType.DIRECT_ACCOUNT && sr.getDirectAccount() != null) {
                    resp.sendRedirect("/?page=account-detail&accNum=" + sr.getDirectAccount().getAccountNumber());
                    return;
                }
                if (sr.getResultType() == SearchResultType.DIRECT_SUBMISSION && sr.getDirectSubmission() != null) {
                    resp.sendRedirect("/?page=submission-wizard&jobNum=" + sr.getDirectSubmission().getJobNumber() + "&step=step1");
                    return;
                }
            }
        }

        String html = switch (page) {
            case "login" -> renderLoginPage(params.containsKey("error"));
            case "desktop" -> renderDesktopPage(params.getOrDefault("tab", "submissions"), params.get("q"));
            case "search" -> renderSearchPage(params.get("q"), params);
            case "new-account" -> renderNewAccountPage(params, resp);
            case "account-detail" -> renderAccountDetailPage(params.get("accNum"));
            case "new-submission" -> renderNewSubmissionPage(params, resp);
            case "submission-wizard" -> renderSubmissionWizard(params.get("jobNum"), params.getOrDefault("step", "step1"), params);
            case "copy-submission" -> handleCopySubmission(params.get("jobNum"), resp);
            case "policy-change" -> renderPolicyChangePage(params.get("jobNum"), params, resp);
            case "cancellation" -> renderCancellationPage(params.get("jobNum"), params, resp);
            case "uw-issues" -> renderUWIssuesPage();
            case "inland-marine" -> renderInlandMarinePage();
            case "fraud-dashboard" -> renderFraudDashboardPage();
            case "reinsurance-ledger" -> renderReinsuranceLedgerPage();
            case "features" -> renderFeaturesPage();
            case "dashboard" -> renderDashboardPage();
            default -> renderDesktopPage("submissions", null);
        };

        if (html == null) return; // Redirect performed

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("text/html; charset=UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }

    // HTML RENDERERS FOR JETTY SERVLET

    private String getHeaderCSS() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.getHeaderCSS");
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
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderLoginPage");
        return "<!DOCTYPE html><html><head><title>Login - Guidewire PolicyCenter (Jetty)</title>" + getHeaderCSS() + "</head>" +
                "<body style='display:flex; justify-content: center; align-items: center; min-height:100vh; background: #1C2B39;'>" +
                "<div style='width: 380px; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 10px 25px rgba(0,0,0,0.3); text-align: center;'>" +
                "<div style='display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 10px;'>" +
                "<span style='background:#0088CC; color:white; padding:6px 12px; border-radius:4px; font-weight:800; font-size:16px;'>GW</span>" +
                "<h2 style='font-size:20px; font-weight:700; color:#1C2B39;'>PolicyCenter</h2>" +
                "</div>" +
                "<p style='color:#718096; font-size:12px; margin-bottom:20px;'>Guidewire PolicyCenter 10.0 (Eclipse Jetty Server)</p>" +
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
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderHeader");
        return "<div class='gw-header'>" +
                "<div class='gw-brand'><span class='gw-brand-logo'>GW</span> Guidewire PolicyCenter <span style='font-size:11px; font-weight:normal; opacity:0.8;'>v10.0 (Jetty)</span></div>" +
                "<div class='gw-search-box'>" +
                "<form action='/' method='GET' style='display:flex; gap:6px;'>" +
                "<input type='hidden' name='page' value='search'>" +
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
                "<a href='/?page=search' class='gw-tab " + ("search".equals(activeTab) ? "active" : "") + "'>🔍 Search</a>" +
                "<a href='/?page=new-account' class='gw-tab'>+ New Account</a>" +
                "<a href='/?page=new-submission' class='gw-tab'>+ New Submission</a>" +
                "<a href='/?page=features' class='gw-tab " + ("features".equals(activeTab) ? "active" : "") + "' style='background:linear-gradient(135deg, #FFD700, #FF8C00); color:#000; font-weight:bold; border-radius:4px;'>🚀 Features (40)</a>" +
                "<a href='/swagger-ui' target='_blank' class='gw-tab' style='color:#38B6FF;'>⚡ Swagger REST API</a>" +
                "<a href='http://localhost:8082' target='_blank' class='gw-tab' style='color:#00C853;'>🗄️ H2 DB Console</a>" +
                "<a href='/pcf-studio/' target='_blank' class='gw-tab' style='color:#A7F3D0;'>🧩 Visual PCF Studio</a>" +
                "</div>";
    }

    private String renderSearchPage(String rawQuery, Map<String, String> params) {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderSearchPage");
        String query = rawQuery != null ? rawQuery.trim() : "";
        String entityFilter = params.getOrDefault("entity", "all");

        SearchResult sr = SearchService.getInstance().executeSearch(query);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Search &amp; QuickJump - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("search"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-content' style='max-width:1100px; margin:0 auto; padding:20px;'>");

        sb.append("<div class='gw-page-header'>");
        sb.append("<div class='gw-page-title'>Search &amp; QuickJump <span class='gw-pcf-tag'>Search.pcf</span></div>");
        sb.append("<div>");
        sb.append("<a href='/?page=new-account' class='gw-btn'>+ New Account</a> ");
        sb.append("<a href='/?page=new-submission' class='gw-btn gw-btn-secondary'>+ New Submission</a>");
        sb.append("</div>");
        sb.append("</div>");

        // Advanced Search Form Card
        sb.append("<div class='gw-card' style='background:#F8FAFC; border:1px solid #CBD5E0; margin-bottom:20px;'>");
        sb.append("<form action='/' method='GET' style='display:flex; gap:12px; align-items:flex-end; flex-wrap:wrap;'>");
        sb.append("<input type='hidden' name='page' value='search'>");
        sb.append("<div style='flex:2; min-width:260px;'>");
        sb.append("<label style='font-size:12px; font-weight:700; color:#4A5568; display:block; margin-bottom:4px;'>Search Term (Account #, Job #, Policy #, Name, FEIN)</label>");
        sb.append("<input type='text' name='q' class='gw-search-input' value='").append(SecurityUtils.escapeHtml(query)).append("' placeholder='e.g. A0001001, S0005001, Acme...' style='width:100%; padding:8px 12px; border:1px solid #CBD5E0; border-radius:4px;'>");
        sb.append("</div>");

        sb.append("<div style='flex:1; min-width:180px;'>");
        sb.append("<label style='font-size:12px; font-weight:700; color:#4A5568; display:block; margin-bottom:4px;'>Entity Type Filter</label>");
        sb.append("<select name='entity' style='width:100%; padding:8px 12px; border:1px solid #CBD5E0; border-radius:4px; background:#fff;'>");
        sb.append("<option value='all' ").append("all".equals(entityFilter) ? "selected" : "").append(">All Entities</option>");
        sb.append("<option value='accounts' ").append("accounts".equals(entityFilter) ? "selected" : "").append(">Accounts Only</option>");
        sb.append("<option value='submissions' ").append("submissions".equals(entityFilter) ? "selected" : "").append(">Submissions Only</option>");
        sb.append("</select>");
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<button type='submit' class='gw-btn' style='padding:8px 20px;'>🔍 Search Records</button>");
        sb.append("</div>");
        sb.append("</form>");
        sb.append("</div>");

        if (!query.isEmpty()) {
            List<Account> accounts = ("submissions".equals(entityFilter)) ? List.of() : sr.getMatchingAccounts();
            List<PolicyPeriod> submissions = ("accounts".equals(entityFilter)) ? List.of() : sr.getMatchingSubmissions();

            int totalMatches = accounts.size() + submissions.size();

            if (totalMatches > 0) {
                sb.append("<div class='gw-card' style='background:#EBF8FF; border-color:#90CDF4; color:#2B6CB0; margin-bottom:20px;'>");
                sb.append("Found <b>").append(totalMatches).append("</b> matching records (").append(accounts.size()).append(" Accounts, ").append(submissions.size()).append(" Submissions) for <b>\"").append(SecurityUtils.escapeHtml(query)).append("\"</b>.");
                sb.append("</div>");

                // Matching Accounts Section
                if (!accounts.isEmpty()) {
                    sb.append("<div class='gw-card'>");
                    sb.append("<div style='font-size:16px; font-weight:700; color:#1C2B39; margin-bottom:12px;'>🏢 Matching Accounts (").append(accounts.size()).append(")</div>");
                    sb.append("<table class='gw-table'>");
                    sb.append("<thead><tr><th>Account #</th><th>Account Holder Name</th><th>FEIN / Tax ID</th><th>City, State</th><th>Status</th><th>Action</th></tr></thead><tbody>");
                    for (Account a : accounts) {
                        sb.append("<tr>");
                        sb.append("<td><b><a href='/?page=account-detail&accNum=").append(a.getAccountNumber()).append("' style='color:#0073B1; text-decoration:none;'>").append(a.getAccountNumber()).append("</a></b></td>");
                        sb.append("<td>").append(SecurityUtils.escapeHtml(a.getAccountHolderName())).append("</td>");
                        sb.append("<td>").append(a.getFein() != null ? SecurityUtils.escapeHtml(a.getFein()) : "N/A").append("</td>");
                        sb.append("<td>").append(a.getCity() != null ? a.getCity() : "").append(", ").append(a.getState() != null ? a.getState() : "").append("</td>");
                        sb.append("<td><span class='gw-badge' style='background:#E6FFFA; color:#234E52;'>").append(a.getAccountStatus()).append("</span></td>");
                        sb.append("<td><a href='/?page=account-detail&accNum=").append(a.getAccountNumber()).append("' class='gw-btn gw-btn-secondary' style='padding:4px 8px; font-size:11px;'>View Account &gt;</a></td>");
                        sb.append("</tr>");
                    }
                    sb.append("</tbody></table>");
                    sb.append("</div>");
                }

                // Matching Submissions Section
                if (!submissions.isEmpty()) {
                    sb.append("<div class='gw-card'>");
                    sb.append("<div style='font-size:16px; font-weight:700; color:#1C2B39; margin-bottom:12px;'>📋 Matching Policy Submissions (").append(submissions.size()).append(")</div>");
                    sb.append("<table class='gw-table'>");
                    sb.append("<thead><tr><th>Job #</th><th>Policy #</th><th>Product</th><th>Base State</th><th>Status</th><th>Total Premium</th><th>Action</th></tr></thead><tbody>");
                    for (PolicyPeriod s : submissions) {
                        sb.append("<tr>");
                        sb.append("<td><b><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("&step=step1' style='color:#0073B1;'>").append(s.getJobNumber()).append("</a></b></td>");
                        sb.append("<td>").append(s.getPolicyNumber() != null ? s.getPolicyNumber() : "Draft").append("</td>");
                        sb.append("<td>").append(s.getProductCode()).append("</td>");
                        sb.append("<td>").append(s.getBaseState()).append("</td>");
                        sb.append("<td><span class='gw-badge' style='background:#EBF8FF; color:#2B6CB0;'>").append(s.getStatus()).append("</span></td>");
                        sb.append("<td>$").append(s.getTotalPremium() != null ? s.getTotalPremium() : "0.00").append("</td>");
                        sb.append("<td><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("&step=step1' class='gw-btn gw-btn-secondary' style='padding:4px 8px; font-size:11px;'>Open Wizard &gt;</a></td>");
                        sb.append("</tr>");
                    }
                    sb.append("</tbody></table>");
                    sb.append("</div>");
                }
            } else {
                sb.append("<div class='gw-card' style='background:#FFF5F5; border-color:#FEB2B2; color:#C53030; text-align:center; padding:30px;'>");
                sb.append("<div style='font-size:18px; font-weight:700; margin-bottom:8px;'>No records found matching \"").append(SecurityUtils.escapeHtml(query)).append("\"</div>");
                sb.append("<p style='color:#742A2A; margin-bottom:16px;'>Please check the Account or Submission number and try again, or create a new record.</p>");
                sb.append("<a href='/?page=new-account' class='gw-btn'>+ Create New Account</a> ");
                sb.append("<a href='/?page=new-submission' class='gw-btn gw-btn-secondary'>+ New Submission</a>");
                sb.append("</div>");
            }
        } else {
            sb.append("<div class='gw-card' style='text-align:center; color:#718096; padding:40px;'>");
            sb.append("Enter an Account Number (e.g. <b>A0001001</b>), Job Number (e.g. <b>S0005001</b>), or Policy Number above to jump directly, or search by name.");
            sb.append("</div>");
        }

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderDesktopPage(String tab, String searchQuery) {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderDesktopPage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Desktop - Guidewire PolicyCenter (Jetty)</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'>");
        sb.append("<div class='gw-sidebar'>");
        sb.append("<div class='gw-sidebar-title'>My Views</div>");
        sb.append("<a href='/?page=desktop&tab=submissions' class='gw-sidebar-item ").append("submissions".equals(tab) ? "active" : "").append("'>📋 My Submissions</a>");
        sb.append("<a href='/?page=desktop&tab=activities' class='gw-sidebar-item ").append("activities".equals(tab) ? "active" : "").append("'>⚡ My Activities</a>");
        sb.append("<a href='/?page=desktop&tab=accounts' class='gw-sidebar-item ").append("accounts".equals(tab) ? "active" : "").append("'>🏢 My Accounts</a>");
        sb.append("<div class='gw-sidebar-title'>Quick Actions</div>");
        sb.append("<a href='/?page=new-account' class='gw-sidebar-item'>+ Create New Account</a>");
        sb.append("<a href='/?page=new-submission' class='gw-sidebar-item'>+ New Submission</a>");
        sb.append("</div>");

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
            sb.append("Search Filter Active: <b>\"").append(SecurityUtils.escapeHtml(searchQuery)).append("\"</b>. <a href='/?page=desktop' style='color:#2B6CB0; font-weight:bold;'>Clear Filter</a>");
            sb.append("</div>");
        }

        sb.append("<div class='gw-toolbar'>");
        sb.append("<a href='/?page=desktop&tab=submissions' class='gw-btn ").append("submissions".equals(tab) ? "" : "gw-btn-secondary").append("'>My Submissions (").append(dataStore.getSubmissionCount()).append(")</a> ");
        sb.append("<a href='/?page=desktop&tab=activities' class='gw-btn ").append("activities".equals(tab) ? "" : "gw-btn-secondary").append("'>My Activities (").append(dataStore.getActivityCount()).append(")</a> ");
        sb.append("<a href='/?page=desktop&tab=accounts' class='gw-btn ").append("accounts".equals(tab) ? "" : "gw-btn-secondary").append("'>My Accounts (").append(dataStore.getAccountCount()).append(")</a>");
        sb.append("</div>");

        switch (tab) {
            case "submissions" -> {
                sb.append("<div class='gw-card'>");
                sb.append("<div class='gw-card-title'>Submissions &amp; Policy Transactions <span class='gw-pcf-tag'>DesktopSubmissionsLV.pcf</span></div>");
                sb.append("<table class='gw-table'>");
                sb.append("<thead><tr><th>Transaction #</th><th>Job Type</th><th>FixedID</th><th>Account Holder</th><th>Policy Line</th><th>Effective Date</th><th>Status</th><th>Total Premium</th><th>Action</th></tr></thead><tbody>");

                List<PolicyPeriod> subs = dataStore.getSubmissions();
                for (PolicyPeriod s : subs) {
                    if (searchQuery != null && !searchQuery.isEmpty()) {
                        String qLower = searchQuery.toLowerCase();
                        boolean matchJob = s.getJobNumber() != null && s.getJobNumber().toLowerCase().contains(qLower);
                        boolean matchAccount = s.getAccount() != null && s.getAccount().getAccountHolderName() != null && s.getAccount().getAccountHolderName().toLowerCase().contains(qLower);
                        boolean matchPolicy = s.getPolicyNumber() != null && s.getPolicyNumber().toLowerCase().contains(qLower);
                        if (!matchJob && !matchAccount && !matchPolicy) continue;
                    }
                    sb.append("<tr>");
                    sb.append("<td><b><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' style='color:#0073B1; text-decoration:none;'>").append(s.getJobNumber()).append("</a></b></td>");
                    sb.append("<td><span class='gw-pcf-tag'>").append(s.getJobType()).append("</span></td>");
                    sb.append("<td><code>").append(s.getPolicyPeriodFixedId()).append("</code></td>");
                    sb.append("<td>").append(s.getAccount() != null ? s.getAccount().getAccountHolderName() : "N/A").append("</td>");
                    sb.append("<td><b>").append(s.getProductCode()).append("</b></td>");
                    sb.append("<td>").append(s.getEffectiveDate()).append("</td>");
                    sb.append("<td><span class='gw-status-badge status-").append(s.getStatus()).append("'>").append(s.getFormattedStatus()).append("</span></td>");
                    sb.append("<td><b>$").append(s.getTotalPremium()).append("</b></td>");
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

    private String renderNewAccountPage(Map<String, String> params, HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderNewAccountPage");
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
            resp.sendRedirect("/?page=account-detail&accNum=" + created.getAccountNumber());
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Create New Account - Guidewire PolicyCenter (Jetty)</title>").append(getHeaderCSS()).append("</head><body>");
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
        sb.append("<div class='gw-field'><label>Street Address Line 1 <span class='required'>*</span></label><input type='text' id='addrLine1' name='addressLine1' placeholder='100 California St' required></div>");
        sb.append("<div class='gw-field'><label>Address Line 2</label><input type='text' id='addrLine2' name='addressLine2' placeholder='Suite 200'></div>");
        sb.append("<div class='gw-field'><label>City <span class='required'>*</span></label><input type='text' id='addrCity' name='city' placeholder='San Francisco' required></div>");
        sb.append("<div class='gw-field'><label>State <span class='required'>*</span></label><input type='text' id='addrState' name='state' placeholder='CA' required></div>");
        sb.append("<div class='gw-field'><label>ZIP / Postal Code <span class='required'>*</span></label><input type='text' id='addrZip' name='postalCode' placeholder='94111' required></div>");
        sb.append("<button type='button' onclick='openAddressIgModal()' class='gw-btn' style='background:linear-gradient(135deg, #10B981, #059669); color:#fff; width:100%; margin-top:8px;'>📍 Validate &amp; Standardize Address via IG</button>");
        sb.append("<div class='gw-field' style='margin-top:12px;'><label>Phone Number</label><input type='text' name='phone' placeholder='(415) 555-0100'></div>");
        sb.append("<div class='gw-field'><label>Email Address</label><input type='text' name='email' placeholder='billing@apex.com'></div>");
        sb.append("</div>");

        sb.append("</div></div>");
        sb.append("</form>");

        // Address Standardization IG Modal Dialog
        sb.append("<div id='addrModal' style='display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.6); z-index:9999; justify-content:center; align-items:center;'>");
        sb.append("<div style='background:#fff; border-radius:10px; width:520px; padding:24px; box-shadow:0 20px 25px -5px rgba(0,0,0,0.3); font-family:sans-serif;'>");
        sb.append("<div style='display:flex; justify-space-between; align-items:center; border-bottom:2px solid #E2E8F0; padding-bottom:12px; margin-bottom:16px;'>");
        sb.append("<h3 style='margin:0; color:#1E293B;'>📍 Guidewire Cloud Address IG Standardization</h3>");
        sb.append("<button onclick='closeAddressIgModal()' style='background:none; border:none; font-size:20px; cursor:pointer; color:#64748B;'>✕</button>");
        sb.append("</div>");
        sb.append("<div id='modalContent' style='color:#334155; line-height:1.6;'>Querying External Address Gateway...</div>");
        sb.append("<div id='modalActions' style='margin-top:20px; display:flex; justify-content:flex-end; gap:10px;'>");
        sb.append("<button onclick='closeAddressIgModal()' class='gw-btn gw-btn-secondary'>Close</button>");
        sb.append("</div>");
        sb.append("</div></div>");

        sb.append("<script>")
          .append("let lastStdResult = null;")
          .append("function openAddressIgModal() {")
          .append("  const modal = document.getElementById('addrModal');")
          .append("  const content = document.getElementById('modalContent');")
          .append("  modal.style.display = 'flex';")
          .append("  content.innerHTML = '<p style=\"color:#0284C7; font-weight:bold;\">⏳ Connecting to Guidewire Cloud Integration Gateway (IG)...</p>';")
          .append("  const line1 = document.getElementById('addrLine1').value || '100 California St';")
          .append("  const city = document.getElementById('addrCity').value || 'San Francisco';")
          .append("  const state = document.getElementById('addrState').value || 'CA';")
          .append("  const zip = document.getElementById('addrZip').value || '94111';")
          .append("  fetch('/rest/v1/ig/address-standardize', {")
          .append("    method: 'POST', credentials: 'same-origin', headers: {'Content-Type': 'application/json'},")
          .append("    body: JSON.stringify({ addressLine1: line1, city: city, state: state, postalCode: zip })")
          .append("  }).then(r => r.json()).then(data => {")
          .append("    lastStdResult = data.addressSpecs;")
          .append("    const s = data.addressSpecs || {};")
          .append("    content.innerHTML = `<div style=\"background:#F1F5F9; padding:12px; border-radius:6px; border-left:4px solid #10B981;\">")
          .append("      <p><b>Status:</b> <span style=\"color:#059669; font-weight:bold;\">${data.standardizationStatus || 'USPS_STANDARDIZED'}</span></p>")
          .append("      <p><b>Standardized Line 1:</b> ${s.standardizedAddressLine1 || line1}</p>")
          .append("      <p><b>City, State ZIP+4:</b> ${s.city || city}, ${s.state || state} ${s.postalCode || zip}-${s.postalCodePlus4 || '4102'}</p>")
          .append("      <p><b>County:</b> ${s.county || 'San Francisco County'}</p>")
          .append("      <p><b>USPS DPV Deliverable:</b> <span style=\"color:#047857; font-weight:bold;\">✔ ${s.deliveryPointValidationDPV || 'CONFIRMED_DELIVERABLE'}</span></p>")
          .append("      <p><b>Geocoding Coordinates:</b> Lat ${s.latitude || 37.7939}, Lon ${s.longitude || -122.3980}</p>")
          .append("      <p style=\"font-size:11px; color:#64748B; margin-top:8px;\">${data.gatewayMetadata || 'Guidewire Cloud IG v1.0'}</p>")
          .append("    </div>`;")
          .append("    document.getElementById('modalActions').innerHTML = '<button onclick=\"applyStandardizedAddress()\" class=\"gw-btn\" style=\"background:#10B981; color:#fff;\">✔ Auto-Fill Standardized Address</button> <button onclick=\"closeAddressIgModal()\" class=\"gw-btn gw-btn-secondary\">Close</button>';")
          .append("  }).catch(err => { content.innerHTML = '<p style=\"color:#DC2626;\">Error contacting IG: ' + err + '</p>'; });")
          .append("}")
          .append("function applyStandardizedAddress() {")
          .append("  if (lastStdResult) {")
          .append("    document.getElementById('addrLine1').value = lastStdResult.standardizedAddressLine1 || '';")
          .append("    document.getElementById('addrCity').value = lastStdResult.city || '';")
          .append("    document.getElementById('addrState').value = lastStdResult.state || '';")
          .append("    document.getElementById('addrZip').value = (lastStdResult.postalCode || '') + '-' + (lastStdResult.postalCodePlus4 || '');")
          .append("  }")
          .append("  closeAddressIgModal();")
          .append("}")
          .append("function closeAddressIgModal() { document.getElementById('addrModal').style.display = 'none'; }")
          .append("</script>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderAccountDetailPage(String accNum) {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderAccountDetailPage");
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

        List<PolicyPeriod> subs = dataStore.getSubmissions();
        sb.append("<div class='gw-card'>");
        sb.append("<div class='gw-card-title'>Submissions &amp; Policies under Account</div>");
        sb.append("<table class='gw-table'>");
        sb.append("<thead><tr><th>Transaction #</th><th>Job Type</th><th>FixedID</th><th>Policy Line</th><th>Effective Date</th><th>Status</th><th>Total Premium</th><th>Action</th></tr></thead><tbody>");
        boolean count = false;
        for (PolicyPeriod s : subs) {
            if (s.getAccount() != null && s.getAccount().getAccountNumber().equalsIgnoreCase(accNum)) {
                count = true;
                sb.append("<tr>");
                sb.append("<td><b><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' style='color:#0073B1;'>").append(s.getJobNumber()).append("</a></b></td>");
                sb.append("<td><span class='gw-pcf-tag'>").append(s.getJobType()).append("</span></td>");
                sb.append("<td><code>").append(s.getPolicyPeriodFixedId()).append("</code></td>");
                sb.append("<td><b>").append(s.getProductCode()).append("</b></td>");
                sb.append("<td>").append(s.getEffectiveDate()).append("</td>");
                sb.append("<td><span class='gw-status-badge status-").append(s.getStatus()).append("'>").append(s.getFormattedStatus()).append("</span></td>");
                sb.append("<td><b>$").append(s.getTotalPremium()).append("</b></td>");
                sb.append("<td><a href='/?page=submission-wizard&jobNum=").append(s.getJobNumber()).append("' class='gw-btn gw-btn-secondary' style='padding:3px 8px; font-size:11px;'>View Wizard</a></td>");
                sb.append("</tr>");
            }
        }
        if (!count) {
            sb.append("<tr><td colspan='8' style='text-align:center; color:#718096; padding:16px;'>No active submissions found for this account. <a href='/?page=new-submission&accNum=").append(accNum).append("'>Create One Now</a></td></tr>");
        }
        sb.append("</tbody></table></div>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderNewSubmissionPage(Map<String, String> params, HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderNewSubmissionPage");
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
            resp.sendRedirect("/?page=submission-wizard&jobNum=" + created.getJobNumber() + "&step=step1");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Create New Submission - Guidewire PolicyCenter (Jetty)</title>").append(getHeaderCSS()).append("</head><body>");
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

    private String handleCopySubmission(String jobNum, HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.handleCopySubmission");
        PolicyPeriod copied = com.guidewire.pc.service.PolicyLifecycleService.getInstance().copySubmission(jobNum);
        resp.sendRedirect("/?page=submission-wizard&jobNum=" + copied.getJobNumber() + "&step=step1");
        return null;
    }

    private String renderSubmissionWizard(String jobNum, String step, Map<String, String> params) {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderSubmissionWizard");
        PolicyPeriod sub = dataStore.findSubmission(jobNum);
        if (sub == null) return "Submission Not Found";

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
                    sub.setStatus(PCConstants.STATUS_QUOTED);
                    step = "step3";
                }
                case "bind" -> {
                    if (sub.getPolicyNumber() == null || sub.getPolicyNumber().trim().isEmpty()) {
                        sub.setPolicyNumber("POL-" + (int)(Math.random() * 900000 + 100000));
                    }
                    sub.setStatus(PCConstants.STATUS_ISSUED);
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
        sb.append("<div style='display:flex; align-items:center; gap:12px;'>");
        sb.append("<div>Status: <span class='gw-status-badge status-").append(sub.getStatus()).append("'>").append(sub.getFormattedStatus()).append("</span></div>");
        
        // OOTB Guidewire Actions Dropdown Menu
        sb.append("<div style='position:relative; display:inline-block;'>");
        sb.append("<button onclick='toggleActionsMenu()' class='gw-btn gw-btn-secondary' style='display:flex; align-items:center; gap:6px;'>Actions ▾</button>");
        sb.append("<div id='gwActionsDropdown' style='display:none; position:absolute; right:0; top:100%; background:white; min-width:200px; box-shadow:0 8px 16px rgba(0,0,0,0.15); border-radius:6px; border:1px solid #CBD5E0; z-index:100; padding:6px 0; margin-top:4px;'>");
        sb.append("<a href='/?page=copy-submission&jobNum=").append(jobNum).append("' style='display:block; padding:8px 16px; color:#2D3748; text-decoration:none; font-size:13px; font-weight:500;' onmouseover=\"this.style.background='#EDF2F7'\" onmouseout=\"this.style.background='transparent'\">📋 Copy Submission</a>");
        if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(sub.getStatus())) {
            sb.append("<a href='/?page=policy-change&jobNum=").append(jobNum).append("' style='display:block; padding:8px 16px; color:#2D3748; text-decoration:none; font-size:13px; font-weight:500;' onmouseover=\"this.style.background='#EDF2F7'\" onmouseout=\"this.style.background='transparent'\">⚡ Policy Change</a>");
            sb.append("<a href='/?page=cancellation&jobNum=").append(jobNum).append("' style='display:block; padding:8px 16px; color:#C53030; text-decoration:none; font-size:13px; font-weight:500;' onmouseover=\"this.style.background='#FFF5F5'\" onmouseout=\"this.style.background='transparent'\">❌ Cancel Policy</a>");
        }
        sb.append("</div>");
        sb.append("<script>function toggleActionsMenu(){var d=document.getElementById('gwActionsDropdown'); d.style.display=d.style.display==='none'?'block':'none';}</script>");
        sb.append("</div>");

        sb.append("</div></div>");

        sb.append("<div class='gw-wizard-steps'>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step1' class='gw-step ").append("step1".equals(step) ? "active" : "completed").append("'>1. Policy Info</a>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step2' class='gw-step ").append("step2".equals(step) ? "active" : ("step3".equals(step) ? "completed" : "")).append("'>2. Coverages &amp; Limits</a>");
        sb.append("<a href='/?page=submission-wizard&jobNum=").append(jobNum).append("&step=step3' class='gw-step ").append("step3".equals(step) ? "active" : "").append("'>3. Rating &amp; Quote Summary</a>");
        sb.append("</div>");

        switch (step) {
            case "step1" -> {
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
            }
            case "step2" -> {
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
            }
            default -> {
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
        }

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderPolicyChangePage(String origJobNum, Map<String, String> params, HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderPolicyChangePage");
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
                resp.sendRedirect("/?page=submission-wizard&jobNum=" + changeBranch.getJobNumber() + "&step=step3");
                return null;
            } catch (java.text.ParseException e) {
                System.err.println("Failed to parse policy change effective date: " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Policy Change Wizard - Guidewire PolicyCenter (Jetty)</title>").append(getHeaderCSS()).append("</head><body>");
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

    private String renderCancellationPage(String origJobNum, Map<String, String> params, HttpServletResponse resp) throws IOException {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderCancellationPage");
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
                resp.sendRedirect("/?page=submission-wizard&jobNum=" + cancelBranch.getJobNumber() + "&step=step3");
                return null;
            } catch (java.text.ParseException e) {
                System.err.println("Failed to parse policy cancellation effective date: " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Cancellation Wizard - Guidewire PolicyCenter (Jetty)</title>").append(getHeaderCSS()).append("</head><body>");
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

    private String renderUWIssuesPage() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderUWIssuesPage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Underwriting Issues Dashboard - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'><div class='gw-content'>");
        sb.append("<div class='gw-page-header'><div class='gw-page-title'>Underwriting Authority & Issues Dashboard <span class='gw-pcf-tag'>UWIssuesPanelSet.pcf</span></div></div>");

        sb.append("<div class='gw-card'><div class='gw-card-title'>Active Underwriting Referrals & Approvals</div>");
        sb.append("<table class='gw-table'><thead><tr><th>Issue Key</th><th>Code</th><th>Description</th><th>Severity</th><th>Status</th><th>Authority Required</th><th>Action</th></tr></thead><tbody>");

        List<com.guidewire.pc.model.UWIssue> issues = new java.util.ArrayList<>();
        for (PolicyPeriod p : dataStore.getSubmissions()) {
            issues.addAll(p.getUwIssues());
        }

        if (issues.isEmpty()) {
            sb.append("<tr><td colspan='7' style='text-align:center; padding:16px; color:#718096;'>No active underwriting issues found. All policies within standard limits.</td></tr>");
        } else {
            for (com.guidewire.pc.model.UWIssue issue : issues) {
                sb.append("<tr>");
                sb.append("<td><code>").append(issue.getIssueKey()).append("</code></td>");
                sb.append("<td><b>").append(issue.getIssueCode()).append("</b></td>");
                sb.append("<td>").append(issue.getShortDescription()).append("</td>");
                sb.append("<td><span class='gw-badge ").append(issue.isBlockingBind() ? "gw-badge-bound" : "gw-badge-draft").append("'>").append(issue.getSeverity()).append("</span></td>");
                sb.append("<td><b>").append(issue.getStatus()).append("</b></td>");
                sb.append("<td>").append(issue.getRequiredAuthorityLevel()).append("</td>");
                sb.append("<td>");
                if (issue.isOpen()) {
                    sb.append("<button onclick=\"fetch('/rest/v1/uw-issues/approve', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({issueKey:'").append(issue.getIssueKey()).append("', approvedBy:'su', reason:'Manager Approval'})}).then(()=>location.reload())\" class='gw-btn' style='padding:4px 8px; font-size:12px;'>Approve</button>");
                } else {
                    sb.append("<span style='color:#38A169; font-weight:bold;'>Approved</span>");
                }
                sb.append("</td></tr>");
            }
        }

        sb.append("</tbody></table></div></div></div></body></html>");
        return sb.toString();
    }

    private String renderInlandMarinePage() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderInlandMarinePage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Inland Marine Line - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'><div class='gw-content'>");
        sb.append("<div class='gw-page-header'><div class='gw-page-title'>Inland Marine (IM) Commercial Line <span class='gw-pcf-tag'>InlandMarineScreen.pcf</span></div></div>");

        sb.append("<div class='gw-card'><div class='gw-card-title'>Contractors Equipment & Scheduled Property</div>");
        sb.append("<p style='margin-bottom:12px;'>OOTB Commercial Inland Marine Line rating for mobile tools, heavy machinery, and high-value transit property.</p>");

        sb.append("<table class='gw-table'><thead><tr><th>Item #</th><th>Equipment Type</th><th>Description</th><th>Serial #</th><th>Stated Value</th><th>Deductible</th></tr></thead><tbody>");
        sb.append("<tr><td>1</td><td>HeavyMachinery</td><td>Caterpillar Excavator 320DL</td><td>SN-CAT-90412</td><td>$185,000.00</td><td>$1,000.00</td></tr>");
        sb.append("<tr><td>2</td><td>MobileTools</td><td>Jobsite Generator & Power Pack</td><td>SN-GEN-55410</td><td>$35,000.00</td><td>$500.00</td></tr>");
        sb.append("<tr><td>3</td><td>TransitCargo</td><td>High-Value Electronics Cargo</td><td>SN-TR-88120</td><td>$120,000.00</td><td>$2,500.00</td></tr>");
        sb.append("</tbody></table>");

        sb.append("<div style='margin-top:20px;'><a href='/?page=new-submission&productCode=InlandMarine' class='gw-btn'>Start Inland Marine Submission</a></div>");
        sb.append("</div></div></div></body></html>");
        return sb.toString();
    }

    private String renderFraudDashboardPage() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderFraudDashboardPage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>SIU Fraud Risk Dashboard - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'><div class='gw-content'>");
        sb.append("<div class='gw-page-header'><div class='gw-page-title'>SIU Fraud Risk & Referral Dashboard <span class='gw-pcf-tag'>SIUFraudDashboard.pcf</span></div></div>");

        sb.append("<div class='gw-card'><div class='gw-card-title'>Fraud Risk Scoring Engine Overview</div>");
        sb.append("<p style='margin-bottom:16px;'>Automated weighted risk scoring for identity anomalies, policy change velocity, backdated endorsements, and loss history.</p>");

        sb.append("<table class='gw-table'><thead><tr><th>Policy #</th><th>Product</th><th>Status</th><th>Fraud Risk Tier</th><th>Action</th></tr></thead><tbody>");
        for (PolicyPeriod p : dataStore.getSubmissions()) {
            if (p.getPolicyNumber() != null) {
                var score = com.guidewire.pc.service.SIURiskScoringEngine.getInstance().evaluatePolicyFraudRisk(p);
                sb.append("<tr>");
                sb.append("<td><code>").append(p.getPolicyNumber()).append("</code></td>");
                sb.append("<td>").append(p.getProductCode()).append("</td>");
                sb.append("<td>").append(p.getStatus()).append("</td>");
                sb.append("<td><span class='gw-badge ").append(score.isSiuHoldRequired() ? "gw-badge-canceled" : "gw-badge-issued").append("'>").append(score.getRiskTier()).append(" (Score: ").append(score.getTotalRiskScore()).append(")</span></td>");
                sb.append("<td><a href='/?page=submission-wizard&jobNum=").append(p.getJobNumber()).append("' class='gw-btn gw-btn-secondary' style='padding:4px 8px; font-size:12px;'>View Policy</a></td>");
                sb.append("</tr>");
            }
        }
        sb.append("</tbody></table></div></div></div></body></html>");
        return sb.toString();
    }

    private String renderReinsuranceLedgerPage() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderReinsuranceLedgerPage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Reinsurance Ledger & Treaties - Guidewire PolicyCenter</title>").append(getHeaderCSS()).append("</head><body>");
        sb.append(renderHeader("desktop"));

        sb.append("<div class='gw-main-container'><div class='gw-content'>");
        sb.append("<div class='gw-page-header'><div class='gw-page-title'>Reinsurance Treaty Layering & Cession Ledger <span class='gw-pcf-tag'>ReinsuranceLedger.pcf</span></div></div>");

        sb.append("<div class='gw-card'><div class='gw-card-title'>Active Reinsurance Treaties</div>");
        sb.append("<table class='gw-table'><thead><tr><th>Treaty #</th><th>Treaty Name</th><th>Type</th><th>Reinsurer</th><th>Attachment Point</th><th>Layer Limit</th><th>Cession %</th></tr></thead><tbody>");

        var treaties = com.guidewire.pc.service.ReinsuranceLedgerEngine.getInstance().getActiveTreaties();
        for (var t : treaties) {
            sb.append("<tr>");
            sb.append("<td><code>").append(t.getTreatyNumber()).append("</code></td>");
            sb.append("<td><b>").append(t.getTreatyName()).append("</b></td>");
            sb.append("<td>").append(t.getTreatyType()).append("</td>");
            sb.append("<td>").append(t.getReinsurerName()).append("</td>");
            sb.append("<td>$").append(t.getAttachmentPoint()).append("</td>");
            sb.append("<td>$").append(t.getLayerLimit()).append("</td>");
            sb.append("<td>").append((int)(t.getCessionPercentage() * 100)).append("%</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table></div></div></div></body></html>");
        return sb.toString();
    }

    private String renderFeaturesPage() {
        LOGGER.log(Level.FINE, "→ GuidewirePolicyCenterServlet.renderFeaturesPage");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>Enterprise Features Suite (24) - Guidewire PolicyCenter</title>")
          .append(getHeaderCSS())
          .append("<style>")
          .append(".gw-feature-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(420px, 1fr)); gap: 20px; margin-top: 20px; }")
          .append(".gw-feature-card { background: #FFFFFF; border-radius: 8px; padding: 20px; border: 1px solid #E2E8F0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); display: flex; flex-direction: column; justify-content: space-between; transition: transform 0.2s, box-shadow 0.2s; }")
          .append(".gw-feature-card:hover { transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border-color: #CBD5E0; }")
          .append(".gw-feature-title { font-size: 16px; font-weight: 700; color: #1A365D; margin-bottom: 6px; display: flex; justify-content: space-between; align-items: flex-start; }")
          .append(".gw-feature-desc { font-size: 13px; color: #4A5568; margin-bottom: 12px; line-height: 1.5; }")
          .append(".gw-feature-purpose { font-size: 12px; color: #718096; background: #F7FAFC; padding: 8px 10px; border-left: 3px solid #3182CE; border-radius: 4px; margin-bottom: 14px; }")
          .append(".gw-feature-form { background: #EDF2F7; padding: 12px; border-radius: 6px; margin-top: auto; }")
          .append(".gw-form-row { display: flex; flex-direction: column; gap: 4px; margin-bottom: 8px; }")
          .append(".gw-form-label { font-size: 11px; font-weight: 600; color: #2D3748; }")
          .append(".gw-form-input { padding: 6px 8px; font-size: 12px; border: 1px solid #CBD5E0; border-radius: 4px; background: #FFF; }")
          .append(".gw-result-box { margin-top: 10px; padding: 10px; background: #1A202C; color: #68D391; font-family: monospace; font-size: 11px; border-radius: 4px; max-height: 180px; overflow-y: auto; display: none; white-space: pre-wrap; }")
          .append(".gw-filter-btn { padding: 8px 14px; border: 1px solid #CBD5E0; background: #FFF; color: #4A5568; border-radius: 20px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; }")
          .append(".gw-filter-btn.active { background: #3182CE; color: #FFF; border-color: #3182CE; }")
          .append("</style>")
          .append("</head><body>");

        sb.append(renderHeader("features"));

        sb.append("<div class='gw-main-container'><div class='gw-content'>");
        sb.append("<div class='gw-page-header'><div class='gw-page-title'>🚀 Guidewire PolicyCenter Enterprise Features Suite <span class='gw-pcf-tag'>EnterpriseFeaturesSuite.pcf</span></div>");
        sb.append("<div style='color:#718096; font-size:14px; margin-top:4px;'>Comprehensive suite of 40 enterprise-grade insurance industry engines & accelerators with interactive TypeScript/REST execution drivers.</div></div>");

        // Filter Bar
        sb.append("<div style='display:flex; gap:10px; margin-top:16px; flex-wrap:wrap;'>");
        sb.append("<button onclick=\"filterFeatures('all')\" class='gw-filter-btn active' id='btn-all'>All Features (40)</button>");
        sb.append("<button onclick=\"filterFeatures('Specialty Lines')\" class='gw-filter-btn' id='btn-specialty'>Specialty Lines</button>");
        sb.append("<button onclick=\"filterFeatures('Commercial Rating & Retrospective')\" class='gw-filter-btn' id='btn-rating'>Commercial Rating</button>");
        sb.append("<button onclick=\"filterFeatures('Underwriting & Risk')\" class='gw-filter-btn' id='btn-uw'>Underwriting & Risk</button>");
        sb.append("<button onclick=\"filterFeatures('Compliance & Regulatory')\" class='gw-filter-btn' id='btn-compliance'>Compliance & Regulatory</button>");
        sb.append("<button onclick=\"filterFeatures('Reinsurance & Portfolio')\" class='gw-filter-btn' id='btn-reinsurance'>Reinsurance & Portfolio</button>");
        sb.append("</div>");

        // Feature Cards Grid
        sb.append("<div class='gw-feature-grid' id='featureContainer'></div>");

        // Client-side JavaScript rendering driven by TypeScript catalog definitions
        sb.append("<script>")
          .append("const FEATURES = [")
          .append("{id:'ai-referral', title:'AI Automated Underwriting Referral & Decision Assistant', category:'Underwriting & Risk', endpoint:'/rest/v1/ai-referral/evaluate', desc:'AI-driven decision assistant evaluating loss history, hazard class codes, and risk scores to provide automated binding recommendations or manager escalation.', purpose:'Accelerates underwriting triage and provides predictive AI risk explanations.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'riskScore',l:'Risk Score (0-100)',t:'number',v:'78'}]},")
          .append("{id:'esignature', title:'DocuSign E-Signature Envelope Integration Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/esignature/create', desc:'Generates secure DocuSign e-signature envelope packages for instant digital policy binding and statutory application execution.', purpose:'Automates digital policy binding via DocuSign integration.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'signerEmail',l:'Signer Email',t:'text',v:'policyholder@example.com'}]},")
          .append("{id:'geospatial', title:'Geospatial GIS Risk & Wildfire Exposure Service', category:'Reinsurance & Portfolio', endpoint:'/rest/v1/geospatial/risk', desc:'Evaluates GIS coordinates against live wildfire risk zones, coastal storm surge maps, and sinkhole fault lines.', purpose:'Protects carrier portfolio concentration via location hazard scoring.', inputs:[{n:'address',l:'Property Address',t:'text',v:'100 Coastal Hwy, Malibu, CA 90265'}]},")
          .append("{id:'payment-gateway', title:'Stripe Payment Gateway Installment Processing', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/payment/process', desc:'Processes real-time credit card, ACH, and installment payments with tokenized security via Stripe Gateway.', purpose:'Digital premium payment collection upon policy binding.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'amount',l:'Payment Amount ($)',t:'number',v:'450.00'}]},")
          .append("{id:'vin-decoder', title:'NHTSA VIN Decoder & Vehicle Safety Feature Lookup', category:'Specialty Lines', endpoint:'/rest/v1/vin/decode', desc:'Decodes 17-digit VIN numbers to populate vehicle make, model, trim, NHTSA safety ratings, and ADAS anti-theft equipment.', purpose:'Automates vehicle schedule data entry and safety credits.', inputs:[{n:'vin',l:'17-Digit VIN',t:'text',v:'1G1YC2D45R5100001'}]},")
          .append("{id:'telematics', title:'Auto Fleet Telematics UBI Discount Engine', category:'Specialty Lines', endpoint:'/rest/v1/telematics/evaluate', desc:'Evaluates UBI telemetry (hard braking, rapid accelerations, late night hours) for dynamic rate discounts (-20%) or surcharges (+15%).', purpose:'Commercial & Personal Auto UBI telematics scoring for driver behavior.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'hardBrakesPer1k',l:'Hard Brakes / 1k mi',t:'number',v:'1.0'},{n:'rapidAccelerationsPer1k',l:'Rapid Accel / 1k mi',t:'number',v:'0.5'},{n:'lateNightDrivingPct',l:'Late Night %',t:'number',v:'0.02'}]},")
          .append("{id:'tria', title:'TRIA Opt-In/Opt-Out Mandatory Disclosure Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/tria/evaluate', desc:'Calculates certified federal terrorism 3.5% surcharges and attaches TRIA-COV-2026 or rejection exclusion forms.', purpose:'Mandatory U.S. Federal TRIA Terrorism Disclosure Compliance.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'optInTerrorismCoverage',l:'Opt-In TRIA',t:'select',o:[{l:'Yes (3.5% Surcharge)',v:true},{l:'No (Rejection Form)',v:false}],v:true},{n:'triaRatePct',l:'TRIA Rate',t:'number',v:'0.035'}]},")
          .append("{id:'pollution', title:'Environmental Pollution Liability Hazard Engine', category:'Specialty Lines', endpoint:'/rest/v1/pollution/assess', desc:'Assesses Underground Storage Tanks (UST), chemical volume, and waterway proximity to compute EIL multipliers.', purpose:'Specialty Underwriting for Environmental Impairment Liability (EIL).', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'ustCount',l:'UST Tank Count',t:'number',v:'4'},{n:'chemicalHazardScore',l:'Hazard Score (1-10)',t:'number',v:'8'},{n:'proximityToWaterwayMiles',l:'Waterway Dist (mi)',t:'number',v:'0.4'}]},")
          .append("{id:'cyber', title:'Cyber Liability Ransomware & Breach Sub-Limit Engine', category:'Specialty Lines', endpoint:'/rest/v1/cyber/evaluate', desc:'Evaluates security controls (MFA, backups, EDR). Enforces $250k ransomware cap and +30% surcharge if MFA is missing.', purpose:'Cyber Insurance risk posture evaluation & ransomware capping.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'mfaEnabled',l:'MFA Enabled',t:'select',o:[{l:'Enabled (-15% Credit)',v:true},{l:'Disabled (+30% Surcharge, $250k Cap)',v:false}],v:false},{n:'offlineBackupsDaily',l:'Daily Offsite Backups',t:'select',o:[{l:'Yes',v:true},{l:'No',v:false}],v:true}]},")
          .append("{id:'flood', title:'Flood Zone Risk & NFIP Elevation Certificate Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/flood/rate', desc:'Evaluates FEMA Flood Zones (A, V, X) and Elevation Certificate differentials relative to Base Flood Elevation (BFE).', purpose:'Commercial & Personal Property Flood Risk Rating.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'floodZone',l:'FEMA Flood Zone',t:'text',v:'Zone A'},{n:'lowestFloorElevationFt',l:'Lowest Floor Elev (ft)',t:'number',v:'14.0'},{n:'baseFloodElevationBFE',l:'Base Flood Elev BFE (ft)',t:'number',v:'12.0'}]},")
          .append("{id:'coinsurance', title:'Property Coinsurance Clause Penalty Engine', category:'Underwriting & Risk', endpoint:'/rest/v1/coinsurance/evaluate', desc:'Evaluates building valuation against 80%/90% coinsurance clauses to apply claim payout penalty reductions.', purpose:'Commercial property under-insurance claim penalty calculation.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'buildingValue',l:'Building Value ($)',t:'number',v:'2000000'},{n:'buildingLimit',l:'Carried Limit ($)',t:'number',v:'1200000'},{n:'coinsurancePct',l:'Coinsurance %',t:'number',v:'0.80'},{n:'claimLoss',l:'Claim Loss ($)',t:'number',v:'500000'}]},")
          .append("{id:'deductible-buyback', title:'Policy Deductible Buyback & Surcharge Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/deductible/buyback', desc:'Calculates actuarial buyback surcharge factors when policyholders reduce high deductibles ($10k down to $1k).', purpose:'Deductible buyback exposure pricing.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'originalDeductible',l:'Original Ded ($)',t:'number',v:'10000'},{n:'targetDeductible',l:'Target Ded ($)',t:'number',v:'1000'}]},")
          .append("{id:'uw-escalation', title:'Multi-Tier UW Authority Escalation Workflow Engine', category:'Underwriting & Risk', endpoint:'/rest/v1/uw/escalation', desc:'Escalates approval hierarchy enforcing dual sign-offs for TIV > $10M or fraud risk scores >= 70.', purpose:'Underwriting referral governance for large commercial risk.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'totalInsuredValue',l:'Total Insured Value TIV ($)',t:'number',v:'15000000'},{n:'riskScore',l:'Risk Score (0-100)',t:'number',v:'75'}]},")
          .append("{id:'sliding-dividend', title:'Loss Sensitive Sliding Scale Policyholder Dividend Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/dividend/calculate', desc:'Evaluates commercial retrospective rating plans returning up to 15% dividend returns for low loss ratios (<30%).', purpose:'Commercial policyholder dividend return calculations.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'incurredLosses',l:'Incurred Losses ($)',t:'number',v:'2500'}]},")
          .append("{id:'rate-cap', title:'Renewal Rate Impact Capping & Transition Smoothing Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/rate-cap/apply', desc:'Enforces maximum annual renewal rate increase caps (e.g. max +10%) to prevent customer churn.', purpose:'Renewal price hike smoothing & carrier subsidy calculation.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'uncappedProposedPremium',l:'Uncapped Proposed Prem ($)',t:'number',v:'15000'},{n:'maxRateCapPct',l:'Max Cap % (e.g. 0.10)',t:'number',v:'0.10'}]},")
          .append("{id:'siu-fraud', title:'SIU Fraud Risk Scoring Engine', category:'Underwriting & Risk', endpoint:'/rest/v1/siu-fraud/evaluate', desc:'Calculates weighted fraud scores for identity anomalies, change velocity, and loss history.', purpose:'Special Investigation Unit (SIU) fraud detection.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'reinsurance-ledger', title:'Reinsurance Treaty Layering & Cession Ledger', category:'Reinsurance & Portfolio', endpoint:'/rest/v1/reinsurance/calculate', desc:'Applies Quota Share, Excess of Loss (XOL), and Catastrophe Treaty layers to partition policy premiums and loss cessions.', purpose:'Automated reinsurance bordereau & treaty accounting.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'grossPremium',l:'Gross Subject Prem ($)',t:'number',v:'100000'}]},")
          .append("{id:'cat-accumulation', title:'Real-Time Catastrophe (CAT) Accumulation Engine', category:'Reinsurance & Portfolio', endpoint:'/rest/v1/cat/evaluate', desc:'Aggregates geospatial Total Insured Value (TIV) across coastal hurricane and earthquake fault zones.', purpose:'Geospatial CAT exposure concentration management.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'tiv',label:'TIV ($)',t:'number',v:'5000000'},{n:'catZone',label:'CAT Zone Code',t:'text',v:'FL-COASTAL-01'}]},")
          .append("{id:'commercial-audit', title:'Commercial Premium Audit & Final Adjustment Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/audit/calculate', desc:'Compares estimated vs actual gross sales/payroll to calculate final audit additional or return premiums.', purpose:'Audited commercial line premium reconciliations.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'actualExposure',l:'Audited Payroll ($)',t:'number',v:'1200000'},{n:'estimatedExposure',l:'Estimated Payroll ($)',t:'number',v:'1000000'},{n:'auditRate',l:'Audit Rate / $100',t:'number',v:'2.50'}]},")
          .append("{id:'experience-mod', title:'Experience Rating Mod (e-Mod) NCCI Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/emod/calculate', desc:'Computes Workers Compensation Experience Modification Factor (e-Mod) using NCCI actual vs expected loss formulas.', purpose:'Workers Comp experience rating mod calculation.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'actualLosses',l:'Actual Losses ($)',t:'number',v:'17000'},{n:'expectedLosses',l:'Expected Losses ($)',t:'number',v:'20000'}]},")
          .append("{id:'inland-marine', title:'Sub-line Inland Marine Rating & Equipment Engine', category:'Specialty Lines', endpoint:'/rest/v1/inland-marine/rate', desc:'Rates contractor heavy machinery, transit cargo, and mobile tools with specific deductible factors.', purpose:'Inland Marine commercial equipment rating.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'proration-refund', title:'Policy Cancellation Short-Rate vs Pro-Rata Refund Calculator', category:'Compliance & Regulatory', endpoint:'/rest/v1/cancellation/refund', desc:'Calculates unearned premium return refunds comparing standard Pro-Rata factor vs Short-Rate 90% penalty table.', purpose:'State statutory cancellation refund compliance.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'cancellationReason',l:'Reason',t:'select',o:[{l:'Insured Request (Short-Rate)',v:'Insured Request'},{l:'Non-Payment (Pro-Rata)',v:'Non-Payment'}],v:'Insured Request'}]},")
          .append("{id:'multinational-ledger', title:'Multi-Currency Multinational Local Policy Ledger', category:'Reinsurance & Portfolio', endpoint:'/rest/v1/multinational/ledger', desc:'Manages global master umbrella policy allocations across local foreign currencies (EUR, GBP, JPY) with FX rates.', purpose:'Multi-national admitted policy ledger management.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'commission-split', title:'Multi-Payee Commission Split Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/commission/split', desc:'Splits gross agency commission across wholesale brokers, MGAs, and producing agents.', purpose:'Producer & agency commission split accounting.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'commissionRate',l:'Commission Rate',t:'number',v:'0.15'}]},")
          .append("{id:'oos-merge', title:'Out-of-Sequence (OOS) Endorsement Merge Engine', category:'Underwriting & Risk', endpoint:'/rest/v1/oos/merge', desc:'Merges effective date endorsement conflicts when backdated policy changes overlap on the timeline slice.', purpose:'Guidewire core OOS endorsement timeline merging.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'renewal-eligibility', title:'Pre-Renewal Portfolio Health Batch Process Engine', category:'Reinsurance & Portfolio', endpoint:'/rest/v1/renewal/eligibility', desc:'Automated batch process scanning portfolio policies 90 days prior to expiration to score renewal profitability.', purpose:'Pre-renewal portfolio screening & health scoring.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'},{n:'proposedRateIncreasePct',l:'Proposed Rate Increase',t:'number',v:'0.18'}]},")
          .append("{id:'uw-override', title:'Underwriting Override Rating Engine & Audit Trail', category:'Underwriting & Risk', endpoint:'/rest/v1/uw/escalation', desc:'Tracks manual underwriter rate overrides, schedule credits, and authority level approval logs.', purpose:'Underwriter override audit trail compliance.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'group-coi', title:'Automated Group Account COI Issuance Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/coi/generate', desc:'Batch generates ACORD 25 COI documents across multi-location commercial policyholder schedules.', purpose:'Automated Certificate of Insurance mass generation.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'forms-inference', title:'Policy Form Inference & Attachment Rules Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/forms/infer', desc:'Evaluates policy coverages, state jurisdictions, and limits to dynamically attach statutory policy forms.', purpose:'Automated policy form inference & mandatory endorsement attachment.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'ig-vehicle-details', title:'Guidewire Cloud Integration Gateway (IG) — Vehicle & MVR Vendor Gateway', category:'Specialty Lines', endpoint:'/rest/v1/ig/vehicle-details', desc:'Integration Gateway (IG) microservice layer executing outbound real-time DMV MVR driver lookup, VIN spec verification, safety scores, and auto underwriting tier recommendations.', purpose:'Connects PolicyCenter Personal Auto and Commercial Auto submissions to external MVR/DMV data vendors via the Integration Gateway microservice JAR.', inputs:[{n:'vin',l:'Vehicle VIN',t:'text',v:'1FA6P8CF0R5100001'},{n:'vehicleYear',l:'Model Year',t:'number',v:'2025'},{n:'vehicleMake',l:'Make',t:'text',v:'Ford'},{n:'vehicleModel',l:'Model',t:'text',v:'Mustang GT'},{n:'driverLicenseNumber',l:'Driver License #',t:'text',v:'DL-CA-9948123'},{n:'driverState',l:'State',t:'text',v:'CA'}]},")
          .append("{id:'ig-address-standardization', title:'Guidewire Cloud Integration Gateway (IG) — Address Standardization & Geocoding Gateway', category:'Compliance & Regulatory', endpoint:'/rest/v1/ig/address-standardize', desc:'Integration Gateway (IG) microservice executing real-time USPS DPV deliverability validation, address standardization, ZIP+4 resolution, and lat/long geocoding.', purpose:'Standardizes policyholder addresses and resolves geospatial risk coordinates via the Integration Gateway microservice JAR.', inputs:[{n:'addressLine1',l:'Address Line 1',t:'text',v:'100 California St'},{n:'city',l:'City',t:'text',v:'San Francisco'},{n:'state',l:'State',t:'text',v:'CA'},{n:'postalCode',l:'ZIP Code',t:'text',v:'94111'}]},")
          .append("{id:'ig-credit-fraud', title:'Guidewire Cloud Integration Gateway (IG) — Credit Score & OFAC Sanctions Gateway', category:'Underwriting & Risk', endpoint:'/rest/v1/ig/credit-fraud', desc:'Integration Gateway microservice executing real-time credit bureau queries (Experian/D&B) and US Treasury OFAC sanctions watchlist checks.', purpose:'Computes Credit-Based Insurance Scores (CBIS) and enforces OFAC compliance via creditfraud_IG-1.0.0.jar.', inputs:[{n:'accountHolderName',l:'Account Name',t:'text',v:'Apex Global Industrial'},{n:'feinOrSsn',l:'FEIN / SSN',t:'text',v:'98-7654321'},{n:'orgType',l:'Organization Type',t:'text',v:'Corporation'},{n:'state',l:'State',t:'text',v:'CA'}]},")
          .append("{id:'acord-ingestion', title:'ACORD 125/126 Application Ingestion Engine', category:'Compliance & Regulatory', endpoint:'/rest/v1/acord/ingest', desc:'Automated document intake engine parsing ACORD 125 commercial applications and pre-populating submissions.', purpose:'Instant policy submission creation from ACORD applications.', inputs:[{n:'acordFormType',l:'ACORD Form Type',t:'text',v:'ACORD_125_COMMERCIAL_AUTO'},{n:'applicantName',l:'Applicant Name',t:'text',v:'Apex Industrial Logistics LLC'},{n:'fein',l:'FEIN',t:'text',v:'98-7654321'},{n:'lineOfBusiness',l:'Line of Business',t:'text',v:'CommercialAuto'},{n:'requestedLimit',l:'Requested Limit ($)',t:'number',v:'1000000'}]},")
          .append("{id:'oos-timeline-visualizer', title:'Out-of-Sequence (OOS) Endorsement Timeline Visualizer', category:'Underwriting & Risk', endpoint:'/rest/v1/oos/timeline-visualizer', desc:'Renders graphical effective date timeline slices and backdated endorsement conflict merge resolutions.', purpose:'Visualizes mid-term endorsement timeline slices on Policy Changes.', inputs:[{n:'jobNumber',l:'Job #',t:'text',v:'S0001001'}]},")
          .append("{id:'ig-telematics', title:'Guidewire Cloud Integration Gateway (IG) — Commercial IoT Telematics Gateway', category:'Specialty Lines', endpoint:'/rest/v1/ig/telematics', desc:'Integration Gateway microservice ingesting Commercial Auto IoT fleet telemetry (hard braking, speeding, mileage).', purpose:'Calculates Usage-Based Insurance (UBI) discounts via telematics_IG-1.0.0.jar.', inputs:[{n:'fleetId',l:'Fleet ID',t:'text',v:'FLT-CA-90812'},{n:'accountNumber',l:'Account #',t:'text',v:'A0001001'},{n:'activeVehiclesCount',l:'Active Vehicles',t:'number',v:'15'}]},")
          .append("{id:'claim-fnol', title:'ClaimCenter FNOL & Loss Ratio Triage Engine', category:'Underwriting & Risk', endpoint:'/rest/v1/claims/fnol', desc:'Ingests First Notice of Loss (FNOL) claims, computes policy loss ratio percentages, and triggers automated Underwriting Holds if Loss Ratio > 75%.', purpose:'Real-time FNOL triage & loss ratio enforcement.', inputs:[{n:'policyNumber',l:'Policy #',t:'text',v:'POL-849102'},{n:'claimType',l:'Claim Type',t:'text',v:'COLLISION'},{n:'lossAmount',l:'Loss Amount ($)',t:'number',v:'2500'},{n:'description',l:'Description',t:'text',v:'Fender bender at intersection'}]},")
          .append("{id:'policy-renewal-mta', title:'Automated Policy Renewal & Mid-Term Endorsement Engine', category:'Commercial Rating & Retrospective', endpoint:'/rest/v1/policy/renewal', desc:'Evaluates policy renewal eligibility, applies inflation rate adjustments, and computes calendar-day pro-rata MTA premium adjustments.', purpose:'Automated policy renewals and pro-rata endorsement calculations.', inputs:[{n:'policyNumber',l:'Policy #',t:'text',v:'POL-849102'},{n:'currentPremium',l:'Current Premium ($)',t:'number',v:'2450.00'},{n:'baseInflationPercent',l:'Inflation Rate %',t:'number',v:'5.00'}]},")
          .append("{id:'graphql-gateway', title:'GraphQL API Gateway & Live Query Sandbox', category:'Compliance & Regulatory', endpoint:'/graphql', desc:'Serves flexible GraphQL queries (policies, accounts, claims, telematics) and mutation operations over HTTP POST.', purpose:'GraphQL API query and mutation execution.', inputs:[{n:'query',l:'GraphQL Query / Mutation',t:'text',v:'query { policy { policyNumber, status, annualPremium } }'}]},")
          .append("{id:'ai-triage-agent', title:'Autonomous AI Underwriting Triage Agent (AGY SDK)', category:'Underwriting & Risk', endpoint:'/rest/v1/ai-triage/evaluate', desc:'Autonomous multi-agent system synthesizing telematics scores, coastal flood risks, and claim history to output Straight-Through Binding, UW Referral, or Decline decisions.', purpose:'Multi-agent AI underwriting triage decisioning.', inputs:[{n:'submissionId',l:'Submission ID',t:'text',v:'SUB-001'},{n:'policyNumber',l:'Policy #',t:'text',v:'POL-849102'},{n:'driverScore',l:'Telematics Score (0-100)',t:'number',v:'70'},{n:'highFloodZone',l:'High Flood Zone A',t:'select',o:[{l:'True (Zone A Coastal)',v:true},{l:'False (Low Risk)',v:false}],v:true}]},")
          .append("{id:'analytics-portal', title:'PolicyCenter Intelligence & Analytics Web Dashboard', category:'Reinsurance & Portfolio', endpoint:'/graphql', desc:'Real-time Glassmorphism web dashboard featuring telematics driver velocity charts, coastal flood heatmaps, and financial KPI metrics.', purpose:'Interactive UI dashboard for policy administration and telemetry analytics.', inputs:[]}")
          .append("];")
          .append("function renderCards(filterCategory) {")
          .append("  const container = document.getElementById('featureContainer');")
          .append("  container.innerHTML = '';")
          .append("  const filtered = filterCategory === 'all' ? FEATURES : FEATURES.filter(f => f.category === filterCategory);")
          .append("  filtered.forEach(f => {")
          .append("    const card = document.createElement('div'); card.className = 'gw-feature-card';")
          .append("    let inputsHtml = f.inputs.map(i => {")
          .append("      if (i.t === 'select') {")
          .append("        return `<div class='gw-form-row'><label class='gw-form-label'>${i.l}</label><select id='input-${f.id}-${i.n}' class='gw-form-input'>${i.o.map(opt => `<option value='${opt.v}' ${opt.v === i.v ? 'selected' : ''}>${opt.l}</option>`).join('')}</select></div>`;")
          .append("      } else {")
          .append("        return `<div class='gw-form-row'><label class='gw-form-label'>${i.l}</label><input id='input-${f.id}-${i.n}' type='${i.t}' value='${i.v}' class='gw-form-input'></div>`;")
          .append("      }")
          .append("    }).join('');")
          .append("    card.innerHTML = `<div><div class='gw-feature-title'><span>${f.title}</span><span class='gw-badge gw-badge-issued'>${f.category}</span></div><div class='gw-feature-desc'>${f.desc}</div><div class='gw-feature-purpose'><b>Business Purpose:</b> ${f.purpose}</div></div><div class='gw-feature-form'>${inputsHtml}<button onclick=\"executeModule('${f.id}', '${f.endpoint}')\" class='gw-btn' style='width:100%; margin-top:8px;'>⚡ Run Module Calculation</button><div id='result-${f.id}' class='gw-result-box'></div></div>`;")
          .append("    container.appendChild(card);")
          .append("  });")
          .append("}")
          .append("function filterFeatures(cat) {")
          .append("  document.querySelectorAll('.gw-filter-btn').forEach(b => b.classList.remove('active'));")
          .append("  if(event && event.target) event.target.classList.add('active');")
          .append("  renderCards(cat);")
          .append("}")
          .append("function executeModule(id, endpoint) {")
          .append("  const feat = FEATURES.find(f => f.id === id);")
          .append("  const bodyData = {};")
          .append("  if (feat && feat.inputs) {")
          .append("    feat.inputs.forEach(i => {")
          .append("      const el = document.getElementById(`input-${id}-${i.n}`);")
          .append("      if (el) {")
          .append("        if (i.t === 'number') bodyData[i.n] = parseFloat(el.value);")
          .append("        else if (i.t === 'select') bodyData[i.n] = el.value === 'true' ? true : (el.value === 'false' ? false : el.value);")
          .append("        else bodyData[i.n] = el.value;")
          .append("      }")
          .append("    });")
          .append("  }")
          .append("  const resBox = document.getElementById(`result-${id}`);")
          .append("  resBox.style.display = 'block';")
          .append("  resBox.innerText = 'Calculating REST API payload...';")
          .append("  fetch(endpoint, { method: 'POST', credentials: 'same-origin', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(bodyData) })")
          .append("  .then(async r => {")
          .append("    const text = await r.text();")
          .append("    try {")
          .append("      const json = JSON.parse(text);")
          .append("      if (!r.ok) { throw new Error(json.error || `HTTP ${r.status}`); }")
          .append("      return json;")
          .append("    } catch(e) {")
          .append("      if (!r.ok) throw new Error(`HTTP ${r.status}: ${text}`);")
          .append("      return { result: text };")
          .append("    }")
          .append("  })")
          .append("  .then(data => { resBox.innerText = '✅ Module Execution Success:\\n' + JSON.stringify(data, null, 2); })")
          .append("  .catch(err => { resBox.innerText = '❌ Error executing module: ' + err.message; });")
          .append("}")
          .append("document.addEventListener('DOMContentLoaded', () => { const btn = document.getElementById('btn-all'); if (btn) btn.innerText = `All Features (${FEATURES.length})`; renderCards('all'); });")
          .append("</script>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }

    private String renderDashboardPage() {
        StringBuilder sb = new StringBuilder();
        sb.append(renderHeader("dashboard"));
        sb.append("<style>")
          .append(".dash-container { max-width: 1280px; margin: 24px auto; padding: 0 16px; font-family: 'Inter', sans-serif; font-size:14px; color: #E2E8F0; }")
          .append(".dash-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }")
          .append(".dash-title { font-size: 24px; font-weight: 700; background: linear-gradient(135deg, #38BDF8, #818CF8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }")
          .append(".dash-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 30px; }")
          .append(".glass-card { background: rgba(30, 41, 59, 0.7); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; padding: 20px; box-shadow: 0 8px 32px rgba(0,0,0,0.3); transition: transform 0.2s ease; }")
          .append(".glass-card:hover { transform: translateY(-3px); }")
          .append(".stat-label { font-size: 12px; text-transform: uppercase; color: #94A3B8; letter-spacing: 0.5px; font-weight: 600; }")
          .append(".stat-val { font-size: 32px; font-weight: 800; margin: 8px 0; color: #F8FAFC; }")
          .append(".stat-desc { font-size: 12px; color: #38BDF8; }")
          .append(".canvas-container { height: 260px; position: relative; background: #0F172A; border-radius: 8px; padding: 12px; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(255,255,255,0.05); }")
          .append(".action-btn { background: linear-gradient(135deg, #0284C7, #4F46E5); color: #FFF; border: none; border-radius: 6px; padding: 10px 18px; font-weight: 600; cursor: pointer; transition: all 0.2s; text-decoration:none; display:inline-block; }")
          .append(".action-btn:hover { opacity: 0.9; transform: scale(1.02); }")
          .append("</style>");

        sb.append("<div class='dash-container'>")
          .append("  <div class='dash-header'>")
          .append("    <div>")
          .append("      <div class='dash-title'>💎 PolicyCenter Intelligence & Analytics Web Portal</div>")
          .append("      <div style='color:#94A3B8; font-size:13px; margin-top:4px;'>Real-time Risk Metrics, Telematics Dynamic Scoring, Catastrophe Overlays & GraphQL Gateway</div>")
          .append("    </div>")
          .append("    <a href='/graphql' target='_blank' class='action-btn'>🌐 Open GraphQL Endpoint</a>")
          .append("  </div>")
          .append("  <div class='dash-grid'>")
          .append("    <div class='glass-card'>")
          .append("      <div class='stat-label'>Total Written Premium</div>")
          .append("      <div class='stat-val'>$42,850,200</div>")
          .append("      <div class='stat-desc'>+14.2% YoY Growth (Java 23 Virtual Threads Engine)</div>")
          .append("    </div>")
          .append("    <div class='glass-card'>")
          .append("      <div class='stat-label'>Loss Ratio Performance</div>")
          .append("      <div class='stat-val' style='color:#34D399;'>54.8%</div>")
          .append("      <div class='stat-desc'>Optimal Underwriting Threshold (<75.0%)</div>")
          .append("    </div>")
          .append("    <div class='glass-card'>")
          .append("      <div class='stat-label'>Telematics Connected Vehicles</div>")
          .append("      <div class='stat-val' style='color:#F472B6;'>14,892</div>")
          .append("      <div class='stat-desc'>Average Driving Score: 88 / 100</div>")
          .append("    </div>")
          .append("    <div class='glass-card'>")
          .append("      <div class='stat-label'>Catastrophe Risk Exposure</div>")
          .append("      <div class='stat-val' style='color:#FBBF24;'>Zone A</div>")
          .append("      <div class='stat-desc'>3 Active Coastal Hurricane Alerts Flagged</div>")
          .append("    </div>")
          .append("  </div>")

          .append("  <div style='display:grid; grid-template-columns: 1fr 1fr; gap:20px; margin-bottom:30px;'>")
          .append("    <div class='glass-card'>")
          .append("      <h3 style='margin-top:0; color:#38BDF8;'>🚗 Telematics Driver Speed & Hard-Braking Monitor</h3>")
          .append("      <div class='canvas-container'>")
          .append("        <canvas id='telematicsCanvas' width='500' height='220'></canvas>")
          .append("      </div>")
          .append("    </div>")
          .append("    <div class='glass-card'>")
          .append("      <h3 style='margin-top:0; color:#F472B6;'>🌊 Geospatial Catastrophe & Flood Zone Map</h3>")
          .append("      <div class='canvas-container'>")
          .append("        <canvas id='catRiskCanvas' width='500' height='220'></canvas>")
          .append("      </div>")
          .append("    </div>")
          .append("  </div>")

          .append("  <div class='glass-card' style='margin-bottom:30px;'>")
          .append("    <h3 style='margin-top:0; color:#A7F3D0;'>🚀 GraphQL Live Query Sandbox</h3>")
          .append("    <div style='display:flex; gap:12px; margin-bottom:12px;'>")
          .append("      <button class='action-btn' onclick='runGql(\"query { policy { policyNumber, status, annualPremium } }\")'>Query Policy</button>")
          .append("      <button class='action-btn' style='background:linear-gradient(135deg, #10B981, #059669);' onclick='runGql(\"mutation { createFNOL(policyNumber: \\\"POL-849102\\\", lossAmount: 3200) }\")'>Trigger FNOL Claim Mutation</button>")
          .append("      <button class='action-btn' style='background:linear-gradient(135deg, #F59E0B, #D97706);' onclick='runGql(\"mutation { evaluateRenewal(policyNumber: \\\"POL-849102\\\") }\")'>Evaluate Policy Renewal</button>")
          .append("    </div>")
          .append("    <pre id='gqlResult' style='background:#0F172A; padding:14px; border-radius:6px; font-family:monospace; font-size:13px; color:#38BDF8; overflow-x:auto; margin:0;'>Click a button above to execute a GraphQL Query/Mutation over HTTP POST...</pre>")
          .append("  </div>")

          .append("<script>")
          .append("function drawTelematics() {")
          .append("  const c = document.getElementById('telematicsCanvas'); if(!c) return;")
          .append("  const ctx = c.getContext('2d');")
          .append("  ctx.clearRect(0,0,500,220);")
          .append("  ctx.strokeStyle = '#38BDF8'; ctx.lineWidth = 3;")
          .append("  ctx.beginPath();")
          .append("  for(let x=0; x<=500; x+=20) {")
          .append("    let y = 110 + Math.sin(x*0.05)*40 + (Math.random()*10 - 5);")
          .append("    if(x===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);")
          .append("  }")
          .append("  ctx.stroke();")
          .append("  ctx.fillStyle = '#94A3B8'; ctx.font = '12px Inter';")
          .append("  ctx.fillText('Live Driving Score Index (88/100) - Smooth Telematics Velocity Signal', 20, 20);")
          .append("}")
          .append("function drawCatMap() {")
          .append("  const c = document.getElementById('catRiskCanvas'); if(!c) return;")
          .append("  const ctx = c.getContext('2d');")
          .append("  ctx.clearRect(0,0,500,220);")
          .append("  ctx.fillStyle = '#1E293B'; ctx.fillRect(0,0,500,220);")
          .append("  ctx.fillStyle = 'rgba(239, 68, 68, 0.4)'; ctx.beginPath(); ctx.arc(150, 100, 70, 0, Math.PI*2); ctx.fill();")
          .append("  ctx.fillStyle = 'rgba(245, 158, 11, 0.3)'; ctx.beginPath(); ctx.arc(350, 130, 80, 0, Math.PI*2); ctx.fill();")
          .append("  ctx.fillStyle = '#F8FAFC'; ctx.font = '12px Inter';")
          .append("  ctx.fillText('Zone A High Risk Coastal Flood Region (Tampa Bay Area)', 40, 100);")
          .append("  ctx.fillText('Zone B Inland Hurricane Windstorm Buffer', 250, 130);")
          .append("}")
          .append("function runGql(queryStr) {")
          .append("  document.getElementById('gqlResult').innerText = 'Executing query over /graphql...';")
          .append("  fetch('/graphql', { method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({query: queryStr}) })")
          .append("    .then(r => r.json())")
          .append("    .then(data => { document.getElementById('gqlResult').innerText = JSON.stringify(data, null, 2); })")
          .append("    .catch(err => { document.getElementById('gqlResult').innerText = 'Error: ' + err.message; });")
          .append("}")
          .append("document.addEventListener('DOMContentLoaded', () => { drawTelematics(); drawCatMap(); });")
          .append("</script>");

        sb.append("</div></div></body></html>");
        return sb.toString();
    }
}
