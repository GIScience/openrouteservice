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
				.body("routes[0].summary.distance", is(13583.9f))
				.body("routes[0].summary.duration", is(5587.6f))
				.body("routes[0].summary.ascent", is(342.4f))
				.body("routes[0].summary.descent", is(333))
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
				.body("routes[0].segments[0].distance", is(7236.2f))
				.body("routes[0].segments[0].duration", is(3922.8f))
				.body("routes[0].segments[1].distance", is(6347.7f))
				.body("routes[0].segments[1].duration", is(1664.8f))
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
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body(
						"routes[0].geometry",
						is(
								"yrqlHkn~s@sqT\\jG}IVrHpE@bInLKpD~@SdCgAYpB]S|@NIALBi@NBkBb@@mDHI}C]G[ICoABQeA?Q@?Ak@BEiA^Ck@RCk@RAQ?Gw@yAgEX}X?BcBBpAyFDlB{O?F]Bj@?Ep@@?JD?d@F?Rh@?Fh@?Tn@PAn@xAKvIzA?tOx@@pEvIS|Sd@A~@dAEeAZAq@F?s@X?u@pACoFR@kAJ?mAN@oA@@oAH?sAPEuAB?wABCyALKuAFGoAVAw@zFKqBdAA?rACq@N?SD?SNHSVBSr@?g@V?SJAeAZAeATEeAFAeAPEeA\\MeAf@QkCLG?PKD^UVh@e@v@VU\\A^RlAwApEPGxAdBcBzOBMRASRdAsA`ERo@fAh@sAt@vB}C`@n@[?vAyB\\ZEf@nAyCdCpDwFsFRc@a@@[[BGUNi@QPm@QRs@gAHc@o@DQk@?]k@F_@i@Fm@gABUcAAUcAASaAC]aAMo@s@EOs@GMq@QgC}I_@mIeFC[IC]IAIIAQIAKIMyBg@?QG?GG?GE?QEB@CD?CBCCDCA?CA@CA@G??I?AG??QH[YRWQRKKRSQROGRw@Uz@u@Sf@MEHDQHBO?@E?BG?x@yB]?CS@CSHQSBEq@FEq@ROq@BEq@JIm@NOk@RSg@HIg@ROg@pAo@{ENKeALKcBh@q@kHHIcBBCcB~Ay@uMTa@kBVu@oDHe@oAH[eAJ[gERWeFJI{ETQ{EPM}DNU}D\\_A{JNo@_IF}@wRAoBwp@?]aM?YqMH{BkbAByCsoA?u@_b@@o@mL?[mL@GmL@GaKD]gHNc@{FT[qEVUqE@?qE@SkDFSgCx@{AqP`@cAoIx@eDyZZw@eRr@}Agh@V_Am[BKaMFE{HFGwG@KwG?mAoPNi@aI@oAuQ[gCmg@GQgJIMgJCOgJa@qEgu@_@qBw[Ws@cKSWuFUEiFaAa@kS]GkC[DkCE@kCGCoCy@w@sVcAq@ed@aBmAsx@QOgGCGkFuAqBma@i@m@}AGEsFUSyFaCeCyv@iDuCebBc@x@eSU`@gJKNiJSHkJi@F{UE@oJi@k@sUc@e@_REGgG[RqFQNyFEHcFGEmEECwDCEcCM_@mBg@kBjMI_@~Cn@E`@DA]TOa@Ga@g@MgA}B]qC{JIcAeHIaDyX@GuC@Iu@@If@BIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC_BDGaCHK_Db@c@yKZ[yFj@_@oPbAWuRbAOmBdAG{@fABoAj@HtAzE~@dCpADuHh@CgEZA}A@GyABCwAvDiAyo@\\OqEFCcED?sDz@G{LhAQoPfCi@_NlGk@bJmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCi@BzGqAEhV{E_Aju@k@IbEgAC`JeAFbCcANAcAViAk@^_A[Za@c@b@mAIJk@EFREBRDFRITREDRCFRMJRo@h@lBCDdACHvGAHvGAHvGAFvGH`Dt\\HbA~E\\pC`WLfArIF`@hDBPhDR`AXTz@z@JTz@DFxAb@d@~Eh@j@xFPNdA^f@rBv@nAw@^x@tFb@fApLf@v@hDjAbBlG`@t@xKv@nCpm@~@nCzJNnAlLLfC~\\G`A|GI`@|AWl@tM[`@bQaBdAdu@q@n@jYY\\jFg@t@vJUh@jCUx@hNS~@`KcBvMbrA?F`C?DfE@DlGBF|FFLlFIL|Ei@f@jKw@h@`VaCrAxl@aAT`K{@FfJm@@nFg@GfJ{@[rI_Ae@pEgAu@bQm@q@xFwCiEhN[Up@m@U`Jk@KhIeA@pOGA`H?LvGbAXtWj@h@~MLN`FJJXT\\fDHd@~CD`AjHEhA`F@PbBHl@bBPn@fEApAjJBZpEPj@pELTpEHNpEHLtMEBtMM?tME?rSOC|Nw@Mp^QMrDYe@lTo@iBvq@SYzEECzEKGzEYIzE]BzEODrDo@d@nZ@D|IV?rLFDjODL`RPjAfRAJ?KRjHCDjHCHjHQx@b[Cy@pYAMdKEAdKKpAjRCTjCGZjCE\\jC?z@fOA@`MA?vQ[kBja@??dAC@?GHdAuBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@jCoHhB`s@K?~ASCvAGBnAO@hAUJdACB`AEB|@oIxAfDE@Sk@H_CG?kA[BkAU@kAG^gACBiAqADiIqAFuIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
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
				.body("routes[0].way_points", hasItems(0, 294, 531))
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
				.body("routes[0].extras.surface.values.size()", is(37))
				.body("routes[0].extras.surface.values[34][1]", is(525))
				.body("routes[0].extras.suitability.values[30][0]", is(447))
				.body("routes[0].extras.steepness.values[11][1]", is(358))

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
				.body("routes[0].segments[0].steps.size()", is(40))
				.body("routes[0].segments[1].steps.size()", is(21))
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
				.body("routes[0].segments[0].steps.size()", is(40))
				.body("routes[0].segments[1].steps.size()", is(21))
				.body("routes[0].segments[0].steps[0].distance", is(513.3f))
				.body("routes[0].segments[0].steps[0].duration", is(231))
				.body("routes[0].segments[0].steps[0].type", is(6))
				.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
				.body("routes[0].segments[0].steps[10].distance", is(191.7f))
				.body("routes[0].segments[0].steps[10].duration", is(35.2f))
				.body("routes[0].segments[0].steps[10].type", is(6))
				.body("routes[0].segments[0].steps[10].instruction", is("Continue straight onto Mittermaierstraße"))
				.statusCode(200);
	}
	
	// test fitness params bike..

}
