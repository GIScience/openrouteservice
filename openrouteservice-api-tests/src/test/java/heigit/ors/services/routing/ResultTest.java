package heigit.ors.services.routing;

import static io.restassured.RestAssured.*;
//import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name = "routes")
public class ResultTest extends ServiceTest {

	public ResultTest() {

		addParameter("coordinatesShort", "8.680916,49.410973|8.687782,49.424597");
		addParameter("coordinatesLong", "8.680916,49.410973|8.680916,49.210973|8.680026,49.210973|8.687782,49.424597");
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
				.body("routes[0].segments.size()", is(3))
				.statusCode(200);
	}

	@Test
	public void testSummary() {

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
				.body("routes[0].segments.size()", is(3))
				.body("routes[0].summary.distance", is(22422.6f))
				.body("routes[0].summary.duration", is(4784.7f))
				//.body("routes[0].summary.ascent", is(37.5f))
				//.body("routes[0].summary.descent", is(21))
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
				.body("routes[0].segments.size()", is(3))
				.body("routes[0].segments[0].distance", is(11344.6f))
				.body("routes[0].segments[0].duration", is(2537.8f))
				.body("routes[0].segments[1].distance", is(0))
				.body("routes[0].segments[1].duration", is(0))
				.body("routes[0].segments[2].distance", is(11078))
				.body("routes[0].segments[2].duration", is(2246.9f))
				.statusCode(200);
	}

