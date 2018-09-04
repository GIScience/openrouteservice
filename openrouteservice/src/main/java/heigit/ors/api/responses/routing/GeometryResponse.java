package heigit.ors.api.responses.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;

public abstract class GeometryResponse {
    protected Coordinate[] coordinates;
    protected boolean includeElevation;

    public GeometryResponse(Coordinate[] coords, boolean includeElevation) {
        this.coordinates = coords;
        this.includeElevation = includeElevation;
    }

    @JsonProperty("geometry")
    public abstract Object getGeometry();

}
