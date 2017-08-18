package heigit.ors.services.routing;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.json.JSONObject;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

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
				.body("routes[0].summary.distance", is(13357.2f))
				.body("routes[0].summary.duration", is(5496.7f))
				.body("routes[0].summary.ascent", is(341.9f))
				.body("routes[0].summary.descent", is(332.5f))
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
				.body("routes[0].segments[0].distance", is(7112.5f))
				.body("routes[0].segments[0].duration", is(3852.1f))
				.body("routes[0].segments[1].distance", is(6244.7f))
				.body("routes[0].segments[1].duration", is(1644.6f))
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
								"yrqlHkn~s@sqT\\jG}IVrHpE@bInLKpD~@SdCy@YpBi@S|@JIAJBi@LBkBb@?iBL@cA@EkAOAy@SAUI?KICGICIICoACQeA?Q@BAk@DEiA`@Ck@RCk@RAQ?Gw@sAgEX_X?BcBBpAyFDlB{O?F]Bj@?Ep@@?JD?d@F?Rh@?Fh@?Tn@PAn@xAKvIzA?tOx@@pEvIS|Sd@A~@dAEeAZAq@F?s@X?u@pACoFR@kAJ?mAN@oA@@oAH?sAPEuAB?wABCyALKuAFGoAVAw@zFKqBdAA?rACq@N?SD?SNHSVBSr@?g@V?SJAeAZAeATEeAFAeAPEeA\\MeAf@QkCLG?PKD^UVh@e@v@VU\\A^RlAwApEPGxAdBcBzOBMRASRdAsAbDRo@`Ah@sA`AvB}Cd@n@[?vAyBp@ZEf@nAyCdCpDwFsFRc@a@@[[BGUNi@QPm@QRs@gAHc@o@DQk@?]k@F_@i@Fm@gABUcAAUcAASaAC]aAMo@s@EOs@GMq@QgCwG_@mIuHC[?C]IAIIAQIAKIMyBg@?QG?GG?GE?QEB@CD?CBCCDCA?CA@CA@G??I?AG??QH[YRWQRKKRSQROGRw@Uz@u@Sf@MEHDQHBO?@E?BG?x@yB]?CS@CSHQSBEq@FEq@ROq@BEq@JIm@NOk@RSg@HIg@ROg@pAo@{ENKeALKcBh@q@gEHIcBBCcB~Ay@sNTa@mBVu@mDHe@oAH[oAJ[qERWoFJIeFTQ{EPM{ENU}D\\_A{JNo@_IF}@wRAoBwp@?]aM?YqMH{BkbAByCsoA?u@_b@@o@mL?[mL@GmL@GaKD]gHNc@{FT[qEVUqE@?qE@SkDFSgCx@{AqP`@cAoIx@eDyZZw@eRr@}Agh@V_Am[BKaMFE{HFGwG@KwG?mAoPNi@aI@oAuQ[gCmg@GQgJIMgJCOgJa@qEgu@_@qBw[Ws@cKSWuFUEiFaAa@kS]GkC[DkCE@kCGCoCy@w@sVcAq@ed@aBmAsx@QOgGCGkFuAqBma@i@m@}AGEsFUSyFaCeCyv@iDuCebBc@x@eSU`@gJKNiJSHkJi@F{UE@oJi@k@sUc@e@_REGgG[RqFQNyFEHcFGEmEECwDCEcCM_@mBg@kBjMI_@~Cn@E`@DA]TOa@Ga@g@MgA}B]qC{JIcAeHIaDyX@GuC@Iu@@If@BIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC_BDGaCHK_Db@c@yKZ[yFj@_@oPbAWuRbAOmBdAG{@fABoAj@HtAzE~@dCpADuHh@CgEZA}A@GyABCwAvDiAyo@\\OqEFCcED?sDz@G{LhAQoPfCi@_NlGk@bJmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCi@BzGqAEhV{E_Aju@k@IbEgAC`JeAFbCcANAcAViAk@^_A[Za@c@b@mAIJk@EFREBRDFRITREDRCFRMJRo@h@lBCDdACHvGAHvGAHvGAFvGH`Dt\\HbA~E\\pC`WLfArIF`@hDBPhDR`AXTz@z@JTz@DFxAb@d@~Eh@j@xFPNdA^f@rBv@nAw@^x@tFb@fApLf@v@bBjAbBdF`@t@xKv@nCtn@~@nCdMNnAlLLfCl[G`AjGI`@vAWl@bM[`@zPaBdAju@q@n@xZY\\bGg@t@nKUh@jCUx@hNS~@`KcBvMbrA?F`C?DfE@DlGBF|FFLlFIL|Ei@f@jKw@h@`VaCrAxl@aAT`K{@FfJm@@nFg@GfJ{@[rI_Ae@pEgAu@bQm@q@xFwCiEhN[Up@m@U`Jk@KhIeA@pOGA`H?LvGbAXtWj@h@~MLN`FJJXT\\fDHd@~CD`AjHEhA`F@PbBHl@bBPn@fEApAjJBZpEPj@pELTpEHNpEHLtMEBtMM?tME?rSOC|Nw@Mp^QMrDYe@lTo@iBvq@SYzEECzEKGzEYIzE]BzEODrDo@d@fR@D|IV?|IFDrLDLjOPjA`WAJxCKRjHCDjHCHjHQx@hXCy@pYAMdKEAdKKpA`WCT`FGZjCE\\jC?z@fOA@jHA?`M[kBvj@??jCC@dAGH?uBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@zBoHhBls@K?`BSCxAGBnAO@hAD^dAY@`AC?|@[B`AmJ`B{Le@ReDOFkAO?gAqADeHqAFyFK?s@I@o@gA?gD{@B}AO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
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
				.body("routes[0].way_points", hasItems(0, 299, 533))
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
				.body("routes[0].bbox", hasItems(8.687794f, 49.393274f, 8.714833f, 49.424601f))
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
				.body("routes[0].bbox", hasItems(8.687794f, 49.393274f, 8.714833f, 49.424601f))
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
				.body("routes[0].extras.surface.values.size()", is(35))
				.body("routes[0].extras.surface.values[34][1]", is(533))
				.body("routes[0].extras.suitability.values[30][0]", is(452))
				.body("routes[0].extras.steepness.values[11][1]", is(339))

				.statusCode(200);
	}

	@Test
	public void testAvoidTrailDifficulty() {
		given()
				.param("coordinates", "8.711343,49.401186|8.738122,49.402275")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "cycling-mountain")
				.param("extra_info", "traildifficulty")
				.param("options", "{\"profile_params\":{\"restrictions\":{\"trail_difficulty\":1}}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
				.body("routes[0].segments[0].steps.size()", is(15))
				.body("routes[0].segments[0].distance", is(3633.8f))
				.body("routes[0].segments[0].duration", is(2019.4f))
				.body("routes[0].extras.traildifficulty.values.size()", is(2))
				.body("routes[0].extras.traildifficulty.values[0][0]", is(0))
				.body("routes[0].extras.traildifficulty.values[0][1]", is(109))
				.body("routes[0].extras.traildifficulty.values[0][2]", is(0))
				.body("routes[0].extras.traildifficulty.values[1][0]", is(109))
				.body("routes[0].extras.traildifficulty.values[1][1]", is(141))
				.body("routes[0].extras.traildifficulty.values[1][2]", is(1))
				.statusCode(200);
	}
	
	@Test
	public void testTrailDifficultyExtraDetails() {
		given()
				.param("coordinates", "8.763442,49.388882|8.762927,49.397541")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "cycling-regular")
				.param("extra_info", "suitability|traildifficulty")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
				.body("routes[0].extras.traildifficulty.values.size()", is(2))
				.body("routes[0].extras.traildifficulty.values[0][0]", is(0))
				.body("routes[0].extras.traildifficulty.values[0][1]", is(16))
				.body("routes[0].extras.traildifficulty.values[0][2]", is(2))
				.body("routes[0].extras.traildifficulty.values[1][0]", is(16))
				.body("routes[0].extras.traildifficulty.values[1][1]", is(31))
				.body("routes[0].extras.traildifficulty.values[1][2]", is(0))
				.statusCode(200);
		
		given()
			.param("coordinates", "8.724174,49.390223|8.716536,49.399622")
			.param("instructions", "true")
			.param("preference", "fastest")
			.param("profile", "foot-hiking")
			.param("extra_info", "traildifficulty")
			.when()
			.get(getEndPointName())
			.then()
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
	}
	
	@Test
	public void testTollwaysExtraDetails() {
		given()
				.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "driving-car")
				.param("extra_info", "suitability|tollways")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
				.body("routes[0].extras.tollways.values.size()", is(1))
				.body("routes[0].extras.tollways.values[0][0]", is(0))
				.body("routes[0].extras.tollways.values[0][1]", is(101))
				.body("routes[0].extras.tollways.values[0][2]", is(0))
				.statusCode(200);
		
		given()
				.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "driving-hgv")
				.param("extra_info", "suitability|tollways")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
				.body("routes[0].extras.tollways.values.size()", is(1))
				.body("routes[0].extras.tollways.values[0][0]", is(0))
				.body("routes[0].extras.tollways.values[0][1]", is(86))
				.body("routes[0].extras.tollways.values[0][2]", is(0))
				.statusCode(200);
		
		 given()
				.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "driving-hgv")
				.param("options", "{\"profile_params\":{\"width\":\"2\",\"height\":\"2\",\"weight\":\"14\"},\"vehicle_type\":\"hgv\"}")
				.param("extra_info", "suitability|tollways")
				.when()
				.get(getEndPointName())
				.then()
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
				.body("routes[0].segments[0].steps.size()", is(46))
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
				.body("routes[0].segments[0].steps.size()", is(46))
				.body("routes[0].segments[1].steps.size()", is(31))
				.body("routes[0].segments[0].steps[0].distance", is(511.4f))
				.body("routes[0].segments[0].steps[0].duration", is(230.1f))
				.body("routes[0].segments[0].steps[0].type", is(11))
				.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
				.body("routes[0].segments[0].steps[10].distance", is(303.2f))
				.body("routes[0].segments[0].steps[10].duration", is(60.6f))
				.body("routes[0].segments[0].steps[10].type", is(5))
				.body("routes[0].segments[0].steps[10].instruction", is("Turn slight right onto Mittermaierstraße"))
				.statusCode(200);
	}
	
	// test fitness params bike..

}
