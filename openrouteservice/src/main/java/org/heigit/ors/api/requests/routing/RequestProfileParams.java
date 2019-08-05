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

package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "Profile Parameters", parent = RouteRequestOptions.class, description = "Specifies additional routing parameters. For all profiles except `driving-car`.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RequestProfileParams {
    public static final String PARAM_WEIGHTINGS = "weightings";
    public static final String PARAM_RESTRICTIONS = "restrictions";

    @JsonProperty(PARAM_WEIGHTINGS)
    private RequestProfileParamsWeightings weightings;
    @JsonIgnore
    private boolean hasWeightings = false;

    @JsonProperty(PARAM_RESTRICTIONS)
    private RequestProfileParamsRestrictions restrictions;
    @JsonIgnore
    private boolean hasRestrictions = false;

    public RequestProfileParamsWeightings getWeightings() {
        return weightings;
    }

    public void setWeightings(RequestProfileParamsWeightings weightings) {
        this.weightings = weightings;
        hasWeightings = true;
    }

    public RequestProfileParamsRestrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(RequestProfileParamsRestrictions restrictions) {
        this.restrictions = restrictions;
        hasRestrictions = true;
    }

    public boolean hasWeightings() {
        return hasWeightings;
    }

    public boolean hasRestrictions() {
        return hasRestrictions;
    }
}
