package org.heigit.ors.apitests.snapping;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.routing.RoutingProfileType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.common.StatusCode.NOT_FOUND;
import static org.heigit.ors.snapping.SnappingErrorCodes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EndPointAnnotation(name = "snap")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    private static final String locationParameter = "locations";
    private static final String radiusParameter = "radius";

    /**
     * radiusParameter
     * Generates a JSONArray with fake locations for testing purposes.
     *
     * @param maximumSize The maximum size of the JSONArray.
     * @return A JSONArray containing fake locations with the specified size.
     */
    private static JSONArray fakeLocations(int maximumSize) {
        JSONArray overloadedLocations = new JSONArray();
        for (int i = 0; i < maximumSize; i++) {
            overloadedLocations.put(invalidLocation());
        }
        return overloadedLocations;
    }

    private static JSONArray location2m() {
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        return coord2;
    }

    private static JSONArray location94m() {
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        return coord1;
    }

    private static JSONArray invalidLocation() {
        JSONArray coord1 = new JSONArray();
        coord1.put(0.0);
        coord1.put(0.0);
        return coord1;
    }

    /**
     * Generates a JSONArray with valid locations for testing purposes.
     *
     * @return A JSONArray containing valid coordinates for testing.
     */
    private static JSONArray createLocations(JSONArray... locations) {
        JSONArray correctTestLocations = new JSONArray();
        correctTestLocations.putAll(locations);
        return correctTestLocations;
    }


    private static JSONObject validBody() {
        return new JSONObject()
                .put(locationParameter, createLocations(location94m(), location2m()))
                .put(radiusParameter, "300");
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
                Arguments.of(Arrays.asList(false, false), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "-1")),
                Arguments.of(Arrays.asList(false, false), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "0")),
                Arguments.of(Arrays.asList(false, false), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "1")),
                Arguments.of(Arrays.asList(false, true), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "10")),
                Arguments.of(Arrays.asList(true, true), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "300")),
                Arguments.of(Arrays.asList(true, false, true), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location2m(), location94m(), location2m())).put(radiusParameter, "10")),
                Arguments.of(Arrays.asList(true, true), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "400")),
                Arguments.of(Arrays.asList(true, true), RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), new JSONObject()
                        .put(locationParameter, createLocations(location94m(), location2m())).put(radiusParameter, "1000"))
        );
    }

    /**
     * Parameterized test method for testing various scenarios in the Snapping Endpoint.
     *
     * @param expectedSnapped Boolean flags indicating if the locations are expected to be snapped.
     * @param body            The request body (JSONObject).
     * @param profile         The routing profile type (String).
     */
    @ParameterizedTest
    @MethodSource("snappingEndpointSuccessTestProvider")
    void testSnappingSuccessJson(List<Boolean> expectedSnapped, String profile, JSONObject body) {
        String endPoint = "json";
        ValidatableResponse result = doRequestAndExceptSuccess(body, profile, endPoint);
        validateJsonResponse(expectedSnapped, result);
    }

    @Test
    void testMissingPathParameterFormat_defaultsToJson() {
        JSONObject body = validBody();
        ValidatableResponse result = doRequestAndExceptSuccess(body, RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV), null);
        validateJsonResponse(Arrays.asList(true, true), result);
    }

    private static void validateJsonResponse(List<Boolean> expectedSnappedList, ValidatableResponse result) {
        result.body("any { it.key == 'locations' }", is(true));

        // Iterate over the snappedLocations array and check the types of the values
        ArrayList<Integer> snappedLocations = result.extract().jsonPath().get(locationParameter);
        for (int i = 0; i < snappedLocations.size(); i++) {
            boolean expectedSnapped = expectedSnappedList.get(i);
            if (!expectedSnapped) {
                assertNull(result.extract().jsonPath().get("locations[" + i + "].location[0]"));
            } else {
                // Type expectations
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].location[0]").getClass());
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].location[1]").getClass());
                assertEquals(Float.class, result.extract().jsonPath().get("locations[" + i + "].snapped_distance").getClass());
                // If name is in the response, check the type
                if (result.extract().jsonPath().get("locations[" + i + "].name") != null)
                    assertEquals(String.class, result.extract().jsonPath().get("locations[" + i + "].name").getClass());
            }
        }
    }

    /**
     * Parameterized test method for testing various scenarios in the Snapping Endpoint.
     *
     * @param expectedSnapped Boolean flags indicating if the locations are expected to be snapped.
     * @param body            The request body (JSONObject).
     * @param profile         The routing profile type (String).
     */
    @ParameterizedTest
    @MethodSource("snappingEndpointSuccessTestProvider")
    void testSnappingSuccessGeojson(List<Boolean> expectedSnapped, String profile, JSONObject body) {
        String endPoint = "geojson";
        ValidatableResponse result = doRequestAndExceptSuccess(body, profile, endPoint);

        result.body("any { it.key == 'features' }", is(true));
        result.body("any { it.key == 'type' }", is(true));
        List<Integer> expectedSourceIds = new ArrayList<>();
        for (int i = 0; i < expectedSnapped.size(); i++) {
            if (expectedSnapped.get(i)) {
                expectedSourceIds.add(i);
            }
        }

        ArrayList<JSONArray> features = result.extract().jsonPath().get("features");
        assertThat(features).hasSize(expectedSourceIds.size());
        for (int i = 0; i < features.size(); i++) {
            assertEquals(Float.class, result.extract().jsonPath().get("features[" + i + "].geometry.coordinates[0]").getClass());
            assertEquals(Float.class, result.extract().jsonPath().get("features[" + i + "].geometry.coordinates[1]").getClass());
            assertEquals(Float.class, result.extract().jsonPath().get("features[" + i + "].properties.snapped_distance").getClass());
            assertEquals(Integer.class, result.extract().jsonPath().get("features[" + i + "].properties.source_id").getClass());
            assertEquals(result.extract().jsonPath().get("features[" + i + "].properties.source_id"), expectedSourceIds.get(i));
            // If name is in the response, check the type
            if (result.extract().jsonPath().get("features[" + i + "].properties.name") != null)
                assertEquals(String.class, result.extract().jsonPath().get("features[" + i + "].properties.name").getClass());
        }
    }

    private ValidatableResponse doRequestAndExceptSuccess(JSONObject body, String profile, String endPoint) {
        String url = getEndPointPath() + "/{profile}";
        if (StringUtils.isNotBlank(endPoint))
            url = "%s/%s".formatted(url, endPoint);

        ValidatableResponse result = given()
                .headers(jsonContent)
                .pathParam("profile", profile)
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(url)
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        // Check if the response contains the expected keys
        result.body("any { it.key == 'metadata' }", is(true));
        result.body("metadata.containsKey('attribution')", is(true));
        result.body("metadata.service", is("snap"));
        result.body("metadata.containsKey('timestamp')", is(true));
        result.body("metadata.containsKey('query')", is(true));
        result.body("metadata.containsKey('engine')", is(true));
        result.body("metadata.containsKey('system_message')", is(true));
        result.body("metadata.query.locations.size()", is(((JSONArray) body.get(locationParameter)).toList().size()));
        result.body("metadata.query.locations[0].size()", is(2));
        result.body("metadata.query.locations[1].size()", is(2));
        result.body("metadata.query.profile", is(profile));

        if (StringUtils.isNotBlank(endPoint))
            result.body("metadata.query.format", is(endPoint));

        if (body.get(radiusParameter) != "0") {
            result.body("metadata.query.radius", is(Float.parseFloat(body.get(radiusParameter).toString())));
        }
        return result;
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

        JSONArray correctTestLocations = createLocations(location94m(), location2m());
        // Return a stream of test arguments
        return Stream.of(
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()),
                // Check exception for one fake location to ensure single locations are checked
                Arguments.of(POINT_NOT_FOUND, NOT_FOUND, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, oneFakeLocation)),
                // Check exception for ten fake locations to ensure multiple locations are checked
                Arguments.of(POINT_NOT_FOUND, NOT_FOUND, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, tenFakeLocations)),
                // Check exception for broken location to ensure invalid locations are checked
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, brokenFakeLocation)),
                // Check exception for wrong profile to ensure invalid profiles are checked
                Arguments.of(INVALID_PARAMETER_VALUE, BAD_REQUEST, "driving-foo", new JSONObject()
                        .put(locationParameter, correctTestLocations)),
                // Check exception for unknown parameter to ensure unknown parameters are checked
                Arguments.of(UNKNOWN_PARAMETER, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, correctTestLocations).put("unknown", "unknown")),
                // Check exception for invalid locations parameter (only one ccordinate)
                Arguments.of(INVALID_PARAMETER_VALUE, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, invalidCoords).put(radiusParameter, "300")),
                // Check exception for invalid locations parameter (only one ccordinate)
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, "noJsonArray").put(radiusParameter, "300")),
                // Check exception for invalid radius
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(locationParameter, correctTestLocations).put(radiusParameter, "notANumber")),
                // Check exception for missing locations parameter
                Arguments.of(INVALID_PARAMETER_FORMAT, BAD_REQUEST, RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR), new JSONObject()
                        .put(radiusParameter, "300")
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
    void testSnappingExceptionsJson(int expectedErrorCode, int expectedStatusCode, String profile, JSONObject body) {
        doRequestAndExpectError(expectedErrorCode, expectedStatusCode, profile, "json", body);
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
    void testSnappingExceptionsGeojson(int expectedErrorCode, int expectedStatusCode, String profile, JSONObject body) {
        doRequestAndExpectError(expectedErrorCode, expectedStatusCode, profile, "geojson", body);
    }

    void doRequestAndExpectError(int expectedErrorCode, int expectedStatusCode, String profile, String endPoint, JSONObject body) {
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

    @Test
    void testMissingPathParametersProfileAndFormat() {
        JSONObject body = validBody();

        given()
                .headers(jsonContent)
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(MISSING_PARAMETER))
                .statusCode(BAD_REQUEST);
    }

    @Test
    void testMissingPathParameterProfile() {
        given()
                .headers(jsonContent)
                .body(validBody().toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/json")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(INVALID_PARAMETER_VALUE))
                .statusCode(BAD_REQUEST);
    }

    @Test
    void testBadExportFormat() {
        given()
                .headers(jsonContent)
                .body(validBody().toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/driving-car/xml")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(SC_NOT_ACCEPTABLE);
    }

}
