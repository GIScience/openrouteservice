package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.OSMReaderConfig;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersHierarchy;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ORSOSMReaderTest {
    private final ORSOSMReader osmReader;

    private final Coordinate[] coords1 = new Coordinate[]{
            new Coordinate(0, 0),
            new Coordinate(1, 0),
            new Coordinate(1, 1),
            new Coordinate(0, 1),
            new Coordinate(0, 0)
    };
    private final Coordinate[] coords2 = new Coordinate[]{
            new Coordinate(1, 0),
            new Coordinate(1, 1),
            new Coordinate(2, 1),
            new Coordinate(2, 0),
            new Coordinate(1, 0)
    };
    private final Coordinate[] coords3 = new Coordinate[]{
            new Coordinate(2, 0),
            new Coordinate(3, 0),
            new Coordinate(3, 1),
            new Coordinate(2, 1),
            new Coordinate(2, 0)
    };
    private final Coordinate[] coordsO1 = new Coordinate[]{
            new Coordinate(100, 100),
            new Coordinate(100, 102),
            new Coordinate(102, 102),
            new Coordinate(102, 100),
            new Coordinate(100, 100)
    };
    private final Coordinate[] coordsO2 = new Coordinate[]{
            new Coordinate(101, 101),
            new Coordinate(103, 101),
            new Coordinate(103, 103),
            new Coordinate(101, 103),
            new Coordinate(101, 101)
    };

    public ORSOSMReaderTest()  throws Exception {
        CountryBordersReader cbr = new CountryBordersReader();

        CountryBordersHierarchy h = new CountryBordersHierarchy();
        CountryBordersHierarchy h2 = new CountryBordersHierarchy();

        GeometryFactory gf = new GeometryFactory();
        h.add(new CountryBordersPolygon("c1", gf.createPolygon(coords1)));
        h.add(new CountryBordersPolygon("c2", gf.createPolygon(coords2)));
        h.add(new CountryBordersPolygon("c3", gf.createPolygon(coords3)));

        h2.add(new CountryBordersPolygon("cO1", gf.createPolygon(coordsO1)));
        h2.add(new CountryBordersPolygon("cO2", gf.createPolygon(coordsO2)));

        cbr.addHierarchy(1L, h);
        cbr.addHierarchy(2L, h2);

        EncodingManager em = EncodingManager.create("car");
        GraphHopperStorage graphStorage = new GraphBuilder(em).build();

        ProfileProperties profileProperties = new ProfileProperties();
        profileProperties.setEncoderName(EncoderNameEnum.DRIVING_CAR);

        GraphProcessContext gpc = new GraphProcessContext(profileProperties);
        gpc.setCountryBordersReader(cbr);
        osmReader = new ORSOSMReader(graphStorage, new OSMReaderConfig(), gpc);
    }

    @Test
    void testLookupCountriesAndSetWayTags(){
        ReaderWay rw = new ReaderWay(1);
        Coordinate[] cs = new Coordinate[]{
                new Coordinate(0.5, 0.5),
                new Coordinate(1.5, 0.5)
        };

        osmReader.lookupCountriesAndSetWayTags(rw, cs);

        assertEquals("c1", rw.getTag("country1"));
        assertEquals("c2", rw.getTag("country2"));

        ReaderWay rw2 = new ReaderWay(1);
        Coordinate[] cs2 = new Coordinate[]{
                new Coordinate(0.5, 0.5),
                new Coordinate(0.75, 0.5)
        };

        osmReader.lookupCountriesAndSetWayTags(rw2, cs2);

        assertEquals("c1", rw2.getTag("country1"));
        assertEquals("c1", rw2.getTag("country2"));

    }

    /**
     * Test that the builder detects that a linestring crosses a border
     */
    @Test
    void TestFindBorderCrossing() {
        String[] names = osmReader.findBorderCrossing(new Coordinate[]{
                new Coordinate(0.5, 0.5),
                new Coordinate(1.5, 0.5)
        });

        assertEquals(2, names.length);
        assertTrue(names[0].equals("c1") || names[1].equals("c1"));
        assertTrue(names[0].equals("c2") || names[1].equals("c2"));
    }

    @Test
    void TestOverlappingRegion() {
        // Overlapping and crossing border - should return the two countries
        String[] names = osmReader.findBorderCrossing(new Coordinate[]{
                new Coordinate(101.5, 101.5),
                new Coordinate(102.5, 101.5)
        });
        assertEquals(2, names.length);

        // Overlapping but not crossing - should return only the one country
        String[] names2 = osmReader.findBorderCrossing(new Coordinate[]{
                new Coordinate(101.5, 101.5),
                new Coordinate(101.75, 101.5)
        });
        assertEquals(1, names2.length);
    }


}