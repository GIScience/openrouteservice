package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Fastest weighting where speed can be adapted by SpeedCalculator
 */
public abstract class AbstractSpeedCalculatorWeighting extends AbstractWeighting {
    protected SpeedCalculator speedCalculator;

    public AbstractSpeedCalculatorWeighting(FlagEncoder encoder){
        super(encoder);
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
                    return (long)(edgeState.getDistance() * 3600.0D / speed);
                }
            } else {
                throw new IllegalStateException("Invalid speed stored in edge! " + speed);
            }
        } else {
            throw new IllegalStateException("Calculating time should not require to read speed from edge in wrong direction. (" + edgeState.getBaseNode() + " - " + edgeState.getAdjNode() + ") " + edgeState.fetchWayGeometry(3) + ", dist: " + edgeState.getDistance() + " Reverse:" + reverse + ", fwd:" + edgeState.get(this.accessEnc) + ", bwd:" + edgeState.getReverse(this.accessEnc) + ", fwd-speed: " + edgeState.get(this.avSpeedEnc) + ", bwd-speed: " + edgeState.getReverse(this.avSpeedEnc));
        }
    }

    public SpeedCalculator getSpeedCalculator() {
        return this.speedCalculator;
    }

    public void setSpeedCalculator(SpeedCalculator speedCalculator) {
        this.speedCalculator = speedCalculator;
    }
}
