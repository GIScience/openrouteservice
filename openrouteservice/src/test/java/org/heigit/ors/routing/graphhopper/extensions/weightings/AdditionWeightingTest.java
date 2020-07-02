package org.heigit.ors.routing.graphhopper.extensions.weightings;

import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.weighting.AdditionWeighting;
import org.heigit.ors.routing.graphhopper.extensions.weighting.ConstantWeighting;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdditionWeightingTest {
    private final EncodingManager encodingManager;
    private final CarFlagEncoder flagEncoder;

    public AdditionWeightingTest() {
        encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS, 4);
        flagEncoder = (CarFlagEncoder)encodingManager.getEncoder(FlagEncoderNames.CAR_ORS);
    }

    @Test
    public void sumOfConstants () {
        ConstantWeighting const1 = new ConstantWeighting(1, 10);
        ConstantWeighting const2 = new ConstantWeighting(2, 20);
        ConstantWeighting const3 = new ConstantWeighting(3, 30);
        ConstantWeighting superWeighting = new ConstantWeighting(10, 100);

        ConstantWeighting[] weightings = {const1, const2, const3};
        AdditionWeighting additionWeighting = new AdditionWeighting(weightings, superWeighting, flagEncoder);
        assertEquals(60, additionWeighting.calcWeight(null, false, 0), 0.0001);
        assertEquals(100, additionWeighting.calcMillis(null, false, 0), 0.0001);
    }
}
