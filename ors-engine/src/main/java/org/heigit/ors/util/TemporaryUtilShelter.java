package org.heigit.ors.util;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.util.PMap;
import org.geotools.referencing.wkt.Preprocessor;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.*;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.parameters.ProfileParameters;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.heigit.ors.util.ProfileTools.*;
import static org.heigit.ors.util.ProfileTools.Flexibility.*;

/**
 * This class is only a temporary shelter for helper methods that lack appropriate places
 * during refactoring.
 * TODO: Once the refactorings are done, the methods should be moved into appropriate places
 *       and this class should be deleted.
 */
public class TemporaryUtilShelter {

    public static boolean supportWeightingMethod(int profileType) {
        return RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isPedestrian(profileType);
    }

    /**
     * Get the flexibility mode necessary for the searchParams.
     * Reults in usage of CH, Core or ALT/AStar
     *
     * @param flexibleMode initial flexibleMode
     * @param searchParams RouteSearchParameters
     * @param profileType  Necessary for HGV
     * @return flexibility as int
     */
    public static Flexibility getFlexibilityMode(Flexibility flexibleMode, RouteSearchParameters searchParams, int profileType) {

        if (searchParams.requiresDynamicWeights())
            return DYNAMIC_WEIGHTS;

        if (searchParams.requiresNonDecreasingWeights())
            return NON_DECREASING_WEIGHTS;

        if (searchParams.requiresPreprocessedWeights() || profileType == RoutingProfileType.WHEELCHAIR)
            return PREPROCESSED_WEIGHTS;

        //If we have special weightings, we have to fall back to ALT with Beeline
        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings())
            flexibleMode = DYNAMIC_WEIGHTS;

        return flexibleMode;
    }

    public static boolean hasTimeDependentSpeed(RouteSearchParameters searchParams, RouteSearchContext searchCntx) {
        FlagEncoder flagEncoder = searchCntx.getEncoder();
        String key = EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED);
        return searchParams.isTimeDependent() && flagEncoder.hasEncodedValue(key);
    }

    public static RouteSearchContext createSearchContext(RouteSearchParameters searchParams, RoutingProfile routingProfile) throws InternalServerException {
        PMap props = new PMap();

        int profileType = searchParams.getProfileType();
        String encoderName = RoutingProfileType.getEncoderName(profileType);

        if (FlagEncoderNames.UNKNOWN.equals(encoderName))
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "unknown vehicle profile.");

        if (!routingProfile.getGraphhopper().getEncodingManager().hasEncoder(encoderName)) {
            throw new IllegalArgumentException("Vehicle " + encoderName + " unsupported. " + "Supported are: "
                    + routingProfile.getGraphhopper().getEncodingManager());
        }

        FlagEncoder flagEncoder = routingProfile.getGraphhopper().getEncodingManager().getEncoder(encoderName);
        ProfileParameters profileParams = searchParams.getProfileParameters();

        // PARAMETERS FOR PathProcessorFactory

        props.putObject("routing_extra_info", searchParams.getExtraInfo());
        props.putObject("routing_suppress_warnings", searchParams.getSuppressWarnings());

        props.putObject("routing_profile_type", profileType);
        props.putObject("routing_profile_params", profileParams);

        /*
         * PARAMETERS FOR EdgeFilterFactory
         * ======================================================================================================
         */

        /* Avoid areas */
        if (searchParams.hasAvoidAreas()) {
            props.putObject("avoid_areas", searchParams.getAvoidAreas());
        }

        /* Heavy vehicle filter */
        if (profileType == RoutingProfileType.DRIVING_HGV) {
            props.putObject("edgefilter_hgv", searchParams.getVehicleType());
        }

        /* Wheelchair filter */
        else if (profileType == RoutingProfileType.WHEELCHAIR) {
            props.putObject("edgefilter_wheelchair", "true");
        }

        /* Avoid features */
        if (searchParams.hasAvoidFeatures()) {
            props.putObject("avoid_features", searchParams);
        }

        /* Avoid borders of some form */
        if ((searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries())
                && (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType))) {
            props.putObject("avoid_borders", searchParams);
            if (searchParams.hasAvoidCountries())
                props.putObject("avoid_countries", Arrays.toString(searchParams.getAvoidCountries()));
        }

        if (profileParams != null && profileParams.hasWeightings()) {
            props.putObject(KEY_CUSTOM_WEIGHTINGS, true);
            Iterator<ProfileWeighting> iterator = profileParams.getWeightings().getIterator();
            while (iterator.hasNext()) {
                ProfileWeighting weighting = iterator.next();
                if (!weighting.getParameters().isEmpty()) {
                    String name = ProfileWeighting.encodeName(weighting.getName());
                    for (Map.Entry<String, Object> kv : weighting.getParameters().toMap().entrySet())
                        props.putObject(name + kv.getKey(), kv.getValue());
                }
            }
        }

        String effectiveWeightingMethod = searchParams.getCustomModel() != null ? WeightingMethod.getName(WeightingMethod.CUSTOM) : WeightingMethod.getName(searchParams.getWeightingMethod());
        String localProfileName = makeProfileName(encoderName, effectiveWeightingMethod,
                Boolean.TRUE.equals(routingProfile.getProfileProperties().getBuild().getEncoderOptions().getTurnCosts()));
        String profileNameCH = makeProfileName(encoderName, WeightingMethod.getName(searchParams.getWeightingMethod()), false);
        RouteSearchContext searchCntx = new RouteSearchContext(routingProfile.getGraphhopper(), flagEncoder, localProfileName, profileNameCH);
        searchCntx.setProperties(props);

        return searchCntx;
    }
}
