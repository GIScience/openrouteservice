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
package org.heigit.ors.apitests.isochrones;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.geoJsonContent;

@EndPointAnnotation(name = "isochrones")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {
    public static final RestAssuredConfig JSON_CONFIG_DOUBLE_NUMBERS = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));

    public ResultTest() {

        // Locations
        addParameter("preference", "fastest");
        // Use only car and cycling and no HGV.
        // Making and HGV request results in the usage of fast isochrones, which are covered in their own tests.
        addParameter("cyclingProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");

        JSONArray firstLocation = new JSONArray();
        firstLocation.put(8.684177);
        firstLocation.put(49.423034);

        JSONArray secondLocation = new JSONArray();
        secondLocation.put(8.684177);
        secondLocation.put(49.410034);

        JSONArray unknownLocation = new JSONArray();
        unknownLocation.put(-18.215332);
        unknownLocation.put(45.79817);

        JSONArray locations_1_unknown = new JSONArray();
        locations_1_unknown.put(unknownLocation);

        JSONArray locations_1 = new JSONArray();
        locations_1.put(firstLocation);

        JSONArray locations_2 = new JSONArray();
        locations_2.put(firstLocation);
        locations_2.put(secondLocation);

        JSONArray ranges_2 = new JSONArray();
        ranges_2.put(1800);
        ranges_2.put(1800);

        JSONArray ranges_400 = new JSONArray();
        ranges_400.put(400);

        JSONArray ranges_1800 = new JSONArray();
        ranges_1800.put(1800);

        JSONArray ranges_2000 = new JSONArray();
        ranges_2000.put(2000);

        Integer interval_100 = 100;
        Integer interval_200 = 200;
        Integer interval_400 = 400;
        Integer interval_900 = 900;

        JSONArray attributesReachfactorArea = new JSONArray();
        attributesReachfactorArea.put("area");
        attributesReachfactorArea.put("reachfactor");

        JSONArray attributesReachfactorAreaFaulty = new JSONArray();
        attributesReachfactorAreaFaulty.put("areaaaa");
        attributesReachfactorAreaFaulty.put("reachfactorrrr");

        addParameter("locations_1", locations_1);
        addParameter("locations_2", locations_2);
        addParameter("locations_1_unknown", locations_1_unknown);

        addParameter("ranges_2", ranges_2);
        addParameter("ranges_400", ranges_400);
        addParameter("ranges_1800", ranges_1800);
        addParameter("ranges_2000", ranges_2000);
        addParameter("interval_100", interval_100);
        addParameter("interval_200", interval_200);
        addParameter("interval_200", interval_400);
        addParameter("interval_900", interval_900);
        addParameter("attributesReachfactorArea", attributesReachfactorArea);
        addParameter("attributesReachfactorAreaFaulty", attributesReachfactorAreaFaulty);

    }

    @Test
    void testPolygon() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(both(greaterThan(48)).and(lessThan(54))))
                .body("features[0].properties.center.size()", is(2))
                .body("bbox", hasItems(closeTo(8.663323f, 0.001f), closeTo(49.40837f, 0.001f), closeTo(8.700336f, 0.001f), closeTo(49.439884f, 0.001f)))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].properties.group_index", is(0))
                .body("features[0].properties.value", is(400.0))
                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);

    }

    @Test
    void testGroupIndices() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_400"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features.size()", is(2))
                .body("features[0].properties.group_index", is(0))
                .body("features[1].properties.group_index", is(1))
                .statusCode(200);

    }

    @Test
    void testUnknownLocation() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1_unknown"));
        body.put("range", getParameter("ranges_400"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(500)
                .body("error.code", is(IsochronesErrorCodes.UNKNOWN));

    }

    @Test
    void testBoundingBox() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("bbox[0]", is(closeTo(8.663323, 0.08)))
                .body("bbox[1]", is(closeTo(49.40837, 0.5)))
                .body("bbox[2]", is(closeTo(8.700336, 0.08)))
                .body("bbox[3]", is(closeTo(49.439884, 0.5)))
                .statusCode(200);
    }

    @Test
    void testLocationType() {

        JSONArray locations = new JSONArray();
        JSONArray loc1 = new JSONArray();
        loc1.put(8.681495);
        loc1.put(49.41461);
        locations.put(loc1);
        JSONArray ranges = new JSONArray();
        ranges.put(200);

        JSONObject body = new JSONObject();
        body.put("locations", locations);
        body.put("range", ranges);
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("range_type", "time");

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .body("features[0].properties.area", is(closeTo(1483816.7f, 34000f)))
                .statusCode(200);

        body.put("location_type", "destination");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .body("features[0].properties.area", is(closeTo(1114937.0f, 10000f)))
                .statusCode(200);
    }

    @Test
    void testReachfactorAndArea() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(closeTo(6600000, 132000)))
                .body("features[0].properties.reachfactor", is(closeTo(0.7429, 0.0148)))
                .statusCode(200);

    }

    @Test
    void testReachfactorAndAreaAreaUnitsM() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", getParameter("m"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(closeTo(6600000, 132000)))
                .body("features[0].properties.reachfactor", is(closeTo(0.7429, 0.0148)))
                .statusCode(200);

    }

    @Test
    void testReachfactorAndAreaAreaUnitsKM() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", "km");

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(closeTo(6.48, 0.132)))
                .body("features[0].properties.reachfactor", is(closeTo(0.7429, 0.0148)))
                .statusCode(200);

    }

    @Test
    void testAreaUnitsOverridesUnits() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("area_units", "km");
        body.put("units", "m");
        body.put("range_type", "time");
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(closeTo(6.60, 0.132)))
                .statusCode(200);

    }

    @Test
    void testReachfactorAndAreaAreaUnitsMI() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", "mi");

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(closeTo(2.55, 0.05)))
                .body("features[0].properties.reachfactor", is(closeTo(0.7429, 0.0148)))
                .statusCode(200);

    }

    @Test
    void testIntersections() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("intersections", "true");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features.size()", is(3))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[1].type", is("Feature"))
                .body("features[1].geometry.type", is("Polygon"))
                .body("features[2].type", is("Feature"))
                .body("features[2].geometry.type", is("Polygon"))
                //.body("features[2].geometry.coordinates[0].size()", is(26))
                .body("features[2].geometry.coordinates[0].size()", is(both(greaterThan(38)).and(lessThan(40))))
                .body("features[2].properties.contours.size()", is(2))
                .body("features[2].properties.containsKey('area')", is(true))
                //.body("features[2].properties.area", is(5824280.5f))
                .body("features[0].properties.area", is(both(greaterThan(6400000f)).and(lessThan(6600000f))))
                .body("features[2].properties.contours[0][0]", is(0))
                .body("features[2].properties.contours[0][1]", is(0))
                .body("features[2].properties.contours[1][0]", is(1))
                .body("features[2].properties.contours[1][1]", is(0))
                .statusCode(200);

    }

    @Test
    void testSmoothingFactor() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_2000"));
        body.put("smoothing", "10");
        body.put("range_type", "distance");


        int lowSmoothingCoordinatesSize = given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .extract().jsonPath().getInt("features[0].geometry.coordinates[0].size()");

        body.put("smoothing", "100");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", lessThan(lowSmoothingCoordinatesSize))
                .statusCode(200);
    }

    @Test
    void testCompleteMetadata() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("id", "request123");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))
                .body("metadata.containsKey('attribution')", is(true))
                .body("metadata.service", is("isochrones"))
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.id", is("request123"))
                .body("metadata.query.containsKey('locations')", is(true))
                .body("metadata.query.locations.size()", is(1))
                .body("metadata.query.locations[0][0]", is(8.684177f))
                .body("metadata.query.locations[0][1]", is(49.423034f))
                .body("metadata.query.containsKey('range')", is(true))
                .body("metadata.query.range.size()", is(1))
                .body("metadata.query.range[0]", is(400.0f))
                .body("metadata.query.profile", is("cycling-regular"))
                .body("metadata.query.id", is("request123"))
                .body("metadata.engine.containsKey('version')", is(true))
                .body("metadata.engine.containsKey('build_date')", is(true))
                .body("metadata.engine.containsKey('graph_date')", is(true))
                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);
    }
}
