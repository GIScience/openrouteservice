package org.heigit.ors.util;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.ConditionalEdges;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.parameters.ProfileParameters;

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
    public static int getFlexibilityMode(int flexibleMode, RouteSearchParameters searchParams, int profileType) {
        if (searchParams.requiresDynamicPreprocessedWeights() || profileType == RoutingProfileType.WHEELCHAIR)
            flexibleMode = ProfileTools.KEY_FLEX_PREPROCESSED;

        if (searchParams.requiresFullyDynamicWeights())
            flexibleMode = ProfileTools.KEY_FLEX_FULLY;
        //If we have special weightings, we have to fall back to ALT with Beeline
        ProfileParameters profileParams = searchParams.getProfileParameters();
        if (profileParams != null && profileParams.hasWeightings())
            flexibleMode = ProfileTools.KEY_FLEX_FULLY;

        return flexibleMode;
    }

    public static boolean hasTimeDependentSpeed(RouteSearchParameters searchParams, RouteSearchContext searchCntx) {
        FlagEncoder flagEncoder = searchCntx.getEncoder();
        String key = EncodingManager.getKey(flagEncoder, ConditionalEdges.SPEED);
        return searchParams.isTimeDependent() && flagEncoder.hasEncodedValue(key);
    }
}
