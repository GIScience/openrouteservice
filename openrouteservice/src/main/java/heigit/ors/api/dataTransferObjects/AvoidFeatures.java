package heigit.ors.api.dataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel
public enum AvoidFeatures {
    @JsonProperty("tollways") TOLLWAYS,
    @JsonProperty("ferries") FERRIES
}
