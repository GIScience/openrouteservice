package org.heigit.ors.apitests.matching;

import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.routing.RoutingProfileType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.common.StatusCode.OK;
import static org.heigit.ors.matching.MatchingErrorCodes.INVALID_PARAMETER_VALUE;
import static org.heigit.ors.matching.MatchingErrorCodes.MISSING_PARAMETER;

@EndPointAnnotation(name = "match")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    private static final String PROFILE_CAR = RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR);
    private static final String PROFILE_HGV = RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV);
    private static final String KEY_FEATURES = "features";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_ERROR_CODE = "error.code";
    private static final String KEY_EDGE_IDS = "edge_ids";
    private static final String KEY_GRAPH_TIMESTAMP = "graph_timestamp";
    private static final String VAL_GRAPH_TIMESTAMP = "2024-09-08T20:21:00Z";
    private static final String PATH_VAR_PROFILE = "/{profile}";
    private static final String GEO_JSON = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "type": 1,
                    "constraints": 2
                  },
                  "geometry": {
                    "coordinates": [
                      [
                        [
                          8.684143117840563,
                          49.403360437437186
                        ],
                        [
                          8.684779169985319,
                          49.40302579535094
                        ],
                        [
                          8.685354323521352,
                          49.403404469120744
                        ],
                        [
                          8.684697971839427,
                          49.40380515562717
                        ],
                        [
                          8.684143117840563,
                          49.403360437437186
                        ]
                      ]
                    ],
                    "type": "Polygon"
                  }
                },
                {
                  "type": "Feature",
                  "properties": {
                    "type": 1,
                    "constraints": 2
                  },
                  "geometry": {
                    "coordinates": [
                      8.685990499833764,
                      49.40267842626105
                    ],
                    "type": "Point"
                  }
                },
                {
                  "type": "Feature",
                  "properties": {
                    "type": 1,
                    "constraints": 2
                  },
                  "geometry": {
                    "coordinates": [
                      [
                        8.684963166296484,
                        49.402520185458116
                      ],
                      [
                        8.684932534157866,
                        49.4019987410295
                      ],
                      [
                        8.685466741364422,
                        49.401268850720754
                      ],
                      [
                        8.686026375124385,
                        49.40092939040173
                      ]
                    ],
                    "type": "LineString"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_SINGLE_FEATURE = """
            {
                  "type": "Feature",
                  "properties": {
                    "type": 1,
                    "constraints": 2
                  },
                  "geometry": {
                    "coordinates": [
                      [
                        8.684963166296484,
                        49.402520185458116
                      ],
                      [
                        8.684932534157866,
                        49.4019987410295
                      ],
                      [
                        8.685466741364422,
                        49.401268850720754
                      ],
                      [
                        8.686026375124385,
                        49.40092939040173
                      ]
                    ],
                    "type": "LineString"
                  }
                }""";
    private static final String GEO_JSON_SINGLE_GEOMETRY = """
            {
                    "coordinates": [
                      [
                        8.684963166296484,
                        49.402520185458116
                      ],
                      [
                        8.684932534157866,
                        49.4019987410295
                      ],
                      [
                        8.685466741364422,
                        49.401268850720754
                      ],
                      [
                        8.686026375124385,
                        49.40092939040173
                      ]
                    ],
                    "type": "LineString"
                }""";

    private static JSONObject validBody() {
        return new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON));
    }

    private static Map<String, Matcher<?>> defaultValidations() {
        return Map.of(
                KEY_GRAPH_TIMESTAMP, isValidTimestamp(),
                KEY_EDGE_IDS + ".size()", is(9)
        );
    }

    /**
     * Provides a stream of test arguments for testing successful scenarios in the Snapping Endpoint.
     * <p>
     * Each test case is represented as an instance of the Arguments class, containing the following parameters:
     * - The expected status code (int).
     * - The routing profile type (String).
     * - The request body (JSONObject).
     * - A Map of validations (JSONObject).
     *
     * @return A stream of Arguments instances for testing successful scenarios in the Snapping Endpoint.
     */
    public static Stream<Arguments> matchingEndpointTestProvider() {
        return Stream.of(
                Arguments.of(OK, PROFILE_CAR, validBody(), defaultValidations()),
                Arguments.of(OK, PROFILE_HGV, validBody(), defaultValidations()),
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject(), Map.of(
                        KEY_ERROR_CODE, is(MISSING_PARAMETER)
                )), // Missing 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject().put(KEY_FEATURES, new JSONObject()), Map.of(
                        KEY_ERROR_CODE, is(MISSING_PARAMETER)
                )), // Empty 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject().put(KEY_FEATURES, new JSONObject().put("type", "not geoJSON")), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_VALUE)
                )), // Invalid 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_CAR, new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON_SINGLE_FEATURE)), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_VALUE)
                )), // Single Feature GeoJSON
                Arguments.of(BAD_REQUEST, PROFILE_CAR, new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON_SINGLE_GEOMETRY)), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_VALUE)
                )) // Single Geometry GeoJSON
        );
    }

    /**
     * Parameterized test method for testing various scenarios in the Snapping Endpoint.
     *
     * @param body    The request body (JSONObject).
     * @param profile The routing profile type (String).
     */
    @ParameterizedTest
    @MethodSource("matchingEndpointTestProvider")
    void testMatchingEndpoint(int statusCode, String profile, JSONObject body, Map<String, Matcher<?>> validations) {
        ValidatableResponse result = given()
                .headers(jsonContent)
                .pathParam(KEY_PROFILE, profile)
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + PATH_VAR_PROFILE)
                .then()
                .log().ifValidationFails()
                .statusCode(statusCode);
        validateJsonResponse(result, validations);
    }

    private static void validateJsonResponse(ValidatableResponse result, Map<String, Matcher<?>> validations) {
        for (Map.Entry<String, Matcher<?>> entry : validations.entrySet()) {
            String query = entry.getKey();
            Matcher<?> matcher = entry.getValue();
            result.body(query, matcher);
        }
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
                .body(KEY_ERROR_CODE, is(MISSING_PARAMETER))
                .statusCode(BAD_REQUEST);
    }

    @Test
    void testGetMatching() {
        given()
                .headers(jsonContent)
                .pathParam(KEY_PROFILE, PROFILE_CAR)
                .when()
                .log().ifValidationFails()
                .get(getEndPointPath() + PATH_VAR_PROFILE)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body(KEY_GRAPH_TIMESTAMP, isValidTimestamp())
                .statusCode(OK);
    }

    private static Matcher<String> isValidTimestamp() {
        return allOf(
                matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"),
                not(is("1970-01-01T00:00:00Z"))
        );
    }
}
