package org.heigit.ors.api.services;

import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.*;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.heigit.ors.snapping.SnappingRequest;
import org.heigit.ors.snapping.SnappingResult;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnappingService extends ApiService {

    @Autowired
    public SnappingService(EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public SnappingResult generateSnappingFromRequest(SnappingApiRequest snappingApiRequest) throws StatusCodeException {
        SnappingRequest snappingRequest = this.convertSnappingRequest(snappingApiRequest);
        validateAgainstConfig(snappingRequest);
        try {
            RoutingProfile rp = RoutingProfileManager.getInstance().getRoutingProfile(snappingRequest.getProfileName());
            if (rp == null)
                throw new InternalServerException(SnappingErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
            return snappingRequest.computeResult(rp);
        } catch (PointNotFoundException e) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, SnappingErrorCodes.POINT_NOT_FOUND, e.getMessage());
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, SnappingErrorCodes.UNKNOWN);
        }
    }

    private SnappingRequest convertSnappingRequest(SnappingApiRequest snappingApiRequest) throws StatusCodeException {
        int profileType = -1;
        try {
            profileType = convertRouteProfileType(snappingApiRequest.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_VALUE, SnappingApiRequest.PARAM_PROFILE);
        }

        SnappingRequest snappingRequest = new SnappingRequest(profileType,
                convertLocations(snappingApiRequest.getLocations()), snappingApiRequest.getMaximumSearchRadius());
        EndpointsProperties.EndpointSnapProperties snapProperties = endpointsProperties.getSnap();
        snappingRequest.setProfileName(snappingApiRequest.getProfileName());
        snappingRequest.setMaximumLocations(snapProperties.getMaximumLocations());
        if (snappingApiRequest.hasId())
            snappingRequest.setId(snappingApiRequest.getId());
        return snappingRequest;

    }

    private Coordinate[] convertLocations(List<List<Double>> locations) throws StatusCodeException {
        Coordinate[] coordinates = new Coordinate[locations.size()];
        int i = 0; // apparently stream().map() does not work with exceptions
        for (var location : locations) {
            coordinates[i] = convertLocation(location);
            i++;
        }
        return coordinates;
    }

    private static Coordinate convertLocation(List<Double> location) throws StatusCodeException {
        if (location.size() != 2) {
            throw new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_VALUE, SnappingApiRequest.PARAM_LOCATIONS);
        }
        return new Coordinate(location.get(0), location.get(1));
    }

    private void validateAgainstConfig(SnappingRequest snappingRequest) throws StatusCodeException {
        int numberOfLocations = snappingRequest.getLocations().length;
        int maximumLocations = snappingRequest.getMaximumLocations();
        if (numberOfLocations > maximumLocations)
            throw new ParameterOutOfRangeException(SnappingErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, SnappingApiRequest.PARAM_LOCATIONS, Integer.toString(numberOfLocations), Integer.toString(maximumLocations));
    }
}
