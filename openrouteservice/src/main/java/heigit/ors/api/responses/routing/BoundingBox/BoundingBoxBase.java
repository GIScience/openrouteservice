package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;

public class BoundingBoxBase implements BoundingBox {
    protected double minLat;
    protected double minLon;
    protected double maxLat;
    protected double maxLon;

    public BoundingBoxBase() {}

    public BoundingBoxBase(BBox bounding) {
        minLat = bounding.minLat;
        minLon = bounding.minLon;
        maxLat = bounding.maxLat;
        maxLon = bounding.maxLon;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMaxLon() {
        return maxLon;
    }
}
