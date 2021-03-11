package org.heigit.ors.v2.services.centrality;

import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@EndPointAnnotation(name = "centrality")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {

    public ParamsTest() {
        // set up coordinates for testing later
        JSONArray lowerLeft = new JSONArray();
        JSONArray upperRight = new JSONArray();
        JSONArray tooLargeCoordinate = new JSONArray();
        JSONArray tooSmallCoordinate = new JSONArray();
        lowerLeft.put(8.677139);
        lowerLeft.put(49.412872);
        upperRight.put(8.690443);
        upperRight.put(49.421080);
        tooLargeCoordinate.put(8.677139);
        tooLargeCoordinate.put(49.395446);
        tooLargeCoordinate.put(49.395446);
        tooSmallCoordinate.put(8.65538);

        // set up valid bounding box encompassing Neuenheim
        JSONArray neuenheimBBox = new JSONArray();
        neuenheimBBox.put(lowerLeft);
        neuenheimBBox.put(upperRight);
        addParameter("neuenheimBox", neuenheimBBox);

        // set up invalid bounding box containing too few coordinates
        JSONArray tooSmallBox = new JSONArray();
        tooSmallBox.put(lowerLeft);
        addParameter("tooSmallBox", tooSmallBox);

        // set up invalid bounding box containing too many coordinates
        JSONArray tooLargeBox = new JSONArray();
        tooLargeBox.put(lowerLeft);
        tooLargeBox.put(upperRight);
        tooLargeBox.put(upperRight);
        addParameter("tooLargeBox", tooLargeBox);

        // set up invalid bounding box containing a coordinate with too many ordinates
        JSONArray tooLargeCoordBox = new JSONArray();
        tooLargeCoordBox.put(lowerLeft);
        tooLargeCoordBox.put(tooLargeCoordinate);
        addParameter("tooLargeCoordBox", tooLargeCoordBox);

        //set up invalid bounding box containing a coordinate with too few ordinates
        JSONArray tooSmallCoordBox = new JSONArray();
        tooSmallCoordBox.put(lowerLeft);
        tooSmallCoordBox.put(tooSmallCoordinate);
        addParameter("tooSmallCoordBox", tooSmallCoordBox);

        // set up empty bounding box by using the same values as edges
        JSONArray emptyBox = new JSONArray();
        emptyBox.put(lowerLeft);
        emptyBox.put(lowerLeft);
        addParameter("emptyBox", emptyBox);

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
        body.put("bbox", getParameter("neuenheimBox"));

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
                .body("$", hasKey("locations"))
                .body("$", hasKey("nodeScores"))
                .statusCode(200);
    }

    // test that excludeNodes get excluded
    @Test
    public void testExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));

        // check that nodes to exclude are present in the response
        List<Integer> nodeIds =  given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .post(getEndPointPath()+"/{profile}/json")
                .jsonPath().getList("locations.nodeId");

        int node0 = nodeIds.get(0);
        int node1 = nodeIds.get(1);
        int node2 = nodeIds.get(2);
        int node3 = nodeIds.get(3);
        System.out.printf("%d, %d, %d, %d\n", node0, node1, node2, node3);
        JSONArray excludeNodes = new JSONArray();
        excludeNodes.put(node0);
        excludeNodes.put(node1);
        excludeNodes.put(node2);

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
                .log().all()
                .body("locations.nodeId", not(hasItem(node0)))
                .body("locations.nodeId", not(hasItem(node1)))
                .body("locations.nodeId", not(hasItem(node2)))
                .body("locations.nodeId", hasItem(node3))
                .statusCode(200);
    }

    // test that invalid excludeNodes don't lead to everything failing
    // TODO: what should the system do here? If it should notify the user, how could that work?
    @Test
    public void testInvalidExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
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
                .body("nodeIds", not(contains(123456789)))
                .statusCode(200);
    }

    // test that invalid bounding boxes with too few/too many coordinates return an error
    @Test
    public void testWrongSizeBBox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("tooSmallBox"));

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

        body.put("bbox", getParameter("tooLargeBox"));
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

    // test that invalid bounding boxes with too large/too small coordinates return an error
    @Test
    public void testWrongSizedCoordinates() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("tooSmallCoordBox"));

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

        body.put("bbox", getParameter("tooLargeCoordBox"));
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

    // test that empty bounding boxes get handled appropriately
    //TODO: currently, both locations and nodeScores will just return empty.
    // This is the expected and valid answer, but not too helpful.
    // Should some kind of error be reported to the user?
    @Test
    public void testEmptyBbox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("emptyBox"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath()+"/{profile}/json")
                .then()
                .log().all()
                .body("$", hasKey("locations"))
                .body("$", hasKey("nodeScores"))
                .statusCode(200);
    }

    @Test
    public void testEdgeMode() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "edges");

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
                .body("$", hasKey("locations")) // $ yields the JSON root
                .body("$", hasKey("edgeScores"))
                .body("$", not(hasKey("nodeScores")))
                .statusCode(200);
    }

    @Test
    public void testNodeMode() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "nodes");

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
                .body("$", hasKey("locations"))
                .body("$", hasKey("nodeScores"))
                .body("$", not(hasKey("edgeScores")))
                .statusCode(200);
    }

    @Test
    public void testWrongMode() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "wrongMode");

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
                .body("error.message", containsString("mode"))
                .statusCode(400);
    }
}

