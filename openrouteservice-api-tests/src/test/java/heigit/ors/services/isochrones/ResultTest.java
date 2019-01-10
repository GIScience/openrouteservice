/*
 *
 *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *
 *  *   	 http://www.giscience.uni-hd.de
 *  *   	 http://www.heigit.org
 *  *
 *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  distributed with this work for additional information regarding copyright
 *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  with the License. You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */
package heigit.ors.services.isochrones;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;


@EndPointAnnotation(name = "isochrones")
public class ResultTest extends ServiceTest {

    public ResultTest() {

        addParameter("location", "8.684177,49.423034");
        addParameter("locations", "8.684177,49.423034|8.684177,49.411034");
        addParameter("preference", "fastest");
        addParameter("profile", "cycling-regular");
    }

    @Test
    public void testPolygon() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size()", is(52))
                //.body("features[0].geometry.coordinates[0].size()", is(30))
                .body("features[0].properties.center.size()", is(2))
                //.body("bbox", hasItems(8.662622f, 49.40911f, 8.695994f, 49.440487f))
                .body("bbox", hasItems(8.663811f, 49.409103f, 8.699429f, 49.43929f))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].properties.group_index", is(0))
                .body("features[0].properties.value", is(400))
                .statusCode(200);
    }

    @Test
    public void testGroupIndices() {

        given()
                .param("locations", getParameter("locations"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .when()
                .get(getEndPointName())
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

        given()
                .param("locations", "-18.215332,45.79817")
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .when()
                .get(getEndPointName())
                .then()
                .statusCode(500)
                .body("error.code", is(IsochronesErrorCodes.UNKNOWN));
    }

    @Test
    public void testReachfactorAndArea() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "reachfactor|area")
                .when().log().all()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6110000f)).and(lessThan(6120000f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
                .statusCode(200);
    }

    @Test
    public void testReachfactorAndAreaAreaUnitsM() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "reachfactor|area")
                .param("area_units", "m")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6110000f)).and(lessThan(6120000f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
                .statusCode(200);
    }

    @Test
    public void testReachfactorAndAreaAreaUnitsKM() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "reachfactor|area")
                .param("area_units", "km")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.11f)).and(lessThan(6.12f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
                .statusCode(200);
    }

    @Test
    public void testAreaUnitsOverridesUnits() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "area")
                .param("area_units", "km")
                .param("units", "m")
                .param("range_type", "time")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(6.11f)).and(lessThan(6.12f))))
                .statusCode(200);
    }

    @Test
    public void testReachfactorAndAreaAreaUnitsMI() {

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "reachfactor|area")
                .param("area_units", "mi")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].properties.area", is(both(greaterThan(2.36f)).and(lessThan(2.37f))))
                .body("features[0].properties.reachfactor", is(0.1752f))
                .statusCode(200);
    }


    @Test
    public void testIntersections() {

        given()
                .param("locations", getParameter("locations"))
                .param("profile", getParameter("profile"))
                .param("range", "400")
                .param("attributes", "reachfactor|area")
                .param("intersections", "true")
                .when()
                .get(getEndPointName())
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
    public void testTwoDifferentTravellers_POST() {
        JSONObject json = new JSONObject();
        JSONArray jTravellers = new JSONArray();
        json.put("travellers", jTravellers);
        JSONObject jTraveller = new JSONObject();
        jTravellers.put(jTraveller);
        jTraveller.put("profile", "driving-car");
        jTraveller.put("location", new JSONArray().put(8.682289).put(49.386172));
        jTraveller.put("location_type", "start");
        jTraveller.put("range", new JSONArray().put(300).put(600));
        jTraveller.put("range_type", "time");

        jTraveller = new JSONObject();
        jTravellers.put(jTraveller);
        jTraveller.put("profile", "cycling-regular");
        jTraveller.put("location", new JSONArray().put(8.673362).put(49.420130));
        jTraveller.put("location_type", "start");
        jTraveller.put("range", new JSONArray().put(300).put(600));
        jTraveller.put("range_type", "time");

        json.put("units", "m");
        json.put("attributes", "area|reachfactor");
        json.put("intersections", true);

        given()
                .body(json.toString())
                .when().log().ifValidationFails()
                .post(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features.size()", is(5))
                .body("features[0].type", is("Feature"))
                .body("features[0].geometry.type", is("Polygon"))
                .body("features[0].geometry.coordinates[0].size", is(22))
                .body("features[0].properties.containsKey('area')", is(true))
                .body("features[1].type", is("Feature"))
                .body("features[1].geometry.type", is("Polygon"))
                .body("features[1].geometry.coordinates[0].size", is(56))
                .body("features[2].type", is("Feature"))
                .body("features[2].geometry.type", is("Polygon"))
                //.body("features[2].geometry.coordinates[0].size", is(25))
                .body("features[2].geometry.coordinates[0].size", is(29))
                .body("features[3].type", is("Feature"))
                .body("features[3].geometry.type", is("Polygon"))
                //.body("features[3].geometry.coordinates[0].size", is(33))
                .body("features[3].geometry.coordinates[0].size", is(36))
                .body("features[4].properties.contours.size", is(2))

                .statusCode(200);
    }

    @Test
    public void testSmoothingFactor() {
        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "2000")
                .param("range_type", "distance")
                .param("smoothing", "10")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size", is(52))
                .statusCode(200);

        given()
                .param("locations", getParameter("location"))
                .param("profile", getParameter("profile"))
                .param("range", "2000")
                .param("range_type", "distance")
                .param("smoothing", "100")
                .when()
                .get(getEndPointName())
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].geometry.coordinates[0].size", is(19))
                .statusCode(200);
    }
}
