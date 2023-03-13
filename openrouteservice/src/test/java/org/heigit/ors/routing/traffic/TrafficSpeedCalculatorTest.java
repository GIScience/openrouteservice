package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DefaultSpeedCalculator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void testOriginalSmallerThan45AndTrafficSlower() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 40.0;
        int edgeId = 2;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(38, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    @Test
    void testOriginalLargerThan45AndTrafficSlower() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 60.0;
        int edgeId = 3;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(50, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    //Do not overwrite speeds slower than 45 in case traffic is faster
    @Test
    void testOriginalSmallerThan45AndTrafficFaster() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 40.0;
        int edgeId = 4;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(40, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    //Do not overwrite speed data if traffic data is much faster than 110% of original speed
    @Test
    void testOriginalLargerThan45AndTrafficMuchFaster() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 60.0;
        int edgeId = 5;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(60, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    //Increase speed only within 110% of original speed
    @Test
    void testOriginalLargerThan45AndTrafficFaster() {
        IntsRef edgeFlags = encodingManager.createEdgeFlags();
        double originalEdgeSpeed = 60.0;
        int edgeId = 6;
        carEncoder.getAverageSpeedEnc().setDecimal(false, edgeFlags, originalEdgeSpeed);
        EdgeIteratorState edgeIteratorState = createMockedEdgeIteratorState(10, edgeFlags, 0, 1, edgeId, 2, 3);
        assertEquals(65, trafficSpeedCalculator.getSpeed(edgeIteratorState, false, 1), 1e-8);
    }

    private class MockTrafficStorage extends TrafficGraphStorage {

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

