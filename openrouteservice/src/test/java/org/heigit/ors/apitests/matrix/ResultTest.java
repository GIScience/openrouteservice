/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.apitests.matrix;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.apitests.utils.HelperFunctions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "matrix")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {
    public static final RestAssuredConfig JSON_CONFIG_DOUBLE_NUMBERS = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));
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

        JSONArray locations5 = new JSONArray();
        coord1 = new JSONArray();
        coord1.put(8.684682);
        coord1.put(49.401961);
        locations5.put(coord1);
        coord2 = new JSONArray();
        coord2.put(8.690518);
        coord2.put(49.405326);
        locations5.put(coord2);
        coord3 = new JSONArray();
        coord3.put(8.690915);
        coord3.put(49.430117);
        locations5.put(coord3);
        coord4 = new JSONArray();
        coord4.put(8.68834);
        coord4.put(49.427758);
        locations5.put(coord4);
        JSONArray coord5 = new JSONArray();
        coord5.put(8.687525);
        coord5.put(49.405437);
        locations5.put(coord5);

        addParameter("locations5", locations5);

        JSONArray locations6 = new JSONArray();
        coord1 = new JSONArray();
        coord1.put(8.684081);
        coord1.put(49.398155);
        locations6.put(coord1);
        coord2 = new JSONArray();
        coord2.put(8.684703);
        coord2.put(49.397359);
        locations6.put(coord2);

        addParameter("locations6", locations6);

        JSONArray locations7 = new JSONArray();
        coord1 = new JSONArray();
        coord1.put(8.703320316580971);
        coord1.put(49.43318333640056);
        locations7.put(coord1);
        coord2 = new JSONArray();
        coord2.put(8.687654576684464);
        coord2.put(49.424556390630144);
        locations7.put(coord2);
        coord3 = new JSONArray();
        coord3.put(8.720827102661133);
        coord3.put(49.450717967273356);
        locations7.put(coord3);
        coord4 = new JSONArray();
        coord4.put(8.708810806274414);
        coord4.put(49.45122015291216);
        locations7.put(coord4);
        addParameter("locations7", locations7);

        // Fake array to test maximum exceedings
        JSONArray maximumLocations = HelperFunctions.fakeJSONLocations(101);
        addParameter("maximumLocations", maximumLocations);
        JSONArray minimalLocations = HelperFunctions.fakeJSONLocations(1);
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
        addParameter("metricsAll", metricsAll);

        JSONArray metricsDuration = new JSONArray();
        metricsDuration.put("duration");
        addParameter("metricsDuration", metricsDuration);

        JSONArray metricsDistance = new JSONArray();
        metricsDistance.put("distance");
        addParameter("metricsDistance", metricsDistance);

        // ID
        addParameter("id", "someID");

        // Profiles
        addParameter("cyclingProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");
    }

    @Test
    void expectTrueResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.query.containsKey('resolve_locations')", is(true))
                .body("metadata.query.resolve_locations", is(true))
                .body("destinations[0].containsKey('name')", is(true))
                .body("destinations[0].name", anyOf(is("Wielandtstraße"), is("Gerhart-Hauptmann-Straße")))
                .body("destinations[1].name", is("Werderplatz"))
                .body("destinations[2].name", is("Roonstraße"))
                .body("sources[0].containsKey('name')", is(true))
                .body("sources[0].name", anyOf(is("Wielandtstraße"), is("Gerhart-Hauptmann-Straße")))
                .body("sources[1].name", is("Werderplatz"))
                .body("sources[2].name", is("Roonstraße"))
                .statusCode(200);
    }

    @Test
    void expectNoResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.query.containsKey('resolve_locations')", is(false))
                .body("destinations[0].containsKey('name')", is(false))
                .body("sources[0].containsKey('name')", is(false))
                .statusCode(200);
    }

    @Test
    void expectDurations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(closeTo(0.0, 0.001)))
                .body("durations[0][1]", is(closeTo(212.67, 2)))
                .body("durations[0][2]", is(closeTo(315.18, 3)))
                .body("durations[1][0]", is(closeTo(211.17, 2)))
                .body("durations[1][1]", is(closeTo(0.0, 0.001)))
                .body("durations[1][2]", is(closeTo(102.53, 1)))
                .body("durations[2][0]", is(closeTo(235.97, 2)))
                .body("durations[2][1]", is(closeTo(90.42, 0.9)))
                .body("durations[2][2]", is(closeTo(0.0, 0.001)))

                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);
    }

    @Test
    void expectDistances() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDistance"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(closeTo(0.0, 0.001)))
                .body("distances[0][1]", is(closeTo(886.14, 9)))
                .body("distances[0][2]", is(closeTo(1365.16f, 13)))
                .body("distances[1][0]", is(closeTo(1171.08, 11)))
                .body("distances[1][1]", is(closeTo(0.0, 0.001)))
                .body("distances[1][2]", is(closeTo(479.08, 5)))
                .body("distances[2][0]", is(closeTo(1274.4, 13)))
                .body("distances[2][1]", is(closeTo(376.77, 4)))
                .body("distances[2][2]", is(closeTo(0.0, 0.001)))
                .statusCode(200);
    }

    @Test
    void expectAllMetrics() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsAll"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(closeTo(0.0, 0.001)))
                .body("durations[0][1]", is(closeTo(212.67, 2)))
                .body("durations[0][2]", is(closeTo(315.18, 3)))
                .body("durations[1][0]", is(closeTo(211.17, 2)))
                .body("durations[1][1]", is(closeTo(0.0, 0.001)))
                .body("durations[1][2]", is(closeTo(102.53, 1)))
                .body("durations[2][0]", is(closeTo(235.97, 2)))
                .body("durations[2][1]", is(closeTo(90.42, 0.9)))
                .body("durations[2][2]", is(closeTo(0.0, 0.001)))

                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(closeTo(0.0, 0.001)))
                .body("distances[0][1]", is(closeTo(886.14, 9)))
                .body("distances[0][2]", is(closeTo(1365.16, 13)))
                .body("distances[1][0]", is(closeTo(1171.08, 11)))
                .body("distances[1][1]", is(closeTo(0.0, 0.001)))
                .body("distances[1][2]", is(closeTo(479.08, 5)))
                .body("distances[2][0]", is(closeTo(1274.4, 12)))
                .body("distances[2][1]", is(closeTo(376.77,4)))
                .body("distances[2][2]", is(closeTo(0.0, 0.001)))

                .statusCode(200);
    }

    @Test
    void expectAllMetricsInKM() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsAll"));
        body.put("units", "km");


        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.query.units", is("km"))

                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(closeTo(0.0, 0.01)))
                .body("durations[0][1]", is(closeTo(212.67, 2)))
                .body("durations[0][2]", is(closeTo(315.18, 3)))
                .body("durations[1][0]", is(closeTo(211.17, 2)))
                .body("durations[1][1]", is(closeTo(0.0, 0.01)))
                .body("durations[1][2]", is(closeTo(102.53, 1)))
                .body("durations[2][0]", is(closeTo(235.97, 2)))
                .body("durations[2][1]", is(closeTo(90.42, 1)))
                .body("durations[2][2]", is(closeTo(0.0, 0)))

                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(closeTo(0.0, 0.01)))
                .body("distances[0][1]", is(closeTo(0.89, 0.009)))
                .body("distances[0][2]", is(closeTo(1.37, 0.01)))
                .body("distances[1][0]", is(closeTo(1.17, 0.01)))
                .body("distances[1][1]", is(closeTo(0.0, 0.01)))
                .body("distances[1][2]", is(closeTo(0.48, 0.005)))
                .body("distances[2][0]", is(closeTo(1.27, 0.01)))
                .body("distances[2][1]", is(closeTo(0.38, 0.004)))
                .body("distances[2][2]", is(closeTo(0.0, 0.01)))

                .statusCode(200);
    }

    @Test
    void expectInfoItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('attribution')", is(true))
                .body("metadata.containsKey('service')", is(true))
                .body("metadata.service", is("matrix"))
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.containsKey('engine')", is(true))
                .statusCode(200);
    }

    @Test
    void expectQueryItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("destinations", getParameter("destination1"));
        body.put("metrics", getParameter("metricsAll"));
        body.put("units", "m");
        body.put("optimized", "false");
        body.put("resolve_locations", "true");
        body.put("sources", getParameter("source1"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('locations')", is(true))
                .body("metadata.query.locations.size()", is(3))
                .body("metadata.query.locations[0][0]", is(8.681495f))
                .body("metadata.query.locations[0][1]", is(49.41461f))
                .body("metadata.query.locations[1][0]", is(8.686507f))
                .body("metadata.query.locations[1][1]", is(49.41943f))
                .body("metadata.query.locations[2][0]", is(8.687872f))
                .body("metadata.query.locations[2][1]", is(49.42032f))
                .body("metadata.query.containsKey('profile')", is(true))
                .body("metadata.query.profile", is("driving-car"))
                .body("metadata.query.containsKey('responseType')", is(true))
                .body("metadata.query.responseType", is("json"))
                .body("metadata.query.containsKey('sources')", is(true))
                .body("metadata.query.sources.size()", is(1))
                .body("metadata.query.sources[0]", is("1"))
                .body("metadata.query.containsKey('destinations')", is(true))
                .body("metadata.query.destinations.size()", is(1))
                .body("metadata.query.destinations[0]", is("1"))
                .body("metadata.query.containsKey('metrics')", is(true))
                .body("metadata.query.metrics.size()", is(2))
                .body("metadata.query.metrics[0]", is("duration"))
                .body("metadata.query.metrics[1]", is("distance"))
                .body("metadata.query.containsKey('resolve_locations')", is(true))
                .body("metadata.query.resolve_locations", is(true))
                .body("metadata.query.containsKey('optimized')", is(false))
                .body("metadata.query.containsKey('units')", is(true))
                .body("metadata.query.units", is("m"))
                .statusCode(200);
    }

    @Test
    void expectLongQueryLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locationsLong"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('locations')", is(true))
                .body("metadata.query.locations.size()", is(4))
                .body("metadata.query.locations[0][0]", is(8.681495f))
                .body("metadata.query.locations[0][1]", is(49.41461f))
                .body("metadata.query.locations[1][0]", is(8.686507f))
                .body("metadata.query.locations[1][1]", is(49.41943f))
                .body("metadata.query.locations[2][0]", is(8.687872f))
                .body("metadata.query.locations[2][1]", is(49.42032f))
                .body("metadata.query.locations[3][0]", is(8.687872f))
                .body("metadata.query.locations[3][1]", is(49.42032f))
                .statusCode(200);
    }

    @Test
    void expectDifferentQueryProfile() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('profile')", is(true))
                .body("metadata.query.profile", is("cycling-regular"))
                .statusCode(200);
    }

    @Test
    void expectKMQueryUnit() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("units", "km");
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('units')", is(true))
                .body("metadata.query.units", is("km"))
                .statusCode(200);
    }

    @Test
    void expectJSONResponseType() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('responseType')", is(true))
                .body("metadata.query.responseType", is("json"))
                .statusCode(200);
    }

    @Test
    void expectDestinationsWithoutResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'destinations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(2))
                .body("destinations[1].size()", is(2))
                .body("destinations[2].size()", is(2))
                .statusCode(200);
    }

    @Test
    void expectDestinationsWithResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(3))
                .body("destinations[1].size()", is(3))
                .body("destinations[2].size()", is(3))
                .statusCode(200);
    }

    @Test
    void expectDestinationItemsWithoutResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(2))
                .body("destinations[0].containsKey('location')", is(true))
                .body("destinations[0].location.size()", is(2))
                .body("destinations[0].location[0]", is(closeTo(8.681495f, 0.001f)))
                .body("destinations[0].location[1]", is(closeTo(49.41461f, 0.001f)))
                .body("destinations[0].containsKey('snapped_distance')", is(true))
                .body("destinations[0].snapped_distance", is(closeTo(0.02f, 0.01f)))

                .body("destinations[1].size()", is(2))
                .body("destinations[1].containsKey('location')", is(true))
                .body("destinations[1].location.size()", is(2))
                .body("destinations[1].location[0]", is(closeTo(8.686507f, 0.001f)))
                .body("destinations[1].location[1]", is(closeTo(49.41943f, 0.001f)))
                .body("destinations[1].containsKey('snapped_distance')", is(true))
                .body("destinations[1].snapped_distance", is(closeTo(0.01f, 0.01f)))

                .body("destinations[2].size()", is(2))
                .body("destinations[2].containsKey('location')", is(true))
                .body("destinations[2].location.size()", is(2))
                .body("destinations[2].location[0]", is(closeTo(8.687872f, 0.001f)))
                .body("destinations[2].location[1]", is(closeTo(49.420318f, 0.001f)))
                .body("destinations[2].containsKey('snapped_distance')", is(true))
                .body("destinations[2].snapped_distance", is(closeTo(0.5f, 0.01f)))
                .statusCode(200);
    }

    @Test
    void expectDestinationItemsWithResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(3))
                .body("destinations[0].containsKey('location')", is(true))
                .body("destinations[0].location.size()", is(2))
                .body("destinations[0].location[0]", is(closeTo(8.681495f, 0.001f)))
                .body("destinations[0].location[1]", is(closeTo(49.41461f, 0.001f)))
                .body("destinations[0].containsKey('name')", is(true))
                .body("destinations[0].name", anyOf(is("Wielandtstraße"), is("Gerhart-Hauptmann-Straße")))
                .body("destinations[0].containsKey('snapped_distance')", is(true))
                .body("destinations[0].snapped_distance", is(closeTo(0.02f, 0.01f)))

                .body("destinations[1].size()", is(3))
                .body("destinations[1].containsKey('location')", is(true))
                .body("destinations[1].location.size()", is(2))
                .body("destinations[1].location[0]", is(closeTo(8.686507f, 0.001f)))
                .body("destinations[1].location[1]", is(closeTo(49.41943f, 0.001f)))
                .body("destinations[1].containsKey('name')", is(true))
                .body("destinations[1].name", is("Werderplatz"))
                .body("destinations[1].containsKey('snapped_distance')", is(true))
                .body("destinations[1].snapped_distance", is(closeTo(0.01f, 0.01f)))

                .body("destinations[2].size()", is(3))
                .body("destinations[2].containsKey('location')", is(true))
                .body("destinations[2].location.size()", is(2))
                .body("destinations[2].location[0]", is(closeTo(8.687872f, 0.001f)))
                .body("destinations[2].location[1]", is(closeTo(49.420318f, 0.001f)))
                .body("destinations[2].containsKey('name')", is(true))
                .body("destinations[2].name", is("Roonstraße"))
                .body("destinations[2].containsKey('snapped_distance')", is(true))
                .body("destinations[2].snapped_distance", is(closeTo(0.5f, 0.01f)))
                .statusCode(200);
    }

    @Test
    void expectSourcesItemsWithoutResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("containsKey('sources')", is(true))
                .body("sources.size()", is(3))
                .body("sources[0].size()", is(2))
                .body("sources[0].containsKey('location')", is(true))
                .body("sources[0].location.size()", is(2))
                .body("sources[0].location[0]", is(closeTo(8.681495f, 0.001f)))
                .body("sources[0].location[1]", is(closeTo(49.41461f, 0.001f)))
                .body("sources[0].containsKey('snapped_distance')", is(true))
                .body("sources[0].snapped_distance", is(closeTo(0.02f, 0.01f)))

                .body("sources[1].size()", is(2))
                .body("sources[1].containsKey('location')", is(true))
                .body("sources[1].location.size()", is(2))
                .body("sources[1].location[0]", is(closeTo(8.686507f, 0.001f)))
                .body("sources[1].location[1]", is(closeTo(49.41943f, 0.001f)))
                .body("sources[1].containsKey('snapped_distance')", is(true))
                .body("sources[1].snapped_distance", is(closeTo(0.01f, 0.01f)))

                .body("sources[2].size()", is(2))
                .body("sources[2].containsKey('location')", is(true))
                .body("sources[2].location.size()", is(2))
                .body("sources[2].location[0]", is(closeTo(8.687872f, 0.001f)))
                .body("sources[2].location[1]", is(closeTo(49.420318f, 0.001f)))
                .body("sources[2].containsKey('snapped_distance')", is(true))
                .body("sources[2].snapped_distance", is(closeTo(0.5f, 0.01f)))
                .statusCode(200);
    }

    @Test
    void expectSourcesItemsWithResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'sources' }", is(true))
                .body("sources.size()", is(3))
                .body("sources[0].size()", is(3))
                .body("sources[0].containsKey('location')", is(true))
                .body("sources[0].location.size()", is(2))
                .body("sources[0].location[0]", is(closeTo(8.681495f, 0.001f)))
                .body("sources[0].location[1]", is(closeTo(49.41461f, 0.001f)))
                .body("sources[0].containsKey('name')", is(true))
                .body("sources[0].name", anyOf(is("Wielandtstraße"), is("Gerhart-Hauptmann-Straße")))
                .body("sources[0].containsKey('snapped_distance')", is(true))
                .body("sources[0].snapped_distance", is(closeTo(0.02f, 0.01f)))

                .body("sources[1].size()", is(3))
                .body("sources[1].containsKey('location')", is(true))
                .body("sources[1].location.size()", is(2))
                .body("sources[1].location[0]", is(closeTo(8.686507f, 0.001f)))
                .body("sources[1].location[1]", is(closeTo(49.41943f, 0.001f)))
                .body("sources[1].containsKey('name')", is(true))
                .body("sources[1].name", is("Werderplatz"))
                .body("sources[1].containsKey('snapped_distance')", is(true))
                .body("sources[1].snapped_distance", is(closeTo(0.01f, 0.01f)))

                .body("sources[2].size()", is(3))
                .body("sources[2].containsKey('location')", is(true))
                .body("sources[2].location.size()", is(2))
                .body("sources[2].location[0]", is(closeTo(8.687872f, 0.001f)))
                .body("sources[2].location[1]", is(closeTo(49.420318f, 0.001f)))
                .body("sources[2].containsKey('name')", is(true))
                .body("sources[2].name", is("Roonstraße"))
                .body("sources[2].containsKey('snapped_distance')", is(true))
                .body("sources[2].snapped_distance", is(closeTo(0.5f, 0.01f)))
                .statusCode(200);
    }

    @Test
    void testIdInSummary() {
        JSONObject body = new JSONObject();body.put("locations", getParameter("locations"));
        body.put("id", "request123");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))
                .statusCode(200);
    }

    @Test
    void testDefinedSources() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("sources", new JSONArray(new int[] {1,2}));
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(2))
                .body("durations[0].size()", is(3))
                .body("durations[0][0]", is(closeTo(211.17, 2)))
                .body("durations[0][1]", is(closeTo(0.0, 0.01)))
                .body("durations[0][2]", is(closeTo(102.53, 1)))
                .body("durations[1][0]", is(closeTo(235.97, 2)))
                .body("durations[1][1]", is(closeTo(90.42, 1)))
                .body("durations[1][2]", is(closeTo(0.0, 0)))
                .statusCode(200);
    }

    @Test
    void testDefinedDestinations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("destinations", new JSONArray(new int[] {1,2}));
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0].size()", is(2))
                .body("durations[0][0]", is(closeTo(212.67, 2)))
                .body("durations[0][1]", is(closeTo(315.18, 3)))
                .body("durations[1][0]", is(closeTo(0.0, 0.01)))
                .body("durations[1][1]", is(closeTo(102.53, 1)))
                .body("durations[2][0]", is(closeTo(90.42, 1)))
                .body("durations[2][1]", is(closeTo(0.0, 0.01)))
                .statusCode(200);
    }

    @Test
    void testDefinedSourcesAndDestinations() {

        JSONObject body = new JSONObject();

        body.put("locations", getParameter("locations5"));
        body.put("sources", new JSONArray(new int[] {0,1}));
        body.put("destinations", new JSONArray(new int[] {2,3,4}));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'destinations' }", is(true))
                .body("any { it.key == 'sources' }", is(true))
                .body("destinations.size()", is(3))
                .body("sources.size()", is(2))
                .body("destinations[0].snapped_distance", is(closeTo(4.18, 0.5)))
                .body("destinations[1].snapped_distance", is(closeTo(2.42, 0.5)))
                .body("destinations[2].snapped_distance", is(closeTo(7.11, 0.7)))
                .body("sources[0].snapped_distance", is(closeTo(8.98, 0.09)))
                .body("sources[1].snapped_distance", is(closeTo(7.87, 0.08)))
                .statusCode(200);
    }

    @Test
    void expectTurnRestrictionDurations() {
        JSONObject body = new JSONObject();
        JSONObject options = new JSONObject();
        body.put("locations", getParameter("locations6"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDuration"));
        body.put("options", options.put("dynamic_speeds", true));// enforce use of CALT over CH


        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(2))
                .body("durations[0][0]", is(closeTo(0.0f, 0f)))
                .body("durations[0][1]", is(closeTo(126.46f, 0.5f)))
                .body("durations[1][0]", is(closeTo(48.25f, 0.5f)))
                .body("durations[1][1]", is(closeTo(0.0f, 0f)))
                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);
    }

    @Test
    void testCrossVirtualNode() {
        JSONObject body = new JSONObject();
        JSONObject options = new JSONObject();
        body.put("locations", getParameter("locations7"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsDuration"));
        body.put("options", options.put("dynamic_speeds", true));// enforce use of CALT over CH


        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(4))
                .body("durations[0][0]", is(closeTo(0.0f, 0.1f)))
                .body("durations[0][1]", is(closeTo(610.8f, 1.0f)))
                .body("durations[0][2]", is(closeTo(1705.5f, 1.0f)))
                .body("durations[0][3]", is(closeTo(1494.7f, 1.0f)))
                .body("durations[1][0]", is(closeTo(560.3f, 1.0f)))
                .body("durations[1][1]", is(closeTo(0.0f, 1.0f)))
                .body("durations[1][2]", is(closeTo(1219.2, 1.0f)))
                .body("durations[1][3]", is(closeTo(1008.3f, 1.0f)))
                .body("durations[2][0]", is(closeTo(1678.2f, 1.0f)))
                .body("durations[2][1]", is(closeTo(1212.6f, 1.0f)))
                .body("durations[2][2]", is(closeTo(0.0f, 1.0f)))
                .body("durations[2][3]", is(closeTo(210.5f, 1.0f)))
                .body("durations[3][0]", is(closeTo(1467.4f, 1.0f)))
                .body("durations[3][1]", is(closeTo(1001.8f, 1.0f)))
                .body("durations[3][2]", is(closeTo(210.8f, 1.0f)))
                .body("durations[3][3]", is(closeTo(0.0f, 1.0f)))
                .statusCode(200);
    }
}
