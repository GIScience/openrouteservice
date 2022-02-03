package org.heigit.ors.v2.services.centrality;

import io.restassured.response.Response;
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
public class ResultTest extends ServiceTest {

    public ResultTest() {
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

        // set different profiles
        addParameter("profile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");

    }

    @Test
    public void testAllEdgeBetweennessNodeIdsInLocations() {
        JSONObject body = new JSONObject();
        body.put("bbox", getParameter("neuenheimBox"));
        body.put("mode", "edges");

        // edgeBetweenness-initialization also initialized edges whose to-node wasn't contained in the bbox.
        // this test makes sure that all node ids in edgeScores are also present in locations
        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .post(getEndPointPath() + "/{profile}/json");

        List<Integer> nodeIds = response.jsonPath().getList("locations.nodeId");
        response.then()
                .log().ifValidationFails()
                .body("edgeScores.fromId", everyItem(is(in(nodeIds))))
                .body("edgeScores.toId", everyItem(is(in(nodeIds))));
    }
}
