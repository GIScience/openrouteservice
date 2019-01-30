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
    @ApiModelProperty(name = "avoid_features", value = "List of features to avoid." +
            "{highways={profile=[driving-*]}," +
            "tollways={profile=[driving-*]}," +
            "ferries={profile=[driving-*,bike-*,foot-*,wheelchair]}," +
            "tunnels={profile=[driving-*]}," +
            "pavedroads={profile=[driving-*,bike-*]}," +
            "unpavedroads={profile=[driving-*,bike-*]}," +
            "tracks={profile=[driving-*]}," +
            "fords={profile=[driving-*,bike-*,foot-*]}," +
            "steps={profile=[driving-*,bike-*,foot-*],wheelchair}}")
    @JsonProperty("avoid_features")
    private APIEnums.AvoidFeatures[] avoidFeatures;
    @JsonIgnore
    private boolean hasAvoidFeatures = false;

    @ApiModelProperty(name = "maximum_speed", value = "Specifies a maximum travel speed restriction in km/h.", example = "100")
    @JsonProperty("maximum_speed")
    private double maximumSpeed;
    @JsonIgnore
    private boolean hasMaximumSpeed = false;

    @ApiModelProperty(value = "\"all\" for no border crossing. \"controlled\" to cross open borders but avoid controlled ones. Only for driving-* profiles.[{profile=[car,hgv]}]")
    @JsonProperty("avoid_borders")
    private APIEnums.AvoidBorders avoidBorders;
    @JsonIgnore
    private boolean hasAvoidBorders = false;

    @ApiModelProperty(value = "List of countries to exclude from routing with driving-* profiles. Can be used together with \"avoid_borders\": \"controlled\". " +
            "[ 11, 193 ] would exclude Austria and Switzerland. List of countries and application examples can be found here.[{profile=[car,hgv]}]", example = "[ 11, 193 ]")
    @JsonProperty("avoid_countries")
    private String[] avoidCountries;
    @JsonIgnore
    private boolean hasAvoidCountries = false;

    @ApiModelProperty(value = "(for profile=driving-hgv only): hgv,bus,agricultural,delivery,forestry and goods. It is needed for vehicle restrictions to work.[{profile=[hgv]}]")
    @JsonProperty(value = "vehicle_type", defaultValue = "unknown")
    private APIEnums.VehicleType vehicleType = APIEnums.VehicleType.UNKNOWN;
    @JsonIgnore
    private boolean hasVehicleType = false;

    @ApiModelProperty(value = " Specifies additional routing parameters.")
    @JsonProperty("profile_params")
    private RequestProfileParams profileParams;
    @JsonIgnore
    private boolean hasProfileParams = false;

    @ApiModelProperty(name = "avoid_polygons", value = "Comprises areas to be avoided for the route. Formatted in GeoJSON as either a Polygon or Multipolygon object.")
    @JsonProperty("avoid_polygons")
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

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
        hasMaximumSpeed = true;
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

    public boolean hasMaximumSpeed() {
        return hasMaximumSpeed;
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
