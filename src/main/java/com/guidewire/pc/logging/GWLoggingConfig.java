package com.guidewire.pc.logging;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * GWLoggingConfig
 *
 * Central Java logging bootstrap for Guidewire PolicyCenter.
 * Call GWLoggingConfig.configure() once at App startup to:
 *   - Write ALL log levels (FINE -> SEVERE) to logs/app.log (rolling daily file)
 *   - Stream INFO+ to the console (Jetty terminal)
 *   - Use a structured single-line format: [TIMESTAMP][LEVEL][CLASS.METHOD] message
 */
public class GWLoggingConfig {

    private static final Logger LOGGER = Logger.getLogger(GWLoggingConfig.class.getName());
    private static final String LOG_DIR  = "logs";
    private static volatile boolean configured = false;

    private GWLoggingConfig() {}

    /**
     * Configure root JUL logger for full file + console output.
     * Safe to call multiple times (idempotent).
     */
    public static synchronized void configure() {
        if (configured) return;
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // --- Root Logger ---
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.ALL);

            // Remove default console handlers to avoid duplication
            for (Handler h : rootLogger.getHandlers()) {
                rootLogger.removeHandler(h);
            }

            // --- File Handler (logs/app.log, append mode, up to 50MB, 10 rolling files) ---
            String pattern = LOG_DIR + File.separator + "app_%g.log";
            FileHandler fileHandler = new FileHandler(pattern, 50 * 1024 * 1024, 10, true);
            fileHandler.setLevel(Level.ALL); // capture FINE (method entry) and above
            fileHandler.setFormatter(new GWLogFormatter());
            rootLogger.addHandler(fileHandler);

            // --- Console Handler (INFO and above only for Jetty terminal readability) ---
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new GWLogFormatter());
            rootLogger.addHandler(consoleHandler);

            // Suppress excessively noisy third-party loggers
            Logger.getLogger("org.eclipse.jetty").setLevel(Level.WARNING);
            Logger.getLogger("org.h2").setLevel(Level.WARNING);
            Logger.getLogger("com.fasterxml.jackson").setLevel(Level.WARNING);

            configured = true;

            // Use System.out here to avoid triggering the formatter we just registered
            System.out.println("[GW-LOGGING] Initialized -> file: " + LOG_DIR + "/app.log | date: " + LocalDate.now());
            LOGGER.info("GWLoggingConfig fully initialized.");

        } catch (IOException e) {
            System.err.println("[GW-LOGGING] Failed to configure file handler: " + e.getMessage());
        }
    }

    /** Simple one-liner log formatter: [timestamp][LEVEL][source] message */
    public static class GWLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            // NOTE: Do NOT call any Logger here – it causes infinite recursion
            String ts = java.time.Instant.ofEpochMilli(record.getMillis())
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            String level   = String.format("%-7s", record.getLevel().getName());
            String source  = record.getSourceClassName() != null
                    ? record.getSourceClassName().replaceAll("com\\.guidewire\\.pc\\.", "")
                    : record.getLoggerName();
            String method  = record.getSourceMethodName() != null ? "." + record.getSourceMethodName() : "";
            String message = formatMessage(record);

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(ts).append("]")
              .append("[").append(level).append("]")
              .append("[").append(source).append(method).append("] ")
              .append(message)
              .append(System.lineSeparator());

            if (record.getThrown() != null) {
                sb.append("  EXCEPTION: ").append(record.getThrown().toString()).append(System.lineSeparator());
                for (StackTraceElement el : record.getThrown().getStackTrace()) {
                    sb.append("    at ").append(el.toString()).append(System.lineSeparator());
                }
            }
            return sb.toString();
        }
    }
}
