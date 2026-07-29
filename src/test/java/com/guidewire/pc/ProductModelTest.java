package com.guidewire.pc;

import com.guidewire.pc.model.Coverage;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.productmodel.CoveragePattern;
import com.guidewire.pc.productmodel.ProductModelLoader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductModelTest {

    @Test
    public void testProductModelXmlParsing() {
        ProductModelLoader loader = ProductModelLoader.getInstance();
        loader.loadProductModels();

        CoveragePattern liabPattern = loader.getCoveragePattern("PersonalAuto", "PAAutoLiabilityCov");
        assertNotNull(liabPattern, "PAAutoLiabilityCov pattern should be loaded");
        assertEquals("Auto Liability Coverage", liabPattern.getName());
        assertEquals("Liability", liabPattern.getCategory());
        assertEquals(new BigDecimal("500000"), liabPattern.getDefaultLimitOrDeductible());
        assertEquals(3, liabPattern.getAvailableOptions().size());

        CoveragePattern commCollision = loader.getCoveragePattern("CommercialAuto", "CACollisionCov");
        assertNotNull(commCollision, "CACollisionCov pattern should be loaded");
        assertEquals(new BigDecimal("1000"), commCollision.getDefaultLimitOrDeductible());
    }

    @Test
    public void testCoverableDynamicCoverageCreation() {
        PolicyPeriod period = new PolicyPeriod();
        period.setProductCode("PersonalAuto");
        period.setEffectiveDate("2026-01-01");
        period.setExpirationDate("2027-01-01");

        assertFalse(period.hasCoverage("PACollisionCov"));

        Coverage collisionCov = period.createCoverage("PACollisionCov");
        assertNotNull(collisionCov);
        assertTrue(period.hasCoverage("PACollisionCov"));
        assertEquals("PACollisionCov", collisionCov.getPatternCode());
        assertEquals("Collision Coverage", collisionCov.getPatternName());
        assertEquals(new BigDecimal("500"), collisionCov.getDeductible());

        assertTrue(period.removeCoverage("PACollisionCov"));
        assertFalse(period.hasCoverage("PACollisionCov"));
    }
}
