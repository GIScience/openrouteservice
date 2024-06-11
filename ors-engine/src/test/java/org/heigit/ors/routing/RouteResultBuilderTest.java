package org.heigit.ors.routing;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.*;
import org.heigit.ors.util.CoordTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteResultBuilderTest {
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
        Coordinate[] coords = request.getCoordinates();
        LineString lineString = new GeometryFactory().createLineString(coords);
        ResponsePath responsePath = new ResponsePath();
        PointList pointList = new PointList();
        PointList startPointList = new PointList();
        PointList endPointList = new PointList();
        PointList wayPointList = new PointList();
        Coordinate startCoordinate = lineString.getCoordinateN(0);
        Coordinate endCoordinate = lineString.getCoordinateN(1);
        double distance = CoordTools.calcDistHaversine(startCoordinate.x, startCoordinate.y, endCoordinate.x, endCoordinate.y);
        pointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        pointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        wayPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        wayPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        startPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        endPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        Translation translation = new TranslationMap.TranslationHashMap(new Locale(""));
        InstructionList instructions = new InstructionList(translation);
        Instruction startInstruction = new Instruction(Instruction.REACHED_VIA, "Instruction 1", startPointList);
        Instruction endInstruction = new Instruction(Instruction.FINISH, "Instruction 2", endPointList);
        instructions.add(0, startInstruction);
        instructions.add(1, endInstruction);
        responsePath.setDistance(distance);
        responsePath.setAscend(0.0);
        responsePath.setDescend(0.0);
        responsePath.setTime(0);
        responsePath.setInstructions(instructions);
        responsePath.setWaypoints(wayPointList);
        responsePath.setPoints(pointList);
        responsePath.setRouteWeight(0.0);
        responsePath.setDescription(new ArrayList<>());
        responsePath.setImpossible(false);
        startInstruction.setDistance(distance);
        startInstruction.setTime(0);
        GHResponse response = new GHResponse();
        response.add(responsePath);
        response.getHints().putObject("skipped_segment", "true");
        return response;
    }

    @Test
    void TestCreateMergedRouteResultFromBestPaths0() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();
        List<RouteExtraInfo> extrasList = new ArrayList<>();
        RouteResultBuilder builder = new RouteResultBuilder();
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
        RouteResultBuilder builder = new RouteResultBuilder();
        extrasList.add(new RouteExtraInfo("osmid"));
        responseList.add(constructResponse(request1));
        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});

        assertEquals(1452977.2, result.getSummary().getDistance(), 0.0, "Single response should return valid RouteResult (summary.duration = 1452977.2)");
        assertEquals("45.6,56.7,12.3,23.4", result.getSummary().getBBox().toString(), "Single response should return valid RouteResult (summary.bbox = 45.6,56.7,12.3,23.4)");
        assertEquals(2, result.getGeometry().length, "Single response should return valid RouteResult (geometry.length = 2)");
        assertEquals("(45.6, 12.3, NaN)", result.getGeometry()[0].toString(), "Single response should return valid RouteResult (geometry[0] = 45.6,12.3,NaN)");
        assertEquals("(56.7, 23.4, NaN)", result.getGeometry()[1].toString(), "Single response should return valid RouteResult (geometry[1] = 56.7,23.4,NaN)");
        assertEquals(1, result.getSegments().size(), "Single response should return valid RouteResult (segments.size = 1)");
        assertEquals(1452977.2, result.getSegments().get(0).getDistance(), 0.0, "Single response should return valid RouteResult (segments[0].distance = 1452977.2)");
        assertEquals(0.85, result.getSegments().get(0).getDetourFactor(), 0.0, "Single response should return valid RouteResult (segments[0].detourFactor = 2)");
        assertEquals(2, result.getSegments().get(0).getSteps().size(), "Single response should return valid RouteResult (segments[0].steps.size = 2)");
        assertEquals("Instruction 1", result.getSegments().get(0).getSteps().get(0).getName(), "Single response should return valid RouteResult (segments[0].steps[0].name = 'Instruction 1')");
        assertEquals(11, result.getSegments().get(0).getSteps().get(0).getType(), "Single response should return valid RouteResult (segments[0].steps[0].type = 11)");
        assertEquals(44, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter(), "Single response should return valid RouteResult (segments[0].steps[0].maneuver.bearingAfter = 44)");
        assertEquals("Instruction 2", result.getSegments().get(0).getSteps().get(1).getName(), "Single response should return valid RouteResult (segments[0].steps[1].name = 'Instruction 2')");
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
        RouteResultBuilder builder = new RouteResultBuilder();
        extrasList.add(new RouteExtraInfo(OSM_ID));
        responseList.add(constructResponse(request1));
        responseList.add(constructResponse(request2));
        //noinspection unchecked
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, request1, new List[]{extrasList});

        assertEquals(2809674.1, result.getSummary().getDistance(), 0.0, "Two responses should return merged RouteResult (summary.duration = 2809674.1)");
        assertEquals("45.6,67.8,12.3,34.5", result.getSummary().getBBox().toString(), "Two responses should return merged RouteResult (summary.bbox = 45.6,67.8,12.3,34.5)");
        assertEquals(3, result.getGeometry().length, "Two responses should return merged RouteResult (geometry.length = 3)");
        assertEquals("(45.6, 12.3, NaN)", result.getGeometry()[0].toString(), "Two responses should return merged RouteResult (geometry[0] = 45.6,12.3,NaN)");
        assertEquals("(56.7, 23.4, NaN)", result.getGeometry()[1].toString(), "Two responses should return merged RouteResult (geometry[1] = 56.7,23.4,NaN)");
        assertEquals("(67.8, 34.5, NaN)", result.getGeometry()[2].toString(), "Two responses should return merged RouteResult (geometry[2] = 67.8,34.5,NaN)");
        assertEquals(2, result.getSegments().size(), "Two responses should return merged RouteResult (segments.size = 2)");
        assertEquals(1452977.2, result.getSegments().get(0).getDistance(), 0.0, "Two responses should return merged RouteResult (segments[0].distance = 1452977.2)");
        assertEquals(0.85, result.getSegments().get(0).getDetourFactor(), 0.0, "Two responses should return merged RouteResult (segments[0].detourFactor = 0.85)");
        assertEquals(2, result.getSegments().get(0).getSteps().size(), "Two responses should return merged RouteResult (segments[0].steps.size = 2)");
        assertEquals("Instruction 1", result.getSegments().get(0).getSteps().get(0).getName(), "Two responses should return merged RouteResult (segments[0].steps[0].name = 'Instruction 1')");
        assertEquals(11, result.getSegments().get(0).getSteps().get(0).getType(), "Two responses should return merged RouteResult (segments[0].steps[0].type = 11)");
        assertEquals(44, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[0].steps[0].maneuver.bearingAfter = 44)");
        assertEquals("Instruction 2", result.getSegments().get(0).getSteps().get(1).getName(), "Two responses should return merged RouteResult (segments[0].steps[1].name = 'Instruction 2')");
        assertEquals(10, result.getSegments().get(0).getSteps().get(1).getType(), "Two responses should return merged RouteResult (segments[0].steps[1].type = 10)");
        assertEquals(0, result.getSegments().get(0).getSteps().get(1).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[0].steps[1].maneuver.bearingAfter = 0)");
        assertEquals(1356696.9, result.getSegments().get(1).getDistance(), 0.0, "Two responses should return merged RouteResult (segments[1].distance = 1356696.9)");
        assertEquals(0.83, result.getSegments().get(1).getDetourFactor(), 0.0, "Two responses should return merged RouteResult (segments[1].detourFactor = 0.83)");
        assertEquals(2, result.getSegments().get(1).getSteps().size(), "Two responses should return merged RouteResult (segments[1].steps.size = 2)");
        assertEquals("Instruction 1", result.getSegments().get(1).getSteps().get(0).getName(), "Two responses should return merged RouteResult (segments[1].steps[0].name = 'Instruction 1')");
        assertEquals(11, result.getSegments().get(1).getSteps().get(0).getType(), "Two responses should return merged RouteResult (segments[1].steps[0].type = 11)");
        assertEquals(41, result.getSegments().get(1).getSteps().get(0).getManeuver().getBearingAfter(), "Two responses should return merged RouteResult (segments[1].steps[0].maneuver.bearingAfter = 41)");
        assertEquals("Instruction 2", result.getSegments().get(1).getSteps().get(1).getName(), "Two responses should return merged RouteResult (segments[1].steps[1].name = 'Instruction 2')");
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
        RouteResultBuilder builder = new RouteResultBuilder();
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
