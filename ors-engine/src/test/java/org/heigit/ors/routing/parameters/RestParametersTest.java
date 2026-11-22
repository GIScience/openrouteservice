package org.heigit.ors.routing.parameters;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.util.EncodingManager;
import org.heigit.ors.exceptions.IncompatibleParameterException;
import org.heigit.ors.exceptions.MissingParameterException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.exceptions.UnknownParameterException;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.heigit.ors.routing.graphhopper.extensions.util.parsers.RestParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestParametersTest {
    private static final EncodedValueLookup WITH_REST_DATA = new EncodingManager.Builder().add(new RestParser()).build();
    private static final EncodedValueLookup WITHOUT_REST_DATA = new EncodingManager.Builder().build();

    @Test
    void requestWithoutProfileParametersIsValid() {
        assertDoesNotThrow(() -> RestParameters.validate(new RouteSearchParameters(), WITHOUT_REST_DATA));
    }

    @Test
    void profileParametersWithoutRestValuesAreValidForAnyProfile() {
        assertDoesNotThrow(() -> RestParameters.validate(searchParameters(new ProfileParameters()), WITHOUT_REST_DATA));
    }

    @Test
    void restThresholdAndPenaltyAreValidForProfileWithRestData() {
        ProfileParameters params = restParameters(500, 0.0);

        assertDoesNotThrow(() -> RestParameters.validate(searchParameters(params), WITH_REST_DATA));
    }

    @Test
    void restPenaltyRequiresRestThreshold() {
        ProfileParameters params = new ProfileParameters();
        params.setRestPenalty(2.0);

        StatusCodeException e = assertThrows(MissingParameterException.class, () -> RestParameters.validateValues(params));
        assertEquals(RoutingErrorCodes.MISSING_PARAMETER, e.getInternalCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -100})
    void restThresholdMustBePositive(int threshold) {
        ProfileParameters params = restParameters(threshold, null);

        StatusCodeException e = assertThrows(ParameterValueException.class, () -> RestParameters.validateValues(params));
        assertEquals(RoutingErrorCodes.INVALID_PARAMETER_VALUE, e.getInternalCode());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.5, Double.NaN, Double.POSITIVE_INFINITY})
    void restPenaltyMustBeFiniteAndNotNegative(double penalty) {
        ProfileParameters params = restParameters(500, penalty);

        StatusCodeException e = assertThrows(ParameterValueException.class, () -> RestParameters.validateValues(params));
        assertEquals(RoutingErrorCodes.INVALID_PARAMETER_VALUE, e.getInternalCode());
    }

    @Test
    void valuesAreCheckedForProfileParameterSubclasses() {
        WheelchairParameters params = new WheelchairParameters();
        params.setRestThreshold(0);

        assertThrows(ParameterValueException.class, () -> RestParameters.validate(searchParameters(params), WITH_REST_DATA));
    }

    @Test
    void restThresholdRequiresRestPointDataInTheGraph() {
        RouteSearchParameters searchParams = searchParameters(restParameters(500, null));

        StatusCodeException e = assertThrows(IncompatibleParameterException.class, () -> RestParameters.validate(searchParams, WITHOUT_REST_DATA));
        assertEquals(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, e.getInternalCode());
    }

    @Test
    void restThresholdIsIncompatibleWithRoundTrip() {
        RouteSearchParameters searchParams = searchParameters(restParameters(500, null));
        searchParams.setRoundTripLength(1000);

        StatusCodeException e = assertThrows(IncompatibleParameterException.class, () -> RestParameters.validate(searchParams, WITH_REST_DATA));
        assertEquals(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, e.getInternalCode());
    }

    @Test
    void restThresholdIsIncompatibleWithAlternativeRoutes() {
        RouteSearchParameters searchParams = searchParameters(restParameters(500, null));
        searchParams.setAlternativeRoutesCount(2);

        StatusCodeException e = assertThrows(IncompatibleParameterException.class, () -> RestParameters.validate(searchParams, WITH_REST_DATA));
        assertEquals(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, e.getInternalCode());
    }

    @Test
    void singleAlternativeRouteIsAnOrdinaryRoute() {
        RouteSearchParameters searchParams = searchParameters(restParameters(500, null));
        searchParams.setAlternativeRoutesCount(1);

        assertDoesNotThrow(() -> RestParameters.validate(searchParams, WITH_REST_DATA));
    }

    @Test
    void servicesWithoutRestPointsAcceptRequestsWithoutRestValues() {
        assertDoesNotThrow(() -> RestParameters.rejectRestParameters(null, IsochronesErrorCodes.UNKNOWN_PARAMETER));
        assertDoesNotThrow(() -> RestParameters.rejectRestParameters(new RouteSearchParameters(), IsochronesErrorCodes.UNKNOWN_PARAMETER));
        assertDoesNotThrow(() -> RestParameters.rejectRestParameters(searchParameters(new WheelchairParameters()), IsochronesErrorCodes.UNKNOWN_PARAMETER));
    }

    @Test
    void servicesWithoutRestPointsRejectRestThreshold() {
        RouteSearchParameters searchParams = searchParameters(restParameters(500, null));

        StatusCodeException e = assertThrows(UnknownParameterException.class, () -> RestParameters.rejectRestParameters(searchParams, IsochronesErrorCodes.UNKNOWN_PARAMETER));
        assertEquals(IsochronesErrorCodes.UNKNOWN_PARAMETER, e.getInternalCode());
        assertTrue(e.getMessage().contains(RestParameters.PARAM_REST_THRESHOLD));
    }

    @Test
    void servicesWithoutRestPointsRejectRestPenalty() {
        ProfileParameters params = new ProfileParameters();
        params.setRestPenalty(2.0);

        StatusCodeException e = assertThrows(UnknownParameterException.class, () -> RestParameters.rejectRestParameters(searchParameters(params), MatrixErrorCodes.UNKNOWN_PARAMETER));
        assertEquals(MatrixErrorCodes.UNKNOWN_PARAMETER, e.getInternalCode());
        assertTrue(e.getMessage().contains(RestParameters.PARAM_REST_PENALTY));
    }

    @Test
    void penaltyValidity() {
        assertTrue(RestParameters.isValidPenalty(0));
        assertTrue(RestParameters.isValidPenalty(10));
        assertFalse(RestParameters.isValidPenalty(-1));
        assertFalse(RestParameters.isValidPenalty(Double.NEGATIVE_INFINITY));
    }

    private static ProfileParameters restParameters(int threshold, Double penalty) {
        ProfileParameters params = new ProfileParameters();
        params.setRestThreshold(threshold);
        params.setRestPenalty(penalty);
        return params;
    }

    private static RouteSearchParameters searchParameters(ProfileParameters params) {
        RouteSearchParameters searchParams = new RouteSearchParameters();
        searchParams.setProfileName("foot-walking");
        searchParams.setProfileParams(params);
        return searchParams;
    }
}
