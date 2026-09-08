package org.heigit.ors.routing.graphhopper.extensions.weightings;

import org.heigit.ors.routing.graphhopper.extensions.weighting.HeatStressWeightingDegreeC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeatStressWeightingDegreeCTest {

    @Test
    void testHeatFactor() {
        assertEquals(1.0, HeatStressWeightingDegreeC.heatFactor(20));
        assertEquals(1.0, HeatStressWeightingDegreeC.heatFactor(26));
        assertEquals(1.03473, HeatStressWeightingDegreeC.heatFactor(27), 0.0001);
        assertEquals(1.1745, HeatStressWeightingDegreeC.heatFactor(30), 0.0001);
        assertEquals(2.1385, HeatStressWeightingDegreeC.heatFactor(40), 0.0001);
    }
}
