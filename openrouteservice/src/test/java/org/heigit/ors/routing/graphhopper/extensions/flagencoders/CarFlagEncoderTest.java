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
import org.heigit.ors.routing.graphhopper.extensions.weighting.LimitedAccessWeighting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarFlagEncoderTest {
    private final EncodingManager em = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS + "," + FlagEncoderNames.BIKE_ORS);
    private ReaderWay way;

    static final double WAY_DISTANCE = 1000;
    static final double CAR_DURATION = 180;
    static final double BIKE_DURATION = 300;

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
        assertEquals(CAR_DURATION, carFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(BIKE_DURATION, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);

        way.setTag("motor_vehicle", "destination");
        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(CAR_DURATION * LimitedAccessWeighting.VEHICLE_DESTINATION_FACTOR, carFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(BIKE_DURATION * LimitedAccessWeighting.DEFAULT_DESTINATION_FACTOR, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);

        Weighting carShortest = createWeighting(FlagEncoderNames.CAR_ORS, "shortest");
        Weighting bikeShortest = createWeighting(FlagEncoderNames.BIKE_ORS, "shortest");

        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(WAY_DISTANCE * LimitedAccessWeighting.VEHICLE_DESTINATION_FACTOR, carShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(WAY_DISTANCE * LimitedAccessWeighting.DEFAULT_DESTINATION_FACTOR, bikeShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
    }
}
