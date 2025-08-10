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

import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.common.StatusCode.OK;
import static org.heigit.ors.common.StatusCode.BAD_REQUEST;
import static org.heigit.ors.matching.MatchingErrorCodes.INVALID_PARAMETER_VALUE;
import static org.heigit.ors.matching.MatchingErrorCodes.MISSING_PARAMETER;

@EndPointAnnotation(name = "match")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    private static final String profile_car = RoutingProfileType.getName(RoutingProfileType.DRIVING_CAR);
    private static final String profile_hgv = RoutingProfileType.getName(RoutingProfileType.DRIVING_HGV);
    private static final String geoJSON = """
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
    private static final String geoJSONSingleFeature = """
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
    private static final String geoJSONSingleGeometry = """
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
    private static final String geoJSONMissingProperties = """
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

    private static JSONObject validBody() {
        return new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSON));
    }

    private static Map<String, Matcher> defaultValidations() {
        return Map.of(
                "any { it.key == 'graph_date' }", is(true),
                "matched.size()", is(3),
                "matched[0]", is(4),
                "matched[1]", is(1),
                "matched[2]", is(4)
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
                Arguments.of(OK, profile_car, validBody(), defaultValidations()),
                Arguments.of(OK, profile_hgv, validBody(), defaultValidations()),
                Arguments.of(BAD_REQUEST, profile_hgv, new JSONObject().put("features", new JSONObject(geoJSON)), Map.of(
                        "error.code", is(MISSING_PARAMETER)
                )), // Missing 'key' parameter
                Arguments.of(BAD_REQUEST, profile_hgv, new JSONObject().put("key", "logie"), Map.of(
                        "error.code", is(MISSING_PARAMETER)
                )), // Missing 'features' parameter
                Arguments.of(BAD_REQUEST, profile_hgv, new JSONObject().put("key", "logie").put("features", new JSONObject()), Map.of(
                        "error.code", is(MISSING_PARAMETER)
                )), // Empty 'features' parameter
                Arguments.of(BAD_REQUEST, profile_hgv, new JSONObject().put("key", "logie").put("features", new JSONObject().put("type", "not geoJSON")), Map.of(
                        "error.code", is(INVALID_PARAMETER_VALUE)
                )), // Invalid 'features' parameter
                Arguments.of(BAD_REQUEST, profile_car, new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSONSingleFeature)), Map.of(
                        "error.code", is(INVALID_PARAMETER_VALUE)
                )), // Single Feature GeoJSON
                Arguments.of(BAD_REQUEST, profile_car, new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSONSingleGeometry)), Map.of(
                        "error.code", is(INVALID_PARAMETER_VALUE)
                )), // Single Geometry GeoJSON
                Arguments.of(BAD_REQUEST, profile_car, new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSONMissingProperties)), Map.of(
                        "error.code", is(INVALID_PARAMETER_VALUE)
                )) // GeoJSON missing properties
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
    void testMatchingEndpoint(int statusCode, String profile, JSONObject body, Map<String, Matcher> validations) {
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
                .statusCode(statusCode);
        validateJsonResponse(result, validations);
    }

    private static void validateJsonResponse(ValidatableResponse result, Map<String, Matcher> validations) {
        for (Map.Entry<String, Matcher> entry : validations.entrySet()) {
            String query = entry.getKey();
            Matcher matcher = entry.getValue();
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
                .body("error.code", Matchers.is(MISSING_PARAMETER))
                .statusCode(BAD_REQUEST);
    }
}
