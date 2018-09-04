package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxBase;
import heigit.ors.util.FormatUtility;


@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"minLat", "minLon", "maxLat", "maxLon"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONBoundingBox extends BoundingBoxBase implements BoundingBox {
    private final int COORDINATE_DECIMAL_PLACES = 6;

    public JSONBoundingBox(BBox bounding) {
        super(bounding);
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
