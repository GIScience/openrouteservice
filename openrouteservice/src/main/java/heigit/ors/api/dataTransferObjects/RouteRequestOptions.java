package heigit.ors.api.dataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = RouteRequestDTO.class)
public class RouteRequestOptions {
    @ApiModelProperty(value = "Types of features to avoid", name = "avoid_features")
    @JsonProperty("avoid_features")
    private AvoidFeatures[] avoidFeatures;

    @ApiModelProperty(value = "maximum speed that can be travelled", name = "maximum_speed")
    @JsonProperty("maximum_speed")
    private double maximumSpeed;

    @ApiModelProperty(value = "Specify which type of border crossing to avoid",
            notes = "Used to specify whether the route should cross specific border types")
    @JsonProperty("avoid_borders")
    private APIEnums.AvoidBorders avoidBorders;

    public AvoidFeatures[] getAvoidFeatures() {
        return avoidFeatures;
    }

    public void setAvoidFeatures(AvoidFeatures[] avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public APIEnums.AvoidBorders getAvoidBorders() {
        return avoidBorders;
    }

    public void setAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
        this.avoidBorders = avoidBorders;
    }
}
