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

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.IntsRef;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.util.PriorityCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HikingFlagEncoderTest {
    private final EncodingManager encodingManager;
    private final HikingFlagEncoder flagEncoder;
    private ReaderWay way;

    public HikingFlagEncoderTest() {
        encodingManager = EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.HIKING_ORS);
        flagEncoder = (HikingFlagEncoder)encodingManager.getEncoder(FlagEncoderNames.HIKING_ORS);
    }

    @BeforeEach
    void initWay() {
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
    void acceptDifficultSacScale() {
        way = generateHikeWay();
        way.getTags().put("sac_scale", "alpine_hiking");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }

    @Test
    void testAddPriorityFromRelation() {
        way = generateHikeWay();
        assertEquals(PriorityCode.AVOID_AT_ALL_COSTS.getValue(), flagEncoder.handlePriority(way, 1));
    }

    @Test
    void testRejectWay() {
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testFerrySpeed() {
        way = generateFerryWay();
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way,
                EncodingManager.Access.FERRY, null);
        assertEquals(5.0, flagEncoder.getAverageSpeedEnc().getDecimal(false, flags), 0.01);
    }

    @Test
    void testHikingFlags() {
        way = generateHikeWay();
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handlePriority(way, 0));

        way.setTag("highway", "living_street");
        assertEquals(PriorityCode.PREFER.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    void testDifficultHikingFlags() {
        way = generateHikeWay();
        way.setTag("sac_scale", "alpine_hiking");
        IntsRef flags = flagEncoder.handleWayTags(encodingManager.createEdgeFlags(), way, EncodingManager.Access.WAY, null);
        assertEquals(FootFlagEncoder.SLOW_SPEED, flagEncoder.getAverageSpeedEnc().getDecimal(false, flags), 0.01);
    }

    @Test
    void testAvoidWaysWithoutSidewalks() {
        way.setTag("highway", "primary");
        assertEquals(PriorityCode.AVOID_AT_ALL_COSTS.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "both");
        assertEquals(PriorityCode.UNCHANGED.getValue(), flagEncoder.handlePriority(way, 0));
        way.setTag("sidewalk", "none");
        assertEquals(PriorityCode.AVOID_AT_ALL_COSTS.getValue(), flagEncoder.handlePriority(way, 0));
    }

    @Test
    void testSafeHighwayPriorities() {
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
    void testAcceptWayFerry() {
        way = generateFerryWay();
        assertTrue(flagEncoder.getAccess(way).isFerry());
    }

    @Test
    void testAcceptFootway() {
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
    void testRejectRestrictedFootway() {
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
    void testAcceptSidewalks() {
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
    void testRejectMotorways() {
        way.getTags().put("highway", "motorway");
        assertTrue(flagEncoder.getAccess(way).canSkip());
        way.getTags().put("highway", "motorway_link");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testRejectMotorRoad() {
        way = generateHikeWay();
        way.getTags().put("motorroad", "yes");
        assertTrue(flagEncoder.getAccess(way).canSkip());
    }

    @Test
    void testDefaultFords() {
        way = generateHikeWay();
        way.getTags().put("ford", "yes");
        assertTrue(flagEncoder.getAccess(way).isWay());
    }
}
