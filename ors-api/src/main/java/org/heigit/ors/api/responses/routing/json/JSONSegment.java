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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RouteSegment;
import org.heigit.ors.routing.RouteStep;
import org.heigit.ors.util.FormatUtility;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "List containing the segments and its correspoding steps which make up the route.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JSONSegment {
    @Schema(description = "Contains the distance of the segment in specified units.", example = "253")
    @JsonProperty("distance")
    @JsonInclude()
    private final Double distance;
    @Schema(description = "Contains the duration of the segment in seconds.", example = "37.7")
    @JsonProperty("duration")
    @JsonInclude()
    private final Double duration;
    @Schema(description = "List containing the specific steps the segment consists of.")
    @JsonProperty("steps")
    @JsonInclude()
    private final List<JSONStep> steps;
    @Schema(description = "Contains the deviation compared to a straight line that would have the factor `1`. Double the Distance would be a `2`.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "attributes"),
                    @ExtensionProperty(name = "valueContains", value = "detourfactor")}
            )}, example = "0.5")
    @JsonProperty("detourfactor")
    private Double detourFactor;
    @Schema(description = "Contains the proportion of the route in percent.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "attributes"),
                    @ExtensionProperty(name = "valueContains", value = "percentage")}
            )}, example = "43.2")
    @JsonProperty("percentage")
    private Double percentage;
    @Schema(description = "Contains the average speed of this segment in km/h.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "attributes"),
                    @ExtensionProperty(name = "valueContains", value = "avgspeed")}
            )}, example = "56.3")
    @JsonProperty("avgspeed")
    private Double averageSpeed;
    @Schema(description = " Contains ascent of this segment in metres.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "elevation"),
                    @ExtensionProperty(name = "value", value = "true", parseValue = true)}
            )}, example = "56.3")
    @JsonProperty("ascent")
    private Double ascent;
    @Schema(description = "Contains descent of this segment in metres.",
            example = "45.2")
    @JsonProperty("descent")
    private Double descent;

    public JSONSegment(RouteSegment routeSegment, RouteRequest request, double routeLength) {
        this.distance = routeSegment.getDistance();
        this.duration = routeSegment.getDuration();
        this.detourFactor = routeSegment.getDetourFactor();
        if (request.hasUseElevation() && request.getUseElevation()) {
            this.ascent = routeSegment.getAscent();
            this.descent = routeSegment.getDescent();
        }
        steps = new ArrayList<>();
        for (RouteStep routeStep : routeSegment.getSteps()) {
            steps.add(new JSONStep(routeStep));
        }

        if (request.hasAttributes()) {
            APIEnums.Attributes[] attributes = request.getAttributes();
            for (APIEnums.Attributes attr : attributes) {
                switch (attr) {
                    case DETOUR_FACTOR -> detourFactor = routeSegment.getDetourFactor();
                    case AVERAGE_SPEED -> {
                        double distFactor = (!request.hasUnits() || request.getUnits() == APIEnums.Units.METRES) ? 1000 : 1;
                        averageSpeed = FormatUtility.roundToDecimals(routeSegment.getDistance() / distFactor / (routeSegment.getDuration() / 3600), 2);
                    }
                    case ROUTE_PERCENTAGE ->
                            percentage = FormatUtility.roundToDecimals(routeSegment.getDistance() * 100 / routeLength, 2);
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
