package com.guidewire.pc.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JettyPolicyCenterServer {
    private static final Logger LOGGER = Logger.getLogger(JettyPolicyCenterServer.class.getName());

    private final int port;
    private final File rootDir;
    private Server server;

    public JettyPolicyCenterServer(int port, File rootDir) {
        LOGGER.log(Level.FINE, "→ JettyPolicyCenterServer.JettyPolicyCenterServer");
        this.port = port;
        this.rootDir = rootDir;
    }

    public void start() throws Exception {
        LOGGER.log(Level.FINE, "→ JettyPolicyCenterServer.start");
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setVirtualThreadsExecutor(Executors.newVirtualThreadPerTaskExecutor());
        threadPool.setName("gw-virtual-jetty-worker");

        server = new Server(threadPool);
        
        // High-performance ServerConnector tuned for max throughput
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setIdleTimeout(30000);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Enable Gzip compression if available
        try {
            org.eclipse.jetty.server.handler.gzip.GzipHandler gzip = new org.eclipse.jetty.server.handler.gzip.GzipHandler();
            gzip.setMinGzipSize(256);
            gzip.setHandler(context);
            server.setHandler(gzip);
        } catch (Throwable ignored) {
            server.setHandler(context);
        }

        // Start official H2 Web Console server on port 8082 (secured to localhost only)
        try {
            org.h2.tools.Server h2WebServer = org.h2.tools.Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("  H2 Web Console: " + h2WebServer.getURL());
        } catch (java.sql.SQLException e) {
            System.out.println("  H2 Web Console: http://localhost:8082 (Already running or port busy)");
        }

        // REST API & Swagger UI Endpoints
        context.addServlet(new ServletHolder(new GuidewireRestServlet()), "/rest/v1/*");
        context.addServlet(new ServletHolder(new SwaggerUiServlet()), "/swagger-ui/*");
        context.addServlet(new ServletHolder(new PcfStudioServlet(rootDir)), "/pcf-studio/*");
        context.addServlet(new ServletHolder(new GraphQLGatewayServlet()), "/graphql/*");

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
        LOGGER.log(Level.FINE, "→ JettyPolicyCenterServer.stop");
        if (server != null) {
            server.stop();
        }
    }
}
