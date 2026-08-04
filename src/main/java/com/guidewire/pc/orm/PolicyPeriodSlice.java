package com.guidewire.pc.orm;

import com.guidewire.pc.model.PolicyPeriod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PolicyPeriodSlice {
    private static final Logger LOGGER = Logger.getLogger(PolicyPeriodSlice.class.getName());

    private final PolicyPeriod rootPeriod;
    private final Date asOfDate;
    private final List<EffDatedBean> slicedBeans;

    public PolicyPeriodSlice(PolicyPeriod rootPeriod, Date asOfDate) {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.PolicyPeriodSlice");
        this.rootPeriod = rootPeriod;
        this.asOfDate = asOfDate;
        this.slicedBeans = rootPeriod.getEffDatedBeans().stream()
                .filter(b -> b.isEffectiveAt(asOfDate))
                .collect(Collectors.toList());
    }

    public PolicyPeriod getRootPeriod() {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.getRootPeriod");
        return rootPeriod;
    }

    public Date getAsOfDate() {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.getAsOfDate");
        return asOfDate;
    }

    public List<EffDatedBean> getSlicedBeans() {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.getSlicedBeans");
        return new ArrayList<>(slicedBeans);
    }

    @SuppressWarnings("unchecked")
    public <T extends EffDatedBean> List<T> getSlicedBeans(Class<T> clazz) {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.getSlicedBeans");
        return slicedBeans.stream()
                .filter(clazz::isInstance)
                .map(b -> (T) b)
                .collect(Collectors.toList());
    }

    public boolean isEffective() {
        LOGGER.log(Level.FINE, "→ PolicyPeriodSlice.isEffective");
        boolean beforeStart = rootPeriod.getPeriodStart() != null && asOfDate.before(rootPeriod.getPeriodStart());
        boolean afterEnd = rootPeriod.getPeriodEnd() != null && !asOfDate.before(rootPeriod.getPeriodEnd());
        return !beforeStart && !afterEnd;
    }
}
