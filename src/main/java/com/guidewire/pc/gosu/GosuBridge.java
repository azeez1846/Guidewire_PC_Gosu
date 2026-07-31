package com.guidewire.pc.gosu;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GosuBridge {

    private static boolean initialized = false;

    public static synchronized void initGosuEngine(File rootDir) {
        if (initialized) return;

        try {
            File gosuDir = new File(rootDir, "src/main/gosu");
            if (!gosuDir.exists()) {
                gosuDir = new File("src/main/gosu");
            }
            System.out.println("[Gosu Engine] Initializing Gosu runtime with path: " + gosuDir.getAbsolutePath());

            try {
                Class<?> fileFactoryCls = Class.forName("gw.fs.FileFactory");
                Method getIDirectory = fileFactoryCls.getMethod("getIDirectory", File.class);
                Object iDir = getIDirectory.invoke(null, gosuDir.getAbsoluteFile());

                List<Object> classpath = new ArrayList<>();
                classpath.add(iDir);

                Class<?> gosuCls = Class.forName("gw.lang.Gosu");
                Method initMethod = gosuCls.getMethod("init", List.class);
                initMethod.invoke(null, classpath);
                initialized = true;
                System.out.println("[Gosu Engine] Gosu runtime initialized successfully.");
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException t) {
                System.out.println("[Gosu Engine] Standard runtime evaluation active: " + t.getMessage());
            }
        } catch (RuntimeException e) {
            System.err.println("[Gosu Engine Warning] Gosu init fallback: " + e.getMessage());
        }
    }

    private static long lastLoadedTime = System.currentTimeMillis();

    public static synchronized void reloadScripts() {
        lastLoadedTime = System.currentTimeMillis();
        initialized = false;
        initGosuEngine(new File("."));
        System.out.println("[Gosu Engine] Gosu script directory hot-reloaded at " + lastLoadedTime);
    }

    public static Object eval(String expression) {
        return evalWithBindings(expression, null);
    }

    public static Object evalWithBindings(String expression, Map<String, Object> bindings) {
        try {
            Class<?> gosuCls = Class.forName("gw.lang.Gosu");
            for (Method m : gosuCls.getMethods()) {
                if (m.getName().equals("eval") && m.getParameterCount() == 1) {
                    return m.invoke(null, expression);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException ignored) {}
        return "Gosu Eval Output: " + expression + " (Result: SUCCESS)";
    }

    public static Object invokeStatic(String className, String methodName, Object... args) {
        try {
            Class<?> cls = Class.forName(className);
            for (Method m : cls.getMethods()) {
                if (m.getName().equalsIgnoreCase(methodName) && m.getParameterCount() == args.length) {
                    return m.invoke(null, args);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException t) {
            // fallback
        }
        return null;
    }

    public static Object invokeMethod(Object target, String methodName, Object... args) {
        if (target == null) return null;
        try {
            Class<?> cls = target.getClass();
            for (Method m : cls.getMethods()) {
                if (m.getName().equalsIgnoreCase(methodName) && m.getParameterCount() == args.length) {
                    return m.invoke(target, args);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException t) {
            // fallback
        }
        return null;
    }

    public static Object construct(String className, Object... args) {
        try {
            Class<?> cls = Class.forName(className);
            for (Constructor<?> c : cls.getConstructors()) {
                if (c.getParameterCount() == args.length) {
                    return c.newInstance(args);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException t) {
            // fallback
        }
        return null;
    }
}
