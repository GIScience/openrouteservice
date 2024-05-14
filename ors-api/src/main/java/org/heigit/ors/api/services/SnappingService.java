package org.heigit.ors.api.services;

import com.graphhopper.GraphHopper;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.heigit.ors.snapping.SnappingRequest;
import org.heigit.ors.snapping.SnappingResult;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnappingService extends ApiService {

    public SnappingResult generateSnappingFromRequest(SnappingApiRequest snappingApiRequest) throws StatusCodeException {
        SnappingRequest snappingRequest = this.convertSnappingRequest(snappingApiRequest);

        try {
            RoutingProfile rp = RoutingProfileManager.getInstance().getProfileFromType(snappingRequest.getProfileType());
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

        if (snappingApiRequest.hasId())
            snappingRequest.setId(snappingApiRequest.getId());
        return snappingRequest;

    }

    private Coordinate[] convertLocations(List<List<Double>> locations) throws StatusCodeException {
        Coordinate[] coordinates = new Coordinate[locations.size()];
        int i = 0; // apparently stream().map() does not work with exceptions
        for (var location: locations) {
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

}
