package com.guidewire.pc;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ScheduledEquipmentItem;
import com.guidewire.pc.service.IMRatingService;
import com.guidewire.pc.service.RatingEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guidewire OOTB Inland Marine (IM) Rating Engine Tests")
public class InlandMarineRatingTest {

    @Test
    @DisplayName("Should rate Inland Marine policy with scheduled contractors equipment")
    public void testInlandMarineRating() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_IM_TEST_1");
        period.setProductCode(PCConstants.PRODUCT_INLAND_MARINE);

        ScheduledEquipmentItem item1 = new ScheduledEquipmentItem(1, "HeavyMachinery", "Caterpillar Excavator 320DL", "SN-90124", new BigDecimal("100000.00"));
        item1.setDeductible(new BigDecimal("1000.00"));
        item1.setCoinsurancePercentage(0.90);

        ScheduledEquipmentItem item2 = new ScheduledEquipmentItem(2, "MobileTools", "Generac Heavy Duty Power Generator", "SN-55912", new BigDecimal("20000.00"));
        item2.setDeductible(new BigDecimal("500.00"));

        period.addScheduledEquipmentItem(item1);
        period.addScheduledEquipmentItem(item2);

        IMRatingService.getInstance().rateInlandMarine(period);

        assertNotNull(period.getBasePremium());
        assertNotNull(period.getTaxesAndFees());
        assertNotNull(period.getTotalPremium());

        assertTrue(period.getBasePremium().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(period.getTotalPremium().compareTo(period.getBasePremium()) > 0);
        assertEquals(PCConstants.PRODUCT_INLAND_MARINE, period.getProductCode());
    }

    @Test
    @DisplayName("Should rate Inland Marine via central RatingEngine dispatcher")
    public void testRatingEngineDispatcherForInlandMarine() {
        PolicyPeriod period = new PolicyPeriod();
        period.setJobNumber("S000_IM_TEST_2");
        period.setProductCode(PCConstants.PRODUCT_INLAND_MARINE);

        RatingEngine.getInstance().rate(period);

        assertNotNull(period.getTotalPremium());
        assertTrue(period.getTotalPremium().compareTo(BigDecimal.ZERO) > 0);
    }
}
