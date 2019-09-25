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
        flagEncoder = (PedestrianFlagEncoder)new EncodingManager(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.PEDESTRIAN_ORS, 4).getEncoder(FlagEncoderNames.PEDESTRIAN_ORS);
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

        assertEquals(0, flagEncoder.acceptWay(way));
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
        assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handleRelationTags(rel, 0));
    }

    @Test
    public void testRejectWay() {
        assertEquals(0, flagEncoder.handleWayTags(way, 0, 0));
    }

    @Test
    public void testFerryFlags() {
        way = generateFerryWay();
        assertEquals(635, flagEncoder.handleWayTags(way, 3, 0));
    }

    @Test
    public void testPlatformFlags() {
        way.getTags().put("railway", "platform");
        assertEquals(1, flagEncoder.acceptWay(way));

        way.getTags().put("railway", "track");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testPierFlags() {
        way.getTags().put("man_made", "pier");
        assertEquals(1, flagEncoder.acceptWay(way));

        way.getTags().put("man_made", "not_a_pier");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testHikingFlags() {
        way = generatePedestrianWay();
        way.getTags().put("sac_scale", "hiking");
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));

        way.getTags().put("highway", "living_street");
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
    }

    @Test
    public void testDesignatedFootwayPriority() {
        way.getTags().put("highway", "secondary");
        assertEquals(299, flagEncoder.handleWayTags(way, 1, 0));

        way.getTags().put("foot", "designated");
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
    }

    @Test
    public void testAvoidWaysWithoutSidewalks() {
        way.getTags().put("highway", "primary");
        assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("sidewalk", "both");
        assertEquals(555, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("sidewalk", "none");
        assertEquals(171, flagEncoder.handleWayTags(way, 1, 0));
    }

    @Test
    public void testAcceptWayFerry() {
        way = generateFerryWay();
        assertEquals(3, flagEncoder.acceptWay(way));
    }

    @Test
    public void testAcceptFootway() {
        way = generatePedestrianWay();
        way.getTags().put("foot", "yes");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "designated");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "official");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "permissive");
        assertEquals(1, flagEncoder.acceptWay(way));
    }

    @Test
    public void testRejectRestrictedFootway() {
        way = generatePedestrianWay();
        way.getTags().put("foot", "no");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "private");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "restricted");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "military");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("foot", "emergency");
        assertEquals(0, flagEncoder.acceptWay(way));

        way.removeTag("foot");
        way.getTags().put("access", "no");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("access", "private");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("access", "restricted");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("access", "military");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("access", "emergency");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testAcceptSidewalks() {
        way.getTags().put("highway", "secondary");
        way.getTags().put("sidewalk", "both");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("sidewalk", "left");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("sidewalk", "right");
        assertEquals(1, flagEncoder.acceptWay(way));
        way.getTags().put("sidewalk", "yes");
        assertEquals(1, flagEncoder.acceptWay(way));
    }

    @Test
    public void testRejectMotorways() {
        way.getTags().put("highway", "motorway");
        assertEquals(0, flagEncoder.acceptWay(way));
        way.getTags().put("highway", "motorway_link");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testRejectMotorRoad() {
        way = generatePedestrianWay();
        way.getTags().put("motorroad", "yes");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testDefaultFords() {
        way = generatePedestrianWay();
        way.getTags().put("ford", "yes");
        assertEquals(0, flagEncoder.acceptWay(way));
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
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("bicycle", "official");
        assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("bicycle", "designated");
        assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("bicycle", "permissive");
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
    }

    @Test
    public void testSpeed() {
        assertEquals(5.0, flagEncoder.getSpeed(683), 0.0);
        assertEquals(20.0, flagEncoder.getSpeed(635), 0.0);
    }

    @Test
    public void testSupports() {
        assertTrue(flagEncoder.supports(PriorityWeighting.class));
        assertFalse(flagEncoder.supports(TurnWeighting.class));
    }

    @Test
    public void getWeighting() {
        assertEquals(0.714, flagEncoder.getDouble(683, PriorityWeighting.KEY), 0.001);
        boolean throwsError = false;
        try {
            // Only priority weighting allowed
            flagEncoder.getDouble(683, 1);
        } catch (UnsupportedOperationException e) {
            throwsError = true;
        }

        assertTrue(throwsError);
    }

    @Test
    public void testRoundaboutFlag() {
        way = generatePedestrianWay();
        way.getTags().put("junction", "roundabout");
        assertEquals(687, flagEncoder.handleWayTags(way, 1, 0));
        way.getTags().put("junction", "circular");
        assertEquals(687, flagEncoder.handleWayTags(way, 1, 0));
    }
}
