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

class HeavyVehicleFlagEncoderTest {
    private final EncodingManager em = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.HEAVYVEHICLE + "," + FlagEncoderNames.BIKE_ORS);
    private ReaderWay way;

    static final double WAY_DISTANCE = 1000;
    static final double HEAVYVEHICLE_DURATION = 180;
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

    private ReaderWay generateFerryWay() {
        way.getTags().put("route", "ferry");
        return way;
    }
    @Test
    void testDestinationTag() {
        IntsRef relFlags = em.createRelationFlags();

        Weighting hgvFastest = createWeighting(FlagEncoderNames.HEAVYVEHICLE, "fastest");
        Weighting bikeFastest = createWeighting(FlagEncoderNames.BIKE_ORS, "fastest");

        way.setTag("highway", "road");
        EncodingManager.AcceptWay acceptWay = new EncodingManager.AcceptWay();
        assertTrue(em.acceptWay(way, acceptWay));
        IntsRef edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(HEAVYVEHICLE_DURATION, hgvFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(BIKE_DURATION, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);

        way.setTag("motor_vehicle", "destination");
        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(HEAVYVEHICLE_DURATION * LimitedAccessWeighting.VEHICLE_DESTINATION_FACTOR, hgvFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(BIKE_DURATION * LimitedAccessWeighting.DEFAULT_DESTINATION_FACTOR, bikeFastest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);

        Weighting carShortest = createWeighting(FlagEncoderNames.HEAVYVEHICLE, "shortest");
        Weighting bikeShortest = createWeighting(FlagEncoderNames.BIKE_ORS, "shortest");

        edgeFlags = em.handleWayTags(way, acceptWay, relFlags);
        assertEquals(WAY_DISTANCE * LimitedAccessWeighting.VEHICLE_DESTINATION_FACTOR, carShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
        assertEquals(WAY_DISTANCE * LimitedAccessWeighting.DEFAULT_DESTINATION_FACTOR, bikeShortest.calcEdgeWeight(GHUtility.createMockedEdgeIteratorState(WAY_DISTANCE, edgeFlags), false), 0.1);
    }

    @Test
    void testFerryTag() {
        way = generateFerryWay();
        HeavyVehicleFlagEncoder flagEncoder = (HeavyVehicleFlagEncoder) em.getEncoder(FlagEncoderNames.HEAVYVEHICLE);
        // motor_vehicle = no -> reject
        way.getTags().put("motor_vehicle", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        // foot = * -> reject
        way.getTags().remove("motor_vehicle");
        way.getTags().put("foot", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.getTags().replace("foot", "yes");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        // only ferry flag -> accept
        way.getTags().remove("foot");
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

}