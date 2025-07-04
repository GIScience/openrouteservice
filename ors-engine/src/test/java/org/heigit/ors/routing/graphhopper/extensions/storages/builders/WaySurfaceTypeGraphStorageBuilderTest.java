package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;
import static org.junit.jupiter.api.Assertions.*;

class WaySurfaceTypeGraphStorageBuilderTest {
    private static final SurfaceType STREET_SURFACE = SurfaceType.ASPHALT;
    private static final SurfaceType SIDEWALK_SURFACE = SurfaceType.PAVING_STONES;
    private static final SurfaceType SIDEWALK_SURFACE_OTHER = SurfaceType.CONCRETE;

    private WaySurfaceTypeGraphStorageBuilder builder;

    @BeforeEach
    void reset() {
        builder = new WaySurfaceTypeGraphStorageBuilder();
    }

    @Test
    void testWayWithNoSidewalk() {
        ReaderWay way = constructWay();
        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.STREET, waySurfaceDescription.getWayType());
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());
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

        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.STREET, waySurfaceDescription.getWayType());
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());

        builder.setUseSidewalks(true);
        builder.processWay(way);
        String[] sides = "both".equals(side) || side.isEmpty() ? new String[]{"left", "right"} : new String[]{side};

        for (String s : sides) {
            way.setTag(KEY_ORS_SIDEWALK_SIDE, s);
            waySurfaceDescription = builder.getStoredValue(way);
            assertEquals(WayType.FOOTWAY, waySurfaceDescription.getWayType());
            assertEquals(setSurface ? SIDEWALK_SURFACE : SurfaceType.UNKNOWN, waySurfaceDescription.getSurfaceType());
        }
    }

    @Test
    void testWayWithSidewalkSurfaceDifferentBetweenSides() {
        ReaderWay way = constructWay();
        attachSidewalk(way, "left", "paving_stones", false);
        attachSidewalk(way, "right", "concrete", false);

        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.STREET, waySurfaceDescription.getWayType());
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());

        builder.setUseSidewalks(true);
        builder.processWay(way);

        way.setTag(KEY_ORS_SIDEWALK_SIDE, "right");
        waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.FOOTWAY, waySurfaceDescription.getWayType());
        assertEquals(SIDEWALK_SURFACE_OTHER, waySurfaceDescription.getSurfaceType());

        way.setTag(KEY_ORS_SIDEWALK_SIDE, "left");
        waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.FOOTWAY, waySurfaceDescription.getWayType());
        assertEquals(SIDEWALK_SURFACE, waySurfaceDescription.getSurfaceType());
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
