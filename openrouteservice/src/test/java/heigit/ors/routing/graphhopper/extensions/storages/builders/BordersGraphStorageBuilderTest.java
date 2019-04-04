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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersHierarchy;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.junit.Assert;
import org.junit.Test;

public class BordersGraphStorageBuilderTest {
    private BordersGraphStorageBuilder _builder;

    private CountryBordersReader _cbr;

    private Coordinate[] coords1 = new Coordinate[] {
            new Coordinate(0,0),
            new Coordinate(1,0),
            new Coordinate(1,1),
            new Coordinate(0,1),
            new Coordinate(0,0)
    };
    private Coordinate[] coords2 = new Coordinate[] {
            new Coordinate(1,0),
            new Coordinate(1,1),
            new Coordinate(2,1),
            new Coordinate(2,0),
            new Coordinate(1,0)
    };
    private Coordinate[] coords3 = new Coordinate[] {
            new Coordinate(2,0),
            new Coordinate(3,0),
            new Coordinate(3,1),
            new Coordinate(2,1),
            new Coordinate(2,0)
    };
    private Coordinate[] coordsO1 = new Coordinate[] {
            new Coordinate(100,100),
            new Coordinate(100,102),
            new Coordinate(102,102),
            new Coordinate(102,100),
            new Coordinate(100,100)
    };
    private Coordinate[] coordsO2 = new Coordinate[] {
            new Coordinate(101,101),
            new Coordinate(103,101),
            new Coordinate(103,103),
            new Coordinate(101,103),
            new Coordinate(101,101)
    };

    private GeometryFactory gf = new GeometryFactory();

    public BordersGraphStorageBuilderTest() {
        _builder= new BordersGraphStorageBuilder();
        _cbr = new CountryBordersReader();

        CountryBordersHierarchy h = new CountryBordersHierarchy(1);
        CountryBordersHierarchy h2 = new CountryBordersHierarchy(2);
        try {
            h.add(new CountryBordersPolygon("c1", gf.createPolygon(coords1), 1));
            h.add(new CountryBordersPolygon("c2", gf.createPolygon(coords2),1));
            h.add(new CountryBordersPolygon("c3", gf.createPolygon(coords3),1));

            h2.add(new CountryBordersPolygon("cO1", gf.createPolygon(coordsO1),2));
            h2.add(new CountryBordersPolygon("cO2", gf.createPolygon(coordsO2),2));
        } catch (Exception e) {

        }

        _cbr.addHierarchy(1l, h);
        _cbr.addHierarchy(2l, h2);
        _builder.setBordersBuilder(_cbr);

        try {
            _builder.init(null);
        } catch (Exception e) {

        }
    }

    /**
     * Test that the builder successfully adds country information to a way that crosses a border
     */
    @Test
    public void TestProcessWay() {
        ReaderWay rw = new ReaderWay(1);
        Coordinate[] cs = new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(1.5,0.5)
        };

        _builder.processWay(rw, cs, null);

        Assert.assertEquals("c1", rw.getTag("country1"));
        Assert.assertEquals("c2", rw.getTag("country2"));

        ReaderWay rw2 = new ReaderWay(1);
        Coordinate[] cs2 = new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(0.75,0.5)
        };

        _builder.processWay(rw2, cs2, null);

        Assert.assertFalse(rw2.hasTag("country1"));
        Assert.assertFalse(rw2.hasTag("country2"));
    }

    /**
     * Test that the builder detects that a linestring crosses a border
     */
    @Test
    public void TestFindBorderCrossing() {
        String[] names = _builder.findBorderCrossing(new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(1.5,0.5)
        });

        Assert.assertEquals(2, names.length);
        Assert.assertTrue(names[0].equals("c1") || names[1].equals("c1"));
        Assert.assertTrue(names[0].equals("c2") || names[1].equals("c2"));
    }

    @Test
    public void TestOverlappingRegion() {
        // Overlapping and crossing border - should return the two countries
        String[] names = _builder.findBorderCrossing(new Coordinate[] {
                new Coordinate(101.5,101.5),
                new Coordinate(102.5,101.5)
        });
        Assert.assertEquals(2, names.length);

        // Overlapping but not crossing - should return only the one country
        String[] names2 = _builder.findBorderCrossing(new Coordinate[] {
                new Coordinate(101.5,101.5),
                new Coordinate(101.75,101.5)
        });
        Assert.assertEquals(1, names2.length);
    }
}
