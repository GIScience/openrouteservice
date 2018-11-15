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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "routes")
public class ResultTest extends ServiceTest {

	public ResultTest() {

		addParameter("coordinatesShort", "8.678613,49.411721|8.687782,49.424597");
		addParameter("coordinatesLong", "8.678613,49.411721|8.714733,49.393267|8.687782,49.424597");
		addParameter("extra_info", "surface|suitability|steepness");
		addParameter("preference", "fastest");
		addParameter("bikeProfile", "cycling-regular");
		addParameter("carProfile", "driving-car");
	}

    @Test
    public void testGpxExport() throws IOException, SAXException, ParserConfigurationException {
        Response response = given()
                .param("coordinates", getParameter("coordinatesShort"))
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("carProfile"))
                .param("format", "gpx")
                .param("instructions", "True")
                .when().log().ifValidationFails()
                .get(getEndPointName());
        response.then()
                .assertThat()
                .contentType("application/xml;charset=UTF-8")
                .statusCode(200);
        testGpxConsistency(response, true);
        Response response_without_instructions = given()
                .param("coordinates", getParameter("coordinatesShort"))
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("carProfile"))
                .param("format", "gpx")
                .param("instructions", "False")
                .when().log().ifValidationFails()
                .get(getEndPointName());
        response_without_instructions.then()
                .assertThat()
                .contentType("application/xml;charset=UTF-8")
                .statusCode(200);
        testGpxConsistency(response_without_instructions, false);
    }

    /**
     * Validates the xml consistency of the gpx output. Instructions can be turned on or off.
     * The functions tests if all xml members are present in the output.
     * Completeness is important for the xml schema verification!
     *
     * @param response
     * @param instructions
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private void testGpxConsistency(Response response, boolean instructions) throws ParserConfigurationException, IOException, SAXException {
        String body = response.body().asString();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(body)));
        Assert.assertEquals(doc.getDocumentElement().getTagName(), "gpx");
        int doc_length = doc.getDocumentElement().getChildNodes().getLength();
        Assert.assertTrue(doc_length > 0);
        boolean gpxMetadata = false;
        boolean gpxRte = false;
        boolean gpxExtensions = false;
        for (int i = 0; i < doc_length; i++) {
            String item = doc.getDocumentElement().getChildNodes().item(i).getNodeName();
            switch (item) {
                case "metadata":
                    gpxMetadata = true;
                    NodeList metadataChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int metadataSize = metadataChildren.getLength();
                    boolean metadataName = false;
                    boolean metadataDescription = false;
                    boolean metadataAuthor = false;
                    boolean metadataCopyright = false;
                    boolean metatadaTime = false;
                    boolean metadataBounds = false;
                    for (int j = 0; j < metadataSize; j++) {
                        Node metadataItem = metadataChildren.item(j);
                        switch (metadataItem.getNodeName()) {
                            case "name":
                                metadataName = true;
                                break;
                            case "desc":
                                metadataDescription = true;
                                break;
                            case "author":
                                metadataAuthor = true;
                                NodeList authorChildren = metadataChildren.item(j).getChildNodes();
                                int authorLength = authorChildren.getLength();
                                boolean authorName = false;
                                boolean authorEmail = false;
                                boolean authorLink = false;
                                for (int k = 0; k < authorLength; k++) {
                                    Node authorItem = authorChildren.item(k);
                                    switch (authorItem.getNodeName()) {
                                        case "name":
                                            authorName = true;
                                            break;
                                        case "email":
                                            authorEmail = true;
                                            break;
                                        case "link":
                                            authorLink = true;
                                            NodeList linkChildren = authorChildren.item(k).getChildNodes();
                                            int linkLength = linkChildren.getLength();
                                            boolean linkText = false;
                                            boolean linkType = false;
                                            for (int l = 0; l < linkLength; l++) {
                                                Node linkItem = linkChildren.item(l);
                                                switch (linkItem.getNodeName()) {
                                                    case "text":
                                                        linkText = true;
                                                        break;
                                                    case "type":
                                                        linkType = true;
                                                }
                                            }
                                            Assert.assertTrue(linkText);
                                            Assert.assertTrue(linkType);
                                            break;
                                    }
                                }
                                Assert.assertTrue(authorName);
                                Assert.assertTrue(authorEmail);
                                Assert.assertTrue(authorLink);
                                break;
                            case "copyright":
                                metadataCopyright = true;
                                NodeList copyrightChildren = metadataChildren.item(j).getChildNodes();
                                int copyrightLength = copyrightChildren.getLength();
                                boolean copyrightYear = false;
                                boolean copyrightLicense = false;
                                for (int k = 0; k < copyrightLength; k++) {
                                    Node copyrightItem = copyrightChildren.item(k);
                                    switch (copyrightItem.getNodeName()) {
                                        case "year":
                                            copyrightYear = true;
                                            break;
                                        case "license":
                                            copyrightLicense = true;
                                            break;
                                    }
                                }
                                Assert.assertTrue(copyrightYear);
                                Assert.assertTrue(copyrightLicense);
                                break;
                            case "time":
                                metatadaTime = true;
                                break;
                            case "bounds":
                                metadataBounds = true;
                                break;
                        }
                    }
                    Assert.assertTrue(metadataName);
                    Assert.assertTrue(metadataDescription);
                    Assert.assertTrue(metadataAuthor);
                    Assert.assertTrue(metadataCopyright);
                    Assert.assertTrue(metatadaTime);
                    Assert.assertTrue(metadataBounds);
                    break;
                case "rte":
                    gpxRte = true;
                    NodeList rteChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int rteSize = rteChildren.getLength();
                    boolean rtept = false;
                    boolean routeExtension = false;
                    for (int j = 0; j < rteSize; j++) {
                        Node rteElement = rteChildren.item(j);
                        switch (rteElement.getNodeName()) {
                            case "rtept":
                                rtept = true;
                                if (instructions) {
                                    int rteptLength = rteElement.getChildNodes().getLength();
                                    boolean rteptName = false;
                                    boolean rteptDescription = false;
                                    boolean rteptextensions = false;
                                    for (int k = 0; k < rteptLength; k++) {
                                        Node rteptElement = rteElement.getChildNodes().item(k);
                                        switch (rteptElement.getNodeName()) {
                                            case "name":
                                                rteptName = true;
                                                break;
                                            case "desc":
                                                rteptDescription = true;
                                                break;
                                            case "extensions":
                                                rteptextensions = true;
                                                int rteptExtensionLength = rteptElement.getChildNodes().getLength();
                                                boolean distance = false;
                                                boolean duration = false;
                                                boolean type = false;
                                                boolean step = false;
                                                for (int l = 0; l < rteptExtensionLength; l++) {
                                                    Node rteptExtensionElement = rteptElement.getChildNodes().item(l);
                                                    switch (rteptExtensionElement.getNodeName()) {
                                                        case "distance":
                                                            distance = true;
                                                            break;
                                                        case "duration":
                                                            duration = true;
                                                            break;
                                                        case "type":
                                                            type = true;
                                                            break;
                                                        case "step":
                                                            step = true;
                                                            break;
                                                    }
                                                }
                                                Assert.assertTrue(distance);
                                                Assert.assertTrue(duration);
                                                Assert.assertTrue(type);
                                                Assert.assertTrue(step);
                                        }
                                    }
                                    Assert.assertTrue(rteptName);
                                    Assert.assertTrue(rteptDescription);
                                    Assert.assertTrue(rteptextensions);
                                }
                                break;
                            case "extensions":
                                routeExtension = true;
                                int rteExtensionsLength = rteElement.getChildNodes().getLength();
                                boolean rteExtensionsDistance = false;
                                boolean rteExtensionsDuration = false;
                                boolean rteExtensionsDistanceActual = false;
                                boolean rteExtensionsAscent = false;
                                boolean rteExtensionsDescent = false;
                                boolean rteExtensionsAvgSpeed = false;
                                boolean rteExtensionsBounds = false;
                                for (int k = 0; k < rteExtensionsLength; k++) {
                                    Node extensionsElement = rteElement.getChildNodes().item(k);
                                    switch (extensionsElement.getNodeName()) {
                                        case "distance":
                                            rteExtensionsDistance = true;
                                            break;
                                        case "duration":
                                            rteExtensionsDuration = true;
                                            break;
                                        case "distanceActual":
                                            rteExtensionsDistanceActual = true;
                                            break;
                                        case "ascent":
                                            rteExtensionsAscent = true;
                                            break;
                                        case "descent":
                                            rteExtensionsDescent = true;
                                            break;
                                        case "avgSpeed":
                                            rteExtensionsAvgSpeed = true;
                                            break;
                                        case "bounds":
                                            rteExtensionsBounds = true;
                                            break;
                                    }
                                }
                                Assert.assertTrue(rteExtensionsDistance);
                                Assert.assertTrue(rteExtensionsDuration);
                                Assert.assertTrue(rteExtensionsDistanceActual);
                                Assert.assertTrue(rteExtensionsAscent);
                                Assert.assertTrue(rteExtensionsDescent);
                                Assert.assertTrue(rteExtensionsAvgSpeed);
                                Assert.assertTrue(rteExtensionsBounds);
                                break;
                        }
                    }
                    Assert.assertTrue(rtept);
                    Assert.assertTrue(routeExtension);
                    break;
                case "extensions":
                    gpxExtensions = true;
                    NodeList gpxExtensionsChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int gpxExtensionLength = gpxExtensionsChildren.getLength();
                    boolean gpxExtensionattribution = false;
                    boolean gpxExtensionengine = false;
                    boolean gpxExtensionbuild_date = false;
                    boolean gpxExtensionprofile = false;
                    boolean gpxExtensionpreference = false;
                    boolean gpxExtensionlanguage = false;
                    boolean gpxExtensioninstructions = false;
                    boolean gpxExtensionelevation = false;
                    for (int j = 0; j < gpxExtensionLength; j++) {
                        Node gpxExtensionElement = gpxExtensionsChildren.item(j);
                        switch (gpxExtensionElement.getNodeName()) {
                            case "attribution":
                                gpxExtensionattribution = true;
                                break;
                            case "engine":
                                gpxExtensionengine = true;
                                break;
                            case "build_date":
                                gpxExtensionbuild_date = true;
                                break;
                            case "profile":
                                gpxExtensionprofile = true;
                                break;
                            case "preference":
                                gpxExtensionpreference = true;
                                break;
                            case "language":
                                gpxExtensionlanguage = true;
                                break;
                            case "instructions":
                                gpxExtensioninstructions = true;
                                break;
                            case "elevation":
                                gpxExtensionelevation = true;
                                break;
                        }
                    }
                    Assert.assertTrue(gpxExtensionattribution);
                    Assert.assertTrue(gpxExtensionengine);
                    Assert.assertTrue(gpxExtensionbuild_date);
                    Assert.assertTrue(gpxExtensionprofile);
                    Assert.assertTrue(gpxExtensionpreference);
                    Assert.assertTrue(gpxExtensionlanguage);
                    Assert.assertTrue(gpxExtensioninstructions);
                    Assert.assertTrue(gpxExtensionelevation);
                    break;
            }
        }
        Assert.assertTrue(gpxMetadata);
        Assert.assertTrue(gpxRte);
        Assert.assertTrue(gpxExtensions);
    }

    /**
	 * The function validates the whole GeoJson export except segments.
	 * Segments hold the instructions and are not necessary for our valid GeoJson-export.
	 */
	@Test
	public void testGeoJsonExport(){
		given()
				.param("coordinates", getParameter("coordinatesShort"))
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("format", "geojson")
				.param("extra_info", getParameter("extra_info"))
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'features' }", is(true))
				.body("any { it.key == 'bbox' }", is(true))
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'info' }", is(true))
				.body("features[0].containsKey('geometry')", is(true))
				.body("features[0].containsKey('type')", is(true))
				.body("features[0].containsKey('properties')", is(true))
				.body("features[0].properties.containsKey('summary')", is(true))
				.body("features[0].properties.containsKey('bbox')", is(true))
				.body("features[0].properties.containsKey('way_points')", is(true))
				.body("features[0].properties.containsKey('segments')", is(true))
				.body("features[0].properties.containsKey('extras')", is(true))
				.body("features[0].geometry.containsKey('coordinates')", is(true))
				.body("features[0].geometry.containsKey('type')", is(true))
				.body("features[0].geometry.type", is("LineString"))
				.body("features[0].type", is("Feature"))
				.body("type", is("FeatureCollection"))

				.statusCode(200);
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
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
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
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))
                .body("routes[0].summary.distance", is(11259.9f))
                .body("routes[0].summary.duration", is(2546.9f))
                .body("routes[0].summary.ascent", is(345.2f))
                .body("routes[0].summary.descent", is(341.8f))
                .statusCode(200);
    }

    @Test
    public void testSegmentDistances() {

        given()
                .param("coordinates", getParameter("coordinatesLong"))
                .param("instructions", "true")
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("bikeProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))
                .body("routes[0].segments[0].distance", is(5666.4f))
                .body("routes[0].segments[0].duration", is(1284.9f))
                .body("routes[0].segments[1].distance", is(5593.5f))
                .body("routes[0].segments[1].duration", is(1262))
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
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body(
                        "routes[0].geometry",
                        is(
                                "gvqlHk`~s@cwUB?tC?Cp@NAdIAE`EQaCi@WiCaDcAuG?C]g@MaBVM_C`HCInAKcA~CAIjA?GhAGqAzDEsAh@Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JHAd@f@K?dAAw@RDAF~HsAxDF?RF@RF@RB@RBDPBBNFRv@HVt@FJr@LZr@JTp@BBp@D@n@B@n@RAl@HCj@RGj@PIcAvAu@}IPGeA~@e@uHVMkCFCkCJCkCRCkCZFkCNFkCDFkC\\ZiDBBiDJDiDPD{@JB{@J?{@R?{@PA{@b@CwB^Eq@L?H@?RB?RFBRBBRJ@R|BObG@?p@FAnAF?nAFFnA@FnALEnAFCnA@?\\HG\\BA\\NK?HC?LA?BG?FS??K?AG?@M?DI?DK?@K??[]?M]@K]BMSAgAg@@MS@IS?o@SC]HCIHEAHMCHICHWCHgAKf@CGq@?Cq@IaCwB?MSCe@SNMSRC]HA]l@e@{@NK]tBwAu\\FIiDBAsD??}DZjBkL@?q@@Ai@?{@_ID]gEF[gEBUiDJqAsMD@aF@LcEBx@cBPy@qEBIkCBEqDJSiO@KoPQkAmVEMkHGEyEW?mCAEkCn@e@uONEkM\\CeKXHeKJFeKDBkMRXwLn@hBuf@Xd@qMPLkMv@L}g@NB}I?G}I@OkOBIwLMU{EQk@{EC[{E@qA}QQo@gYIm@cLAQcLDiA}ZEaAsNIe@oIU]}PKKuMMOuMk@i@gTcAYuR?MqEGA{CEAcBECcBCEcBOUjCQk@ReA_IpW[eA{@M]]HF]V`@]N`@]Ph@mG\\jBeIRz@YRl@qIHRqEDLeEN^wDPHmDRG_DHO_D?O_DCQ_D]aAqOEWkHAGkHAU_IOiDslAe@oEmLG{BlEAcApD@GxABUlBV{@sKTg@}GvFwH{aG~CwCov@~Am@sj@BCkIDAgIRMaIhAeAeXzAcB{f@z@s@{Nr@uAgM^m@oFLOwBDB_DFDgEDIoFPOyFZSsEKUmDU{@eFSaAwDCQ}BGa@cDMgAgJ]qCwLIcAgGIaDgK@G[@I]@IBBIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC}ADGaCHK_Db@c@yKZ[yFj@_@qPbAWsRbAOmBdAG}@fABmAj@HrAzE~@dCpADsHh@CgEZA_B@GyABCwAvDiAyo@\\OqEFCcED?qDz@G}LhAQoPfCi@_NlGk@bJmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCi@BzGqAEhV{E_Aju@k@IbEgAC`JeAFbCcANAcAViAk@^_A[Za@c@b@mAIJk@EFREBRDFRITREDRCFRMJRo@h@lBCDdACHvGAHvGAHvGAFvGH`Dt\\HbA~E\\pC`WLfArIF`@hDBPhDR`AXTz@z@JTz@[RxAQNxAEHvBGEtCECpBMNnA_@l@fCs@tAfQ{@r@zJ{AbB~\\iAdAnUSLvBE@vBCBvB_Bl@fR_DvCp~@wFvH|zBUf@|BWz@vZCT~OAFxO@bAb`@FzBbiAd@nE`{ANhD|V@TyA@FyADVyA\\`AcABPo@?NoAIN_@SFLQIz@O_@lBEMzCIShESm@hYS{@vZ]kB~i@Qi@jHOa@jHWa@dFIG~CL\\~CZdA~HdA~Hll@Pj@lGNT\\BD\\DB\\D@\\F@\\?L\\bAXr@j@h@YLNa@JJiFT\\aBHd@mBD`AaGEhA_B@PkCHl@bBPn@fEApAjJBZpEPj@pELTpECHpEANtM?FtMOCtMw@Mfc@QMxFYe@~Zo@iBns@SYhIECpGKGzEYIzE]BzEODvLo@d@jZ@D|IV?|IFDrLDLjOPjA`WAJxCKRjHCDjHCHjHQx@hXCy@pYAMdKEAdKKpA`WCT`FGZjCE\\jC?z@fOA@jHA?`M[kBvj@??jCC@dAGH?uBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@zBoHhBls@K?`BSCxAGBnAO@hAUJdACB`AEB|@oIxApDE@Sk@HaCG?mA[BkAU@kAG^iACBiAqADkIqAFwIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
                .statusCode(200);
    }


    @Test
    public void testWaypoints() {

        given()
                .param("coordinates", getParameter("coordinatesLong"))
                .param("instructions", "true")
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("bikeProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].way_points", hasItems(0, 302, 540))
                .statusCode(200);
    }

	@Test
	public void testBbox() {

		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("bikeProfile"))
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
                .body("routes[0].bbox", hasItems(8.678615f, 49.393272f, 8.714833f, 49.424603f))
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
                .body("routes[0].bbox", hasItems(8.678615f, 49.393272f, 8.714833f, 49.424603f))
				.body("routes[0].segments[0].steps[0].maneuver.bearing_before", is(0))
				//.body("routes[0].segments[0].steps[0].maneuver.bearing_after", is(260))
                .body("routes[0].segments[0].steps[0].maneuver.bearing_after", is(175))
				.body("routes[0].segments[0].steps[0].maneuver.containsKey('location')", is(true))
				//.body("routes[0].segments[0].steps[1].maneuver.bearing_before", is(298))
                .body("routes[0].segments[0].steps[1].maneuver.bearing_before", is(175))
				//.body("routes[0].segments[0].steps[1].maneuver.bearing_after", is(4))
                .body("routes[0].segments[0].steps[1].maneuver.bearing_after", is(80))
				//.body("routes[0].segments[0].steps[1].maneuver.location", hasItems(8.673925f, 49.411283f))
                .body("routes[0].segments[0].steps[1].maneuver.location", hasItems(8.678618f, 49.411697f))
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
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
				.get(getEndPointName());

		response.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.surface.values.size()", is(43))
                .body("routes[0].extras.surface.values[26][1]", is(347))
                .body("routes[0].extras.suitability.values[29][0]", is(461))
                .body("routes[0].extras.steepness.values[11][1]", is(296))

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
				.when().log().ifValidationFails()
				.get(getEndPointName());

		Assert.assertEquals(response.getStatusCode(), 200);

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
				.when().log().ifValidationFails()
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
				//.body("routes[0].extras.traildifficulty.values[1][1]", is(20))
                .body("routes[0].extras.traildifficulty.values[1][1]", is(6))
				.body("routes[0].extras.traildifficulty.values[1][2]", is(1))
				.statusCode(200);

		checkExtraConsistency(response);

		response = given()
				.param("coordinates", "8.724174,49.390223|8.716536,49.399622")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "foot-hiking")
				.param("extra_info", "traildifficulty")
				.when().log().ifValidationFails()
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
		// Test that the response indicates that the whole route is tollway free. The first two tests check that the waypoint ids
		// in the extras.tollways.values match the final waypoint of the route
		Response response = given()
				.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
				.param("instructions", "true")
				.param("preference", "fastest")
				.param("profile", "driving-car")
				.param("extra_info", "suitability|tollways")
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
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
		// Test that the "right turn only" restriction at the juntion is taken into account
		given()
				.param("coordinates", "8.684081,49.398155|8.684703,49.397359")
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", "driving-car")
				.param("optimized", "false")
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				//.body("routes[0].summary.distance", is(620.1f))
                .body("routes[0].summary.distance", is(617.1f))
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
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				//.body("routes[0].summary.distance", is(714.7f))
                .body("routes[0].summary.distance", is(751.5f))
				.statusCode(200);
	}

	@Test
	public void testSteps() {
		given()
				.param("coordinates", getParameter("coordinatesLong"))
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("bikeProfile"))
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].segments[0].containsKey('steps')", is(true))
				.body("routes[0].segments[1].containsKey('steps')", is(true))
				//.body("routes[0].segments[0].steps.size()", is(55))
                .body("routes[0].segments[0].steps.size()", is(42))
				//.body("routes[0].segments[1].steps.size()", is(28))
                .body("routes[0].segments[1].steps.size()", is(25))
				.statusCode(200);
	}

    @Test
    public void testStepsDetails() {

        given()
                .param("coordinates", getParameter("coordinatesLong"))
                .param("instructions", "true")
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("bikeProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].segments[0].containsKey('steps')", is(true))
                .body("routes[0].segments[1].containsKey('steps')", is(true))
                .body("routes[0].segments[0].steps.size()", is(42))
                .body("routes[0].segments[1].steps.size()", is(25))
                .body("routes[0].segments[0].steps[3].distance", is(337.3f))
                .body("routes[0].segments[0].steps[3].duration", is(67.5f))
                .body("routes[0].segments[0].steps[3].type", is(0))
                .body("routes[0].segments[0].steps[3].instruction", is("Turn left"))
                .body("routes[0].segments[0].steps[9].distance", is(44.8f))
                .body("routes[0].segments[0].steps[9].duration", is(9))
                .body("routes[0].segments[0].steps[9].type", is(1))
                .body("routes[0].segments[0].steps[9].instruction", is("Turn right"))
                .statusCode(200);
    }

	private void checkExtraConsistency(Response response) {
		JSONObject jResponse = new JSONObject(response.body().asString());

		JSONObject jRoute = (jResponse.getJSONArray("routes")).getJSONObject(0);
		double routeDistance = jRoute.getJSONObject("summary").getDouble("distance");
		JSONObject jExtras = (jResponse.getJSONArray("routes")).getJSONObject(0).getJSONObject("extras");

		JSONArray jExtraNames = jExtras.names();
		for (int i = 0; i < jExtraNames.length(); i++) {
			String name = jExtraNames.getString(i);
			JSONArray jExtraValues = jExtras.getJSONObject(name).getJSONArray("values");

			JSONArray jValues = jExtraValues.getJSONArray(0);
			int fromValue = jValues.getInt(0);
			int toValue = jValues.getInt(1);
			Assert.assertEquals(fromValue < toValue, true);

			for (int j = 1; j < jExtraValues.length(); j++) {
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

			for (int j = 0; j < jSummary.length(); j++) {
				JSONObject jSummaryValues = jSummary.getJSONObject(j);
				distance += jSummaryValues.getDouble("distance");
				amount += jSummaryValues.getDouble("amount");
			}

			Assert.assertEquals(Math.abs(routeDistance - distance) < 0.5, true);

			Assert.assertEquals(Math.abs(amount - 100.0) < 0.1, true);
		}
	}

	@Test
	public void testHGVWidthRestriction() {
		given()
				.param("coordinates", "8.690915,49.430117|8.68834,49.427758")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", "driving-hgv")
				.param("options", "{\"profile_params\":{\"restrictions\":{\"width\":\"3\"}},\"vehicle_type\":\"hgv\"}")
				.param("units", "m")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(809.3f))
				.body("routes[0].summary.duration", is(239.1f))
				.statusCode(200);

		given()
				.param("coordinates", "8.690915,49.430117|8.68834,49.427758")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", "driving-hgv")
				.param("options", "{\"profile_params\":{\"restrictions\":{\"width\":\"2\"}},\"vehicle_type\":\"hgv\"}")
				.param("units", "m")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(379.5f))
				.body("routes[0].summary.duration", is(136))
				.statusCode(200);
	}

	@Test
	public void testHGVHeightRestriction() {
		given()
				.param("coordinates", "8.687992,49.426312|8.691315,49.425962")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", "driving-hgv")
				.param("options", "{\"profile_params\":{\"restrictions\":{\"height\":\"4\"}},\"vehicle_type\":\"hgv\"}")
				.param("units", "m")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(549))
				.body("routes[0].summary.duration", is(163.2f))
				.statusCode(200);

		given()
				.param("coordinates", "8.687992,49.426312|8.691315,49.425962")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", "driving-hgv")
				.param("options", "{\"profile_params\":{\"restrictions\":{\"height\":\"2\"}},\"vehicle_type\":\"hgv\"}")
				.param("units", "m")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(376.5f))
				.body("routes[0].summary.duration", is(130))
				.statusCode(200);
	}

	@Test
	public void testCarDistanceAndDuration() {
		// Generic test to ensure that the distance and duration dont get changed
		given()
				.param("coordinates", "8.690915,49.430117|8.68834,49.427758")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(379.5f))
				.body("routes[0].summary.duration", is(270))
				.statusCode(200);
	}

	// test fitness params bike..

	@Test
	public void testBordersAvoid() {
		// Test that providing border control in avoid_features works
		given()
				.param("coordinates", "8.684682,49.401961|8.690518,49.405326")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_borders\":\"controlled\"}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(1404))
				.statusCode(200);

		// Option 1 signifies that the route should not cross any borders
		given()
				.param("coordinates", "8.684682,49.401961|8.690518,49.405326")
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_borders\":\"all\"}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(false))
				.body("error.code", is(RoutingErrorCodes.ROUTE_NOT_FOUND))
				.statusCode(404);
	}

	@Test
	public void testCountryExclusion() {
		given()
				.param("coordinates", "8.684682,49.401961|8.690518,49.405326")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_countries\":\"3\"}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(1156.6f))
				.statusCode(200);

		given()
				.param("coordinates", "8.684682,49.401961|8.690518,49.405326")
				.param("instructions", "false")
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_countries\":\"1|3\"}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.3f))
                .statusCode(200);

	}

	@Test
	public void testBordersAndCountry() {
		// Test that routing avoids crossing into borders specified
		given()
				.param("coordinates", "8.684682,49.401961|8.690518,49.405326")
				.param("instructions", "false")
				.param("preference", getParameter("preference"))
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_borders\":\"controlled\",\"avoid_countries\":\"1\"}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(false))
				.body("error.code", is(RoutingErrorCodes.ROUTE_NOT_FOUND))
				.statusCode(404);
	}

	@Test
	public void testDetourFactor() {
		// Test that a detourfactor is returned when requested
		given()
				.param("coordinates",getParameter("coordinatesShort"))
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.param("attributes", "detourfactor")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].segments[0].detourfactor", is(1.38f))
				.statusCode(200);
	}

	@Test
	public void testAvoidArea() {
		given()
				.param("coordinates",getParameter("coordinatesShort"))
				.param("preference", "shortest")
				.param("profile", getParameter("carProfile"))
				.param("options", "{\"avoid_polygons\":{\"type\":\"Polygon\",\"coordinates\":[[[\"8.680\",\"49.421\"],[\"8.687\",\"49.421\"],[\"8.687\",\"49.418\"],[\"8.680\",\"49.418\"],[\"8.680\",\"49.421\"]]]}}")
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(2181.7f))
				.body("routes[0].summary.duration", is(433.2f))
				.statusCode(200);
	}


	@Test
	public void testWheelchairWidthRestriction() {
		given()
				.param("coordinates", "8.708605,49.410688|8.709844,49.411160")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"minimum_width\":\"2.0\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(129.6f))
				.body("routes[0].summary.duration", is(93.3f))
				.statusCode(200);

		given()
				.param("coordinates", "8.708605,49.410688|8.709844,49.411160")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"minimum_width\":\"2.1\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(158.7f))
				.body("routes[0].summary.duration", is(114.3f))
				.statusCode(200);
	}

	@Test
	public void testWheelchairInclineRestriction() {
		given()
				.param("coordinates", "8.670290,49.418041|8.667490,49.418376")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"maximum_incline\":\"0.0\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(594.4f))
				.body("routes[0].summary.duration", is(493.8f))
				.statusCode(200);

		given()
				.param("coordinates", "8.670290,49.418041|8.667490,49.418376")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"maximum_incline\":\"2\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(230.5f))
				.body("routes[0].summary.duration", is(172.5f))
				.statusCode(200);
	}

	@Test
	public void testWheelchairKerbRestriction() {
		given()
				.param("coordinates", "8.681125,49.403070|8.681434,49.402991")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"maximum_sloped_kerb\":\"0.1\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(74.1f))
				.body("routes[0].summary.duration", is(57.9f))
				.statusCode(200);

		given()
				.param("coordinates", "8.681125,49.403070|8.681434,49.402991")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"maximum_sloped_kerb\":\"0.03\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(146.7f))
				.body("routes[0].summary.duration", is(126.1f))
				.statusCode(200);
	}

	@Test
	public void testWheelchairSurfaceRestriction() {
		given()
				.param("coordinates", "8.686388,49.412449|8.690858,49.413009")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"surface_type\":\"cobblestone\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(333.7f))
				.body("routes[0].summary.duration", is(240.3f))
				.statusCode(200);

		given()
				.param("coordinates", "8.686388,49.412449|8.690858,49.413009")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"surface_type\":\"paved\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(336))
				.body("routes[0].summary.duration", is(302.4f))
				.statusCode(200);
	}

	@Test
	public void testWheelchairSmoothnessRestriction() {
		given()
				.param("coordinates", "8.676730,49.421513|8.678545,49.421117")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"smoothness_type\":\"excellent\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(748.4f))
				.body("routes[0].summary.duration", is(593.3f))
				.statusCode(200);

		given()
				.param("coordinates", "8.676730,49.421513|8.678545,49.421117")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"smoothness_type\":\"bad\"}}")
				.when()
				.get(getEndPointName())
				.then()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(172.1f))
				.body("routes[0].summary.duration", is(129.2f))
				.statusCode(200);
	}

	@Test
    public void testOsmIdExtras() {
        given()
                .param("coordinates", "8.676730,49.421513|8.678545,49.421117")
                .param("preference", "shortest")
                .param("profile", "wheelchair")
                .param("extra_info", "osmid")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('osmId')", is(true))
                .statusCode(200);
    }
}
