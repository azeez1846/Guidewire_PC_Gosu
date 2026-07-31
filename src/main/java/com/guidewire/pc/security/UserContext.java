package com.guidewire.pc.security;

public class UserContext {
    private static final ThreadLocal<String> CURRENT_USER = InheritableThreadLocal.withInitial(() -> "system");
    private static final ThreadLocal<String> PRODUCER_CODE = InheritableThreadLocal.withInitial(() -> "PR-10000");

    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static String getProducerCode() {
        return PRODUCER_CODE.get();
    }

    public static void runWithUser(String username, String producerCode, Runnable task) {
        String prevUser = CURRENT_USER.get();
        String prevProducer = PRODUCER_CODE.get();
        try {
            CURRENT_USER.set(username != null ? username : "system");
            PRODUCER_CODE.set(producerCode != null ? producerCode : "PR-10000");
            task.run();
        } finally {
            CURRENT_USER.set(prevUser);
            PRODUCER_CODE.set(prevProducer);
        }
    }
}
