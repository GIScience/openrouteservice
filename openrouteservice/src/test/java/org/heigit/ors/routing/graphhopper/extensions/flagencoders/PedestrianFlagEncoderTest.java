/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class PedestrianFlagEncoderTest {
    private final EncodingManager encodingManager = EncodingManager.create(
            new ORSDefaultFlagEncoderFactory(),
            FlagEncoderNames.PEDESTRIAN_ORS + "|conditional_access=true", // Added conditional access for time restriction testing
            4
    );
    private final PedestrianFlagEncoder flagEncoder;
    private final BooleanEncodedValue roundaboutEnc = encodingManager.getBooleanEncodedValue("roundabout");
    private ReaderWay way;
    // TODO: Refactor the design of this test class to make more sense. Currently, the member variable 'way' is
    // TODO: modified in methods like 'generatePedestrianWay' or 'generateFerryWay', but also returned by this methods
    // TODO: only to re-assign the return value to the member variable at the call sites of the methods. This is quite
    // TODO: confusing and a potential source of subtle bugs.

    public PedestrianFlagEncoderTest() {
        flagEncoder = (PedestrianFlagEncoder)encodingManager.getEncoder(FlagEncoderNames.PEDESTRIAN_ORS);
    }

    @BeforeEach
    void initWay() {
        way = new ReaderWay(1);
    }

    private ReaderWay generatePedestrianWay() {
        way.setTag("highway", "path");
        return way;
    }

    private ReaderWay generateFerryWay() {
        way.setTag("route", "ferry");
        way.setTag("estimated_distance", 20000);
        way.setTag("duration:seconds", "1800");
        return way;
    }

    @ParameterizedTest
    @CsvSource({
            "sac_scale, alpine_hiking",
            "motorroad, yes",
            "ford, yes",
    })
    void rejectDifficultSacScale(String name, String value) {
        way = generatePedestrianWay();
        way.setTag(name, value);

        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void noTurnRestrictions() {
        assertFalse(flagEncoder.isTurnRestricted(1));
    }

    @Test
    void noTurnCost() {
        assertEquals(0, flagEncoder.getTurnCost(1), 0.0);
    }

    @Test
    void allwaysNoTurnFlags() {
        assertEquals(0.0, flagEncoder.getTurnFlags(false, 1.0), 0.0);
    }

    @Test
    void handleRelationTags() {
        ReaderRelation rel = new ReaderRelation(1);

        rel.setTag("route", "ferry");
        // TODO GH0.10: assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handleRelationTags(rel, 0));
        assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handleRelationTags(0, rel));
    }

    @Test
    void testRejectWay() {
        // TODO GH0.10: assertEquals(0, flagEncoder.handleWayTags(way, 0, 0));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testFerryFlags() {
        way = generateFerryWay();
        // TODO GH0.10: assertEquals(635, flagEncoder.handleWayTags(way, 3, 0));
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way,
                EncodingManager.Access.FERRY, 0);
        assertEquals(20, flagEncoder.getSpeed(flags), 0.01); // TODO should use AbstractFlagEncoder.SHORT_TRIP_FERRY_SPEED
    }

    @Test
    void testPlatformFlags() {
        way.setTag("railway", "platform");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("railway", "track");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testPierFlags() {
        way.setTag("man_made", "pier");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("man_made", "not_a_pier");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testHikingFlags() {
        way = generatePedestrianWay();
        way.setTag("sac_scale", "hiking");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("highway", "living_street");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    void testDesignatedFootwayPriority() {
        way.setTag("highway", "secondary");
        // TODO GH0.10: assertEquals(299, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.REACH_DEST.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("foot", "designated");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    void testAvoidWaysWithoutSidewalks() {
        way.setTag("highway", "primary");
        // TODO GH0.10: assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.AVOID_AT_ALL_COSTS.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "both");
        // TODO GH0.10: assertEquals(555, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.UNCHANGED.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "none");
        // TODO GH0.10: assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.AVOID_AT_ALL_COSTS.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    void testAcceptWayFerry() {
        way = generateFerryWay();
        // TODO GH0.10: assertEquals(3, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

    @Test
    void testAcceptFootway() {
        way = generatePedestrianWay();
        way.setTag("foot", "yes");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "designated");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "official");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "permissive");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void testRejectRestrictedFootway() {
        way = generatePedestrianWay();
        way.setTag("foot", "no");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "private");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "restricted");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "military");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "emergency");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.removeTag("foot");
        way.setTag("access", "no");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "private");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "restricted");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "military");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "emergency");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testAcceptRestrictedWayAllowedForFoot() {
        way = generatePedestrianWay();
        way.setTag("access", "no");
        way.setTag("foot", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "designated");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "official");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "permissive");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void testAccessOfBridleways() {
        way.setTag("highway", "bridleway");
        // we shouldn't route over bridleways…
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.setTag("foot", "yes");
        // …unless we're explicitly allowed to
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "designated");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "official");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("foot", "permissive");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void testAcceptSidewalks() {
        way.setTag("highway", "secondary");
        way.setTag("sidewalk", "both");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "left");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "right");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "yes");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void testRejectMotorways() {
        way.setTag("highway", "motorway");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("highway", "motorway_link");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testTunnelValues() {
        TreeMap<Double, Integer> priorityMap = new TreeMap<>();
        way.setTag("highway", "residential");
        way.setTag("tunnel", "yes");
        way.setTag("sidewalk", "no");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.AVOID_IF_POSSIBLE.getValue(), priorityMap.lastEntry().getValue());

        way.setTag("sidewalk", "both");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.UNCHANGED.getValue(), priorityMap.lastEntry().getValue());
    }

    @Test
    void testBicyclePathPriority() {
        way.setTag("highway", "path");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "official");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "designated");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "permissive");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Disabled // TODO: What is this test meant to test?
    @Test
    @SuppressWarnings("java:S1607")
    void testSpeed() {
        // TODO GH0.10: assertEquals(5.0, flagEncoder.getSpeed(683), 0.0);
        // TODO GH0.10: assertEquals(20.0, flagEncoder.getSpeed(635), 0.0);
        fail("TODO: find out how to test this.");
    }

    @Test
    void testSupports() {
        assertTrue(flagEncoder.supports(PriorityWeighting.class));
        assertFalse(flagEncoder.supports(TurnWeighting.class));
    }

    @Disabled // TODO: What is this test meant to test?
    @Test
    @SuppressWarnings("java:S1607")
    void getWeighting() {
        fail("TODO: find out how to test this.");
// TODO GH0.10:
//        assertEquals(0.714, flagEncoder.getDouble(683, FlagEncoderKeys.PRIORITY_KEY), 0.001);
//        boolean throwsError = false;
//        try {
//            // Only priority weighting allowed
//            flagEncoder.getDouble(683, 1);
//        } catch (UnsupportedOperationException e) {
//            throwsError = true;
//        }
//
//        assertTrue(throwsError);
    }

    /**
     * Test the routing of pedestrian ways with time restrictions.
     * An encoding manager with conditional access activated must be used.
     */
    @Test
    void testHighwayConditionallyOpen() {
        assertTrue(encodingManager.hasConditionalAccess());

        way = generatePedestrianWay();
        way.setTag("access", "no");
        way.setTag("access:conditional", "yes @ (15:00-19:30)");

        assertTrue(flagEncoder.getAccess(way).isConditional());
    }

    @Test
    void testHighwayConditionallyClosed() {
        assertTrue(encodingManager.hasConditionalAccess());

        way = generatePedestrianWay();
        way.setTag("access:conditional", "no @ (15:00-19:30)");

        assertTrue(flagEncoder.getAccess(way).isConditional());
    }

    @Test
    void testNonHighwayConditionallyOpen() {
        assertTrue(encodingManager.hasConditionalAccess());

        way.setTag("railway", "platform");
        way.setTag("access", "no");
        way.setTag("access:conditional", "yes @ (5:00-23:30)");

        assertTrue(flagEncoder.getAccess(way).isConditional());
    }

    @Test
    void testNonHighwayConditionallyClosed() {
        assertTrue(encodingManager.hasConditionalAccess());

        way.setTag("railway", "platform");
        way.setTag("access:conditional", "no @ (5:00-23:30)");

        assertTrue(flagEncoder.getAccess(way).isConditional());
    }

    // End of time restriction testing

    @Test
    void acceptLockGateFootAllowed() {
        way.setTag("waterway", "lock_gate");
        way.setTag("foot", "yes");

        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void rejectLockGateFootAccessMissing() {
        way.setTag("waterway", "lock_gate");

        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void rejectLockGateFootForbidden() {
        way.setTag("waterway", "lock_gate");
        way.setTag("foot", "no");

        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

}
