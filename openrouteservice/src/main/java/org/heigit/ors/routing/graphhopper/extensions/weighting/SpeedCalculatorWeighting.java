package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Fastest weighting where speed can be adapted by SpeedCalculator
 */
public class SpeedCalculatorWeighting extends FastestWeighting {
    private SpeedCalculator speedCalculator;
    private final double headingPenalty;
    private final long headingPenaltyMillis;

    public SpeedCalculatorWeighting(FlagEncoder encoder, PMap map) {
        super(encoder, map);
        this.headingPenalty = map.getDouble("heading_penalty", 300.0D);
        this.headingPenaltyMillis = Math.round(this.headingPenalty * 1000.0D);
    }

    public SpeedCalculatorWeighting(FlagEncoder encoder, PMap map, SpeedCalculator speedCalculator) {
        super(encoder, map);
        setSpeedCalculator(speedCalculator);
        this.headingPenalty = map.getDouble("heading_penalty", 300.0D);
        this.headingPenaltyMillis = Math.round(this.headingPenalty * 1000.0D);
    }

    /**
     * Calculate edge weight with speed adapted by speedcalculator.
     * @param edge
     * @param reverse
     * @param prevOrNextEdgeId
     * @return time needed for edge traversal
     */
    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        if(speedCalculator == null)
            throw new IllegalStateException("No SpeedCalculator set");
        double speed = reverse ? edge.getReverse(this.avSpeedEnc) : edge.get(this.avSpeedEnc);
        if (speed == 0.0D) {
            return 1.0D / 0.0;
        } else {
            double adaptedSpeed = this.speedCalculator.getSpeed(edge, reverse, -1);
            if(adaptedSpeed != -1)
                speed = adaptedSpeed;
            double time = edge.getDistance() / speed * 3.6D;
            boolean unfavoredEdge = edge.get(EdgeIteratorState.UNFAVORED_EDGE);
            if (unfavoredEdge) {
                time += this.headingPenalty;
            }

            return time;
        }
    }

    @Override
    //TODO Adapt this to support the speeds from speedcalculator
    public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        // TODO move this to AbstractWeighting? see #485
        long time = 0;
        boolean unfavoredEdge = edgeState.get(EdgeIteratorState.UNFAVORED_EDGE);
        if (unfavoredEdge)
            time += headingPenaltyMillis;

        return time + super.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    }

    @Override
    public String getName() {
        return "speedcalculatorfastest";
    }

    public SpeedCalculator getSpeedCalculator() {
        return this.speedCalculator;
    }

    public void setSpeedCalculator(SpeedCalculator speedCalculator) {
        this.speedCalculator = speedCalculator;
    }
}
