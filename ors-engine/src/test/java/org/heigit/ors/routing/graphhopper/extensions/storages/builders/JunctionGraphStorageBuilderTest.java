package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import org.heigit.ors.routing.graphhopper.extensions.JunctionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JunctionGraphStorageBuilderTest {
    private JunctionGraphStorageBuilder builder;

    public JunctionGraphStorageBuilderTest() {
        builder = new JunctionGraphStorageBuilder();
    }

    @BeforeEach
    void reset() {
        builder = new JunctionGraphStorageBuilder();
    }

    @Test
    void testProcessWayWithJunctionTagYes() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("junction", "yes");

        builder.processWay(way);

        assertEquals(JunctionType.CYCLING_CARGO, builder.getCurrentJunctionValue());
    }

    @Test
    void testProcessWayWithJunctionTagNo() {
        ReaderWay way = new ReaderWay(1);
        way.setTag("junction", "no");

        builder.processWay(way);

        assertEquals(JunctionType.NONE, builder.getCurrentJunctionValue());
    }
}
