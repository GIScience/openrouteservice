package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.config.Profile;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarFlagEncoderTest {
    private final EncodingManager em = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS + "," + FlagEncoderNames.BIKE_ORS);
    private ReaderWay way;

    @BeforeEach
    void initWay() {
        way = new ReaderWay(1);
    }

    Weighting createWeighting(String vehicle, String weighting) {
        GraphHopperStorage g = new GraphBuilder(em).create();
        WeightingFactory weightingFactory = new ORSWeightingFactory(g, em);

        Profile profile = new Profile(vehicle + "_" + weighting).setVehicle(vehicle).setWeighting(weighting);

        return weightingFactory.createWeighting(profile, new PMap(), false);
    }

    @Test
    public void testDestinationTag() {
        IntsRef relFlags = em.createRelationFlags();

        Weighting carFastest = createWeighting(FlagEncoderNames.CAR_ORS, "fastest");
        Weighting bikeFastest = createWeighting(FlagEncoderNames.BIKE_ORS, "fastest");

        way.setTag("highway", "road");
        EncodingManager.AcceptWay acceptWay = new EncodingManager.AcceptWay();
        assertTrue(em.acceptWay(way, acceptWay));
        IntsRef edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(180, carFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);
        assertEquals(300, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);

        // no change for bike!
        way.setTag("motor_vehicle", "destination");
        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(1800, carFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);
        assertEquals(300, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);

        Weighting carShortest = createWeighting(FlagEncoderNames.CAR_ORS, "shortest");
        Weighting bikeShortest = createWeighting(FlagEncoderNames.BIKE_ORS, "shortest");

        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(10000, carShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);
        assertEquals(1000, bikeShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(1000, edgeFlags), false), 0.1);
    }
}
