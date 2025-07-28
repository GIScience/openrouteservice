package org.heigit.ors.apitests.matching;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.routing.RoutingProfileType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.matching.MatchingErrorCodes.MISSING_PARAMETER;

@EndPointAnnotation(name = "match")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    private static final String profile_car = RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR);
    private static final String profile_hgv = RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV);

    private static void validateJsonResponse(ValidatableResponse result) {
        result.body("any { it.key == 'graph_date' }", is(true));
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
    public static Stream<Arguments> matchingEndpointSuccessTestProvider() {
        return Stream.of(
                Arguments.of(profile_car, new JSONObject()),
                Arguments.of(profile_hgv, new JSONObject())
        );
    }

    /**
     * Parameterized test method for testing various scenarios in the Snapping Endpoint.
     *
     * @param body            The request body (JSONObject).
     * @param profile         The routing profile type (String).
     */
    @ParameterizedTest
    @MethodSource("matchingEndpointSuccessTestProvider")
    void testSnappingSuccessJson(String profile, JSONObject body) {
        ValidatableResponse result = doRequestAndExpectSuccess(body, profile);
        validateJsonResponse(result);
    }

    private ValidatableResponse doRequestAndExpectSuccess(JSONObject body, String profile) {
        String url = getEndPointPath() + "/{profile}";

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

        return result;
    }

    private static JSONObject validBody() {
        return new JSONObject().put("foo", "bar");
    }

    @Test
    void testMissingPathParameterProfile() {
        given()
                .headers(jsonContent)
                .body(validBody().toString())
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
    void testValidPathParameterProfile() {
        given()
                .headers(jsonContent)
                .body(validBody().toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/driving-car")
                .then()
                .log().ifValidationFails()
                .statusCode(200).body("any { it.key == 'graph_date' }", is(true));
    }
}
