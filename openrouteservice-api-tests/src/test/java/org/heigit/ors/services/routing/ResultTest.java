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
package org.heigit.ors.services.routing;

import org.heigit.ors.services.common.EndPointAnnotation;
import org.heigit.ors.services.common.ServiceTest;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "routes")
public class ResultTest extends ServiceTest {

	public ResultTest() {

		addParameter("coordinatesShort", "8.678613,49.411721|8.687782,49.424597");
		addParameter("coordinatesLong", "8.678613,49.411721|8.714733,49.393267|8.687782,49.424597");
		addParameter("extra_info", "surface|suitability|steepness");
		addParameter("preference", "recommended");
		addParameter("bikeProfile", "cycling-regular");
		addParameter("carProfile", "driving-car");

		// query for testing the alternative routes algorithm
        addParameter("coordinatesAR", "8.673191,49.446812|8.689499,49.398295");
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
        testGpxSchema(response);

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
        testGpxSchema(response);
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
                    boolean metadataTime = false;
                    boolean metadataBounds = false;
                    boolean metadataExtensions = false;
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
                                metadataTime = true;
                                break;
                            case "extensions":
                                metadataExtensions = true;
                                int metadataExtensionsLength = metadataItem.getChildNodes().getLength();
                                boolean metadataExtensionsSystemMessage = false;
                                for (int k = 0; k < metadataExtensionsLength; k++) {
                                    Node extensionsElement = metadataItem.getChildNodes().item(k);
                                    switch (extensionsElement.getNodeName()) {
                                        case "system-message":
                                            metadataExtensionsSystemMessage = true;
                                            break;
                                    }
                                }
                                Assert.assertTrue(metadataExtensionsSystemMessage);
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
                    Assert.assertTrue(metadataTime);
                    Assert.assertTrue(metadataBounds);
                    Assert.assertTrue(metadataExtensions);
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
     * Validates the gpx against the ors xsd schema.
     */
    private void testGpxSchema(Response response) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String xsdSchema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xs:element name=\"gpx\" type=\"ors:gpxType\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "    <xs:complexType name=\"extensionsType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:string\" name=\"distance\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"duration\" minOccurs=\"0\"/>\n" +
                "            <xs:element name=\"type\" minOccurs=\"0\">\n" +
                "                <xs:simpleType>\n" +
                "                    <xs:restriction base=\"xs:string\">\n" +
                "                        <xs:enumeration value=\"0\"/>\n" +
                "                        <xs:enumeration value=\"1\"/>\n" +
                "                        <xs:enumeration value=\"2\"/>\n" +
                "                        <xs:enumeration value=\"3\"/>\n" +
                "                        <xs:enumeration value=\"4\"/>\n" +
                "                        <xs:enumeration value=\"5\"/>\n" +
                "                        <xs:enumeration value=\"6\"/>\n" +
                "                        <xs:enumeration value=\"7\"/>\n" +
                "                        <xs:enumeration value=\"8\"/>\n" +
                "                        <xs:enumeration value=\"9\"/>\n" +
                "                        <xs:enumeration value=\"10\"/>\n" +
                "                        <xs:enumeration value=\"11\"/>\n" +
                "                        <xs:enumeration value=\"12\"/>\n" +
                "                        <xs:enumeration value=\"13\"/>\n" +
                "                    </xs:restriction>\n" +
                "                </xs:simpleType>\n" +
                "            </xs:element>\n" +
                "            <xs:element type=\"xs:string\" name=\"step\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"distanceActual\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"ascent\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"descent\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"avgspeed\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"attribution\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"engine\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"build_date\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"profile\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"preference\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"language\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"distance-units\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"instructions\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"elevation\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"ors:boundsType\" name=\"bounds\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"system-message\" minOccurs=\"0\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"metadataType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:string\" name=\"name\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"desc\"/>\n" +
                "            <xs:element type=\"ors:authorType\" name=\"author\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:copyrightType\" name=\"copyright\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"time\"/>\n" +
                "            <xs:element type=\"ors:boundsType\" name=\"bounds\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:extensionsType\" name=\"extensions\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"boundsType\">\n" +
                "        <xs:simpleContent>\n" +
                "            <xs:extension base=\"xs:string\">\n" +
                "                <xs:attribute type=\"xs:string\" name=\"minLat\"/>\n" +
                "                <xs:attribute type=\"xs:string\" name=\"minLon\"/>\n" +
                "                <xs:attribute type=\"xs:string\" name=\"maxLat\"/>\n" +
                "                <xs:attribute type=\"xs:string\" name=\"maxLon\"/>\n" +
                "            </xs:extension>\n" +
                "        </xs:simpleContent>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"linkType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:string\" name=\"text\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"type\"/>\n" +
                "        </xs:sequence>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"href\"/>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"gpxType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"ors:metadataType\" name=\"metadata\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:rteType\" name=\"rte\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:extensionsType\" name=\"extensions\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "        </xs:sequence>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"version\"/>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"creator\"/>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"emailType\">\n" +
                "        <xs:simpleContent>\n" +
                "            <xs:extension base=\"xs:string\">\n" +
                "                <xs:attribute type=\"xs:string\" name=\"id\"/>\n" +
                "                <xs:attribute type=\"xs:string\" name=\"domain\"/>\n" +
                "            </xs:extension>\n" +
                "        </xs:simpleContent>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"authorType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:string\" name=\"name\"/>\n" +
                "            <xs:element type=\"ors:emailType\" name=\"email\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:linkType\" name=\"link\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"copyrightType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:string\" name=\"year\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"license\"/>\n" +
                "        </xs:sequence>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"author\"/>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"rteptType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"xs:decimal\" name=\"ele\" minOccurs=\"0\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"name\"/>\n" +
                "            <xs:element type=\"xs:string\" name=\"desc\"/>\n" +
                "            <xs:element type=\"ors:extensionsType\" name=\"extensions\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "        </xs:sequence>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"lat\" use=\"optional\"/>\n" +
                "        <xs:attribute type=\"xs:string\" name=\"lon\" use=\"optional\"/>\n" +
                "    </xs:complexType>\n" +
                "    <xs:complexType name=\"rteType\">\n" +
                "        <xs:sequence>\n" +
                "            <xs:element type=\"ors:rteptType\" name=\"rtept\" maxOccurs=\"unbounded\" minOccurs=\"0\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "            <xs:element type=\"ors:extensionsType\" name=\"extensions\" xmlns:ors=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\"/>\n" +
                "        </xs:sequence>\n" +
                "    </xs:complexType>\n" +
                "</xs:schema>\n";
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
                .body("info.containsKey('system_message')", is(true))

				.statusCode(200);
	}

