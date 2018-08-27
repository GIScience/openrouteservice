package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RouteGeometryFormat {
    @JsonProperty("json") JSON,
    @JsonProperty("geojson") GEOJSON,
    @JsonProperty("encoded_polyline") POLYLINE
}
