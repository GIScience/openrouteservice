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
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

public class HikingFlagEncoderTest {
    private HikingFlagEncoder flagEncoder;
    private ReaderWay way;

    public HikingFlagEncoderTest() {
        PMap properties = new PMap();
        ORSDefaultFlagEncoderFactory encoderFactory = new ORSDefaultFlagEncoderFactory();
        flagEncoder = (HikingFlagEncoder)new EncodingManager(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.HIKING_ORS, 4).getEncoder(FlagEncoderNames.HIKING_ORS);
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

        assertEquals(1, flagEncoder.acceptWay(way));
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
        rel.getTags().put("route", "hiking");

        rel.getTags().put("network", "iwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "nwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "rwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "lwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(rel, 0));

        rel.getTags().put("route","foot");rel.getTags().put("network", "iwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "nwn");
        assertEquals(PriorityCode.BEST.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "rwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(rel, 0));
        rel.getTags().put("network", "lwn");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(rel, 0));

        rel.getTags().put("network", "unknown");
        assertEquals(PriorityCode.VERY_NICE.getValue(), flagEncoder.handleRelationTags(rel, 0));

        rel.getTags().put("route", "ferry");
        assertEquals(PriorityCode.AVOID_IF_POSSIBLE.getValue(), flagEncoder.handleRelationTags(rel, 0));

    }

    @Test
    public void testOldRelationValueMaintained() {
        ReaderRelation rel = new ReaderRelation(1);
        rel.getTags().put("route", "hiking");

        rel.getTags().put("network", "rwn");
        assertEquals(7, flagEncoder.handleRelationTags(rel, 7));
    }

    @Test
    public void testAddPriorityFromRelation() {
        way = generateHikeWay();
        assertEquals(171, flagEncoder.handleWayTags(way, 1, 1));
    }

    @Test
    public void testRejectWay() {
        assertEquals(0, flagEncoder.handleWayTags(way, 0, 0));
    }

    @Test
    public void testFerrySpeed() {
        way = generateFerryWay();
        assertEquals(555, flagEncoder.handleWayTags(way, 3, 0));
    }

    @Test
    public void testHikingFlags() {
        way = generateHikeWay();
        assertEquals(811, flagEncoder.handleWayTags(way, 1, 0));

        way.getTags().put("highway", "living_street");
        assertEquals(683, flagEncoder.handleWayTags(way, 1, 0));
    }

    @Test
    public void testDifficultHikingFlags() {
        way = generateHikeWay();
        way.getTags().put("sac_scale", "alpine_hiking");
        assertEquals(787, flagEncoder.handleWayTags(way, 1, 0));
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
    public void testSafeHighwayPriorities() {
        TreeMap<Double, Integer> priorityMap = new TreeMap<>();
        way.getTags().put("highway", "track");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
        way.getTags().put("highway", "path");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
        way.getTags().put("highway", "footway");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.VERY_NICE.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();

        way.getTags().put("highway", "living_street");
        flagEncoder.assignSafeHighwayPriority(way, priorityMap);
        assertEquals((Integer)PriorityCode.PREFER.getValue(), priorityMap.lastEntry().getValue());
        priorityMap.clear();
    }

    @Test
    public void testAcceptWayFerry() {
        way = generateFerryWay();
        assertEquals(3, flagEncoder.acceptWay(way));
    }

    @Test
    public void testAcceptFootway() {
        way = generateHikeWay();
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
        way = generateHikeWay();
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
        way = generateHikeWay();
        way.getTags().put("motorroad", "yes");
        assertEquals(0, flagEncoder.acceptWay(way));
    }

    @Test
    public void testDefaultFords() {
        way = generateHikeWay();
        way.getTags().put("ford", "yes");
        assertEquals(1, flagEncoder.acceptWay(way));
    }
}
