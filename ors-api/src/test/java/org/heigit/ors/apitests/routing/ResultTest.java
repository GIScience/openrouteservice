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
package org.heigit.ors.apitests.routing;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.apitests.utils.CommonHeaders;
import org.heigit.ors.apitests.utils.HelperFunctions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {

    public static final RestAssuredConfig JSON_CONFIG_DOUBLE_NUMBERS = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));

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

        JSONArray coordsFoot = new JSONArray();
        JSONArray coordFoot1 = new JSONArray();
        coordFoot1.put(8.676023);
        coordFoot1.put(49.416809);
        coordsFoot.put(coordFoot1);
        JSONArray coordFoot2 = new JSONArray();
        coordFoot2.put(8.696837);
        coordFoot2.put(49.411839);
        coordsFoot.put(coordFoot2);

        addParameter("coordinatesWalking", coordsFoot);

        JSONArray coordsFootBridge = new JSONArray();
        JSONArray coordFootBridge1 = new JSONArray();
        coordFootBridge1.put(8.692013);
        coordFootBridge1.put(49.415036);
        coordsFootBridge.put(coordFootBridge1);
        JSONArray coordFootBridge2 = new JSONArray();
        coordFootBridge2.put(8.692765);
        coordFootBridge2.put(49.410540);
        coordsFootBridge.put(coordFootBridge2);

        addParameter("coordinatesWalkingBridge", coordsFootBridge);

        JSONArray coordinatesPT = new JSONArray();
        JSONArray coordinatesPTFlipped = new JSONArray();
        JSONArray coordPT1 = new JSONArray();
        coordPT1.put(8.704433);
        coordPT1.put(49.403378);
        JSONArray coordPT2 = new JSONArray();
        coordPT2.put(8.676101);
        coordPT2.put(49.408324); //
        coordinatesPT.put(coordPT1);
        coordinatesPT.put(coordPT2);
        coordinatesPTFlipped.put(coordPT2);
        coordinatesPTFlipped.put(coordPT1);

        JSONArray coordinatesPT2 = new JSONArray();
        JSONArray coordPT3 = new JSONArray();
        coordPT3.put(8.758935);
        coordPT3.put(49.337371);
        JSONArray coordPT4 = new JSONArray();
        coordPT4.put(8.771123);
        coordPT4.put(49.511863);
        coordinatesPT2.put(coordPT3);
        coordinatesPT2.put(coordPT4);

        addParameter("coordinatesPT", coordinatesPT);
        addParameter("coordinatesPTFlipped", coordinatesPTFlipped);
        addParameter("coordinatesPT2", coordinatesPT2);


        JSONArray extraInfo = new JSONArray();
        extraInfo.put("surface");
        extraInfo.put("suitability");
        extraInfo.put("steepness");
        extraInfo.put("countryinfo");
        addParameter("extra_info", extraInfo);

        addParameter("preference", "recommended");
        addParameter("bikeProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");
        addParameter("ptProfile", "public-transport");
    }

    @Test
    void testSimpleGetRoute() {
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
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
                .body("features[0].properties.summary.distance", is(closeTo(1046.2, 1)))
                .body("features[0].properties.summary.duration", is(closeTo(215.0, 1)))
                .statusCode(200);
    }

    @Test
    void testGpxExport() throws IOException, SAXException, ParserConfigurationException { // xml serialization fails, java version / jackson / spring problem? Sascha /Johannes are looking at it
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));

        JSONArray attributes = new JSONArray();
        attributes.put("avgspeed");
        body.put("attributes", attributes);

        body.put("instructions", true);

        Response response = given()
                .headers(CommonHeaders.gpxContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/gpx");

        response.then().log().ifValidationFails()
                .log().ifValidationFails()
                .assertThat()
                .contentType("application/gpx+xml;charset=UTF-8")
                .statusCode(200);
        testGpxConsistency(response, true);
        testGpxSchema(response);
        testGpxGeometry(response);

        body.put("instructions", false);
        Response response_without_instructions = given()
                .headers(CommonHeaders.gpxContent)
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
        testGpxSchema(response);
        testGpxGeometry(response_without_instructions);
    }

    private void testGpxGeometry(Response response) throws ParserConfigurationException, IOException, SAXException {
        String body = response.body().asString();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(body)));
        assertEquals("gpx", doc.getDocumentElement().getTagName());
        int doc_length = doc.getDocumentElement().getChildNodes().getLength();
        assertTrue(doc_length > 0);
        boolean gpxRte = false;
        for (int i = 0; i < doc_length; i++) {
            String item = doc.getDocumentElement().getChildNodes().item(i).getNodeName();
            if ("rte".equals(item)) {
                gpxRte = true;
                NodeList rteChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                int rteSize = rteChildren.getLength();
                assertEquals(48, rteSize);
                assertEquals(49.41172f, Float.parseFloat(rteChildren.item(0).getAttributes().getNamedItem("lat").getNodeValue()), 0.005);
                assertEquals(8.678615f, Float.parseFloat(rteChildren.item(0).getAttributes().getNamedItem("lon").getNodeValue()), 0.005);
                assertEquals(49.42208f, Float.parseFloat(rteChildren.item(rteSize / 2).getAttributes().getNamedItem("lat").getNodeValue()), 0.005);
                assertEquals(8.677165f, Float.parseFloat(rteChildren.item(rteSize / 2).getAttributes().getNamedItem("lon").getNodeValue()), 0.005);
                assertEquals(49.424603f, Float.parseFloat(rteChildren.item(rteSize - 2).getAttributes().getNamedItem("lat").getNodeValue()), 0.005); // The last item (-1) is the extension pack
                assertEquals(8.687809f, Float.parseFloat(rteChildren.item(rteSize - 2).getAttributes().getNamedItem("lon").getNodeValue()), 0.005); // The last item (-1) is the extension pack
                Node extensions = rteChildren.item(rteSize - 1);
                assertEquals(2362.2f, Float.parseFloat(extensions.getChildNodes().item(0).getTextContent()), 2);
                assertEquals(273.5f, Float.parseFloat(extensions.getChildNodes().item(1).getTextContent()), 0.2);
                assertEquals(0.0f, Float.parseFloat(extensions.getChildNodes().item(2).getTextContent()), 0.001);
                assertEquals(0.0f, Float.parseFloat(extensions.getChildNodes().item(3).getTextContent()), 0.001);
                assertEquals(31.1f, Float.parseFloat(extensions.getChildNodes().item(4).getTextContent()), 0.03);
            }
        }
        assertTrue(gpxRte);
    }

    /**
     * Validates the xml consistency of the gpx output. Instructions can be turned on or off.
     * The functions tests if all xml members are present in the output.
     * Completeness is important for the xml schema verification!
     * It does not validate the correctness of the route geometry!
     */
    private void testGpxConsistency(Response response, boolean instructions) throws ParserConfigurationException, IOException, SAXException {
        String body = response.body().asString();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(body)));
        assertEquals("gpx", doc.getDocumentElement().getTagName());
        int doc_length = doc.getDocumentElement().getChildNodes().getLength();
        assertTrue(doc_length > 0);
        boolean gpxMetadata = false;
        boolean gpxRte = false;
        boolean gpxExtensions = false;
        Node gpxMetadataNode = new IIOMetadataNode();
        for (int i = 0; i < doc_length; i++) {
            String item = doc.getDocumentElement().getChildNodes().item(i).getNodeName();
            switch (item) {
                case "metadata" -> {
                    gpxMetadata = true;
                    NodeList metadataChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int metadataSize = metadataChildren.getLength();
                    boolean metadataName = false;
                    boolean metadataDescription = false;
                    boolean metadataAuthor = false;
                    boolean metadataCopyright = false;
                    boolean metadataTime = false;
                    boolean metadataBounds = false;
                    boolean metadataExtensions = false;
                    for (int j = 0; j < metadataSize; j++) {
                        Node metadataItem = metadataChildren.item(j);
                        switch (metadataItem.getNodeName()) {
                            case "name" -> {
                                metadataName = true;
                                gpxMetadataNode = metadataItem;
                            }
                            case "desc" -> metadataDescription = true;
                            case "author" -> {
                                metadataAuthor = true;
                                NodeList authorChildren = metadataChildren.item(j).getChildNodes();
                                int authorLength = authorChildren.getLength();
                                boolean authorName = false;
                                boolean authorEmail = false;
                                boolean authorLink = false;
                                for (int k = 0; k < authorLength; k++) {
                                    Node authorItem = authorChildren.item(k);
                                    switch (authorItem.getNodeName()) {
                                        case "name" -> authorName = true;
                                        case "email" -> authorEmail = true;
                                        case "link" -> {
                                            authorLink = true;
                                            NodeList linkChildren = authorChildren.item(k).getChildNodes();
                                            int linkLength = linkChildren.getLength();
                                            boolean linkText = false;
                                            boolean linkType = false;
                                            for (int l = 0; l < linkLength; l++) {
                                                Node linkItem = linkChildren.item(l);
                                                switch (linkItem.getNodeName()) {
                                                    case "text" -> linkText = true;
                                                    case "type" -> linkType = true;
                                                }
                                            }
                                            assertTrue(linkText);
                                            assertTrue(linkType);
                                        }
                                    }
                                }
                                assertTrue(authorName);
                                assertTrue(authorEmail);
                                assertTrue(authorLink);
                            }
                            case "copyright" -> {
                                metadataCopyright = true;
                                NodeList copyrightChildren = metadataChildren.item(j).getChildNodes();
                                int copyrightLength = copyrightChildren.getLength();
                                boolean copyrightYear = false;
                                boolean copyrightLicense = false;
                                for (int k = 0; k < copyrightLength; k++) {
                                    Node copyrightItem = copyrightChildren.item(k);
                                    switch (copyrightItem.getNodeName()) {
                                        case "year" -> copyrightYear = true;
                                        case "license" -> copyrightLicense = true;
                                    }
                                }
                                assertTrue(copyrightYear);
                                assertTrue(copyrightLicense);
                            }
                            case "time" -> metadataTime = true;
                            case "extensions" -> {
                                metadataExtensions = true;
                                int metadataExtensionsLength = metadataItem.getChildNodes().getLength();
                                boolean metadataExtensionsSystemMessage = false;
                                for (int k = 0; k < metadataExtensionsLength; k++) {
                                    Node extensionsElement = metadataItem.getChildNodes().item(k);
                                    if ("system-message".equals(extensionsElement.getNodeName())) {
                                        metadataExtensionsSystemMessage = true;
                                    }
                                }
                                assertTrue(metadataExtensionsSystemMessage);
                            }
                            case "bounds" -> metadataBounds = true;
                        }
                    }
                    assertTrue(metadataName);
                    assertEquals("ORSRouting", gpxMetadataNode.getTextContent());
                    assertTrue(metadataDescription);
                    assertTrue(metadataAuthor);
                    assertTrue(metadataCopyright);
                    assertTrue(metadataTime);
                    assertTrue(metadataBounds);
                    assertTrue(metadataExtensions);
                }
                case "rte" -> {
                    gpxRte = true;
                    NodeList rteChildren = doc.getDocumentElement().getChildNodes().item(i).getChildNodes();
                    int rteSize = rteChildren.getLength();
                    boolean rtept = false;
                    boolean routeExtension = false;
                    for (int j = 0; j < rteSize; j++) {
                        Node rteElement = rteChildren.item(j);
                        switch (rteElement.getNodeName()) {
                            case "rtept" -> {
                                rtept = true;
                                if (instructions) {
                                    int rteptLength = rteElement.getChildNodes().getLength();
                                    boolean rteptName = false;
                                    boolean rteptDescription = false;
                                    boolean rteptextensions = false;
                                    for (int k = 0; k < rteptLength; k++) {
                                        Node rteptElement = rteElement.getChildNodes().item(k);
                                        switch (rteptElement.getNodeName()) {
                                            case "name" -> rteptName = true;
                                            case "desc" -> rteptDescription = true;
                                            case "extensions" -> {
                                                rteptextensions = true;
                                                int rteptExtensionLength = rteptElement.getChildNodes().getLength();
                                                boolean distance = false;
                                                boolean duration = false;
                                                boolean type = false;
                                                boolean step = false;
                                                for (int l = 0; l < rteptExtensionLength; l++) {
                                                    Node rteptExtensionElement = rteptElement.getChildNodes().item(l);
                                                    switch (rteptExtensionElement.getNodeName()) {
                                                        case "distance" -> distance = true;
                                                        case "duration" -> duration = true;
                                                        case "type" -> type = true;
                                                        case "step" -> step = true;
                                                    }
                                                }
                                                assertTrue(distance);
                                                assertTrue(duration);
                                                assertTrue(type);
                                                assertTrue(step);
                                            }
                                        }
                                    }
                                    assertTrue(rteptName);
                                    assertTrue(rteptDescription);
                                    assertTrue(rteptextensions);
                                }
                            }
                            case "extensions" -> {
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
                                        case "distance" -> rteExtensionsDistance = true;
                                        case "duration" -> rteExtensionsDuration = true;
                                        case "ascent" -> rteExtensionsAscent = true;
                                        case "descent" -> rteExtensionsDescent = true;
                                        case "avgspeed" -> rteExtensionsAvgSpeed = true;
                                        case "bounds" -> rteExtensionsBounds = true;
                                    }
                                }
                                assertTrue(rteExtensionsDistance);
                                assertTrue(rteExtensionsDuration);
                                assertTrue(rteExtensionsAscent);
                                assertTrue(rteExtensionsDescent);
                                assertTrue(rteExtensionsAvgSpeed);
                                assertTrue(rteExtensionsBounds);
                            }
                        }
                    }
                    assertTrue(rtept);
                    assertTrue(routeExtension);
                }
                case "extensions" -> {
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
                            case "attribution" -> gpxExtensionattribution = true;
                            case "engine" -> gpxExtensionengine = true;
                            case "build_date" -> gpxExtensionbuild_date = true;
                            case "profile" -> gpxExtensionprofile = true;
                            case "preference" -> gpxExtensionpreference = true;
                            case "language" -> gpxExtensionlanguage = true;
                            case "instructions" -> gpxExtensioninstructions = true;
                            case "elevation" -> gpxExtensionelevation = true;
                        }
                    }
                    assertTrue(gpxExtensionattribution);
                    assertTrue(gpxExtensionengine);
                    assertTrue(gpxExtensionbuild_date);
                    assertTrue(gpxExtensionprofile);
                    assertTrue(gpxExtensionpreference);
                    assertTrue(gpxExtensionlanguage);
                    assertTrue(gpxExtensioninstructions);
                    assertTrue(gpxExtensionelevation);
                }
            }
        }
        assertTrue(gpxMetadata);
        assertTrue(gpxRte);
        assertTrue(gpxExtensions);
    }


    /**
     * Validates the gpx against the ors xsd schema.
     */
    private void testGpxSchema(Response response) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String xsdSchema = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd" xmlns:xs="http://www.w3.org/2001/XMLSchema">
                    <xs:element name="gpx" type="ors:gpxType" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                    <xs:complexType name="extensionsType">
                        <xs:sequence>
                            <xs:element type="xs:string" name="distance" minOccurs="0"/>
                            <xs:element type="xs:string" name="duration" minOccurs="0"/>
                            <xs:element name="type" minOccurs="0">
                                <xs:simpleType>
                                    <xs:restriction base="xs:string">
                                        <xs:enumeration value="0"/>
                                        <xs:enumeration value="1"/>
                                        <xs:enumeration value="2"/>
                                        <xs:enumeration value="3"/>
                                        <xs:enumeration value="4"/>
                                        <xs:enumeration value="5"/>
                                        <xs:enumeration value="6"/>
                                        <xs:enumeration value="7"/>
                                        <xs:enumeration value="8"/>
                                        <xs:enumeration value="9"/>
                                        <xs:enumeration value="10"/>
                                        <xs:enumeration value="11"/>
                                        <xs:enumeration value="12"/>
                                        <xs:enumeration value="13"/>
                                    </xs:restriction>
                                </xs:simpleType>
                            </xs:element>
                            <xs:element type="xs:string" name="step" minOccurs="0"/>
                            <xs:element type="xs:string" name="distanceActual" minOccurs="0"/>
                            <xs:element type="xs:string" name="ascent" minOccurs="0"/>
                            <xs:element type="xs:string" name="descent" minOccurs="0"/>
                            <xs:element type="xs:string" name="avgspeed" minOccurs="0"/>
                            <xs:element type="xs:string" name="attribution" minOccurs="0"/>
                            <xs:element type="xs:string" name="engine" minOccurs="0"/>
                            <xs:element type="xs:string" name="build_date" minOccurs="0"/>
                            <xs:element type="xs:string" name="profile" minOccurs="0"/>
                            <xs:element type="xs:string" name="preference" minOccurs="0"/>
                            <xs:element type="xs:string" name="language" minOccurs="0"/>
                            <xs:element type="xs:string" name="distance-units" minOccurs="0"/>
                            <xs:element type="xs:string" name="instructions" minOccurs="0"/>
                            <xs:element type="xs:string" name="elevation" minOccurs="0"/>
                            <xs:element type="ors:boundsType" name="bounds" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd" minOccurs="0"/>
                            <xs:element type="xs:string" name="system-message" minOccurs="0"/>
                        </xs:sequence>
                    </xs:complexType>
                    <xs:complexType name="metadataType">
                        <xs:sequence>
                            <xs:element type="xs:string" name="name"/>
                            <xs:element type="xs:string" name="desc"/>
                            <xs:element type="ors:authorType" name="author" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:copyrightType" name="copyright" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="xs:string" name="time"/>
                            <xs:element type="ors:boundsType" name="bounds" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:extensionsType" name="extensions" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                        </xs:sequence>
                    </xs:complexType>
                    <xs:complexType name="boundsType">
                        <xs:simpleContent>
                            <xs:extension base="xs:string">
                                <xs:attribute type="xs:string" name="minLat"/>
                                <xs:attribute type="xs:string" name="minLon"/>
                                <xs:attribute type="xs:string" name="maxLat"/>
                                <xs:attribute type="xs:string" name="maxLon"/>
                            </xs:extension>
                        </xs:simpleContent>
                    </xs:complexType>
                    <xs:complexType name="linkType">
                        <xs:sequence>
                            <xs:element type="xs:string" name="text"/>
                            <xs:element type="xs:string" name="type"/>
                        </xs:sequence>
                        <xs:attribute type="xs:string" name="href"/>
                    </xs:complexType>
                    <xs:complexType name="gpxType">
                        <xs:sequence>
                            <xs:element type="ors:metadataType" name="metadata" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:rteType" name="rte" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:extensionsType" name="extensions" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                        </xs:sequence>
                        <xs:attribute type="xs:string" name="version"/>
                        <xs:attribute type="xs:string" name="creator"/>
                    </xs:complexType>
                    <xs:complexType name="emailType">
                        <xs:simpleContent>
                            <xs:extension base="xs:string">
                                <xs:attribute type="xs:string" name="id"/>
                                <xs:attribute type="xs:string" name="domain"/>
                            </xs:extension>
                        </xs:simpleContent>
                    </xs:complexType>
                    <xs:complexType name="authorType">
                        <xs:sequence>
                            <xs:element type="xs:string" name="name"/>
                            <xs:element type="ors:emailType" name="email" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:linkType" name="link" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                        </xs:sequence>
                    </xs:complexType>
                    <xs:complexType name="copyrightType">
                        <xs:sequence>
                            <xs:element type="xs:string" name="year"/>
                            <xs:element type="xs:string" name="license"/>
                        </xs:sequence>
                        <xs:attribute type="xs:string" name="author"/>
                    </xs:complexType>
                    <xs:complexType name="rteptType">
                        <xs:sequence>
                            <xs:element type="xs:decimal" name="ele" minOccurs="0"/>
                            <xs:element type="xs:string" name="name"/>
                            <xs:element type="xs:string" name="desc"/>
                            <xs:element type="ors:extensionsType" name="extensions" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                        </xs:sequence>
                        <xs:attribute type="xs:string" name="lat" use="optional"/>
                        <xs:attribute type="xs:string" name="lon" use="optional"/>
                    </xs:complexType>
                    <xs:complexType name="rteType">
                        <xs:sequence>
                            <xs:element type="ors:rteptType" name="rtept" maxOccurs="unbounded" minOccurs="0" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                            <xs:element type="ors:extensionsType" name="extensions" xmlns:ors="https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:schema>
                """;
        Schema schema = factory.newSchema(new StreamSource(new StringReader(xsdSchema)));
        Validator validator = schema.newValidator();
        Source xmlSource = new StreamSource(new StringReader(response.body().asString()));
        validator.validate(xmlSource);
    }

    /**
     * The function validates the whole GeoJson export except segments.
     * Segments hold the instructions and are not necessary for our valid GeoJson-export.
     */
    @Test
    void testGeoJsonExport() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("any { it.key == 'bbox' }", is(true))
                .body("any { it.key == 'type' }", is(true))
                .body("features[0].containsKey('geometry')", is(true))
                .body("features[0].containsKey('type')", is(true))
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('summary')", is(true))
                .body("features[0].containsKey('bbox')", is(true))
                .body("features[0].properties.containsKey('transfers')", is(false))
                .body("features[0].properties.containsKey('fare')", is(false))
                .body("features[0].properties.containsKey('way_points')", is(true))
                .body("features[0].properties.containsKey('segments')", is(true))
                .body("features[0].properties.containsKey('extras')", is(true))
                .body("features[0].geometry.containsKey('coordinates')", is(true))
                .body("features[0].geometry.containsKey('type')", is(true))
                .body("features[0].geometry.type", is("LineString"))
                .body("features[0].type", is("Feature"))
                .body("type", is("FeatureCollection"))
                .body("metadata.containsKey('system_message')", is(true))

                .statusCode(200);
    }

    @Test
    void testIdInSummary() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("id", "request123");

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('system_message')", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))

                .statusCode(200);

        given()
                .headers(CommonHeaders.jsonContent)
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
    void testCompleteMetadata() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("id", "request123");

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .assertThat()
                .body("any {it.key == 'metadata'}", is(true))
                .body("metadata.containsKey('id')", is(true))
                .body("metadata.id", is("request123"))
                .body("metadata.containsKey('attribution')", is(true))
                .body("metadata.service", is("routing"))
                .body("metadata.containsKey('timestamp')", is(true))
                .body("metadata.containsKey('query')", is(true))
                .body("metadata.query.id", is("request123"))
                .body("metadata.query.containsKey('coordinates')", is(true))
                .body("metadata.query.coordinates.size()", is(2))
                .body("metadata.query.coordinates[0][0]", is(8.678613f))
                .body("metadata.query.coordinates[0][1]", is(49.411721f))
                .body("metadata.query.coordinates[1][0]", is(8.687782f))
                .body("metadata.query.coordinates[1][1]", is(49.424597f))
                .body("metadata.query.profile", is("driving-car"))
                .body("metadata.query.id", is("request123"))
                .body("metadata.engine.containsKey('version')", is(true))
                .body("metadata.engine.containsKey('build_date')", is(true))
                .body("metadata.engine.containsKey('graph_date')", is(true))
                .body("metadata.containsKey('system_message')", is(true))
                .statusCode(200);
    }

    @Test
    void expectSegmentsToMatchCoordinates() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .headers(CommonHeaders.jsonContent)
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
    void testSummary() { // waiting for elevation & turn restrictions
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.containsKey('transfers')", is(false))
                .body("routes[0].summary.containsKey('fare')", is(false))
                .body("routes[0].summary.containsKey('distance')", is(true))
                .body("routes[0].summary.containsKey('duration')", is(true))
                .body("routes[0].summary.containsKey('ascent')", is(true))
                .body("routes[0].summary.containsKey('descent')", is(true))
                .statusCode(200);
    }

    @Test
    void testSegmentDistances() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments.size()", is(2))

                .body("routes[0].segments[0].distance", is(closeTo(6689.5f, 1)))
                .body("routes[0].segments[0].duration", is(closeTo(1397.0f, 1)))
                .body("routes[0].segments[1].distance", is(closeTo(6377.1f, 1)))
                .body("routes[0].segments[1].duration", is(closeTo(1337.6f, 1)))
                .statusCode(200);
    }

    @Test
    void testNoLegsIfNotPT() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].containsKey('legs')", is(false))
                .statusCode(200);
    }

    @Test
    void testWaypoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].way_points", hasItems(0, 229, 426))
                .statusCode(200);
    }

    @Test
    void testBbox() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].bbox", hasItems(closeTo(8.678615, 0.1), closeTo(49.388405, 0.5), closeTo(106.83, 1), closeTo(8.719662, 0.1), closeTo(49.424603, 0.5), closeTo(411.73, 4)))
                .statusCode(200);
    }

    @Test
    void testManeuver() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);
        body.put("maneuvers", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].bbox", hasItems(closeTo(8.678615, 0.1), closeTo(49.388405f, 0.5), closeTo(106.83f, 1), closeTo(8.719662f, 0.1), closeTo(49.424603f, 0.5), closeTo(411.73f, 4)))
                .body("routes[0].segments[0].steps[0].maneuver.bearing_before", is(0))
                .body("routes[0].segments[0].steps[0].maneuver.bearing_after", is(175))
                .body("routes[0].segments[0].steps[0].maneuver.containsKey('location')", is(true))
                .body("routes[0].segments[0].steps[1].maneuver.bearing_before", is(175))
                .body("routes[0].segments[0].steps[1].maneuver.bearing_after", is(80))
                .body("routes[0].segments[0].steps[1].maneuver.location", hasItems(closeTo(8.678618, 0.1), closeTo(49.411697, 0.5)))
                .statusCode(200);
    }

    @Test
    void testExtras() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('surface')", is(true))
                .body("routes[0].extras.containsKey('suitability')", is(true))
                .body("routes[0].extras.containsKey('steepness')", is(true))
                .body("routes[0].extras.containsKey('countryinfo')", is(true))
                .statusCode(200);
    }

    @Test
    void testExtrasDetails() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", getParameter("extra_info"));

        Response response = given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

        response.then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.surface.values.size()", is(38))
                .body("routes[0].extras.surface.values[18][1]", is(181))
                .body("routes[0].extras.suitability.values[18][0]", is(359))
                .body("routes[0].extras.containsKey('steepness')", is(true))
                .statusCode(200);

        checkExtraConsistency(response);
    }

    @Test
    void testExtrasConsistency() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", constructExtras("surface|suitability|steepness"));

        Response response = given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}");

        assertEquals(200, response.getStatusCode());

        checkExtraConsistency(response);
    }

    @Test
    void testTrailDifficultyExtraDetails() { // route geometry needs to be checked, might be edge simplification issue
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.763442,49.388882|8.762927,49.397541"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", constructExtras("suitability|traildifficulty"));

        Response response = given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

        response.then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.traildifficulty.values.size()", is(3))
                .body("routes[0].extras.traildifficulty.values[0][0]", is(0))
                .body("routes[0].extras.traildifficulty.values[0][1]", is(2))
                .body("routes[0].extras.traildifficulty.values[0][2]", is(2))
                .body("routes[0].extras.traildifficulty.values[1][0]", is(2))
                .body("routes[0].extras.traildifficulty.values[1][1]", is(4))
                .body("routes[0].extras.traildifficulty.values[1][2]", is(1))
                .statusCode(200);

        checkExtraConsistency(response);

        body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.724174,49.390223|8.716536,49.399622"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", constructExtras("traildifficulty"));

        response = given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "foot-hiking")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}");

        response.then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.traildifficulty.values.size()", is(3))
                .body("routes[0].extras.traildifficulty.values[0][0]", is(0))
                .body("routes[0].extras.traildifficulty.values[0][1]", is(9))
                .body("routes[0].extras.traildifficulty.values[0][2]", is(0))
                .body("routes[0].extras.traildifficulty.values[1][0]", is(9))
                .body("routes[0].extras.traildifficulty.values[1][1]", is(25))
                .body("routes[0].extras.traildifficulty.values[1][2]", is(1))
                .body("routes[0].extras.traildifficulty.values[2][0]", is(25))
                .body("routes[0].extras.traildifficulty.values[2][1]", is(27))
                .body("routes[0].extras.traildifficulty.values[2][2]", is(0))
                .statusCode(200);

        checkExtraConsistency(response);
    }

    @Test
    void testTollwaysExtraDetails() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676281,49.414715|8.6483,49.413291"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("extra_info", constructExtras("suitability|tollways"));

        // Test that the response indicates that the whole route is tollway free. The first two tests check that the waypoint ids
        // in the extras.tollways.values match the final waypoint of the route
        Response response = given()
                .headers(CommonHeaders.jsonContent)
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
                .body("routes[0].extras.tollways.values[0][1]", is(75))
                .body("routes[0].extras.tollways.values[0][2]", is(0))
                .statusCode(200);

        checkExtraConsistency(response);

        response = given()
                .headers(CommonHeaders.jsonContent)
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
                .body("routes[0].extras.tollways.values[0][1]", is(36))
                .body("routes[0].extras.tollways.values[0][2]", is(0))
                .body("routes[0].extras.tollways.values[1][0]", is(36))
                .body("routes[0].extras.tollways.values[1][1]", is(49))
                .body("routes[0].extras.tollways.values[1][2]", is(1))
                .body("routes[0].extras.tollways.values[2][0]", is(49))
                .body("routes[0].extras.tollways.values[2][1]", is(79))
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
                .headers(CommonHeaders.jsonContent)
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
                .body("routes[0].extras.tollways.values[0][1]", is(36))
                .body("routes[0].extras.tollways.values[0][2]", is(0))
                .body("routes[0].extras.tollways.values[1][0]", is(36))
                .body("routes[0].extras.tollways.values[1][1]", is(49))
                .body("routes[0].extras.tollways.values[1][2]", is(1))
                .body("routes[0].extras.tollways.values[2][0]", is(49))
                .body("routes[0].extras.tollways.values[2][1]", is(79))
                .body("routes[0].extras.tollways.values[2][2]", is(0))
                .statusCode(200);

        checkExtraConsistency(response);
    }

    @Test
    void testGreenExtraInfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        body.put("extra_info", constructExtras("green"));

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.green.values[0][0]", is(0))
                .body("routes[0].extras.green.values[0][1]", is(6))
                .body("routes[0].extras.green.values[0][2]", is(9))
                .body("routes[0].extras.green.values[3][0]", is(11))
                .body("routes[0].extras.green.values[3][1]", is(30))
                .body("routes[0].extras.green.values[3][2]", is(10))

                .statusCode(200);
    }

    @Test
    void testNoiseExtraInfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        body.put("extra_info", constructExtras("noise"));

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.noise.values[0][0]", is(0))
                .body("routes[0].extras.noise.values[0][1]", is(8))
                .body("routes[0].extras.noise.values[0][2]", is(10))
                .body("routes[0].extras.noise.values[4][0]", is(23))
                .body("routes[0].extras.noise.values[4][1]", is(30))
                .body("routes[0].extras.noise.values[4][2]", is(9))

                .statusCode(200);
    }

    @Test
    void testCsvExtraInfo() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        body.put("extra_info", constructExtras("csv"));
        JSONObject weightings = new JSONObject();
        weightings.put("csv_factor", 1.0);
        weightings.put("csv_column", "less_than_0.5");
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.csv.values[2][0]", is(lessThan(50)))

                .statusCode(200);
    }

    @Test
    void testInvalidExtraInfoWarning() {
        JSONObject body = new JSONObject();
        body.put("preference", "recommended");
        body.put("coordinates", new JSONArray("[[8.682386, 49.417412],[8.690583, 49.413347]]"));
        body.put("extra_info", new JSONArray("[\"steepness\",\"suitability\",\"surface\",\"waycategory\",\"waytype\",\"tollways\",\"traildifficulty\",\"osmid\",\"roadaccessrestrictions\",\"countryinfo\",\"green\",\"noise\"]"));

        given()
                .headers(CommonHeaders.jsonContent)
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/foot-walking/json")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].extras.size()", is(8))
                .body("routes[0].extras.containsKey('steepness')", is(true))
                .body("routes[0].extras.containsKey('suitability')", is(true))
                .body("routes[0].extras.containsKey('surface')", is(true))
                .body("routes[0].extras.containsKey('waycategory')", is(true))
                .body("routes[0].extras.containsKey('waytypes')", is(true))
                .body("routes[0].extras.containsKey('traildifficulty')", is(true))
                .body("routes[0].extras.containsKey('green')", is(true))
                .body("routes[0].extras.containsKey('noise')", is(true))
                .body("routes[0].warnings.size()", is(1))
                .body("routes[0].warnings[0].code", is(4))
                .body("routes[0].warnings[0].message", is("Extra info requested but not available: tollways, osmid, roadaccessrestrictions, countryinfo"))
                .statusCode(200);
    }

    @Test
    void testOptimizedAndTurnRestrictions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.684081,49.398155|8.684703,49.397359"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("optimized", false);

        // Test that the "right turn only" restriction at the junction is taken into account
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testMaximumSpeed() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.63348,49.41766|8.6441,49.4672"));
        body.put("preference", getParameter("preference"));
        body.put("maximum_speed", 85);

        //Test against default maximum speed lower bound setting
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.duration", is(closeTo(1710.7, 1)))
                .statusCode(200);

        //Test profile-specific maximum speed lower bound
        body.put("maximum_speed", 75);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.duration", is(closeTo(1996.2, 1)))
                .statusCode(200);
    }

    @Test
    void testTurnRestrictions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.684081,49.398155|8.684703,49.397359"));
        body.put("preference", getParameter("preference"));

        JSONObject options = new JSONObject();
        JSONArray avoidFeatures = new JSONArray();
        body.put("options", options.put("avoid_features", avoidFeatures.put("ferries")));// enforce use of CALT over CH

        // Test that the "right turn only" restriction at the junction is taken into account
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testUTurnRestrictions() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.698302,49.412282|8.698801,49.41223"));
        body.put("preference", getParameter("preference"));

        JSONObject options = new JSONObject();
        JSONArray avoidFeatures = new JSONArray();
        body.put("options", options.put("avoid_features", avoidFeatures.put("ferries")));// enforce use of CALT over CH

        // Test that the "right turn only" restriction at the junction is taken into account
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-car")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(2968.5, 2)))//once issue#1073 is resolved it should be equal to the reference A* route distance of 2816.7
                .statusCode(200);
    }

    @Test
    void testNoBearings() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", getParameter("preference"));
        body.put("geometry", true);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(617.3f))
                .statusCode(200);
    }

    @Test
    void testBearingsForStartAndEndPoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", getParameter("preference"));
        body.put("geometry", true);
        body.put("bearings", constructBearings("25,30|90,20"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "cycling-road")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(804.9, 1)))
                .statusCode(200);
    }

    @Test
    void testBearingsExceptLastPoint() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", getParameter("preference"));
        body.put("geometry", true);
        body.put("bearings", constructBearings("25,30"));

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "cycling-road")
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
    void testBearingsSkipwaypoint() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.688694,49.399374|8.686495,49.40349"));
        body.put("preference", getParameter("preference"));
        body.put("geometry", true);
        body.put("bearings", constructBearings("|90,20"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(751.5, 1)))
                .statusCode(200);
    }

    @Test
    void testContinueStraightNoBearings() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("continue_straight", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(15173.0, 150)))
                .statusCode(200);
    }

    @Test
    void testSteps() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].segments[0].containsKey('steps')", is(true))
                .body("routes[0].segments[1].containsKey('steps')", is(true))
                .body("routes[0].segments[0].steps.size()", is(greaterThan(0)))
                .body("routes[0].segments[1].steps.size()", is(greaterThan(0)))
                .statusCode(200);
    }

    @Test
    void testStepsDetails() { // evaluate if necessary
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].segments[0].containsKey('steps')", is(true))
                .body("routes[0].segments[1].containsKey('steps')", is(true))
                .body("routes[0].segments[0].steps.size()", is(greaterThan(0)))
                .body("routes[0].segments[1].steps.size()", is(greaterThan(0)))
                .body("routes[0].segments[0].steps[3].distance", is(any(Float.TYPE)))
                .body("routes[0].segments[0].steps[3].duration", is(any(Float.TYPE)))
                .body("routes[0].segments[0].steps[3].type", is(any(Integer.TYPE)))
                .body("routes[0].segments[0].steps[3].instruction", is(any(String.class)))
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
            assertTrue(fromValue < toValue);

            for (int j = 1; j < jExtraValues.length(); j++) {
                jValues = jExtraValues.getJSONArray(j);
                int fromValue1 = jValues.getInt(0);
                int toValue1 = jValues.getInt(1);

                assertTrue(fromValue1 < toValue1);
                assertEquals(fromValue1, toValue);

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

            assertEquals(routeDistance, distance, 0.5);
            assertEquals(100, amount, 0.1);
        }
    }

    @Test
    void testVehicleType() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.71189,49.41165|8.71128,49.40971"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("vehicle_type", "hgv");
        body.put("options", options);

        // Test that buses are not allowed on Neue Schlossstraße (https://www.openstreetmap.org/way/150549948)
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(605.3, 6)))
                .statusCode(200);

        options.put("vehicle_type", "bus");
        body.put("options", options);
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1039.9, 10)))
                .statusCode(200);
    }

    @Test
    void testHGVWidthRestriction() { // check route
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.76121,49.417929|8.761028,49.419332"));
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(719.0, 1)))
                .body("routes[0].summary.duration", is(closeTo(172.5, 1)))
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(237.0, 1)))
                .body("routes[0].summary.duration", is(closeTo(57.0, 1)))
                .statusCode(200);
    }

    @Test
    void testHGVHeightRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.688941,49.427668|8.691315,49.425962"));
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(573.5, 1)))
                .body("routes[0].summary.duration", is(closeTo(183.5, 1)))
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(376.5, 1)))
                .body("routes[0].summary.duration", is(closeTo(130.0, 1)))
                .statusCode(200);
    }

    @Test
    void testCarDistanceAndDuration() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.690915,49.430117|8.68834,49.427758"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        // Generic test to ensure that the distance and duration dont get changed
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(808.0, 1)))
                .body("routes[0].summary.duration", is(closeTo(238.7, 1)))
                .statusCode(200);
    }

    // test fitness params bike..

    @Test
    void testBordersAvoid() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("optimized", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_borders", "controlled");
        body.put("options", options);

        // Test that providing border control in avoid_features works
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1404, 1)))
                .statusCode(200);

        options = new JSONObject();
        options.put("avoid_borders", "all");
        body.put("options", options);

        // Option 1 signifies that the route should not cross any borders
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testCountryExclusion() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("optimized", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_countries", constructFromPipedList("3"));
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1156.6, 1)))
                .statusCode(200);
        options = new JSONObject();
        options.put("avoid_countries", constructFromPipedList("1|3"));
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(3172.4, 3)))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-2 parameters
        options.put("avoid_countries", constructFromPipedList("AT|FR"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(3172.4, 3)))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-3 parameters
        options.put("avoid_countries", constructFromPipedList("AUT|FRA"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(3172.4f, 3)))
                .statusCode(200);

    }

    @Test
    void testBordersAndCountry() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.684682,49.401961|8.690518,49.405326"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("units", "m");

        JSONObject options = new JSONObject();
        options.put("avoid_borders", "controlled");
        options.put("avoid_countries", constructFromPipedList("1"));
        body.put("options", options);

        // Test that routing avoids crossing into borders specified
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testDetourFactor() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");
        body.put("attributes", constructFromPipedList("detourfactor"));

        // Test that a detourfactor is returned when requested
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testAvoidArea() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", "shortest");

        JSONObject avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[8.680,49.421],[8.687,49.421],[8.687,49.418],[8.680,49.418],[8.680,49.421]]]}}");
        JSONObject options = new JSONObject();
        options.put("avoid_polygons", avoidGeom);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(2181.7, 1)))
                .body("routes[0].summary.duration", is(closeTo(433.2, 1)))
                .statusCode(200);
    }


    @Test
    void testWheelchairWidthRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.708605,49.410688|8.709844,49.411160"));
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
                .headers(CommonHeaders.jsonContent)
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
                .headers(CommonHeaders.jsonContent)
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
    void testWheelchairInclineRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.670290,49.418041|8.667490,49.418376"));
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
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(284.0f))
                .body("routes[0].summary.duration", is(231.5f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("maximum_incline", 2);
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(230.5, 1)))
                .body("routes[0].summary.duration", is(closeTo(172.5, 1)))
                .statusCode(200);
    }

    @Test
    void testWheelchairKerbRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.681125,49.403070|8.681434,49.402991"));
        body.put("preference", "shortest");
        body.put("instructions", false);

        JSONObject restrictions = new JSONObject();
        restrictions.put("maximum_sloped_kerb", 0.31);
        JSONObject params = new JSONObject();
        params.put("restrictions", restrictions);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .headers(CommonHeaders.jsonContent)
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
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(105.8f))
                .body("routes[0].summary.duration", is(90.7f))
                .statusCode(200);
    }

    @Test
    void testWheelchairSurfaceRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.686388,49.412449|8.690858,49.413009"));
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(359.0, 1)))
                .body("routes[0].summary.duration", is(closeTo(264.0, 1)))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("surface_type", "cobblestone:flattened");
        params = new JSONObject();
        params.put("restrictions", restrictions);
        params.put("allow_unsuitable", true);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(380.0, 1)))
                .body("routes[0].summary.duration", is(closeTo(342.0, 1)))
                .statusCode(200);
    }

    @Test
    void testWheelchairSmoothnessRestriction() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676730,49.421513|8.678545,49.421117"));
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
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(473.7f))
                .body("routes[0].summary.duration", is(379.0f))
                .statusCode(200);

        restrictions = new JSONObject();
        restrictions.put("smoothness_type", "bad");
        params = new JSONObject();
        params.put("restrictions", restrictions);
        options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .headers(CommonHeaders.jsonContent)
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
    void testWheelchairDebugExport() {
        JSONObject body = new JSONObject();
        body.put("bbox", HelperFunctions.constructCoords("8.662440776824953, 49.41372343556617|8.677289485931398, 49.42018658125273"));
        body.put("debug", true);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath("export") + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(200);

    }

    @Test
    void testWheelchairSurfaceQualityKnown() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.6639,49.381199|8.670702,49.378978"));
        body.put("preference", "recommended");
        body.put("instructions", true);

        given()
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(749.1f, 1)))
                .body("routes[0].summary.duration", is(closeTo(559.9f, 1)))
                .statusCode(200);

        JSONObject params = new JSONObject();
        params.put("surface_quality_known", true);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(2215.7, 1)))
                .body("routes[0].summary.duration", is(closeTo(1656.7, 1)))
                .statusCode(200);
    }

    @Test
    void testWheelchairAllowUnsuitable() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.668277,49.377836|8.664753,49.376104"));
        body.put("preference", "shortest");
        body.put("instructions", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(566.4, 1)))
                .body("routes[0].summary.duration", is(closeTo(456.7, 1)))
                .statusCode(200);

        JSONObject params = new JSONObject();
        params.put("allow_unsuitable", true);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "wheelchair")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(382.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(326.0, 1)))
                .statusCode(200);
    }

    @Test
    void testOsmIdExtras() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676730,49.421513|8.678545,49.421117"));
        body.put("preference", "shortest");
        body.put("instructions", false);
        body.put("extra_info", constructExtras("osmid"));

        given()
                .headers(CommonHeaders.jsonContent)
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
    void testAccessRestrictionsWarnings() {
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
                .headers(CommonHeaders.jsonContent)
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
                .headers(CommonHeaders.geoJsonContent)
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
    void testSimplifyHasLessWayPoints() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesLong"));

        Response res = given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson");
        res.then().log().ifValidationFails()
                .assertThat()
                .statusCode(200);
        int notSimplifiedSize = res.path("features[0].geometry.coordinates.size()");

        body.put("geometry_simplify", true);

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(lessThan(notSimplifiedSize)))
                .statusCode(200);
    }

    @Test
    void testSkipSegmentWarning() {
        List<Integer> skipSegments = new ArrayList<>(1);
        skipSegments.add(1);

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));


        body.put("skip_segments", skipSegments);
        given()
                .headers(CommonHeaders.jsonContent)
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
                .headers(CommonHeaders.geoJsonContent)
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
    void testSkipSegments() {
        List<Integer> skipSegments = new ArrayList<>(1);
        skipSegments.add(1);

        JSONObject body = new JSONObject();
        body.put("skip_segments", skipSegments);

        body.put("coordinates", getParameter("coordinatesShort"));
        given()
                .headers(CommonHeaders.jsonContent)
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
                .headers(CommonHeaders.geoJsonContent)
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(10936.3, 10)))
                .body("routes[0].containsKey('geometry')", is(true))
                .body("routes[0].containsKey('way_points')", is(true))
                .body("routes[0].way_points[0]", is(0))
                .body("routes[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[0].distance", is(closeTo(4499.5, 5)))
                .body("routes[0].segments[0].steps[0].duration", is(closeTo(561.2, 1)))
                .body("routes[0].segments[0].steps[0].type", is(11))
                .body("routes[0].segments[0].steps[0].name", is("free hand route"))
                .body("routes[0].segments[0].steps[0].containsKey('instruction')", is(true))
                .body("routes[0].segments[0].steps[0].containsKey('way_points')", is(true))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))

                .body("routes[0].segments[0].steps[1].distance", is(0.0))
                .body("routes[0].segments[0].steps[1].duration", is(0.0))
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
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.geoJsonContent)
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

                .body("features[0].properties.segments[0].distance", is(closeTo(4499.5, 5)))
                .body("features[0].properties.segments[0].duration", is(closeTo(561.2, 1)))
                .body("features[0].properties.segments[0].steps[0].distance", is(closeTo(4499.5, 5)))
                .body("features[0].properties.segments[0].steps[0].duration", is(closeTo(561.2, 1)))
                .body("features[0].properties.segments[0].steps[0].type", is(11))
                .body("features[0].properties.segments[0].steps[0].name", is("free hand route"))
                .body("features[0].properties.segments[0].steps[0].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[0].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[0].way_points[0]", is(0))
                .body("features[0].properties.segments[0].steps[0].way_points[1]", is(1))

                .body("features[0].properties.segments[0].steps[1].distance", is(0.0))
                .body("features[0].properties.segments[0].steps[1].duration", is(0.0))
                .body("features[0].properties.segments[0].steps[1].type", is(10))
                .body("features[0].properties.segments[0].steps[1].name", is("end of free hand route"))
                .body("features[0].properties.segments[0].steps[1].containsKey('instruction')", is(true))
                .body("features[0].properties.segments[0].steps[1].containsKey('way_points')", is(true))
                .body("features[0].properties.segments[0].steps[1].way_points[0]", is(1))
                .body("features[0].properties.segments[0].steps[1].way_points[1]", is(1))


                .body("features[0].properties.summary.distance", is(closeTo(10936.3, 10)))
                .body("features[0].properties.summary.duration", is(closeTo(1364.0, 5)))
                .body("features[0].properties.way_points[0]", is(0))
                .body("features[0].properties.way_points[1]", is(1))

                .body("features[0].properties.containsKey('warnings')", is(true))
                .body("features[0].properties.warnings[0].containsKey('code')", is(true))
                .body("features[0].properties.warnings[0].code", is(3))
                .statusCode(200);

        skipSegments = new ArrayList<>(2);
        skipSegments.add(2);
        skipSegments.add(3);
        body.put("skip_segments", skipSegments);
        JSONArray coordsTooLong = new JSONArray();
        JSONArray coordLong1 = new JSONArray();
        coordLong1.put(8.678613);
        coordLong1.put(49.411721);
        coordsTooLong.put(coordLong1);
        JSONArray coordLong2 = new JSONArray();
        coordLong2.put(8.714733);
        coordLong2.put(49.393267);
        coordsTooLong.put(coordLong2);
        JSONArray coordLong3 = new JSONArray();
        coordLong3.put(0);
        coordLong3.put(0);
        coordsTooLong.put(coordLong3);
        JSONArray coordLong4 = new JSONArray();
        coordLong4.put(8.714733);
        coordLong4.put(49.393267);
        coordsTooLong.put(coordLong4);
        JSONArray coordLong5 = new JSONArray();
        coordLong5.put(8.687782);
        coordLong5.put(49.424597);
        coordsTooLong.put(coordLong5);
        body.put("coordinates", coordsTooLong);
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].containsKey('way_points')", is(true))
                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].containsKey('code')", is(true))
                .body("routes[0].warnings[0].code", is(3))
                .body("routes[0].segments.size()", is(4))
                .body("routes[0].way_points.size()", is(5))
                .body("routes[0].bbox[0]", is(0.0f))
                .body("routes[0].bbox[1]", is(0.0f))
                .statusCode(200);
    }

    @Test
    void testAvgSpeedValues() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        JSONArray attributes = new JSONArray();
        attributes.put("avgspeed");
        body.put("attributes", attributes);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.1f))
                .statusCode(200);

        body.put("units", "km");
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.1f))
                .statusCode(200);

        body.put("units", "m");
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('segments')", is(true))
                .body("routes[0].segments[0].containsKey('avgspeed')", is(true))
                .body("routes[0].segments[0].avgspeed", is(31.1f))
                .statusCode(200);

        body.put("units", "mi");
        given()
                .headers(CommonHeaders.jsonContent)
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
    void testPreferGreen() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2097.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(1510.0, 1)))
                .statusCode(200);

        JSONObject weightings = new JSONObject();
        weightings.put("green", 1.0);
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2308.3, 2)))
                .body("routes[0].summary.duration", is(closeTo(1662.0, 2)))
                .statusCode(200);
    }

    @Test
    void testPreferShadow() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2097.2, 2)))
                .body("routes[0].summary.duration", is(closeTo(1510.0, 2)))
                .statusCode(200);

        JSONObject weightings = new JSONObject();
        weightings.put("shadow", 1.0);
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2125.7, 2)))
                .body("routes[0].summary.duration", is(closeTo(1530.5, 2)))
                .statusCode(200);
    }

    @Test
    void testPreferQuiet() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2097.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(1510.0, 1)))
                .statusCode(200);

        JSONObject weightings = new JSONObject();
        weightings.put("quiet", 1.0);
        JSONObject params = new JSONObject();
        params.put("weightings", weightings);
        JSONObject options = new JSONObject();
        options.put("profile_params", params);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('summary')", is(true))
                .body("routes[0].summary.distance", is(closeTo(2878.7, 2)))
                .body("routes[0].summary.duration", is(closeTo(2072.6, 2)))
                .statusCode(200);
    }

    @Test
    void testRouteMergeIndexing() {
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676131,49.418149|8.676142,49.417555|8.680733,49.417248"));
        body.put("preference", getParameter("preference"));

        // ensure indexing of merged routes waypoints dont get messed up
        given()
                .headers(CommonHeaders.jsonContent)
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
                .body("routes[0].segments[1].steps[0].way_points[1]", is(3))
                .body("routes[0].segments[1].steps[1].way_points[0]", is(3))
                .body("routes[0].segments[1].steps[1].way_points[1]", is(10))
                .body("routes[0].segments[1].steps[2].way_points[0]", is(10))
                .body("routes[0].segments[1].steps[2].way_points[1]", is(10))
                .statusCode(200);
    }

    @Test
    void testIdenticalCoordinatesIndexing() { // Taki needs to look into this, see if the problem in question is addressed properly...
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676131,49.418149|8.676142,49.457555|8.676142,49.457555|8.680733,49.417248"));
        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(216))
                .body("features[0].properties.segments[1].steps[0].way_points[0]", is(107))
                .body("features[0].properties.segments[1].steps[0].way_points[1]", is(107))
                .statusCode(200);
    }

    @Test
    void testRouteMergeInstructionsWithoutGeometry() { // need to check route geometry, might be edge simplifications
        JSONObject body = new JSONObject();
        body.put("coordinates", HelperFunctions.constructCoords("8.676131,49.418149|8.676142,49.417555|8.680733,49.417248"));
        body.put("preference", getParameter("preference"));
        body.put("geometry", "false");

        // ensure indexing of merged routes waypoints dont get messed up
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('geometry')", is(false))
                .body("routes[0].segments[0].steps[0].way_points[0]", is(0))
                .body("routes[0].segments[0].steps[0].way_points[1]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[0]", is(1))
                .body("routes[0].segments[0].steps[1].way_points[1]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[0]", is(1))
                .body("routes[0].segments[1].steps[0].way_points[1]", is(3))
                .body("routes[0].segments[1].steps[1].way_points[0]", is(3))
                .body("routes[0].segments[1].steps[1].way_points[1]", is(10))
                .body("routes[0].segments[1].steps[2].way_points[0]", is(10))
                .body("routes[0].segments[1].steps[2].way_points[1]", is(10))
                .statusCode(200);
    }

    @Test
    void testCountryTraversalNoBorderCrossing() {
        JSONObject body = new JSONObject();
        JSONArray noBorderCrossing = new JSONArray();
        JSONArray coord = new JSONArray();
        coord.put(8.692256212234497);
        coord.put(49.405004518240005);
        noBorderCrossing.put(coord);
        coord = new JSONArray();
        coord.put(8.689970970153809);
        coord.put(49.40532565875338);
        noBorderCrossing.put(coord);
        body.put("coordinates", noBorderCrossing);
        JSONArray extraInfo = new JSONArray();
        extraInfo.put("countryinfo");
        body.put("extra_info", extraInfo);

        // No border crossing
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('countryinfo')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('values')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('summary')", is(true))
                .body("routes[0].extras.countryinfo.values[0][0]", is(0))
                .body("routes[0].extras.countryinfo.values[0][1]", is(2))
                .body("routes[0].extras.countryinfo.values[0][2]", is(4))
                .body("routes[0].extras.countryinfo.summary[0].value", is(4.0f))
                .body("routes[0].extras.countryinfo.summary[0].distance", is(169.2f))
                .body("routes[0].extras.countryinfo.summary[0].amount", is(100.0f))
                .statusCode(200);
    }

    @Test
    void testCountryTraversalOuterBorder() {
        JSONObject body = new JSONObject();
        JSONArray outerBorder = new JSONArray();
        JSONArray coord = new JSONArray();
        coord.put(8.688002);
        coord.put(49.392946);
        outerBorder.put(coord);
        coord = new JSONArray();
        coord.put(8.687809);
        coord.put(49.39472);
        outerBorder.put(coord);
        body.put("coordinates", outerBorder);
        JSONArray extraInfo = new JSONArray();
        extraInfo.put("countryinfo");
        body.put("extra_info", extraInfo);

        // Outside of any borders
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('countryinfo')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('values')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('summary')", is(true))
                .body("routes[0].extras.countryinfo.values", empty())
                .statusCode(200);
    }

    @Test
    void testCoutryTraversalCloseToBorder() {
        JSONObject body = new JSONObject();
        JSONArray closeToBorder = new JSONArray();
        JSONArray coord = new JSONArray();
        coord.put(8.685869872570038);
        coord.put(49.402674441283786);
        closeToBorder.put(coord);
        coord = new JSONArray();
        coord.put(8.687363862991333);
        coord.put(49.4027128404518);
        closeToBorder.put(coord);
        body.put("coordinates", closeToBorder);
        JSONArray extraInfo = new JSONArray();
        extraInfo.put("countryinfo");
        body.put("extra_info", extraInfo);

        // Close to a border crossing
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('countryinfo')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('values')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('summary')", is(true))
                .body("routes[0].extras.countryinfo.values[0][0]", is(0))
                .body("routes[0].extras.countryinfo.values[0][1]", is(2))
                .body("routes[0].extras.countryinfo.values[0][2]", is(3))
                .body("routes[0].extras.countryinfo.summary[0].value", is(3.0f))
                .body("routes[0].extras.countryinfo.summary[0].distance", is(108.0f))
                .body("routes[0].extras.countryinfo.summary[0].amount", is(100.0f))
                .statusCode(200);
    }

    @Test
    void testCountryTraversalWithBorderCrossing() {
        JSONObject body = new JSONObject();
        JSONArray borderCrossing = new JSONArray();
        JSONArray coord = new JSONArray();
        coord.put(8.685046434402466);
        coord.put(49.40267269586634);
        borderCrossing.put(coord);
        coord = new JSONArray();
        coord.put(8.687556982040405);
        coord.put(49.40271458586781);
        borderCrossing.put(coord);
        body.put("coordinates", borderCrossing);
        JSONArray extraInfo = new JSONArray();
        extraInfo.put("countryinfo");
        body.put("extra_info", extraInfo);

        // With Border crossing
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/json")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('countryinfo')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('values')", is(true))
                .body("routes[0].extras.countryinfo.containsKey('summary')", is(true))
                .body("routes[0].extras.countryinfo.values[0][0]", is(0))
                .body("routes[0].extras.countryinfo.values[0][1]", is(2))
                .body("routes[0].extras.countryinfo.values[0][2]", is(2))
                .body("routes[0].extras.countryinfo.summary[0].value", is(2.0f))
                .body("routes[0].extras.countryinfo.summary[0].distance", is(150.4f))
                .body("routes[0].extras.countryinfo.summary[0].amount", is(82.88f))
                .body("routes[0].extras.countryinfo.values[1][0]", is(2))
                .body("routes[0].extras.countryinfo.values[1][1]", is(3))
                .body("routes[0].extras.countryinfo.values[1][2]", is(3))
                .body("routes[0].extras.countryinfo.summary[1].value", is(3.0f))
                .body("routes[0].extras.countryinfo.summary[1].distance", is(31.1f))
                .body("routes[0].extras.countryinfo.summary[1].amount", is(17.12f))
                .statusCode(200);
    }

    @Test
    void testAlternativeRoutes() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.673191);
        coord1.put(49.446812);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.689499);
        coord2.put(49.398295);
        coordinates.put(coord2);

        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));
        JSONObject ar = new JSONObject();
        ar.put("target_count", "2");
        ar.put("share_factor", "0.5");
        body.put("alternative_routes", ar);
        body.put("extra_info", getParameter("extra_info"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(2))
                .body("routes[0].summary.distance", is(closeTo(5942.2, 5)))
                .body("routes[0].summary.duration", is(closeTo(776.1, 1)))
                .body("routes[1].summary.distance", is(closeTo(6435.1, 6)))
                .body("routes[1].summary.duration", is(closeTo(801.5, 1)))
                .statusCode(200);

        JSONObject avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[8.685873,49.414421], [8.688169,49.403978], [8.702095,49.407762], [8.695185,49.416013], [8.685873,49.414421]]]}}");
        JSONObject options = new JSONObject();
        options.put("avoid_polygons", avoidGeom);
        body.put("options", options);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                .body("routes[0].summary.distance", is(closeTo(6435.1, 6)))
                .body("routes[0].summary.duration", is(closeTo(801.5, 1)))
                .statusCode(200);


    }

    // TODO: revisit after the update is done, this test is to be ignored for now.
    @Test
    @Disabled("revisit after the update is done, this test is to be ignored for now.")
    void testRoundTrip() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.673191);
        coord1.put(49.446812);
        coordinates.put(coord1);
        body.put("coordinates", coordinates);
        body.put("preference", "shortest");

        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("length", "2000");
        JSONObject options = new JSONObject();

        options.put("round_trip", roundTripOptions);
        body.put("options", options);
        body.put("instructions", false);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                //A* Beeline and ALT values, respectively
                .body("routes[0].summary.distance", anyOf(is(closeTo(1866.2, 1)), is(closeTo(1792.8, 1))))
                .body("routes[0].summary.duration", anyOf(is(closeTo(1343.6, 1)), is(closeTo(1290.8, 1))))
                .statusCode(200);

        JSONObject avoidGeom = new JSONObject("{\"type\":\"Polygon\",\"coordinates\":[[[8.670658,49.446519], [8.671023,49.446331], [8.670723,49.446212], [8.670658,49.446519]]]}}");
        options.put("avoid_polygons", avoidGeom);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                .body("routes[0].summary.distance", anyOf(is(closeTo(1784.2, 1)), is(closeTo(1792.8, 1))))
                .body("routes[0].summary.duration", anyOf(is(closeTo(1284.6, 1)), is(closeTo(1290.8, 1))))
                .statusCode(200);

        options.remove("avoid_polygons");
        roundTripOptions.put("points", 3);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                .body("routes[0].summary.distance", anyOf(is(closeTo(1559.3, 1)), is(closeTo(1559.3, 1))))
                .body("routes[0].summary.duration", anyOf(is(closeTo(1122.7, 1)), is(closeTo(1122.7, 1))))
                .statusCode(200);

        body.put("bearings", constructBearings("25,30"));
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                .body("routes[0].summary.distance", anyOf(is(closeTo(2519.8, 2)), is(closeTo(2496.8, 2))))
                .body("routes[0].summary.duration", anyOf(is(closeTo(1814.2, 2)), is(closeTo(1797.6, 2))))
                .statusCode(200);
    }

    // TODO: revisit after the update is done, this test is to be ignored for now.
    @Test
    @Disabled("revisit after the update is done, this test is to be ignored for now.")
    void testWaypointCount() {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.673191);
        coord1.put(49.446812);
        coordinates.put(coord1);
        body.put("coordinates", coordinates);
        body.put("preference", "shortest");

        JSONObject roundTripOptions = new JSONObject();
        roundTripOptions.put("length", "2000");
        JSONObject options = new JSONObject();

        options.put("round_trip", roundTripOptions);
        body.put("options", options);
        body.put("instructions", false);

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].properties.way_points[1]", is(65))
                .statusCode(200);

        body = new JSONObject();
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        coordinates.put(coord2);
        body.put("coordinates", coordinates);
        body.put("preference", "shortest");

        given()
                .headers(CommonHeaders.geoJsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when().log().ifValidationFails()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("features[0].properties.way_points[1]", is(72))
                .statusCode(200);
    }

    @Test
    @Disabled("Should be tested with unit test")
    void expectNoInterpolationOfBridgesAndTunnels() { // consider rewriting as unit test
        // wait for elevation smoothing check
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesWalking"));
        body.put("preference", getParameter("preference"));
        body.put("elevation", true);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("footProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(2097.2, 1)))
                .body("routes[0].summary.ascent", is(16.7))
                .body("routes[0].summary.descent", is(14.0))
                .statusCode(200);
    }

    @Test
    void expectDepartureAndArrival() {
        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter("coordinatesShort"));
        body.put("preference", getParameter("preference"));
        body.put("departure", "2021-01-31T12:00");

        // Test that if the request specifies departure time then the response contains both departure and arrival time
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("bikeProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('departure')", is(true))
                .body("routes[0].containsKey('arrival')", is(true))
                .statusCode(200);
    }

    @Test
    void testConditionalAccess() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.645178);
        coord1.put(49.399496);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.646015);
        coord2.put(49.400899);
        coordinates.put(coord2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        // Tag "motor_vehicle:conditional = no @ Mo-Fr 12:45-13:30" on way 27884831
        // Test that way is accessible if no time is specified
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(230.2f))
                .body("routes[0].summary.duration", is(72.1f))
                .statusCode(200);

        // Test that way is accessible on weekends
        body.put("departure", "2021-01-31T13:00");
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(230.2f))
                .body("routes[0].summary.duration", is(72.1f))
                .statusCode(200);

        // Test that way is closed at certain times throughout the week
        body.put("departure", "2021-12-31T13:00");
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(369.5f))
                .body("routes[0].summary.duration", is(75.2f))
                .statusCode(200);

        // Test that a shorter route around closed edge exists
        body.put("preference", "shortest");
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(367.2f))
                .body("routes[0].summary.duration", is(88.1f))
                .statusCode(200);
    }

    @Test
    void testConditionalSpeed() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.689993);
        coord1.put(49.399208);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.692824);
        coord2.put(49.406562);
        coordinates.put(coord2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        // Tag "maxspeed:conditional = 30 @ (22:00-06:00)" along Rohrbacher Strasse
        // Test that the speed limit is not taken into account if no time is specified
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(850.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(97.9, 1)))
                .statusCode(200);

        // Test that the speed limit does not apply throughout the day
        body.put("arrival", "2021-01-31T22:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(850.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(97.9, 1)))
                .statusCode(200);

        // Test that the speed limit applies at night
        body.remove("arrival");
        body.put("departure", "2021-01-31T22:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(850.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(119.9, 1)))
                .statusCode(200);

        // Test that the speed limit applies for shortest weighting as well
        body.put("preference", "shortest");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(850.2, 1)))
                .body("routes[0].summary.duration", is(closeTo(119.9, 1)))
                .statusCode(200);
    }

    @Test
    void testTrafficSpeed() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.676538);
        coord1.put(49.412299);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.676764);
        coord2.put(49.412303);
        coordinates.put(coord2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        // Test that traffic speed limit is not taken into account if no time is specified
        body.put("optimized", "false");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1327.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(196.3, 1)))
                .statusCode(200);
        body.remove("optimized");

        // Middle of the night
        body.put("departure", "2023-05-02T03:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1327.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(194.5, 1)))
                .statusCode(200);

        // Morning rush hour
        body.put("departure", "2023-05-02T09:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(1327.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(477.9, 1)))
                .statusCode(200);
    }

    @Test
    void testTrafficSpeedMultipleMatches() {
        JSONArray coordinatesTheodorHeuss = new JSONArray();
        JSONArray coordTh1 = new JSONArray();
        coordTh1.put(8.6928696);
        coordTh1.put(49.4115257);
        coordinatesTheodorHeuss.put(coordTh1);
        JSONArray coordTh2 = new JSONArray();
        coordTh2.put(8.6923596);
        coordTh2.put(49.4134954);
        coordinatesTheodorHeuss.put(coordTh2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinatesTheodorHeuss);
        body.put("preference", getParameter("preference"));

        // No traffic data
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(222.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(20.0, 1)))
                .statusCode(200);

        // Saturday evening
        body.put("departure", "2023-05-06T17:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(222.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(28.6, 1)))
                .statusCode(200);

        // Sunday evening
        body.put("departure", "2023-05-07T17:00");
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(222.1, 1)))
                .body("routes[0].summary.duration", is(closeTo(23.5, 1)))
                .statusCode(200);
    }

    @Test
    void expectZoneMaxpeed() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.676031);
        coord1.put(49.417011);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.674965);
        coord2.put(49.419587);
        coordinates.put(coord2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        // Test that "zone:maxspeed = DE:urban" overrides default "highway = residential" speed for both car and hgv
        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(367.9f))
                .body("routes[0].summary.duration", is(53.0f))
                .statusCode(200);

        given()
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(367.9f))
                .body("routes[0].summary.duration", is(53.0f))
                .statusCode(200);
    }

    @Test
    void expectMaxpeedHgvForward() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.696237);
        coord1.put(49.37186);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.693427);
        coord2.put(49.367914);
        coordinates.put(coord2);

        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("preference", getParameter("preference"));

        // Test that "maxspeed:hgv:forward = 30" when going downhill on Am Götzenberg is taken into account for hgv profile
        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(497.5f, 4)))
                .body("routes[0].summary.duration", is(closeTo(61.9f, 0.6)))
                .statusCode(200);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", "driving-hgv")
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(497.5f, 4)))
                .body("routes[0].summary.duration", is(closeTo(81.1f, 0.8)))
                .statusCode(200);
    }

    @Test
    void testPTJSON() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.6729581);
        coord1.put(49.4468535);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.7067204);
        coord2.put(49.3786147);
        coordinates.put(coord2);
        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("instructions", true);
        body.put("elevation", true);
        body.put("departure", "2022-07-04T13:02:26Z");
        body.put("walking_time", "PT30M");
        Response res = given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("ptProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(3))
                .body("routes[0].summary.transfers", is(2))
                .body("routes[0].summary.containsKey('fare')", is(true))
                .body("routes[0].legs.size()", is(6))
                .body("routes[0].legs[0].containsKey('arrival')", is(true))
                .body("routes[0].legs[0].containsKey('departure')", is(true))
                .body("routes[0].legs[0].containsKey('instructions')", is(true))
                .body("routes[0].legs[0].containsKey('geometry')", is(true))
                .body("routes[0].legs[1].containsKey('feed_id')", is(true))
                .body("routes[0].legs[1].containsKey('trip_id')", is(true))
                .body("routes[0].legs[1].containsKey('route_id')", is(true))
                .body("routes[0].legs[1].containsKey('route_type')", is(true))
                .body("routes[0].legs[1].containsKey('route_desc')", is(true))
                .body("routes[0].legs[1].containsKey('geometry')", is(true))
                .statusCode(200).extract().response();
    }

    @Test
    void testPTGeoJSON() {
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.6729581);
        coord1.put(49.4468535);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.7067204);
        coord2.put(49.3786147);
        coordinates.put(coord2);
        JSONObject body = new JSONObject();
        body.put("coordinates", coordinates);
        body.put("instructions", true);
        body.put("elevation", true);
        body.put("departure", "2022-07-04T13:02:26Z");
        body.put("walking_time", "PT30M");
        Response res = given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("ptProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'features' }", is(true))
                .body("features.size()", is(3))
                .body("features[0].properties.transfers", is(2))
                .body("features[0].properties.containsKey('fare')", is(true))
                .body("features[0].properties.legs.size()", is(6))
                .body("features[0].properties.legs[0].containsKey('arrival')", is(true))
                .body("features[0].properties.legs[0].containsKey('departure')", is(true))
                .body("features[0].properties.legs[0].containsKey('instructions')", is(true))
                .body("features[0].properties.legs[0].containsKey('geometry')", is(true))
                .body("features[0].properties.legs[1].containsKey('feed_id')", is(true))
                .body("features[0].properties.legs[1].containsKey('trip_id')", is(true))
                .body("features[0].properties.legs[1].containsKey('route_id')", is(true))
                .body("features[0].properties.legs[1].containsKey('route_type')", is(true))
                .body("features[0].properties.legs[1].containsKey('route_desc')", is(true))
                .body("features[0].properties.legs[1].containsKey('geometry')", is(true))
                .statusCode(200).extract().response();
    }

    @ParameterizedTest
    @CsvSource({"coordinatesPT,PT1M,2013,0", "coordinatesPTFlipped,PT1M,2014,1", "coordinatesPTFlipped,PT10S,2015,2", "coordinatesPTFlipped,PT10M,2016,3", "coordinatesPT2,PT4H,2017,4"})
    void testPTFail(String coords, String walkingTime, int errorCode, int messageIndex) {
        String[] messages = {
                "PT entry point cannot be reached within given street time.",
                "PT exit point cannot be reached within given street time.",
                "PT exit point cannot be reached within given street time. PT entry point cannot be reached within given street time.",
                "PT entry and exit points found but no connecting route. Increase walking time to explore more results.",
                "Maximum number of nodes exceeded: 15000"
        };

        JSONObject body = new JSONObject();
        body.put("coordinates", getParameter(coords));
        body.put("departure", "2022-09-26T07:30:26Z");
        body.put("walking_time", walkingTime);
        given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("ptProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'error' }", is(true))
                .body("error.code", is(errorCode))
                .body("error.message", containsString(messages[messageIndex]))
                .statusCode(404);
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
