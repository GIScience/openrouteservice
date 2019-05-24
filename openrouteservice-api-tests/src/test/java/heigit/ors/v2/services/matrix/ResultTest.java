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
package heigit.ors.v2.services.matrix;

import heigit.ors.v2.services.common.EndPointAnnotation;
import heigit.ors.v2.services.common.ServiceTest;
import heigit.ors.v2.services.common.VersionAnnotation;
import heigit.ors.v2.services.serviceSettings.MatrixServiceSettings;
import heigit.ors.v2.services.utils.HelperFunctions;
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

        // Fake array to test maximum exceedings
        JSONArray maximumLocations = HelperFunctions.fakeJSONLocations(MatrixServiceSettings.getMaximumRoutes(false) + 1);
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
                .body("metadata.query.containsKey('resolve_locations')", is(true))
                .body("metadata.query.resolve_locations", is(true))
                .body("destinations[0].containsKey('name')", is(true))
                .body("destinations[0].name", is("Wielandtstraße"))
                .body("destinations[1].name", is("Werderplatz"))
                .body("destinations[2].name", is("Roonstraße"))
                .body("sources[0].containsKey('name')", is(true))
                .body("sources[0].name", is("Wielandtstraße"))
                .body("sources[1].name", is("Werderplatz"))
                .body("sources[2].name", is("Roonstraße"))
                .statusCode(200);
    }

    @Test
    public void expectNoResolveLocations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().all()
                .assertThat()
                .body("metadata.query.containsKey('resolve_locations')", is(false))
                .body("destinations[0].containsKey('name')", is(false))
                .body("sources[0].containsKey('name')", is(false))
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
                .when().log().all()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().all()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(0.0f))
                .body("durations[0][1]", is(212.67f))
                .body("durations[0][2]", is(315.18f))
                .body("durations[1][0]", is(211.17f))
                .body("durations[1][1]", is(0.0f))
                .body("durations[1][2]", is(102.53f))
                .body("durations[2][0]", is(235.97f))
                .body("durations[2][1]", is(90.42f))
                .body("durations[2][2]", is(0.0f))
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
                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(0.0f))
                .body("distances[0][1]", is(886.14f))
                .body("distances[0][2]", is(1365.15f))
                .body("distances[1][0]", is(1171.07f))
                .body("distances[1][1]", is(0.0f))
                .body("distances[1][2]", is(479.07f))
                .body("distances[2][0]", is(1274.4f))
                .body("distances[2][1]", is(376.77f))
                .body("distances[2][2]", is(0.0f))
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
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(0.0f))
                .body("durations[0][1]", is(212.67f))
                .body("durations[0][2]", is(315.18f))
                .body("durations[1][0]", is(211.17f))
                .body("durations[1][1]", is(0.0f))
                .body("durations[1][2]", is(102.53f))
                .body("durations[2][0]", is(235.97f))
                .body("durations[2][1]", is(90.42f))
                .body("durations[2][2]", is(0.0f))

                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(0.0f))
                .body("distances[0][1]", is(886.14f))
                .body("distances[0][2]", is(1365.15f))
                .body("distances[1][0]", is(1171.07f))
                .body("distances[1][1]", is(0.0f))
                .body("distances[1][2]", is(479.07f))
                .body("distances[2][0]", is(1274.4f))
                .body("distances[2][1]", is(376.77f))
                .body("distances[2][2]", is(0.0f))

                .statusCode(200);
    }

    @Test
    public void expectAllMetricsInKM() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("resolve_locations", true);
        body.put("metrics", getParameter("metricsAll"));
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
                .body("metadata.query.units", is("km"))

                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0][0]", is(0.0f))
                .body("durations[0][1]", is(212.67f))
                .body("durations[0][2]", is(315.18f))
                .body("durations[1][0]", is(211.17f))
                .body("durations[1][1]", is(0.0f))
                .body("durations[1][2]", is(102.53f))
                .body("durations[2][0]", is(235.97f))
                .body("durations[2][1]", is(90.42f))
                .body("durations[2][2]", is(0.0f))

                .body("any { it.key == 'distances' }", is(true))
                .body("distances.size()", is(3))
                .body("distances[0][0]", is(0.0f))
                .body("distances[0][1]", is(0.89f))
                .body("distances[0][2]", is(1.37f))
                .body("distances[1][0]", is(1.17f))
                .body("distances[1][1]", is(0.0f))
                .body("distances[1][2]", is(0.48f))
                .body("distances[2][0]", is(1.27f))
                .body("distances[2][1]", is(0.38f))
                .body("distances[2][2]", is(0.0f))

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
                .body("metadata.containsKey('attribution')", is(true))
                .body("metadata.containsKey('service')", is(true))
                .body("metadata.service", is("matrix"))
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.containsKey('engine')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryItems() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("destinations", getParameter("destination1"));
        body.put("metrics", getParameter("metricsAll"));
        body.put("units", "m");
        body.put("optimized", "false");
        body.put("resolve_locations", "true");
        body.put("sources", getParameter("source1"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('profile')", is(true))
                .body("metadata.query.profile", is("cycling-regular"))
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('units')", is(true))
                .body("metadata.query.units", is("km"))
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('responseType')", is(true))
                .body("metadata.query.responseType", is("json"))
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
                .when().log().all()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().all()
                .assertThat()
                .body("any { it.key == 'destinations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(2))
                .body("destinations[1].size()", is(2))
                .body("destinations[2].size()", is(2))
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
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(3))
                .body("destinations[1].size()", is(3))
                .body("destinations[2].size()", is(3))
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
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(2))
                .body("destinations[0].containsKey('location')", is(true))
                .body("destinations[0].location.size()", is(2))
                .body("destinations[0].location[0]", is(8.681495f))
                .body("destinations[0].location[1]", is(49.41461f))
                .body("destinations[0].containsKey('snapped_distance')", is(true))
                .body("destinations[0].snapped_distance", is(0.02f))

                .body("destinations[1].size()", is(2))
                .body("destinations[1].containsKey('location')", is(true))
                .body("destinations[1].location.size()", is(2))
                .body("destinations[1].location[0]", is(8.686507f))
                .body("destinations[1].location[1]", is(49.41943f))
                .body("destinations[1].containsKey('snapped_distance')", is(true))
                .body("destinations[1].snapped_distance", is(0.02f))

                .body("destinations[2].size()", is(2))
                .body("destinations[2].containsKey('location')", is(true))
                .body("destinations[2].location.size()", is(2))
                .body("destinations[2].location[0]", is(8.687872f))
                .body("destinations[2].location[1]", is(49.420318f))
                .body("destinations[2].containsKey('snapped_distance')", is(true))
                .body("destinations[2].snapped_distance", is(0.05f))
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
                .body("any { it.key == 'durations' }", is(true))
                .body("destinations.size()", is(3))
                .body("destinations[0].size()", is(3))
                .body("destinations[0].containsKey('location')", is(true))
                .body("destinations[0].location.size()", is(2))
                .body("destinations[0].location[0]", is(8.681495f))
                .body("destinations[0].location[1]", is(49.41461f))
                .body("destinations[0].containsKey('name')", is(true))
                .body("destinations[0].name", is("Wielandtstraße"))
                .body("destinations[0].containsKey('snapped_distance')", is(true))
                .body("destinations[0].snapped_distance", is(0.02f))

                .body("destinations[1].size()", is(3))
                .body("destinations[1].containsKey('location')", is(true))
                .body("destinations[1].location.size()", is(2))
                .body("destinations[1].location[0]", is(8.686507f))
                .body("destinations[1].location[1]", is(49.41943f))
                .body("destinations[1].containsKey('name')", is(true))
                .body("destinations[1].name", is("Werderplatz"))
                .body("destinations[1].containsKey('snapped_distance')", is(true))
                .body("destinations[1].snapped_distance", is(0.02f))

                .body("destinations[2].size()", is(3))
                .body("destinations[2].containsKey('location')", is(true))
                .body("destinations[2].location.size()", is(2))
                .body("destinations[2].location[0]", is(8.687872f))
                .body("destinations[2].location[1]", is(49.420318f))
                .body("destinations[2].containsKey('name')", is(true))
                .body("destinations[2].name", is("Roonstraße"))
                .body("destinations[2].containsKey('snapped_distance')", is(true))
                .body("destinations[2].snapped_distance", is(0.05f))
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
                .body("containsKey('sources')", is(true))
                .body("sources.size()", is(3))
                .body("sources[0].size()", is(2))
                .body("sources[0].containsKey('location')", is(true))
                .body("sources[0].location.size()", is(2))
                .body("sources[0].location[0]", is(8.681495f))
                .body("sources[0].location[1]", is(49.41461f))
                .body("sources[0].containsKey('snapped_distance')", is(true))
                .body("sources[0].snapped_distance", is(0.02f))

                .body("sources[1].size()", is(2))
                .body("sources[1].containsKey('location')", is(true))
                .body("sources[1].location.size()", is(2))
                .body("sources[1].location[0]", is(8.686507f))
                .body("sources[1].location[1]", is(49.41943f))
                .body("sources[1].containsKey('snapped_distance')", is(true))
                .body("sources[1].snapped_distance", is(0.02f))

                .body("sources[2].size()", is(2))
                .body("sources[2].containsKey('location')", is(true))
                .body("sources[2].location.size()", is(2))
                .body("sources[2].location[0]", is(8.687872f))
                .body("sources[2].location[1]", is(49.420318f))
                .body("sources[2].containsKey('snapped_distance')", is(true))
                .body("sources[2].snapped_distance", is(0.05f))
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
                .body("any { it.key == 'sources' }", is(true))
                .body("sources.size()", is(3))
                .body("sources[0].size()", is(3))
                .body("sources[0].containsKey('location')", is(true))
                .body("sources[0].location.size()", is(2))
                .body("sources[0].location[0]", is(8.681495f))
                .body("sources[0].location[1]", is(49.41461f))
                .body("sources[0].containsKey('name')", is(true))
                .body("sources[0].name", is("Wielandtstraße"))
                .body("sources[0].containsKey('snapped_distance')", is(true))
                .body("sources[0].snapped_distance", is(0.02f))

                .body("sources[1].size()", is(3))
                .body("sources[1].containsKey('location')", is(true))
                .body("sources[1].location.size()", is(2))
                .body("sources[1].location[0]", is(8.686507f))
                .body("sources[1].location[1]", is(49.41943f))
                .body("sources[1].containsKey('name')", is(true))
                .body("sources[1].name", is("Werderplatz"))
                .body("sources[1].containsKey('snapped_distance')", is(true))
                .body("sources[1].snapped_distance", is(0.02f))

                .body("sources[2].size()", is(3))
                .body("sources[2].containsKey('location')", is(true))
                .body("sources[2].location.size()", is(2))
                .body("sources[2].location[0]", is(8.687872f))
                .body("sources[2].location[1]", is(49.420318f))
                .body("sources[2].containsKey('name')", is(true))
                .body("sources[2].name", is("Roonstraße"))
                .body("sources[2].containsKey('snapped_distance')", is(true))
                .body("sources[2].snapped_distance", is(0.05f))
                .statusCode(200);
    }

    @Test
    public void testIdInSummary() {
        JSONObject body = new JSONObject();body.put("locations", getParameter("locations"));
        body.put("id", "request123");

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
    public void testDefinedSources() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("sources", new JSONArray(new int[] {1,2}));
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(2))
                .body("durations[0].size()", is(3))
                .body("durations[0][0]", is(211.17f))
                .body("durations[0][1]", is(0.0f))
                .body("durations[0][2]", is(102.53f))
                .body("durations[1][0]", is(235.97f))
                .body("durations[1][1]", is(90.42f))
                .body("durations[1][2]", is(0.0f))
                .statusCode(200);
    }

    @Test
    public void testDefinedDestinations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("destinations", new JSONArray(new int[] {1,2}));
        body.put("metrics", getParameter("metricsDuration"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'durations' }", is(true))
                .body("durations.size()", is(3))
                .body("durations[0].size()", is(2))
                .body("durations[0][0]", is(212.67f))
                .body("durations[0][1]", is(315.18f))
                .body("durations[1][0]", is(0.0f))
                .body("durations[1][1]", is(102.53f))
                .body("durations[2][0]", is(90.42f))
                .body("durations[2][1]", is(0.0f))
                .statusCode(200);
    }
}
