package org.heigit.ors.apitests.snapping;

import org.hamcrest.Matchers;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.*;
import static org.heigit.ors.snapping.SnappingErrorCodes.*;

@EndPointAnnotation(name = "snap")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    /**
     * This function creates a {@link JSONArray} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link JSONArray}
     * @return {@link JSONArray}
     */
    private static JSONArray fakeLocations(int maximumSize) {
        JSONArray overloadedLocations = new JSONArray();
        for (int i = 0; i < maximumSize; i++) {
            JSONArray location = new JSONArray();
            location.put(0.0);
            location.put(0.0);
            overloadedLocations.put(location);
        }
        return overloadedLocations;
    }

    public ParamsTest() {
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        coordsShort.put(coord2);
        addParameter("coordinates", coordsShort);
    }

    @Test
    void basicTest() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("coordinates"));
        body.put("maximum_search_radius", "300");
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("any { it.key == 'locations' }", is(true))
                .statusCode(200);

    }

    /**
     * Provides a stream of test arguments for testing the Snapping Endpoint with various scenarios.
     * <p>
     * The scenarios include:
     * 1. Single fake location to check exception handling for single locations.
     * 2. Ten fake locations to check exception handling for multiple locations.
     * 3. Broken fake location to check exception handling for invalid locations.
     * 4. Wrong profile to check exception handling for invalid profiles.
     * 5. Unknown parameter to check exception handling for unknown parameters.
     * <p>
     * Each test case is represented as an instance of the Arguments class, containing the following parameters:
     * - The routing profile type (String).
     * - The locations (JSONArray).
     * - Expected error code for the test case (SnappingErrorCodes).
     * - Expected HTTP status code for the test case (StatusCode).
     * - Body parameter for testing (JSONObject).
     *
     * @return A stream of Arguments instances for testing the Snapping Endpoint with different scenarios.
     */
    public static Stream<Arguments> snappingEndpointExceptionTestProvider() {
        // Create fake locations for testing
        JSONArray oneFakeLocation = fakeLocations(1);
        JSONArray tenFakeLocations = fakeLocations(10);

        // Create a broken fake location with invalid coordinates
        JSONArray brokenFakeLocation = new JSONArray();
        brokenFakeLocation.put(0.0);
        brokenFakeLocation.put(0.0);

        JSONArray invalidCoords = new JSONArray();
        JSONArray invalidCoord1 = new JSONArray();
        invalidCoord1.put(8.680916);
        invalidCoords.put(invalidCoord1);

        // Create correct test locations with valid coordinates
        JSONArray correctTestLocations = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        correctTestLocations.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        correctTestLocations.put(coord2);
        // Return a stream of test arguments
        return Stream.of(
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, "json", "driving-car", new JSONObject()),
                // Check exception for one fake location to ensure single locations are checked
                Arguments.of(POINT_NOT_FOUND, NOT_FOUND, "json", "driving-car", new JSONObject()
                        .put("locations", oneFakeLocation)),
                // Check exception for ten fake locations to ensure multiple locations are checked
                Arguments.of(POINT_NOT_FOUND, NOT_FOUND, "json", "driving-car", new JSONObject()
                        .put("locations", tenFakeLocations)),
                // Check exception for broken location to ensure invalid locations are checked
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("locations", brokenFakeLocation)),
                // Check exception for wrong profile to ensure invalid profiles are checked
                Arguments.of(INVALID_PARAMETER_VALUE, BAD_REQUEST, "json", "driving-foo", new JSONObject()
                        .put("locations", correctTestLocations)),
                // Check exception for unknown parameter to ensure unknown parameters are checked
                Arguments.of(UNKNOWN_PARAMETER, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("locations", correctTestLocations).put("unknown", "unknown")),
                // Check exception for invalid locations parameter (only one ccordinate)
                Arguments.of(INVALID_PARAMETER_VALUE, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("locations", invalidCoords).put("maximum_search_radius", "300")),
                // Check exception for invalid locations parameter (only one ccordinate)
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("locations", "noJsonArray").put("maximum_search_radius", "300")),
                // Check exception for invalid maximum_search_radius
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("locations", correctTestLocations).put("maximum_search_radius", "notANumber")),
                // Check exception for missing locations parameter
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, "json", "driving-car", new JSONObject()
                        .put("maximum_search_radius", "300")
                ));
    }

    /**
     * Parameterized test method for testing various exception scenarios in the Snapping Endpoint.
     *
     * @param expectedErrorCode  The expected error code for the test case (SnappingErrorCodes).
     * @param expectedStatusCode The expected HTTP status code for the test case (StatusCode).
     * @param profile            The routing profile type (String).
     * @param body               The request body (JSONObject).
     */
    @ParameterizedTest
    @MethodSource("snappingEndpointExceptionTestProvider")
    void testSnappingExceptions(int expectedErrorCode, int expectedStatusCode, String endPoint, String profile, JSONObject body) {

        given()
                .headers(jsonContent)
                .pathParam("profile", profile)
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/" + endPoint)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(expectedErrorCode))
                .statusCode(expectedStatusCode);
    }
}
