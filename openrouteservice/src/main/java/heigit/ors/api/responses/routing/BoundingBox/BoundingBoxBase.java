package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.util.FormatUtility;

public class BoundingBoxBase implements BoundingBox {
    protected final int COORDINATE_DECIMAL_PLACES = 6;
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

    public double[] getAsArray() {
        return new double[] {
                FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES),
                FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES)};
    }
}
