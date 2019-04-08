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
package heigit.ors.v2.services.routing;

import heigit.ors.v2.services.common.EndPointAnnotation;
import heigit.ors.v2.services.common.ServiceTest;
import heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
public class ParamsTest extends ServiceTest {

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

		JSONArray extraInfo = new JSONArray();
		extraInfo.put("surface");
		extraInfo.put("suitability");
		extraInfo.put("steepness");
		addParameter("extra_info", extraInfo);

		addParameter("preference", "fastest");
		addParameter("profile", "cycling-regular");
		addParameter("carProfile", "driving-car");
		addParameter("footProfile", "foot-walking");
	}

	@Test
	public void basicPingTest() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
		given()
				.header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
                .log().ifValidationFails()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
                .log().ifValidationFails()
				.body("any { it.key == 'routes' }", is(true))
				.statusCode(200);
	}

	@Test
	public void expectNoInstructions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("instructions", "false");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(false))
				.statusCode(200);
	}

	@Test
	public void expectInstructions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(true))
				.body("routes[0].segments.size()", is(greaterThan(0)))
				.statusCode(200);
	}

	@Test
	public void expectInstructionsAsText() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("instructions_format", "text");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("metadata.query.instructions_format", is("text"))
				.statusCode(200);
	}

	@Test
	public void expectInstructionsAsHtml() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("instructions", "true");
        body.put("instructions_format", "html");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("metadata.query.instructions_format", is("html"))
				.statusCode(200);
	}

	@Test
	public void expectGeometry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectNoGeometry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "false");
        body.put("preference", getParameter("preference"));
		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
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
	public void expectGeometryGeojson() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
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
	public void expectGeoJsonExportInstructions(){
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
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
	public void expectElevation() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
				.then().log().ifValidationFails()
				.assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("features[0].containsKey('geometry')", is(true))
				.body("features[0].geometry.type", is("LineString"))
				.body("features[0].geometry.coordinates[0]", hasSize(3))
				.statusCode(200);
	}

	@Test
	public void expectNoElevation() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "false");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
				.then()
				.assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("features[0].containsKey('geometry')", is(true))
				.body("features[0].geometry.type", is("LineString"))
				.body("features[0].geometry.coordinates[0]", hasSize(2))
				.statusCode(200);
	}

	@Test
	public void expectExtrainfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));
        body.put("extra_info", getParameter("extra_info"));

		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
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
	public void expectNoExtrainfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "false");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("profile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(false))
				.statusCode(200);
	}

	@Test
	public void expectUnknownProfile() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("geometry", "true");
        body.put("elevation", "true");
        body.put("preference", getParameter("preference"));

		given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-car-123")
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectInvalidCoordinatesInGet() {
		given()
				.param("start", "8.686581")
				.param("end", "8.688126,49.409074")
				.pathParam("profile", getParameter("carProfile"))
				.when().log().ifValidationFails()
				.get(getEndPointPath() + "/{profile}")
				.then().log().ifValidationFails()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);

		given()
				.param("start", "8.686581,49.403154")
				.param("end", "8.688126")
				.pathParam("profile", getParameter("carProfile"))
				.when().log().ifValidationFails()
				.get(getEndPointPath() + "/{profile}")
				.then().log().ifValidationFails()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
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
	public void expect4002001() {
		JSONObject body = new JSONObject();
		body.put("coordinates", getParameter("coordinatesShort"));
		body.put("geometry", "true");
		body.put("preference", getParameter("preference"));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.body(body.toString())
				.when()
				.post(getEndPointPath())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.MISSING_PARAMETER))
				.statusCode(400);
	}

	@Test
	public void expect4002002() {
		JSONObject body = new JSONObject();
		body.put("coordinates", getParameter("coordinatesShortFaulty"));
		body.put("geometry", "true");
		body.put("preference", getParameter("preference"));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expect4002003() {
		JSONObject body = new JSONObject();
		body.put("coordinates", getParameter("coordinatesShort"));
		body.put("geometry", "true");
		body.put("preference", getParameter("preference"));
		body.put("language", "yuhd");

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expect400204() {

		JSONObject body = new JSONObject();
		body.put("coordinates", getParameter("coordinatesLong"));
		body.put("geometry", "true");
		body.put("preference", getParameter("preference"));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT))
				.statusCode(400);
	}

	@Test
	public void expectOptions() {
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("fords");
		options.put("avoid_features", avoids);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("metadata.query.options.containsKey('avoid_features')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectAvoidablesError() {
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("highwass");
		avoids.put("tolllways");
		avoids.put("f3erries");
		options.put("avoid_features", avoids);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("profile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectAvoidpolygons() {

		// options for avoid polygon
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		options.put("avoid_features", avoids);

		JSONObject polygon = new JSONObject();
		polygon.put("type", "Polygon");
		String[][][] coords = new String[][][] { { { "8.91197", "53.07257" }, { "8.91883", "53.06081" },
				{ "8.86699", "53.07381" }, { "8.91197", "53.07257" } } };
		polygon.put("coordinates", coords);
		options.put("avoid_polygons", polygon);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("metadata.query.options.containsKey('avoid_polygons')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectAvoidpolygonsError() {

		// options for avoid polygon faulty
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("ferries");
		options.put("avoid_features", avoids);

		JSONObject polygon = new JSONObject();
		polygon.put("type", "Polygon");
		String[][][] coords = new String[][][] { { { "8b.91197", "53a.07257" }, { "c8.91883", "53.06081" },
				{ "8.86699", "53.07381" }, { "8.91197", "d53.07257" } } };
		polygon.put("coordinates", coords);
		options.put("avoid_polygons", polygon);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectAvoidpolygonsTypeError() {

		// options for avoid polygon wrong feature type (can be polygon or
		// linestring)
		JSONObject options = new JSONObject();

		JSONObject polygon = new JSONObject();
		polygon.put("type", "Polygon");
		String[][] polygonCoords = new String[][] { { "8.91197", "53.07257" }, { "8.91883", "53.06081" },
				{ "8.86699", "53.07381" }, { "8.91197", "53.07257" } };
		polygon.put("coordinates", polygonCoords);
		options.put("avoid_polygons", polygon);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectCyclingToRejectHgvAvoidables() {
		// options for HGV profiles
		JSONObject options = new JSONObject();
		options.put("avoid_features", new String[] {"highways","tollways","ferries","fords"});
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
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", "cycling-road")
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectCarToRejectWalkingAvoidables() {

		// options for walking profiles
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("steps");
		avoids.put("fords");
		options.put("avoid_features", avoids);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectBearingsFormatError() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);

		body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectRadiusesFormatError() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));

		JSONArray radii = new JSONArray();
		radii.put(50);
		radii.put(50);
		radii.put(100);

		body.put("radiuses", radii);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);

		body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));

		radii = new JSONArray();
		radii.put("h50");
		radii.put(50);

		body.put("radiuses", radii);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectNoNearestEdge() {
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
		body.put("preference", "fastest");

		JSONArray radii = new JSONArray();
		radii.put(50);
		radii.put(150);

		body.put("radiuses", radii);


		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", "cycling-regular")
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
				.statusCode(404);
	}
	@Test
	public void expectUnknownUnits() {

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("units", "j");

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when().log().ifValidationFails()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownInstructionFormat() {

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("instructions_format", "blah");

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownPreference() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", "blah");

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownLanguage() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("language", "blah");

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownAttributes() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("attributes", Arrays.asList("blah"));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);

		body.put("attributes", new JSONArray(Arrays.asList("blah", "percentage")));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownAvoidFeatures() {
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray(Arrays.asList("blah", "ferries", "highways"));
		options.put("avoid_features", avoids);

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectUnknownAvoidBorders() {
		JSONObject options = new JSONObject();
		options.put("avoid_borders", "blah");

		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/json")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectInvalidResponseFormat() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath()+"/{profile}/blah")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
				.statusCode(406);
	}

	@Test
	public void expectWarningsAndExtraInfo() {
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
	public void expectSuppressedWarnings() {
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
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
	public void expectSimplifyIncompatibleWithExtraInfo() {
		JSONObject body = new JSONObject();
		body.put("coordinates", getParameter("coordinatesShort"));
		body.put("geometry_simplify", true);
		body.put("extra_info", getParameter("extra_info"));

		given()
				.header("Accept", "application/geo+json")
				.header("Content-Type", "application/json")
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
    public void expectSkipSegmentsErrors() {
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
    public void expectSkipSegmentsWarnings() {
        List<Integer> skipSegments = new ArrayList<>(1);
        skipSegments.add(1);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));


        body.put("skip_segments", skipSegments);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
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
	public void expectPointNotFoundError() {

        JSONArray coords = new JSONArray();
        coords.put(new JSONArray(new double[]{8.688544, 49.435462}));
        coords.put(new JSONArray(new double[]{8.678727, 49.440115}));

        JSONObject body = new JSONObject();
        body.put("coordinates", coords);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
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
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
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
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
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
	public void testGreenWeightingTooHigh() {
		JSONObject body = new JSONObject();

		JSONArray coordinates = new JSONArray();
		JSONArray coord1 = new JSONArray();
		coord1.put(8.676023);
		coord1.put(49.416809);
		coordinates.put(coord1);
		JSONArray coord2 = new JSONArray();
		coord2.put(8.696837);
		coord2.put(49.411839);
		coordinates.put(coord2);
		body.put("coordinates", coordinates);

		JSONObject weightings = new JSONObject();
		weightings.put("green", 1.1);
		JSONObject params = new JSONObject();
		params.put("weightings", weightings);
		JSONObject options = new JSONObject();
		options.put("profile_params", params);
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
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
	public void testQuietWeightingTooHigh() {
		JSONObject body = new JSONObject();

		JSONArray coordinates = new JSONArray();
		JSONArray coord1 = new JSONArray();
		coord1.put(8.676023);
		coord1.put(49.416809);
		coordinates.put(coord1);
		JSONArray coord2 = new JSONArray();
		coord2.put(8.696837);
		coord2.put(49.411839);
		coordinates.put(coord2);
		body.put("coordinates", coordinates);

		JSONObject weightings = new JSONObject();
		weightings.put("quiet", 1.1);
		JSONObject params = new JSONObject();
		params.put("weightings", weightings);
		JSONObject options = new JSONObject();
		options.put("profile_params", params);
		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("footProfile"))
				.body(body.toString())
				.when()
				.post(getEndPointPath() + "/{profile}/json")
				.then().log().ifValidationFails()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}
}
