package org.heigit.ors.v2.services.centrality;

import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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


        // set up invalid bounding box containing too few coordinates
        JSONArray tooFewCoordsBox = new JSONArray();
        tooFewCoordsBox.put(lowerLeft);
        addParameter("invalidBox", tooFewCoordsBox);

        // set up invalid/non-present Node
        JSONArray nonPresentNodes = new JSONArray();
        nonPresentNodes.put(123456789);

        addParameter("invalidNodes", nonPresentNodes);

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

    // test that excludeNodes get excluded
    @Test
    public void testExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("heidelberg"));

        // check that nodes to exclude are present in the response
        List<Integer> nodeIds =  given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .post(getEndPointPath()+"/{profile}/json")
                .jsonPath().getList("nodeIds");

        int node0 = nodeIds.get(0);
        int node1 = nodeIds.get(1);
        int node2 = nodeIds.get(2);
        int node3 = nodeIds.get(3);
        JSONArray excludeNodes = new JSONArray();
        excludeNodes.put(node0);
        excludeNodes.put(node1);
        excludeNodes.put(node2);
        excludeNodes.put(node3);

        body.put("excludeNodes", excludeNodes);
        //check that they are not present anymore if excluded
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
                .body("nodeIds", not(contains(node0)))
                .body("nodeIds", not(contains(node1)))
                .body("nodeIds", not(contains(node2)))
                .body("nodeIds", not(contains(node3)))
                .statusCode(200);
    }

    // test that invalid excludeNodes don't lead to everything failing
    // TODO: what should the system do here? If it should notify the user, how could that work?
    @Test
    public void testInvalidExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("heidelberg"));
        body.put("excludeNodes", getParameter("invalidNodes"));

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
                .body("nodeIds.contains(123456789)", is(false))
                .statusCode(200);
    }

    // test wrong bounding box:
    // + too few coords
    @Test
    public void testInvalidBBox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("invalidBox"));

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
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }
    // + too many coords
    // + coords out of range?

    //
}
