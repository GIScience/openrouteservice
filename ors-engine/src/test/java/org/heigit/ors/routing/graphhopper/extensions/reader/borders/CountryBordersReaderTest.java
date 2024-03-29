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
package org.heigit.ors.routing.graphhopper.extensions.reader.borders;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.*;

class CountryBordersReaderTest {
    CountryBordersReader _oldReaderSingleton;
    CountryBordersReader _reader;

    CountryBordersHierarchy[] hierarchies;

    Coordinate[] country1Geom = new Coordinate[]{
            new Coordinate(0, 1),
            new Coordinate(1, 1),
            new Coordinate(1, 0),
            new Coordinate(0, 0),
            new Coordinate(0, 1)
    };
    Coordinate[] country2Geom = new Coordinate[]{
            new Coordinate(1, 2),
            new Coordinate(2, 2),
            new Coordinate(2, 1),
            new Coordinate(1, 1),
            new Coordinate(1, 2)
    };
    Coordinate[] country3Geom = new Coordinate[]{
            new Coordinate(-1, 0),
            new Coordinate(0, -1),
            new Coordinate(-1, -1),
            new Coordinate(-1, 0)
    };
    Coordinate[] country4Geom = new Coordinate[]{
            new Coordinate(-2, -1),
            new Coordinate(-1, -1),
            new Coordinate(-1, -2),
            new Coordinate(-2, -2),
            new Coordinate(-2, -1)
    };

    GeometryFactory gf = new GeometryFactory();

    @BeforeEach
    void prepareCountryBordersReader() {

        _oldReaderSingleton = CountryBordersReader.currentInstance;
        // Construct the objects
        _reader = new CountryBordersReader();
        hierarchies = new CountryBordersHierarchy[2];

        try {
            hierarchies[0] = new CountryBordersHierarchy();
            hierarchies[0].add(new CountryBordersPolygon("country1", gf.createPolygon(country1Geom)));
            hierarchies[0].add(new CountryBordersPolygon("country2", gf.createPolygon(country2Geom)));

            _reader.addHierarchy(1L, hierarchies[0]);

            hierarchies[1] = new CountryBordersHierarchy();
            hierarchies[1].add(new CountryBordersPolygon("country3", gf.createPolygon(country3Geom)));
            hierarchies[1].add(new CountryBordersPolygon("country4", gf.createPolygon(country4Geom)));

            _reader.addHierarchy(2L, hierarchies[1]);
        } catch (Exception e) {
            System.out.println(e);
        }

        _reader.addOpenBorder("country1", "country2");
        _reader.addId("1", "country1", "country1 English", "CT", "CTR");
    }

    @AfterEach
    void resetCountryBordersReader() {
        CountryBordersReader.currentInstance = _oldReaderSingleton;
    }

    /**
     * Test that correct countries are being returned that surround the given point
     */
    @Test
    void TestGetCountry() {
        Coordinate c = new Coordinate(0.5, 0.5);
        CountryBordersPolygon[] polys = _reader.getCountry(c);

        assertEquals(1, polys.length);
        assertEquals("country1", polys[0].getName());
    }

    /**
     * Test that correct candidate countries (based on bbox) are being returned that surround the given coordinate
     */
    @Test
    void TestGetCandidateCountry() {
        Coordinate c = new Coordinate(-0.25, -0.25);
        CountryBordersPolygon[] polys = _reader.getCandidateCountry(c);

        assertEquals(1, polys.length);
        assertEquals("country3", polys[0].getName());
    }

    /**
     * Test that the correct id is returned for a country of the given local name
     */
    @Test
    void TestGetCountryId() {
        assertEquals("1", _reader.getId("country1"));
    }

    /**
     * Test that the correct English name is returned for a country of the given local name
     */
    @Test
    void TestGetCountryEnglishName() {
        assertEquals("country1 English", _reader.getEngName("country1"));
    }

    /**
     * Test that borders are correctly identified as being open or not
     */
    @Test
    void TestGetOpenBorder() {
        assertTrue(_reader.isOpen("country1", "country2"));
        assertFalse(_reader.isOpen("country1", "country3"));
    }

    /**
     * Test that the correct id is returned for ISO codes
     */
    @Test
    void TestGetCountryIdByISOCode() {
        assertEquals(1, CountryBordersReader.getCountryIdByISOCode("CT"));
        assertEquals(1, CountryBordersReader.getCountryIdByISOCode("CTR"));
        assertEquals(0, CountryBordersReader.getCountryIdByISOCode("FOO"));
    }

}
