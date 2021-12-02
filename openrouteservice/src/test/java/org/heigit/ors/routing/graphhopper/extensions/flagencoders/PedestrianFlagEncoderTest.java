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
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

public class PedestrianFlagEncoderTest {
    private final EncodingManager encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.PEDESTRIAN_ORS);
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

    @Before
    public void initWay() {
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

    @Test
    public void rejectDifficultSacScale() {
        way = generatePedestrianWay();
        way.setTag("sac_scale", "alpine_hiking");

        // TODO GH0.10: assertEquals(0, flagEncoder.acceptWay(way));
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void handleRelationTags() {
        ReaderRelation rel = new ReaderRelation(1);

        rel.setTag("route", "ferry");
        IntsRef ref = new IntsRef(2);
        assertEquals(PriorityCode.VERY_BAD.getValue(), flagEncoder.handleRelationTags(ref, rel));
    }

    @Test
    public void testRejectWay() {
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testFerryFlags() {
        way = generateFerryWay();
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way,
                EncodingManager.Access.FERRY, null);
        assertEquals(15, flagEncoder.getAverageSpeedEnc().getDecimal(false,flags), 0.01);
    }

    @Test
    public void testPlatformFlags() {
        way.setTag("railway", "platform");
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("railway", "track");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testPierFlags() {
        way.setTag("man_made", "pier");
        assertTrue(flagEncoder.getAccess(way).isWay());

        way.setTag("man_made", "not_a_pier");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testHikingFlags() {
        way = generatePedestrianWay();
        way.setTag("sac_scale", "hiking");
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("highway", "living_street");
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    public void testDesignatedFootwayPriority() {
        way.setTag("highway", "secondary");
        assertEquals(PriorityCode.REACH_DESTINATION.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("foot", "designated");
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    public void testAvoidWaysWithoutSidewalks() {
        way.setTag("highway", "primary");
        assertEquals(PriorityCode.REACH_DESTINATION.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "both");
        assertEquals(PriorityCode.UNCHANGED.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "none");
        assertEquals(PriorityCode.REACH_DESTINATION.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    public void testAcceptWayFerry() {
        way = generateFerryWay();
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

    @Test
    public void testAcceptFootway() {
        way = generatePedestrianWay();
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
    public void testRejectRestrictedFootway() {
        way = generatePedestrianWay();
        way.setTag("foot", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "private");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "restricted");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "military");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("foot", "emergency");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.removeTag("foot");
        way.setTag("access", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "private");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "restricted");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "military");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("access", "emergency");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testAcceptSidewalks() {
        way.setTag("highway", "secondary");
        way.setTag("sidewalk", "both");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "left");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "right");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.setTag("sidewalk", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void testRejectMotorways() {
        way.setTag("highway", "motorway");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.setTag("highway", "motorway_link");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testRejectMotorRoad() {
        way = generatePedestrianWay();
        way.setTag("motorroad", "yes");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testDefaultFords() {
        way = generatePedestrianWay();
        way.setTag("ford", "yes");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testTunnelValues() {
        TreeMap<Double, Integer> priorityMap = new TreeMap<>();
        way.setTag("highway", "residential");
        way.setTag("tunnel", "yes");
        way.setTag("sidewalk", "no");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_BAD.getValue(), priorityMap.lastEntry().getValue());

        way.setTag("sidewalk", "both");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.UNCHANGED.getValue(), priorityMap.lastEntry().getValue());
    }

    @Test
    public void testBicyclePathPriority(){
        way.setTag("highway", "path");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "official");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.VERY_BAD.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "designated");
        // TODO GH0.10: assertEquals(427, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.VERY_BAD.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("bicycle", "permissive");
        // TODO GH0.10: assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }
}
