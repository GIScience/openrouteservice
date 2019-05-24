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

package heigit.ors.api.requests.matrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.services.matrix.MatrixServiceSettings;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "MatrixRequest", description = "The JSON body request sent to the matrix service which defines options and parameters regarding the matrix to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatrixRequest {
    public static final String PARAM_ID = "id";
    public static final String PARAM_PROFILE = "profile";
    public static final String PARAM_LOCATIONS = "locations";
    public static final String PARAM_SOURCES = "sources";
    public static final String PARAM_DESTINATIONS = "destinations";
    public static final String PARAM_METRICS = "metrics";
    public static final String PARAM_RESOLVE_LOCATIONS = "resolve_locations";
    public static final String PARAM_UNITS = "units";
    public static final String PARAM_OPTIMIZED = "optimized";

    @ApiModelProperty(name = "PARAM_ID", value = "Arbitrary identification string of the request reflected in the meta information.",
            example = "matrix_request")
    @JsonProperty(PARAM_ID)
    private String id;

    @ApiModelProperty(name = PARAM_LOCATIONS, value = "List of comma separated lists of `longitude,latitude` coordinates.",
            example = "[[9.70093, 48.477473], [9.207916, 49.153868], [37.573242, 55.801281], [115.663757, 38.106467]]",
            required = true)
    @JsonProperty(PARAM_LOCATIONS)
    private List<List<Double>> locations;

    @ApiModelProperty(name = PARAM_PROFILE, hidden = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = PARAM_SOURCES, value = "A list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default). example `[0,3]` for the first and fourth locations " +
            "CUSTOM_KEYS:{'apiDefault':['all']}")
    @JsonProperty(value = PARAM_SOURCES)
    private String[] sources;
    @JsonIgnore
    private boolean hasSources = false;

    @ApiModelProperty(name = PARAM_DESTINATIONS, value = "A list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default). `[0,3]` for the first and fourth locations " +
            "CUSTOM_KEYS:{'apiDefault':['all']}")
    @JsonProperty(value = PARAM_DESTINATIONS)
    private String[] destinations;
    @JsonIgnore
    private boolean hasDestinations = false;

    @ApiModelProperty(name = PARAM_METRICS, value = "Specifies a list of returned metrics.\n" +
            "* `distance` - Returns distance matrix for specified points in defined `units`.\n* `duration` - Returns duration matrix for specified points in **seconds**. " +
            "CUSTOM_KEYS:{'apiDefault':'duration'}")
    @JsonProperty(value = PARAM_METRICS)
    private MatrixRequestEnums.Metrics[] metrics;
    @JsonIgnore
    private boolean hasMetrics = false;

    @ApiModelProperty(name = PARAM_RESOLVE_LOCATIONS, value = "Specifies whether given locations are resolved or not. If the parameter value set to `true`, every element in " +
            "`destinations` and `sources` will contain a `name` element that identifies the name of the closest street. Default is `false`. " +
            "CUSTOM_KEYS:{'apiDefault':false}")
    @JsonProperty(value = PARAM_RESOLVE_LOCATIONS)
    private Boolean resolveLocations;
    @JsonIgnore
    private boolean hasResolveLocations = false;

    @ApiModelProperty(name = PARAM_UNITS, value = "Specifies the distance unit.\n" +
            "Default: m. CUSTOM_KEYS:{'apiDefault':'m','validWhen':{'ref':'metrics','value':'distance'}`}")
    @JsonProperty(value = PARAM_UNITS)
    private APIEnums.Units units;
    @JsonIgnore
    private boolean hasUnits = false;

    @ApiModelProperty(name = PARAM_OPTIMIZED, value = "Specifies whether flexible mode is used or not.", hidden = true)
    @JsonProperty(value = PARAM_OPTIMIZED)
    private Boolean optimized;
    @JsonIgnore
    private boolean hasOptimized = false;

    @ApiModelProperty(hidden = true)
    private APIEnums.MatrixResponseType responseType;

    @JsonCreator
    public MatrixRequest(@JsonProperty(value = "locations", required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public MatrixRequest(Double[][] locations) throws ParameterValueException {
        if (locations.length < 2) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
        }
        if (locations.length > MatrixServiceSettings.getMaximumRoutes(false))
            throw new ParameterValueException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations");
        this.locations = new ArrayList<>();
        for (Double[] coordPair : locations) {
            if (coordPair.length != 2)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
            List<Double> coordPairList = new ArrayList<>();
            coordPairList.add(coordPair[0]);
            coordPairList.add(coordPair[1]);
            this.locations.add(coordPairList);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return this.id != null;
    }

    public APIEnums.Profile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.Profile profile) {
        this.profile = profile;
    }

    public List<List<Double>> getLocations() {
        return locations;
    }

    public void setLocations(List<List<Double>> locations) {
        this.locations = locations;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
        hasSources = true;
    }

    public boolean hasSources() {
        return hasSources;
    }

    public String[] getDestinations() {
        return destinations;
    }

    public void setDestinations(String[] destinations) {
        this.destinations = destinations;
        hasDestinations = true;
    }

    public boolean hasDestinations() {
        return hasDestinations;
    }

    public MatrixRequestEnums.Metrics[] getMetrics() {
        return metrics;
    }

    public void setMetrics(MatrixRequestEnums.Metrics[] metrics) {
        this.metrics = metrics;
        hasMetrics = true;
    }

    public boolean hasMetrics() {
        return hasMetrics;
    }

    public Boolean getResolveLocations() {
        return resolveLocations;
    }

    public void setResolveLocations(boolean resolveLocations) {
        this.resolveLocations = resolveLocations;
        hasResolveLocations = true;
    }

    public boolean hasResolveLocations() {
        return hasResolveLocations;
    }

    public APIEnums.Units getUnits() {
        return units;
    }

    public void setUnits(APIEnums.Units units) {
        this.units = units;
        hasUnits = true;
    }

    public boolean hasUnits() {
        return hasUnits;
    }

    public Boolean getOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
        hasOptimized = true;
    }

    public boolean hasOptimized() {
        return hasOptimized;
    }

    public APIEnums.MatrixResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.MatrixResponseType responseType) {
        this.responseType = responseType;
    }
}

