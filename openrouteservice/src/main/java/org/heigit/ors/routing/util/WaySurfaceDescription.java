package org.heigit.ors.routing.util;

public class WaySurfaceDescription {
    private byte wayType;
    private byte surfaceType;

    public byte getWayType() {
        return wayType;
    }

    public void setWayType(byte wayType) {
        this.wayType = wayType;
    }

    public byte getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(byte surfaceType) {
        this.surfaceType = surfaceType;
    }

    public void reset() {
        wayType = 0;
        surfaceType = 0;
    }
}
