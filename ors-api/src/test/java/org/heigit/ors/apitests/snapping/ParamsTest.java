package org.heigit.ors.apitests.snapping;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.common.StatusCode.NOT_FOUND;
import static org.heigit.ors.snapping.SnappingErrorCodes.*;
import static org.junit.jupiter.api.Assertions.*;

@EndPointAnnotation(name = "snap")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    /**
     * Generates a JSONArray with fake locations for testing purposes.
     *
     * @param maximumSize The maximum size of the JSONArray.
     * @return A JSONArray containing fake locations with the specified size.
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

    /**
     * Generates a JSONArray with valid locations for testing purposes.
     *
     * @return A JSONArray containing valid coordinates for testing.
     */
    public static JSONArray validLocations() {
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.4246);
        coordsShort.put(coord2);
        return coordsShort;
    }

    /**
     * Provides a stream of test arguments for testing successful scenarios in the Snapping Endpoint.
     * <p>
     * Each test case is represented as an instance of the Arguments class, containing the following parameters:
     * - The request body (JSONObject).
     * - Boolean flag indicating whether an empty result is expected.
     * - Boolean flag indicating whether a partially empty result is expected.
     * - The endpoint type (String).
     * - The routing profile type (String).
     *
     * @return A stream of Arguments instances for testing successful scenarios in the Snapping Endpoint.
     */
    public static Stream<Arguments> snappingEndpointSuccessTestProvider() {
        return Stream.of(
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "-1"), true, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "0"), true, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "1"), true, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "10"), false, true, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "300"), false, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "400"), false, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "1000"), false, false, "json", "driving-hgv"),
                Arguments.of(new JSONObject().put("locations", validLocations()).put("maximum_search_radius", "1000"), false, false, null, "driving-hgv")
        );
    }

    /**
     * Parameterized test method for testing various scenarios in the Snapping Endpoint.
     *
     * @param body                 The request body (JSONObject).
     * @param emptyResult          Boolean flag indicating whether an empty result is expected.
     * @param partiallyEmptyResult Boolean flag indicating whether a partially empty result is expected.
     * @param endPoint             The endpoint type (String).
     * @param profile              The routing profile type (String).
     */
    @ParameterizedTest
    @MethodSource("snappingEndpointSuccessTestProvider")
    void testSnappingSuccess(JSONObject body, Boolean emptyResult, Boolean partiallyEmptyResult, String endPoint, String profile) {

        RequestSpecification requestSpecification = given()
                .headers(jsonContent);

        if (profile != null)
            requestSpecification = requestSpecification.pathParam("profile", profile);

        String url = getEndPointPath();
        if (StringUtils.isNotBlank(profile))
            url = url + "/{profile}";

        if (StringUtils.isNotBlank(endPoint))
            url = url + "/" + endPoint;

        ValidatableResponse result = requestSpecification
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(url)
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        // Check if the response contains the expected keys
        result.body("any { it.key == 'locations' }", is(true));
        result.body("any { it.key == 'metadata' }", is(true));
        result.body("metadata.containsKey('attribution')", is(true));
        result.body("metadata.service", is("snap"));
        result.body("metadata.containsKey('timestamp')", is(true));
        result.body("metadata.containsKey('query')", is(true));
        result.body("metadata.containsKey('engine')", is(true));
        result.body("metadata.containsKey('system_message')", is(true));
        result.body("metadata.query.locations.size()", is(2));
        result.body("metadata.query.locations[0].size()", is(2));
        result.body("metadata.query.locations[1].size()", is(2));
        result.body("metadata.query.profile", is(profile));

        if (body.get("maximum_search_radius") != "0")
            result.body("metadata.query.maximum_search_radius", is(Float.parseFloat(body.get("maximum_search_radius").toString())));

        if (StringUtils.isNotBlank(endPoint))
            result.body("metadata.query.format", is(endPoint));


        boolean foundValidLocation = false;
        boolean foundInvalidLocation = false;

        // Iterate over the locations array and check the types of the values
        ArrayList<Integer> locations = result.extract().jsonPath().get("locations");
        for (int i = 0; i < locations.size(); i++) {
            // if empty result is expected, check if the locations array is empty
            if (emptyResult) {
                assertNull(result.extract().jsonPath().get("locations[" + i + "].location[0]"));
                foundValidLocation = true;
                foundInvalidLocation = true;
            } else if (partiallyEmptyResult && !foundInvalidLocation && result.extract().jsonPath().get("locations[" + i + "]") == null) {
                foundInvalidLocation = true;
            } else {
                // Type expectations
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].location[0]").getClass());
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].location[1]").getClass());
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].snapped_distance").getClass());
                // If name is in the response, check the type
                if (result.extract().jsonPath().get("locations[" + i + "].name") != null)
                    assertEquals(String.class, result.extract().jsonPath().get("locations[" + i + "].name").getClass());
                foundValidLocation = true;
            }
        }

        assertTrue(foundValidLocation);
        if (partiallyEmptyResult)
            assertTrue(foundInvalidLocation);
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
                //Check exception for missing profile and return type
                Arguments.of(MISSING_PARAMETER, BAD_REQUEST, null, null, new JSONObject()
                        .put("locations", correctTestLocations).put("maximum_search_radius", "300")),
                //Check exception for missing profile - "json" is interpreted as profile
                Arguments.of(INVALID_PARAMETER_VALUE, BAD_REQUEST, "json", null, new JSONObject()
                        .put("locations", correctTestLocations).put("maximum_search_radius", "300")),
                //Check exception for missing profile - "json" is interpreted as profile
                Arguments.of(UNSUPPORTED_EXPORT_FORMAT, SC_NOT_ACCEPTABLE, "badExportFormat", "driving-car", new JSONObject()
                        .put("locations", correctTestLocations).put("maximum_search_radius", "300")),
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

        RequestSpecification requestSpecification = given()
                .headers(jsonContent);

        if (profile != null)
                requestSpecification = requestSpecification.pathParam("profile", profile);

        String url = getEndPointPath();
        if (StringUtils.isNotBlank(profile))
            url = url + "/{profile}";

        if (StringUtils.isNotBlank(endPoint))
            url = url + "/" + endPoint;

        requestSpecification
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(url)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(expectedErrorCode))
                .statusCode(expectedStatusCode);
    }
}
