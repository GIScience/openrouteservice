package org.heigit.ors.routing.graphhopper.extensions.weightings;

import org.heigit.ors.routing.graphhopper.extensions.weighting.AdditionWeighting;
import org.heigit.ors.routing.graphhopper.extensions.weighting.ConstantWeighting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdditionWeightingTest {
    @Test
    void sumOfConstants() {
        ConstantWeighting const1 = new ConstantWeighting(1, 10);
        ConstantWeighting const2 = new ConstantWeighting(2, 20);
        ConstantWeighting const3 = new ConstantWeighting(3, 30);
        ConstantWeighting superWeighting = new ConstantWeighting(10, 100);

        ConstantWeighting[] weightings = {const1, const2, const3};
        AdditionWeighting additionWeighting = new AdditionWeighting(weightings, superWeighting);
        assertEquals(60, additionWeighting.calcEdgeWeight(null, false, 0), 0.0001);
        assertEquals(100, additionWeighting.calcEdgeMillis(null, false, 0), 0.0001);
    }
}
