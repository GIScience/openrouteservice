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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MatrixRequest {
    @ApiModelProperty(name = "id", value = "Arbitrary identification string of the request reflected in the meta information.")
    private String id;

    @ApiModelProperty(name = "locations", value = "List of comma separated lists of `longitude,latitude` coordinates (note, without quotes around the coordinates, this is a displaying error of swagger). \\nexample : `\\\"locations\\\":[[9.70093,48.477473],[9.207916,49.153868],[37.573242,55.801281],[115.663757,38.106467]]")
    @JsonProperty("locations")
    private List<List<Double>> locations;

    @ApiModelProperty(hidden = true)
    private APIEnums.Profile profile;

    @ApiModelProperty(name = "sources", value = "A comma separated list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default).\\n\\nExample: `0,3` for the first and fourth Location.\\n")
    @JsonProperty(value = "sources", defaultValue = "all")
    private String[] sources = {"all"};

    @ApiModelProperty(name = "destinations", value = "A comma separated list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default).\\n\\nExample: `0,3` for the first and fourth Location.\\n      type: \\\"array\\\"\\n")
    @JsonProperty(value = "destinations", defaultValue = "all")
    private String[] destinations = {"all"};

    @ApiModelProperty(name = "metrics", value = "Specifies a list of returned metrics separated with a (,) character.\\n* `distance` - Returns distance matrix for specified points in defined `units`.\\n* `duration` - Returns duration matrix for specified points in *seconds*.\\n")
    @JsonProperty(value = "metrics", defaultValue = "duration")
    private String[] metrics = {"duration"};

    @ApiModelProperty(name = "resolve_locations", value = "Specifies whether given locations are resolved or not. If the parameter value set to `true`, every element in destinations and sources will contain `name` element that identifies the name of the closest street. Default is `false`")
    @JsonProperty(value = "resolve_locations", defaultValue = "false")
    private Boolean resolveLocations = false;

    @ApiModelProperty(name = "units", value = "Specifies the distance unit.\n" +
            "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private String units = "m";

    @ApiModelProperty(name = "optimized", value = "Specifies whether flexible mode is used or not.", hidden = true)
    @JsonProperty(value = "optimized")
    private boolean optimized = false;

    @ApiModelProperty(hidden = true)
    private APIEnums.MatrixResponseType responseType;
    @ApiModelProperty(hidden = true)
    private String weightingMethod;
    @ApiModelProperty(hidden = true)
    private String algorithm;

    @JsonCreator
    public MatrixRequest(@JsonProperty(value = "locations", required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public MatrixRequest(Double[][] locations) throws ParameterValueException {
        if (locations.length < 2) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
        }
        if (locations.length > MatrixServiceSettings.getMaximumLocations(false))
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

    public MatrixRequest() {
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
    }

    public String[] getDestinations() {
        return destinations;
    }

    public void setDestinations(String[] destinations) {
        this.destinations = destinations;
    }

    public String[] getMetrics() {
        return metrics;
    }

    public void setMetrics(String[] metrics) {
        this.metrics = metrics;
    }

    public boolean isResolveLocations() {
        return resolveLocations;
    }

    public void setResolve_Locations(boolean resolveLocations) {
        this.resolveLocations = resolveLocations;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public boolean isOptimized() {
        return optimized;
    }

    public void setOptimized(boolean optimized) {
        this.optimized = optimized;
    }

    public APIEnums.MatrixResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.MatrixResponseType responseType) {
        this.responseType = responseType;
    }

    public String getWeightingMethod() {
        return weightingMethod;
    }

    public void setWeightingMethod(String weightingMethod) {
        this.weightingMethod = weightingMethod;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean hasValidSourceIndex() {
        return validateLocationsIndex(sources);
    }

    public boolean hasValidDestinationIndex() {
        return validateLocationsIndex(destinations);
    }

    private boolean validateLocationsIndex(String[] index) {
        int indexLength;
        try {
            indexLength = index.length;
        } catch (NullPointerException ne) {
            return false;
        }
        if (indexLength == 1 && "all".equalsIgnoreCase(index[0])) return true;
        for (String indexString : index) {
            int indexInt;
            try {
                indexInt = Integer.parseInt(indexString);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (indexInt > locations.size())
                return false;
        }
        return true;
    }
}

