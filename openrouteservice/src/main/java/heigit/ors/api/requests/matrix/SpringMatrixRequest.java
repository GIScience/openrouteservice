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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.services.ServiceRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "MatrixRequest", description = "The JSON body request sent to the matrix service which defines options and parameters regarding the matrix to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SpringMatrixRequest {
    @ApiModelProperty(name = "id", value = "Arbitrary identification string of the request reflected in the meta information.")
    @JsonProperty("id")
    private String id;
    private boolean hasId = false;

    @ApiModelProperty(hidden = true)
    private APIEnums.MatrixProfile profile;

    @ApiModelProperty(name = "locations", value = "List of comma separated lists of `longitude,latitude` coordinates (note, without quotes around the coordinates, this is a displaying error of swagger). \\nexample : `\\\"locations\\\":[[9.70093,48.477473],[9.207916,49.153868],[37.573242,55.801281],[115.663757,38.106467]]")
    @JsonProperty("locations")
    private List<List<Double>> locations;

    @ApiModelProperty(name = "sources", value = "A comma separated list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default).\\n\\nExample: `0,3` for the first and fourth Location.\\n")
    @JsonProperty("sources")
    private String[] sources;

    @ApiModelProperty(name = "destinations", value = "A comma separated list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default).\\n\\nExample: `0,3` for the first and fourth Location.\\n      type: \\\"array\\\"\\n")
    @JsonProperty("destinations")
    private String[] destinations;

    @ApiModelProperty(name = "metrics", value = "Specifies a list of returned metrics separated with a pipe character (|).\\n* `distance` - Returns distance matrix for specified points in defined `units`.\\n* `duration` - Returns duration matrix for specified points in *seconds*.\\n")
    @JsonProperty("metrics")
    private int metrics = MatrixMetricsType.Duration;

    @ApiModelProperty(name = "resolve_locations", value = "Specifies whether given locations are resolved or not. If the parameter value set to `true`, every element in destinations and sources will contain `name` element that identifies the name of the closest street. Default is `false`")
    @JsonProperty("resolve_locations")
    private boolean resolveLocations = false;

    @ApiModelProperty(name = "units", value = "Specifies the distance unit.\n" +
            "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private APIEnums.Units units = APIEnums.Units.METRES;

    @ApiModelProperty(name = "flexible_mode", value = "Specifies weather flexible mode is used or not. Standard is `flexible_mode=false`")
    @JsonProperty("flexible_mode")
    private boolean flexibleMode = false;

    @ApiModelProperty(hidden = true)
    private APIEnums.MatrixResponseType responseType = APIEnums.MatrixResponseType.JSON;
    @ApiModelProperty(hidden = true)
    private String weightingMethod;
    @ApiModelProperty(hidden = true)
    private String algorithm;
    @ApiModelProperty(hidden = true)
    private int profileType = -1;

    public SpringMatrixRequest(Double[][] locations) throws ParameterValueException {
        if (locations.length < 2) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
        }
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
        this.hasId = true;
    }

    public boolean isHasId() {
        return hasId;
    }

    public void setHasId(boolean hasId) {
        this.hasId = hasId;
    }

    public APIEnums.MatrixProfile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.MatrixProfile profile) {
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

    public int getMetrics() {
        return metrics;
    }

    public void setMetrics(int metrics) {
        this.metrics = metrics;
    }

    public boolean isResolveLocations() {
        return resolveLocations;
    }

    public void setResolveLocations(boolean resolveLocations) {
        this.resolveLocations = resolveLocations;
    }

    public APIEnums.Units getUnits() {
        return units;
    }

    public void setUnits(APIEnums.Units units) {
        this.units = units;
    }

    public boolean isFlexibleMode() {
        return flexibleMode;
    }

    public void setFlexibleMode(boolean flexibleMode) {
        this.flexibleMode = flexibleMode;
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

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public boolean hasMetrics() {
        return false;
    }

    public boolean hasUnits() {
        return false;
    }

    public boolean hasValidSourceIndex() {
        return validateLocationsIndex(sources);
    }

    public boolean hasValidDestinationIndex() {
        return validateLocationsIndex(destinations);
    }

    private boolean validateLocationsIndex(String[] index) {
        int indexLength = index.length;
        if (indexLength == 0) return true;
        if (indexLength == 1 && "all".equalsIgnoreCase(index[0])) return true;
        for (String indexString : index) {
            int indexInt = 0;
            try {
                indexInt = Integer.parseInt(indexString);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (indexInt > locations.size())
                return false;
        }
        return false;
    }
}

