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
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.common.RequestOptions;

@Schema(title = "Route Options", name = "routeOptions", description = "Advanced options for routing", subTypes = {RouteRequestAlternativeRoutes.class, RequestProfileParams.class, RouteRequestRoundTripOptions.class})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequestOptions extends RequestOptions {
    public static final String PARAM_ROUND_TRIP_OPTIONS = "round_trip";

    @Schema(name = PARAM_ROUND_TRIP_OPTIONS, description = "Options to be applied on round trip routes.",
            example = "{\"length\":10000,\"points\":5}")
    @JsonProperty(PARAM_ROUND_TRIP_OPTIONS)
    private RouteRequestRoundTripOptions roundTripOptions;
    @JsonIgnore
    private boolean hasRoundTripOptions = false;

    public RouteRequestRoundTripOptions getRoundTripOptions() {
        return roundTripOptions;
    }

    public void setRoundTripOptions(RouteRequestRoundTripOptions roundTripOptions) {
        this.roundTripOptions = roundTripOptions;
        hasRoundTripOptions = true;
    }

    public boolean hasRoundTripOptions() {
        return hasRoundTripOptions;
    }
}
