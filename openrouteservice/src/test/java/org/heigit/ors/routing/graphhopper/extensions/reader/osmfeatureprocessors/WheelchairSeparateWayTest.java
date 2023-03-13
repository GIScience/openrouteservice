package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WheelchairSeparateWayTest {
    WheelchairSeparateWay way;

    @BeforeEach
    void reset() {
        ReaderWay readerWay = new ReaderWay(1);
        way = new WheelchairSeparateWay(readerWay);
    }

    @Test
    void TestShowAsPedestrian() {
        assertTrue(way.isPedestrianised());
    }

    @Test
    void TestInitiallyNotProcessed() {
        assertFalse(way.hasWayBeenFullyProcessed());
    }

    @Test
    void TestMarkedAsProcessedOncePrepared() {
        way.prepare();
        assertTrue(way.hasWayBeenFullyProcessed());
    }
}
