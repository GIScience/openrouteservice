package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ORSGraphHopperTest {

    @Test
    void directRouteTest() {
        GHRequest ghRequest = new GHRequest(49.41281601436809, 8.686215877532959, 49.410163456220076, 8.687160015106201);
        GHResponse ghResponse = new ORSGraphHopper().constructFreeHandRoute(ghRequest);

        assertTrue(ghResponse.getHints().has("skipped_segment"));
        assertTrue(ghResponse.getHints().getBool("skipped_segment", false));

        assertEquals(1, ghResponse.getAll().size());
        PathWrapper directRouteWrapper = ghResponse.getAll().get(0);

        assertEquals(0, directRouteWrapper.getErrors().size());
        assertEquals(0, directRouteWrapper.getDescription().size());
        assertEquals(309.892f, directRouteWrapper.getDistance(), 3);
        assertEquals(0.0, directRouteWrapper.getAscend(), 0);
        assertEquals(0.0, directRouteWrapper.getDescend(), 0);
        assertEquals(0.0, directRouteWrapper.getRouteWeight(), 0);
        assertEquals(0, directRouteWrapper.getTime());
        assertEquals("", directRouteWrapper.getDebugInfo());
        assertEquals(2, directRouteWrapper.getInstructions().size());
        assertEquals(1, directRouteWrapper.getInstructions().get(0).getPoints().size());
        assertEquals(0, directRouteWrapper.getNumChanges());
        assertEquals(0, directRouteWrapper.getLegs().size());
        assertEquals(0, directRouteWrapper.getPathDetails().size());
        assertNull(directRouteWrapper.getFare());
        assertFalse(directRouteWrapper.isImpossible());

        checkInstructions(directRouteWrapper.getInstructions());
        checkPointList(directRouteWrapper.getWaypoints());
        checkPointList(directRouteWrapper.getPoints());

    }

    private void checkInstructions(InstructionList instructions) {
        for (Instruction instruction : instructions) {
            PointList points = instruction.getPoints();

            assertEquals(2, points.getDimension());
            assertFalse(points.isEmpty());
            assertFalse(points.is3D());
            assertFalse(points.isImmutable());
            assertTrue(instruction.getAnnotation().isEmpty());
            assertEquals(0, instruction.getExtraInfoJSON().size());

            if (instruction.getName().equals("free hand route") && instruction.getSign() == Instruction.REACHED_VIA) {
                assertEquals(1, instruction.getPoints().size());
                assertEquals(49.41281601436809, instruction.getPoints().getLat(0), 0);
                assertEquals(8.686215877532959, instruction.getPoints().getLon(0), 0);
                assertEquals(309.892f, instruction.getDistance(), 3);
                assertEquals(0, instruction.getTime());
            } else if (instruction.getName().equals("end of free hand route") && instruction.getSign() == Instruction.FINISH) {
                assertEquals(1, instruction.getPoints().size());
                assertEquals(49.410163456220076, instruction.getPoints().getLat(0), 0);
                assertEquals(8.687160015106201, instruction.getPoints().getLon(0), 0);
                assertEquals(0.0, instruction.getDistance(), 0);
                assertEquals(0, instruction.getTime());
            } else {
                fail("The name or instruction sign of the skipped_segments instructions are wrong.");
            }
        }

    }

    private void checkPointList(PointList waypoints) {
        assertFalse(waypoints.is3D());
        assertFalse(waypoints.isImmutable());
        assertEquals(2, waypoints.getSize());
        assertEquals(49.41281601436809, waypoints.getLat(0), 0);
        assertEquals(49.410163456220076, waypoints.getLat(1), 0);
        assertEquals(8.686215877532959, waypoints.getLon(0), 0);
        assertEquals(8.687160015106201, waypoints.getLon(1), 0);
    }

}