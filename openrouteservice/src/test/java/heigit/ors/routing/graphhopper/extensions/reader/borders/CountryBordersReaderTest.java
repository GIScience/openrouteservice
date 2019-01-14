/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
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
package heigit.ors.routing.graphhopper.extensions.reader.borders;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountryBordersReaderTest {
    CountryBordersReader _reader;

    CountryBordersHierarchy[] hierarchies;

    Coordinate[] country1Geom = new Coordinate[] {
            new Coordinate(0,1),
            new Coordinate(1,1),
            new Coordinate(1,0),
            new Coordinate(0,0),
            new Coordinate(0,1)
    };
    Coordinate[] country2Geom = new Coordinate[] {
            new Coordinate(1,2),
            new Coordinate(2,2),
            new Coordinate(2,1),
            new Coordinate(1,1),
            new Coordinate(1,2)
    };
    Coordinate[] country3Geom = new Coordinate[] {
            new Coordinate(-1,0),
            new Coordinate(0,-1),
            new Coordinate(-1,-1),
            new Coordinate(-1,0)
    };
    Coordinate[] country4Geom = new Coordinate[] {
            new Coordinate(-2,-1),
            new Coordinate(-1,-1),
            new Coordinate(-1,-2),
            new Coordinate(-2,-2),
            new Coordinate(-2,-1)
    };

    GeometryFactory gf = new GeometryFactory();

    public CountryBordersReaderTest() {

        // Construct the objects
        _reader = new CountryBordersReader();
        hierarchies = new CountryBordersHierarchy[2];

        try {
            hierarchies[0] = new CountryBordersHierarchy(1);
            hierarchies[0].add(new CountryBordersPolygon("country1", gf.createPolygon(country1Geom),1));
            hierarchies[0].add(new CountryBordersPolygon("country2", gf.createPolygon(country2Geom),1));

            _reader.addHierarchy(1l, hierarchies[0]);

            hierarchies[1] = new CountryBordersHierarchy(2);
            hierarchies[1].add(new CountryBordersPolygon("country3", gf.createPolygon(country3Geom),2));
            hierarchies[1].add(new CountryBordersPolygon("country4", gf.createPolygon(country4Geom),2));

            _reader.addHierarchy(2l, hierarchies[1]);
        } catch (Exception e) {
            System.out.println(e);
        }

        _reader.addOpenBorder("country1", "country2");
        _reader.addId("1", "country1", "country1 English", "CT", "CTR");
    }

    /**
     * Test that correct countries are being returned that surround the given point
     */
    @Test
    public void TestGetCountry() {
        Coordinate c = new Coordinate(0.5, 0.5);
        CountryBordersPolygon[] polys = _reader.getCountry(c);

        assertEquals(1, polys.length);
        assertEquals("country1", polys[0].getName());
    }

    /**
     * Test that correct candidate countries (based on bbox) are being returned that surround the given coordinate
     */
    @Test
    public void TestGetCandidateCountry() {
        Coordinate c = new Coordinate(-0.25, -0.25);
        CountryBordersPolygon[] polys = _reader.getCandidateCountry(c);

        assertEquals(1, polys.length);
        assertEquals("country3", polys[0].getName());
    }

    /**
     * Test that the correct id is returned for a country of the given local name
     */
    @Test
    public void TestGetCountryId() {
        assertEquals("1", _reader.getId("country1"));
    }

    /**
     * Test that the correct English name is returned for a country of the given local name
     */
    @Test
    public void TestGetCountryEnglishName() {
        assertEquals("country1 English", _reader.getEngName("country1"));
    }

    /**
     * Test that borders are correctly identified as being open or not
     */
    @Test
    public void TestGetOpenBorder() {
        assertTrue(_reader.isOpen("country1", "country2"));
        assertFalse(_reader.isOpen("country1", "country3"));
    }

    /**
     * Test that the correct id is returned for ISO codes
     */
    @Test
    public void TestGetCountryIdByISOCode() {
        assertEquals(1, CountryBordersReader.getCountryIdByISOCode("CT"));
        assertEquals(1, CountryBordersReader.getCountryIdByISOCode("CTR"));
        assertEquals(0, CountryBordersReader.getCountryIdByISOCode("FOO"));
    }

}
