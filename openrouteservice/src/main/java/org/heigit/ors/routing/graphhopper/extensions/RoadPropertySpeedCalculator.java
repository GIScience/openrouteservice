package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.EdgeKeys;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.SpeedCalculator;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.util.WaySurfaceDescription;

import java.time.ZonedDateTime;

public class RoadPropertySpeedCalculator implements SpeedCalculator {
    protected DecimalEncodedValue avSpeedEnc;
    private WaySurfaceTypeGraphStorage waySurfaceTypeGraphStorage;
    private RoadPropertySpeedMap roadPropertySpeedMap;
    private byte[] buffer = new byte[4];


    public void init(GraphHopperStorage graphHopperStorage, FlagEncoder flagEncoder, RoadPropertySpeedMap roadPropertySpeedMap) {
        setEncoder(flagEncoder);
        setWaySurfaceTypeGraphStorage(GraphStorageUtils.getGraphExtension(graphHopperStorage, WaySurfaceTypeGraphStorage.class));
        setRoadPropertySpeedMap(roadPropertySpeedMap);
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = reverse ? edge.getReverse(avSpeedEnc) : edge.get(avSpeedEnc);
        WaySurfaceDescription wsd = waySurfaceTypeGraphStorage.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
        Double surfaceTypeSpeed = roadPropertySpeedMap.getByTypedOrdinal(SurfaceType.class, wsd.getSurfaceType());
        if(surfaceTypeSpeed != null && speed > surfaceTypeSpeed)
            speed = surfaceTypeSpeed;
        Double wayTypeSpeed = roadPropertySpeedMap.getByTypedOrdinal(WayType.class, wsd.getWayType());
        if(wayTypeSpeed != null && speed > wayTypeSpeed)
            speed = wayTypeSpeed;
        return speed;
    }

    public void setEncoder(FlagEncoder flagEncoder) {
        this.avSpeedEnc = flagEncoder.getAverageSpeedEnc();
    }

    public void setWaySurfaceTypeGraphStorage(WaySurfaceTypeGraphStorage waySurfaceTypeGraphStorage) {
        this.waySurfaceTypeGraphStorage = waySurfaceTypeGraphStorage;
    }

    public void setRoadPropertySpeedMap(RoadPropertySpeedMap roadPropertySpeedMap){
        this.roadPropertySpeedMap = roadPropertySpeedMap;
    }
}
