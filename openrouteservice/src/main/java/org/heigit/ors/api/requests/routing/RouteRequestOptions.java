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
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.isochrones.IsochroneRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

@ApiModel(value = "Route Options", description = "Advanced options for routing", subTypes = {RouteRequest.class, IsochroneRequest.class})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RouteRequestOptions {
    public static final String PARAM_AVOID_FEATURES = "avoid_features";
    public static final String PARAM_AVOID_BORDERS = "avoid_borders";
    public static final String PARAM_AVOID_COUNTRIES = "avoid_countries";
    public static final String PARAM_VEHICLE_TYPE = "vehicle_type";
    public static final String PARAM_PROFILE_PARAMS = "profile_params";
    public static final String PARAM_AVOID_POLYGONS = "avoid_polygons";

    @ApiModelProperty(name = PARAM_AVOID_FEATURES, value = "List of features to avoid. " +
            "CUSTOM_KEYS:{'itemRestrictions':{'ref':'profile', 'itemsWhen':{'driving-*':['highways','tollways','ferries'],'cycling-*':['ferries','steps','fords'],'foot-*':['ferries','fords','steps'],'wheelchair':['ferries','steps']}}}")
    @JsonProperty(PARAM_AVOID_FEATURES)
    private APIEnums.AvoidFeatures[] avoidFeatures;
    @JsonIgnore
    private boolean hasAvoidFeatures = false;

    @ApiModelProperty(name = PARAM_AVOID_BORDERS, value = "`all` for no border crossing. `controlled` to cross open borders but avoid controlled ones. Only for `driving-*` profiles. " +
            "CUSTOM_KEYS:{'validWhen':{'ref':'profile','value':['driving-*']}}")
    @JsonProperty(PARAM_AVOID_BORDERS)
    private APIEnums.AvoidBorders avoidBorders;
    @JsonIgnore
    private boolean hasAvoidBorders = false;

    @ApiModelProperty(name = PARAM_AVOID_COUNTRIES, value = "List of countries to exclude from routing with `driving-*` profiles. Can be used together with `'avoid_borders': 'controlled'`. " +
            "`[ 11, 193 ]` would exclude Austria and Switzerland. List of countries and application examples can be found [here](https://github.com/GIScience/openrouteservice-docs#country-list). " +
            "Also, ISO standard country codes cna be used in place of the numerical ids, for example, DE or DEU for Germany. " +
            "CUSTOM_KEYS:{'validWhen':{'ref':'profile','value':['driving-*']}}",
            example = "[ 11, 193 ]")
    @JsonProperty(PARAM_AVOID_COUNTRIES)
    private String[] avoidCountries;
    @JsonIgnore
    private boolean hasAvoidCountries = false;

    @ApiModelProperty(name = PARAM_VEHICLE_TYPE, value = "(for profile=driving-hgv only): hgv,bus,agricultural,delivery,forestry and goods. It is needed for vehicle restrictions to work. " +
            "CUSTOM_KEYS:{'validWhen':{'ref':'profile','value':['driving-hgv']}}")
    @JsonProperty(value = PARAM_VEHICLE_TYPE)
    private APIEnums.VehicleType vehicleType;
    @JsonIgnore
    private boolean hasVehicleType = false;

    @ApiModelProperty(name = PARAM_PROFILE_PARAMS, value = " Specifies additional routing parameters.")
    @JsonProperty(PARAM_PROFILE_PARAMS)
    private RequestProfileParams profileParams;
    @JsonIgnore
    private boolean hasProfileParams = false;

    @ApiModelProperty(name = PARAM_AVOID_POLYGONS, value = "Comprises areas to be avoided for the route. Formatted in GeoJSON as either a Polygon or Multipolygon object.")
    @JsonProperty(PARAM_AVOID_POLYGONS)
    private JSONObject avoidPolygonFeatures;
    @JsonIgnore
    private boolean hasAvoidPolygonFeatures = false;

    public APIEnums.AvoidFeatures[] getAvoidFeatures() {
        return avoidFeatures;
    }

    public void setAvoidFeatures(APIEnums.AvoidFeatures[] avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
        hasAvoidFeatures = true;
    }

    public APIEnums.AvoidBorders getAvoidBorders() {
        return avoidBorders;
    }

    public void setAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
        this.avoidBorders = avoidBorders;
        hasAvoidBorders = true;
    }

    public String[] getAvoidCountries() {
        return avoidCountries;
    }

    public void setAvoidCountries(String[] avoidCountries) {
        this.avoidCountries = avoidCountries;
        hasAvoidCountries = true;
    }

    public APIEnums.VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(APIEnums.VehicleType vehicleType) {
        this.vehicleType = vehicleType;
        hasVehicleType = true;
    }

    public RequestProfileParams getProfileParams() {
        return profileParams;
    }

    public void setProfileParams(RequestProfileParams profileParams) {
        this.profileParams = profileParams;
        hasProfileParams = true;
    }

    public JSONObject getAvoidPolygonFeatures() {
        return avoidPolygonFeatures;
    }

    public void setAvoidPolygonFeatures(JSONObject avoidPolygonFeatures) {
        this.avoidPolygonFeatures = avoidPolygonFeatures;
        hasAvoidPolygonFeatures = true;
    }

    public boolean hasAvoidFeatures() {
        return hasAvoidFeatures;
    }

    public boolean hasAvoidBorders() {
        return hasAvoidBorders;
    }

    public boolean hasAvoidCountries() {
        return hasAvoidCountries;
    }

    public boolean hasVehicleType() {
        return hasVehicleType;
    }

    public boolean hasProfileParams() {
        return hasProfileParams;
    }

    public boolean hasAvoidPolygonFeatures() {
        return hasAvoidPolygonFeatures;
    }
}
