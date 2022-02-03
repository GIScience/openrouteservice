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
package org.heigit.ors.v2.services.isochrones;

import org.heigit.ors.services.isochrones.IsochronesErrorCodes;
import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@EndPointAnnotation(name = "isochrones")
@VersionAnnotation(version = "v2")
public class ResultTest extends ServiceTest {

    public ResultTest() {

        // Locations
        addParameter("preference", "fastest");
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

        Integer interval_100 = new Integer(100);
        Integer interval_200 = new Integer(200);
        Integer interval_400 = new Integer(400);
        Integer interval_900 = new Integer(900);

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
    public void testPolygon() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(49))
                .body("features[0].properties.center.size()", is(2))
                .body("bbox", hasItems(8.663323f, 49.40837f, 8.700336f, 49.439884f))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].properties.group_index", is(0))
                .body("features[0].properties.value", is(400f))
                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);

    }

    @Test
    public void testGroupIndices() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_400"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
    public void testUnknownLocation() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1_unknown"));
        body.put("range", getParameter("ranges_400"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(500)
                .body("error.code", is(IsochronesErrorCodes.UNKNOWN));

    }

    @Test
    public void testBoundingBox() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("bbox[0]", is(8.663323f))
                .body("bbox[1]", is(49.40837f))
                .body("bbox[2]", is(8.700336f))
                .body("bbox[3]", is(49.439884f))
                .statusCode(200);
    }

    @Test
    public void testLocationType() {

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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .body("features[0].properties.area", is(1699492.0f))
                .statusCode(200);

        body.put("location_type", "destination");
        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .body("features[0].properties.area", is(1561223.9f))
                .statusCode(200);
    }

    @Test
    public void testReachfactorAndArea() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6590000f)).and(lessThan(6600000f))))
                .body("features[0].properties.reachfactor", is(0.7561f))
                .statusCode(200);

    }

    @Test
    public void testReachfactorAndAreaAreaUnitsM() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", getParameter("m"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6590000f)).and(lessThan(6600000f))))
                .body("features[0].properties.reachfactor", is(0.7561f))
                .statusCode(200);

    }

    @Test
    public void testReachfactorAndAreaAreaUnitsKM() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", "km");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.59f)).and(lessThan(6.60f))))
                .body("features[0].properties.reachfactor", is(0.7561f))
                .statusCode(200);

    }

    @Test
    public void testAreaUnitsOverridesUnits() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("area_units", "km");
        body.put("units", "m");
        body.put("range_type", "time");
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.59f)).and(lessThan(6.60f))))
                .statusCode(200);

    }

    @Test
    public void testReachfactorAndAreaAreaUnitsMI() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("area_units", "mi");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(2.53f)).and(lessThan(2.55f))))
                .body("features[0].properties.reachfactor", is(0.7561f))
                .statusCode(200);

    }

    @Test
    public void testIntersections() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_400"));
        body.put("attributes", getParameter("attributesReachfactorArea"));
        body.put("intersections", "true");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
                .body("features[2].geometry.coordinates[0].size()", is(38))
                .body("features[2].properties.contours.size()", is(2))
                .body("features[2].properties.containsKey('area')", is(true))
                //.body("features[2].properties.area", is(5824280.5f))
                .body("features[0].properties.area", is(both(greaterThan(6590000f)).and(lessThan(6600000f))))
                .body("features[2].properties.contours[0][0]", is(0))
                .body("features[2].properties.contours[0][1]", is(0))
                .body("features[2].properties.contours[1][0]", is(1))
                .body("features[2].properties.contours[1][1]", is(0))
                .statusCode(200);

    }

    @Test
    public void testSmoothingFactor() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_2000"));
        body.put("smoothing", "10");
        body.put("range_type", "distance");


        int lowSmoothingCoordinatesSize = given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
    public void testCompleteMetadata() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("id", "request123");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
