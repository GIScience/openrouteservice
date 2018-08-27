package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoutingPreference {
    @JsonProperty("fastest") FASTEST,
    @JsonProperty("shortest") SHORTEST,
    @JsonProperty("recommended") RECOMMENDED
}
