package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

public class RoutingTrafficSpeedCalculator extends TrafficSpeedCalculator {

    public RoutingTrafficSpeedCalculator(SpeedCalculator superSpeedCalculator, GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder) {
        super(superSpeedCalculator);
        init(graphHopperStorage, flagEncoder);
    }

    @Override
    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = superSpeedCalculator.getSpeed(edge, reverse, time);
        if (time != -1) {
            int edgeId = EdgeKeys.getOriginalEdge(edge);
            double trafficSpeed = reverse ?
                    trafficGraphStorage.getSpeedValue(edgeId, edge.getAdjNode(), edge.getBaseNode(), time, timeZoneOffset)
                    :trafficGraphStorage.getSpeedValue(edgeId, edge.getBaseNode(), edge.getAdjNode(), time, timeZoneOffset);
            if (trafficSpeed != -1) {
                // This is important for the correctness of LM approximation
                if (trafficSpeed < speed)
                    speed = trafficSpeed;
            }
        }
        return speed;
    }
}
