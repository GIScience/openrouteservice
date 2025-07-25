package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedestrianSidewalkWayTest {
    @Test
    void TestShowAsPedestrian() {
        PedestrianSidewalkWay way = new PedestrianSidewalkWay(new ReaderWay(1));
        assertTrue(way.isPedestrianised());
    }

    @Test
    void TestInitiallyProcessedIfNoSidewalk() {
        PedestrianSidewalkWay way = new PedestrianSidewalkWay(new ReaderWay(1));
        assertTrue(way.hasWayBeenFullyProcessed());
    }

    @Test
    void TestInitiallyNotProcessedIfSidewalk() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("sidewalk", "left");
        PedestrianSidewalkWay way = new PedestrianSidewalkWay(readerWay);
        assertFalse(way.hasWayBeenFullyProcessed());
    }

    @Test
    void TestThatBothSidesGetProcessed() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("sidewalk", "both");
        PedestrianSidewalkWay way = new PedestrianSidewalkWay(readerWay);

        int count = 0;

        while (!way.hasWayBeenFullyProcessed()) {
            count++;
            way.prepare();
        }

        assertEquals(2, count);
    }
}
