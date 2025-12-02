package org.heigit.ors.api.services;

import org.heigit.ors.api.config.ApiEngineProperties;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.requests.matching.MatchingApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matching.MatchingErrorCodes;
import org.heigit.ors.matching.MatchingRequest;
import org.heigit.ors.routing.RoutingProfile;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MatchingService extends ApiService {

    @Autowired
    public MatchingService(EngineService engineService, EndpointsProperties endpointsProperties, ApiEngineProperties apiEngineProperties) {
        super(engineService);
        this.endpointsProperties = endpointsProperties;
        this.apiEngineProperties = apiEngineProperties;
    }

    public MatchingInfo generateMatchingInformation(String profileName) throws StatusCodeException {
        RoutingProfile rp = engineService.waitForActiveRoutingProfileManager().getRoutingProfile(profileName);
        if (rp == null) {
            throw new InternalServerException(MatchingErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
        }
        String graphTimestamp = rp.getGraphhopper().getGraphHopperStorage().getProperties().get("datareader.import.date");
        return new MatchingInfo(graphTimestamp);
    }

    public MatchingRequest.MatchingResult generateMatchingFromRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        MatchingRequest matchingRequest = this.convertMatchingRequest(matchingApiRequest);
        try {
            RoutingProfile rp = engineService.waitForActiveRoutingProfileManager().getRoutingProfile(matchingRequest.getProfileName());
            if (rp == null) {
                throw new InternalServerException(MatchingErrorCodes.UNKNOWN, "Unable to find an appropriate routing profile.");
            }
            return matchingRequest.computeResult(rp);
        } catch (PointNotFoundException e) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, MatchingErrorCodes.POINT_NOT_FOUND, e.getMessage());
        } catch (StatusCodeException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new StatusCodeException(StatusCode.NOT_FOUND, MatchingErrorCodes.LINE_NOT_MATCHED, e.getMessage());
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.INTERNAL_SERVER_ERROR, MatchingErrorCodes.UNKNOWN);
        }
    }

    private MatchingRequest convertMatchingRequest(MatchingApiRequest matchingApiRequest) throws StatusCodeException {
        int profileType;
        try {
            profileType = convertRouteProfileType(matchingApiRequest.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(MatchingErrorCodes.INVALID_PARAMETER_VALUE, MatchingApiRequest.PARAM_PROFILE);
        }

        MatchingRequest matchingRequest = new MatchingRequest(profileType, endpointsProperties.getMatch().getMaximumSearchRadius());
        if (matchingApiRequest.getProfileName() == null || matchingApiRequest.getProfileName().isEmpty()) {
            throw new ParameterValueException(MatchingErrorCodes.MISSING_PARAMETER, MatchingApiRequest.PARAM_PROFILE);
        }
        matchingRequest.setProfileName(matchingApiRequest.getProfileName());

        JSONObject features = matchingApiRequest.getFeatures();
        if (features == null || features.isEmpty()) {
            throw new ParameterValueException(MatchingErrorCodes.MISSING_PARAMETER, MatchingApiRequest.PARAM_FEATURES);
        }
        if (features.get("type") == null || !features.get("type").equals("FeatureCollection")) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_FORMAT, "invalid GeoJSON type");
        }

        GeoJsonReader reader = new GeoJsonReader();
        try {
            if (!(features.get("features") instanceof List<?> featuresList)) {
                throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_FORMAT, "invalid GeoJSON format: 'features' is not a list");
            }
            List<Geometry> geometries = new ArrayList<>();
            for (Object featureObj : featuresList) {
                if (!(featureObj instanceof Map<?, ?> featureMap)) {
                    throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_FORMAT, "invalid GeoJSON format: 'feature' is not a map");
                }
                Map<String, Object> feature = (Map<String, Object>) featureMap;
                Geometry geometry = reader.read(JSONValue.toJSONString(feature));
                if (geometry == null) {
                    throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_VALUE, "geometry is null");
                } else if (geometry.isEmpty()) {
                    throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_VALUE, "geometry is empty");
                }
                geometry.setUserData(feature.get("properties"));
                geometries.add(geometry);
            }
            matchingRequest.setGeometry(new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[0])));
        } catch (Exception e) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatchingErrorCodes.INVALID_PARAMETER_FORMAT, "invalid GeoJSON format: %s".formatted(e.getMessage()));
        }
        return matchingRequest;
    }

    public record MatchingInfo(String graphTimestamp) {
    }
}
