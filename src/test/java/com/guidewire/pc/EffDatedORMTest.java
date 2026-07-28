package com.guidewire.pc;

import com.guidewire.pc.model.Coverage;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.orm.FixedId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class EffDatedORMTest {

    @Test
    public void testFixedIdEqualityAcrossSlices() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        PolicyPeriod sub = new PolicyPeriod();
        sub.setEffectiveDate("2026-01-01");
        sub.setExpirationDate("2027-01-01");

        Coverage cov = new Coverage("PAAutoLiabilityCov", "Auto Liability", new BigDecimal("500000"), new BigDecimal("500"));
        cov.setEffectiveDate(sdf.parse("2026-01-01"));
        cov.setExpirationDate(sdf.parse("2027-01-01"));

        sub.addEffDatedBean(cov);

        assertNotNull(cov.getID());
        assertNotNull(cov.getFixedId());
        FixedId<?> originalFixedId = cov.getFixedId();

        // Create Policy Change branch
        Date changeDate = sdf.parse("2026-06-01");
        PolicyPeriod changeBranch = sub.createPolicyChangeBranch(changeDate, "C0001001");

        assertEquals(1, changeBranch.getEffDatedBeans().size());
        Coverage clonedCov = (Coverage) changeBranch.getEffDatedBeans().get(0);

        // Physical IDs must be DIFFERENT (new slice row in DB)
        assertNotEquals(cov.getID(), clonedCov.getID());

        // FixedIDs MUST BE EQUAL across slices!
        assertEquals(originalFixedId, clonedCov.getFixedId());

        // Check Temporal Effective Dates
        assertEquals(changeDate, clonedCov.getEffectiveDate());
        assertTrue(clonedCov.isEffectiveAt(sdf.parse("2026-07-01")));
        assertFalse(clonedCov.isEffectiveAt(sdf.parse("2026-03-01")));
    }

    @Test
    public void testCancellationSliceTruncation() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        PolicyPeriod sub = new PolicyPeriod();
        sub.setEffectiveDate("2026-01-01");
        sub.setExpirationDate("2027-01-01");

        Coverage cov = new Coverage("PAAutoLiabilityCov", "Auto Liability", new BigDecimal("500000"), new BigDecimal("500"));
        cov.setEffectiveDate(sdf.parse("2026-01-01"));
        cov.setExpirationDate(sdf.parse("2027-01-01"));
        sub.addEffDatedBean(cov);

        Date cancelDate = sdf.parse("2026-09-01");
        PolicyPeriod cancelBranch = sub.createCancellationBranch(cancelDate, "X0001001");

        Coverage cancelCov = (Coverage) cancelBranch.getEffDatedBeans().get(0);
        assertEquals(cov.getFixedId(), cancelCov.getFixedId());
        assertEquals(cancelDate, cancelCov.getExpirationDate());
        assertEquals("REMOVE", cancelCov.getChangeType());
    }
}