	@Test
	public void expectCarToRejectProfileParams() {

		// options for cycling profiles
		JSONObject options = new JSONObject();
		JSONObject profileParams = new JSONObject();
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
				.statusCode(400);
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
                .body("routes[0].summary.distance", is(13079))
                .body("routes[0].summary.duration", is(2737))
                .body("routes[0].summary.ascent", is(351))
                .body("routes[0].summary.descent", is(347.6f))
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
                .body("routes[0].segments[0].distance", is(6696.6f))
                .body("routes[0].segments[0].duration", is(1398.4f))
                .body("routes[0].segments[1].distance", is(6382.4f))
                .body("routes[0].segments[1].duration", is(1338.6f))
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
                        is("gvqlHk`~s@cwUB?tC?Cp@NAdIAE`EQaCi@WiCaDcAuG?C]g@MaBVM_C`HCInAKcA~CAIjA?GhAGqAzDEsAh@Eu@a@CcA{@GqAuCAQeAC_A_DAOoAEcAaCEgAsB@Wu@E?q@KB]AYIEo@?AOBcAyGbBIiADIaA?EmBq@CyA]AaAHAa@HAgAeARCHHAHCqBp@BIHAy@VAURJQX@M\\?E\\?]\\Cm@\\ATR@RH?JFAd@d@K?`AAw@HDA?~HsArCF?VF@XF@XB@VBDVBBRFRz@HVz@FJv@LZv@JTr@BBt@D@p@B@p@RAl@HCl@RGl@PIcAvAu@{IPGeA~@e@uHVMkCFCkCJCkCRCkCZFkCNFkCDFkC\\ZiDBBiDJDiDPD{@JB{@J?{@R?{@PA{@b@CwB^Eq@L?H@?RB?RFBRBBRJ@R|BObG@?p@FAnAF?nAFFnA@FnALEnAFCnA@?\\HG\\BA\\NK?HC?LA?BG?FS??K?AG?@M?DI?DK?@K??[]?M]@K]BMSAgAg@@MS@IS?o@SC]HCIHDDHBHH`DVnAJ@Ht@XIlDtA{Oz@PmGx@R}D~A\\uD`HbBfCtBv@{Av@ZwAnGrAcJBB[B@]D@BHBNF@\\D?\\F@ZFJ\\BBXFEXROXBEXJIVNOVRSVHIVRORpAo@QNKSLKeAh@q@kCHIeABCeA~Ay@uMTa@mBVu@mDHe@oAH[oAJ[qERWoFJIeFTQ{EPM{ENU}D\\_A{JNo@_IF}@wRAoBwp@?]aM?YqMH{BkbAByCsoA?u@_b@@o@mL?[mL@GmL@GaKD]gHNc@{FT[qEVUqE@?qE@SkDFSgCx@{AqP`@cAoIx@eDyZZw@eRr@}Agh@V_Am[BKaMAI_L?E{J?[{JFaBq_@A[sHUsBen@CWsKAMgJ@a@gJH_@gJ@CgJBAgJJEsBJBQ`AjAqA\\J_@ZAo@|AUcLl@?H|ADcGz@ImVP@yFHJyF@TyFMf@cGWh@mNo@d@eKEH{C?NaC?BaC?@aCFLiBN@qAdAe@oBdBc@uMTFkC^b@wGBBiCFDmCTHkC\\E_DlBeB_b@PO_DPOaCLMWBI@NY^n@uApFhAgCfNLc@\\Fa@^BMUF]Sj@{CaTJe@}DVu@{Jb@_A{TRa@cGNUmD`@}@cJFUmBJa@qEF[_DHa@_D@QqC@IaCDaA}I@UmD?_A_I@{BgTD[kCHYkCHYeFPc@kHJe@kH@k@kH?EgE?CgE?MgEAIgEAQgEC[aFKe@sDS_@sDQUsDECsDECiDKG_DCAuCSIkCgG_CseAg@E_I{@F_NGAsCCIkCAC_COEgC]E_CgBFwMqAKqI[CoAy@KoFSEoAUEoAuC_A}]}DcAyd@aCO_O{ASaBA?SMASuAW_NsFu@obAIEkCKKkCZAkC@GcBBC{@vDiAoU\\OoCFCoCD?sCz@GkLhAQoPfCi@_NlGk@bJmGj@f@gCh@gBiAP}A{@FwAE?_@GB]]N_@wDhAzQCBjCAFjC[@jCi@BzGqAEhV{E_Aju@k@IbEgAC`JeAFbCcANAcAViAk@^_A[Za@c@b@mAIJk@EFREBRGFRCBRODRSERUYq@Mg@fEyAeK`}AGq@jCO{CpOS{BeGk@sEnf@k@uDx|@YkA~OGOzCSM~CK?nBIB~@IHPGJ]QXmAg@p@i@QNq@MLa@c@b@U_@f@h@MVj@IPp@s@pAxU_@j@~MGLnFEFnFg@j@nUKJzHGFdFs@j@lLk@v@jHKRbBMT`Ho@tA~\\_@lAxPa@fB~HW`B`H?JfE?DfE@DfEJFfED?fEFCR\\oAg@Vk@q@l@q@hIz@a@|N|@SxKn@B`Mr@XjWZPbGPRrGHNdH@FtHDVtHARtHU`AnUStA~\\Gb@~HIf@dKIb@dKQ~@dUMr@pOMr@zOObAzOYhBle@IlAbSAr@lLFbC`x@C~Ahg@Ex@|XO~@`YKd@bLEPbLERtKOx@rSKf@`HSv@bISf@HGPiCGPyCS^kDG@}DGIxF?AxFACxF?GxF?AxF@ArFb@uAbB@GeA?Ca@@m@?OoAjCEy@lG?i@fE?SfECw@w@CGyFEAoF??oFA@oFU\\oFKTrACFxDGL`HKT`Hm@rAlYEHrFEFzE]b@pOoCrBd~AEN~C?@~C?@~CBBjH@?jH@@jHj@CvQ@?jHTC`Cx@M`AD@a@@@k@?@w@BHiB?NuBEN_CKLjCi@`@vGo@VjCQF?IB?ID?GB?GD?s@`@nZuArAzaA_@^v[CBrDOP~HAD~HA?~Ha@bA~\\IZ~HG\\~HWlDpe@Kr@tCAJrDIh@rDIPrDE@rDJpEjM?d@p@?tAhB?rAdA?v@f@?n@I@`@I?HIiBHEB|CfA@tApB@x@nA@Lf@BXf@HbBvBP|BnCHv@fA@H^Fn@ZFn@B@J??B?D^?Fv@??F?FbA]?BS@RS?RSBnAQ@ZG?^I?RM@f@SBrASBb@HDtB{@F~BeA?V]BV]KDg@{C~@iBoHhBxm@K?`BSCxAGBnAO@hAUJdACB`AEBz@oIxAsHE@gAk@HsCG?gA[BaAU@_AG^{@CBw@qADiFqAFkEK?i@I@e@gA?mC{@ByAO?][@]o@Bg@iCHMO@HC?Hk@@Xm@Hd@ODR]VRgAlAnD_AfAfEURp@EDp@C?p@Q?p@OBRE@RqBn@xCA@RSHHOJ]ELg@CDg@gAb@_Dq@\\wBmAt@{@y@f@q@y@X{@eBt@XYJ?E@?_@LSmA`@Bc@NR{C`Av@_DfAf@uAf@{BMHYKJWG@WGCUINSCGSI?SKBQ"))
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
                .body("routes[0].way_points", hasItems(0, 332, 624))
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
                .body("routes[0].bbox", hasItems(8.678615f, 49.388405f, 8.719662f, 49.424603f))
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
                .body("routes[0].bbox", hasItems(8.678615f, 49.388405f, 8.719662f, 49.424603f))
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
                .body("routes[0].extras.surface.values.size()", is(28))
                .body("routes[0].extras.surface.values[18][1]", is(342))
                .body("routes[0].extras.suitability.values[18][0]", is(521))
                .body("routes[0].extras.steepness.values[10][1]", is(326))

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
				.param("preference", getParameter("preference"))
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
				.param("preference", getParameter("preference"))
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
				.param("preference", getParameter("preference"))
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
                .param("preference", getParameter("preference"))
                .param("profile", "driving-hgv")
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
                .body("routes[0].extras.tollways.values[2][1]", is(101))
                .body("routes[0].extras.tollways.values[2][2]", is(0))
				.statusCode(200);

