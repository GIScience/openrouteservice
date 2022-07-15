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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.routing.RoutePtStop;
import org.heigit.ors.routing.RouteStep;
import org.heigit.ors.util.StringUtility;

import java.util.Arrays;

@ApiModel(value="JSONPtStop", description = "Stop of a public transport leg")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONPtStop {
    @ApiModelProperty(value = "The ID of the stop.", example = "de:08221:1138:0:O")
    @JsonProperty("stop_id")
    private final String stopId;
    @ApiModelProperty(value = "The name of the stop.", example = "Heidelberg, Alois-Link-Platz")
    @JsonProperty("name")
    private String name;

    public JSONPtStop(RoutePtStop stop) {
        stopId = stop.stopId;
        name = stop.stopName;

    }
}
