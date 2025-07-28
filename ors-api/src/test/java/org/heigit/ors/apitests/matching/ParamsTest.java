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
    private static final String geoJSON = "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"type\": 1,\n" +
            "        \"constraints\": 2\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"coordinates\": [\n" +
            "          8.680647166648555,\n" +
            "          49.42176599469272\n" +
            "        ],\n" +
            "        \"type\": \"Point\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"type\": 2,\n" +
            "        \"constraints\": 1\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            8.676513391112195,\n" +
            "            49.4169131706428\n" +
            "          ],\n" +
            "          [\n" +
            "            8.676561249583898,\n" +
            "            49.41905360928652\n" +
            "          ],\n" +
            "          [\n" +
            "            8.676465532639412,\n" +
            "            49.4209526839596\n" +
            "          ],\n" +
            "          [\n" +
            "            8.676920188125592,\n" +
            "            49.42175432047108\n" +
            "          ],\n" +
            "          [\n" +
            "            8.678260225347856,\n" +
            "            49.42271938006394\n" +
            "          ]\n" +
            "        ],\n" +
            "        \"type\": \"LineString\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"type\": 1,\n" +
            "        \"constraints\": 0\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            [\n" +
            "              8.68236408933791,\n" +
            "              49.420614124800636\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68266320478918,\n" +
            "              49.41815852811973\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68326741800152,\n" +
            "              49.4172050541369\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68477495987537,\n" +
            "              49.41715835289463\n" +
            "            ],\n" +
            "            [\n" +
            "              8.686132944024052,\n" +
            "              49.41723229650793\n" +
            "            ],\n" +
            "            [\n" +
            "              8.686617511055516,\n" +
            "              49.418329762301454\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68653974103853,\n" +
            "              49.41927153962135\n" +
            "            ],\n" +
            "            [\n" +
            "              8.686276519441378,\n" +
            "              49.419567300670565\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68604919169826,\n" +
            "              49.420699737682185\n" +
            "            ],\n" +
            "            [\n" +
            "              8.685738111628325,\n" +
            "              49.42101105599241\n" +
            "            ],\n" +
            "            [\n" +
            "              8.682752939424859,\n" +
            "              49.42080869931536\n" +
            "            ],\n" +
            "            [\n" +
            "              8.68236408933791,\n" +
            "              49.420614124800636\n" +
            "            ]\n" +
            "          ]\n" +
            "        ],\n" +
            "        \"type\": \"Polygon\"\n" +
            "      }\n" +
            "    } \n" +
            "  ]\n" +
            "}";
    private static final String geoJSONSingleFeature = "{\n" +
            "  \"type\": \"Feature\",\n" +
            "  \"properties\": {\n" +
            "    \"type\": 2,\n" +
            "    \"constraints\": 1\n" +
            "  },\n" +
            "  \"geometry\": {\n" +
            "    \"coordinates\": [\n" +
            "      [\n" +
            "        8.676513391112195,\n" +
            "        49.4169131706428\n" +
            "      ],\n" +
            "      [\n" +
            "        8.676561249583898,\n" +
            "        49.41905360928652\n" +
            "      ],\n" +
            "      [\n" +
            "        8.676465532639412,\n" +
            "        49.4209526839596\n" +
            "      ],\n" +
            "      [\n" +
            "        8.676920188125592,\n" +
            "        49.42175432047108\n" +
            "      ],\n" +
            "      [\n" +
            "        8.678260225347856,\n" +
            "        49.42271938006394\n" +
            "      ]\n" +
            "    ],\n" +
            "    \"type\": \"LineString\"\n" +
            "  }\n" +
            "}\n" +
            "  ";


    private static JSONObject validBody() {
        return new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSON));
    }

    private static Map<String, Matcher> defaultValidations() {
        return Map.of(
                "any { it.key == 'graph_date' }", is(true)
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
                Arguments.of(OK, profile_hgv, new JSONObject().put("key", "logie").put("features", new JSONObject(geoJSONSingleFeature)), defaultValidations()) // Single Feature GeoJSON
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
