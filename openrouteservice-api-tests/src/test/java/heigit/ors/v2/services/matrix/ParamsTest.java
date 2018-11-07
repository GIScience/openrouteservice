/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.v2.services.matrix;

import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.v2.services.common.EndPointAnnotation;
import heigit.ors.v2.services.common.ServiceTest;
import heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "matrix")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {

    public ParamsTest() {
        // Locations
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.681495);
        coord1.put(49.41461);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.686507);
        coord2.put(49.41943);
        coordsShort.put(coord2);
        JSONArray coord3 = new JSONArray();
        coord3.put(8.687872);
        coord3.put(49.420318);
        coordsShort.put(coord3);
        addParameter("locations", coordsShort);
        JSONArray coordsFaulty = new JSONArray();
        JSONArray coordFaulty1 = new JSONArray();
        coordFaulty1.put("8.680916a");
        coordFaulty1.put("49.41b0973");
        coordsFaulty.put(coordFaulty1);
        JSONArray coordFaulty2 = new JSONArray();
        coordFaulty2.put("8.6c87782");
        coordFaulty2.put("049gbd.424597");
        coordsFaulty.put(coordFaulty2);
        JSONArray coordFaulty3 = new JSONArray();
        coordFaulty3.put("8.w87872");
        coordFaulty3.put("49.420c318");
        coordsFaulty.put(coordFaulty3);
        addParameter("locationsFaulty", coordsFaulty);

        // Fake array to test maximum exceedings
        JSONArray maximumLocations = fakeLocations(MatrixServiceSettings.getMaximumLocations(false) + 1);
        addParameter("maximumLocations", maximumLocations);
        JSONArray minimalLocations = fakeLocations(1);
        addParameter("minimalLocations", minimalLocations);
        // Sources
        JSONArray sourcesAll = new JSONArray();
        sourcesAll.put("all");
        addParameter("sourcesAll", sourcesAll);
        JSONArray sourcesAllBlank = new JSONArray();
        sourcesAllBlank.put("1");
        sourcesAllBlank.put("2");
        sourcesAllBlank.put("3");
        addParameter("sourcesAllBlank", sourcesAllBlank);

        JSONArray source1 = new JSONArray();
        source1.put("1");
        addParameter("source1", source1);
        JSONArray source2 = new JSONArray();
        source2.put("2");
        addParameter("source2", source1);
        JSONArray source3 = new JSONArray();
        source3.put("3");
        addParameter("source3", source1);
        JSONArray source4Fail = new JSONArray();
        source4Fail.put("4");
        addParameter("source4Fail", source1);

        JSONArray faultySource = new JSONArray();
        faultySource.put("fail");
        addParameter("faultySource", faultySource);

        // Destinations
        JSONArray destinationsAll = new JSONArray();
        destinationsAll.put("all");
        addParameter("destinationsAll", destinationsAll);
        JSONArray destinationsAllBlank = new JSONArray();
        destinationsAll.put("1");
        destinationsAll.put("2");
        destinationsAll.put("3");
        addParameter("destinationsAllBlank", destinationsAllBlank);

        JSONArray destination1 = new JSONArray();
        destination1.put("1");
        addParameter("destination1", source1);
        JSONArray destination2 = new JSONArray();
        destination2.put("2");
        addParameter("destination2", source1);
        JSONArray destination3 = new JSONArray();
        destination3.put("3");
        addParameter("destination3", source1);
        JSONArray destination4Fail = new JSONArray();
        destination4Fail.put("4");
        addParameter("destination4Fail", source1);

        JSONArray faultydestination = new JSONArray();
        faultydestination.put("fail");
        addParameter("faultyDestination", faultydestination);

        // Metrics
        JSONArray metricsAll = new JSONArray();
        metricsAll.put("duration");
        metricsAll.put("distance");
        metricsAll.put("weight");
        addParameter("metricsAll", metricsAll);

        JSONArray metricsDuration = new JSONArray();
        metricsDuration.put("duration");
        addParameter("metricsDuration", metricsDuration);

        JSONArray metricsDistance = new JSONArray();
        metricsDistance.put("distance");
        addParameter("metricsDistance", metricsDistance);

        JSONArray metricsWeight = new JSONArray();
        metricsWeight.put("weight");
        addParameter("metricsWeight", metricsWeight);

        // ID
        addParameter("id", "someID");

        // Profiles
        addParameter("cyclingProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");
    }

    /**
     * This function creates a {@link JSONArray} with fake coordinates.
     * The size depends on maximumSize.
     *
     * @param maximumSize number of maximum coordinates in the {@link JSONArray}
     * @return {@link JSONArray}
     */
    private JSONArray fakeLocations(int maximumSize) {
        JSONArray overloadedLocations = new JSONArray();
        for (int i = 0; i < maximumSize; i++) {
            JSONArray location = new JSONArray();
            location.put(0.0);
            location.put(0.0);
            overloadedLocations.put(location);
        }
        return overloadedLocations;
    }

    @Test
    public void basicPingTest() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().all()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().all()
                .body("any { it.key == 'info' }", is(true))
                .body("any { it.key == 'matrix' }", is(true))
                .statusCode(200);
    }

    @Test
    public void expectUnknownProfile() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-car-123")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    public void expectTooLittleLocationsError() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("minimalLocations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-car-123")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    public void expect4006001() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .when()
                .post(getEndPointPath())
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.MISSING_PARAMETER))
                .statusCode(400);
    }

    @Test
    public void expect4006002() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locationsFaulty"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .when()
                .post(getEndPointPath())
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_FORMAT))
                .statusCode(400);
    }

    @Test
    public void expect4006003() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("sources", getParameter("faultySource"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    //TODO Check how to get the MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM
    // For now it is importing the config module from the ors mein package
    /*@Test
    public void expect4006004() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("maximumLocations"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM))
                .statusCode(400);
    }*/

    @Test
    public void expectResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.query.containsKey('resolve_locations')", is(true))
                .body("matrix[0].destinations[0].containsKey('name')", is(true))
                .body("matrix[0].sources[0].containsKey('name')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDurations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('durations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDistances() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDistance"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('distances')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectWeights() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsWeight"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('weights')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectAllMetrics() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsAll"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('weights')", is(true))
                .body("matrix[0].containsKey('durations')", is(true))
                .body("matrix[0].containsKey('distances')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectInfoItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('attribution')", is(true))
                .body("info.containsKey('service')", is(true))
                .body("info.containsKey('timestamp')", is(true))
                .body("info.containsKey('timestamp')", is(true))
                .body("info.containsKey('query')", is(true))
                .body("info.containsKey('engine')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectEngineItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.engine.containsKey('version')", is(true))
                .body("info.engine.containsKey('build_date')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQuery() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('locations')", is(true))
                .body("info.query.containsKey('profile')", is(true))
                .body("info.query.containsKey('responseType')", is(true))
                .body("info.query.containsKey('profileType')", is(true))
                .body("info.query.containsKey('sources')", is(true))
                .body("info.query.containsKey('destinations')", is(true))
                .body("info.query.containsKey('metrics')", is(true))
                .body("info.query.containsKey('resolve_locations')", is(true))
                .body("info.query.containsKey('flexible_mode')", is(true))
                .body("info.query.containsKey('units')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('locations')", is(true))
                .statusCode(200);

    }

    @Test
    public void expectQueryProfile() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('profile')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryResponseType() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('responseType')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryProfileType() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('profileType')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQuerySources() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('sources')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryDestinations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('destinations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryMetrics() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('metrics')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryUnits() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('units')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('resolve_locations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDestinations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('destinations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDestinationItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].destinations[0].containsKey('location')", is(true))
                .body("matrix[0].destinations[0].containsKey('snapped_distance')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectSources() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].containsKey('sources')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectSourceItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("matrix[0].sources[0].containsKey('location')", is(true))
                .body("matrix[0].sources[0].containsKey('snapped_distance')", is(true))
                .statusCode(200);
    }
}
