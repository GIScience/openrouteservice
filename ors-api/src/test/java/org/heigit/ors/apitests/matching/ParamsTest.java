package org.heigit.ors.apitests.matching;

import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.common.StatusCode.OK;
import static org.heigit.ors.matching.MatchingErrorCodes.*;

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
    private static final String GEO_JSON_POINT_ON_BORDER = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "coordinates": [
                      8.68109049010145,
                      49.400998811981474
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_POINT_ON_BRIDGE = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "coordinates": [
                      8.680940263471234,
                      49.40091328353546
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_INVALID_POINT = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "coordinates": [
                      0.0,
                      0.0
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_VALID_AND_INVALID_POINT = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
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
                  "geometry": {
                    "coordinates": [
                      0.0,
                      0.0
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_INVALID_LINE = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "coordinates": [
                      [
                        1.0,
                        2.0
                      ],
                      [
                        3.0,
                        4.0
                      ]
                    ],
                    "type": "LineString"
                  }
                }
              ]
            }
            """;
    private static final String GEO_JSON_EMPTY_GEOMETRY = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                  }
                }
              ]
            }
            """;

    private static final String FEATURE_BORDER = "border";
    private static final String FEATURE_BRIDGE = "bridge";


    private static JSONObject validBody(String geoJson) {
        return new JSONObject().put(KEY_FEATURES, new JSONObject(geoJson));
    }

    private static JSONObject addFeatureType(JSONObject body, String featureType) {
        JSONObject feature = body.getJSONObject(KEY_FEATURES).getJSONArray("features").getJSONObject(0);
        feature.put("properties", Map.of("type", featureType));
        return body;
    }

    private static Map<String, Matcher<?>> expectedEdgeIdsSize(List<Integer> sizes) {
        Map<String, Matcher<?>> ret = new HashMap<>();
        for (Integer size : sizes) {
            ret.put(KEY_EDGE_IDS + "[" + ret.size() + "].size()", is(size));
        }
        ret.put(KEY_GRAPH_TIMESTAMP, isValidTimestamp());
        return ret;
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
                Arguments.of(OK, PROFILE_CAR, validBody(GEO_JSON), expectedEdgeIdsSize(List.of(4, 1, 4))),
                Arguments.of(OK, PROFILE_HGV, validBody(GEO_JSON), expectedEdgeIdsSize(List.of(4, 1, 4))),
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject(), Map.of(
                        KEY_ERROR_CODE, is(MISSING_PARAMETER)
                )), // Missing 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject().put(KEY_FEATURES, new JSONObject()), Map.of(
                        KEY_ERROR_CODE, is(MISSING_PARAMETER)
                )), // Empty 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_HGV, new JSONObject().put(KEY_FEATURES, new JSONObject().put("type", "not geoJSON")), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_FORMAT)
                )), // Invalid 'features' parameter
                Arguments.of(BAD_REQUEST, PROFILE_CAR, new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON_SINGLE_FEATURE)), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_FORMAT)
                )), // Single Feature GeoJSON
                Arguments.of(BAD_REQUEST, PROFILE_CAR, new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON_SINGLE_GEOMETRY)), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_FORMAT)
                )), // Single Geometry GeoJSON
                Arguments.of(BAD_REQUEST, PROFILE_CAR, new JSONObject().put(KEY_FEATURES, new JSONObject(GEO_JSON_EMPTY_GEOMETRY)), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_FORMAT)
                )), // Empty geometry
                Arguments.of(BAD_REQUEST, "foo-bar", validBody(GEO_JSON), Map.of(
                        KEY_ERROR_CODE, is(INVALID_PARAMETER_VALUE)
                )), // Invalid profile
                Arguments.of(OK, PROFILE_CAR, validBody(GEO_JSON_INVALID_POINT), expectedEdgeIdsSize(List.of(0))), // Invalid point
                Arguments.of(OK, PROFILE_CAR, validBody(GEO_JSON_VALID_AND_INVALID_POINT), expectedEdgeIdsSize(List.of(1, 0))), // One valid and one invalid point
                Arguments.of(OK, PROFILE_CAR, validBody(GEO_JSON_INVALID_LINE), expectedEdgeIdsSize(List.of(0))), // Invalid line
                Arguments.of(BAD_REQUEST, PROFILE_CAR, validBody(GEO_JSON).put("foo", "bar"), Map.of(
                        KEY_ERROR_CODE, is(UNKNOWN_PARAMETER)
                )) // Unknown parameter
        );
    }

    private static void validateJsonResponse(ValidatableResponse result, Map<String, Matcher<?>> validations) {
        for (Map.Entry<String, Matcher<?>> entry : validations.entrySet()) {
            String query = entry.getKey();
            Matcher<?> matcher = entry.getValue();
            result.body(query, matcher);
        }
    }

    /**
     * Provides a stream of test arguments for testing successful scenarios of matching to specific feature types.
     * <p>
     * Each test case is represented as an instance of the Arguments class, containing the following parameters:
     * - feature-specific request body (JSONObject)
     * - reference request body (JSONObject)
     *
     * @return A stream of Arguments instances.
     */
    public static Stream<Arguments> matchingFeaturesTestProvider() {
        return Stream.of(
                Arguments.of(addFeatureType(validBody(GEO_JSON_POINT_ON_BORDER), FEATURE_BRIDGE), validBody(GEO_JSON_POINT_ON_BRIDGE)),
                Arguments.of(addFeatureType(validBody(GEO_JSON_POINT_ON_BRIDGE), FEATURE_BORDER), validBody(GEO_JSON_POINT_ON_BORDER))
        );
    }

    private static Matcher<String> isValidTimestamp() {
        return allOf(
                matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"),
                not(is("1970-01-01T00:00:00Z"))
        );
    }

    /**
     * Parameterized test method for testing various scenarios in the matching endpoint.
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

    @Test
    void testMissingPathParameterProfile() {
        given()
                .headers(jsonContent)
                .body(validBody(GEO_JSON).toString())
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

    @Test
    void testBadExportFormat() {
        given()
                .headers(jsonContent)
                .body(validBody(GEO_JSON).toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/driving-car/xml")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body("error.code", Matchers.is(UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(SC_NOT_ACCEPTABLE);
    }

    /**
     * Parameterized test method for testing of matching to specific features.
     *
     * @param featureRequest   feature-specific request body (JSONObject)
     * @param referenceRequest reference request body (JSONObject)
     */
    @ParameterizedTest
    @MethodSource("matchingFeaturesTestProvider")
    void testMatchingFeatures(JSONObject featureRequest, JSONObject referenceRequest) {
        var refValue = given()
                .headers(jsonContent)
                .pathParam(KEY_PROFILE, PROFILE_CAR)
                .body(referenceRequest.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + PATH_VAR_PROFILE)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body(KEY_EDGE_IDS + "[0].size()", is(1))
                .statusCode(OK)
                .extract().path(KEY_EDGE_IDS);

        given()
                .headers(jsonContent)
                .pathParam(KEY_PROFILE, PROFILE_CAR)
                .body(featureRequest.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + PATH_VAR_PROFILE)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .body(KEY_EDGE_IDS, is(refValue))
                .statusCode(OK);
    }
}
