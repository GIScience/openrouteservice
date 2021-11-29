package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GHRequest;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.junit.Assert;
import org.junit.Test;

public class ORSGraphHopperTest {

    @Test
    public void directRouteTest() {
        GHRequest ghRequest = new GHRequest(49.41281601436809, 8.686215877532959, 49.410163456220076, 8.687160015106201);
        GHResponse ghResponse = new ORSGraphHopper().constructFreeHandRoute(ghRequest);

        Assert.assertTrue(ghResponse.getHints().has("skipped_segment"));
        Assert.assertTrue(ghResponse.getHints().getBool("skipped_segment", false));

        Assert.assertEquals(1, ghResponse.getAll().size());
        ResponsePath responsePath = ghResponse.getAll().get(0);

        Assert.assertEquals(0, responsePath.getErrors().size());
        Assert.assertEquals(0, responsePath.getDescription().size());
        Assert.assertEquals(309.892f, responsePath.getDistance(), 3);
        Assert.assertEquals(0.0, responsePath.getAscend(), 0);
        Assert.assertEquals(0.0, responsePath.getDescend(), 0);
        Assert.assertEquals(0.0, responsePath.getRouteWeight(), 0);
        Assert.assertEquals(0, responsePath.getTime());
        Assert.assertEquals("", responsePath.getDebugInfo());
        Assert.assertEquals(2, responsePath.getInstructions().size());
        Assert.assertEquals(1, responsePath.getInstructions().get(0).getPoints().size());
        Assert.assertEquals(0, responsePath.getNumChanges());
        Assert.assertEquals(0, responsePath.getLegs().size());
        Assert.assertEquals(0, responsePath.getPathDetails().size());
        Assert.assertNull(responsePath.getFare());
        Assert.assertFalse(responsePath.isImpossible());

        checkInstructions(responsePath.getInstructions());
        checkPointList(responsePath.getWaypoints());
        checkPointList(responsePath.getPoints());

    }

    private void checkInstructions(InstructionList instructions) {
        for (Instruction instruction : instructions) {
            PointList points = instruction.getPoints();

            Assert.assertEquals(2, points.getDimension());
            Assert.assertFalse(points.isEmpty());
            Assert.assertFalse(points.is3D());
            Assert.assertFalse(points.isImmutable());
            Assert.assertEquals(0, instruction.getExtraInfoJSON().size());

            if (instruction.getName().equals("free hand route") && instruction.getSign() == Instruction.REACHED_VIA) {
                Assert.assertEquals(1, instruction.getPoints().size());
                Assert.assertEquals(49.41281601436809, instruction.getPoints().getLat(0), 0);
                Assert.assertEquals(8.686215877532959, instruction.getPoints().getLon(0), 0);
                Assert.assertEquals(309.892f, instruction.getDistance(), 3);
                Assert.assertEquals(0, instruction.getTime());
            } else if (instruction.getName().equals("end of free hand route") && instruction.getSign() == Instruction.FINISH) {
                Assert.assertEquals(1, instruction.getPoints().size());
                Assert.assertEquals(49.410163456220076, instruction.getPoints().getLat(0), 0);
                Assert.assertEquals(8.687160015106201, instruction.getPoints().getLon(0), 0);
                Assert.assertEquals(0.0, instruction.getDistance(), 0);
                Assert.assertEquals(0, instruction.getTime());
            } else {
                Assert.fail("The name or instruction sign of the skipped_segments instructions are wrong.");
            }
        }

    }

    private void checkPointList(PointList waypoints) {
        Assert.assertFalse(waypoints.is3D());
        Assert.assertFalse(waypoints.isImmutable());
        Assert.assertEquals(2, waypoints.size());
        Assert.assertEquals(49.41281601436809, waypoints.getLat(0), 0);
        Assert.assertEquals(49.410163456220076, waypoints.getLat(1), 0);
        Assert.assertEquals(8.686215877532959, waypoints.getLon(0), 0);
        Assert.assertEquals(8.687160015106201, waypoints.getLon(1), 0);
    }

}