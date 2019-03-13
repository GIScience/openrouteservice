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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;
import heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "List containing the segments and its correspoding steps which make up the route.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONSegment {
    @ApiModelProperty(value = "Contains the distance of the segment in specified units.", example = "253")
    @JsonProperty("distance")
    private Double distance;
    @ApiModelProperty(value = "Contains the duration of the segment in seconds.", example = "37.7")
    @JsonProperty("duration")
    private Double duration;
    @ApiModelProperty("List containing the specific steps the segment consists of.")
    @JsonProperty("steps")
    private List<JSONStep> steps;
    @ApiModelProperty(value = "Contains the deviation compared to a straight line that would have the factor `1`. Double the Distance would be a `2`. CUSTOM_KEYS:{'validWhen':{'ref':'attributes','valueContains':'detourfactor'}}", example = "0.5")
    @JsonProperty("detourfactor")
    private Double detourFactor;
    @ApiModelProperty(value = "Contains the proportion of the route in percent. CUSTOM_KEYS:{'validWhen':{'ref':'attributes','valueContains':'percentage'}}", example = "43.2")
    @JsonProperty("percentage")
    private Double percentage;
    @ApiModelProperty(value = "Contains the average speed of this segment in km/h. CUSTOM_KEYS:{'validWhen':{'ref':'attributes','valueContains':'avgspeed'}}", example = "56.3")
    @JsonProperty("avgspeed")
    private Double averageSpeed;
    @ApiModelProperty(value = " Contains ascent of this segment in metres. CUSTOM_KEYS:{'validWhen':{'ref':'elevation',value:true}}", example = "56.3")
    @JsonProperty("ascent")
    private Double ascent;
    @ApiModelProperty(value = "Contains descent of this segment in metres. CUSTOM_KEYS:{'validWhen':{'ref':'elevation',value:true}}", example = "45.2")
    @JsonProperty("descent")
    private Double descent;

    public JSONSegment(RouteSegment routeSegment, RouteRequest request, double routeLength) {
        this.distance = routeSegment.getDistance();
        this.duration = routeSegment.getDuration();
        this.detourFactor = routeSegment.getDetourFactor();
        if(request.hasUseElevation() && request.getUseElevation()) {
            this.ascent = routeSegment.getAscent();
            this.descent = routeSegment.getDescent();
        }
        steps = new ArrayList<>();
        for(RouteStep routeStep : routeSegment.getSteps()) {
            steps.add(new JSONStep(routeStep));
        }

        if(request.hasAttributes()) {
            APIEnums.Attributes[] attributes = request.getAttributes();
            for(APIEnums.Attributes attr : attributes) {
                switch(attr) {
                    case DETOUR_FACTOR:
                        detourFactor = routeSegment.getDetourFactor();
                        break;
                    case AVERAGE_SPEED:
                        double distFactor = (!request.hasUnits() || request.getUnits() == APIEnums.Units.METRES) ? 1000 : 1;
                        averageSpeed = FormatUtility.roundToDecimals(routeSegment.getDistance() / distFactor / (routeSegment.getDuration() /3600), 2);
                        break;
                    case ROUTE_PERCENTAGE:
                        percentage = FormatUtility.roundToDecimals(routeSegment.getDistance() * 100 / routeLength, 2);
                        break;
                }
            }
        }
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getDetourFactor() {
        return detourFactor;
    }

    public Double getAscent() {
        return ascent;
    }

    public Double getDescent() {
        return descent;
    }

    public List<JSONStep> getSteps() {
        return steps;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }
}
