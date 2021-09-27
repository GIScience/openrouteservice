package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.AbstractAdjustedSpeedCalculator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.ExpiringSpeedStorage;

/**
 * Simple SpeedCalculator based on a SpeedStorage.
 * Default speed of Byte.MIN_VALUE results in original graph speed
 */
public class ExpiringSpeedStorageSpeedCalculator extends AbstractAdjustedSpeedCalculator {
    private ExpiringSpeedStorage expiringSpeedStorage;

    public ExpiringSpeedStorageSpeedCalculator(SpeedCalculator superSpeedCalculator, ExpiringSpeedStorage expiringSpeedStorage) {
        super(superSpeedCalculator);
        this.expiringSpeedStorage = expiringSpeedStorage;
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        int edgeId = EdgeKeys.getOriginalEdge(edge);
        double modifiedSpeed = expiringSpeedStorage.getSpeed(edgeId, reverse);
        if (modifiedSpeed == Byte.MIN_VALUE)
            return this.superSpeedCalculator.getSpeed(edge, reverse, time);
        return modifiedSpeed;
    }

    public void setSpeedStorage(ExpiringSpeedStorage expiringSpeedStorage) {
        this.expiringSpeedStorage = expiringSpeedStorage;
    }
}
