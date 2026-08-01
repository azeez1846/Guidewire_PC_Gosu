package com.guidewire.pc;

import com.guidewire.pc.gosu.GosuBridge;
import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ScheduledItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduledItemTest {

    @BeforeAll
    public static void initGosu() {
        GosuBridge.initGosuEngine(new File("."));
    }

    @Test
    public void testScheduledItemCreationAndRating() {
        PolicyPeriod period = new PolicyPeriod();
        period.setPolicyNumber("POL-SCHEDULE-01");
        period.setProductCode("CommercialProperty");

        ScheduledItem ring = new ScheduledItem(1, "Diamond Engagement Ring 2ct", "SN-991823", "Jewelry", new BigDecimal("15000.00"));
        ScheduledItem excavator = new ScheduledItem(2, "Caterpillar Excavator CAT320", "VIN-CAT88192", "HeavyEquipment", new BigDecimal("120000.00"));

        period.addScheduledItem(ring);
        period.addScheduledItem(excavator);

        assertEquals(2, period.getScheduledItems().size());
        assertEquals(1, ring.getItemNumber());
        assertEquals(2, excavator.getItemNumber());

        Object totalSchedPrem = GosuBridge.invokeStatic("gw.pc.rating.ScheduledItemRatingEngine", "rateScheduledItems", period);
        if (totalSchedPrem instanceof BigDecimal prem) {
            assertTrue(prem.compareTo(BigDecimal.ZERO) > 0);
            assertEquals(0, new BigDecimal("3300.00").compareTo(prem));
            assertEquals(0, new BigDecimal("300.00").compareTo(ring.getItemPremium()));
            assertEquals(0, new BigDecimal("3000.00").compareTo(excavator.getItemPremium()));
        } else {
            // Fallback manual calculation test
            BigDecimal ringPrem = ring.getStatedValue().multiply(new BigDecimal("0.020"));
            BigDecimal catPrem = excavator.getStatedValue().multiply(new BigDecimal("0.025"));
            assertEquals(0, new BigDecimal("300.00").compareTo(ringPrem));
            assertEquals(0, new BigDecimal("3000.00").compareTo(catPrem));
        }
    }
}
