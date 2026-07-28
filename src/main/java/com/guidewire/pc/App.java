package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.web.JettyPolicyCenterServer;

import java.io.File;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting Guidewire PolicyCenter Application Engine...");

        File rootDir = new File(".");
        // Init Gosu runtime
        GosuBridge.initGosuEngine(rootDir);

        int port = 8080;
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
