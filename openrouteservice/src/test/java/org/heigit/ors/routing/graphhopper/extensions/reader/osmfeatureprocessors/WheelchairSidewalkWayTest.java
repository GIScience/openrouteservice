package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WheelchairSidewalkWayTest {
    @Test
    void TestShowAsPedestrian() {
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(new ReaderWay(1));
        assertTrue(way.isPedestrianised());
    }

    @Test
    void TestInitiallyProcessedIfNoSidewalk() {
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(new ReaderWay(1));
        assertTrue(way.hasWayBeenFullyProcessed());
    }

    @Test
    void TestInitiallyNotProcessedIfSidewalk() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("sidewalk", "left");
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(readerWay);
        assertFalse(way.hasWayBeenFullyProcessed());
    }

    @Test
    void TestThatBothSidesGetProcessed() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("sidewalk", "both");
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(readerWay);

        int count = 0;

        while(!way.hasWayBeenFullyProcessed()) {
            count++;
            way.prepare();
        }

        assertEquals(2, count);
    }
}
