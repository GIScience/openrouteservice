/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package heigit.ors.globalResponseProcessor.gpx.beans;

import com.graphhopper.util.shapes.BBox;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

public class XMLBuilderTest {
    // Setup Gpx feature
    private static Gpx gpx = new Gpx();

    /**
     * This class initializes the dummy Gpx.class object
     */
    @BeforeClass
    public static void setUp() {
        // Time Element
        XMLGregorianCalendar cal = new XMLGregorianCalendarImpl();
        cal.setTime(0, 0, 0, 0);
        // template value
        BigDecimal bigDecimal = BigDecimal.valueOf(0.0);
        // Combination of classes
        // Route and Point test
        WptType wpt = new WptType();
        RteType rte = new RteType();
        // set route Extensions
        RteTypeExtensions rteTypeExtensions = new RteTypeExtensions();
        rteTypeExtensions.setAscent(0);
        rteTypeExtensions.setAvgSpeed(0);
        rteTypeExtensions.setDescent(0);
        rteTypeExtensions.setDistance(0);
        rteTypeExtensions.setDistanceActual(0);
        rteTypeExtensions.setDuration(0);
        rteTypeExtensions.setBBox(new BBox(0.0,0.0,0.0,0.0,0.0,0.0));
        // set point extensions
        WptTypeExtensions wptTypeExtensions = new WptTypeExtensions();
        wptTypeExtensions.setDistance(0);
        wptTypeExtensions.setDuration(0);
        wptTypeExtensions.setStep(0);
        wptTypeExtensions.setType(0);
        wpt.setExtensions(wptTypeExtensions);
        // set point
        wpt.setLat(bigDecimal);
        wpt.setLon(bigDecimal);
        wpt.setEle(bigDecimal);
        // set route
        rte.setExtensions(rteTypeExtensions);
        rte.getRtept().add(wpt);
        // add point directly to gpx
        gpx.getWpt().add(wpt);
        // add rte to gpx
        gpx.getRte().add(rte);
        //Track test
        TrksegType trkseq = new TrksegType();
        TrkType trkType = new TrkType();
        // set track extensions
        TrksegTypeExtensions trksegTypeExtensions = new TrksegTypeExtensions();
        TrkTypeExtensions trkTypeExtensions = new TrkTypeExtensions();
        trksegTypeExtensions.setExample1(0);
        trkseq.setExtensions(trksegTypeExtensions);
        trkTypeExtensions.setExample1(0);
        trkType.setExtensions(trkTypeExtensions);
        // set track
        trkseq.getTrkpt().add(wpt);
        trkType.getTrkseg().add(trkseq);
        gpx.getTrk().add(trkType);

        // Metadata test
        MetadataType metadataType = new MetadataType();
        // set metadata extensions
        MetadataTypeExtensions metadataTypeExtensions = new MetadataTypeExtensions();
        metadataTypeExtensions.setExample1(0.0);
        // set metadata
        metadataType.setExtensions(metadataTypeExtensions);
        PersonType personType = new PersonType();
        EmailType emailType = new EmailType();
        emailType.setDomain("@domain");
        emailType.setId("id");
        personType.setEmail(emailType);
        LinkType linkType = new LinkType();
        linkType.setHref("");
        linkType.setText("");
        linkType.setType("");
        personType.setLink(linkType);
        personType.setName("");
        metadataType.setAuthor(personType);
        CopyrightType copyrightType = new CopyrightType();
        copyrightType.setAuthor("");
        copyrightType.setLicense("");
        copyrightType.setYear(cal);
        metadataType.setCopyright(copyrightType);
        BoundsType boundsType = new BoundsType();
        boundsType.setMaxlat(bigDecimal);
        boundsType.setMaxlon(bigDecimal);
        boundsType.setMinlat(bigDecimal);
        boundsType.setMinlon(bigDecimal);
        metadataType.setBounds(boundsType);
        metadataType.setDesc("");
        metadataType.setKeywords("");
        metadataType.setName("");
        metadataType.setTime(cal);
        gpx.setMetadata(metadataType);
        // gpx extensions
        GpxExtensions gpxExtensions = new GpxExtensions();
        gpxExtensions.setAttribution("");
        gpxExtensions.setBuild_date("");
        gpxExtensions.setDistance_units("");
        gpxExtensions.setDuration_units("");
        gpxExtensions.setElevation("");
        gpxExtensions.setEngine("");
        gpxExtensions.setInstructions("");
        gpxExtensions.setLanguage("");
        gpxExtensions.setPreference("");
        gpxExtensions.setProfile("");
        gpx.setExtensions(gpxExtensions);
    }

