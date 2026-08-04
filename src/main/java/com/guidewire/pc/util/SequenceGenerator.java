package com.guidewire.pc.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Thread-safe sequence generator for unique Job Numbers, Policy Numbers, and Account Numbers.
 */
public class SequenceGenerator {
    private static final Logger LOGGER = Logger.getLogger(SequenceGenerator.class.getName());

    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis() % 800000 + 100000);

    public static long nextId() {
        LOGGER.log(Level.FINE, "→ SequenceGenerator.nextId");
        return COUNTER.incrementAndGet();
    }
}
