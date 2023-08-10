package org.heigit.ors.apitests.snapping;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "snap")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

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
    void basicTest () {
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
}
