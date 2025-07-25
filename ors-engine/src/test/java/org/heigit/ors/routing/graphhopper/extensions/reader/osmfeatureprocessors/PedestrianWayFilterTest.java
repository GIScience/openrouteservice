package org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors;

import com.graphhopper.reader.ReaderWay;
import org.junit.jupiter.api.Test;

import java.io.InvalidObjectException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PedestrianWayFilterTest {
    PedestrianWayFilter filter;

    public PedestrianWayFilterTest() {
        filter = new PedestrianWayFilter();
    }

    @Test
    void TestPedestrianisedWaysAreAccepted() throws InvalidObjectException {
        ReaderWay way = createSidewalkedWay(1);

        filter.assignFeatureForFiltering(way);
        assertTrue(filter.accept());
    }

    private ReaderWay createSidewalkedWay(int id) {
        ReaderWay way = new ReaderWay(id);

        way.setTag("sidewalk", "left");

        return way;
    }
}
