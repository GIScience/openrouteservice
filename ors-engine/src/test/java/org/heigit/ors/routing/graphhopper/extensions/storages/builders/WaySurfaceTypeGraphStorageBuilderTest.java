package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class WaySurfaceTypeGraphStorageBuilderTest {
    private final static SurfaceType STREET_SURFACE = SurfaceType.ASPHALT;
    private final static SurfaceType SIDEWALK_SURFACE = SurfaceType.PAVING_STONE;

    private WaySurfaceTypeGraphStorageBuilder builder;

    @BeforeEach
    void reset() {
        builder = new WaySurfaceTypeGraphStorageBuilder();
    }

    @Test
    void TestProcessSeparateWay() {
        ReaderWay way = constructWay("");
        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(WayType.STREET, waySurfaceDescription.getWayType());
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());
    }

    @ValueSource(strings = {"left", "right", "both"})
    @ParameterizedTest
    void TestProcessWayWithSidewalkAttached(String side) {
        ReaderWay way = constructWay(side);

        builder.processWay(way);
        var waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(STREET_SURFACE, waySurfaceDescription.getSurfaceType());

        builder.setUseSidewalks(true);
        builder.processWay(way);
        waySurfaceDescription = builder.getStoredValue(way);
        assertEquals(SIDEWALK_SURFACE, waySurfaceDescription.getSurfaceType());
    }

    private ReaderWay constructWay(String side) {
        ReaderWay way = new ReaderWay(1);
        way.setTag("highway", "residential");
        way.setTag("surface", "asphalt");

        if (!side.isEmpty()) {
            way.setTag("sidewalk", side);
            way.setTag("sidewalk:" + side + ":surface", "paving_stones");
            way.setTag("ors-sidewalk-side", side.equals("left") ? side : "right");
        }

        return way;
    }
}
