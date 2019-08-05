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

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.ServerLimitExceededException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.util.DistanceUnitUtil;

import java.util.ArrayList;
import java.util.List;

public class MatrixRequestHandler {
    private MatrixRequestHandler() throws InternalServerException {
        throw new InternalServerException("MatrixRequestHandler should not be instantiated empty");
    }

    public static MatrixResult generateMatrixFromRequest(MatrixRequest request) throws StatusCodeException {
        heigit.ors.matrix.MatrixRequest coreRequest = convertMatrixRequest(request);

        try {
            return RoutingProfileManager.getInstance().computeMatrix(coreRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(MatrixErrorCodes.UNKNOWN);
        }
    }

    public static heigit.ors.matrix.MatrixRequest convertMatrixRequest(MatrixRequest request) throws StatusCodeException {
        heigit.ors.matrix.MatrixRequest coreRequest = new heigit.ors.matrix.MatrixRequest();

        int sources = request.getSources() == null ? request.getLocations().size() : request.getSources().length;
        int destinations = request.getDestinations() == null ? request.getLocations().size() : request.getDestinations().length;
        Coordinate[] locations = convertLocations(request.getLocations(), sources * destinations);

        coreRequest.setProfileType(convertToMatrixProfileType(request.getProfile()));

        if (request.hasMetrics())
            coreRequest.setMetrics(convertMetrics(request.getMetrics()));

        if (request.hasDestinations())
            coreRequest.setDestinations(convertDestinations(request.getDestinations(), locations));
        else {
            coreRequest.setDestinations(convertDestinations(new String[]{"all"}, locations));
        }
        if (request.hasSources())
            coreRequest.setSources(convertSources(request.getSources(), locations));
        else {
            coreRequest.setSources(convertSources(new String[]{"all"}, locations));
        }
        if (request.hasId())
            coreRequest.setId(request.getId());
        if (request.hasOptimized())
            coreRequest.setFlexibleMode(!request.getOptimized());
        if (request.hasResolveLocations())
            coreRequest.setResolveLocations(request.getResolveLocations());
        if (request.hasUnits())
            coreRequest.setUnits(convertUnits(request.getUnits()));

        return coreRequest;
    }

    public static int convertMetrics(MatrixRequestEnums.Metrics[] metrics) throws ParameterValueException {
        List<String> metricsAsStrings = new ArrayList<>();
        for (int i=0; i<metrics.length; i++) {
            metricsAsStrings.add(metrics[i].toString());
        }

        String concatMetrics = String.join("|", metricsAsStrings);

        int combined = MatrixMetricsType.getFromString(concatMetrics);

        if (combined == MatrixMetricsType.Unknown)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_METRICS);

        return combined;
    }

    protected static Coordinate[] convertLocations(List<List<Double>> locations, int numberOfRoutes) throws ParameterValueException, ServerLimitExceededException {
        if (locations == null || locations.size() < 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        if (numberOfRoutes > MatrixServiceSettings.getMaximumRoutes(false))
            throw new ServerLimitExceededException(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "Only a total of " + numberOfRoutes + " routes are allowed.");
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

    protected static Coordinate convertSingleLocationCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }

    protected static Coordinate[] convertSources(String[] sourcesIndex, Coordinate[] locations) throws ParameterValueException {
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

    protected static Coordinate[] convertDestinations(String[] destinationsIndex, Coordinate[] locations) throws ParameterValueException {
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

    protected static ArrayList<Coordinate> convertIndexToLocations(String[] index, Coordinate[] locations) {
        ArrayList<Coordinate> indexCoordinates = new ArrayList<>();
        for (String indexString : index) {
            int indexInteger = Integer.parseInt(indexString);
            indexCoordinates.add(locations[indexInteger]);
        }
        return indexCoordinates;
    }

    protected static DistanceUnit convertUnits(APIEnums.Units unitsIn) throws ParameterValueException {
        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);
        if (units == DistanceUnit.Unknown)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_UNITS, unitsIn.toString());
        return units;
    }

    protected static int convertToMatrixProfileType(APIEnums.Profile profile) throws ParameterValueException {
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
