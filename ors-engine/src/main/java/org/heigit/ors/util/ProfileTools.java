package org.heigit.ors.util;

import com.graphhopper.util.PMap;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;

public class ProfileTools {
    public static final String VAL_RECOMMENDED = "recommended";
    private static final String KEY_WEIGHTING = "weighting";
    private static final String KEY_WEIGHTING_METHOD = "weighting_method";
    public static final String KEY_CH_DISABLE = "ch.disable";
    public static final String KEY_LM_DISABLE = "lm.disable";
    public static final String KEY_CORE_DISABLE = "core.disable";
    public static final String KEY_PREPARE_CORE_WEIGHTINGS = "prepare.core.weightings";
    public static final String KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS = "prepare.fastisochrone.weightings";
    public static final String KEY_METHODS_CH = "methods.ch";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_THREADS = "threads";
    public static final String KEY_WEIGHTINGS = "weightings";
    public static final String KEY_LMSETS = "lmsets";
    public static final String KEY_MAXCELLNODES = "maxcellnodes";
    public static final String KEY_METHODS_LM = "methods.lm";
    public static final String KEY_LANDMARKS = "landmarks";
    public static final String KEY_METHODS_CORE = "methods.core";
    public static final String KEY_ACTIVE_LANDMARKS = "active_landmarks";
    public static final String KEY_TOTAL_POP = "total_pop";
    public static final String KEY_TOTAL_AREA_KM = "total_area_km";
    public static final int KEY_FLEX_STATIC = 0;
    public static final int KEY_FLEX_PREPROCESSED = 1;
    public static final int KEY_FLEX_FULLY = 2;
    public static final String KEY_CUSTOM_WEIGHTINGS = "custom_weightings";
    public static final String VAL_SHORTEST = "shortest";
    public static final String VAL_FASTEST = "fastest";

    public static String makeProfileName(String vehicleName, String weightingName, boolean hasTurnCosts) {
        String profileName = vehicleName + "_" + weightingName;
        if (hasTurnCosts)
            profileName += "_with_turn_costs";
        return profileName;
    }

    /**
     * Set the weightingMethod for the request based on input weighting.
     *
     * @param map              Hints map for setting up the request
     * @param requestWeighting Originally requested weighting
     * @param profileType      Necessary for HGV
     */
    public static void setWeightingMethod(PMap map, int requestWeighting, int profileType, boolean hasTimeDependentSpeed) {
        //Defaults
        String weightingMethod = VAL_RECOMMENDED;

        if (requestWeighting == WeightingMethod.SHORTEST)
            weightingMethod = VAL_SHORTEST;

        //For a requested recommended weighting, use recommended for bike, walking and hgv. Use fastest for car.
        if (requestWeighting == WeightingMethod.RECOMMENDED || requestWeighting == WeightingMethod.FASTEST) {
            if (profileType == RoutingProfileType.DRIVING_CAR) {
                weightingMethod = VAL_FASTEST;
            }
            if (RoutingProfileType.isHeavyVehicle(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)) {
                weightingMethod = VAL_RECOMMENDED;
            }
        }

        map.putObject(KEY_WEIGHTING_METHOD, weightingMethod);

        if (hasTimeDependentSpeed)
            map.putObject(ORSParameters.Weighting.TIME_DEPENDENT_SPEED_OR_ACCESS, true);
    }

    /**
     * Set the weighting for the request based on input weighting.
     *
     * @param map              Hints map for setting up the request
     * @param requestWeighting Originally requested weighting
     * @param profileType      Necessary for HGV
     */
    public static void setWeighting(PMap map, int requestWeighting, int profileType, boolean hasTimeDependentSpeed) {
        //Defaults
        String weighting = VAL_RECOMMENDED;

        if (requestWeighting == WeightingMethod.SHORTEST)
            weighting = VAL_SHORTEST;

        //For a requested recommended weighting, use recommended for bike, walking and hgv. Use fastest for car.
        if (requestWeighting == WeightingMethod.RECOMMENDED || requestWeighting == WeightingMethod.FASTEST) {
            if (profileType == RoutingProfileType.DRIVING_CAR) {
                weighting = VAL_FASTEST;
            }
            if (RoutingProfileType.isHeavyVehicle(profileType) || RoutingProfileType.isCycling(profileType) || RoutingProfileType.isWalking(profileType)) {
                weighting = VAL_RECOMMENDED;
            }
        }

        map.putObject(KEY_WEIGHTING, weighting);

        if (hasTimeDependentSpeed)
            map.putObject(ORSParameters.Weighting.TIME_DEPENDENT_SPEED_OR_ACCESS, true);
    }
}
