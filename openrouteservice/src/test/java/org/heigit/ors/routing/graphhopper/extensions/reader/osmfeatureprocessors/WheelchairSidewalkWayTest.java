package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WheelchairSidewalkWayTest {
    @Test
    public void TestShowAsPedestrian() {
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(new ReaderWay(1));
        assertTrue(way.isPedestrianised());
    }

    @Test
    public void TestInitiallyProcessedIfNoSidewalk() {
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(new ReaderWay(1));
        assertTrue(way.hasWayBeenFullyProcessed());
    }

    @Test
    public void TestInitiallyNotProcessedIfSidewalk() {
        ReaderWay readerWay = new ReaderWay(1);
        readerWay.setTag("sidewalk", "left");
        WheelchairSidewalkWay way = new WheelchairSidewalkWay(readerWay);
        assertFalse(way.hasWayBeenFullyProcessed());
    }

    @Test
    public void TestThatBothSidesGetProcessed() {
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
