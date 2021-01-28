package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Fastest weighting where speed can be adapted by SpeedCalculator
 */
public class ShortestSpeedCalculatorWeighting extends AbstractSpeedCalculatorWeighting {
    public ShortestSpeedCalculatorWeighting(FlagEncoder encoder) {
        super(encoder);
    }

    public ShortestSpeedCalculatorWeighting(FlagEncoder encoder, SpeedCalculator speedCalculator) {
        this(encoder);
        setSpeedCalculator(speedCalculator);
    }

    public double getMinWeight(double currDistToGoal) {
        return currDistToGoal;
    }

    /**
     * Calculate shortest weight. This is mostly independent of speedcalculator, but need to account for speed 0 case.
     * @param edgeState
     * @param reverse
     * @param prevOrNextEdgeId
     * @return distance of edge or infinity if speed == 0
     */
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (speedCalculator == null)
            throw new IllegalStateException("No SpeedCalculator set");
        if(this.speedCalculator.getSpeed(edgeState, reverse, -1) == 0.0D)
            return 1.0D / 0.0;
        return edgeState.getDistance();
    }

    @Override
    public String getName() {
        return "shortestspeedcalculator";
    }
}
