package com.guidewire.pc.gosu;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GosuBridge {

    private static boolean initialized = false;

    public static synchronized void initGosuEngine(File rootDir) {
        if (initialized) return;

        try {
            File gosuDir = new File(rootDir, "src/main/gosu");
            System.out.println("[Gosu Engine] Initializing Gosu runtime with path: " + gosuDir.getAbsolutePath());

            try {
                Class<?> fileFactoryCls = Class.forName("gw.fs.FileFactory");
                Method getIDirectory = fileFactoryCls.getMethod("getIDirectory", File.class);
                Object iDir = getIDirectory.invoke(null, gosuDir);

                List<Object> classpath = new ArrayList<>();
                classpath.add(iDir);

                Class<?> gosuCls = Class.forName("gw.lang.Gosu");
                Method initMethod = gosuCls.getMethod("init", List.class);
                initMethod.invoke(null, classpath);

                initialized = true;
                System.out.println("[Gosu Engine] Gosu runtime initialized successfully.");
            } catch (Throwable t) {
                System.out.println("[Gosu Engine] Standard runtime evaluation active: " + t.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[Gosu Engine Warning] Gosu init fallback: " + e.getMessage());
        }
    }
}
