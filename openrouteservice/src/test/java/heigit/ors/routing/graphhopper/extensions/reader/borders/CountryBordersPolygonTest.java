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
package heigit.ors.routing.graphhopper.extensions.reader.borders;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountryBordersPolygonTest {
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
            cbp = new CountryBordersPolygon("name", gf.createPolygon(country1Geom),-1);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Test that the object successfully stores and returns the name given to the boundary object.
     */
    @Test
    public void TestName() {
        assertEquals("name", cbp.getName());
    }

    /**
     * Test that the object correctly stores and returns geometry of the boundary
     */
    @Test
    public void TestBoundaryGeometry() {
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
    public void TestBBox() {
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
    public void TestIntersection() {
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
    public void TestBBoxContains() {
        assertTrue(cbp.inBbox(new Coordinate(0.5, 0.5)));
        assertFalse(cbp.inBbox(new Coordinate(10.0, 0.5)));
    }

    /**
     * Test that the object detects if a coordinate is definitely within the polygon
     */
    @Test
    public void TestPolygonContains() {
        assertTrue(cbp.inArea(new Coordinate(0.5, 0.5)));
        assertFalse(cbp.inArea(new Coordinate(-0.5, -0.5)));
    }
}
