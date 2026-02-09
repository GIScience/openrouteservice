package org.heigit.ors.routing;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteResultBuilderTest {
    private RouteResultBuilder builder = new RouteResultBuilder();
    private RoutingRequest request1;
    private RoutingRequest request2;
    private static final String OSM_ID = "osmid"; // TODO: find a better solution than a hardcoded string

    public RouteResultBuilderTest() {
        init();
    }

    @BeforeEach
    void init() {
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(12.3, 45.6);
        coordinates[1] = new Coordinate(23.4, 56.7);
        request1 = new RoutingRequest();
        request1.setCoordinates(coordinates);
        request1.setAttributes(new String[]{"detourfactor"});
        request1.setExtraInfo(RouteExtraInfoFlag.getFromString(OSM_ID));
        request1.setIncludeManeuvers(true);

        coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(23.4, 56.7);
        coordinates[1] = new Coordinate(34.5, 67.8);
        request2 = new RoutingRequest();
        request2.setCoordinates(coordinates);
    }

    private GHResponse constructResponse(RoutingRequest request) {
        Coordinate[] coordinates = request.getCoordinates();
        GHRequest ghRequest = new GHRequest(
                coordinates[0].y, coordinates[0].x, // Start point (latitude, longitude)
                coordinates[1].y, coordinates[1].x  // End point (latitude, longitude)
        );
        return builder.constructFreeHandRoute(ghRequest);
    }

    @Test
    void directRouteTest() {
        GHRequest ghRequest = new GHRequest(49.41281601436809, 8.686215877532959, 49.410163456220076, 8.687160015106201);
        GHResponse ghResponse = builder.constructFreeHandRoute(ghRequest);

        assertTrue(ghResponse.getHints().has("skipped_segment"));
        assertTrue(ghResponse.getHints().getBool("skipped_segment", false));

        assertEquals(1, ghResponse.getAll().size());
        ResponsePath responsePath = ghResponse.getAll().get(0);

        assertEquals(0, responsePath.getErrors().size());
        assertEquals(0, responsePath.getDescription().size());
        assertEquals(302, responsePath.getDistance(), 1);
        assertEquals(0.0, responsePath.getAscend(), 0);
        assertEquals(0.0, responsePath.getDescend(), 0);
        assertEquals(0.0, responsePath.getRouteWeight(), 0);
        assertEquals(0, responsePath.getTime());
        assertEquals("", responsePath.getDebugInfo());
        assertEquals(2, responsePath.getInstructions().size());
        assertEquals(1, responsePath.getInstructions().get(0).getPoints().size());
        assertEquals(0, responsePath.getNumChanges());
        assertEquals(0, responsePath.getLegs().size());
        assertEquals(0, responsePath.getPathDetails().size());
        assertNull(responsePath.getFare());
        assertFalse(responsePath.isImpossible());

        checkInstructions(responsePath.getInstructions());
        checkPointList(responsePath.getWaypoints());
        checkPointList(responsePath.getPoints());
    }

    private void checkInstructions(InstructionList instructions) {
        for (Instruction instruction : instructions) {
            PointList points = instruction.getPoints();

            assertEquals(2, points.getDimension());
            assertFalse(points.isEmpty());
            assertFalse(points.is3D());
            assertFalse(points.isImmutable());
            assertEquals(0, instruction.getExtraInfoJSON().size());

            if (instruction.getName().equals("free hand route") && instruction.getSign() == Instruction.REACHED_VIA) {
                assertEquals(1, instruction.getPoints().size());
                assertEquals(49.41281601436809, instruction.getPoints().getLat(0), 0);
                assertEquals(8.686215877532959, instruction.getPoints().getLon(0), 0);
                assertEquals(302, instruction.getDistance(), 1);
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
        assertEquals(2, waypoints.size());
        assertEquals(49.41281601436809, waypoints.getLat(0), 0);
        assertEquals(49.410163456220076, waypoints.getLat(1), 0);
        assertEquals(8.686215877532959, waypoints.getLon(0), 0);
        assertEquals(8.687160015106201, waypoints.getLon(1), 0);
    }

    @Test
    void TestCreateMergedRouteResultFromBestPaths0() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();
        List<RouteExtraInfo> extrasList = new ArrayList<>();
        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});

        assertEquals(0.0, result.getSummary().getDistance(), 0.0, "Empty response list should return empty RouteResult (summary.distance = 0.0)");
        assertEquals(0, result.getSegments().size(), "Empty response list should return empty RouteResult (no segments)");
        assertEquals(0, result.getExtraInfo().size(), "Empty response list should return empty RouteResult (no extra info)");
        assertNull(result.getGeometry(), "Empty response list should return empty RouteResult (no geometry)");
    }

    @Test
    void TestCreateMergedRouteResultFromBestPaths1() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();
        List<RouteExtraInfo> extrasList = new ArrayList<>();
        extrasList.add(new RouteExtraInfo("osmid"));
        responseList.add(constructResponse(request1));
        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});

        assertEquals(1452977.2, result.getSummary().getDistance(), 0.0, "Single response should return valid RouteResult (summary.duration = 1452977.2)");
        assertEquals("12.3,23.4,45.6,56.7", result.getSummary().getBBox().toString(), "Single response should return valid RouteResult (summary.bbox = 12.3,23.4,45.6,56.7)");
        assertEquals(2, result.getGeometry().length, "Single response should return valid RouteResult (geometry.length = 2)");
        assertEquals("(12.3, 45.6, NaN)", result.getGeometry()[0].toString(), "Single response should return valid RouteResult (geometry[0] = 12.3, 45.6, NaN)");
        assertEquals("(23.4, 56.7, NaN)", result.getGeometry()[1].toString(), "Single response should return valid RouteResult (geometry[1] = 23.4, 56.7, NaN)");
        assertEquals(1, result.getSegments().size(), "Single response should return valid RouteResult (segments.size = 1)");
        assertEquals(1452977.2, result.getSegments().get(0).getDistance(), 0.0, "Single response should return valid RouteResult (segments[0].distance = 1452977.2)");
        assertEquals(1.0, result.getSegments().get(0).getDetourFactor(), 0.0, "Single response should return valid RouteResult (segments[0].detourFactor = 1.0)");
        assertEquals(2, result.getSegments().get(0).getSteps().size(), "Single response should return valid RouteResult (segments[0].steps.size = 2)");
        assertEquals("free hand route", result.getSegments().get(0).getSteps().get(0).getName(), "Single response should return valid RouteResult (segments[0].steps[0].name = 'free hand route')");
        assertEquals(11, result.getSegments().get(0).getSteps().get(0).getType(), "Single response should return valid RouteResult (segments[0].steps[0].type = 11)");
        assertEquals(32, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter(), "Single response should return valid RouteResult (segments[0].steps[0].maneuver.bearingAfter = 32)");
        assertEquals("end of free hand route", result.getSegments().get(0).getSteps().get(1).getName(), "Single response should return valid RouteResult (segments[0].steps[1].name = 'end of free hand route')");
        assertEquals(10, result.getSegments().get(0).getSteps().get(1).getType(), "Single response should return valid RouteResult (segments[0].steps[1].type = 10)");
        assertEquals(0, result.getSegments().get(0).getSteps().get(1).getManeuver().getBearingAfter(), "Single response should return valid RouteResult (segments[0].steps[1].maneuver.bearingAfter = 0)");
        assertEquals(1, result.getExtraInfo().size(), "Single response should return valid RouteResult (extrainfo.size = 1)");
        assertEquals(OSM_ID, result.getExtraInfo().get(0).getName(), "Single response should return valid RouteResult (extrainfo[0].name = 'osmid)");
        assertEquals(2, result.getWayPointsIndices().size(), "Single response should return valid RouteResult (waypointindices.size = 2)");
    }

    @Test
    @SuppressWarnings("java:S5961")
    void TestCreateMergedRouteResultFromBestPaths2() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();
        List<RouteExtraInfo> extrasList = new ArrayList<>();
        extrasList.add(new RouteExtraInfo(OSM_ID));
        responseList.add(constructResponse(request1));
        responseList.add(constructResponse(request2));
        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});

        assertEquals(2809674.1, result.getSummary().getDistance(), 0.0, "Two responses should return merged RouteResult (summary.duration = 2809674.1)");
        assertEquals("12.3,34.5,45.6,67.8", result.getSummary().getBBox().toString(), "Two responses should return merged RouteResult (summary.bbox = 12.3,34.5,45.6,67.8)");
        assertEquals(3, result.getGeometry().length, "Two responses should return merged RouteResult (geometry.length = 3)");
        assertEquals("(12.3, 45.6, NaN)", result.getGeometry()[0].toString(), "Two responses should return merged RouteResult (geometry[0] = 12.3, 45.6, NaN)");
        assertEquals("(23.4, 56.7, NaN)", result.getGeometry()[1].toString(), "Two responses should return merged RouteResult (geometry[1] = 23.4, 56.7, NaN)");
        assertEquals("(34.5, 67.8, NaN)", result.getGeometry()[2].toString(), "Two responses should return merged RouteResult (geometry[2] = 34.5, 67.8, NaN)");
        assertEquals(2, result.getSegments().size(), "Two responses should return merged RouteResult (segments.size = 2)");
        assertEquals(1452977.2, result.getSegments().get(0).getDistance(), 0.0, "Two responses should return merged RouteResult (segments[0].distance = 1452977.2)");
        assertEquals(1.0, result.getSegments().get(0).getDetourFactor(), 0.0, "Two responses should return merged RouteResult (segments[0].detourFactor = 1.0)");
        assertEquals(2, result.getSegments().get(0).getSteps().size(), "Two responses should return merged RouteResult (segments[0].steps.size = 2)");
        assertEquals("free hand route", result.getSegments().get(0).getSteps().get(0).getName(), "Two responses should return merged RouteResult (segments[0].steps[0].name = 'free hand route')");
        assertEquals(11, result.getSegments().get(0).getSteps().get(0).getType(), "Two responses should return merged RouteResult (segments[0].steps[0].type = 11)");
        assertEquals(32, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[0].steps[0].maneuver.bearingAfter = 32)");
        assertEquals("end of free hand route", result.getSegments().get(0).getSteps().get(1).getName(), "Two responses should return merged RouteResult (segments[0].steps[1].name = 'end of free hand route')");
        assertEquals(10, result.getSegments().get(0).getSteps().get(1).getType(), "Two responses should return merged RouteResult (segments[0].steps[1].type = 10)");
        assertEquals(0, result.getSegments().get(0).getSteps().get(1).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[0].steps[1].maneuver.bearingAfter = 0)");
        assertEquals(1356696.9, result.getSegments().get(1).getDistance(), 0.0, "Two responses should return merged RouteResult (segments[1].distance = 1356696.9)");
        assertEquals(1.0, result.getSegments().get(1).getDetourFactor(), 0.0, "Two responses should return merged RouteResult (segments[1].detourFactor = 1.0)");
        assertEquals(2, result.getSegments().get(1).getSteps().size(), "Two responses should return merged RouteResult (segments[1].steps.size = 2)");
        assertEquals("free hand route", result.getSegments().get(1).getSteps().get(0).getName(), "Two responses should return merged RouteResult (segments[1].steps[0].name = 'free hand route')");
        assertEquals(11, result.getSegments().get(1).getSteps().get(0).getType(), "Two responses should return merged RouteResult (segments[1].steps[0].type = 11)");
        assertEquals(25, result.getSegments().get(1).getSteps().get(0).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[1].steps[0].maneuver.bearingAfter = 25)");
        assertEquals("end of free hand route", result.getSegments().get(1).getSteps().get(1).getName(), "Two responses should return merged RouteResult (segments[1].steps[1].name = 'end of free hand route')");
        assertEquals(10, result.getSegments().get(1).getSteps().get(1).getType(), "Two responses should return merged RouteResult (segments[1].steps[1].type = 10)");
        assertEquals(0, result.getSegments().get(1).getSteps().get(1).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[1].steps[1].maneuver.bearingAfter = 0)");
        assertEquals(1, result.getExtraInfo().size(), "Two responses should return merged RouteResult (extrainfo.size = 1)");
        assertEquals(OSM_ID, result.getExtraInfo().get(0).getName(), "Two responses should return merged RouteResult (extrainfo[0].name = 'osmid)");
        assertEquals(3, result.getWayPointsIndices().size(), "Two responses should return merged RouteResult (waypointindices.size = 3)");
    }

    @Test
    void TestCreateMergedRouteResultFromBestPaths3() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();
        List<RouteExtraInfo> extrasList = new ArrayList<>();
        extrasList.add(new RouteExtraInfo(OSM_ID));
        List<Integer> skipSegments = new ArrayList<>();
        skipSegments.add(1);
        request1.setSkipSegments(skipSegments);
        responseList.add(constructResponse(request1));

        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});
        assertEquals(1, result.getWarnings().size(), "Response with SkipSegments should return RouteResult with warning");
        assertEquals(3, result.getWarnings().get(0).getWarningCode(), "Response with SkipSegments should return RouteResult with warning (code 3)");
    }

}
