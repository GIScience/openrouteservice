package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.*;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox3DBase;
import heigit.ors.util.FormatUtility;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"minLat", "minLon", "minEle", "maxLat", "maxLon", "maxEle"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSON3DBoundingBox extends BoundingBox3DBase implements BoundingBox {
    private final int ELEVATION_DECIMAL_PLACES = 2;
    private final int COORDINATE_DECIMAL_PLACES = 6;

    public JSON3DBoundingBox(BBox bounds) {
        super(bounds);
    }

    @Override
    public double getMaxEle() {
        return FormatUtility.roundToDecimals(maxEle, ELEVATION_DECIMAL_PLACES);
    }

    @Override
    public double getMinEle() {
        return FormatUtility.roundToDecimals(minEle, ELEVATION_DECIMAL_PLACES);
    }

    @Override
    public double getMinLat() {
        return FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMinLon() {
        return FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLat() {
        return FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLon() {
        return FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES);
    }
}
