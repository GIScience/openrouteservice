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
import org.heigit.ors.matching.MatchingErrorCodes;
import org.heigit.ors.matching.MatchingRequest;
import org.heigit.ors.matching.MatchingResult;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchingService extends ApiService {

    @Autowired
    public MatchingService(EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public MatchingResult generateMatchingFromRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        MatchingRequest matchingRequest = this.convertMatchingRequest(matchingApiRequest);
        validateAgainstConfig(matchingRequest);

        try {
            RoutingProfile rp = RoutingProfileManager.getInstance().getRoutingProfile(matchingRequest.getProfileName());
            if (rp == null)
                throw new InternalServerException(MatchingErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
            return matchingRequest.computeResult(rp);
        } catch (PointNotFoundException e) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, MatchingErrorCodes.POINT_NOT_FOUND, e.getMessage());
        } catch (StatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, MatchingErrorCodes.UNKNOWN);
        }
    }

    private MatchingRequest convertMatchingRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        int profileType = -1;
        try {
            profileType = convertRouteProfileType(matchingApiRequest.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_PROFILE);
        }

        MatchingRequest matchingRequest = new MatchingRequest(profileType);
        if (matchingApiRequest.getProfileName() == null || matchingApiRequest.getProfileName().isEmpty()) {
            throw new ParameterValueException(MatchingErrorCodes.MISSING_PARAMETER, MatchingApiRequest.PARAM_PROFILE);
        }
        matchingRequest.setProfileName(matchingApiRequest.getProfileName());

        if (matchingApiRequest.getKey() == null || matchingApiRequest.getKey().isEmpty()) {
            throw new ParameterValueException(MatchingErrorCodes.MISSING_PARAMETER, MatchingApiRequest.PARAM_KEY);
        }
        matchingRequest.setKey(matchingApiRequest.getKey());

        JSONObject features = matchingApiRequest.getFeatures();
        if (features == null || features.isEmpty()) {
            throw new ParameterValueException(MatchingErrorCodes.MISSING_PARAMETER, MatchingApiRequest.PARAM_FEATURES);
        }
        if (features.get("type") == null || !features.get("type").equals("FeatureCollection")) {
            throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_FEATURES, "invalid GeoJSON type");
        }
        List<Map<String, String>> properties = new ArrayList<>();
        for (Object feature : (Iterable<?>) features.get("features")) {
            Map featureObj = (Map) feature;
            if (featureObj.get("properties") != null) {
                properties.add((Map<String, String>) featureObj.get("properties"));
            }
        }
        matchingRequest.setProperties(properties);

        GeoJsonReader reader = new GeoJsonReader();
        try {
            Geometry geometry = reader.read(features.toJSONString());
            if (geometry == null || geometry.isEmpty()) {
                throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_FEATURES, "geometry is null or empty");
            }
            if (!geometry.isValid()) {
                throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_FEATURES, "invalid geometry");
            }
            if (geometry.getNumGeometries() != properties.size()) {
                throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_FEATURES, "mismatching number of features and geometries");
            }
            matchingRequest.setGeometry(geometry);
        } catch (Exception e) {
            throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_FEATURES, "invalid GeoJSON format", e.getMessage());
        }
        return matchingRequest;
    }

    private void validateAgainstConfig(MatchingRequest matchingRequest) throws StatusCodeException { }
}
