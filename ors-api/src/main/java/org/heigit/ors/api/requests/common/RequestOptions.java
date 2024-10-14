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

package org.heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.routing.RequestProfileParams;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RouteRequestParameterNames;
import org.json.simple.JSONObject;

@Schema(name = "Matrix Options", description = "Advanced options for matrix", subTypes = {MatrixRequest.class})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RequestOptions implements RouteRequestParameterNames {

    @Schema(name = PARAM_AVOID_FEATURES, description = "List of features to avoid. ",
            extensions = {@Extension(name = "itemRestrictions", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "itemsWhen", value = "{\"driving-*\":[\"highways\",\"tollways\",\"ferries\"],\"cycling-*\":[\"ferries\",\"steps\",\"fords\",\"junction\"],\"foot-*\":[\"ferries\",\"fords\",\"steps\"],\"wheelchair\":[\"ferries\",\"steps\"]}", parseValue = true)}
            )},
            example = "[\"highways\"]")
    @JsonProperty(PARAM_AVOID_FEATURES)
    private APIEnums.AvoidFeatures[] avoidFeatures;
    @JsonIgnore
    private boolean hasAvoidFeatures = false;

    @Schema(name = PARAM_AVOID_BORDERS, description = "`all` for no border crossing. `controlled` to cross open borders but avoid controlled ones. Only for `driving-*` profiles. ",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "driving-*")}
            )},
            example = "controlled")
    @JsonProperty(PARAM_AVOID_BORDERS)
    private APIEnums.AvoidBorders avoidBorders;
    @JsonIgnore
    private boolean hasAvoidBorders = false;

    @Schema(name = PARAM_AVOID_COUNTRIES, description = """
            List of countries to exclude from matrix with `driving-*` profiles. Can be used together with `'avoid_borders': 'controlled'`. \
            `[ 11, 193 ]` would exclude Austria and Switzerland. List of countries and application examples can be found [here](https://giscience.github.io/openrouteservice/technical-details/country-list). \
            Also, ISO standard country codes cna be used in place of the numerical ids, for example, DE or DEU for Germany. \
            """,
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "driving-*")}
            )},
            example = "[ 11, 193 ]")
    @JsonProperty(PARAM_AVOID_COUNTRIES)
    private String[] avoidCountries;
    @JsonIgnore
    private boolean hasAvoidCountries = false;

    @Schema(name = PARAM_VEHICLE_TYPE, description = "(for profile=driving-hgv only): hgv,bus,agricultural,delivery,forestry and goods. It is needed for vehicle restrictions to work. ",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "value", value = "driving-hgv")}
            )},
            defaultValue = "hgv")
    @JsonProperty(value = PARAM_VEHICLE_TYPE)
    private APIEnums.VehicleType vehicleType;
    @JsonIgnore
    private boolean hasVehicleType = false;

    @Schema(name = PARAM_PROFILE_PARAMS, description = " Specifies additional matrix parameters.",
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "profile"),
                    @ExtensionProperty(name = "valueNot", value = "driving-car")}
            )})
    @JsonProperty(PARAM_PROFILE_PARAMS)
    private RequestProfileParams profileParams;
    @JsonIgnore
    private boolean hasProfileParams = false;

    @Schema(name = PARAM_AVOID_POLYGONS, description = "Comprises areas to be avoided for the route. Formatted in GeoJSON as either a Polygon or Multipolygon object.")
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
