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

package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

public class PedestrianFlagEncoderTest {
    private PedestrianFlagEncoder flagEncoder;
    private ReaderWay way;

    public PedestrianFlagEncoderTest() {
        flagEncoder = (PedestrianFlagEncoder)EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.PEDESTRIAN_ORS, 4).getEncoder(FlagEncoderNames.PEDESTRIAN_ORS);
    }

    @Before
    public void initWay() {
        way = new ReaderWay(1);
    }

    private ReaderWay generatePedestrianWay() {
        way.getTags().put("highway", "path");
        return way;
    }

    private ReaderWay generateFerryWay() {
        way.getTags().put("route", "ferry");
        way.getTags().put("estimated_distance", 20000);
        way.getTags().put("duration:seconds", "1800");
        return way;
    }

    @Test
    public void rejectDifficultSacScale() {
        way = generatePedestrianWay();
        way.getTags().put("sac_scale", "alpine_hiking");

        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void noTurnRestrictions() {
        assertFalse(flagEncoder.isTurnRestricted(1));
    }

    @Test
    public void noTurnCost() {
        assertEquals(0, flagEncoder.getTurnCost(1), 0.0);
    }

    @Test
    public void allwaysNoTurnFlags() {
        assertEquals(0.0, flagEncoder.getTurnFlags(false, 1.0), 0.0);
    }

    @Test
    public void handleRelationTags() {
        ReaderRelation rel = new ReaderRelation(1);

        rel.getTags().put("route", "ferry");
        // TODO GH0.10: assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handleRelationTags(rel, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testRejectWay() {
        // TODO GH0.10: assertEquals(0, flagEncoder.handleWayTags(way, 0, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testFerryFlags() {
        way = generateFerryWay();
        // TODO GH0.10: assertEquals(635, flagEncoder.handleWayTags(way, 3, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testPlatformFlags() {
        way.getTags().put("railway", "platform");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.getTags().put("railway", "track");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testPierFlags() {
        way.getTags().put("man_made", "pier");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.getTags().put("man_made", "not_a_pier");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testHikingFlags() {
        way = generatePedestrianWay();
        way.getTags().put("sac_scale", "hiking");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");

        way.getTags().put("highway", "living_street");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testDesignatedFootwayPriority() {
        way.getTags().put("highway", "secondary");
        // TODO GH0.10: assertEquals(299, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");

        way.getTags().put("foot", "designated");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testAvoidWaysWithoutSidewalks() {
        way.getTags().put("highway", "primary");
        // TODO GH0.10: assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("sidewalk", "both");
        // TODO GH0.10: assertEquals(555, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("sidewalk", "none");
        // TODO GH0.10: assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testAcceptWayFerry() {
        way = generateFerryWay();
        // TODO GH0.10: assertEquals(3, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

    @Test
    public void testAcceptFootway() {
        way = generatePedestrianWay();
        way.getTags().put("foot", "yes");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "designated");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "official");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "permissive");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void testRejectRestrictedFootway() {
        way = generatePedestrianWay();
        way.getTags().put("foot", "no");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "private");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "restricted");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "military");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "emergency");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.removeTag("foot");
        way.getTags().put("access", "no");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "private");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "restricted");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "military");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "emergency");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testAcceptSidewalks() {
        way.getTags().put("highway", "secondary");
        way.getTags().put("sidewalk", "both");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "left");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "right");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "yes");
        // TODO GH0.10: assertEquals(1, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void testRejectMotorways() {
        way.getTags().put("highway", "motorway");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("highway", "motorway_link");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testRejectMotorRoad() {
        way = generatePedestrianWay();
        way.getTags().put("motorroad", "yes");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testDefaultFords() {
        way = generatePedestrianWay();
        way.getTags().put("ford", "yes");
        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testTunnelValues() {
        TreeMap<Double, Integer> priorityMap = new TreeMap<>();
        way.getTags().put("highway", "residential");
        way.getTags().put("tunnel", "yes");
        way.getTags().put("sidewalk", "no");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.AVOID_IF_POSSIBLE.getValue(), priorityMap.lastEntry().getValue());

        way.getTags().put("sidewalk", "both");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.UNCHANGED.getValue(), priorityMap.lastEntry().getValue());
    }

    @Test
    public void testBicyclePathPriority(){
        way.getTags().put("highway", "path");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("bicycle", "official");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("bicycle", "designated");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("bicycle", "permissive");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testSpeed() {
        // TODO GH0.10: assertEquals(5.0, flagEncoder.getSpeed(683), 0.0);
        // TODO GH0.10: assertEquals(20.0, flagEncoder.getSpeed(635), 0.0);
        fail("TODO: find out how to test this.");
    }

    @Test
    public void testSupports() {
        assertTrue(flagEncoder.supports(PriorityWeighting.class));
        assertFalse(flagEncoder.supports(TurnWeighting.class));
    }

    @Test
    public void getWeighting() {
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

    @Test
    public void testRoundaboutFlag() {
        way = generatePedestrianWay();
        way.getTags().put("junction", "roundabout");
        // TODO GH0.10: assertEquals(687, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
        way.getTags().put("junction", "circular");
        // TODO GH0.10: assertEquals(687, flagEncoder.handleWayTags(way, 1, 0));
        fail("TODO: find out how to test this.");
    }
}
