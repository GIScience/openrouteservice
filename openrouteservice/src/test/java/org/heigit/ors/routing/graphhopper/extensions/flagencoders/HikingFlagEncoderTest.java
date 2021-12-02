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
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

public class HikingFlagEncoderTest {
    private final EncodingManager encodingManager;
    private final HikingFlagEncoder flagEncoder;
    private ReaderWay way;

    public HikingFlagEncoderTest() {
        encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.HIKING_ORS);
        flagEncoder = (HikingFlagEncoder)encodingManager.getEncoder(FlagEncoderNames.HIKING_ORS);
    }

    @Before
    public void initWay() {
        way = new ReaderWay(1);
    }

    private ReaderWay generateHikeWay() {
        way.getTags().put("highway", "path");
        return way;
    }

    private ReaderWay generateFerryWay() {
        way.getTags().put("route", "ferry");
        return way;
    }

    @Test
    public void acceptDifficultSacScale() {
        way = generateHikeWay();
        way.getTags().put("sac_scale", "alpine_hiking");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void handleRelationTags() {
        ReaderRelation rel = new ReaderRelation(1);
        IntsRef ref = new IntsRef(2);
        rel.getTags().put("route", "hiking");

        rel.getTags().put("network", "iwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "nwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "rwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "lwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));

        rel.getTags().put("route","foot");rel.getTags().put("network", "iwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "nwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "rwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));
        rel.getTags().put("network", "lwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));

        rel.getTags().put("network", "unknown");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));

        rel.getTags().put("route", "ferry");
        assertEquals(PriorityCode.VERY_BAD.getValue(), flagEncoder.handleRelationTags(ref, rel));

    }

    @Test
    public void testOldRelationValueMaintained() {
        ReaderRelation rel = new ReaderRelation(1);
        rel.setTag("route", "hiking");

        rel.setTag("network", "rwn");
        IntsRef ref = new IntsRef(2);
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(ref, rel));
    }

    @Test
    public void testAddPriorityFromRelation() {
        way = generateHikeWay();
        assertEquals(PriorityCode.REACH_DESTINATION.getValue(), flagEncoder.handlePriority(way, 1));
    }

    @Test
    public void testRejectWay() {
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testFerrySpeed() {
        way = generateFerryWay();
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way,
                EncodingManager.Access.FERRY, 0);
        assertEquals(5.0, flagEncoder.getSpeed(flags), 0.01);
    }

    @Test
    public void testHikingFlags() {
        way = generateHikeWay();
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("highway", "living_street");
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    public void testDifficultHikingFlags() {
        way = generateHikeWay();
        way.setTag("sac_scale", "alpine_hiking");
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way, EncodingManager.Access.WAY, 0);
        assertEquals(FootFlagEncoder.SLOW_SPEED, flagEncoder.getSpeed(false, flags), 0.01);
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
    public void testSafeHighwayPriorities() {
        TreeMap<Double, Integer> priorityMap = new TreeMap<>();
        way.setTag("highway", "track");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
        way.setTag("highway", "path");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
        way.setTag("highway", "footway");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();

        way.setTag("highway", "living_street");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.PREFER.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
    }

    @Test
    public void testAcceptWayFerry() {
        way = generateFerryWay();
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

    @Test
    public void testAcceptFootway() {
        way = generateHikeWay();
        way.getTags().put("foot", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "designated");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "official");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("foot", "permissive");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void testRejectRestrictedFootway() {
        way = generateHikeWay();
        way.getTags().put("foot", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "private");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "restricted");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "military");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("foot", "emergency");
        assertTrue(flagEncoder.getAccess(way).canSkip());

        way.removeTag("foot");
        way.getTags().put("access", "no");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "private");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "restricted");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "military");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("access", "emergency");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testAcceptSidewalks() {
        way.getTags().put("highway", "secondary");
        way.getTags().put("sidewalk", "both");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "left");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "right");
        assertTrue(flagEncoder.getAccess(way).isWay());
        way.getTags().put("sidewalk", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    public void testRejectMotorways() {
        way.getTags().put("highway", "motorway");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("highway", "motorway_link");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testRejectMotorRoad() {
        way = generateHikeWay();
        way.getTags().put("motorroad", "yes");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    public void testDefaultFords() {
        way = generateHikeWay();
        way.getTags().put("ford", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }
}
