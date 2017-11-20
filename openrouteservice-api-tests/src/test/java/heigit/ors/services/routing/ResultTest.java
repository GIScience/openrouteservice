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

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "routes")
public class ResultTest extends ServiceTest {

	public ResultTest() {

		addParameter("coordinatesShort", "8.680916,49.410973|8.687782,49.424597");
		addParameter("coordinatesLong", "8.680916,49.410973|8.714733,49.393267|8.687782,49.424597");
		addParameter("extra_info", "surface|suitability|steepness");
		addParameter("preference", "fastest");
		addParameter("bikeProfile", "cycling-regular");
		addParameter("carProfile", "driving-car");
	}

	@Test
	public void expectCarToRejectBikeParams() {

		// options for cycling profiles
		JSONObject options = new JSONObject();
		JSONObject profileParams = new JSONObject();
		profileParams.put("maximum_gradient", "5");
		profileParams.put("difficulty_level", "1");
		options.put("profile_params", profileParams);

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
		.statusCode(200);
	}

	@Test
	public void expectSegmentsToMatchCoordinates() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('segments')", is(true))
		.body("routes[0].segments.size()", is(2))
		.statusCode(200);
	}

	@Test
	public void testSummary() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.param("elevation", "true")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('segments')", is(true))
		.body("routes[0].segments.size()", is(2))
		.body("routes[0].summary.distance", is(12270.9f))
		.body("routes[0].summary.duration", is(3461.3f))
		.body("routes[0].summary.ascent", is(346.8f))
		.body("routes[0].summary.descent", is(337.4f))
		.statusCode(200);
	}

	@Test
	public void testSegmentDistances() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('segments')", is(true))
		.body("routes[0].segments.size()", is(2))
		.body("routes[0].segments[0].distance", is(6418.2f))
		.body("routes[0].segments[0].duration", is(2420.8f))
		.body("routes[0].segments[1].distance", is(5852.7f))
		.body("routes[0].segments[1].duration", is(1040.5f))
		.statusCode(200);
	}

	@Test
	public void testEncodedPolyline() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.param("elevation", "true")
		.when()
		.log().all()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body(
				"routes[0].geometry",
				is(
						"yrqlHkn~s@sqT\\jG}IVrHpE@bInLKpD~@SdCy@YpBi@S|@JIAJBi@LBkBb@?iBL@cA@EkAOAy@SAUI?KICGICIICoACQeA?Q@BAk@DEiA`@Ck@RCk@RAQ?Gw@sA?Cq@Ek@q@S{BaFScBqEa@mCuJQmAaCM{@cBNEIC]RMaBdBM_CbHCIxAKcApDAItA?GrAGqArEEsA\\Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JHAd@f@K?dAAw@RDAF~HsAxDF?RF@RF@RB@RAQPAKN?GNAILAKJAUJ@OHAQHJSFE]_@OcBiBO_CuDCq@q@IoAcB]gE}IEm@q@Em@q@OqBaAAOOEs@VCsAvAAM\\CS\\HM\\BI\\BC\\HE\\FA\\D?\\D@\\DD\\@E\\Lg@|@?C`A?A`A@EdALwBlBFYyAFSyALOyAPIyA~@OmGn@IsDB?yAF?cBFAcB`B_@cSjA]qHzAa@p@BAHHCHAMHAy@\\AuAz@C}CX?YOG{DsA?aASGmEaAE_CNEmBS?MOF?Sx@@{@B?S@?SJ?q@VGq@@Jq@DrAuC?FSB~@g@DAMJAILCE@_@?@K?BiAk@VmD{CF]IH[I`@cA{@@?g@@Eg@NQsDBCsD^_@gJtAsAuWr@a@sWFEaIFC_IHE_IHC_IPG}Hn@WaSh@a@sNJMiMDOmM?OkMCIcL?AcLFGcL`@g@gYHOgJBEkH@?oF@@oFD?oFN?oF??oF@?oFBCoFDEoFBEoFJMoF@Sf@?Af@ACf@EG?ES?AU{@DO{@JK{@@G{@?C{@?C{@@M{@AM{@AA{@BC{@@G{@HQ{@?E{@CM{@EG{@JUwGT]wG@AwG??wGD@aHBFeFBv@{L?R{E?h@{EDx@wLNnA_XAl@aH?BaHAFaHc@tA]A@nF?@nF?FnF@BxFGJxFALrF?LnF@@nFJNnF@@gEh@a@wJC\\yCI^aC@@aCFFaCFFaCADcMAJmPARsS@?sSXu@}]JKwL\\a@oZRWwLtA_Cqr@dAcD_NLm@bBL{@aCHw@_FD}@{E@yBuc@ToDmbAJo@qKPiAaXLdBkf@^vAah@DbAaW^lCsDZ`BtCZr@{JNJ}GN?kHbAMeYhAO{u@xBMelBn@Eun@DAiXTOiSGa@iNMgAs]]qC_iAIcAsTIaDeb@@GuC@Iu@@If@BIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC_BDGaC@G_D@G}DDK{EBGyFTc@wGVi@oPN[wGBBeArAo@mQv@UaM|BS{TdBJkMjEd@kRvBN_@fCSaFh@@_DVDiAZHgA\\OaAFC_AD?{@z@GyFhAQmNfCi@kDlGk@pKmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@[I]WE]i@Ay@gCRfNwBO~OkEe@lTeBKoA}BRrHw@TzBsAn@xJCC~AOZzAWh@jDUb@nACFnAEJnAAFnAAFnAEFtCEBtCDFtCITtCEDtCCFtCDFdCNTtB?LdBQnI|q@GhBtMEn@hDAJhDF`@hDBPhDR`ApLTz@hLJTbEDFxDb@d@dHh@j@|EPNzA^f@tCv@nAg@^x@tFb@fApLf@v@bBjAbBdF`@t@xKv@nCtn@~@nCdMNnAlLLfCl[G`AjGI`@vAWl@bM[`@zPaBdAju@q@n@xZY\\bGg@t@nKUh@jCUx@hNS~@`KcBvMbrA?F`C?DfE@DlGBF|FFLlFIL|Ei@f@jKw@h@`VaCrAxl@aAT`K{@FfJm@@nFg@GfJ{@[rI_Ae@pEgAu@bQm@q@xFwCiEhN[Up@m@U`Jk@KhIeA@pOGA`H?LvGbAXtWj@h@~MLN`FJJXT\\fDHd@~CD`AjHEhA`F@PbBHl@bBPn@fEApAjJBZpEPj@pELTpECHpEANtM?FtMOCtMw@Mfc@QMxFYe@~Zo@iBns@SYhIECpGKGzEYIzE]BzEODvLo@d@jZ@D|IV?|IFDrLDLjOPjA`WAJxCKRjHCDjHCHjHQx@hXCy@pYAMdKEAdKKpA`WCT`FGZjCE\\jC?z@fOA@jHA?`M[kBvj@??jCC@dAGH?uBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@zBoHhBls@K?`BSCxAGBnAO@hAUJdACB`AEB|@oIxApDE@Sk@HaCG?mA[BkAU@kAG^iACBiAqADkIqAFwIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
		.statusCode(200);
	}

	@Test
	public void testWaypoints() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].way_points", hasItems(0, 330, 563))
		.statusCode(200);
	}

	@Test
	public void testBbox() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].bbox", hasItems(8.687794f, 49.393272f, 8.714833f, 49.424603f))
		.statusCode(200);
	}

	@Test
	public void testManeuver() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.param("maneuvers", "true")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].bbox", hasItems(8.687794f, 49.393272f, 8.714833f, 49.424603f))
		.body("routes[0].segments[0].steps[0].maneuver.bearing_before", is(0))
		.body("routes[0].segments[0].steps[0].maneuver.bearing_after", is(260))
		.body("routes[0].segments[0].steps[0].maneuver.containsKey('location')", is(true))
		.body("routes[0].segments[0].steps[1].maneuver.bearing_before", is(298))
		.body("routes[0].segments[0].steps[1].maneuver.bearing_after", is(4))
		.body("routes[0].segments[0].steps[1].maneuver.location", hasItems(8.673925f, 49.411283f))
		.statusCode(200);
	}

	@Test
	public void testExtras() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.param("extra_info", getParameter("extra_info"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.containsKey('surface')", is(true))
		.body("routes[0].extras.containsKey('suitability')", is(true))
		.body("routes[0].extras.containsKey('steepness')", is(true))
		.statusCode(200);
	}

	@Test
	public void testExtrasDetails() {

		Response response = given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("bikeProfile"))
				.param("extra_info", getParameter("extra_info"))
				.when()
				.get(getEndPointName());

		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.surface.values.size()", is(55))
		.body("routes[0].extras.surface.values[34][1]", is(261))
		.body("routes[0].extras.suitability.values[30][0]", is(357))
		.body("routes[0].extras.steepness.values[11][1]", is(317))
		.statusCode(200);
		
		checkExtraConsistency(response);
	}

	@Test
	public void testExtrasConsistency() {

		Response response = given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("bikeProfile"))
				.param("extra_info", "surface|suitability|avgspeed|steepness")
				.when()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);

		checkExtraConsistency(response);
	}

	@Test
	public void testAvoidTrailDifficulty() {
		Response response = given()
		.param("coordinates", "8.711343,49.401186|8.738122,49.402275")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "cycling-mountain")
		.param("extra_info", "traildifficulty")
		.param("options", "{\"profile_params\":{\"restrictions\":{\"trail_difficulty\":1}}}")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].segments[0].steps.size()", is(18))
		.body("routes[0].segments[0].distance", is(4310.5f))
		.body("routes[0].segments[0].duration", is(1628.5f))
		.body("routes[0].extras.traildifficulty.values.size()", is(4))
		.body("routes[0].extras.traildifficulty.values[0][0]", is(0))
		.body("routes[0].extras.traildifficulty.values[0][1]", is(52))
		.body("routes[0].extras.traildifficulty.values[0][2]", is(0))
		.body("routes[0].extras.traildifficulty.values[1][0]", is(52))
		.body("routes[0].extras.traildifficulty.values[1][1]", is(61))
		.body("routes[0].extras.traildifficulty.values[1][2]", is(1))
		.statusCode(200);
		
		checkExtraConsistency(response);
	}

	@Test
	public void testTrailDifficultyExtraDetails() {
		Response response = given()
		.param("coordinates", "8.763442,49.388882|8.762927,49.397541")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "cycling-regular")
		.param("extra_info", "suitability|traildifficulty")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.traildifficulty.values.size()", is(3))
		.body("routes[0].extras.traildifficulty.values[0][0]", is(0))
		.body("routes[0].extras.traildifficulty.values[0][1]", is(2))
		.body("routes[0].extras.traildifficulty.values[0][2]", is(2))
		.body("routes[0].extras.traildifficulty.values[1][0]", is(2))
		.body("routes[0].extras.traildifficulty.values[1][1]", is(20))
		.body("routes[0].extras.traildifficulty.values[1][2]", is(1))
		.statusCode(200);

		checkExtraConsistency(response);
		
		response = given()
		.param("coordinates", "8.724174,49.390223|8.716536,49.399622")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "foot-hiking")
		.param("extra_info", "traildifficulty")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.traildifficulty.values.size()", is(3))
		.body("routes[0].extras.traildifficulty.values[0][0]", is(0))
		.body("routes[0].extras.traildifficulty.values[0][1]", is(12))
		.body("routes[0].extras.traildifficulty.values[0][2]", is(0))
		.body("routes[0].extras.traildifficulty.values[1][0]", is(12))
		.body("routes[0].extras.traildifficulty.values[1][1]", is(27))
		.body("routes[0].extras.traildifficulty.values[1][2]", is(1))
		.body("routes[0].extras.traildifficulty.values[2][0]", is(27))
		.body("routes[0].extras.traildifficulty.values[2][1]", is(30))
		.body("routes[0].extras.traildifficulty.values[2][2]", is(0))
		.statusCode(200);
		
		checkExtraConsistency(response);
	}

	@Test
	public void testTollwaysExtraDetails() {
		Response response = given()
		.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "driving-car")
		.param("extra_info", "suitability|tollways")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.tollways.values.size()", is(1))
		.body("routes[0].extras.tollways.values[0][0]", is(0))
		.body("routes[0].extras.tollways.values[0][1]", is(101))
		.body("routes[0].extras.tollways.values[0][2]", is(0))
		.statusCode(200);
		
		checkExtraConsistency(response);

		response = given()
		.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "driving-hgv")
		.param("extra_info", "suitability|tollways")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.tollways.values.size()", is(1))
		.body("routes[0].extras.tollways.values[0][0]", is(0))
		.body("routes[0].extras.tollways.values[0][1]", is(86))
		.body("routes[0].extras.tollways.values[0][2]", is(0))
		.statusCode(200);
		
		checkExtraConsistency(response);

		response = given()
		.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
		.param("instructions", "true")
		.param("preference", "fastest")
		.param("profile", "driving-hgv")
		.param("continue_straight", "false")
		.param("options", "{\"profile_params\":{\"width\":\"2\",\"height\":\"2\",\"weight\":\"14\"},\"vehicle_type\":\"hgv\"}")
		.param("extra_info", "suitability|tollways")
		.when()
		.get(getEndPointName());
		
		response.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].containsKey('extras')", is(true))
		.body("routes[0].extras.tollways.values.size()", is(3))
		.body("routes[0].extras.tollways.values[0][0]", is(0))
		.body("routes[0].extras.tollways.values[0][1]", is(52))
		.body("routes[0].extras.tollways.values[0][2]", is(0))
		.body("routes[0].extras.tollways.values[1][0]", is(52))
		.body("routes[0].extras.tollways.values[1][1]", is(66))
		.body("routes[0].extras.tollways.values[1][2]", is(1))
		.body("routes[0].extras.tollways.values[2][0]", is(66))
		.body("routes[0].extras.tollways.values[2][1]", is(86))
		.body("routes[0].extras.tollways.values[2][2]", is(0))
		.statusCode(200);
		
		checkExtraConsistency(response);
	}
	
	@Test
	public void testOptimizedAndTurnRestrictions() {
		given()
		.param("coordinates", "8.684081,49.398155|8.684703,49.397359")
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", "driving-car")
		.param("optimized", "false")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].summary.distance", is(693.8f))
		.statusCode(200);
	}

	@Test
	public void testNoBearings() {
		given()
		.param("coordinates", "8.688694,49.399374|8.686495,49.40349")
		.param("preference", "fastest")
		.param("geometry", "true")
		.param("profile", "cycling-regular")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].summary.distance", is(620.1f))
		.statusCode(200);
	}
	
	@Test
	public void testBearingsForStartAndEndPoints() {
		given()
		.param("coordinates", "8.688694,49.399374|8.686495,49.40349")
		.param("preference", "fastest")
		.param("geometry", "true")
		.param("profile", "cycling-regular")
		.param("bearings", "25,30|90,20")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].summary.distance", is(804.9f))
		.statusCode(200);
	}
	
	@Test
	public void testBearingsExceptLastPoint() {
		given()
		.param("coordinates", "8.688694,49.399374|8.686495,49.40349")
		.param("preference", "fastest")
		.param("geometry", "true")
		.param("profile", "cycling-regular")
		.param("bearings", "25,30")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].summary.distance", is(647.2f))
		.statusCode(200);
	}
	
	@Test
	public void testBearingsSkipwaypoint() {
		given()
		.param("coordinates", "8.688694,49.399374|8.686495,49.40349")
		.param("preference", "fastest")
		.param("geometry", "true")
		.param("profile", "cycling-regular")
		.param("bearings", "|90,20")
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].summary.distance", is(714.7f))
		.statusCode(200);
	}
	
	@Test
	public void testSteps() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].segments[0].containsKey('steps')", is(true))
		.body("routes[0].segments[1].containsKey('steps')", is(true))
		.body("routes[0].segments[0].steps.size()", is(55))
		.body("routes[0].segments[1].steps.size()", is(31))
		.statusCode(200);
	}

	@Test
	public void testStepsDetails() {

		given()
		.param("coordinates", getParameter("coordinatesLong"))
		.param("instructions", "true")
		.param("preference", getParameter("preference"))
		.param("profile", getParameter("bikeProfile"))
		.when()
		.get(getEndPointName())
		.then()
		.assertThat()
		.body("any { it.key == 'routes' }", is(true))
		.body("routes[0].segments[0].containsKey('steps')", is(true))
		.body("routes[0].segments[1].containsKey('steps')", is(true))
		.body("routes[0].segments[0].steps.size()", is(55))
		.body("routes[0].segments[1].steps.size()", is(31))
		.body("routes[0].segments[0].steps[0].distance", is(511.4f))
		.body("routes[0].segments[0].steps[0].duration", is(230.1f))
		.body("routes[0].segments[0].steps[0].type", is(11))
		.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
		.body("routes[0].segments[0].steps[10].distance", is(74))
		.body("routes[0].segments[0].steps[10].duration", is(22.2f))
		.body("routes[0].segments[0].steps[10].type", is(0))
		.body("routes[0].segments[0].steps[10].instruction", is("Turn left"))
		.statusCode(200);
	}

	private void checkExtraConsistency(Response response)
	{
		JSONObject jResponse = new JSONObject(response.body().asString());

		JSONObject jRoute = (jResponse.getJSONArray("routes")).getJSONObject(0);
		double routeDistance = jRoute.getJSONObject("summary").getDouble("distance");
		JSONObject jExtras = (jResponse.getJSONArray("routes")).getJSONObject(0).getJSONObject("extras");

		JSONArray jExtraNames = jExtras.names();
		for(int i = 0; i < jExtraNames.length(); i++)
		{
			String name = jExtraNames.getString(i);
			JSONArray jExtraValues = jExtras.getJSONObject(name).getJSONArray("values");

			JSONArray jValues = jExtraValues.getJSONArray(0);
			int fromValue = jValues.getInt(0);
			int toValue = jValues.getInt(1);
			Assert.assertEquals(fromValue < toValue, true);

			for(int j = 1; j < jExtraValues.length(); j++)
			{
				jValues = jExtraValues.getJSONArray(j);
				int fromValue1 = jValues.getInt(0);
				int toValue1 = jValues.getInt(1);

				Assert.assertEquals(fromValue1 < toValue1, true);
				Assert.assertEquals(fromValue1 == toValue, true);

				fromValue = fromValue1;
				toValue = toValue1;
			}


			JSONArray jSummary = jExtras.getJSONObject(name).getJSONArray("summary");
			double distance = 0.0;
			double amount = 0.0;

			for(int j = 0; j < jSummary.length(); j++)
			{
				JSONObject jSummaryValues = jSummary.getJSONObject(j);
				distance += jSummaryValues.getDouble("distance");
				amount += jSummaryValues.getDouble("amount");
			}

			Assert.assertEquals(Math.abs(routeDistance - distance) < 0.5, true);

			Assert.assertEquals(Math.abs(amount - 100.0) < 0.1, true);
		}
	}

	// test fitness params bike..


	@Test
	public void testBorders() {
		// Test that border crossings work. Hard borders (1) are those that are closed/controlled, and soft borders (2) are those that are open

		// Uses dummy data that give some ways in Heidelberg hard borders and some soft borders
		// With option 2, the route can cross soft borders, but not hard borders
		given()
				.param("coordinates", "8.688301,49.404454|8.684266,49.404223")
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"profile_params\":{\"weightings\":{\"borders\":{\"level\":2}}}}")
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(292.8f))
				.body("routes[0].summary.duration", is(210.8f))
				.statusCode(200);

		// Option 1 signifies that the route should not cross any borders
		given()
				.param("coordinates", "8.688301,49.404454|8.684266,49.404223")
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"profile_params\":{\"weightings\":{\"borders\":{\"level\":1}}}}")
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(1255.5f))
				.body("routes[0].summary.duration", is(360.9f))
				.statusCode(200);

		// Option 0 signifies that no borders are taken into account when routing, so the route can cross any borders
		given()
				.param("coordinates", "8.688301,49.404454|8.684266,49.404223")
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"profile_params\":{\"weightings\":{\"borders\":{\"level\":0}}}}")
				.when()
				.log().all()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(292.8f))
				.body("routes[0].summary.duration", is(210.8f))
				.statusCode(200);

	}

}