	@Test
	public void testEncodedPolyline() {

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
				.body(
						"routes[0].geometry",
						is(
							"yrqlHkn~s@\\jGVrH@bIKpDSdCYpBS|@IABi@BkB@mDI}CG[CoAQeAQ@Ak@EiACk@Ck@AQGw@gEX?BBpADlB?FBj@Ep@?J?d@?R?F?TPAxAKzA?x@@vISd@AdAEZAF?X?pACR@J?N@@@H?PEB?BCLKFGVAzFKdAArACN?D?NHVBr@?V?JAZATEFAPE\\Mf@QBTLIFEHEh@Y^YPQlAwAPGdBcBBMASdAsARo@h@sAvB}Cn@[vAyBZEnAyCpDwFRc@@[BGNi@Pm@Rs@Hc@DQ?]F_@Fm@BUAUASC]Mo@EOGMQgC_@mIC[BGBEBGr@iB@CFG@AFA\\ADAZA@?@?TCD?JATCx@SNA`@EXGZOd@IJAf@Gj@I`@EjDYrFY?CR{DLcBxC?rCKrB]jDInAJfBEdAQv@Iv@@xAHrBD|FCn@AC|F?Dn@?`BCzBAD?dDD`BBdABtABB@~@DZD?DKjFpFHvCrGDfBPA`@Cf@E^AXAh@AH?f@AfAKxBIjBGP@PDNB~@HVAp@AFA~@G^E\\I`@OlFQvFSrEM`LY`EK~DQpI_@NFVAbAE\\A\\AJ?h@A|AGZCD?TCNAd@CzBM~BWNAj@O~AGtJk@tDOvAKvAMn@E`@EHAPAZCr@Cr@Cz@?F?nC?R?vB@@?D?|@BxFF`CH|@Bt@?r@@^@rA@hB@|D?B?rB?b@BF@ZD^FD@j@Ld@HJ?PBH@N@J@`@B`@DA`@KfC?F?B@DDP`AxAPZ\\r@N]v@aAR\\z@bBRVJLj@f@f@V\\J^@RCj@QxAu@ZQh@UHCTGf@GRA\\?R?bBDV?L?bAI@P?P@b@DnBhCOFA|@GBRLBLBAHNJh@TrAp@bAd@tCtAl@RHBZHTEDI@QDg@DgAB{ABgAL?zCRnAb@zE\\pJv@|AA|DQhBQ~AK_BJiBP}DP}A@qJw@{E]oAc@{CSM?CfACzAEfAEf@APEHUD[IICm@SuCuAcAe@sAq@i@UOK@IMCMCCS}@FG@iCNEoBAc@?QAQcAHM?W?cBES?]?S@g@FUFIBi@T[PyAt@k@PSB_@A]Kg@Wk@g@KMSW{@cBS]w@`AO\\]s@Q[aAyAEQAE?C?GJgC@a@a@Ea@CKAOAIAQCK?e@Ik@MEA_@G[EGAc@CsB?C?}D?iBAsAA_@As@Au@?}@CaCIyFG}@CE?A?wBAS?oC?G?{@?s@Bs@B[BQ@I@a@Do@DwALwAJuDNuJj@_BFk@C{AH_CLW@]BE?O@I?M@E?S@C@Y@y@DeAF[Bs@DeAFWBOJqI^_EPaEJaLXsELwFRmFPc@GU?e@?s@@O@_@@U?UB}@He@DQDkBFyBHgAJg@@IEKIIMU_@Yo@AA]w@iBcEaAsBKOa@e@_@Sw@Ue@KeCY[E_AECAuACeACaBCeDEE?{B@aBBo@??EB}Fo@@}FBsBEyAIw@Aw@HeAPgBDoAKkDHsB\\sCJyC?MbBSzD?BsFXkDXa@Dk@Hg@FK@u@@gDPA?E?GAI?S@O@c@BE?E@OEIEEACCEECGW_@KQAGACEIM?A@EMEGAEEIGKKKSQOGw@Uu@SMEOEaC_AkC{@YIiBi@[KgCs@wCo@{E{@YGuEw@GAEAUAi@AMEa@AMGCECACEKWFe@Bg@@KBy@?MKg@KWKIOEOCm@CUA[H_@HQFw@VeBh@m@NgAXo@PsA`@QDSFcBf@{@X_@LKBO@M@Y@C?[BmJ`Be@ROFO?qADqAFK?I@gA?{@BO?[@o@BiCHO@C?k@@m@HOD]VgAlA_AfAUREDC?Q?OBE@qBn@A@SHOJELCDgAb@q@\\mAt@y@f@y@XeBt@YJE@_@LmA`@c@N{C`A_DfAuAf@MHKJG@GCINCGI?KB"))
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
				.body("routes[0].way_points", hasItems(0, 337, 337, 688))
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
				.body("routes[0].bbox", hasItems(8.684671f, 49.333622f, 8.694327f, 49.424603f))
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
				.body("routes[0].extras.surface.values.size()", is(27))
				.body("routes[0].extras.surface.values[26][1]", is(688))
				.body("routes[0].extras.suitability.values[38][1]", is(688))
				.body("routes[0].extras.steepness.values[1][1]", is(688))
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
				.body("routes[0].segments[2].containsKey('steps')", is(true))
				.body("routes[0].segments[0].steps.size()", is(31))
				.body("routes[0].segments[1].steps.size()", is(1))
				.body("routes[0].segments[2].steps.size()", is(30))
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
				.body("routes[0].segments[2].containsKey('steps')", is(true))
				.body("routes[0].segments[0].steps.size()", is(31))
				.body("routes[0].segments[1].steps.size()", is(1))
				.body("routes[0].segments[2].steps.size()", is(30))
				.body("routes[0].segments[0].steps[0].distance", is(774.2f))
				.body("routes[0].segments[0].steps[0].duration", is(392.4f))
				.body("routes[0].segments[0].steps[0].type", is(6))
				.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
				.body("routes[0].segments[0].steps[10].distance", is(183.2f))
				.body("routes[0].segments[0].steps[10].duration", is(36.6f))
				.body("routes[0].segments[0].steps[10].type", is(5))
				.body("routes[0].segments[0].steps[10].instruction", is("Turn slight right onto Franz-Knauff-Straße, L 598"))
				.statusCode(200);
	}
	
	// test fitness params bike..

}
