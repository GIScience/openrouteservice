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

public class BordersGraphStorageTest {
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

    private GeometryFactory gf = new GeometryFactory();

    public BordersGraphStorageTest() {
        _builder= new BordersGraphStorageBuilder();
        _cbr = new CountryBordersReader();

        CountryBordersHierarchy h = new CountryBordersHierarchy(1);
        try {
            h.add(new CountryBordersPolygon("c1", gf.createPolygon(coords1)));
            h.add(new CountryBordersPolygon("c2", gf.createPolygon(coords2)));
            h.add(new CountryBordersPolygon("c3", gf.createPolygon(coords3)));
        } catch (Exception e) {

        }

        _cbr.addHierarchy(1l, h);
        _builder.setBordersBuilder(_cbr);
    }

    /**
     * Test that the builder successfully adds country information to a way that crosses a border
     */
    @Test
    public void TestProcessWay() {
        ReaderWay rw = new ReaderWay(1);
        LineString ls = gf.createLineString(new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(1.5,0.5)
        });

        _builder.processWay(rw, ls);

        Assert.assertEquals("c1", rw.getTag("country1"));
        Assert.assertEquals("c2", rw.getTag("country2"));

        ReaderWay rw2 = new ReaderWay(1);
        LineString ls2 = gf.createLineString(new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(0.75,0.5)
        });

        _builder.processWay(rw2, ls2);

        Assert.assertFalse(rw2.hasTag("country1"));
        Assert.assertFalse(rw2.hasTag("country2"));
    }

    /**
     * Test that the builder detects that a linestring crosses a border
     */
    @Test
    public void TestFindBorderCrossing() {
        String[] names = _builder.findBorderCrossing(gf.createLineString(new Coordinate[] {
                new Coordinate(0.5,0.5),
                new Coordinate(1.5,0.5)
        }));

        Assert.assertEquals(2, names.length);
        Assert.assertTrue(names[0].equals("c1") || names[1].equals("c1"));
        Assert.assertTrue(names[0].equals("c2") || names[1].equals("c2"));
    }
}
