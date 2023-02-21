package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DefaultSpeedCalculator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.graphhopper.util.GHUtility.createMockedEdgeIteratorState;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TrafficSpeedCalculatorTest {
    private CarFlagEncoder carEncoder;
    private EncodingManager encodingManager;
    private TrafficSpeedCalculator trafficSpeedCalculator;

    @BeforeEach
    void setUp() {
        carEncoder = new CarFlagEncoder();
        encodingManager = EncodingManager.create(carEncoder);
        trafficSpeedCalculator = new TrafficSpeedCalculator(new DefaultSpeedCalculator(carEncoder));
        trafficSpeedCalculator.setTrafficGraphStorage(new MockTrafficStorage());
    }

    @Test
    void testNoTrafficData() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 80.0;
        int edgeId = 0;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(originalEdgeSpeed, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    @Test
    void testOriginalSlowerThanTraffic() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 5.0;
        int edgeId = 1;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(originalEdgeSpeed, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }


    /**
     * Parametrized test for the following former test scenarios:
     * testOriginalSmallerThan45AndTrafficSlower
     * testOriginalLargerThan45AndTrafficSlower
     * testOriginalSmallerThan45AndTrafficFaster
     * testOriginalLargerThan45AndTrafficMuchFaster
     * testOriginalLargerThan45AndTrafficFaster
     */
    @ParameterizedTest
    @CsvSource({
            "40.0, 2, 38",
            "60.0, 3, 50",
            "40.0, 4, 40",
            "60.0, 5, 60",
            "60.0, 6, 65"
    })
    void testGetConvertedSpeedFromEdges(Double originalEdgeSpeed, Integer edgeId, Integer expectedEdgeSpeed) {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(expectedEdgeSpeed, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    private static class MockTrafficStorage extends TrafficGraphStorage {

        @Override
        public int getSpeedValue(int edgeId, int baseNode, int adjNode, long unixMilliSeconds, int timeZoneOffset) {
            switch (edgeId) {
                case 1:
                    return 10;
                case 2:
                    return 38;
                case 3:
                    return 50;
                case 4:
                    return 60;
                case 5:
                    return 90;
                case 6:
                    return 65;
                default:
                    return -1;
            }
        }
    }
}

