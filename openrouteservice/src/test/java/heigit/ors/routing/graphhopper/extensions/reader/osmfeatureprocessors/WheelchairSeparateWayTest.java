package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WheelchairSeparateWayTest {
    WheelchairSeparateWay way;

    @Before
    public void reset() {
        ReaderWay readerWay = new ReaderWay(1);
        way = new WheelchairSeparateWay(readerWay);
    }

    @Test
    public void TestShowAsPedestrian() {
        assertTrue(way.isPedestrianised());
    }

    @Test
    public void TestInitiallyNotProcessed() {
        assertFalse(way.hasWayBeenFullyProcessed());
    }

    @Test
    public void TestMarkedAsProcessedOncePrepared() {
        way.prepare();
        assertTrue(way.hasWayBeenFullyProcessed());
    }
}