		checkExtraConsistency(response);

		response = given()
				.param("coordinates", "8.676281,49.414715|8.6483,49.413291")
				.param("instructions", "true")
				.param("preference", getParameter("preference"))
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
				.body("routes[0].extras.tollways.values[2][1]", is(101))
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
				.param("preference", getParameter("preference"))
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
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", "cycling-road")
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
				.param("preference", getParameter("preference"))
				.param("geometry", "true")
				.param("profile", "cycling-road")
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
				.param("preference", getParameter("preference"))
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
                .body("routes[0].segments[0].steps.size()", is(34))
                .body("routes[0].segments[1].steps.size()", is(17))
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
                .body("routes[0].segments[0].steps.size()", is(34))
                .body("routes[0].segments[1].steps.size()", is(17))
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
				.body("routes[0].summary.duration", is(270))
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
				.body("routes[0].summary.duration", is(185.4f))
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
				.body("routes[0].summary.duration", is(184.2f))
				.statusCode(200);
	}

    @Test
    public void testHGVAxleLoadRestriction() {
        given()
                .param("coordinates", "8.686849,49.406093|8.687525,49.405437")
                .param("instructions", "false")
                .param("preference", "shortest")
                .param("profile", "driving-hgv")
                .param("options", "{\"profile_params\":{\"restrictions\":{\"axleload\":\"12.9\"}},\"vehicle_type\":\"hgv\"}")
                .param("units", "m")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(132.9f))
                .body("routes[0].summary.duration", is(44.3f))
                .statusCode(200);

        given()
                .param("coordinates", "8.686849,49.406093|8.687525,49.405437")
                .param("instructions", "true")
                .param("preference", "shortest")
                .param("profile", "driving-hgv")
                .param("options", "{\"profile_params\":{\"restrictions\":{\"axleload\":\"13.1\"}},\"vehicle_type\":\"hgv\"}")
                .param("units", "m")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(364.3f))
                .body("routes[0].summary.duration", is(92.7f))
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
                .param("optimized", false)
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
                .param("optimized", false)
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
                .body("routes[0].summary.distance", is(3172.4f))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-2 parameters
        given()
                .param("coordinates", "8.684682,49.401961|8.690518,49.405326")
                .param("instructions", "false")
                .param("preference", "shortest")
                .param("profile", getParameter("carProfile"))
                .param("options", "{\"avoid_countries\":\"AT|FR\"}")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.4f))
                .statusCode(200);

        // Test avoid_countries with ISO 3166-1 Alpha-3 parameters
        given()
                .param("coordinates", "8.684682,49.401961|8.690518,49.405326")
                .param("instructions", "false")
                .param("preference", "shortest")
                .param("profile", getParameter("carProfile"))
                .param("options", "{\"avoid_countries\":\"AUT|FRA\"}")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(3172.4f))
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
				.body("routes[0].segments[0].detourfactor", is(1.3f))
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then().log().ifValidationFails()
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then().log().ifValidationFails()
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
				.when().log().ifValidationFails()
				.get(getEndPointName())
				.then().log().ifValidationFails()
				.assertThat()
				.body("any { it.key == 'routes' }", is(true))
				.body("routes[0].summary.distance", is(591.7f))
				.body("routes[0].summary.duration", is(498.7f))
				.statusCode(200);

		given()
				.param("coordinates", "8.670290,49.418041|8.667490,49.418376")
				.param("preference", "shortest")
				.param("profile", "wheelchair")
				.param("options", "{\"profile_params\":{\"maximum_incline\":\"2\"}}")
				.when()
				.get(getEndPointName())
				.then().log().ifValidationFails()
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

    @Test
    public void testAccessRestrictionsWarnings() {
        given()
                .param("coordinates", "8.675154,49.407727|8.675863,49.407162")
                .param("preference", "shortest")
                .param("profile", getParameter("carProfile"))
                .when()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].containsKey('warnings')", is(true))
                .body("routes[0].warnings[0].code", is(1))
                .body("routes[0].containsKey('extras')", is(true))
                .body("routes[0].extras.containsKey('roadaccessrestrictions')", is(true))
                .body("routes[0].extras.roadaccessrestrictions.values[1][2]", is(32))
                .statusCode(200);
    }

    @Test
    public void testSimplifyHasLessWayPoints() {

        given()
                .param("coordinates", getParameter("coordinatesShort"))
                .param("profile", "driving-car")
                .param("format", "geojson")
                .when()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(75))
                .statusCode(200);

        given()
                .param("coordinates", getParameter("coordinatesShort"))
                .param("profile", "driving-car")
                .param("format", "geojson")
                .param("geometry_simplify", "true")
                .when()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("features[0].geometry.coordinates.size()", is(34))
                .statusCode(200);
	}

    @Test
    public void testAlternativeRoutes() {
        given()
                .param("coordinates", getParameter("coordinatesAR"))
                .param("instructions", "true")
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("carProfile"))
                .param("options", "{\"alternative_routes_count\": 2, \"alternative_routes_share_factor\": 0.5}")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(2))
                .body("routes[0].summary.distance", is(5942.2f))
                .body("routes[0].summary.duration", is(776.1f))
                .body("routes[1].summary.distance", is( 6435.1f))
                .body("routes[1].summary.duration", is(801.5f))
                .statusCode(200);

        given()
                .param("coordinates", getParameter("coordinatesAR"))
                .param("instructions", "true")
                .param("preference", getParameter("preference"))
                .param("profile", getParameter("carProfile"))
                .param("options", "{\"avoid_polygons\":{\"type\":\"Polygon\",\"coordinates\":[[[8.685873,49.414421], [8.688169,49.403978], [8.702095,49.407762], [8.695185,49.416013], [8.685873,49.414421]]]},\"alternative_routes_count\": 2}")
                .when().log().ifValidationFails()
                .get(getEndPointName())
                .then()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes.size()", is(1))
                .body("routes[0].summary.distance", is( 6435.1f))
                .body("routes[0].summary.duration", is(801.5f))
                .statusCode(200);
    }
}

