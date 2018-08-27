package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RoutingUnits {
    @JsonProperty("m") METRES,
    @JsonProperty("km") KILOMETRES,
    @JsonProperty("mi") MILES
}
