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

package org.heigit.ors.api.responses.routing.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.heigit.ors.routing.RouteResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Contains total sums of duration, route distance and actual distance of the route.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONSummary {
    @ApiModelProperty(value = "Total route distance in specified units.", example = "12.6")
    @JsonProperty(value = "distance")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.2d")
    protected Double distance;
    @ApiModelProperty(value = "Total duration in seconds.", example = "604")
    @JsonProperty(value = "duration")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double duration;
    @ApiModelProperty(value = "Total ascent in meters." +
            "CUSTOM_KEYS:{'validWhen':{'ref':'elevation','value':true}}", example = "166.3")
    @JsonProperty(value = "ascent")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double ascent;
    @ApiModelProperty(value = "Total descent in meters." +
            "CUSTOM_KEYS:{'validWhen':{'ref':'elevation','value':true}}", example = "201.3")
    @JsonProperty(value = "descent")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
    protected Double descent;

    @JsonProperty(value = "transfers")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected int transfers;

    public int getTransfers() {
        return transfers;
    }

    public void setTransfers(int transfers) {
        this.transfers = transfers;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    @JsonProperty(value = "fare")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    protected int fare;

    public JSONSummary(Double distance, Double duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public JSONSummary(Double distance, Double duration, Double ascent, Double descent) {
        this(distance, duration);
        this.ascent = ascent;
        this.descent = descent;
    }

    public JSONSummary(RouteResult route, boolean includeElevation, boolean isPtRequest) {
        if(includeElevation) {
            this.ascent = route.getSummary().getAscent();
            this.descent = route.getSummary().getDescent();
        }
        if(isPtRequest) {
            this.transfers = route.getSummary().getTransfers();
            this.fare = route.getSummary().getFare();
        }
        this.distance = route.getSummary().getDistance();
        this.duration = route.getSummary().getDuration();
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getDescent() {
        return descent;
    }

    public Double getAscent() {
        return ascent;
    }
}