    @Test
    public void testBuild() throws JAXBException {
        XMLBuilder xMLBuilder = new XMLBuilder();
        String result = xMLBuilder.build(gpx);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns2:gpx version=\"1.0\" xmlns:ns2=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v1/ors-gpx.xsd\">\n" +
                "    <ns2:metadata>\n" +
                "        <ns2:name></ns2:name>\n" +
                "        <ns2:desc></ns2:desc>\n" +
                "        <ns2:author>\n" +
                "            <ns2:name></ns2:name>\n" +
                "            <ns2:email id=\"id\" domain=\"@domain\"/>\n" +
                "            <ns2:link href=\"\">\n" +
                "                <ns2:text></ns2:text>\n" +
                "                <ns2:type></ns2:type>\n" +
                "            </ns2:link>\n" +
                "        </ns2:author>\n" +
                "        <ns2:copyright author=\"\">\n" +
                "            <ns2:year></ns2:year>\n" +
                "            <ns2:license></ns2:license>\n" +
                "        </ns2:copyright>\n" +
                "        <ns2:time></ns2:time>\n" +
                "        <ns2:keywords></ns2:keywords>\n" +
                "        <ns2:bounds minlat=\"0.0\" minlon=\"0.0\" maxlat=\"0.0\" maxlon=\"0.0\"/>\n" +
                "        <ns2:extensions>\n" +
                "            <ns2:example1>0.0</ns2:example1>\n" +
                "        </ns2:extensions>\n" +
                "    </ns2:metadata>\n" +
                "    <ns2:wpt lat=\"0.0\" lon=\"0.0\">\n" +
                "        <ns2:ele>0.0</ns2:ele>\n" +
                "        <ns2:extensions>\n" +
                "            <ns2:distance>0.0</ns2:distance>\n" +
                "            <ns2:duration>0.0</ns2:duration>\n" +
                "            <ns2:type>0</ns2:type>\n" +
                "            <ns2:step>0</ns2:step>\n" +
                "        </ns2:extensions>\n" +
                "    </ns2:wpt>\n" +
                "    <ns2:rte>\n" +
                "        <ns2:rtept lat=\"0.0\" lon=\"0.0\">\n" +
                "            <ns2:ele>0.0</ns2:ele>\n" +
                "            <ns2:extensions>\n" +
                "                <ns2:distance>0.0</ns2:distance>\n" +
                "                <ns2:duration>0.0</ns2:duration>\n" +
                "                <ns2:type>0</ns2:type>\n" +
                "                <ns2:step>0</ns2:step>\n" +
                "            </ns2:extensions>\n" +
                "        </ns2:rtept>\n" +
                "        <ns2:extensions>\n" +
                "            <ns2:distance>0.0</ns2:distance>\n" +
                "            <ns2:duration>0.0</ns2:duration>\n" +
                "            <ns2:distanceActual>0.0</ns2:distanceActual>\n" +
                "            <ns2:ascent>0.0</ns2:ascent>\n" +
                "            <ns2:descent>0.0</ns2:descent>\n" +
                "            <ns2:avgSpeed>0.0</ns2:avgSpeed>\n" +
                "            <ns2:BBox>\n" +
                "                <minLon>0.0</minLon>\n" +
                "                <maxLon>0.0</maxLon>\n" +
                "                <minLat>0.0</minLat>\n" +
                "                <maxLat>0.0</maxLat>\n" +
                "                <minEle>0.0</minEle>\n" +
                "                <maxEle>0.0</maxEle>\n" +
                "            </ns2:BBox>\n" +
                "        </ns2:extensions>\n" +
                "    </ns2:rte>\n" +
                "    <ns2:trk>\n" +
                "        <ns2:extensions>\n" +
                "            <ns2:example1>0.0</ns2:example1>\n" +
                "        </ns2:extensions>\n" +
                "        <ns2:trkseg>\n" +
                "            <ns2:trkpt lat=\"0.0\" lon=\"0.0\">\n" +
                "                <ns2:ele>0.0</ns2:ele>\n" +
                "                <ns2:extensions>\n" +
                "                    <ns2:distance>0.0</ns2:distance>\n" +
                "                    <ns2:duration>0.0</ns2:duration>\n" +
                "                    <ns2:type>0</ns2:type>\n" +
                "                    <ns2:step>0</ns2:step>\n" +
                "                </ns2:extensions>\n" +
                "            </ns2:trkpt>\n" +
                "            <ns2:extensions>\n" +
                "                <ns2:example1>0.0</ns2:example1>\n" +
                "            </ns2:extensions>\n" +
                "        </ns2:trkseg>\n" +
                "    </ns2:trk>\n" +
                "    <ns2:extensions>\n" +
                "        <ns2:attribution></ns2:attribution>\n" +
                "        <ns2:engine></ns2:engine>\n" +
                "        <ns2:build_date></ns2:build_date>\n" +
                "        <ns2:profile></ns2:profile>\n" +
                "        <ns2:preference></ns2:preference>\n" +
                "        <ns2:language></ns2:language>\n" +
                "        <ns2:distance-units></ns2:distance-units>\n" +
                "        <ns2:duration-units></ns2:duration-units>\n" +
                "        <ns2:instructions></ns2:instructions>\n" +
                "        <ns2:elevation></ns2:elevation>\n" +
                "    </ns2:extensions>\n" +
                "</ns2:gpx>\n", result);
    }
}