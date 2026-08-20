package org.heigit.ors.routing.graphhopper.extensions.weightings;

import org.heigit.ors.routing.graphhopper.extensions.weighting.HeatStressWeightingDegreeC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeatStressWeightingDegreeCTest {

    @Test
    void testHeatFactor() {
        assertEquals(HeatStressWeightingDegreeC.heatFactor(20), 1.0);
        assertEquals(HeatStressWeightingDegreeC.heatFactor(25), 1.0);
        assertEquals(HeatStressWeightingDegreeC.heatFactor(26), 1.0334);
        assertEquals(HeatStressWeightingDegreeC.heatFactor(30), 1.2184, 0.0001);
        assertEquals(HeatStressWeightingDegreeC.heatFactor(40), 2.9807, 0.0001);
    }
}
