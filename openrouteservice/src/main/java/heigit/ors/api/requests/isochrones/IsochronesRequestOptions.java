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

package heigit.ors.api.requests.isochrones;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.simple.JSONObject;

@ApiModel(value = "Isochrones Options", description = "Advanced options for isochrones", parent = IsochronesRequest.class)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IsochronesRequestOptions {
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

    @ApiModelProperty(value = "\"all\" for no border crossing. \"controlled\" to cross open borders but avoid controlled ones. Only for driving-* profiles.[{profile=[car,hgv]}]")
    @JsonProperty("avoid_borders")
    private APIEnums.AvoidBorders avoidBorders;
    @JsonIgnore
    private boolean hasAvoidBorders = false;

    @ApiModelProperty(value = "List of countries to exclude from isochrones with driving-* profiles. Can be used together with \"avoid_borders\": \"controlled\". " +
            "[ 11, 193 ] would exclude Austria and Switzerland. List of countries and application examples can be found here.[{profile=[car,hgv]}]", example = "[ 11, 193 ]")
    @JsonProperty("avoid_countries")
    private int[] avoidCountries;
    @JsonIgnore
    private boolean hasAvoidCountries = false;

    @ApiModelProperty(name = "avoid_polygons", value = "Comprises areas to be avoided for the isochrones. Formatted in GeoJSON as either a Polygon or Multipolygon object.")
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

    public APIEnums.AvoidBorders getAvoidBorders() {
        return avoidBorders;
    }

    public void setAvoidBorders(APIEnums.AvoidBorders avoidBorders) {
        this.avoidBorders = avoidBorders;
        hasAvoidBorders = true;
    }

    public int[] getAvoidCountries() {
        return avoidCountries;
    }

    public void setAvoidCountries(int[] avoidCountries) {
        this.avoidCountries = avoidCountries;
        hasAvoidCountries = true;
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

    public boolean hasAvoidPolygonFeatures() {
        return hasAvoidPolygonFeatures;
    }
}
