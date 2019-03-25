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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

public class XMLBuilderTest {
    // Setup Gpx feature
    private static Gpx gpx = new Gpx();

    /**
     * This class initializes the dummy Gpx.class object
     */
    @BeforeClass
    public static void setUp() throws DatatypeConfigurationException {
        // Time Element
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
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
        BoundsType boundsType = new BoundsType();
        boundsType.setMaxlat(bigDecimal);
        boundsType.setMaxlon(bigDecimal);
        boundsType.setMinlat(bigDecimal);
        boundsType.setMinlon(bigDecimal);
        rteTypeExtensions.setBounds(boundsType);
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
        BoundsType boundsType1 = new BoundsType();
        boundsType.setMaxlat(bigDecimal);
        boundsType.setMaxlon(bigDecimal);
        boundsType.setMinlat(bigDecimal);
        boundsType.setMinlon(bigDecimal);
        metadataType.setBounds(boundsType1);
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
                "<gpx version=\"1.0\" xmlns=\"https://raw.githubusercontent.com/GIScience/openrouteservice-schema/master/gpx/v2/ors-gpx.xsd\">\n" +
                "    <metadata>\n" +
                "        <name></name>\n" +
                "        <desc></desc>\n" +
                "        <author>\n" +
                "            <name></name>\n" +
                "            <email id=\"id\" domain=\"@domain\"/>\n" +
                "            <link href=\"\">\n" +
                "                <text></text>\n" +
                "                <type></type>\n" +
                "            </link>\n" +
                "        </author>\n" +
                "        <copyright author=\"\">\n" +
                "            <year></year>\n" +
                "            <license></license>\n" +
                "        </copyright>\n" +
                "        <time></time>\n" +
                "        <keywords></keywords>\n" +
                "        <bounds/>\n" +
                "        <extensions>\n" +
                "            <example1>0.0</example1>\n" +
                "        </extensions>\n" +
                "    </metadata>\n" +
                "    <wpt lat=\"0.0\" lon=\"0.0\">\n" +
                "        <ele>0.0</ele>\n" +
                "        <extensions>\n" +
                "            <distance>0.0</distance>\n" +
                "            <duration>0.0</duration>\n" +
                "            <type>0</type>\n" +
                "            <step>0</step>\n" +
                "        </extensions>\n" +
                "    </wpt>\n" +
                "    <rte>\n" +
                "        <rtept lat=\"0.0\" lon=\"0.0\">\n" +
                "            <ele>0.0</ele>\n" +
                "            <extensions>\n" +
                "                <distance>0.0</distance>\n" +
                "                <duration>0.0</duration>\n" +
                "                <type>0</type>\n" +
                "                <step>0</step>\n" +
                "            </extensions>\n" +
                "        </rtept>\n" +
                "        <extensions>\n" +
                "            <distance>0.0</distance>\n" +
                "            <duration>0.0</duration>\n" +
                "            <distanceActual>0.0</distanceActual>\n" +
                "            <ascent>0.0</ascent>\n" +
                "            <descent>0.0</descent>\n" +
                "            <avgSpeed>0.0</avgSpeed>\n" +
                "            <bounds minlat=\"0.0\" minlon=\"0.0\" maxlat=\"0.0\" maxlon=\"0.0\"/>\n" +
                "        </extensions>\n" +
                "    </rte>\n" +
                "    <trk>\n" +
                "        <extensions>\n" +
                "            <example1>0.0</example1>\n" +
                "        </extensions>\n" +
                "        <trkseg>\n" +
                "            <trkpt lat=\"0.0\" lon=\"0.0\">\n" +
                "                <ele>0.0</ele>\n" +
                "                <extensions>\n" +
                "                    <distance>0.0</distance>\n" +
                "                    <duration>0.0</duration>\n" +
                "                    <type>0</type>\n" +
                "                    <step>0</step>\n" +
                "                </extensions>\n" +
                "            </trkpt>\n" +
                "            <extensions>\n" +
                "                <example1>0.0</example1>\n" +
                "            </extensions>\n" +
                "        </trkseg>\n" +
                "    </trk>\n" +
                "    <extensions>\n" +
                "        <attribution></attribution>\n" +
                "        <engine></engine>\n" +
                "        <build_date></build_date>\n" +
                "        <profile></profile>\n" +
                "        <preference></preference>\n" +
                "        <language></language>\n" +
                "        <distance-units></distance-units>\n" +
                "        <duration-units></duration-units>\n" +
                "        <instructions></instructions>\n" +
                "        <elevation></elevation>\n" +
                "    </extensions>\n" +
                "</gpx>\n", result);
    }
}
