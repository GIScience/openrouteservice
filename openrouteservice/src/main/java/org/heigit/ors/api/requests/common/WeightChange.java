package org.heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vividsolutions.jts.geom.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.json.JSONObject;

@ApiModel(value = "Weight Change", parent = WeightChanges.class, description = "TODO")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JsonIgnoreProperties(ignoreUnknown = true)
class WeightChange {
  // TODO
  @ApiModelProperty
  @JsonProperty("geometry")
  private Geometry geometry;

  // TODO really!
  @ApiModelProperty
//  @JsonProperty("properties")
  @JsonRawValue
  private String properties;

  @JsonIgnore
  private Double weight;

  public WeightChange() {}

  public WeightChange(Geometry geometry, Double weight) {
    this.geometry = geometry;
    this.weight = weight;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public Double getWeight() {
    return weight;
  }
}
