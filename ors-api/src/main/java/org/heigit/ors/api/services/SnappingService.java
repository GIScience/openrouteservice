package org.heigit.ors.api.services;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.heigit.ors.api.requests.snapping.SnappingApiRequest;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.PointNotFoundException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.matrix.MatrixSearchContext;
import org.heigit.ors.matrix.MatrixSearchContextBuilder;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.heigit.ors.snapping.SnappingErrorCodes;
import org.heigit.ors.snapping.SnappingRequest;
import org.heigit.ors.snapping.SnappingResult;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnappingService extends ApiService {

    public SnappingResult generateSnappingFromRequest(SnappingApiRequest snappingApiRequest) throws StatusCodeException {
        SnappingRequest snappingRequest = this.convertSnappingRequest(snappingApiRequest);

        try {
            RoutingProfileManager rpm = RoutingProfileManager.getInstance();
            RoutingProfile rp = rpm.getProfiles().getRouteProfile(snappingRequest.getProfileType());
            GraphHopper gh = rp.getGraphhopper();
            return computeResult(snappingRequest, gh);
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

    public SnappingResult computeResult(SnappingRequest snappingRequest, GraphHopper gh) throws Exception {
        String encoderName = RoutingProfileType.getEncoderName(snappingRequest.getProfileType());
        FlagEncoder flagEncoder = gh.getEncodingManager().getEncoder(encoderName);
        PMap hintsMap = new PMap();
        int weightingMethod = WeightingMethod.RECOMMENDED; // Only needed to create the profile string
        ProfileTools.setWeightingMethod(hintsMap, weightingMethod, snappingRequest.getProfileType(), false);
        ProfileTools.setWeighting(hintsMap, weightingMethod, snappingRequest.getProfileType(), false);
        String profileName = ProfileTools.makeProfileName(encoderName, hintsMap.getString("weighting", ""), false);
        GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
        String graphDate = ghStorage.getProperties().get("datareader.import.date");

        // TODO: replace usage of matrix search context by snapping-specific class
        MatrixSearchContextBuilder builder = new MatrixSearchContextBuilder(ghStorage, gh.getLocationIndex(), AccessFilter.allEdges(flagEncoder.getAccessEnc()), true);
        Weighting weighting = new ORSWeightingFactory(ghStorage, gh.getEncodingManager()).createWeighting(gh.getProfile(profileName), hintsMap, false);
        MatrixSearchContext mtxSearchCntx = builder.create(ghStorage.getBaseGraph(), null, weighting, profileName, snappingRequest.getLocations(), snappingRequest.getLocations(), snappingRequest.getMaximumSearchRadius());
        return new SnappingResult(mtxSearchCntx.getSources().getLocations(), graphDate);
    }
}
