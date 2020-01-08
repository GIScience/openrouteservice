/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Alternative Routes", parent = RouteRequestOptions.class, description = "Specifies whether alternative routes are computed, and parameters for the algorithm determining suitable alternatives.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteRequestAlternativeRoutes {
    public static final String PARAM_TARGET_COUNT = "target_count";
    public static final String PARAM_WEIGHT_FACTOR = "weight_factor";
    public static final String PARAM_SHARE_FACTOR = "share_factor";

    @ApiModelProperty(name = PARAM_TARGET_COUNT, value = "Target number of alternative routes to compute. Service returns up to this number of routes that fulfill the share-factor and weight-factor constraints.", example = "2")
    @JsonProperty(PARAM_TARGET_COUNT)
    private Integer targetCount;
    @JsonIgnore
    private boolean hasTargetCount = false;

    @ApiModelProperty(name = PARAM_WEIGHT_FACTOR, value = "Maximum factor by which route weight may diverge from the optimal route. The default value of 1.4 means alternatives can be up to 1.4 times longer (costly) than the optimal route.", example = "1.4")
    @JsonProperty(PARAM_WEIGHT_FACTOR)
    private Double weightFactor;
    @JsonIgnore
    private boolean hasWeightFactor = false;

    @ApiModelProperty(name = PARAM_SHARE_FACTOR, value = "Maximum fraction of the route that alternatives may share with the optimal route. The default value of 0.6 means alternatives can share up to 60% of path segments with the optimal route.", example = "0.6")
    @JsonProperty(PARAM_SHARE_FACTOR)
    private Double shareFactor;
    @JsonIgnore
    private boolean hasShareFactor = false;

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
        hasTargetCount = true;
    }

    public boolean hasTargetCount() {
        return hasTargetCount;
    }

    public Double getWeightFactor() {
        return weightFactor;
    }

    public void setWeightFactor(Double weightFactor) {
        this.weightFactor = weightFactor;
        hasWeightFactor = true;
    }

    public boolean hasWeightFactor() {
        return hasWeightFactor;
    }

    public Double getShareFactor() {
        return shareFactor;
    }

    public void setShareFactor(Double shareFactor) {
        this.shareFactor = shareFactor;
        hasShareFactor = true;
    }

    public boolean hasShareFactor() {
        return hasShareFactor;
    }
}
