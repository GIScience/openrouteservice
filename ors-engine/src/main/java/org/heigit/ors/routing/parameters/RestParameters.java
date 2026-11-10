package org.heigit.ors.routing.parameters;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Rest;
import org.heigit.ors.exceptions.IncompatibleParameterException;
import org.heigit.ors.exceptions.MissingParameterException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteRequestParameterNames;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingErrorCodes;

/**
 * Parameters of rest point routing: walking more than {@code rest_threshold} metres since the last rest point costs
 * {@code rest_penalty} times the weight of the extra distance. The names double as GraphHopper hint keys.
 */
public final class RestParameters {
    public static final String PARAM_REST_THRESHOLD = "rest_threshold";
    public static final String PARAM_REST_PENALTY = "rest_penalty";
    public static final double DEFAULT_REST_PENALTY = 2.0;
    private static final String PARAM_PROFILE = "profile";

    private RestParameters() {
    }

    /**
     * Checks the values of the rest parameters, independent of the profile.
     */
    public static void validateValues(ProfileParameters params) throws StatusCodeException {
        if (params == null || !params.hasRestThreshold() && !params.hasRestPenalty())
            return;
        if (!params.hasRestThreshold())
            throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, PARAM_REST_THRESHOLD);
        if (params.getRestThreshold() <= 0)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, PARAM_REST_THRESHOLD,
                    String.valueOf(params.getRestThreshold()), "It must be a positive number of metres.");
        if (params.hasRestPenalty() && !isValidPenalty(params.getRestPenalty()))
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, PARAM_REST_PENALTY,
                    String.valueOf(params.getRestPenalty()), "It must be a finite number of zero or more.");
    }

    /**
     * Checks the rest parameters of a route request: their values, that the profile's graph has rest point data, and
     * that no option is set that the rest point routing does not support.
     */
    public static void validate(RouteSearchParameters searchParams, EncodedValueLookup encodedValues) throws StatusCodeException {
        ProfileParameters params = searchParams.getProfileParameters();
        validateValues(params);
        if (params == null || !params.hasRestThreshold())
            return;
        String threshold = String.valueOf(params.getRestThreshold());
        if (!encodedValues.hasEncodedValue(Rest.KEY))
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, PARAM_REST_THRESHOLD,
                    threshold, PARAM_PROFILE, searchParams.getProfileName());
        if (searchParams.getRoundTripLength() > 0)
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, PARAM_REST_THRESHOLD,
                    RouteRequestParameterNames.PARAM_ROUND_TRIP_OPTIONS);
        if (searchParams.getAlternativeRoutesCount() > 1)
            throw new IncompatibleParameterException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, PARAM_REST_THRESHOLD,
                    RouteRequestParameterNames.PARAM_ALTERNATIVE_ROUTES);
    }

    public static boolean isValidPenalty(double penalty) {
        return penalty >= 0 && Double.isFinite(penalty);
    }
}
