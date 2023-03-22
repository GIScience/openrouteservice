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
package org.heigit.ors.apitests.isochrones.fast;

import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.apitests.isochrones.IsochronesErrorCodes;
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
    private static final float REACHFACTOR_REFERENCE_VALUE = 0.0544f;

    public ResultTest() {

        // Locations
        addParameter("preference", "fastest");
        addParameter("hgvProfile", "driving-hgv");

        JSONArray firstLocation = new JSONArray();
        firstLocation.put(8.684177);
        firstLocation.put(49.423034);

        JSONArray secondLocation = new JSONArray();
        secondLocation.put(8.684177);
        secondLocation.put(49.411034);

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
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(both(greaterThan(60)).and(lessThan(80))))
                .body("features[0].properties.center.size()", is(2))
                .body("bbox", hasItems(closeTo(8.652489f, 0.02f), closeTo(49.40263f, 0.02f), closeTo(8.708881f, 0.02f), closeTo(49.447865f, 0.02f)))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].properties.group_index", is(0))
                .body("features[0].properties.value", is(400.0))
                .statusCode(200);

    }

    @Test
    void testGroupIndices() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_400"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
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
                .pathParam("profile", getParameter("hgvProfile"))
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
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("bbox[0]", is(closeTo(8.652489f, 0.05)))
                .body("bbox[1]", is(closeTo(49.40263f, 0.05)))
                .body("bbox[2]", is(closeTo(8.708881f, 0.05)))
                .body("bbox[3]", is(closeTo(49.447865f, 0.05)))
                .statusCode(200);
    }

    @Test
    void testReachfactorAndArea() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(8000000d)).and(lessThan(18000000d))))
                .body("features[0].properties.reachfactor", is(closeTo(REACHFACTOR_REFERENCE_VALUE, 0.01)))
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
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(8000000d)).and(lessThan(15000000d))))
                .body("features[0].properties.reachfactor", is(closeTo(REACHFACTOR_REFERENCE_VALUE, 0.01)))
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
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(8.0d)).and(lessThan(15.0d))))
                .body("features[0].properties.reachfactor", is(closeTo(REACHFACTOR_REFERENCE_VALUE, 0.01)))
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
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(8.0f)).and(lessThan(15.0f))))
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
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(3.0d)).and(lessThan(6.0d))))
                .body("features[0].properties.reachfactor", is(closeTo(REACHFACTOR_REFERENCE_VALUE, 0.01)))
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
                .pathParam("profile", getParameter("hgvProfile"))
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
                .body("features[2].geometry.coordinates[0].size()", is(both(greaterThan(65)).and(lessThan(85))))
                .body("features[2].properties.contours.size()", is(2))
                .body("features[2].properties.containsKey('area')", is(true))
                .body("features[0].properties.area", is(both(greaterThan(8000000f)).and(lessThan(15000000f))))
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

        // Updated in the GH 0.12 update from size = 52 as there is a difference in the order that edges are returned and
        // so neighbourhood search results in slightly different results

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(both(greaterThan(60)).and(lessThan(80))))
                .statusCode(200);

        body.put("smoothing", "100");
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(both(greaterThan(55)).and(lessThan(85))))
                .statusCode(200);
    }

    @Test
    void testIdInSummary() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("id", "request123");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))
                .statusCode(200);
    }
}
