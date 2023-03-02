package org.heigit.ors.routing.graphhopper.extensions.userspeed;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.AbstractAdjustedSpeedCalculator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.util.WaySurfaceDescription;

public class RoadPropertySpeedCalculator extends AbstractAdjustedSpeedCalculator {
    private static final double speedFactor = 0.9; //Adapted from ConditionalSpeedCalculator
    private WaySurfaceTypeGraphStorage waySurfaceTypeGraphStorage;
    private RoadPropertySpeedMap roadPropertySpeedMap;
    private byte[] buffer = new byte[4];

    public RoadPropertySpeedCalculator(SpeedCalculator superSpeedCalculator, GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder) {
        super(superSpeedCalculator);
        setWaySurfaceTypeGraphStorage(GraphStorageUtils.getGraphExtension(graphHopperStorage, WaySurfaceTypeGraphStorage.class));
    }

    /**
     * Return the speed on an edge, potentially modified by user speed map
     *
     * @param edge
     * @param reverse
     * @param time    currently unused
     * @return speed of edge
     */
    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = this.superSpeedCalculator.getSpeed(edge, reverse, time);
        WaySurfaceDescription wsd = waySurfaceTypeGraphStorage.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
        Double surfaceTypeSpeed = roadPropertySpeedMap.getByTypedOrdinal(SurfaceType.class, wsd.getSurfaceType());
        if (surfaceTypeSpeed != null)
            speed = Math.min(speed, speedFactor * surfaceTypeSpeed);
        Double wayTypeSpeed = roadPropertySpeedMap.getByTypedOrdinal(WayType.class, wsd.getWayType());
        if (wayTypeSpeed != null)
            speed = Math.min(speed, speedFactor * wayTypeSpeed);
        return speed;
    }

    @Override
    public boolean isTimeDependent() {
        return false;
    }

    public void setWaySurfaceTypeGraphStorage(WaySurfaceTypeGraphStorage waySurfaceTypeGraphStorage) {
        this.waySurfaceTypeGraphStorage = waySurfaceTypeGraphStorage;
    }

    public void setRoadPropertySpeedMap(RoadPropertySpeedMap roadPropertySpeedMap) {
        this.roadPropertySpeedMap = roadPropertySpeedMap;
    }
}
