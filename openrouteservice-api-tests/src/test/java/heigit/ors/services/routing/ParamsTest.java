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
package heigit.ors.services.routing;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.json.JSONObject;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name = "routes")
public class ParamsTest extends ServiceTest {

	public ParamsTest() {
		addParameter("coordinatesShort", "8.678613,49.411721|8.687782,49.424597");
		addParameter("coordinatesShortFaulty", "8.680916a,49.41b0973|8.6c87782,049gbd.424597");
		addParameter("coordinatesLong", "8.678613,49.411721|4.78906,53.071752");
		addParameter("extra_info", "surface|suitability|steepness");
		addParameter("preference", "fastest");
		addParameter("profile", "cycling-regular");
		addParameter("carProfile", "driving-car");
	}

	@Test
	public void basicPingTest() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'routes' }", is(true))
				.statusCode(200);
	}

	@Test
	public void expectNoInstructions() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(false))
				.statusCode(200);
	}

	@Test
	public void expectInstructions() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(true))
				.body("routes[0].segments.size()", is(greaterThan(0)))
				.statusCode(200);
	}

	@Test
	public void expectInstructionsAsText() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("instructions", "true")
				.param("instructions_format", "text")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("info.query.instructions_format", is("text"))
				.statusCode(200);
	}

	@Test
	public void expectInstructionsAsHtml() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("instructions", "true")
				.param("instructions_format", "html")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("info.query.instructions_format", is("html"))
				.statusCode(200);
	}

	@Test
	public void expectGeometry() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectNoGeometry() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "false")
				.when()
				.get(getEndPointName())
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

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("geometry_format", "geojson")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.body("routes[0].geometry_format", is("geojson"))
				.body("routes[0].geometry.type", is("LineString"))
				.statusCode(200);
	}

    /**
     * This test validates the GeoJson-Export Parameter, together with and without instructions.
     * The difference to expectGeometryGeojson is, that it validates the proper geojson export.
     */
	@Test
	public void expectGeoJsonExportInstructions(){
		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("format", "geojson")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("any { it.key == 'bbox' }", is(true))
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'info' }", is(true))
				.body("features[0].containsKey('properties')", is(true))
				.body("features[0].properties.containsKey('segments')", is(true))
				.statusCode(200);
        given()
                .param("coordinates", getParameter("coordinatesShort"))
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("profile"))
                .param("instructions", "false")
                .param("format", "geojson")
                .when()
                .get(getEndPointName())
                .then()
                .assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("any { it.key == 'bbox' }", is(true))
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'info' }", is(true))
				.body("features[0].containsKey('properties')", is(true))
				.body("features[0].properties.containsKey('segments')", is(false))
                .statusCode(200);
	}

	@Test
	public void expectGeometryPolyline() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("geometry_format", "polyline")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.body("routes[0].geometry_format", is("polyline"))
				//.body("routes[0].geometry", hasSize(243))
				.statusCode(200);
	}

	@Test
	public void expectGeometryEncodedPolyline() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("geometry_format", "encodedpolyline")
				.when()
				.get(getEndPointName())
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

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("geometry_format", "geojson")
				.param("elevation", "true")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.body("routes[0].geometry_format", is("geojson"))
				.body("routes[0].geometry.type", is("LineString"))
				.body("routes[0].geometry.coordinates[0]", hasSize(3))
				.statusCode(200);
	}

	@Test
	public void expectNoElevation() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("geometry_format", "geojson")
				.param("elevation", "false")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('geometry')", is(true))
				.body("routes[0].geometry_format", is("geojson"))
				.body("routes[0].geometry.type", is("LineString"))
				.body("routes[0].geometry.coordinates[0]", hasSize(2))
				.statusCode(200);
	}

	@Test
	public void expectExtrainfo() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "true")
				.param("extra_info", getParameter("extra_info"))
				.when()
				.get(getEndPointName())
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

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.param("geometry", "false")
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(false))
				.statusCode(200);
	}

	@Test
	public void expectUnknownProfile() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", "driving-car123")
				.param("geometry", "true")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expect400201() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.MISSING_PARAMETER))
				.statusCode(400);
	}

	@Test
	public void expect400202() {

		given()
				.param("coordinates", getParameter("coordinatesShortFaulty"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expect400203() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.param("language", "yuhd")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expect400204() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.REQUEST_EXCEEDS_SERVER_LIMIT))
				.statusCode(400);
	}

	@Test
	public void expectOptions() {

		JSONObject options = new JSONObject();
		options.put("avoid_features", "unpavedroads|tracks|fords");
		options.put("maximum_speed", "105");

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("info.query.options.containsKey('avoid_features')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectAvoidablesError() {

		JSONObject options = new JSONObject();
		options.put("avoid_features", "highwayss|tolllways|f3erries");

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectAvoidpolygons() {

		// options for avoid polygon
		JSONObject options = new JSONObject();
		options.put("avoid_features", "tunnels");
		options.put("maximum_speed", "75");
		JSONObject polygon = new JSONObject();
		polygon.put("type", "Polygon");
		String[][][] coords = new String[][][] { { { "8.91197", "53.07257" }, { "8.91883", "53.06081" },
				{ "8.86699", "53.07381" }, { "8.91197", "53.07257" } } };
		polygon.put("coordinates", coords);
		options.put("avoid_polygons", polygon);

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("info.query.options.containsKey('avoid_polygons')", is(true))
				.statusCode(200);
	}

	@Test
	public void expectAvoidpolygonsError() {

		// options for avoid polygon faulty
		JSONObject options = new JSONObject();
		options.put("avoid_features", "tunnels");
		options.put("maximum_speed", "75");
		JSONObject polygon = new JSONObject();
		polygon.put("type", "Polygon");
		String[][][] coords = new String[][][] { { { "8b.91197", "53a.07257" }, { "c8.91883", "53.06081" },
				{ "8.86699", "53.07381" }, { "8.91197", "d53.07257" } } };
		polygon.put("coordinates", coords);
		options.put("avoid_polygons", polygon);

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
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

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("profile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_JSON_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectCyclingToRejectHgvAvoidables() {
		// options for HGV profiles
		JSONObject options = new JSONObject();
		options.put("avoid_features", "highways|tollways|ferries|tunnels|unpavedroads|tracks|fords");
		options.put("maximum_speed", "75");
		JSONObject profileParams = new JSONObject();
		profileParams.put("width", "5");
		profileParams.put("height", "3");
		profileParams.put("length", "10");
		profileParams.put("axleload", "2");
		profileParams.put("weight", "5");
		profileParams.put("hazmat", "true");
		options.put("profile_params", profileParams);

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", "cycling-road")
				.param("options", options.toString())
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectCarToRejectWalkingAvoidables() {

		// options for walking profiles
		JSONObject options = new JSONObject();
		options.put("avoid_features", "steps|fords");
		options.put("maximum_speed", "5");

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("options", options.toString())
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}

	@Test
	public void expectMaximumspeedError() {

		// options for cycling profiles
		JSONObject options = new JSONObject();
		options.put("maximum_speed", "25fgf");

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("options", options.toString())
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_FORMAT))
				.statusCode(400);
	}

	@Test
	public void expectBearingsFormatError() {
		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("bearings", "50,50|50,50|100,100")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);

		given()
		.param("coordinates", getParameter("coordinatesShort"))
		.param("preference", getParameter("preference"))
		.param("geometry", "true")
		.param("profile", getParameter("carProfile"))
		.param("bearings", "50k,50|50,50")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}

	@Test
	public void expectRadiusesFormatError() {
		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("radiuses", "50|50|100")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);

		given()
		.param("coordinates", getParameter("coordinatesShort"))
		.param("preference", getParameter("preference"))
		.param("geometry", "true")
		.param("profile", getParameter("carProfile"))
		.param("radiuses", "h50|50")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
		.statusCode(400);
	}

	@Test
	public void expectNoNearestEdge() {
		given()
				.param("coordinates", "8.689585,49.399733|8.686495,49.40349")
				.param("preference", "fastest")
				.param("geometry", "true")
				.param("profile", "cycling-regular")
				.param("radiuses", "5|150")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.POINT_NOT_FOUND))
				.statusCode(404);
	}
	@Test
	public void expectUnknownUnits() {

		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", getParameter("carProfile"))
				.param("units", "j")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("error.code", is(RoutingErrorCodes.INVALID_PARAMETER_VALUE))
				.statusCode(400);
	}
}
