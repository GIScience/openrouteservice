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
public class ResultTest extends ServiceTest {
    public ResultTest() {
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
        JSONArray locationsLong = new JSONArray();
        coord1 = new JSONArray();
        coord1.put(8.681495);
        coord1.put(49.41461);
        locationsLong.put(coord1);
        coord2 = new JSONArray();
        coord2.put(8.686507);
        coord2.put(49.41943);
        locationsLong.put(coord2);
        coord3 = new JSONArray();
        coord3.put(8.687872);
        coord3.put(49.420318);
        locationsLong.put(coord3);
        JSONArray coord4 = new JSONArray();
        coord4.put(8.787872);
        coord4.put(49.620318);
        locationsLong.put(coord3);
        addParameter("locationsLong", locationsLong);

        /*// Fake array to test maximum exceedings
        JSONArray maximumLocations = fakeLocations(MatrixServiceSettings.getMaximumLocations(false) + 1);
        addParameter("maximumLocations", maximumLocations);
        JSONArray minimalLocations = fakeLocations(1);
        addParameter("minimalLocations", minimalLocations);*/
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

    @Test
    public void expectTrueResolveLocations() {
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
                .body("info.query.resolve_locations", is(true))
                .body("matrix[0].destinations[0].containsKey('name')", is(true))
                .body("matrix[0].destinations[0].name", is("Wielandtstraße"))
                .body("matrix[0].destinations[1].name", is("Werderplatz"))
                .body("matrix[0].destinations[2].name", is("Roonstraße"))
                .body("matrix[0].sources[0].containsKey('name')", is(true))
                .body("matrix[0].sources[0].name", is("Wielandtstraße"))
                .body("matrix[0].sources[1].name", is("Werderplatz"))
                .body("matrix[0].sources[2].name", is("Roonstraße"))
                .statusCode(200);
    }

    @Test
    public void expectNoResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", false);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.query.containsKey('resolve_locations')", is(false))
                .body("matrix[0].destinations[0].containsKey('name')", is(false))
                .body("matrix[0].sources[0].containsKey('name')", is(false))
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
                .body("any { it.key == 'matrix' }", is(true))
                .body("matrix[0].containsKey('durations')", is(true))
                /*          [0.0, 212.67, 315.18, 211.17, 0.0, 102.53, 235.97, 90.42, 0.0]*/
                .body("matrix[0].durations[0]", is(0.0f))
                .body("matrix[0].durations[1]", is(212.67f))
                .body("matrix[0].durations[2]", is(315.18f))
                .body("matrix[0].durations[3]", is(211.17f))
                .body("matrix[0].durations[4]", is(0.0f))
                .body("matrix[0].durations[5]", is(102.53f))
                .body("matrix[0].durations[6]", is(235.97f))
                .body("matrix[0].durations[7]", is(90.42f))
                .body("matrix[0].durations[8]", is(0.0f))
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
                .body("any { it.key == 'matrix' }", is(true))
                .body("matrix[0].containsKey('distances')", is(true))
                /*          [0.0, 886.14, 1365.15, 1171.07, 0.0, 479.07, 1274.4, 376.77, 0.0]*/
                .body("matrix[0].distances[0]", is(0.0f))
                .body("matrix[0].distances[1]", is(886.14f))
                .body("matrix[0].distances[2]", is(1365.15f))
                .body("matrix[0].distances[3]", is(1171.07f))
                .body("matrix[0].distances[4]", is(0.0f))
                .body("matrix[0].distances[5]", is(479.07f))
                .body("matrix[0].distances[6]", is(1274.4f))
                .body("matrix[0].distances[7]", is(376.77f))
                .body("matrix[0].distances[8]", is(0.0f))
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
                .body("any { it.key == 'matrix' }", is(true))
                .body("matrix[0].containsKey('weights')", is(true))
                /*          [0.0, 212.67, 315.19, 211.18, 0.0, 102.53, 235.98, 90.42, 0.0]*/
                .body("matrix[0].weights[0]", is(0.0f))
                .body("matrix[0].weights[1]", is(212.67f))
                .body("matrix[0].weights[2]", is(315.19f))
                .body("matrix[0].weights[3]", is(211.18f))
                .body("matrix[0].weights[4]", is(0.0f))
                .body("matrix[0].weights[5]", is(102.53f))
                .body("matrix[0].weights[6]", is(235.98f))
                .body("matrix[0].weights[7]", is(90.42f))
                .body("matrix[0].weights[8]", is(0.0f))
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
                .body("any { it.key == 'matrix' }", is(true))
                .body("matrix[0].containsKey('durations')", is(true))
                /*          [0.0, 212.67, 315.18, 211.17, 0.0, 102.53, 235.97, 90.42, 0.0]*/
                .body("matrix[0].durations[0]", is(0.0f))
                .body("matrix[0].durations[1]", is(212.67f))
                .body("matrix[0].durations[2]", is(315.18f))
                .body("matrix[0].durations[3]", is(211.17f))
                .body("matrix[0].durations[4]", is(0.0f))
                .body("matrix[0].durations[5]", is(102.53f))
                .body("matrix[0].durations[6]", is(235.97f))
                .body("matrix[0].durations[7]", is(90.42f))
                .body("matrix[0].durations[8]", is(0.0f))
                .body("matrix[0].containsKey('distances')", is(true))
                /*          [0.0, 886.14, 1365.15, 1171.07, 0.0, 479.07, 1274.4, 376.77, 0.0]*/
                .body("matrix[0].distances[0]", is(0.0f))
                .body("matrix[0].distances[1]", is(886.14f))
                .body("matrix[0].distances[2]", is(1365.15f))
                .body("matrix[0].distances[3]", is(1171.07f))
                .body("matrix[0].distances[4]", is(0.0f))
                .body("matrix[0].distances[5]", is(479.07f))
                .body("matrix[0].distances[6]", is(1274.4f))
                .body("matrix[0].distances[7]", is(376.77f))
                .body("matrix[0].distances[8]", is(0.0f))
                .body("matrix[0].containsKey('weights')", is(true))
                /*          [0.0, 212.67, 315.19, 211.18, 0.0, 102.53, 235.98, 90.42, 0.0]*/
                .body("matrix[0].weights[0]", is(0.0f))
                .body("matrix[0].weights[1]", is(212.67f))
                .body("matrix[0].weights[2]", is(315.19f))
                .body("matrix[0].weights[3]", is(211.18f))
                .body("matrix[0].weights[4]", is(0.0f))
                .body("matrix[0].weights[5]", is(102.53f))
                .body("matrix[0].weights[6]", is(235.98f))
                .body("matrix[0].weights[7]", is(90.42f))
                .body("matrix[0].weights[8]", is(0.0f))
                .statusCode(200);
    }

    @Test
    public void expectAllMetricsInKM() {
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
                .body("any { it.key == 'matrix' }", is(true))
                .body("matrix[0].containsKey('durations')", is(true))
                /*          [0.0, 212.67, 315.18, 211.17, 0.0, 102.53, 235.97, 90.42, 0.0]*/
                .body("matrix[0].durations[0]", is(0.0f))
                .body("matrix[0].durations[1]", is(212.67f))
                .body("matrix[0].durations[2]", is(315.18f))
                .body("matrix[0].durations[3]", is(211.17f))
                .body("matrix[0].durations[4]", is(0.0f))
                .body("matrix[0].durations[5]", is(102.53f))
                .body("matrix[0].durations[6]", is(235.97f))
                .body("matrix[0].durations[7]", is(90.42f))
                .body("matrix[0].durations[8]", is(0.0f))
                .body("matrix[0].containsKey('distances')", is(true))
                /*          [0.0, 886.14, 1365.15, 1171.07, 0.0, 479.07, 1274.4, 376.77, 0.0]*/
                .body("matrix[0].distances[0]", is(0.0f))
                .body("matrix[0].distances[1]", is(886.14f))
                .body("matrix[0].distances[2]", is(1365.15f))
                .body("matrix[0].distances[3]", is(1171.07f))
                .body("matrix[0].distances[4]", is(0.0f))
                .body("matrix[0].distances[5]", is(479.07f))
                .body("matrix[0].distances[6]", is(1274.4f))
                .body("matrix[0].distances[7]", is(376.77f))
                .body("matrix[0].distances[8]", is(0.0f))
                .body("matrix[0].containsKey('weights')", is(true))
                /*          [0.0, 212.67, 315.19, 211.18, 0.0, 102.53, 235.98, 90.42, 0.0]*/
                .body("matrix[0].weights[0]", is(0.0f))
                .body("matrix[0].weights[1]", is(212.67f))
                .body("matrix[0].weights[2]", is(315.19f))
                .body("matrix[0].weights[3]", is(211.18f))
                .body("matrix[0].weights[4]", is(0.0f))
                .body("matrix[0].weights[5]", is(102.53f))
                .body("matrix[0].weights[6]", is(235.98f))
                .body("matrix[0].weights[7]", is(90.42f))
                .body("matrix[0].weights[8]", is(0.0f))
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
                .body("info.service", is("matrix"))
                .body("info.containsKey('timestamp')", is(true))
                .body("info.containsKey('query')", is(true))
                .body("info.containsKey('engine')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("metrics", getParameter("metricsAll"));
        body.put("units", "m");
        body.put("resolve_locations", "true");
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
                .body("info.query.locations.size()", is(3))
                .body("info.query.locations[0][0]", is(8.681495f))
                .body("info.query.locations[0][1]", is(49.41461f))
                .body("info.query.locations[1][0]", is(8.686507f))
                .body("info.query.locations[1][1]", is(49.41943f))
                .body("info.query.locations[2][0]", is(8.687872f))
                .body("info.query.locations[2][1]", is(49.42032f))
                .body("info.query.containsKey('profile')", is(true))
                .body("info.query.profile", is("driving-car"))
                .body("info.query.containsKey('responseType')", is(true))
                .body("info.query.responseType", is("json"))
                .body("info.query.containsKey('sources')", is(true))
                .body("info.query.sources.size()", is(1))
                .body("info.query.sources[0]", is("all"))
                .body("info.query.containsKey('destinations')", is(true))
                .body("info.query.destinations.size()", is(1))
                .body("info.query.destinations[0]", is("all"))
                .body("info.query.containsKey('metrics')", is(true))
                .body("info.query.metrics.size()", is(3))
                .body("info.query.metrics[0]", is("duration"))
                .body("info.query.metrics[1]", is("distance"))
                .body("info.query.metrics[2]", is("weight"))
                .body("info.query.containsKey('resolve_locations')", is(true))
                .body("info.query.resolve_locations", is(true))
                .body("info.query.containsKey('optimized')", is(false))
                .body("info.query.containsKey('units')", is(true))
                .body("info.query.units", is("m"))
                .statusCode(200);
    }

    @Test
    public void expectLongQueryLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locationsLong"));
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
                .body("info.query.locations.size()", is(4))
                .body("info.query.locations[0][0]", is(8.681495f))
                .body("info.query.locations[0][1]", is(49.41461f))
                .body("info.query.locations[1][0]", is(8.686507f))
                .body("info.query.locations[1][1]", is(49.41943f))
                .body("info.query.locations[2][0]", is(8.687872f))
                .body("info.query.locations[2][1]", is(49.42032f))
                .body("info.query.locations[3][0]", is(8.687872f))
                .body("info.query.locations[3][1]", is(49.42032f))
                .statusCode(200);
    }

    @Test
    public void expectDifferentQueryProfile() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("info.containsKey('query')", is(true))
                .body("info.query.containsKey('profile')", is(true))
                .body("info.query.profile", is("cycling-regular"))
                .statusCode(200);
    }

    @Test
    public void expectKMQueryUnit() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("units", "km");
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
                .body("info.query.units", is("km"))
                .statusCode(200);
    }

    @Test
    public void expectJSONResponseType() {
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
                .body("info.query.responseType", is("json"))
                .statusCode(200);
    }

    @Test
    public void expectDestinationsWithoutResolveLocations() {
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
                .body("matrix[0].destinations.size()", is(3))
                .body("matrix[0].destinations[0].size()", is(2))
                .body("matrix[0].destinations[1].size()", is(2))
                .body("matrix[0].destinations[2].size()", is(2))
                .statusCode(200);
    }

    @Test
    public void expectDestinationsWithResolveLocations() {
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
                .body("matrix[0].containsKey('destinations')", is(true))
                .body("matrix[0].destinations.size()", is(3))
                .body("matrix[0].destinations[0].size()", is(3))
                .body("matrix[0].destinations[1].size()", is(3))
                .body("matrix[0].destinations[2].size()", is(3))
                .statusCode(200);
    }

    @Test
    public void expectDestinationItemsWithoutResolveLocations() {
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
                .body("matrix[0].destinations.size()", is(3))
                .body("matrix[0].destinations[0].size()", is(2))
                .body("matrix[0].destinations[0].containsKey('location')", is(true))
                .body("matrix[0].destinations[0].location.size()", is(2))
                .body("matrix[0].destinations[0].location[0]", is(8.681495f))
                .body("matrix[0].destinations[0].location[1]", is(49.41461f))
                .body("matrix[0].destinations[0].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[0].snapped_distance", is(0.02f))

                .body("matrix[0].destinations[1].size()", is(2))
                .body("matrix[0].destinations[1].containsKey('location')", is(true))
                .body("matrix[0].destinations[1].location.size()", is(2))
                .body("matrix[0].destinations[1].location[0]", is(8.686507f))
                .body("matrix[0].destinations[1].location[1]", is(49.41943f))
                .body("matrix[0].destinations[1].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[1].snapped_distance", is(0.02f))

                .body("matrix[0].destinations[2].size()", is(2))
                .body("matrix[0].destinations[2].containsKey('location')", is(true))
                .body("matrix[0].destinations[2].location.size()", is(2))
                .body("matrix[0].destinations[2].location[0]", is(8.687872f))
                .body("matrix[0].destinations[2].location[1]", is(49.420318f))
                .body("matrix[0].destinations[2].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[2].snapped_distance", is(0.05f))
                .statusCode(200);
    }

    @Test
    public void expectDestinationItemsWithResolveLocations() {
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
                .body("matrix[0].containsKey('destinations')", is(true))
                .body("matrix[0].destinations.size()", is(3))
                .body("matrix[0].destinations[0].size()", is(3))
                .body("matrix[0].destinations[0].containsKey('location')", is(true))
                .body("matrix[0].destinations[0].location.size()", is(2))
                .body("matrix[0].destinations[0].location[0]", is(8.681495f))
                .body("matrix[0].destinations[0].location[1]", is(49.41461f))
                .body("matrix[0].destinations[0].containsKey('name')", is(true))
                .body("matrix[0].destinations[0].name", is("Wielandtstraße"))
                .body("matrix[0].destinations[0].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[0].snapped_distance", is(0.02f))

                .body("matrix[0].destinations[1].size()", is(3))
                .body("matrix[0].destinations[1].containsKey('location')", is(true))
                .body("matrix[0].destinations[1].location.size()", is(2))
                .body("matrix[0].destinations[1].location[0]", is(8.686507f))
                .body("matrix[0].destinations[1].location[1]", is(49.41943f))
                .body("matrix[0].destinations[1].containsKey('name')", is(true))
                .body("matrix[0].destinations[1].name", is("Werderplatz"))
                .body("matrix[0].destinations[1].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[1].snapped_distance", is(0.02f))

                .body("matrix[0].destinations[2].size()", is(3))
                .body("matrix[0].destinations[2].containsKey('location')", is(true))
                .body("matrix[0].destinations[2].location.size()", is(2))
                .body("matrix[0].destinations[2].location[0]", is(8.687872f))
                .body("matrix[0].destinations[2].location[1]", is(49.420318f))
                .body("matrix[0].destinations[2].containsKey('name')", is(true))
                .body("matrix[0].destinations[2].name", is("Roonstraße"))
                .body("matrix[0].destinations[2].containsKey('snapped_distance')", is(true))
                .body("matrix[0].destinations[2].snapped_distance", is(0.05f))
                .statusCode(200);
    }

    @Test
    public void expectSourcesItemsWithoutResolveLocations() {
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
                .body("matrix[0].sources.size()", is(3))
                .body("matrix[0].sources[0].size()", is(2))
                .body("matrix[0].sources[0].containsKey('location')", is(true))
                .body("matrix[0].sources[0].location.size()", is(2))
                .body("matrix[0].sources[0].location[0]", is(8.681495f))
                .body("matrix[0].sources[0].location[1]", is(49.41461f))
                .body("matrix[0].sources[0].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[0].snapped_distance", is(0.02f))

                .body("matrix[0].sources[1].size()", is(2))
                .body("matrix[0].sources[1].containsKey('location')", is(true))
                .body("matrix[0].sources[1].location.size()", is(2))
                .body("matrix[0].sources[1].location[0]", is(8.686507f))
                .body("matrix[0].sources[1].location[1]", is(49.41943f))
                .body("matrix[0].sources[1].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[1].snapped_distance", is(0.02f))

                .body("matrix[0].sources[2].size()", is(2))
                .body("matrix[0].sources[2].containsKey('location')", is(true))
                .body("matrix[0].sources[2].location.size()", is(2))
                .body("matrix[0].sources[2].location[0]", is(8.687872f))
                .body("matrix[0].sources[2].location[1]", is(49.420318f))
                .body("matrix[0].sources[2].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[2].snapped_distance", is(0.05f))
                .statusCode(200);
    }

    @Test
    public void expectSourcesItemsWithResolveLocations() {
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
                .body("matrix[0].containsKey('sources')", is(true))
                .body("matrix[0].sources.size()", is(3))
                .body("matrix[0].sources[0].size()", is(3))
                .body("matrix[0].sources[0].containsKey('location')", is(true))
                .body("matrix[0].sources[0].location.size()", is(2))
                .body("matrix[0].sources[0].location[0]", is(8.681495f))
                .body("matrix[0].sources[0].location[1]", is(49.41461f))
                .body("matrix[0].sources[0].containsKey('name')", is(true))
                .body("matrix[0].sources[0].name", is("Wielandtstraße"))
                .body("matrix[0].sources[0].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[0].snapped_distance", is(0.02f))

                .body("matrix[0].sources[1].size()", is(3))
                .body("matrix[0].sources[1].containsKey('location')", is(true))
                .body("matrix[0].sources[1].location.size()", is(2))
                .body("matrix[0].sources[1].location[0]", is(8.686507f))
                .body("matrix[0].sources[1].location[1]", is(49.41943f))
                .body("matrix[0].sources[1].containsKey('name')", is(true))
                .body("matrix[0].sources[1].name", is("Werderplatz"))
                .body("matrix[0].sources[1].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[1].snapped_distance", is(0.02f))

                .body("matrix[0].sources[2].size()", is(3))
                .body("matrix[0].sources[2].containsKey('location')", is(true))
                .body("matrix[0].sources[2].location.size()", is(2))
                .body("matrix[0].sources[2].location[0]", is(8.687872f))
                .body("matrix[0].sources[2].location[1]", is(49.420318f))
                .body("matrix[0].sources[2].containsKey('name')", is(true))
                .body("matrix[0].sources[2].name", is("Roonstraße"))
                .body("matrix[0].sources[2].containsKey('snapped_distance')", is(true))
                .body("matrix[0].sources[2].snapped_distance", is(0.05f))
                .statusCode(200);
    }
}
