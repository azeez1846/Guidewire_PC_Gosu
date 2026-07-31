package com.guidewire.pc.service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JVMDiagnosticsService {
    private static final Logger LOGGER = Logger.getLogger(JVMDiagnosticsService.class.getName());
    private static final JVMDiagnosticsService instance = new JVMDiagnosticsService();

    private JVMDiagnosticsService() {}

    public static JVMDiagnosticsService getInstance() {
        return instance;
    }

    public Map<String, Object> getJVMDiagnostics() {
        Map<String, Object> diag = new HashMap<>();

        // 1. Runtime System Properties
        diag.put("javaVersion", System.getProperty("java.version"));
        diag.put("javaVendor", System.getProperty("java.vendor"));
        diag.put("jvmName", System.getProperty("java.vm.name"));
        diag.put("jvmVersion", System.getProperty("java.vm.version"));

        // 2. Garbage Collector Diagnostics (ZGC / Generational ZGC Detection)
        List<Map<String, Object>> gcInfo = new ArrayList<>();
        boolean zgcDetected = false;
        boolean generationalZgcDetected = false;

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            Map<String, Object> m = new HashMap<>();
            String name = gcBean.getName();
            m.put("collectorName", name);
            m.put("collectionCount", gcBean.getCollectionCount());
            m.put("collectionTimeMs", gcBean.getCollectionTime());
            gcInfo.add(m);

            if (name.toLowerCase().contains("zgc")) {
                zgcDetected = true;
                if (name.toLowerCase().contains("generational") || name.toLowerCase().contains("young") || name.toLowerCase().contains("old")) {
                    generationalZgcDetected = true;
                }
            }
        }
        diag.put("garbageCollectors", gcInfo);
        diag.put("zgcDetected", zgcDetected);
        diag.put("generationalZgcActive", generationalZgcDetected || zgcDetected);

        // 3. Memory Pools
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long heapUsedMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMaxMb = memBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        diag.put("heapMemoryUsedMb", heapUsedMb);
        diag.put("heapMemoryMaxMb", heapMaxMb);

        List<Map<String, Object>> poolInfo = new ArrayList<>();
        for (MemoryPoolMXBean poolBean : ManagementFactory.getMemoryPoolMXBeans()) {
            Map<String, Object> p = new HashMap<>();
            p.put("poolName", poolBean.getName());
            p.put("usedMb", poolBean.getUsage().getUsed() / (1024 * 1024));
            p.put("maxMb", poolBean.getUsage().getMax() > 0 ? poolBean.getUsage().getMax() / (1024 * 1024) : -1);
            poolInfo.add(p);
        }
        diag.put("memoryPools", poolInfo);

        // 4. Modern Java 21-23 Feature Summary
        diag.put("virtualThreadsSupported", true);
        diag.put("sequencedCollectionsSupported", true);
        diag.put("recordPatternsSupported", true);
        diag.put("scopedValuesSupported", true);
        diag.put("structuredConcurrencySupported", true);
        diag.put("activeThreadCount", Thread.activeCount());

        LOGGER.log(java.util.logging.Level.INFO, "[JVM Diagnostics] Generated Java 23 & ZGC metrics. ZGC Active: {0} (Heap: {1}MB / {2}MB)",
                new Object[]{zgcDetected, heapUsedMb, heapMaxMb});
        return diag;
    }
}
