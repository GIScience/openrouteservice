package org.heigit.ors.apitests.export;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "export")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {
    private static JSONArray coord1(){
        JSONArray coord1 = new JSONArray();
        coord1.put(0.001);
        coord1.put(0.001);
        return coord1;
    }
    private static JSONArray bbox() {
        JSONArray bbox = new JSONArray();
        bbox.put(coord1());
        bbox.put(coord1());
        return bbox;
    }

    @Test
    void basicPingTest() {
        JSONObject body = new JSONObject();
        body.put("bbox", bbox());
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/topojson")
                .then()
                .statusCode(200);
    }
}
