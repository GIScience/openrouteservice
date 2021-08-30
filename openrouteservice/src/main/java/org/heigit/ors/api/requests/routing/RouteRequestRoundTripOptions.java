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

@ApiModel(value = "Round Trip Route Options", parent = RouteRequestOptions.class, description = "Specifies the parameters for generating round trip routes.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteRequestRoundTripOptions {
    public static final String PARAM_LENGTH = "length";
    public static final String PARAM_POINTS = "points";
    public static final String PARAM_SEED = "seed";

    @ApiModelProperty(name = PARAM_LENGTH, value = "The target length of the route in `m` (note that this is a preferred value, but results may be different).",
            example = "10000")
    @JsonProperty(PARAM_LENGTH)
    private Float length;
    @JsonIgnore
    private boolean hasLength = false;

    @ApiModelProperty(name = PARAM_POINTS, value = "The number of points to use on the route. Larger values create more circular routes.",
            example = "5")
    @JsonProperty(PARAM_POINTS)
    private Integer points;
    @JsonIgnore
    private boolean hasPoints = false;

    @ApiModelProperty(name = PARAM_SEED, value = "A seed to use for adding randomisation to the overall direction of the generated route",
            example = "1")
    @JsonProperty(PARAM_SEED)
    private Long seed;
    @JsonIgnore
    private boolean hasSeed = false;

    public Float getLength() {
        return length;
    }

    public void setLength(Float length) {
        this.length = length;
        hasLength = true;
    }

    public Integer getPoints() {
        return this.points;
    }

    public void setPoints(Integer points) {
        this.points = points;
        hasPoints = true;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
        hasSeed = true;
    }

    public boolean hasLength() {
        return hasLength;
    }

    public boolean hasPoints() {
        return hasPoints;
    }

    public boolean hasSeed() {
        return hasSeed;
    }
}
