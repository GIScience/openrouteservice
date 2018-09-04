package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GeometryResponse;
import heigit.ors.util.PolylineEncoder;

public class EncodedPolylineGeometryResponse extends GeometryResponse {
    public EncodedPolylineGeometryResponse(Coordinate[]coordinates, boolean includeElevation) {
        super(coordinates, includeElevation);
    }

    @Override
    public Object getGeometry() {
        StringBuffer strBuffer = new StringBuffer();
        return PolylineEncoder.encode(coordinates, includeElevation, strBuffer);
    }
}
