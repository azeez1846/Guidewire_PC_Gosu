package com.guidewire.pc.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe sequence generator for unique Job Numbers, Policy Numbers, and Account Numbers.
 */
public class SequenceGenerator {
    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 800000 + 100000);

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}
