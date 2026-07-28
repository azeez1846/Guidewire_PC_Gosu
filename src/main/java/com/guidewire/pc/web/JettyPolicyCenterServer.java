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

        GuidewirePolicyCenterServlet servlet = new GuidewirePolicyCenterServlet(rootDir);
        context.addServlet(new ServletHolder(servlet), "/*");

        server.start();
        System.out.println("===============================================================");
        System.out.println("  Guidewire PolicyCenter Application (Eclipse Jetty Server)");
        System.out.println("  Access URL: http://localhost:" + port);
        System.out.println("  Default Credentials: Username = su | Password = gw");
        System.out.println("===============================================================");
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
