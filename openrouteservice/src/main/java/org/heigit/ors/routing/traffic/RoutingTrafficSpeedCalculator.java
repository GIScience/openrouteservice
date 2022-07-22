package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

public class RoutingTrafficSpeedCalculator extends TrafficSpeedCalculator {
    private double maxPossibleSpeed;

    public RoutingTrafficSpeedCalculator(SpeedCalculator superSpeedCalculator, GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder) {
        super(superSpeedCalculator);
        init(graphHopperStorage, flagEncoder);
        maxPossibleSpeed = flagEncoder.getMaxSpeed();
    }

    @Override
    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = super.getSpeed(edge, reverse, time);

        if (speed > maxPossibleSpeed)
            speed = maxPossibleSpeed;

        return speed;
    }
}
