package org.heigit.ors.routing.util;

public class WaySurfaceDescription {
    private byte wayType;
    private byte surfaceType;

    public byte getWayType() {
        return wayType;
    }

    public void setWayType(int wayType) {
        this.wayType = (byte)wayType;
    }

    public byte getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(int surfaceType) {
        this.surfaceType = (byte)surfaceType;
    }

    public void reset() {
        wayType = 0;
        surfaceType = 0;
    }
}
