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

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
public class ResultTest extends ServiceTest {

    public ResultTest() {
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.678613);
        coord1.put(49.411721);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        coordsShort.put(coord2);
        addParameter("coordinatesShort", coordsShort);

        JSONArray coordsLong = new JSONArray();
        JSONArray coordLong1 = new JSONArray();

        coordLong1.put(8.678613);
        coordLong1.put(49.411721);
        coordsLong.put(coordLong1);
        JSONArray coordLong2 = new JSONArray();
        coordLong2.put(8.714733);
        coordLong2.put(49.393267);
        coordsLong.put(coordLong2);
        JSONArray coordLong3 = new JSONArray();
        coordLong3.put(8.687782);
        coordLong3.put(49.424597);
        coordsLong.put(coordLong3);
        addParameter("coordinatesLong", coordsLong);

        JSONArray extraInfo = new JSONArray();
        extraInfo.put("surface");
        extraInfo.put("suitability");
        extraInfo.put("steepness");
        addParameter("extra_info", extraInfo);

        addParameter("preference", "fastest");
        addParameter("bikeProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");
    }

    @Test
    public void testSimpleGetRoute() {
        given()
                .param("start", "8.686581,49.403154")
                .param("end", "8.688126,49.409074")
                .pathParam("profile", getParameter("carProfile"))
                .when().log().ifValidationFails()
                .get(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('summary')", is(true))
                .body("features[0].properties.summary.distance", is(1046.1f))
                .body("features[0].properties.summary.duration", is(215.0f))
                .statusCode(200);
    }

    @Test
    public void testGpxExport() throws IOException, SAXException, ParserConfigurationException {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        JSONArray attributes = new JSONArray();
        attributes.put("avgspeed");
        body.put("attributes", attributes);

        body.put("instructions", true);

        Response response = given()
                .header("Accept", "application/gpx+xml")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/gpx");

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .contentType("application/gpx+xml;charset=UTF-8")
                .statusCode(200);
        testGpxConsistency(response, true);
        testGpxGeometry(response);

        body.put("instructions", false);
        Response response_without_instructions = given()
                .header("Accept", "application/gpx+xml")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/gpx");
        response_without_instructions.then()
                .log().ifValidationFails()
                .assertThat()
                .contentType("application/gpx+xml;charset=UTF-8")
                .statusCode(200);
        testGpxConsistency(response_without_instructions, false);
        testGpxGeometry(response_without_instructions);
    }

    private void testGpxGeometry(Response response) throws ParserConfigurationException, IOException, SAXException {
        String body = response.body().asString();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(body)));
        Assert.assertEquals(doc.getDocumentElement().getTagName(), "gpx");
        int doc_length = doc.getDocumentElement().getChildNodes().getLength();
        Assert.assertTrue(doc_length > 0);
        boolean gpxRte = false;
        for (int i = 0; i < doc_length; i++) {
            String item = doc.getDocumentElement().getChildNodes().item(i).getNodeName();
            switch (item) {
                case "rte":
                    gpxRte = true;
                    NodeList rteChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int rteSize = rteChildren.getLength();
                    Assert.assertEquals(76, rteSize);
                    Assert.assertEquals(49.41172f, Float.parseFloat(rteChildren.item(0).getAttributes().getNamedItem("lat").getNodeValue()));
                    Assert.assertEquals(8.678615f, Float.parseFloat(rteChildren.item(0).getAttributes().getNamedItem("lon").getNodeValue()));
                    Assert.assertEquals(49.42208f, Float.parseFloat(rteChildren.item(rteSize / 2).getAttributes().getNamedItem("lat").getNodeValue()));
                    Assert.assertEquals(8.677165f, Float.parseFloat(rteChildren.item(rteSize / 2).getAttributes().getNamedItem("lon").getNodeValue()));
                    Assert.assertEquals(49.424603f, Float.parseFloat(rteChildren.item(rteSize - 2).getAttributes().getNamedItem("lat").getNodeValue())); // The last item (-1) is the extension pack
                    Assert.assertEquals(8.687809f, Float.parseFloat(rteChildren.item(rteSize - 2).getAttributes().getNamedItem("lon").getNodeValue())); // The last item (-1) is the extension pack
                    Node extensions = rteChildren.item(rteSize - 1);
                    String item1 = extensions.getChildNodes().item(0).getTextContent();
                    Assert.assertEquals(2362.1f, Float.parseFloat(extensions.getChildNodes().item(0).getTextContent()));
                    Assert.assertEquals(273.5f, Float.parseFloat(extensions.getChildNodes().item(1).getTextContent()));
                    Assert.assertEquals(0.0f, Float.parseFloat(extensions.getChildNodes().item(2).getTextContent()));
                    Assert.assertEquals(0.0f, Float.parseFloat(extensions.getChildNodes().item(3).getTextContent()));
                    Assert.assertEquals(31.1f, Float.parseFloat(extensions.getChildNodes().item(4).getTextContent()));
                    break;
            }
        }
        Assert.assertTrue(gpxRte);
    }

    /**
     * Validates the xml consistency of the gpx output. Instructions can be turned on or off.
     * The functions tests if all xml members are present in the output.
     * Completeness is important for the xml schema verification!
     * It does not validate the correctness of the route geometry!
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
        Node gpxMetadataNode = new IIOMetadataNode();
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
                                gpxMetadataNode = metadataItem;
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
                    Assert.assertEquals("ORSRouting", gpxMetadataNode.getTextContent());
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
                                        case "ascent":
                                            rteExtensionsAscent = true;
                                            break;
                                        case "descent":
                                            rteExtensionsDescent = true;
                                            break;
                                        case "avgspeed":
                                            rteExtensionsAvgSpeed = true;
                                            break;
                                        case "bounds":
                                            rteExtensionsBounds = true;
                                            break;
                                    }
                                }
                                Assert.assertTrue(rteExtensionsDistance);
                                Assert.assertTrue(rteExtensionsDuration);
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
    public void testGeoJsonExport() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].containsKey('type')", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('summary')", is(true))
                .body("features[0].containsKey('bbox')", is(true))
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
    public void testIdInSummary() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesShort"));
        body.put("id", "request123");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))

                .statusCode(200);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))

                .statusCode(200);
    }

    @Test
    public void expectSegmentsToMatchCoordinates() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))
                .statusCode(200);
    }

    @Test
    public void testSummary() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
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
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))

                .body("routes[0].segments[0].distance", is(5666.4f))
                .body("routes[0].segments[0].duration", is(1284.9f))
                .body("routes[0].segments[1].distance", is(5593.5f))
                .body("routes[0].segments[1].duration", is(1262f))
                .statusCode(200);
    }

    @Test
    public void testEncodedPolyline() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body(
                        "routes[0].geometry",
                        is(
                                //"yrqlHkn~s@sqT\\jG}IVrHpE@bInLKpD~@SdCy@YpBi@S|@JIAJBi@LBkBb@?iBL@cA@EkAOAy@SAUI?KICGICIICoACQeA?Q@BAk@DEiA`@Ck@RCk@RAQ?Gw@sA?Cq@Ek@q@S{BaFScBqEa@mCuJQmAaCM{@cBNEIC]RMaBdBM_CbHCIxAKcApDAItA?GrAGqArEEsA\\Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JHAd@f@K?dAAw@RDAF~HsAxDF?RF@RF@RB@RAQPAKN?GNAILAKJAUJ@OHAQHJSFE]_@OcBiBO_CuDCq@q@IoAcB]gE}IEm@q@Em@q@OqBaAAOOEs@VCsAvAAM\\CS\\HM\\BI\\BC\\HE\\FA\\D?\\D@\\DD\\@E\\Lg@|@?C`A?A`A@EdALwBlBFYyAFSyALOyAPIyA~@OmGn@IsDB?yAF?cBFAcB`B_@cSjA]qHzAa@p@BAHHCHAMHAy@\\AuAz@C}CX?YOG{DsA?aASGmEaAE_CNEmBS?MOF?Sx@@{@B?S@?SJ?q@VGq@@Jq@DrAuC?FSB~@g@DAMJAILCE@_@?@K?BiAk@VmD{CF]IH[I`@cA{@@?g@@Eg@NQsDBCsD^_@gJtAsAuWr@a@sWFEaIFC_IHE_IHC_IPG}Hn@WaSh@a@sNJMiMDOmM?OkMCIcL?AcLFGcL`@g@gYHOgJBEkH@?oF@@oFD?oFN?oF??oF@?oFBCoFDEoFBEoFJMoF@Sf@?Af@ACf@EG?ES?AU{@DO{@JK{@@G{@?C{@?C{@@M{@AM{@AA{@BC{@@G{@HQ{@?E{@CM{@EG{@JUwGT]wG@AwG??wGD@aHBFeFBv@{L?R{E?h@{EDx@wLNnA_XAl@aH?BaHAFaHc@tA]A@nF?@nF?FnF@BxFGJxFALrF?LnF@@nFJNnF@@gEh@a@wJC\\yCI^aC@@aCFFaCFFaCADcMAJmPARsS@?sSXu@}]JKwL\\a@oZRWwLtA_Cqr@dAcD_NLm@bBL{@aCHw@_FD}@{E@yBuc@ToDmbAJo@qKPiAaXLdBkf@^vAah@DbAaW^lCsDZ`BtCZr@{JNJ}GN?kHbAMeYhAO{u@xBMelBn@Eun@DAiXTOiSGa@iNMgAs]]qC_iAIcAsTIaDeb@@GuC@Iu@@If@BIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC_BDGaC@G_D@G}DDK{EBGyFTc@wGVi@oPN[wGBBeArAo@mQv@UaM|BS{TdBJkMjEd@kRvBN_@fCSaFh@@_DVDiAZHgA\\OaAFC_AD?{@z@GyFhAQmNfCi@kDlGk@pKmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@[I]WE]i@Ay@gCRfNwBO~OkEe@lTeBKoA}BRrHw@TzBsAn@xJCC~AOZzAWh@jDUb@nACFnAEJnAAFnAAFnAEFtCEBtCDFtCITtCEDtCCFtCDFdCNTtB?LdBQnI|q@GhBtMEn@hDAJhDF`@hDBPhDR`ApLTz@hLJTbEDFxDb@d@dHh@j@|EPNzA^f@tCv@nAg@^x@tFb@fApLf@v@bBjAbBdF`@t@xKv@nCtn@~@nCdMNnAlLLfCl[G`AjGI`@vAWl@bM[`@zPaBdAju@q@n@xZY\\bGg@t@nKUh@jCUx@hNS~@`KcBvMbrA?F`C?DfE@DlGBF|FFLlFIL|Ei@f@jKw@h@`VaCrAxl@aAT`K{@FfJm@@nFg@GfJ{@[rI_Ae@pEgAu@bQm@q@xFwCiEhN[Up@m@U`Jk@KhIeA@pOGA`H?LvGbAXtWj@h@~MLN`FJJXT\\fDHd@~CD`AjHEhA`F@PbBHl@bBPn@fEApAjJBZpEPj@pELTpECHpEANtM?FtMOCtMw@Mfc@QMxFYe@~Zo@iBns@SYhIECpGKGzEYIzE]BzEODvLo@d@jZ@D|IV?|IFDrLDLjOPjA`WAJxCKRjHCDjHCHjHQx@hXCy@pYAMdKEAdKKpA`WCT`FGZjCE\\jC?z@fOA@jHA?`M[kBvj@??jCC@dAGH?uBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@zBoHhBls@K?`BSCxAGBnAO@hAUJdACB`AEB|@oIxApDE@Sk@HaCG?mA[BkAU@kAG^iACBiAqADkIqAFwIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
                                "gvqlHk`~s@cwUB?tC?Cp@NAdIAE`EQaCi@WiCaDcAuG?C]g@MaBVM_C`HCInAKcA~CAIjA?GhAGqAzDEsAh@Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JHAd@f@K?dAAw@RDAF~HsAxDF?RF@RF@RB@RBDPBBNFRv@HVt@FJr@LZr@JTp@BBp@D@n@B@n@RAl@HCj@RGj@PIcAvAu@}IPGeA~@e@uHVMkCFCkCJCkCRCkCZFkCNFkCDFkC\\ZiDBBiDJDiDPD{@JB{@J?{@R?{@PA{@b@CwB^Eq@L?H@?RB?RFBRBBRJ@R|BObG@?p@FAnAF?nAFFnA@FnALEnAFCnA@?\\HG\\BA\\NK?HC?LA?BG?FS??K?AG?@M?DI?DK?@K??[]?M]@K]BMSAgAg@@MS@IS?o@SC]HCIHEAHMCHICHWCHgAKf@CGq@?Cq@IaCwB?MSCe@SNMSRC]HA]l@e@{@NK]tBwAu\\FIiDBAsD??}DZjBkL@?q@@Ai@?{@_ID]gEF[gEBUiDJqAsMD@aF@LcEBx@cBPy@qEBIkCBEqDJSiO@KoPQkAmVEMkHGEyEW?mCAEkCn@e@uONEkM\\CeKXHeKJFeKDBkMRXwLn@hBuf@Xd@qMPLkMv@L}g@NB}I?G}I@OkOBIwLMU{EQk@{EC[{E@qA}QQo@gYIm@cLAQcLDiA}ZEaAsNIe@oIU]}PKKuMMOuMk@i@gTcAYuR?MqEGA{CEAcBECcBCEcBOUjCQk@ReA_IpW[eA{@M]]HF]V`@]N`@]Ph@mG\\jBeIRz@YRl@qIHRqEDLeEN^wDPHmDRG_DHO_D?O_DCQ_D]aAqOEWkHAGkHAU_IOiDslAe@oEmLG{BlEAcApD@GxABUlBV{@sKTg@}GvFwH{aG~CwCov@~Am@sj@BCkIDAgIRMaIhAeAeXzAcB{f@z@s@{Nr@uAgM^m@oFLOwBDB_DFDgEDIoFPOyFZSsEKUmDU{@eFSaAwDCQ}BGa@cDMgAgJ]qCwLIcAgGIaDgK@G[@I]@IBBIiDBEiDn@i@}NLKaDBGyCDEqCHUiCEGaCDC}ADGaCHK_Db@c@yKZ[yFj@_@qPbAWsRbAOmBdAG}@fABmAj@HrAzE~@dCpADsHh@CgEZA_B@GyABCwAvDiAyo@\\OqEFCcED?qDz@G}LhAQoPfCi@_NlGk@bJmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCi@BzGqAEhV{E_Aju@k@IbEgAC`JeAFbCcANAcAViAk@^_A[Za@c@b@mAIJk@EFREBRDFRITREDRCFRMJRo@h@lBCDdACHvGAHvGAHvGAFvGH`Dt\\HbA~E\\pC`WLfArIF`@hDBPhDR`AXTz@z@JTz@[RxAQNxAEHvBGEtCECpBMNnA_@l@fCs@tAfQ{@r@zJ{AbB~\\iAdAnUSLvBE@vBCBvB_Bl@fR_DvCp~@wFvH|zBUf@|BWz@vZCT~OAFxO@bAb`@FzBbiAd@nE`{ANhD|V@TyA@FyADVyA\\`AcABPo@?NoAIN_@SFLQIz@O_@lBEMzCIShESm@hYS{@vZ]kB~i@Qi@jHOa@jHWa@dFIG~CL\\~CZdA~HdA~Hll@Pj@lGNT\\BD\\DB\\D@\\F@\\?L\\bAXr@j@h@YLNa@JJiFT\\aBHd@mBD`AaGEhA_B@PkCHl@bBPn@fEApAjJBZpEPj@pELTpECHpEANtM?FtMOCtMw@Mfc@QMxFYe@~Zo@iBns@SYhIECpGKGzEYIzE]BzEODvLo@d@jZ@D|IV?|IFDrLDLjOPjA`WAJxCKRjHCDjHCHjHQx@hXCy@pYAMdKEAdKKpA`WCT`FGZjCE\\jC?z@fOA@jHA?`M[kBvj@??jCC@dAGH?uBvAni@OJfEm@d@~HI@f@SBp@OLp@Bd@xA?L`CH`CzT?BhDG@jCI@pB}@ZdD_DbAqJKD{@KDq@{C~@zBoHhBls@K?`BSCxAGBnAO@hAUJdACB`AEB|@oIxApDE@Sk@HaCG?mA[BkAU@kAG^iACBiAqADkIqAFwIK?sAI@qAgA?{H{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
                .statusCode(200);
    }

    @Test
    public void testWaypoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].way_points", hasItems(0, 302, 540))
                .statusCode(200);
    }

    @Test
    public void testBbox() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].bbox", hasItems(8.678615f, 49.393272f, 8.714833f, 49.424603f))
                .statusCode(200);
    }

    @Test
    public void testManeuver() {
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);
        body.put("maneuvers", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
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
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
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
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

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
        JSONObject body = new JSONObject();
        body.put("coordinates", (JSONArray) getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", constructExtras("surface|suitability|steepness"));

        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}");

        Assert.assertEquals(response.getStatusCode(), 200);

        checkExtraConsistency(response);
    }

    @Test
    public void testTrailDifficultyExtraDetails() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.763442,49.388882|8.762927,49.397541"));
        body.put("preference", "fastest");
        body.put("instructions", true);
        body.put("extra_info", constructExtras("suitability|traildifficulty"));

        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "cycling-regular")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

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

        body = new JSONObject();
        body.put("coordinates", constructCoords("8.724174,49.390223|8.716536,49.399622"));
        body.put("preference", "fastest");
        body.put("instructions", true);
        body.put("extra_info", constructExtras("traildifficulty"));

        response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "foot-hiking")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

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
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.676281,49.414715|8.6483,49.413291"));
        body.put("preference", "fastest");
        body.put("instructions", true);
        body.put("extra_info", constructExtras("suitability|tollways"));

        // Test that the response indicates that the whole route is tollway free. The first two tests check that the waypoint ids
        // in the extras.tollways.values match the final waypoint of the route
        Response response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

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
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

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

        JSONObject restrictions = new JSONObject();
        restrictions.put("width", 2);
        restrictions.put("height", 2);
        restrictions.put("weight", 14);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        options.put("vehicle_type", "hgv");
        body.put("options", options);
        body.put("continue_straight", false);

        response = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}");

        response.then().log().ifValidationFails()
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
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.684081,49.398155|8.684703,49.397359"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("optimized", false);

        // Test that the "right turn only" restriction at the junction is taken into account
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(693.8f))
                .statusCode(200);
    }

    @Test
    public void testNoBearings() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", "fastest");
        body.put("geometry", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "cycling-regular")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                //.body("routes[0].summary.distance", is(620.1f))
                .body("routes[0].summary.distance", is(617.1f))
                .statusCode(200);
    }

    @Test
    public void testBearingsForStartAndEndPoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", "fastest");
        body.put("geometry", true);
        body.put("bearings", constructBearings("25,30|90,20"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "cycling-regular")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(804.9f))
                .statusCode(200);
    }

    @Test
    public void testBearingsExceptLastPoint() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", "fastest");
        body.put("geometry", true);
        body.put("bearings", constructBearings("25,30"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "cycling-regular")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(647.2f))
                .statusCode(200);
    }

    @Test
    public void testBearingsSkipwaypoint() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", "fastest");
        body.put("geometry", true);
        body.put("bearings", constructBearings("|90,20"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "cycling-regular")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                //.body("routes[0].summary.distance", is(714.7f))
                .body("routes[0].summary.distance", is(751.5f))
                .statusCode(200);
    }

    @Test
    public void testSteps() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
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
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
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
                .body("routes[0].segments[0].steps[9].duration", is(9f))
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
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.690915,49.430117|8.68834,49.427758"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject restrictions = new JSONObject();
        restrictions.put("width", 3);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        options.put("vehicle_type", "hgv");
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(809.3f))
                .body("routes[0].summary.duration", is(239.1f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("width", 2);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        options.put("vehicle_type", "hgv");
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(379.5f))
                .body("routes[0].summary.duration", is(136.0f))
                .statusCode(200);
    }

    @Test
    public void testHGVHeightRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.687992,49.426312|8.691315,49.425962"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject restrictions = new JSONObject();
        restrictions.put("height", 4);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        options.put("vehicle_type", "hgv");
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(549.0f))
                .body("routes[0].summary.duration", is(163.2f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("height", 2);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        options.put("vehicle_type", "hgv");
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(376.5f))
                .body("routes[0].summary.duration", is(130.0f))
                .statusCode(200);
    }

    @Test
    public void testCarDistanceAndDuration() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.690915,49.430117|8.68834,49.427758"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        // Generic test to ensure that the distance and duration dont get changed
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(379.5f))
                .body("routes[0].summary.duration", is(270.0f))
                .statusCode(200);
    }

    // test fitness params bike..

    @Test
    public void testBordersAvoid() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_borders", "controlled");
        body.put("options", options);

        // Test that providing border control in avoid_features works
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(1394.4f))
                .statusCode(200);

        options = new JSONObject();
        options.put("avoid_borders", "all");
        body.put("options", options);

        // Option 1 signifies that the route should not cross any borders
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(false))
                .body("error.code", is(RoutingErrorCodes.ROUTE_NOT_FOUND))
                .statusCode(404);
    }

    @Test
    public void testCountryExclusion() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_countries", constructFromPipedList("3"));
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(1147.0f))
                .statusCode(200);

        options = new JSONObject();
        options.put("avoid_countries", constructFromPipedList("1|3"));
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.3f))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-2 parameters
        options.put("avoid_countries", constructFromPipedList("AT|FR"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.3f))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-3 parameters
        options.put("avoid_countries", constructFromPipedList("AUT|FRA"));
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.3f))
                .statusCode(200);

    }

    @Test
    public void testBordersAndCountry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_borders", "controlled");
        options.put("avoid_countries", constructFromPipedList("1"));
        body.put("options", options);

        // Test that routing avoids crossing into borders specified
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(false))
                .body("error.code", is(RoutingErrorCodes.ROUTE_NOT_FOUND))
                .statusCode(404);
    }

    @Test
    public void testDetourFactor() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");
        body.put("attributes", constructFromPipedList("detourfactor"));

        // Test that a detourfactor is returned when requested
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].segments[0].detourfactor", is(1.3f))
                .statusCode(200);
    }

    @Test
    public void testAvoidArea() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");

        JSONObject avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[\"8.680\",\"49.421\"],[\"8.687\",\"49.421\"],[\"8.687\",\"49.418\"],[\"8.680\",\"49.418\"],[\"8.680\",\"49.421\"]]]}}");
        JSONObject options = new JSONObject();
        options.put("avoid_polygons", avoidGeom);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(2181.7f))
                .body("routes[0].summary.duration", is(433.2f))
                .statusCode(200);
    }


    @Test
    public void testWheelchairWidthRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.708605,49.410688|8.709844,49.411160"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject restrictions = new JSONObject();
        restrictions.put("minimum_width", 2.0);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(129.6f))
                .body("routes[0].summary.duration", is(93.3f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("minimum_width", 2.1);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(158.7f))
                .body("routes[0].summary.duration", is(114.3f))
                .statusCode(200);
    }

    @Test
    public void testWheelchairInclineRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.670290,49.418041|8.667490,49.418376"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject restrictions = new JSONObject();
        restrictions.put("maximum_incline", 0.0);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(591.6f))
                .body("routes[0].summary.duration", is(498.7f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("maximum_incline", 2);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(230.5f))
                .body("routes[0].summary.duration", is(172.5f))
                .statusCode(200);
    }

    @Test
    public void testWheelchairKerbRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.681125,49.403070|8.681434,49.402991"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        JSONObject restrictions = new JSONObject();
        restrictions.put("maximum_sloped_kerb", 0.1);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(74.1f))
                .body("routes[0].summary.duration", is(57.9f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("maximum_sloped_kerb", 0.03);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(146.7f))
                .body("routes[0].summary.duration", is(126.1f))
                .statusCode(200);
    }

    @Test
    public void testWheelchairSurfaceRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.686388,49.412449|8.690858,49.413009"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        JSONObject restrictions = new JSONObject();
        restrictions.put("surface_type", "cobblestone");
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(333.7f))
                .body("routes[0].summary.duration", is(240.3f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("surface_type", "paved");
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(336.0f))
                .body("routes[0].summary.duration", is(302.4f))
                .statusCode(200);
    }

    @Test
    public void testWheelchairSmoothnessRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.676730,49.421513|8.678545,49.421117"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        JSONObject restrictions = new JSONObject();
        restrictions.put("smoothness_type", "excellent");
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(748.4f))
                .body("routes[0].summary.duration", is(593.3f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("smoothness_type", "bad");
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(172.1f))
                .body("routes[0].summary.duration", is(129.2f))
                .statusCode(200);
    }

    @Test
    public void testOsmIdExtras() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.676730,49.421513|8.678545,49.421117"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("extra_info", constructExtras("osmid"));

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('osmId')", is(true))
                .statusCode(200);
    }

    @Test
    public void testAccessRestrictionsWarnings() {
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
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].code", is(1))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('roadaccessrestrictions')", is(true))
                .body("routes[0].extras.roadaccessrestrictions.values[1][2]", is(32))
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
                .body("features[0].properties.warnings[0].code", is(1))
                .body("features[0].properties.extras.containsKey('roadaccessrestrictions')", is(true))
                .body("features[0].properties.extras.roadaccessrestrictions.values[1][2]", is(32))
                .statusCode(200);
    }

    @Test
    public void testSimplifyHasLessWayPoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(534))
                .statusCode(200);

        body.put("geometry_simplify", true);

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(299))
                .statusCode(200);
    }

    @Test
    public void testSkipSegmentWarning() {
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
                .body("routes[0].warnings[0].code", is(3))
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
                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].code", is(3))
                .statusCode(200);
    }

    @Test
    public void testSkipSegments() {
        List<Integer> skipSegments = new ArrayList<>(1);
        skipSegments.add(1);

        JSONObject body = new JSONObject();
        body.put("skip_segments", skipSegments);

        body.put("coordinates", getParameter("coordinatesShort"));
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
                .body("routes[0].summary.distance", is(1744.3f))
                .body("routes[0].containsKey('geometry')", is(true))
                .body("routes[0].containsKey('way_points')", is(true))
                .body("routes[0].geometry", is("gvqlHi`~s@ooAix@"))
                .body("routes[0].way_points[0]", is(0))
                .body("routes[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[0].distance", is(1744.3f))
                .body("routes[0].segments[0].steps[0].duration", is(0.0f))
                .body("routes[0].segments[0].steps[0].type", is(11))
                .body("routes[0].segments[0].steps[0].name", is("free hand route"))
                .body("routes[0].segments[0].steps[0].containsKey('instruction')", is(true))
                .body("routes[0].segments[0].steps[0].containsKey('way_points')", is(true))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))

                .body("routes[0].segments[0].steps[1].distance", is(0.0f))
                .body("routes[0].segments[0].steps[1].duration", is(0.0f))
                .body("routes[0].segments[0].steps[1].type", is(10))
                .body("routes[0].segments[0].steps[1].name", is("end of free hand route"))
                .body("routes[0].segments[0].steps[1].containsKey('instruction')", is(true))
                .body("routes[0].segments[0].steps[1].containsKey('way_points')", is(true))
                .body("routes[0].segments[0].steps[1].way_points[0]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[1]", is(1))

                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].containsKey('code')", is(true))
                .body("routes[0].warnings[0].code", is(3))
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
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].geometry.coordinates[0][0]", is(8.678613f))
                .body("features[0].geometry.coordinates[0][1]", is(49.411721f))
                .body("features[0].geometry.coordinates[1][0]", is(8.687782f))
                .body("features[0].geometry.coordinates[1][1]", is(49.424597f))
                .body("features[0].geometry.type", is("LineString"))
                .body("features[0].properties.containsKey('segments')", is(true))
                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.containsKey('summary')", is(true))
                .body("features[0].properties.containsKey('way_points')", is(true))

                .body("features[0].properties.segments[0].distance", is(1744.3f))
                .body("features[0].properties.segments[0].steps[0].distance", is(1744.3f))
                .body("features[0].properties.segments[0].steps[0].duration", is(0.0f))
                .body("features[0].properties.segments[0].steps[0].type", is(11))
                .body("features[0].properties.segments[0].steps[0].name", is("free hand route"))
                .body("features[0].properties.segments[0].steps[0].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[0].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[0].way_points[0]", is(0))
                .body("features[0].properties.segments[0].steps[0].way_points[1]", is(1))

                .body("features[0].properties.segments[0].steps[1].distance", is(0.0f))
                .body("features[0].properties.segments[0].steps[1].duration", is(0.0f))
                .body("features[0].properties.segments[0].steps[1].type", is(10))
                .body("features[0].properties.segments[0].steps[1].name", is("end of free hand route"))
                .body("features[0].properties.segments[0].steps[1].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[1].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[1].way_points[0]", is(1))
                .body("features[0].properties.segments[0].steps[1].way_points[1]", is(1))


                .body("features[0].properties.summary.distance", is(1744.3f))
                .body("features[0].properties.way_points[0]", is(0))
                .body("features[0].properties.way_points[1]", is(1))

                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].code", is(3))
                .statusCode(200);

        body.put("coordinates", getParameter("coordinatesLong"));
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
                .body("routes[0].summary.distance", is(10936.3f))
                .body("routes[0].containsKey('geometry')", is(true))
                .body("routes[0].containsKey('way_points')", is(true))
                .body("routes[0].geometry", is("gvqlHi`~s@hrBw`Fq@lAiEf@qEn@wH^[@i@BqAEuDu@qASgACi@B_BRs@N]L]" +
                        "X_A~@IJEFEBGFCBODSEUYMg@yAeKGq@O{CS{Bk@sEk@uDYkAGOSMK?IBIHGJQXg@p@cA`A_@f@MVIPs@pA_@j@GLEFg@j@" +
                        "gA~@k@v@KRMTo@tA_@lAa@fBW`B?J?D@DJFD?FC\\oAVk@l@q@z@a@|@Sn@Br@XZPPRHN@FDVARU`AStAGb@If@Ib@Q~@[" +
                        "fBm@dEEt@Ar@FbCCjBEl@O~@Kd@EPEROx@Kf@Sv@Sf@GPGPOZGDICCS?A@Ab@uA@G?C@m@OoAEy@?i@?SAm@EQEAEBQZKTC" +
                        "FGLKTm@rAEHEF]b@oCrBEN?@?@BB@?@@bAGz@MDBBH@JCLY^g@\\g@PQFIBcAh@_BzA_@^CBSV[t@Oh@G\\WlDKr@AJIh@I" +
                        "PE@JpE?d@?tA?rA?v@?n@@`@?HHfAJfARjB@TPdBJdAT|BBPDh@BNDZFr@D`@b@pEBVP~ARnBBLZxCD\\JhA@T[H_@HQFw@V" +
                        "eBh@m@NgAXo@PsA`@QDSFcBf@{@X_@LKBO@M@Y@C?[BmJ`Be@ROFO?qADqAFK?I@gA?{@Bk@@o@BiCHO@C?k@@m@HOD]VgA" +
                        "lA_AfAUREDC?Q?OBE@qBn@A@SHOJELCDgAb@q@\\mAt@y@f@y@XeBt@YJsBp@c@N{C`A_DfAuAf@MHKJQVEEACCGI?KB"))
                .body("routes[0].way_points[0]", is(0))
                .body("routes[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[0].distance", is(4499.5f))
                .body("routes[0].segments[0].steps[0].duration", is(561.2f))
                .body("routes[0].segments[0].steps[0].type", is(11))
                .body("routes[0].segments[0].steps[0].name", is("free hand route"))
                .body("routes[0].segments[0].steps[0].containsKey('instruction')", is(true))
                .body("routes[0].segments[0].steps[0].containsKey('way_points')", is(true))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))

                .body("routes[0].segments[0].steps[1].distance", is(0.0f))
                .body("routes[0].segments[0].steps[1].duration", is(0.0f))
                .body("routes[0].segments[0].steps[1].type", is(10))
                .body("routes[0].segments[0].steps[1].name", is("end of free hand route"))
                .body("routes[0].segments[0].steps[1].containsKey('instruction')", is(true))
                .body("routes[0].segments[0].steps[1].containsKey('way_points')", is(true))
                .body("routes[0].segments[0].steps[1].way_points[0]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[1]", is(1))

                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].containsKey('code')", is(true))
                .body("routes[0].warnings[0].code", is(3))
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
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].geometry.type", is("LineString"))
                .body("features[0].properties.containsKey('segments')", is(true))
                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.containsKey('summary')", is(true))
                .body("features[0].properties.containsKey('way_points')", is(true))

                .body("features[0].properties.segments[0].distance", is(4499.5f))
                .body("features[0].properties.segments[0].duration", is(561.2f))
                .body("features[0].properties.segments[0].steps[0].distance", is(4499.5f))
                .body("features[0].properties.segments[0].steps[0].duration", is(561.2f))
                .body("features[0].properties.segments[0].steps[0].type", is(11))
                .body("features[0].properties.segments[0].steps[0].name", is("free hand route"))
                .body("features[0].properties.segments[0].steps[0].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[0].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[0].way_points[0]", is(0))
                .body("features[0].properties.segments[0].steps[0].way_points[1]", is(1))

                .body("features[0].properties.segments[0].steps[1].distance", is(0.0f))
                .body("features[0].properties.segments[0].steps[1].duration", is(0.0f))
                .body("features[0].properties.segments[0].steps[1].type", is(10))
                .body("features[0].properties.segments[0].steps[1].name", is("end of free hand route"))
                .body("features[0].properties.segments[0].steps[1].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[1].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[1].way_points[0]", is(1))
                .body("features[0].properties.segments[0].steps[1].way_points[1]", is(1))


                .body("features[0].properties.summary.distance", is(10936.3f))
                .body("features[0].properties.summary.duration", is(1364.0f))
                .body("features[0].properties.way_points[0]", is(0))
                .body("features[0].properties.way_points[1]", is(1))

                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].code", is(3))
                .statusCode(200);
    }

    @Test
    public void testAvgSpeedValues() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        JSONArray attributes = new JSONArray();
        attributes.put("avgspeed");
        body.put("attributes", attributes);

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.09f))
                .statusCode(200);

        body.put("units", "km");
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.09f))
                .statusCode(200);

        body.put("units", "m");
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.09f))
                .statusCode(200);

        body.put("units", "mi");
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(19.32f))
                .statusCode(200);
    }

    @Test
    public void testPreferGreen() {
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

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(2097.2f))
                .body("routes[0].summary.duration", is(1510.0f))
                .statusCode(200);

        JSONObject weightings = new JSONObject();
        weightings.put("green", 1.0);
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
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(2308.3f))
                .body("routes[0].summary.duration", is(3323.9f))
                .statusCode(200);
    }

    @Test
    public void testPreferQuiet() {
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

        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(2097.2f))
                .body("routes[0].summary.duration", is(1510.0f))
                .statusCode(200);

        JSONObject weightings = new JSONObject();
        weightings.put("quiet", 1.0);
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
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(2878.6f))
                .body("routes[0].summary.duration", is(4145.2f))
                .statusCode(200);
    }

    @Test
    public void testRouteMergeIndexing() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.676131,49.418149|8.676142,49.417555|8.680733,49.417248"));
        body.put("preference", "fastest");

        // ensure indexing of merged routes waypoints dont get messed up
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[0]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[1]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[0]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[1]", is(4))
                .body("routes[0].segments[1].steps[1].way_points[0]", is(4))
                .body("routes[0].segments[1].steps[1].way_points[1]", is(15))
                .body("routes[0].segments[1].steps[2].way_points[0]", is(15))
                .body("routes[0].segments[1].steps[2].way_points[1]", is(15))
                .statusCode(200);
    }

    @Test
    public void testRouteMergeInstructionsWithoutGeometry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", constructCoords("8.676131,49.418149|8.676142,49.417555|8.680733,49.417248"));
        body.put("preference", "fastest");
        body.put("geometry", "false");

        // ensure indexing of merged routes waypoints dont get messed up
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('geometry')", is(false))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[0]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[1]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[0]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[1]", is(4))
                .body("routes[0].segments[1].steps[1].way_points[0]", is(4))
                .body("routes[0].segments[1].steps[1].way_points[1]", is(15))
                .body("routes[0].segments[1].steps[2].way_points[0]", is(15))
                .body("routes[0].segments[1].steps[2].way_points[1]", is(15))
                .statusCode(200);
    }

    private JSONArray constructCoords(String coordString) {
        JSONArray coordinates = new JSONArray();
        String[] coordPairs = coordString.split("\\|");
        for (String pair : coordPairs) {
            JSONArray coord = new JSONArray();
            String[] pairCoords = pair.split(",");
            coord.put(Double.parseDouble(pairCoords[0]));
            coord.put(Double.parseDouble(pairCoords[1]));
            coordinates.put(coord);
        }

        return coordinates;
    }

    private JSONArray constructBearings(String coordString) {
        JSONArray coordinates = new JSONArray();
        String[] coordPairs = coordString.split("\\|");
        for (String pair : coordPairs) {
            JSONArray coord = new JSONArray();
            if (pair != null && !pair.isEmpty()) {
                String[] pairCoords = pair.split(",");
                coord.put(Double.parseDouble(pairCoords[0]));
                coord.put(Double.parseDouble(pairCoords[1]));
            }
            coordinates.put(coord);
        }

        return coordinates;
    }


    private JSONArray constructExtras(String extrasString) {
        return constructFromPipedList(extrasString);
    }

    private JSONArray constructFromPipedList(String piped) {
        JSONArray items = new JSONArray();
        String[] extrasSplit = piped.split("\\|");
        for (String extra : extrasSplit) {
            items.put(extra);
        }
        return items;
    }
}
