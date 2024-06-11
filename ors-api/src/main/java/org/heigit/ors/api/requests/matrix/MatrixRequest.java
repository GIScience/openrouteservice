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

package org.heigit.ors.api.requests.matrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.api.APIEnums;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(name = "MatrixRequest", description = "The JSON body request sent to the matrix service which defines options and parameters regarding the matrix to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MatrixRequest extends APIRequest {
    public static final String PARAM_LOCATIONS = "locations";
    public static final String PARAM_SOURCES = "sources";
    public static final String PARAM_DESTINATIONS = "destinations";
    public static final String PARAM_METRICS = "metrics";
    public static final String PARAM_RESOLVE_LOCATIONS = "resolve_locations";
    public static final String PARAM_UNITS = "units";
    public static final String PARAM_OPTIMIZED = "optimized";
    public static final String PARAM_OPTIONS = "options";

    @Schema(name = PARAM_LOCATIONS, description = "List of comma separated lists of `longitude,latitude` coordinates in WGS 84 (EPSG:4326)",
            example = "[[9.70093, 48.477473], [9.207916, 49.153868], [37.573242, 55.801281], [115.663757, 38.106467]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(PARAM_LOCATIONS)
    private List<List<Double>> locations;

    @Schema(name = PARAM_SOURCES, description = "A list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default). example `[0,3]` for the first and fourth locations ",
            defaultValue = "[\"all\"]")
    @JsonProperty(value = PARAM_SOURCES)
    private String[] sources;
    @JsonIgnore
    private boolean hasSources = false;

    @Schema(name = PARAM_DESTINATIONS, description = "A list of indices that refers to the list of locations (starting with `0`). `{index_1},{index_2}[,{index_N} ...]` or `all` (default). `[0,3]` for the first and fourth locations ",
            defaultValue = "[\"all\"]")
    @JsonProperty(value = PARAM_DESTINATIONS)
    private String[] destinations;
    @JsonIgnore
    private boolean hasDestinations = false;

    @Schema(name = PARAM_METRICS, description = """
            Specifies a list of returned metrics.
            "* `distance` - Returns distance matrix for specified points in defined `units`.
            * `duration` - Returns duration matrix for specified points in **seconds**.
            """,
            defaultValue = "[\"duration\"]")
    @JsonProperty(value = PARAM_METRICS)
    private MatrixRequestEnums.Metrics[] metrics;
    @JsonIgnore
    private boolean hasMetrics = false;

    @Schema(name = PARAM_RESOLVE_LOCATIONS, description = """
            Specifies whether given locations are resolved or not. If the parameter value set to `true`, every element in \
            `destinations` and `sources` will contain a `name` element that identifies the name of the closest street. Default is `false`. \
            """,
            defaultValue = "false")
    @JsonProperty(value = PARAM_RESOLVE_LOCATIONS)
    private boolean resolveLocations;
    @JsonIgnore
    private boolean hasResolveLocations = false;

    @Schema(name = PARAM_UNITS, description = """
            Specifies the distance unit.
            Default: m.\
            """,
            extensions = {@Extension(name = "validWhen", properties = {
                    @ExtensionProperty(name = "ref", value = "metrics"),
                    @ExtensionProperty(name = "value", value = "distance")}
            )},
            defaultValue = "m")
    @JsonProperty(value = PARAM_UNITS)
    private APIEnums.Units units;
    @JsonIgnore
    private boolean hasUnits = false;

    @Schema(name = PARAM_OPTIMIZED, description = "Specifies whether flexible mode is used or not.", hidden = true)
    @JsonProperty(value = PARAM_OPTIMIZED)
    private Boolean optimized;
    @JsonIgnore
    private boolean hasOptimized = false;

    @Schema(name = PARAM_OPTIONS,
            description = "For advanced options formatted as json object. For structure refer to the [these examples](src/main/java/org/heigit/ors/api/requests/matrix/MatrixRequest.java).",
            example = "{\"avoid_borders\":\"controlled\"}",
            hidden = true)
    @JsonProperty(PARAM_OPTIONS)
    private MatrixRequestOptions matrixOptions;

    @Schema(hidden = true)
    private APIEnums.MatrixResponseType responseType;

    @JsonCreator
    public MatrixRequest(@JsonProperty(value = "locations", required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public MatrixRequest(Double[][] locations, EndpointsProperties endpointsProperties) throws ParameterValueException {
        if (locations.length < 2) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, PARAM_LOCATIONS);
        }
        if (locations.length > endpointsProperties.getMatrix().getMaximumRoutes(false))
            throw new ParameterValueException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, PARAM_LOCATIONS);
        this.locations = new ArrayList<>();
        for (Double[] coordPair : locations) {
            if (coordPair.length != 2)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, PARAM_LOCATIONS);
            List<Double> coordPairList = new ArrayList<>();
            coordPairList.add(coordPair[0]);
            coordPairList.add(coordPair[1]);
            this.locations.add(coordPairList);
        }
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

    @JsonIgnore
    public Set<String> getMetricsStrings() {
        Set<String> ret = new HashSet<>();
        if (metrics != null) {
            for (MatrixRequestEnums.Metrics metric : metrics) {
                ret.add(metric.name());
            }
        }
        return ret;
    }

    public void setMetrics(MatrixRequestEnums.Metrics[] metrics) {
        this.metrics = metrics;
        hasMetrics = true;
    }

    public boolean hasMetrics() {
        return hasMetrics;
    }

    public boolean getResolveLocations() {
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

    public void setMatrixOptions(MatrixRequestOptions matrixOptions) {
        this.matrixOptions = matrixOptions;
    }

    public MatrixRequestOptions getMatrixOptions() {
        return matrixOptions;
    }

    public boolean hasMatrixOptions() {
        return matrixOptions != null;
    }

    public APIEnums.MatrixResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.MatrixResponseType responseType) {
        this.responseType = responseType;
    }

    public static boolean isFlexibleMode(MatrixRequestOptions opt) {
        return opt.hasAvoidBorders() || opt.hasAvoidPolygonFeatures() || opt.hasAvoidCountries() || opt.hasAvoidFeatures() || opt.hasDynamicSpeeds();
    }

}

