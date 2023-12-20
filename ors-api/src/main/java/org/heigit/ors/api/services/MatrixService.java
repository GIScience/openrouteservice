package org.heigit.ors.api.services;

import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.api.requests.matrix.MatrixRequestEnums;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.ServerLimitExceededException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.MatrixSearchParameters;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.heigit.ors.api.requests.matrix.MatrixRequest.isFlexibleMode;

@Service
public class MatrixService extends ApiService {

    @Autowired
    public MatrixService(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    public MatrixResult generateMatrixFromRequest(MatrixRequest matrixRequest) throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = this.convertMatrixRequest(matrixRequest);

        try {
            return RoutingProfileManager.getInstance().computeMatrix(coreRequest);
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(MatrixErrorCodes.UNKNOWN);
        }
    }

    public org.heigit.ors.matrix.MatrixRequest convertMatrixRequest(MatrixRequest matrixRequest) throws StatusCodeException {
        org.heigit.ors.matrix.MatrixRequest coreRequest = new org.heigit.ors.matrix.MatrixRequest(
                endpointsProperties.getMatrix().getMaximumSearchRadius(),
                endpointsProperties.getMatrix().getMaximumVisitedNodes(),
                endpointsProperties.getMatrix().getUTurnCost());

        int numberOfSources = matrixRequest.getSources() == null ? matrixRequest.getLocations().size() : matrixRequest.getSources().length;
        int numberODestinations = matrixRequest.getDestinations() == null ? matrixRequest.getLocations().size() : matrixRequest.getDestinations().length;
        Coordinate[] locations = convertLocations(matrixRequest.getLocations(), numberOfSources * numberODestinations, endpointsProperties);

        coreRequest.setProfileType(convertToMatrixProfileType(matrixRequest.getProfile()));

        if (matrixRequest.hasMetrics())
            coreRequest.setMetrics(convertMetrics(matrixRequest.getMetrics()));

        if (matrixRequest.hasDestinations())
            coreRequest.setDestinations(convertDestinations(matrixRequest.getDestinations(), locations));
        else {
            coreRequest.setDestinations(convertDestinations(new String[]{"all"}, locations));
        }
        if (matrixRequest.hasSources())
            coreRequest.setSources(convertSources(matrixRequest.getSources(), locations));
        else {
            coreRequest.setSources(convertSources(new String[]{"all"}, locations));
        }
        if (matrixRequest.hasId())
            coreRequest.setId(matrixRequest.getId());
        if (matrixRequest.hasOptimized())
            coreRequest.setFlexibleMode(!matrixRequest.getOptimized());
        if (matrixRequest.hasResolveLocations())
            coreRequest.setResolveLocations(matrixRequest.getResolveLocations());
        if (matrixRequest.hasUnits())
            coreRequest.setUnits(convertUnits(matrixRequest.getUnits()));

        MatrixSearchParameters params = new MatrixSearchParameters();
        if (matrixRequest.hasMatrixOptions())
            coreRequest.setFlexibleMode(processMatrixRequestOptions(matrixRequest, params));
        coreRequest.setSearchParameters(params);
        return coreRequest;
    }

    private boolean processMatrixRequestOptions(MatrixRequest matrixRequest, MatrixSearchParameters params) throws StatusCodeException {
        try {
            int profileType = convertRouteProfileType(matrixRequest.getProfile());
            params.setProfileType(profileType);
        } catch (Exception e) {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_PROFILE);
        }
        processRequestOptions(matrixRequest.getMatrixOptions(), params);

        if (matrixRequest.getMatrixOptions().hasDynamicSpeeds()) {
            params.setDynamicSpeeds(matrixRequest.getMatrixOptions().getDynamicSpeeds());
        }

        return isFlexibleMode(matrixRequest.getMatrixOptions());
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

    protected Coordinate[] convertLocations(List<List<Double>> locations, int numberOfRoutes, EndpointsProperties endpointsProperties) throws ParameterValueException, ServerLimitExceededException {
        if (locations == null || locations.size() < 2)
            throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, MatrixRequest.PARAM_LOCATIONS);
        int maximumNumberOfRoutes = endpointsProperties.getMatrix().getMaximumRoutes(false);
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
