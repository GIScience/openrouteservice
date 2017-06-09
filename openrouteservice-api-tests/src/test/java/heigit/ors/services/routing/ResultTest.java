package heigit.ors.services.routing;

import static io.restassured.RestAssured.*;
//import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matcher;
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
		addParameter("profile", "cycling-regular");

	}

	@Test
	public void expectSegmentsToMatchCoordinates() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
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
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(true))
				.body("routes[0].segments.size()", is(3))
				.body("routes[0].summary.distance", is(22667))
				.body("routes[0].summary.duration", is(4963.2f))
				.body("routes[0].summary.ascent", is(37.5f))
				.body("routes[0].summary.descent", is(21))
				.statusCode(200);
	}

	@Test
	public void testSegmentDistances() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('segments')", is(true))
				.body("routes[0].segments.size()", is(3))
				.body("routes[0].segments[0].distance", is(11458.9f))
				.body("routes[0].segments[0].duration", is(2601.9f))
				.body("routes[0].segments[1].distance", is(0))
				.body("routes[0].segments[1].duration", is(0))
				.body("routes[0].segments[2].distance", is(11208.1f))
				.body("routes[0].segments[2].duration", is(2361.3f))
				.statusCode(200);
	}

	@Test
	public void testEncodedPolyline() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body(
						"routes[0].geometry",
						is(
								"yrqlHkn~s@\\jGVrH@bIKpDSdCYpBS|@IABi@BkB@mDI}CG[CoAQeAQ@Ak@EiACk@Ck@AQGw@gEX?BBpADlB?FBj@Ep@?J?d@?R?F?TPAxAKzA?x@@vISd@AdAEZAF?X?pACR@J?N@@@H?PEB?BCLKFGVAzFKdAArACN?D?NHVBr@?V?JAZATEFAPE\\Mf@QLGPK^Uh@e@VUA^lAwAPGdBcBBMASdAsARo@h@sAvB}Cn@[vAyBZEnAyCpDwFRc@@[BGNi@Pm@Rs@Hc@DQ?]F_@Fm@BUAUASC]Mo@EOGMQgC_@mIC[BGBEBGr@iB@CFG@AFA?E@E?I?G?KF?t@GRAD?H?D@LBR@D?N@P?\\C|@MRGd@IJAf@Gj@I`@EjDYrFY?CR{DLcBxC?IzBAjC?FvBKnFOhAChEIn@CdACp@ApEIp@A|FIn@?`BCzBAD?dDD`BBdABtABB@~@DZD?DKjFpFHvCrGDfBPA`@Cf@E^AXAh@AH?f@AfAKxBIjBGP@PDNB~@HVAp@AFA~@G^E\\I`@OlFQvFSrEM`LY`EK~DQpI_@NFVAbAE\\A\\AJ?h@A|AGZCD?TCNAd@CzBM~BWNAj@O~AGtJk@tDOvAKvAMn@E`@EHAPAZCr@Cr@Cz@?F?nC?R?vB@@?D?|@BxFF`CH|@Bt@?r@@^@rA@hB@|D?B?rB?b@BF@ZD^FD@j@Ld@HJ?PBH@N@J@`@B`@DA`@KfC?F?B@DDP`AxAPZ\\r@L^Pp@R`ATnARjAVz@lArBlAhBVZHJJUHIt@i@p@e@r@]\\Of@MB?lBYt@KhAKfBORGVEb@@v@FJ@BBAVBBFFFOJDhEjBxDhBj@oBLOHk@@E@A@ABADMHBZHTEDI@QDg@DgAB{ABgAL?zCRnAb@zE\\pJv@|AA|DQhBQ~AK_BJiBP}DP}A@qJw@{E]oAc@{CSM?CfACzAEfAEf@APEHUD[IICm@SuCuAcAe@sAq@i@UOK@IMCMCCS}@FG@iCNEoBAc@?QAQcAHM?W?cBES?]?S@g@FUFIBi@T[PyAt@k@PSB_@A]Kg@Wk@g@KMSW{@cBS]w@`AO\\]s@Q[aAyAEQAE?C?GJgC@a@a@Ea@CKAOAIAQCK?e@Ik@MEA_@G[EGAc@CsB?C?}D?iBAsAA_@As@Au@?}@CaCIyFG}@CE?A?wBAS?oC?G?{@?s@Bs@B[B?QACS?GKCOBaCFgBPmC?QKMQGkBg@_@Mm@MWEcAAqAEgBGSA_HpA}Ld@}CPsB?aA@{@@iDFSDcA@SEWL_APuCAgCe@OEwAe@sA_@w@QeAOy@OiAUs@Im@AmAAi@AWAy@CgCGa@AyCSsCWOCmBSo@Gm@Og@Wc@[OOMIi@M]?CBGOMc@EMUC{@Ka@@}A^a@?UFc@BWAa@EmHX@bAApA{Dl@uAP{@Ri@FkIFoGF?Vo@@}FBsBEyAIw@Aw@HeAPgBDoAKkDHsB\\sCJyC?MbBSzD?BsFXkDXa@Dk@Hg@FK@u@@gDPA?E?GAI?S@O@c@BE?E@OEIEEACCEECGW_@KQAGACEIM?A@EMEGAEEIGKKKSQOGw@Uu@SMEOEaC_AkC{@YIiBi@[KgCs@wCo@{E{@YGuEw@GAEAUAi@AMEa@AMGCECACEKWFe@Bg@@KBy@?MKg@KWKIOEOCm@CCKAOG@I@}@Z_DbAKDKD{C~@oHhBK?SCGBO@UJCBEBoIxAE@k@HG?[BU@G^CBqADqAFK?I@gA?{@BO?[@o@BiCHO@C?k@@m@HOD]VgAlA_AfAUREDC?Q?OBE@qBn@A@SHOJELCDgAb@q@\\mAt@y@f@y@XeBt@YJE@_@LmA`@c@N{C`A_DfAuAf@MHKJG@GCINCGI?KB"))
				.statusCode(200);
	}

	@Test
	public void testWaypoints() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].way_points", hasItems(0, 341, 341, 696))
				.statusCode(200);
	}

	@Test
	public void testBbox() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.log()
				.all()
				.get(getEndPointName())
				.then()
				.log()
				.all()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].bbox", hasItems(8.684671f, 49.333622f, 8.694456f, 49.424601f))
				.statusCode(200);
	}

	@Test
	public void testExtras() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
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
				.param("profile", getParameter("profile"))
				.param("extra_info", getParameter("extra_info"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
				.body("routes[0].extras.surface.values.size()", is(35))
				.body("routes[0].extras.surface.values[34][1]", is(696))
				.body("routes[0].extras.suitability.values[47][1]", is(696))
				.body("routes[0].extras.steepness.values[1][1]", is(696))
				.statusCode(200);
	}

	@Test
	public void testSteps() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].segments[0].containsKey('steps')", is(true))
				.body("routes[0].segments[1].containsKey('steps')", is(true))
				.body("routes[0].segments[2].containsKey('steps')", is(true))
				.body("routes[0].segments[0].steps.size()", is(33))
				.body("routes[0].segments[1].steps.size()", is(1))
				.body("routes[0].segments[2].steps.size()", is(37))
				.statusCode(200);
	}

	@Test
	public void testStepsDetails() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("profile"))
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].segments[0].containsKey('steps')", is(true))
				.body("routes[0].segments[1].containsKey('steps')", is(true))
				.body("routes[0].segments[2].containsKey('steps')", is(true))
				.body("routes[0].segments[0].steps.size()", is(33))
				.body("routes[0].segments[1].steps.size()", is(1))
				.body("routes[0].segments[2].steps.size()", is(37))
				.body("routes[0].segments[0].steps[0].distance", is(774.7f))
				.body("routes[0].segments[0].steps[0].duration", is(392.7f))
				.body("routes[0].segments[0].steps[0].type", is(6))
				.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
				.body("routes[0].segments[0].steps[10].distance", is(197.7f))
				.body("routes[0].segments[0].steps[10].duration", is(39.5f))
				.body("routes[0].segments[0].steps[10].type", is(5))
				.body("routes[0].segments[0].steps[10].instruction", is("Turn slight right onto Lessingstraße"))
				.statusCode(200);
	}
	
	// test fitness params bike..

}
