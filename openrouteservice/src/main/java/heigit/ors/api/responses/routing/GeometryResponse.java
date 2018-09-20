package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONGeometryResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.EncodedPolylineGeometryResponse;
import io.swagger.annotations.ApiModel;


public abstract class GeometryResponse {
    protected Coordinate[] coordinates;
    protected boolean includeElevation;

    public GeometryResponse(Coordinate[] coords, boolean includeElevation) {
        this.coordinates = coords;
        this.includeElevation = includeElevation;
    }

    public abstract Object getGeometry();

}
