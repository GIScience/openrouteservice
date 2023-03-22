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
package org.heigit.ors.apitests.routing;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.config.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.geoJsonContent;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;
import static org.heigit.ors.apitests.utils.HelperFunctions.constructCoords;

@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    public ParamsTest() {
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        coordsShort.put(coord2);
        addParameter("coordinatesShort", coordsShort);

        JSONArray coordsFaulty = new JSONArray();
        JSONArray coordFaulty1 = new JSONArray();
        coordFaulty1.put("8.680916a");
        coordFaulty1.put("49.41b0973");
        coordsFaulty.put(coordFaulty1);
        JSONArray coordFaulty2 = new JSONArray();
        coordFaulty2.put("8.6c87782");
        coordFaulty2.put("049gbd.424597");
        coordsFaulty.put(coordFaulty2);

        addParameter("coordinatesShortFaulty", coordsFaulty);

        JSONArray coordsLong = new JSONArray();
        JSONArray coordsLong1 = new JSONArray();
        coordsLong1.put(8.502045);
        coordsLong1.put(49.47794);
        coordsLong.put(coordsLong1);
        JSONArray coordsLong2 = new JSONArray();
        coordsLong2.put(4.78906);
        coordsLong2.put(53.071752);
        coordsLong.put(coordsLong2);

        addParameter("coordinatesLong", coordsLong);

        JSONArray coordsVia = new JSONArray();
        JSONArray coordv1 = new JSONArray();
        coordv1.put(8.680916);
        coordv1.put(49.410973);
        coordsVia.put(coordv1);
        JSONArray coordv2 = new JSONArray();
        coordv2.put(8.687782);
        coordv2.put(49.424597);
        coordsVia.put(coordv2);
        JSONArray coordv3 = new JSONArray();
        coordv3.put(8.689061);
        coordv3.put(49.421752);
        coordsVia.put(coordv3);

        addParameter("coordinatesWithViaPoint", coordsVia);

        JSONArray coordsFoot = new JSONArray();
        JSONArray coordFoot1 = new JSONArray();
        coordFoot1.put(8.676023);
        coordFoot1.put(49.416809);
        coordsFoot.put(coordFoot1);
        JSONArray coordFoot2 = new JSONArray();
        coordFoot2.put(8.696837);
        coordFoot2.put(49.411839);
        coordsFoot.put(coordFoot2);

        addParameter("coordinatesWalking", coordsFoot);

        JSONArray extraInfo = new JSONArray();
        extraInfo.put("surface");
        extraInfo.put("suitability");
        extraInfo.put("steepness");
        addParameter("extra_info", extraInfo);

        addParameter("preference", "recommended");
        addParameter("profile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");
    }

    @Test
    void basicPingTest() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .log().ifValidationFails()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);
    }

    @Test
    void expectNoInstructions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("instructions", "false");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(false))
                .statusCode(200);
    }

    @Test
    void expectInstructions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(greaterThan(0)))
                .statusCode(200);
    }

    @Test
    void expectInstructionsAsText() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("instructions_format", "text");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("metadata.query.instructions_format", is("text"))
                .statusCode(200);
    }

    @Test
    void expectInstructionsAsHtml() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("instructions_format", "html");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("metadata.query.instructions_format", is("html"))
                .statusCode(200);
    }

    @Test
    void expectGeometry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('geometry')", is(true))
                .statusCode(200);
    }

    @Test
    void expectNoGeometry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "false");
        body.put("preference", getParameter("preference"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('geometry')", is(false))
                .statusCode(200);
    }

    /**
     * Expects the typical json response with geojson and addtitional elements in it.
     * The difference to expectGeoJsonExport is, that it validates the typical json export.
     */
    @Test
    void expectGeometryGeojson() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].geometry.type", is("LineString"))
                .statusCode(200);
    }

    /**
     * This test validates the GeoJson-Export Parameter, together with and without instructions.
     * The difference to expectGeometryGeojson is, that it validates the proper geojson export.
     */
    @Test
    void expectGeoJsonExportInstructions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('segments')", is(true))
                .statusCode(200);

        body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));
        body.put("instructions", "false");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('segments')", is(false))
                .statusCode(200);
    }

    @Test
    void expectElevation() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].geometry.type", is("LineString"))
                .body("features[0].geometry.coordinates[0]", hasSize(3))
                .statusCode(200);
    }

    @Test
    void expectNoElevation() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "false");
        body.put("preference", getParameter("preference"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].geometry.type", is("LineString"))
                .body("features[0].geometry.coordinates[0]", hasSize(2))
                .statusCode(200);
    }

    @Test
    void expectExtrainfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));
        body.put("extra_info", getParameter("extra_info"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('geometry')", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('surface')", is(true))
                .body("routes[0].extras.containsKey('suitability')", is(true))
                .body("routes[0].extras.containsKey('steepness')", is(true))
                .statusCode(200);
    }

    @Test
    void expectNoExtrainfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "false");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(false))
                .statusCode(200);
    }

    @Test
    void expectUnknownProfile() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car-123")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectInvalidCoordinatesInGet() {
        given()
                .param("start", "8.686581")
                .param("end", "8.688126,49.409074")
                .pathParam("profile", getParameter("carProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        given()
                .param("start", "8.686581,49.403154")
                .param("end", "8.688126")
                .pathParam("profile", getParameter("carProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        given()
                .param("start", "8.686581,49.403154")
                .pathParam("profile", getParameter("carProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.MISSING_PARAMETER))
                .statusCode(400);
    }

    @Test
    void expect4002001() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .body(body.toString())
                .when()
                .post(getEndPointPath())
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.MISSING_PARAMETER))
                .statusCode(400);
    }

    @Test
    void expect4002002() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShortFaulty"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
                .statusCode(400);
    }

    @Test
    void expect4002003() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));
        body.put("language", "yuhd");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expect400204() {

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT))
                .statusCode(400);
    }

    @Test
    void expectOptions() {
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray();
        avoids.put("fords");
        options.put("avoid_features", avoids);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.query.options.containsKey('avoid_features')", is(true))
                .statusCode(200);
    }

    @Test
    void expectAvoidablesError() {
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray();
        avoids.put("highwass");
        avoids.put("tolllways");
        avoids.put("f3erries");
        options.put("avoid_features", avoids);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectAvoidpolygons() {

        // options for avoid polygon
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray();
        options.put("avoid_features", avoids);

        JSONObject polygon = new JSONObject();
        polygon.put("type", "Polygon");
        String[][][] coords = new String[][][]{{{"8.91197", "53.07257"}, {"8.91883", "53.07381"},
                {"8.92699", "53.07381"}, {"8.91197", "53.07257"}}};
        polygon.put("coordinates", coords);
        options.put("avoid_polygons", polygon);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("metadata.query.options.containsKey('avoid_polygons')", is(true))
                .statusCode(200);
    }

    @Test
    void expectAvoidpolygonsError() {

        // options for avoid polygon faulty
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray();
        avoids.put("ferries");
        options.put("avoid_features", avoids);

        JSONObject polygon = new JSONObject();
        polygon.put("type", "Polygon");
        String[][][] coords = new String[][][]{{{"8b.91197", "53a.07257"}, {"c8.91883", "53.06081"},
                {"8.86699", "53.07381"}, {"8.91197", "d53.07257"}}};
        polygon.put("coordinates", coords);
        options.put("avoid_polygons", polygon);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
                .statusCode(400);
    }

    @Test
    void expectAvoidpolygonsTypeError() {

        // options for avoid polygon wrong feature type (can be polygon or
        // linestring)
        JSONObject options = new JSONObject();

        JSONObject polygon = new JSONObject();
        polygon.put("type", "Polygon");
        String[][] polygonCoords = new String[][]{{"8.91197", "53.07257"}, {"8.91883", "53.06081"},
                {"8.86699", "53.07381"}, {"8.91197", "53.07257"}};
        polygon.put("coordinates", polygonCoords);
        options.put("avoid_polygons", polygon);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
                .statusCode(400);
    }

    @Test
    void expectAvoidpolygonsRejectTooLargePolygons() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");

        JSONObject avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[2,0],[2,0.001],[0,0.001],[0,0]]]}");
        JSONObject options = new JSONObject();
        options.put("avoid_polygons", avoidGeom);
        body.put("options", options);

        double maximumAvoidPolygonExtent = Double.parseDouble(AppConfig.getGlobal().getServiceParameter("routing", "profiles.default_params.maximum_avoid_polygon_extent"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("error.code", is(2003))
                .body("error.message", is(String.format("The extent of a polygon to avoid must not exceed %s meters.", maximumAvoidPolygonExtent)))
                .statusCode(400);

        body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");

        avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[0.3,0],[0.3,0.1],[0,0.1],[0,0]]]}");
        options = new JSONObject();
        options.put("avoid_polygons", avoidGeom);
        body.put("options", options);

        double maximumAvoidPolygonArea = Double.parseDouble(AppConfig.getGlobal().getServiceParameter("routing", "profiles.default_params.maximum_avoid_polygon_area"));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("error.code", is(2003))
                .body("error.message", is(String.format("The area of a polygon to avoid must not exceed %s square meters.", maximumAvoidPolygonArea)))
                .statusCode(400);
    }

    @Test
    void expectCyclingToRejectHgvAvoidables() {
        // options for HGV profiles
        JSONObject options = new JSONObject();
        options.put("avoid_features", new String[]{"highways", "tollways", "ferries", "fords"});
        JSONObject profileParams = new JSONObject();
        JSONObject restrictions = new JSONObject();
        restrictions.put("width", "5");
        restrictions.put("height", "3");
        restrictions.put("length", "10");
        restrictions.put("axleload", "2");
        restrictions.put("weight", "5");
        restrictions.put("hazmat", "true");
        profileParams.put("restrictions", restrictions);
        options.put("profile_params", profileParams);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", "cycling-road")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectCarToRejectWalkingAvoidables() {

        // options for walking profiles
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray();
        avoids.put("steps");
        avoids.put("fords");
        options.put("avoid_features", avoids);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectBearingsFormatError() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        JSONArray bearings = new JSONArray();
        JSONArray bearing1 = new JSONArray();
        bearing1.put(50);
        bearing1.put(50);
        bearings.put(bearing1);
        JSONArray bearing2 = new JSONArray();
        bearing2.put(50);
        bearing2.put(50);
        bearings.put(bearing2);
        JSONArray bearing3 = new JSONArray();
        bearing3.put(50);
        bearing3.put(50);
        bearings.put(bearing3);

        body.put("bearings", bearings);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        bearings = new JSONArray();
        bearing1 = new JSONArray();
        bearing1.put("50k");
        bearing1.put(50);
        bearings.put(bearing1);
        bearing2 = new JSONArray();
        bearing2.put(50);
        bearing2.put(50);
        bearings.put(bearing2);

        body.put("bearings", bearings);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
                .statusCode(400);
    }

    @Test
    void expectRadiusesFormatError() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        JSONArray radii = new JSONArray();
        radii.put(50);
        radii.put(50);
        radii.put(100);

        body.put("radiuses", radii);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        radii = new JSONArray();
        radii.put("h50");
        radii.put(50);

        body.put("radiuses", radii);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
                .statusCode(400);
    }

    @Test
    void expectNoNearestEdge() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.689585);
        coord1.put(49.399733);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.686495);
        coord2.put(49.40349);
        coordinates.put(coord2);

        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        JSONArray radii = new JSONArray();
        radii.put(50);
        radii.put(150);

        body.put("radiuses", radii);


        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);
    }

    @Test
    void expectUnknownUnits() {

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("units", "j");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownInstructionFormat() {

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("instructions_format", "blah");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownPreference() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "blah");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownLanguage() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("language", "blah");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownAttributes() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("attributes", List.of("blah"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("attributes", new JSONArray(Arrays.asList("blah", "percentage")));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownAvoidFeatures() {
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray(Arrays.asList("blah", "ferries", "highways"));
        options.put("avoid_features", avoids);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownAvoidBorders() {
        JSONObject options = new JSONObject();
        options.put("avoid_borders", "blah");

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectInvalidResponseFormat() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/blah")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);
    }

    @Test
    void expectWarningsAndExtraInfo() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.675154);
        coord1.put(49.407727);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.675863);
        coord2.put(49.407162);
        coordinates.put(coord2);
        body.put("coordinates", coordinates);

        body.put("preference", "shortest");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].containsKey('code')", is(true))
                .body("routes[0].warnings[0].containsKey('message')", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('roadaccessrestrictions')", is(true))
                .statusCode(200);

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('extras')", is(true))
                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].containsKey('message')", is(true))
                .body("features[0].properties.extras.containsKey('roadaccessrestrictions')", is(true))
                .statusCode(200);
    }

    @Test
    void expectSuppressedWarnings() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.675154);
        coord1.put(49.407727);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.675863);
        coord2.put(49.407162);
        coordinates.put(coord2);
        body.put("coordinates", coordinates);

        body.put("preference", "shortest");
        body.put("suppress_warnings", "true");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('warnings')", is(false))
                .statusCode(200);
    }

    @Test
    void expectSimplifyIncompatibleWithExtraInfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry_simplify", true);
        body.put("extra_info", getParameter("extra_info"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS))
                .statusCode(400);
    }

    @Test
    void expectSkipSegmentsErrors() {
        List<Integer> skipSegmentsEmpty = new ArrayList<>(1);
        List<Integer> skipSegmentsTooHigh = new ArrayList<>(1);
        List<Integer> skipSegmentsTooSmall = new ArrayList<>(1);
        List<Integer> skipSegmentsTooMany = new ArrayList<>(3);
        skipSegmentsEmpty.add(0, 0);
        skipSegmentsTooHigh.add(0, 99);
        skipSegmentsTooSmall.add(0, -99);
        skipSegmentsTooMany.add(0, 1);
        skipSegmentsTooMany.add(1, 1);
        skipSegmentsTooMany.add(2, 1);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));

        body.put("skip_segments", skipSegmentsEmpty);
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("skip_segments", skipSegmentsTooHigh);
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("skip_segments", skipSegmentsTooSmall);
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("skip_segments", skipSegmentsTooMany);
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectSkipSegmentsWarnings() {
        List<Integer> skipSegments = new ArrayList<>(1);
        skipSegments.add(1);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));


        body.put("skip_segments", skipSegments);
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].containsKey('code')", is(true))
                .body("routes[0].warnings[0].containsKey('message')", is(true))
                .statusCode(200);

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].containsKey('message')", is(true))
                .statusCode(200);
    }

    /**
     * This test needs the maximum_snapping_radius of cycling-regular to be >= 10 and <= 100
     * and maximum_snapping_radius of the default parameters to be >= 350 in the test config to run through.
     */
    @Test
    void expectPointNotFoundError() {

        JSONArray coords = new JSONArray();
        coords.put(new JSONArray(new double[]{8.688544, 49.435462}));
        coords.put(new JSONArray(new double[]{8.678727, 49.440115}));

        JSONObject body = new JSONObject();
        body.put("coordinates", coords);

        given()
                .headers(jsonContent)
                .pathParam("profile", "cycling-mountain")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);
        body.put("radiuses", new int[]{5, 10});

        given()
                .headers(jsonContent)
                .pathParam("profile", "cycling-mountain")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);

        coords = new JSONArray();
        coords.put(new JSONArray(new double[]{8.688297, 49.436299}));
        coords.put(new JSONArray(new double[]{8.678727, 49.440115}));

        body = new JSONObject();
        body.put("coordinates", coords);

        given()
                .headers(jsonContent)
                .pathParam("profile", "cycling-mountain")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);

        body.put("radiuses", new int[]{100, 10});

        given()
                .headers(jsonContent)
                .pathParam("profile", "cycling-mountain")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);

        coords = new JSONArray();
        coords.put(new JSONArray(new double[]{8.647348, 49.366612}));
        coords.put(new JSONArray(new double[]{8.678727, 49.440115}));

        body = new JSONObject();
        body.put("coordinates", coords);

        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);

        body.put("radiuses", new int[]{150, 10});

        given()
                .headers(jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
                .statusCode(404);
    }

    @Test
    void testGreenWeightingTooHigh() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        JSONObject weightings = new JSONObject();
        weightings.put("green", 1.1);
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testQuietWeightingTooHigh() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        JSONObject weightings = new JSONObject();
        weightings.put("quiet", 1.1);
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectAlternativeRoutesToRejectMoreThanTwoWaypoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWithViaPoint"));
        body.put("preference", getParameter("preference"));

        JSONObject ar = new JSONObject();
        ar.put("target_count", "3");
        body.put("alternative_routes", ar);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS))
                .statusCode(400);
    }

    @Test
    void expectRoundTripToRejectMoreThanOneCoordinate() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWithViaPoint"));

        JSONObject options = new JSONObject();
        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("length", 100);
        options.put("round_trip", roundTripOptions);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectRoundTripToRejectTooLongLength() {
        JSONObject body = new JSONObject();
        JSONArray singleCoordinate = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        singleCoordinate.put(coord1);
        body.put("coordinates", singleCoordinate);

        JSONObject options = new JSONObject();
        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("length", 110000);
        options.put("round_trip", roundTripOptions);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT))
                .statusCode(400);
    }

    @Test
    void expectRejectSingleCoordinate() {
        JSONObject body = new JSONObject();
        JSONArray singleCoordinate = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        singleCoordinate.put(coord1);
        body.put("coordinates", singleCoordinate);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectAcceptSingleCoordinateForRoundTrip() {
        JSONObject body = new JSONObject();
        JSONArray singleCoordinate = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        singleCoordinate.put(coord1);
        body.put("coordinates", singleCoordinate);

        JSONObject options = new JSONObject();
        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("length", 100);
        options.put("round_trip", roundTripOptions);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);
    }

    @Test
    void expectRejectRoundTripWithoutLength() {
        JSONObject body = new JSONObject();
        JSONArray singleCoordinate = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.680916);
        coord1.put(49.410973);
        singleCoordinate.put(coord1);
        body.put("coordinates", singleCoordinate);

        JSONObject options = new JSONObject();
        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("points", 5);
        options.put("round_trip", roundTripOptions);
        body.put("options", options);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.MISSING_PARAMETER))
                .statusCode(400);
    }

    @Test
    void testMaximumSpeedLowerBound() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.63348,49.41766|8.6441,49.4672"));
        body.put("preference", getParameter("preference"));
        body.put("maximum_speed", 75);

        //Test that the distance of the computed route.
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(false))
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testMaximumSpeedUnsupportedProfile() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.63348,49.41766|8.6441,49.4672"));
        body.put("preference", getParameter("preference"));
        body.put("maximum_speed", 80);

        //Test that the distance of the computed route.
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(false))
                .body("error.code", is(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS))
                .statusCode(400);
    }

    @Test
    void expectDepartureAndArrivalToBeMutuallyExclusive() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("departure", "2021-01-31T12:00");
        body.put("arrival", "2021-01-31T12:00");

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(false))
                .body("error.code", is(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS))
                .statusCode(400);
    }

    @Test
    void expectNoErrorOnSingleRadiusForMultipleCoordinates() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        // setting a single value should work
        body.put("radiuses", 500);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);

        // as should setting an array containing a single value
        JSONArray radii = new JSONArray();
        radii.put(500);

        body.put("radiuses", radii);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .statusCode(200);
    }
}
