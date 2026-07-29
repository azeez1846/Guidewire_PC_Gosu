package com.guidewire.pc;

import com.guidewire.pc.model.Coverage;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.orm.EffDatedBean;
import com.guidewire.pc.orm.PolicyPeriodSlice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AsOfSlicingTest {

    @Test
    public void testAsOfSlicingFiltering() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        PolicyPeriod period = new PolicyPeriod();
        period.setEffectiveDate("2026-01-01");
        period.setExpirationDate("2027-01-01");

        // Coverage 1: Effective full year
        Coverage covFullYear = new Coverage("PAAutoLiabilityCov", "Auto Liability", new BigDecimal("500000"), new BigDecimal("500"));
        covFullYear.setEffectiveDate(sdf.parse("2026-01-01"));
        covFullYear.setExpirationDate(sdf.parse("2027-01-01"));
        period.addEffDatedBean(covFullYear);

        // Coverage 2: Added mid-year on 2026-06-01
        Coverage covMidYear = new Coverage("PACollisionCov", "Collision", new BigDecimal("50000"), new BigDecimal("1000"));
        covMidYear.setEffectiveDate(sdf.parse("2026-06-01"));
        covMidYear.setExpirationDate(sdf.parse("2027-01-01"));
        period.addEffDatedBean(covMidYear);

        // Test Slice on 2026-03-01 (before Collision added)
        Date marchDate = sdf.parse("2026-03-01");
        PolicyPeriodSlice marchSlice = period.getSlice(marchDate);
        List<Coverage> marchCoverages = marchSlice.getSlicedBeans(Coverage.class);

        assertEquals(1, marchCoverages.size());
        assertEquals("PAAutoLiabilityCov", marchCoverages.get(0).getPatternCode());

        // Test Slice on 2026-07-01 (after Collision added)
        Date julyDate = sdf.parse("2026-07-01");
        PolicyPeriodSlice julySlice = period.getSlice(julyDate);
        List<Coverage> julyCoverages = julySlice.getSlicedBeans(Coverage.class);

        assertEquals(2, julyCoverages.size());
    }

    @Test
    public void testEffDatedBeanGetSlice() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Coverage cov = new Coverage("PAAutoLiabilityCov", "Auto Liability", new BigDecimal("500000"), new BigDecimal("500"));
        cov.setEffectiveDate(sdf.parse("2026-01-01"));
        cov.setExpirationDate(sdf.parse("2026-06-01"));

        assertNotNull(cov.getSlice(sdf.parse("2026-03-01")));
        assertNull(cov.getSlice(sdf.parse("2026-07-01")));
    }
}
