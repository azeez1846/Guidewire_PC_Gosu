package com.guidewire.pc.orm;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GosuORMSession {
    private static final Logger LOGGER = Logger.getLogger(GosuORMSession.class.getName());

    private static final GosuORMSession instance = new GosuORMSession();

    private final AtomicLong idSequence = new AtomicLong(10000);
    private final AtomicLong fixedIdSequence = new AtomicLong(50000);

    // FixedId index -> List of Effective Dated Slices across branches
    private final Map<FixedId<?>, List<EffDatedBean>> fixedIdIndex = new HashMap<>();

    private GosuORMSession() {
        LOGGER.log(Level.FINE, "→ GosuORMSession.GosuORMSession");}

    public static GosuORMSession getInstance() {
        LOGGER.log(Level.FINE, "→ GosuORMSession.getInstance");
        return instance;
    }

    public synchronized Long nextID() {
        LOGGER.log(Level.FINE, "→ GosuORMSession.nextID");
        return idSequence.incrementAndGet();
    }

    public synchronized <T extends KeyableBean> FixedId<T> nextFixedId(Class<T> entityClass) {
        LOGGER.log(Level.FINE, "→ GosuORMSession.nextFixedId");
        return new FixedId<>(fixedIdSequence.incrementAndGet(), entityClass);
    }

    public synchronized void saveEffDatedBean(EffDatedBean bean) {
        LOGGER.log(Level.FINE, "→ GosuORMSession.saveEffDatedBean");
        if (bean.getID() == null) {
            bean.setID(nextID());
        }
        if (bean.getFixedId() == null) {
            @SuppressWarnings("unchecked")
            Class<EffDatedBean> cls = (Class<EffDatedBean>) bean.getClass();
            bean.setFixedId(nextFixedId(cls));
        }

        List<EffDatedBean> slices = fixedIdIndex.computeIfAbsent(bean.getFixedId(), k -> new ArrayList<>());
        slices.add(bean);
    }

    public synchronized EffDatedBean getSliceAt(FixedId<?> fixedId, Date sliceDate) {
        LOGGER.log(Level.FINE, "→ GosuORMSession.getSliceAt");
        List<EffDatedBean> slices = fixedIdIndex.get(fixedId);
        if (slices == null) return null;

        for (EffDatedBean b : slices) {
            if (b.isEffectiveAt(sliceDate)) {
                return b;
            }
        }
        return null;
    }

    public synchronized List<EffDatedBean> getSlicesForFixedId(FixedId<?> fixedId) {
        LOGGER.log(Level.FINE, "→ GosuORMSession.getSlicesForFixedId");
        List<EffDatedBean> slices = fixedIdIndex.get(fixedId);
        return slices != null ? new ArrayList<>(slices) : Collections.emptyList();
    }
}
