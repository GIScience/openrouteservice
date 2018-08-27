package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RouteResponseType {
    @JsonProperty("gpx") GPX,
    @JsonProperty("json") JSON,
    @JsonProperty("geojson") GEOJSON
}
