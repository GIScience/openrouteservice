package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.KEY_ORS_SIDEWALK_SIDE;
import static org.junit.jupiter.api.Assertions.*;

class WaySurfaceTypeGraphStorageBuilderTest {
    private final static SurfaceType STREET_SURFACE = SurfaceType.ASPHALT;
    private final static SurfaceType SIDEWALK_SURFACE = SurfaceType.PAVING_STONE;
    private final static SurfaceType SIDEWALK_SURFACE_OTHER = SurfaceType.CONCRETE;

    private WaySurfaceTypeGraphStorageBuilder builder;

    @BeforeEach
    void reset() {
        builder = new WaySurfaceTypeGraphStorageBuilder();
    }

    @Test
    void TestWayWithNoSidewalksSpecified() {
        ReaderWay way = constructWay();
        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.STREET, waySurfaceDescription.getWayType());
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());
    }

    @ValueSource(strings = {"left", "right", "both", ""})
    @ParameterizedTest
    void TestWayWithSidewalkAttached(String side) {
        ReaderWay way = constructWay();
        attachSidewalk(way, side, "paving_stones");

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
            assertEquals(SIDEWALK_SURFACE, waySurfaceDescription.getSurfaceType());
        }
    }

    @Test
    void TestWayWithDifferentSidewalkSurfaces() {
        ReaderWay way = constructWay();
        way.setTag("sidewalk:left:surface", "paving_stones");
        way.setTag("sidewalk:right:surface", "concrete");

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

    private void attachSidewalk(ReaderWay way, String side, String surface) {
        if (side.isEmpty()) {
            way.setTag("sidewalk:surface", surface);
        } else {
            way.setTag("sidewalk", side);
            way.setTag("sidewalk:" + side + ":surface", surface);
        }
    }
}
