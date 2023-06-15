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

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;

import static org.junit.jupiter.api.Assertions.*;

class CountryBordersPolygonTest {
    GeometryFactory gf = new GeometryFactory();

    CountryBordersPolygon cbp;
    Coordinate[] country1Geom = new Coordinate[] {
            new Coordinate(0,0),
            new Coordinate(-1,1),
            new Coordinate(1,2),
            new Coordinate(1,-1),
            new Coordinate(0,0)
    };

    public CountryBordersPolygonTest() {
        try {
            cbp = new CountryBordersPolygon("name", gf.createPolygon(country1Geom));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Test that the object successfully stores and returns the name given to the boundary object.
     */
    @Test
    void TestName() {
        assertEquals("name", cbp.getName());
    }

    /**
     * Test that the object correctly stores and returns geometry of the boundary
     */
    @Test
    void TestBoundaryGeometry() {
        MultiPolygon boundary = cbp.getBoundary();
        Coordinate[] cbpCoords = boundary.getCoordinates();
        assertEquals(country1Geom.length, cbpCoords.length);
        assertEquals(country1Geom[0].x, cbpCoords[0].x, 0.0);
        assertEquals(country1Geom[3].y, cbpCoords[3].y,0.0);
    }

    /**
     * Test that the object generates a corect bounding box for the contained geometry
     */
    @Test
    void TestBBox() {
        double[] bbox = cbp.getBBox();
        assertEquals(-1.0, bbox[0], 0.0);
        assertEquals(1.0, bbox[1], 0.0);
        assertEquals(-1.0, bbox[2], 0.0);
        assertEquals(2.0, bbox[3], 0.0);
    }

    /**
     * Test that the object correctly identifies if the given linestring crosses the boundary designated by the geometry
     */
    @Test
    void TestIntersection() {
        LineString ls = gf.createLineString(new Coordinate[] {
                new Coordinate(0.5, 0.5),
                new Coordinate(-10.5, -10.5)
        });

        assertTrue(cbp.crossesBoundary(ls));

        ls = gf.createLineString(new Coordinate[] {
                new Coordinate(0.5, 0.5),
                new Coordinate(0.25, 0.25)
        });

        assertFalse(cbp.crossesBoundary(ls));
    }

    /**
     * Test that the object determines correctly if the given coordinate is within the bounding box of its geometry
     */
    @Test
    void TestBBoxContains() {
        assertTrue(cbp.inBbox(new Coordinate(0.5, 0.5)));
        assertFalse(cbp.inBbox(new Coordinate(10.0, 0.5)));
    }

    /**
     * Test that the object detects if a coordinate is definitely within the polygon
     */
    @Test
    void TestPolygonContains() {
        assertTrue(cbp.inArea(new Coordinate(0.5, 0.5)));
        assertFalse(cbp.inArea(new Coordinate(-0.5, -0.5)));
    }
}
