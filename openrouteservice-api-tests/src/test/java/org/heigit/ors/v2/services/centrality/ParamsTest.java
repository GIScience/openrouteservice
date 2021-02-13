package org.heigit.ors.v2.services.centrality;

import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "centrality")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {

    public ParamsTest() {
        // set up valid bounding box encompassing main Heidelberg
        JSONArray heidelbergBBox = new JSONArray();
        JSONArray lowerLeft = new JSONArray();
        JSONArray upperRight = new JSONArray();
        lowerLeft.put(8.655705);
        lowerLeft.put(49.395446);
        upperRight.put(8.718184);
        upperRight.put(49.434366);

        heidelbergBBox.put(lowerLeft);
        heidelbergBBox.put(upperRight);

        addParameter("heidelberg", heidelbergBBox);


        // set up invalid bounding box(es)?

        // set up nodes to exclude
        JSONArray theodorHeussBridgeNodes = new JSONArray();
        theodorHeussBridgeNodes.put(6462);
        theodorHeussBridgeNodes.put(11546);
        theodorHeussBridgeNodes.put(4967);
        theodorHeussBridgeNodes.put(7493);
        theodorHeussBridgeNodes.put(6463);

        addParameter("theodorHeussBridge", theodorHeussBridgeNodes);

        // set different profiles
        addParameter("profile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");

    }

    // test general functionality
    @Test
    public void testBasicFunctionality() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("heidelberg"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath()+"/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("any { it.key == 'locations' }", is(true))
                .body("any { it.key == 'centralityScores' }", is(true))
                .statusCode(200);
    }

    // test that excludenodes get excluded
    @Test
    public void testExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("heidelberg"));

        // check that nodes to exclude are present in the response
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath()+"/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("locations.containsValue(6462)", is(true))
                .statusCode(200);

        //check that they are not present anymore if excluded

    }

    // test invalid excludenodes (return value unclear)

    // test wrong bounding box:
    // + too few coords
    // + too many coords
    // + coords out of range?

    //
}
