package org.heigit.ors.apitests.matching;

import org.hamcrest.Matchers;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.routing.RoutingProfileType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

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
