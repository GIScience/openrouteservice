/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.v2.services.routing;

import heigit.ors.v2.services.common.EndPointAnnotation;
import heigit.ors.v2.services.common.ServiceTest;
import heigit.ors.v2.services.common.VersionAnnotation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@EndPointAnnotation(name = "routes")
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
                .log().all()
				.post(getEndPointPath()+"/{profile}")
				.then()
                .log().all()
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("info.query.instructions_format", is("text"))
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
                .post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("info.query.instructions_format", is("html"))
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
				.then().log().all()
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
                .post(getEndPointPath()+"/{profile}")
                .then().log().all()
                .assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("any { it.key == 'bbox' }", is(true))
				.body("any { it.key == 'type' }", is(true))
				.body("features[0].containsKey('properties')", is(true))
				.body("features[0].properties.containsKey('segments')", is(false))
                .statusCode(200);
	}

	@Test
	public void expectGeometryEncodedPolyline() {
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
                .post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.body("routes[0].geometry_format", is("encodedpolyline"))
//				.body(
//						"routes[0].geometry",
//						is(
//								"cqacIwn`u@p@lCN|@@B`@rAFG|AhEhApC`@x@r@xA`@t@Vf@|@`BVb@T`@bBbDf@`ADH^p@f@~@x@|AHNjAlBPRPRfAlAn@j@tAbArA|@VRVVdAhA`C`DrAtBz@tA`AnBDHNXDOBGDSP`@H\\DVTrAl@`DdBpKFZBJ@HPfATvA`@hC@FN`AN|@L`AjBxLLl@BPA\\?J?JTpAl@nDVvAHf@f@vCDX@Lx@jF~@|Fv@jFN~@BZ@P?F@j@?LBNDRPvABLA@CBCDEHADBNAJCP@VDRDJFHHD?LBV@L@FBV@lFLhF\\hGDf@\\|DZvCJlAD^@HJr@Hb@Nj@x@dCFN@DDHDHDFFJd@p@dClDLRj@~@FHBFPLLJDFZXDDVV@B@BJTDNd@rDFd@Rz@DLBDv@pANVPZNj@@FNx@Pz@DTBJ@F@FH^DNJVR\\Zb@LLFHBBPPt@v@JL^ZVTCJCJi@rCQbAIj@StAERSxAQlAI\\_@t@AJi@~D[`CCLE\\AJEZCPADCJAHCRXj@b@t@^r@Rf@Xn@~BlGDLFNTf@DJBBBH@BFHVb@p@lA^n@VXBFDDjDbG`@r@BDrA~BJPFJFJzFxJzA|CVh@D\\FHDHDFHJ`C|DJP~AjCHLv@nAPVd@r@RZLPDFFJz@xAL???PNt@pAJHFJAB_AjB"))
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
                .post(getEndPointPath()+"/{profile}")
				.then().log().all()
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
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
                .post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT))
				.statusCode(400);
	}

	@Test
	public void expectOptions() {
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("unpavedroads");
		avoids.put("tracks");
		avoids.put("fords");
		options.put("avoid_features", avoids);

		options.put("maximum_speed", 105);

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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("info.query.options.containsKey('avoid_features')", is(true))
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
				.post(getEndPointPath()+"/{profile}")
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
		avoids.put("tunnels");
		options.put("avoid_features", avoids);
		options.put("maximum_speed", "75");

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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("info.query.options.containsKey('avoid_polygons')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectAvoidpolygonsError() {

		// options for avoid polygon faulty
		JSONObject options = new JSONObject();
		JSONArray avoids = new JSONArray();
		avoids.put("tunnels");
		options.put("avoid_features", avoids);
		options.put("maximum_speed", "75");

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
				.post(getEndPointPath()+"/{profile}")
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
		options.put("maximum_speed", "75");

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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectCyclingToRejectHgvAvoidables() {
		// options for HGV profiles
		JSONObject options = new JSONObject();
		options.put("avoid_features", new String[] {"highways","tollways","ferries","tunnels","unpavedroads","tracks","fords"});
		options.put("maximum_speed", "75");
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
				.post(getEndPointPath()+"/{profile}")
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

		options.put("maximum_speed", 5);

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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectCarToRejectBikeParams() {
		JSONObject body = new JSONObject();
		body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
		body.put("preference", getParameter("preference"));
		body.put("geometry", true);

		// options for cycling profiles
		JSONObject options = new JSONObject();
		JSONObject profileParams = new JSONObject();
		JSONObject profileRestrictions = new JSONObject();
		profileRestrictions.put("gradient", "5");
		profileRestrictions.put("trail_difficulty", "1");
		profileParams.put("restrictions", profileRestrictions);
		options.put("profile_params", profileParams);

		body.put("options", options);

		given()
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.pathParam("profile", getParameter("carProfile"))
				.body(body.toString())
				.when().log().all()
				.post(getEndPointPath()+"/{profile}")
				.then().log().all()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.UNKNOWN_PARAMETER))
				.statusCode(400);
	}

	@Test
	public void expectMaximumspeedError() {

		// options for cycling profiles
		JSONObject options = new JSONObject();
		options.put("maximum_speed", "25fgf");

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
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
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
				.post(getEndPointPath()+"/{profile}")
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
				.when()
				.post(getEndPointPath()+"/{profile}")
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}
}
