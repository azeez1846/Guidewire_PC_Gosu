package com.guidewire.pc.orm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Non-blocking thread-safe Gosu ORM Session for Guidewire PolicyCenter.
 * Supports efficient slice lookups and sequence ID allocation for high-throughput Virtual Threads.
 */
public class GosuORMSession {
    private static final Logger LOGGER = Logger.getLogger(GosuORMSession.class.getName());

    private static final GosuORMSession INSTANCE = new GosuORMSession();

    private final AtomicLong idSequence = new AtomicLong(10000);
    private final AtomicLong fixedIdSequence = new AtomicLong(50000);

    // FixedId index -> Thread-safe List of Effective Dated Slices across branches
    private final Map<FixedId<?>, List<EffDatedBean>> fixedIdIndex = new ConcurrentHashMap<>();

    private GosuORMSession() {
        LOGGER.log(Level.FINE, "GosuORMSession initialized");
    }

    public static GosuORMSession getInstance() {
        return INSTANCE;
    }

    public Long nextID() {
        return idSequence.incrementAndGet();
    }

    public <T extends KeyableBean> FixedId<T> nextFixedId(Class<T> entityClass) {
        return new FixedId<>(fixedIdSequence.incrementAndGet(), entityClass);
    }

    public void saveEffDatedBean(EffDatedBean bean) {
        if (bean == null) return;
        if (bean.getID() == null) {
            bean.setID(nextID());
        }
        if (bean.getFixedId() == null) {
            @SuppressWarnings("unchecked")
            Class<EffDatedBean> cls = (Class<EffDatedBean>) bean.getClass();
            bean.setFixedId(nextFixedId(cls));
        }

        List<EffDatedBean> slices = fixedIdIndex.computeIfAbsent(bean.getFixedId(), k -> new CopyOnWriteArrayList<>());
        slices.add(bean);
    }

    public EffDatedBean getSliceAt(FixedId<?> fixedId, Date sliceDate) {
        if (fixedId == null || sliceDate == null) return null;
        List<EffDatedBean> slices = fixedIdIndex.get(fixedId);
        if (slices == null) return null;

        for (EffDatedBean b : slices) {
            if (b.isEffectiveAt(sliceDate)) {
                return b;
            }
        }
        return null;
    }

    public List<EffDatedBean> getSlicesForFixedId(FixedId<?> fixedId) {
        if (fixedId == null) return Collections.emptyList();
        List<EffDatedBean> slices = fixedIdIndex.get(fixedId);
        return slices != null ? new ArrayList<>(slices) : Collections.emptyList();
    }
}
