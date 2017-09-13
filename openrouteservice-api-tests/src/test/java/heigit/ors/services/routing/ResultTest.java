package heigit.ors.services.routing;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;
import io.restassured.response.Response;
import junit.framework.Assert;

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
		.body("routes[0].summary.distance", is(14132.5f))
		.body("routes[0].summary.duration", is(3815.6f))
		.body("routes[0].summary.ascent", is(349.4f))
		.body("routes[0].summary.descent", is(340))
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
		.body("routes[0].segments[0].distance", is(7199.4f))
		.body("routes[0].segments[0].duration", is(2597.4f))
		.body("routes[0].segments[1].distance", is(6933.1f))
		.body("routes[0].segments[1].duration", is(1218.2f))
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
						"yrqlHkn~s@sqT\\jG}IVrHpE@bInLKpD~@SdCy@YpBi@S|@JIAJBi@LBkBb@?iBL@cA@EkAOAy@SAUI?KICGICIICoACQeA?Q@BAk@DEiA`@Ck@RCk@RAQ?Gw@sA?Cq@Ek@q@S{BaFScBqEa@mCuJQmAaCM{@cBNEIC]RMaBdBM_CbHCIxAKcApDAItA?GrAGqArEEsA\\Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JHAd@f@K?dAAw@RDAF~HsAxDF?RF@RF@RB@RBDPBBNFRv@HVt@FJr@LZr@JTp@BBp@D@n@B@n@RAl@HCj@CKj@D?h@NIf@PKeAvAq@}INIeABLmB~@e@eKVMkCFCkCJCkCRCkCZFkCNFkCDFkC\\ZiDBBiDJD{@PD{@JB{@J?{@R?{@PA{@b@CwB^Eq@L?H@?RB?RFBRBBRJ@R|BObG@?p@FAnAF?nAFFnA@FnALEnAFCnA@?\\HG\\BA\\NK?HC?LA?BG?FS??K?J??j@?NH@HVNHHJS@DS@BSBH]FN]DM]@QS?AS?CSNSH@KH??HF@HtEv@kCXFIzEz@iCvCn@FfCr@eCZJYhBh@gDXH_@jCz@oD`C~@f@NDRDQRBOH@EHBGRx@yBbB?CR@CRHQRBEMFEQROUBE[JI_@NOc@RSg@HIg@ROg@pAo@{ENKeALKcBh@q@gEHIcBBCcB~Ay@sNTa@mBVu@mDHe@oAH[oAJ[qERWoFJIeFTQ{EPM{ENU}D\\_A{JNo@_IF}@wRAoBwp@?]aM?YqMH{BkbAByCsoA?u@_b@@o@mL?[mL@GmL@GaKD]gHNc@{FT[qEVUqE@?qE@SkDFSgCx@{AqP`@cAoIx@eDyZZw@eRr@}Agh@V_Am[BKaMFE{HFGwG@KwG?mAoPNi@aI@oAuQ[gCmg@GQgJIMgJCOgJa@qEgu@_@qBw[Ws@cKSWuFUEiFaAa@kS]GkC[DkCE@kCGCoCy@w@sVcAq@ed@aBmAsx@QOgGCGkFuAqBma@i@m@}AGEsFUSyFaCeCyv@iDuCebBc@x@eSU`@gJKNiJSHkJi@F{UE@oJi@k@sUc@e@_REGgG[RqFQNyFEHcFGEmEECwDCEcCM_@mBg@kBjMI_@~Cn@E`@DA]TOa@Ga@g@MgA}B]qC{JIcAeHIaDyX@GuC@Iu@@If@BIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC_BDGaC@G_D@G}DDK{EBGyFTc@wGVi@oPN[wGBBeArAo@mQv@UaM|BS{TdBJkMjEd@kRvBN_@fCSaFh@@_DVDiAZHgA\\OaAFC_AD?{@z@GyFhAQmNfCi@kDlGk@pKmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCJJjCHDnCrFt@pf@tAVnIL@jC@?jCzARdW`CNtd@|DbAjVtC~@lMTDbBRDbBx@JjHZBbBpAJdPfBGrU\\DlBNDtB@BfBBH|AF@nAz@GnFf@D~CfG~Bpr@RH~CB@~CJF~CDB~CDB~CPTrCR^jCJd@~BBZxD@PpD@HdD?LxC?BpC?DdCAj@xBKd@pBQb@jDIX~CIXvCEZjCAzBrS?~@fJATrDE`AfLAHfEAPfEI`@fEGZfEK`@jHGTjHa@|@pOOTjCS`@jCc@~@|NWt@hIKd@~Ck@zC~\\G\\~CCLpEG`@lBMb@rDiAfC|g@o@tAvVOXnDCH~CML~@QNp@QNb@mBdBuH]DeAUIcAGE}@CC{@_@c@wBUG{@eBb@vGeAd@~ROA~CGM~C?A~C?C~C?OjCDIjCn@e@vGVi@vGLg@tCAUxBIK|AQA`A{@Hx@}AExKm@?bG}ATn_@[@bG]KxFaAkAf^KCxFKDbBC@jAABr@I^ZA`@B@LSBVHTrBlI@ZtAG`BbB?ZR?DR@HRCJRW~@xMs@|A~k@[v@xXy@dDv_Aa@bAfb@y@zAlv@GRhIARhIA?hIWThIUZhIOb@|GE\\|DAFjDAF|C?ZjCAn@xB?t@dECxC`TIzBhS?XpE?\\pE@nBti@G|@zYOn@nP]~@~a@OTxOQLbOUPlNKHvMSVhNKZnQIZhPId@dOWt@r[U`@`M_Bx@lv@CB~HIH|Di@p@zJMJzEOJzEqAn@vVSNnAIHnASR~AONlBKHlBGBdBi@?rDIAlAMCdAIAp@C?p@A?p@oGsAiDw@[HuBw@rBaHcBbK_B]`By@Sc@{@Q}AmDuAHu@Y~BKAp@aDWnSCI`AEEx@EAp@MCh@IC`@WCXgAKd@CG{@G@{@I@{@}@ZsD_DbA_SKDoAKDoA{C~@YoHhBls@K?`BSCxAGBnAO@hAUJdACB`AEB|@oIxApDE@Sk@HaCG?mA[BkAU@kAG^iACBiAqADkIqAFwIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
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
		.body("routes[0].way_points", hasItems(0, 333, 608))
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
		.body("routes[0].bbox", hasItems(8.687794f, 49.388405f, 8.714833f, 49.424603f))
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
		.body("routes[0].bbox", hasItems(8.687794f, 49.388405f, 8.714833f, 49.424603f))
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
		.body("routes[0].extras.surface.values.size()", is(41))
		.body("routes[0].extras.surface.values[34][1]", is(527))
		.body("routes[0].extras.suitability.values[30][0]", is(507))
		.body("routes[0].extras.steepness.values[11][1]", is(370))
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
		.body("routes[0].segments[0].distance", is(4632.5f))
		.body("routes[0].segments[0].duration", is(1755.9f))
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
		.body("routes[0].segments[0].steps.size()", is(49))
		.body("routes[0].segments[1].steps.size()", is(24))
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
		.body("routes[0].segments[0].steps.size()", is(49))
		.body("routes[0].segments[1].steps.size()", is(24))
		.body("routes[0].segments[0].steps[0].distance", is(511.4f))
		.body("routes[0].segments[0].steps[0].duration", is(230.1f))
		.body("routes[0].segments[0].steps[0].type", is(11))
		.body("routes[0].segments[0].steps[0].instruction", is("Head west"))
		.body("routes[0].segments[0].steps[10].distance", is(74.1f))
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

}
