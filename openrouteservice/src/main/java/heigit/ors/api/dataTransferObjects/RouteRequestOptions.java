package heigit.ors.api.dataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = RouteRequestDTO.class)
public class RouteRequestOptions {
    @ApiModelProperty(value = "Types of features to avoid", name = "avoid_features")
    @JsonProperty("avoid_features")
    private AvoidFeatures[] avoidFeatures;

    public AvoidFeatures[] getAvoidFeatures() {
        return avoidFeatures;
    }

    public void setAvoidFeatures(AvoidFeatures[] avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
    }
}
