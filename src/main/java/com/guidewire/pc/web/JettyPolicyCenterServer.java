package com.guidewire.pc.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;

public class JettyPolicyCenterServer {
    private final int port;
    private final File rootDir;
    private Server server;

    public JettyPolicyCenterServer(int port, File rootDir) {
        this.port = port;
        this.rootDir = rootDir;
    }

    public void start() throws Exception {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Start official H2 Web Console server on port 8082 (matching OOTB Guidewire setup)
        try {
            org.h2.tools.Server h2WebServer = org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("  H2 Web Console: " + h2WebServer.getURL());
        } catch (Exception e) {
            System.out.println("  H2 Web Console: http://localhost:8082 (Already running or port busy)");
        }

        // REST API & Swagger UI Endpoints
        context.addServlet(new ServletHolder(new GuidewireRestServlet()), "/rest/v1/*");
        context.addServlet(new ServletHolder(new SwaggerUiServlet()), "/swagger-ui/*");

        GuidewirePolicyCenterServlet servlet = new GuidewirePolicyCenterServlet(rootDir);
        context.addServlet(new ServletHolder(servlet), "/*");

        server.start();
        System.out.println("===============================================================");
        System.out.println("  Guidewire PolicyCenter Application (Eclipse Jetty Server)");
        System.out.println("  Access URL: http://localhost:" + port);
        System.out.println("  H2 Web Console: http://localhost:8082");
        System.out.println("  REST APIs: http://localhost:" + port + "/rest/v1/openapi.json");
        System.out.println("  Swagger UI: http://localhost:" + port + "/swagger-ui");
        System.out.println("  Default Credentials: Username = su | Password = gw");
        System.out.println("===============================================================");
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
