package org.heigit.ors.api.services;

import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.matching.MatchingApiRequest;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matching.MatchingRequest;
import org.heigit.ors.matching.MatchingResult;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchingService extends ApiService {

    @Autowired
    public MatchingService(EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public MatchingResult generateSnappingFromRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        MatchingRequest matchingRequest = this.convertMatchingRequest(matchingApiRequest);
        validateAgainstConfig(matchingRequest);

        try {
            RoutingProfile rp = RoutingProfileManager.getInstance().getRoutingProfile(matchingRequest.getProfileName());
            if (rp == null)
                throw new InternalServerException(SnappingErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
            return matchingRequest.computeResult(rp);
        } catch (PointNotFoundException e) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, SnappingErrorCodes.POINT_NOT_FOUND, e.getMessage());
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, SnappingErrorCodes.UNKNOWN);
        }
    }

    private MatchingRequest convertMatchingRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        int profileType = -1;
        try {
            profileType = convertRouteProfileType(matchingApiRequest.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(SnappingErrorCodes.INVALID_PARAMETER_VALUE, SnappingApiRequest.PARAM_PROFILE);
        }

        MatchingRequest matchingRequest = new MatchingRequest(profileType);

        return matchingRequest;
    }

    private void validateAgainstConfig(MatchingRequest matchingRequest) throws StatusCodeException { }
}
