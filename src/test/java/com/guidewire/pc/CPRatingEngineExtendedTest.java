package com.guidewire.pc;

import com.guidewire.pc.service.CPRatingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commercial Property Deep Endorsements & Rating Engine Tests")
public class CPRatingEngineExtendedTest {

    @Test
    @DisplayName("Should correctly calculate Tenants Improvements and Betterments Coverage")
    void testTenantsImprovementsRating() {
        BigDecimal limit = new BigDecimal("150000.00");
        // Replacement Cost basis has 15% surcharge over ACV
        BigDecimal acvPrem = CPRatingService.rateTenantsImprovements(limit, "ActualCashValue");
        BigDecimal rcPrem = CPRatingService.rateTenantsImprovements(limit, "ReplacementCost");

        assertNotNull(acvPrem);
        assertNotNull(rcPrem);
        assertTrue(rcPrem.compareTo(acvPrem) > 0, "Replacement cost premium must exceed ACV premium");
        assertEquals(new BigDecimal("630.00"), acvPrem); // 1500 * 0.42
        assertEquals(new BigDecimal("724.50"), rcPrem);  // 630 * 1.15
    }

    @Test
    @DisplayName("Should correctly rate Business Income with different monthly indemnity limitations")
    void testBusinessIncomeRating() {
        BigDecimal biLimit = new BigDecimal("300000.00");
        BigDecimal premOneThird = CPRatingService.rateBusinessIncome(biLimit, "1/3", false);
        BigDecimal premOneFourth = CPRatingService.rateBusinessIncome(biLimit, "1/4", false);
        BigDecimal premOneSixth = CPRatingService.rateBusinessIncome(biLimit, "1/6", false);
        BigDecimal premWithPayroll = CPRatingService.rateBusinessIncome(biLimit, "1/3", true);

        // 1/3 (0.90x) > 1/4 (0.80x) > 1/6 (0.65x)
        assertTrue(premOneThird.compareTo(premOneFourth) > 0);
        assertTrue(premOneFourth.compareTo(premOneSixth) > 0);
        assertTrue(premWithPayroll.compareTo(premOneThird) > 0);
    }

    @Test
    @DisplayName("Should correctly rate Boiler and Machinery / Equipment Breakdown")
    void testBoilerAndMachineryRating() {
        BigDecimal eqLimit = new BigDecimal("500000.00");
        BigDecimal standardPrem = CPRatingService.rateBoilerAndMachinery(eqLimit, false);
        BigDecimal productionPrem = CPRatingService.rateBoilerAndMachinery(eqLimit, true);

        assertEquals(new BigDecimal("900.00"), standardPrem); // 5000 * 0.18
        assertEquals(new BigDecimal("1125.00"), productionPrem); // 900 * 1.25
    }

    @Test
    @DisplayName("Should correctly rate Blanket Coverage with coinsurance discount credits")
    void testBlanketCoverageRating() {
        BigDecimal blanketLimit = new BigDecimal("5000000.00");
        BigDecimal weightedRate = new BigDecimal("0.45");

        BigDecimal blanket80 = CPRatingService.rateBlanketCoverage(blanketLimit, weightedRate, 80);
        BigDecimal blanket90 = CPRatingService.rateBlanketCoverage(blanketLimit, weightedRate, 90);
        BigDecimal blanket100 = CPRatingService.rateBlanketCoverage(blanketLimit, weightedRate, 100);

        assertEquals(new BigDecimal("22500.00"), blanket80); // 50000 * 0.45
        assertEquals(new BigDecimal("21375.00"), blanket90); // 22500 * 0.95 (5% credit)
        assertEquals(new BigDecimal("20250.00"), blanket100); // 22500 * 0.90 (10% credit)
    }

    @Test
    @DisplayName("Should correctly calculate Coinsurance Penalty formula")
    void testCoinsurancePenaltyCalculation() {
        BigDecimal propertyValue = new BigDecimal("1000000.00");
        int requiredCoinsurance = 80; // Required = $800,000
        BigDecimal carriedLimit = new BigDecimal("600000.00"); // 75% of required ($600k / $800k)
        BigDecimal claimLoss = new BigDecimal("200000.00");
        BigDecimal deductible = new BigDecimal("5000.00");

        Map<String, Object> penalty = CPRatingService.calculateCoinsurancePenalty(carriedLimit, requiredCoinsurance, propertyValue, claimLoss, deductible);

        assertTrue((Boolean) penalty.get("hasPenalty"));
        assertEquals(new BigDecimal("800000.00"), penalty.get("requiredInsurance"));
        assertEquals(new BigDecimal("0.7500"), penalty.get("coinsuranceFactor"));
        // Payout = 200,000 * 0.75 - 5,000 = 150,000 - 5,000 = 145,000
        assertEquals(new BigDecimal("145000.00"), penalty.get("netPayout"));
        // Penalty = 195,000 (unpenalized) - 145,000 = 50,000
        assertEquals(new BigDecimal("50000.00"), penalty.get("penaltyAmount"));
    }

    @Test
    @DisplayName("Should correctly rate full commercial property endorsement package")
    void testFullCommercialPropertyPackage() {
        Map<String, Object> input = new HashMap<>();
        input.put("buildingLimit", "2000000.00");
        input.put("bppLimit", "500000.00");
        input.put("protectionClass", "3");
        input.put("earthquake", true);
        input.put("flood", true);
        input.put("sprinkler", true);
        input.put("tenantsImprovementLimit", "200000.00");
        input.put("tenantsValuationBasis", "ReplacementCost");
        input.put("businessIncomeLimit", "400000.00");
        input.put("indemnityFraction", "1/3");
        input.put("includePayroll", true);
        input.put("equipmentBreakdownLimit", "500000.00");
        input.put("hasProductionMachinery", true);

        Map<String, Object> res = CPRatingService.rateFullCommercialPropertyPackage(input);
        assertEquals("SUCCESS", res.get("status"));
        assertNotNull(res.get("basePropertyPremium"));
        assertNotNull(res.get("tenantsImprovementsPremium"));
        assertNotNull(res.get("businessIncomePremium"));
        assertNotNull(res.get("equipmentBreakdownPremium"));
        assertNotNull(res.get("totalPackagePremium"));

        BigDecimal total = (BigDecimal) res.get("totalPackagePremium");
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }
}
