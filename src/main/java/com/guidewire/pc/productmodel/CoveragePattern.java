package com.guidewire.pc.productmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CoveragePattern {
    private static final Logger LOGGER = Logger.getLogger(CoveragePattern.class.getName());

    private final String code;
    private final String name;
    private final String category;
    private final BigDecimal defaultLimitOrDeductible;
    private final List<String> availableOptions = new ArrayList<>();

    public CoveragePattern(String code, String name, String category, BigDecimal defaultLimitOrDeductible) {
        LOGGER.log(Level.FINE, "→ CoveragePattern.CoveragePattern");
        this.code = code;
        this.name = name;
        this.category = category;
        this.defaultLimitOrDeductible = defaultLimitOrDeductible;
    }

    public String getCode() {
        LOGGER.log(Level.FINE, "→ CoveragePattern.getCode");
        return code;
    }

    public String getName() {
        LOGGER.log(Level.FINE, "→ CoveragePattern.getName");
        return name;
    }

    public String getCategory() {
        LOGGER.log(Level.FINE, "→ CoveragePattern.getCategory");
        return category;
    }

    public BigDecimal getDefaultLimitOrDeductible() {
        LOGGER.log(Level.FINE, "→ CoveragePattern.getDefaultLimitOrDeductible");
        return defaultLimitOrDeductible;
    }

    public List<String> getAvailableOptions() {
        LOGGER.log(Level.FINE, "→ CoveragePattern.getAvailableOptions");
        return availableOptions;
    }

    public void addOption(String option) {
        LOGGER.log(Level.FINE, "→ CoveragePattern.addOption");
        this.availableOptions.add(option);
    }
}
