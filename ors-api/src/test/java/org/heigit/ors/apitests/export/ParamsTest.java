package org.heigit.ors.apitests.export;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.export.ExportErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import org.hamcrest.Matchers;

@EndPointAnnotation(name = "export")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {
    public ParamsTest(){
        JSONArray coord1 = new JSONArray();
        coord1.put(0.001);
        coord1.put(0.001);
        
        JSONArray bboxMock = new JSONArray();
        bboxMock.put(coord1);
        bboxMock.put(coord1);

        JSONArray bboxFaulty = new JSONArray();
        bboxFaulty.put(coord1);
        
        addParameter("bboxFake", bboxMock);
        addParameter("bboxFaulty", bboxFaulty);
    }

    @Test
    void basicPingTest() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("bboxFake"));
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/topojson")
                .then()
                .statusCode(200);
    }

    @Test
    void expectUnknownProfile() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("bboxFake"));
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car-123")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", Matchers.is(ExportErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectInvalidResponseFormat() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("bboxFake"));
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/blah")
                .then()
                .assertThat()
                .body("error.code", Matchers.is(ExportErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);
    }

    @Test
    void expectMissingBbox() {
        JSONObject body = new JSONObject();

        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                //not sure mismatched input is correct here, missing parameter seems more intuitive?
                .body("error.code", Matchers.is(ExportErrorCodes.MISMATCHED_INPUT))
                .statusCode(400);
    }

    @Test
    void expectFaultyBbox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("bboxFaulty"));
        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/topojson")
                .then()
                .assertThat()
                .body("error.code", Matchers.is(ExportErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

}
