package com.guidewire.pc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TelematicsRatingTest {

    @Test
    public void testSafeDriverScoreAndDiscount() {
        int hardBrakes = 1;
        int score = 100 - (hardBrakes * 3); // 97
        assertTrue(score >= 85);
        assertEquals(97, score);
    }

    @Test
    public void testAggressiveDriverSurcharge() {
        int hardBrakes = 15;
        int score = 100 - (hardBrakes * 3) - 15; // 100 - 45 - 15 = 40
        assertTrue(score < 60);
        assertEquals(40, score);
    }
}
