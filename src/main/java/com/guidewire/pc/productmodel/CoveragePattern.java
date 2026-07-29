package com.guidewire.pc.productmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CoveragePattern {
    private final String code;
    private final String name;
    private final String category;
    private final BigDecimal defaultLimitOrDeductible;
    private final List<String> availableOptions = new ArrayList<>();

    public CoveragePattern(String code, String name, String category, BigDecimal defaultLimitOrDeductible) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.defaultLimitOrDeductible = defaultLimitOrDeductible;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getDefaultLimitOrDeductible() {
        return defaultLimitOrDeductible;
    }

    public List<String> getAvailableOptions() {
        return availableOptions;
    }

    public void addOption(String option) {
        this.availableOptions.add(option);
    }
}
