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
        JSONArray maximumLocations = fakeLocations(MatrixServiceSettings.getMaximumRoutes(false) + 1);
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
                .body("any { it.key == 'metadata' }", is(true))
                .body("any { it.key == 'sources' }", is(true))
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
    public void expectUnknownUnits() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("units", "j");
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
    public void expectInvalidResponseFormat() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/blah")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);
    }

    @Test
    public void expectTooLittleLocationsError() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("minimalLocations"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "carProfile")
                .body(body.toString())
                .when()
                .log().all()
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
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().all()
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

    @Test
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
    }

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
                .body("metadata.query.containsKey('resolve_locations')", is(true))
                .body("destinations[0].containsKey('name')", is(true))
                .body("sources[0].containsKey('name')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDurations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
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
                .body("containsKey('durations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectDistances() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
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
                .body("containsKey('distances')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectAllMetrics() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
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
                .body("containsKey('durations')", is(true))
                .body("containsKey('distances')", is(true))
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
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.containsKey('engine')", is(true))
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
                .body("metadata.engine.containsKey('version')", is(true))
                .body("metadata.engine.containsKey('build_date')", is(true))
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
                .body("metadata.containsKey('query')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectFlexibleMode() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("optimized", true);
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
                .body("metadata.query.containsKey('optimized')", is(true))
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('locations')", is(true))
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('profile')", is(true))
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('responseType')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQuerySources() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("sources", new String[] {"all"});
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
                .body("metadata.query.containsKey('sources')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryDestinations() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("destinations", new String[] {"all"});
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
                .body("metadata.query.containsKey('destinations')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryMetrics() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("metrics", new String[]{"distance"});
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
                .body("metadata.query.containsKey('metrics')", is(true))
                .statusCode(200);
    }

    @Test
    public void expectQueryUnits() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations"));
        body.put("units", "m");
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
                .statusCode(200);
    }

    @Test
    public void expectQueryResolveLocations() {
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
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.containsKey('resolve_locations')", is(true))
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
                .body("containsKey('destinations')", is(true))
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
                .body("destinations[0].containsKey('location')", is(true))
                .body("destinations[0].containsKey('snapped_distance')", is(true))
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
                .body("containsKey('sources')", is(true))
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
                .body("sources[0].containsKey('location')", is(true))
                .body("sources[0].containsKey('snapped_distance')", is(true))
                .statusCode(200);
    }

    @Test
    public void pointOutOfBoundsTest() {
        given()
                .param("profile", "driving-car")
                .param("locations", "9.0,48.7|9.0,49.1")
                .param("sources", "all")
                .param("destinations", "all")
                .when()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("error.code", is(MatrixErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);
    }
}
