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
import org.locationtech.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.ServerLimitExceededException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.MatrixSearchParameters;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.config.MatrixServiceSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApiModel(value = "MatrixRequest", description = "The JSON body request sent to the matrix service which defines options and parameters regarding the matrix to generate.")
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

    @ApiModelProperty(name = PARAM_LOCATIONS, value = "List of comma separated lists of `longitude,latitude` coordinates in WGS 84 (EPSG:4326)",
            example = "[[9.70093, 48.477473], [9.207916, 49.153868], [37.573242, 55.801281], [115.663757, 38.106467]]",
            required = true)
    @JsonProperty(PARAM_LOCATIONS)
    private List<List<Double>> locations;

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
    private boolean resolveLocations;
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

    @ApiModelProperty(name = PARAM_OPTIONS,
            value = "For advanced options formatted as json object. For structure refer to the [these examples](https://GIScience.github.io/openrouteservice/documentation/routing-options/Examples.html).",
            example = "{\"avoid_borders\":\"controlled\"}",
            hidden = true)
    @JsonProperty(PARAM_OPTIONS)
    private MatrixRequestOptions matrixOptions;

    @ApiModelProperty(hidden = true)
    private APIEnums.MatrixResponseType responseType;

    @JsonCreator
    public MatrixRequest(@JsonProperty(value = "locations", required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public MatrixRequest(Double[][] locations) throws ParameterValueException {
        if (locations.length < 2) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, PARAM_LOCATIONS);
        }
        if (locations.length > MatrixServiceSettings.getMaximumRoutes(false))
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

    public MatrixResult generateMatrixFromRequest() throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = this.convertMatrixRequest();

        try {
            return RoutingProfileManager.getInstance().computeMatrix(coreRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(MatrixErrorCodes.UNKNOWN);
        }
    }

    public org.heigit.ors.matrix.MatrixRequest convertMatrixRequest() throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = new org.heigit.ors.matrix.MatrixRequest();

        int numberOfSources = sources == null ? locations.size() : sources.length;
        int numberODestinations = destinations == null ? locations.size() : destinations.length;
        Coordinate[] locations = convertLocations(this.locations, numberOfSources * numberODestinations);

        coreRequest.setProfileType(convertToMatrixProfileType(profile));

        if (this.hasMetrics())
            coreRequest.setMetrics(convertMetrics(metrics));

        if (this.hasDestinations())
            coreRequest.setDestinations(convertDestinations(destinations, locations));
        else {
            coreRequest.setDestinations(convertDestinations(new String[]{"all"}, locations));
        }
        if (this.hasSources())
            coreRequest.setSources(convertSources(sources, locations));
        else {
            coreRequest.setSources(convertSources(new String[]{"all"}, locations));
        }
        if (this.hasId())
            coreRequest.setId(id);
        if (this.hasOptimized())
            coreRequest.setFlexibleMode(!optimized);
        if (this.hasResolveLocations())
            coreRequest.setResolveLocations(resolveLocations);
        if (this.hasUnits())
            coreRequest.setUnits(convertUnits(units));

        MatrixSearchParameters params = new MatrixSearchParameters();
        if(this.hasMatrixOptions())
            coreRequest.setFlexibleMode(this.processMatrixRequestOptions( params));
        coreRequest.setSearchParameters(params);
        return coreRequest;
    }

    private boolean processMatrixRequestOptions(MatrixSearchParameters params) throws StatusCodeException {
        try {
            int profileType = convertRouteProfileType(profile);
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }
        processRequestOptions(matrixOptions, params);

        if (matrixOptions.hasDynamicSpeeds()) {
            params.setDynamicSpeeds(matrixOptions.getDynamicSpeeds());
        }

        return isFlexibleMode(matrixOptions);
    }
    public static boolean isFlexibleMode(MatrixRequestOptions opt){
        return  opt.hasAvoidBorders() || opt.hasAvoidPolygonFeatures() || opt.hasAvoidCountries() || opt.hasAvoidFeatures() || opt.hasDynamicSpeeds();
    }


    public int convertMetrics(MatrixRequestEnums.Metrics[] metrics) throws ParameterValueException {
        List<String> metricsAsStrings = new ArrayList<>();
        for (MatrixRequestEnums.Metrics metric : metrics) {
            metricsAsStrings.add(metric.toString());
        }

        String concatMetrics = String.join("|", metricsAsStrings);

        int combined = MatrixMetricsType.getFromString(concatMetrics);

        if (combined == MatrixMetricsType.UNKNOWN)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_METRICS);

        return combined;
    }

    protected Coordinate[] convertLocations(List<List<Double>> locations, int numberOfRoutes) throws ParameterValueException, ServerLimitExceededException {
        if (locations == null || locations.size() < 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        int maximumNumberOfRoutes = MatrixServiceSettings.getMaximumRoutes(false);
        if (numberOfRoutes > maximumNumberOfRoutes)
            throw new ServerLimitExceededException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "Only a total of " + maximumNumberOfRoutes + " routes are allowed.");
        ArrayList<Coordinate> locationCoordinates = new ArrayList<>();

        for (List<Double> coordinate : locations) {
            locationCoordinates.add(convertSingleLocationCoordinate(coordinate));
        }
        try {
            return locationCoordinates.toArray(new Coordinate[locations.size()]);
        } catch (NumberFormatException | ArrayStoreException | NullPointerException ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        }
    }

    protected Coordinate convertSingleLocationCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    protected Coordinate[] convertSources(String[] sourcesIndex, Coordinate[] locations) throws ParameterValueException {
        int length = sourcesIndex.length;
        if (length == 0) return locations;
        if (length == 1 && "all".equalsIgnoreCase(sourcesIndex[0])) return locations;
        try {
            ArrayList<Coordinate> indexCoordinateArray = convertIndexToLocations(sourcesIndex, locations);
            return indexCoordinateArray.toArray(new Coordinate[0]);
        } catch (Exception ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_SOURCES);
        }
    }

    protected Coordinate[] convertDestinations(String[] destinationsIndex, Coordinate[] locations) throws ParameterValueException {
        int length = destinationsIndex.length;
        if (length == 0) return locations;
        if (length == 1 && "all".equalsIgnoreCase(destinationsIndex[0])) return locations;
        try {
            ArrayList<Coordinate> indexCoordinateArray = convertIndexToLocations(destinationsIndex, locations);
            return indexCoordinateArray.toArray(new Coordinate[0]);
        } catch (Exception ex) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_DESTINATIONS);
        }
    }

    protected ArrayList<Coordinate> convertIndexToLocations(String[] index, Coordinate[] locations) {
        ArrayList<Coordinate> indexCoordinates = new ArrayList<>();
        for (String indexString : index) {
            int indexInteger = Integer.parseInt(indexString);
            indexCoordinates.add(locations[indexInteger]);
        }
        return indexCoordinates;
    }

    protected int convertToMatrixProfileType(APIEnums.Profile profile) throws ParameterValueException {
        try {
            int profileFromString = RoutingProfileType.getFromString(profile.toString());
            if (profileFromString == 0) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_PROFILE);
            }
            return profileFromString;
        } catch (Exception e) {
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_PROFILE);
        }
    }
}

