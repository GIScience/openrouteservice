package heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.Test;

import java.io.InvalidObjectException;

import static org.junit.Assert.assertTrue;

public class WheelchairWayFilterTest {
    WheelchairWayFilter filter;

    public WheelchairWayFilterTest() {
        filter = new WheelchairWayFilter();
    }

    @Test
    public void TestPedestrianisedWaysAreAccepted() {
        ReaderWay way = createSidewalkedWay(1);

        try {
            filter.assignFeatureForFiltering(way);

            assertTrue(filter.accept());


        } catch (InvalidObjectException ioe) {

        }
    }

    private ReaderWay createSidewalkedWay(int id) {
        ReaderWay way = new ReaderWay(id);

        way.setTag("sidewalk", "left");

        return way;
    }
}
