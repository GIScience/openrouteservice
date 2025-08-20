package org.heigit.ors.routing.util;

import com.graphhopper.routing.ev.WayType;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;

public class WaySurfaceDescription {
    private WayType wayType;
    private SurfaceType surfaceType;

    public WaySurfaceDescription() {
        reset();
    }

    public WayType getWayType() {
        return wayType;
    }

    public void setWayType(int wayTypeId) {
        this.wayType = WayType.getFromId(wayTypeId);
    }

    public void setWayType(WayType wayType) {
        this.wayType = wayType;
    }

    public SurfaceType getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(SurfaceType surfaceType) {
        this.surfaceType = surfaceType;
    }

    public void reset() {
        wayType = WayType.UNKNOWN;
        surfaceType = SurfaceType.UNKNOWN;
    }
}
