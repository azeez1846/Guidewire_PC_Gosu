package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.web.GuidewireServer;

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
            GuidewireServer server = new GuidewireServer(port, rootDir);
            server.start();
        } catch (java.io.IOException e) {
            System.err.println("Failed to start Guidewire PolicyCenter server: " + e.getMessage());
        }
    }
}
