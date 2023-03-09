package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.AbstractAdjustedSpeedCalculator;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.ExpiringSpeedStorage;

/**
 * Simple SpeedCalculator based on a SpeedStorage.
 * Default speed of Byte.MIN_VALUE results in original graph speed
 */
public class ExpiringSpeedCalculator extends AbstractAdjustedSpeedCalculator {
    private ExpiringSpeedStorage expiringSpeedStorage;

    public ExpiringSpeedCalculator(SpeedCalculator superSpeedCalculator, ExpiringSpeedStorage expiringSpeedStorage) {
        super(superSpeedCalculator);
        this.expiringSpeedStorage = expiringSpeedStorage;
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        int edgeId = EdgeIteratorStateHelper.getOriginalEdge(edge);
        double modifiedSpeed = expiringSpeedStorage.getSpeed(edgeId, reverse);
        if (modifiedSpeed == Byte.MIN_VALUE)
            return this.superSpeedCalculator.getSpeed(edge, reverse, time);
        return modifiedSpeed;
    }
}
