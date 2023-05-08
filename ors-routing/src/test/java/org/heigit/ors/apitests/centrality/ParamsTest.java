package org.heigit.ors.apitests.centrality;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.centrality.CentralityErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "centrality")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    public ParamsTest() {
        // set up coordinates for testing later
        JSONArray lowerLeft = new JSONArray();
        JSONArray upperRight = new JSONArray();
        JSONArray additionalComponentCoordinate = new JSONArray();
        JSONArray missingComponentCoordinate = new JSONArray();
        lowerLeft.put(8.677139);
        lowerLeft.put(49.412872);
        upperRight.put(8.690443);
        upperRight.put(49.421080);
        additionalComponentCoordinate.put(8.677139);
        additionalComponentCoordinate.put(49.395446);
        additionalComponentCoordinate.put(49.395446);
        missingComponentCoordinate.put(8.65538);

        // set up valid bounding box encompassing Neuenheim
        JSONArray neuenheimBBox = new JSONArray();
        neuenheimBBox.put(lowerLeft);
        neuenheimBBox.put(upperRight);
        addParameter("neuenheimBox", neuenheimBBox);

        // set up invalid bounding box containing too few coordinates
        JSONArray missingCoordinatesBox = new JSONArray();
        missingCoordinatesBox.put(lowerLeft);
        addParameter("missingCoordinatesBox", missingCoordinatesBox);

        // set up invalid bounding box containing too many coordinates
        JSONArray additionalCoordinatesBox = new JSONArray();
        additionalCoordinatesBox.put(lowerLeft);
        additionalCoordinatesBox.put(upperRight);
        additionalCoordinatesBox.put(upperRight);
        addParameter("additionalCoordinatesBox", additionalCoordinatesBox);

        // set up invalid bounding box containing a coordinate with too many ordinates
        JSONArray additionalComponentCoordinateBox = new JSONArray();
        additionalComponentCoordinateBox.put(lowerLeft);
        additionalComponentCoordinateBox.put(additionalComponentCoordinate);
        addParameter("additionalComponentCoordinateBox", additionalComponentCoordinateBox);

        //set up invalid bounding box containing a coordinate with too few ordinates
        JSONArray missingComponentCoordinateBox = new JSONArray();
        missingComponentCoordinateBox.put(lowerLeft);
        missingComponentCoordinateBox.put(missingComponentCoordinate);
        addParameter("missingComponentCoordinateBox", missingComponentCoordinateBox);

        // set up empty bounding box by using the same values as corners
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

    @Test
    void testCentralityEndpointIsAvailable() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("$", hasKey("locations"))
                .body("$", hasKey("nodeScores"))
                .statusCode(200);
    }

    @Test
    void testExcludeNodesGetExcluded() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));

        // Since node IDs are not consistent over graph builds, they cannot be specified beforehand,
        // but have to be taken from a valid response
        List<Integer> nodeIds = given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .post(getEndPointPath() + "/{profile}/json")
                .jsonPath().getList("locations.nodeId");

        // save three nodes that should be excluded and one that should still be present
        int node0 = nodeIds.get(0);
        int node1 = nodeIds.get(1);
        int node2 = nodeIds.get(2);
        int node3 = nodeIds.get(3);

        JSONArray excludeNodes = new JSONArray();
        excludeNodes.put(node0);
        excludeNodes.put(node1);
        excludeNodes.put(node2);

        body.put("excludeNodes", excludeNodes);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("locations.nodeId", not(hasItem(node0)))
                .body("locations.nodeId", not(hasItem(node1)))
                .body("locations.nodeId", not(hasItem(node2)))
                .body("locations.nodeId", hasItem(node3))
                .statusCode(200);
    }

    @Test
    void testNotFailingOnInvalidExcludeNodes() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("excludeNodes", getParameter("invalidNodes"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("nodeIds", not(contains(123456789)))
                .statusCode(200);
    }

    @Test
    void testErrorOnWrongSizeBBox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("missingCoordinatesBox"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("bbox", getParameter("additionalCoordinatesBox"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testErrorOnWrongSizeCoordinates() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("missingComponentCoordinateBox"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("bbox", getParameter("additionalComponentCoordinateBox"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testWarningAndEmptyLocationsOnEmptyBbox() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("emptyBox"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("$", hasKey("locations"))
                .body("locations", is(empty()))
                .body("warning.code", is(1))
                .statusCode(200);
    }

    @Test
    void testEdgeCentralityCalculationIfEdgeModeSpecified() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "edges");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("$", hasKey("locations")) // $ yields the JSON root
                .body("$", hasKey("edgeScores"))
                .body("$", not(hasKey("nodeScores")))
                .statusCode(200);
    }

    @Test
    void testNodeScoreCalculationIfNodeModeSpecified() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "nodes");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("$", hasKey("locations"))
                .body("$", hasKey("nodeScores"))
                .body("$", not(hasKey("edgeScores")))
                .statusCode(200);
    }

    @Test
    void testErrorIfWrongModeSpecified() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "wrongMode");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("error.code", is(CentralityErrorCodes.INVALID_PARAMETER_VALUE))
                .body("error.message", containsString("mode"))
                .statusCode(400);
    }
}

