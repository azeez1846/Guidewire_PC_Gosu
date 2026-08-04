package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.logging.GWLoggingConfig;
import com.guidewire.pc.web.JettyPolicyCenterServer;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "→ App.main");
        // ── Configure centralised logging (MUST be first) ──────────────────
        GWLoggingConfig.configure();

        System.out.println("Starting Guidewire PolicyCenter Application Engine...");

        File rootDir = new File(".");
        // Init Gosu runtime
        GosuBridge.initGosuEngine(rootDir);

        int port = 8085;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            JettyPolicyCenterServer server = new JettyPolicyCenterServer(port, rootDir);
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start Guidewire PolicyCenter Jetty server: " + e.getMessage());
        }
    }
}
