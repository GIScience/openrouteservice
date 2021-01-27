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
     * @param edgeState
     * @param reverse
     * @param prevOrNextEdgeId
     * @return weight needed for edge traversal
     */
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if(speedCalculator == null)
            throw new IllegalStateException("No SpeedCalculator set");
        double speed = reverse ? edgeState.getReverse(this.avSpeedEnc) : edgeState.get(this.avSpeedEnc);
        if (speed == 0.0D) {
            return 1.0D / 0.0;
        } else {
            double adaptedSpeed = this.speedCalculator.getSpeed(edgeState, reverse, -1);
            if(adaptedSpeed != -1)
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
        if (edgeState.getBaseNode() == edgeState.getAdjNode()) {
            reverse = false;
        }

        if ((!reverse || edgeState.getReverse(this.accessEnc)) && (reverse || edgeState.get(this.accessEnc))) {
            double speed = reverse ? edgeState.getReverse(this.avSpeedEnc) : edgeState.get(this.avSpeedEnc);
            if (!Double.isInfinite(speed) && !Double.isNaN(speed) && speed >= 0.0D) {
                if (speed == 0.0D) {
                    throw new IllegalStateException("Speed cannot be 0 for unblocked edge, use access properties to mark edge blocked! Should only occur for shortest path calculation. See #242.");
                } else {
                    double adaptedSpeed = this.speedCalculator.getSpeed(edgeState, reverse, -1);
                    if(adaptedSpeed != -1)
                        speed = adaptedSpeed;
                    return time + (long)(edgeState.getDistance() * 3600.0D / speed);
                }
            } else {
                throw new IllegalStateException("Invalid speed stored in edge! " + speed);
            }
        } else {
            throw new IllegalStateException("Calculating time should not require to read speed from edge in wrong direction. (" + edgeState.getBaseNode() + " - " + edgeState.getAdjNode() + ") " + edgeState.fetchWayGeometry(3) + ", dist: " + edgeState.getDistance() + " Reverse:" + reverse + ", fwd:" + edgeState.get(this.accessEnc) + ", bwd:" + edgeState.getReverse(this.accessEnc) + ", fwd-speed: " + edgeState.get(this.avSpeedEnc) + ", bwd-speed: " + edgeState.getReverse(this.avSpeedEnc));
        }
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
