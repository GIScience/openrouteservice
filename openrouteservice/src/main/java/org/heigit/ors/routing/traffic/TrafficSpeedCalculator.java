package org.heigit.ors.routing.traffic;

import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.util.AbstractAdjustedSpeedCalculator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.VehicleFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HereTrafficGraphStorage;

import java.time.ZonedDateTime;

public class TrafficSpeedCalculator extends AbstractAdjustedSpeedCalculator {
    // time-dependent stuff
    protected HereTrafficGraphStorage hereTrafficGraphStorage;
    protected int timeZoneOffset;
    private VehicleFlagEncoder vehicleFlagEncoder;
    private boolean isVehicle = false;
    private boolean isHGV = false;
    private double HGVTrafficSpeedLimit = 80.0;

    public TrafficSpeedCalculator(SpeedCalculator superSpeedCalculator) {
        super(superSpeedCalculator);
    }

    public void init(GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder) {
        if (flagEncoder instanceof VehicleFlagEncoder)
            setVehicleFlagEncoder((VehicleFlagEncoder) flagEncoder);
        if (flagEncoder instanceof HeavyVehicleFlagEncoder)
            isHGV = true;
        setTrafficGraphStorage(GraphStorageUtils.getGraphExtension(graphHopperStorage, HereTrafficGraphStorage.class));
    }

    @Override
    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = superSpeedCalculator.getSpeed(edge, reverse, time);

        int edgeId = EdgeKeys.getOriginalEdge(edge);
        double trafficSpeed;
        if (time == -1)
            trafficSpeed = reverse ?
                    hereTrafficGraphStorage.getMaxSpeedValue(edgeId, edge.getAdjNode(), edge.getBaseNode())
                    : hereTrafficGraphStorage.getMaxSpeedValue(edgeId, edge.getBaseNode(), edge.getAdjNode());
        else
            trafficSpeed = reverse ?
                    hereTrafficGraphStorage.getSpeedValue(edgeId, edge.getAdjNode(), edge.getBaseNode(), time, timeZoneOffset)
                    : hereTrafficGraphStorage.getSpeedValue(edgeId, edge.getBaseNode(), edge.getAdjNode(), time, timeZoneOffset);

        if (trafficSpeed > 0) {
            //TODO: This is a heuristic to provide expected results given traffic data and ORS internal speed calculations.
            if (isVehicle) {
                trafficSpeed = vehicleFlagEncoder.adjustSpeedForAcceleration(edge.getDistance(), trafficSpeed);
                // For heavy vehicles, consider the traffic speeds only up to a predefined speeds
                if(!isHGV || (isHGV && trafficSpeed <= HGVTrafficSpeedLimit)) {
                    speed = trafficSpeed;
                }
            } else {
                if (speed >= 45.0 && !(trafficSpeed > 1.1 * speed) || trafficSpeed < speed) {
                    speed = trafficSpeed;
                }
            }
        }

        return speed;
    }

    public void setVehicleFlagEncoder(VehicleFlagEncoder flagEncoder) {
        this.vehicleFlagEncoder = flagEncoder;
        isVehicle = true;
    }

    public void setTrafficGraphStorage(HereTrafficGraphStorage hereTrafficGraphStorage) {
        this.hereTrafficGraphStorage = hereTrafficGraphStorage;
    }

    public void setZonedDateTime(ZonedDateTime zdt) {
        this.timeZoneOffset = zdt.getOffset().getTotalSeconds() / 3600;
    }

    @Override
    public boolean isTimeDependent() {
        return true;
    }
}
