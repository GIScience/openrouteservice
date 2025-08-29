package org.heigit.ors.routing.graphhopper.extensions.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.WaySurface;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WaySurfaceParserTest {
    private static final WaySurface STREET_SURFACE = WaySurface.ASPHALT;
    private static final WaySurface SIDEWALK_SURFACE = WaySurface.PAVING_STONES;
    private static final WaySurface SIDEWALK_SURFACE_OTHER = WaySurface.CONCRETE;

    private EncodingManager em;
    private EnumEncodedValue<WaySurface> surfaceEnc;
    private WaySurfaceParser parser;
    private IntsRef intsRef, relFlags;

    @BeforeEach
    void setUp() {
        parser = new WaySurfaceParser();
        em = new EncodingManager.Builder().add(parser).build();
        surfaceEnc = em.getEnumEncodedValue(WaySurface.KEY, WaySurface.class);
        relFlags = em.createRelationFlags();
        intsRef = em.createEdgeFlags();
    }

    @Test
    void testSimpleTags() {
        ReaderWay way = new ReaderWay(1);

        way.setTag("highway", "primary");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(WaySurface.UNKNOWN, surfaceEnc.getEnum(false, intsRef));

        way.setTag("surface", "paved");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(WaySurface.PAVED, surfaceEnc.getEnum(false, intsRef));

        way.setTag("surface", "earth");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(WaySurface.DIRT, surfaceEnc.getEnum(false, intsRef));
    }

    @Test
    void testWayWithNoSidewalk() {
        ReaderWay way = constructWay();
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(STREET_SURFACE, surfaceEnc.getEnum(false, intsRef));
    }

    @ParameterizedTest(name = "Side: {0},  set side: {1}, set surface: {2}")
    @CsvSource({
            "left,true,true", "right,true,true", "both,true,true",
            "left,true,false", "right,true,false", "both,true,false",
            "left,false,true", "right,false,true", "both,false,true", ",false,true"
    })
    void testWayWithSidewalk(String side, boolean setSide, boolean setSurface) {
        ReaderWay way = constructWay();
        side = side == null ? "" : side;
        String surface = setSurface ? SIDEWALK_SURFACE.name().toLowerCase() : "";
        attachSidewalk(way, side, surface, setSide);

        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(STREET_SURFACE, surfaceEnc.getEnum(false, intsRef));

        String[] sides = "both".equals(side) || side.isEmpty() ? new String[]{"left", "right"} : new String[]{side};
        for (String s : sides) {
            way.setTag(KEY_ORS_SIDEWALK_SIDE, s);
            parser.handleWayTags(intsRef, way, false, relFlags);
            assertEquals(setSurface ? SIDEWALK_SURFACE : WaySurface.UNKNOWN, surfaceEnc.getEnum(false, intsRef));
        }
    }

    @Test
    void testWayWithSidewalkSurfaceDifferentBetweenSides() {
        ReaderWay way = constructWay();
        attachSidewalk(way, "left", "paving_stones", false);
        attachSidewalk(way, "right", "concrete", false);

        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(STREET_SURFACE, surfaceEnc.getEnum(false, intsRef));

        way.setTag(KEY_ORS_SIDEWALK_SIDE, "left");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(SIDEWALK_SURFACE, surfaceEnc.getEnum(false, intsRef));

        way.setTag(KEY_ORS_SIDEWALK_SIDE, "right");
        parser.handleWayTags(intsRef, way, false, relFlags);
        assertEquals(SIDEWALK_SURFACE_OTHER, surfaceEnc.getEnum(false, intsRef));
    }

    private ReaderWay constructWay() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "residential");
        way.setTag("surface", "asphalt");
        return way;
    }

    private void attachSidewalk(ReaderWay way, String side, String surface, boolean setSide) {
        if (!side.isEmpty() && setSide) {
            way.setTag("sidewalk", side);
        }
        if (!surface.isEmpty()) {
            String tag = side.isEmpty() ? "sidewalk:surface" : "sidewalk:" + side + ":surface";
            way.setTag(tag, surface);
        }
    }
}