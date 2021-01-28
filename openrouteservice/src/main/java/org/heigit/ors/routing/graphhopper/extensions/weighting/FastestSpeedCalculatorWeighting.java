package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Fastest weighting where speed can be adapted by SpeedCalculator
 */
public class FastestSpeedCalculatorWeighting extends AbstractSpeedCalculatorWeighting {
    private final double headingPenalty;
    private final long headingPenaltyMillis;
    private final double maxSpeed;

    public FastestSpeedCalculatorWeighting(FlagEncoder encoder, PMap map) {
        super(encoder);
        this.headingPenalty = map.getDouble("heading_penalty", 300.0D);
        this.headingPenaltyMillis = Math.round(this.headingPenalty * 1000.0D);
        this.maxSpeed = encoder.getMaxSpeed() / 3.6D;
    }

    public FastestSpeedCalculatorWeighting(FlagEncoder encoder, PMap map, SpeedCalculator speedCalculator) {
        this(encoder, map);
        setSpeedCalculator(speedCalculator);
    }

    public double getMinWeight(double distance) {
        return distance / this.maxSpeed;
    }

    /**
     * Calculate edge weight with speed adapted by speedcalculator.
     *
     * @param edgeState
     * @param reverse
     * @param prevOrNextEdgeId
     * @return weight needed for edge traversal
     */
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (speedCalculator == null)
            throw new IllegalStateException("No SpeedCalculator set");
        double speed = reverse ? edgeState.getReverse(this.avSpeedEnc) : edgeState.get(this.avSpeedEnc);
        if (speed == 0.0D) {
            return 1.0D / 0.0;
        } else {
            double adaptedSpeed = this.speedCalculator.getSpeed(edgeState, reverse, -1);
            if (adaptedSpeed != -1)
                speed = adaptedSpeed;
            double time = edgeState.getDistance() / speed * 3.6D;
            boolean unfavoredEdge = edgeState.get(EdgeIteratorState.UNFAVORED_EDGE);
            if (unfavoredEdge) {
                time += this.headingPenalty;
            }

            return time;
        }
    }

    /**
     * Calculate edge milliseconds with speed adapted by speedcalculator.
     * This needs to override the AbstractWeighting method as the speed is changed.
     *
     * @param edgeState
     * @param reverse
     * @param prevOrNextEdgeId
     * @return millis needed for edge traversal
     */
    @Override
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
        return "fastestspeedcalculator";
    }
}
