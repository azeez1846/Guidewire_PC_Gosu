package com.guidewire.pc.productmodel;

import com.guidewire.pc.model.Coverage;

import java.util.List;

public interface Coverable {
    List<Coverage> getCoverages();
    Coverage getCoverage(String patternCode);
    Coverage createCoverage(String patternCode);
    boolean removeCoverage(String patternCode);
    boolean hasCoverage(String patternCode);
}
