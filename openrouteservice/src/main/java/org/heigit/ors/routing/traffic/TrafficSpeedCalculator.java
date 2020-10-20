package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

public class TrafficSpeedCalculator implements SpeedCalculator {
    protected final DecimalEncodedValue avSpeedEnc;
    // time-dependent stuff
//    private final TrafficGraphStorage trafficGraphStorage;

    public TrafficSpeedCalculator(GraphHopperStorage graph, FlagEncoder encoder) {
        avSpeedEnc = encoder.getAverageSpeedEnc();

        // time-dependent stuff
        EncodingManager encodingManager = graph.getEncodingManager();
//        trafficGraphStorage = GraphStorageUtils.getGraphExtension(graph, TrafficGraphStorage.class);
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = reverse ? edge.getReverse(avSpeedEnc) : edge.get(avSpeedEnc);

//         retrieve time-dependent maxspeed here
//        if (time != -1) {
//            int edgeId = EdgeKeys.getOriginalEdge(edge);
//            double trafficSpeed = trafficGraphStorage.getEdgeValue(edgeId, edge.getBaseNode(), time);
//            if (trafficSpeed < speed)
//                speed = trafficSpeed;
//        }
        return speed;
    }
}
