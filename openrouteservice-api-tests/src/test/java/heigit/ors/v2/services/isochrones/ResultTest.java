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
package heigit.ors.v2.services.isochrones;

import heigit.ors.services.isochrones.IsochronesErrorCodes;
import heigit.ors.v2.services.common.EndPointAnnotation;
import heigit.ors.v2.services.common.ServiceTest;
import heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.lessThan;

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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().all()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(52))
                .body("features[0].properties.center.size()", is(2))
                .body("bbox", hasItems(8.663811f, 49.409103f, 8.699429f, 49.43929f))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].properties.group_index", is(0))
                .body("features[0].properties.value", is(400f))
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
                .log().all()
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
                .log().all()
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("bbox[0]", is(8.663811f))
                .body("bbox[1]", is(49.409103f))
                .body("bbox[2]", is(8.699429f))
                .body("bbox[3]", is(49.439289f))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6110000f)).and(lessThan(6120000f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6110000f)).and(lessThan(6120000f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.11f)).and(lessThan(6.12f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.11f)).and(lessThan(6.12f))))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(2.36f)).and(lessThan(2.37f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
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
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().all()
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
                .body("features[2].geometry.coordinates[0].size()", is(35))
                .body("features[2].properties.contours.size()", is(2))
                .body("features[2].properties.containsKey('area')", is(true))
                //.body("features[2].properties.area", is(5824280.5f))
                .body("features[0].properties.area", is(both(greaterThan(6110000f)).and(lessThan(6120000f))))
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

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .log().all()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size", is(52))
                .statusCode(200);

        body.put("smoothing", "100");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("cyclingProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().all()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size", is(19))
                .statusCode(200);
    }

    @Test
    public void testIdInSummary() {
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
                .statusCode(200);
    }
}
