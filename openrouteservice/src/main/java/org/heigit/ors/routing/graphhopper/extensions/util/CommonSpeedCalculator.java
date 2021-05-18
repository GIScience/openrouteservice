package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.ExpiringSpeedStorage;

/**
 * Simple SpeedCalculator based on a SpeedStorage.
 * Default speed of Byte.MIN_VALUE results in original graph speed
 */
public class CommonSpeedCalculator implements SpeedCalculator {
    private ExpiringSpeedStorage expiringSpeedStorage;
    private DecimalEncodedValue avSpeedEnc;
    private FlagEncoder flagEncoder;

    public void init(ExpiringSpeedStorage expiringSpeedStorage, FlagEncoder flagEncoder) {
        this.expiringSpeedStorage = expiringSpeedStorage;
        setEncoder(flagEncoder);
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        int edgeId = EdgeKeys.getOriginalEdge(edge);
        double modifiedSpeed = expiringSpeedStorage.getSpeed(edgeId, reverse);
        if (modifiedSpeed == Byte.MIN_VALUE)
            return reverse ? edge.getReverse(avSpeedEnc) : edge.get(avSpeedEnc);;
        return modifiedSpeed;
    }

    public void setEncoder(FlagEncoder flagEncoder) {
        this.avSpeedEnc = flagEncoder.getAverageSpeedEnc();
    }

    public void setSpeedStorage(ExpiringSpeedStorage expiringSpeedStorage) {
        this.expiringSpeedStorage = expiringSpeedStorage;
    }
}
