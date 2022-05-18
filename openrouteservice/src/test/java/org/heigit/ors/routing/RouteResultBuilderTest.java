package org.heigit.ors.routing;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;
import org.heigit.ors.util.CoordTools;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class RouteResultBuilderTest {
    private RoutingRequest request1;
    private RoutingRequest request2;


    public RouteResultBuilderTest() throws Exception {
        init();
    }

    @Before
    public void init() throws Exception {
        Coordinate[] coords1 = new Coordinate[] {new Coordinate(12.3,45.6), new Coordinate(23.4,56.7)};
        request1 = createRoutingRequest(coords1);
        Coordinate[] coords2 = new Coordinate[] {new Coordinate(23.4,56.7), new Coordinate (34.5,67.8)};
        request2 = createRoutingRequest(coords2);
    }

    private RoutingRequest createRoutingRequest(Coordinate[] coords) {
        RoutingRequest routingRequest = new RoutingRequest();
        routingRequest.setCoordinates(coords);
        routingRequest.setAttributes(new String[] {"detourfactor"});
        int extraInfoFlags = RouteExtraInfoFlag.getFromString("osmid");
        routingRequest.setExtraInfo(extraInfoFlags);
        routingRequest.setGeometryFormat("encodedpolyline");
        routingRequest.setIncludeGeometry(true);
        routingRequest.setIncludeInstructions(true);
        routingRequest.setIncludeRoundaboutExits(true);
        routingRequest.setIncludeManeuvers(true);
        routingRequest.setLanguage("de");
        routingRequest.setInstructionsFormat(RouteInstructionsFormat.HTML);
        routingRequest.setIncludeElevation(true);

        RouteSearchParameters params = new RouteSearchParameters();
        try {
            params.setProfileType(RoutingProfileType.getFromString("driving-car"));
        } catch (Exception e) {
            // TODO: throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        }
        params.setExtraInfo(extraInfoFlags);
        params.setWeightingMethod(WeightingMethod.FASTEST);
        params.setAvoidBorders(BordersExtractor.Avoid.CONTROLLED);
        params.setConsiderTurnRestrictions(false);

        routingRequest.setSearchParameters(params);

        return routingRequest;
    }
    private GHResponse constructResponse(Coordinate[] coords) {
        // TODO: Why are the coordinates put into a LineString just to pull
        //       them out again? Can't this be simplified?
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
        response.getHints().put("skipped_segment", "true");
        return response;
    }

    @Test
    public void TestCreateMergedRouteResultFromBestPaths() throws Exception {
        List<GHResponse> responseList = new ArrayList<>();

        RoutingRequest routingRequest = request1;

        List<RouteExtraInfo> extrasList = new ArrayList<>();

        RouteResultBuilder builder = new RouteResultBuilder();
        RouteResult result = builder.createMergedRouteResultFromBestPaths(responseList, routingRequest, new List[]{extrasList});
        Assert.assertEquals("Empty response list should return empty RouteResult (summary.distance = 0.0)", 0.0, result.getSummary().getDistance(), 0.0);
        Assert.assertEquals("Empty response list should return empty RouteResult (no segments)", 0, result.getSegments().size());
        Assert.assertEquals("Empty response list should return empty RouteResult (no extra info)", 0, result.getExtraInfo().size());
        Assert.assertNull("Empty response list should return empty RouteResult (no geometry)", result.getGeometry());

        extrasList.add(new RouteExtraInfo("osmid"));

        responseList.add(constructResponse(request1.getCoordinates()));
        result = builder.createMergedRouteResultFromBestPaths(responseList, routingRequest, new List[]{extrasList});
        Assert.assertEquals("Single response should return valid RouteResult (summary.duration = 1452977.2)", 1452977.2, result.getSummary().getDistance(), 0.0);
        Assert.assertEquals("Single response should return valid RouteResult (summary.bbox = 45.6,56.7,12.3,23.4)", "45.6,56.7,12.3,23.4", result.getSummary().getBBox().toString());
        Assert.assertEquals("Single response should return valid RouteResult (geometry.length = 2)", 2, result.getGeometry().length);
        Assert.assertEquals("Single response should return valid RouteResult (geometry[0] = 45.6,12.3,NaN)", "(45.6, 12.3, NaN)", result.getGeometry()[0].toString());
        Assert.assertEquals("Single response should return valid RouteResult (geometry[1] = 56.7,23.4,NaN)", "(56.7, 23.4, NaN)", result.getGeometry()[1].toString());
        Assert.assertEquals("Single response should return valid RouteResult (segments.size = 1)", 1, result.getSegments().size());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].distance = 1452977.2)", 1452977.2, result.getSegments().get(0).getDistance(), 0.0);
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].detourFactor = 2)", 0.85, result.getSegments().get(0).getDetourFactor(), 0.0);
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps.size = 2)", 2, result.getSegments().get(0).getSteps().size());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[0].name = 'Instruction 1')", "Instruction 1", result.getSegments().get(0).getSteps().get(0).getName());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[0].type = 11)", 11, result.getSegments().get(0).getSteps().get(0).getType());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[0].maneuver.bearingAfter = 44)", 44, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[1].name = 'Instruction 2')", "Instruction 2", result.getSegments().get(0).getSteps().get(1).getName());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[1].type = 10)", 10, result.getSegments().get(0).getSteps().get(1).getType());
        Assert.assertEquals("Single response should return valid RouteResult (segments[0].steps[1].maneuver.bearingAfter = 0)", 0, result.getSegments().get(0).getSteps().get(1).getManeuver().getBearingAfter());
        Assert.assertEquals("Single response should return valid RouteResult (extrainfo.size = 1)", 1, result.getExtraInfo().size());
        Assert.assertEquals("Single response should return valid RouteResult (extrainfo[0].name = 'osmid)", "osmid", result.getExtraInfo().get(0).getName());
        Assert.assertEquals("Single response should return valid RouteResult (waypointindices.size = 2)", 2, result.getWayPointsIndices().size());

        responseList.add(constructResponse(request2.getCoordinates()));
        result = builder.createMergedRouteResultFromBestPaths(responseList, routingRequest, new List[]{extrasList}); // TODO: Why is not use request2 instead of routingRequest?
        Assert.assertEquals("Two responses should return merged RouteResult (summary.duration = 2809674.1)", 2809674.1, result.getSummary().getDistance(), 0.0);
        Assert.assertEquals("Two responses should return merged RouteResult (summary.bbox = 45.6,67.8,12.3,34.5)", "45.6,67.8,12.3,34.5", result.getSummary().getBBox().toString());
        Assert.assertEquals("Two responses should return merged RouteResult (geometry.length = 3)", 3, result.getGeometry().length);
        Assert.assertEquals("Two responses should return merged RouteResult (geometry[0] = 45.6,12.3,NaN)", "(45.6, 12.3, NaN)", result.getGeometry()[0].toString());
        Assert.assertEquals("Two responses should return merged RouteResult (geometry[1] = 56.7,23.4,NaN)", "(56.7, 23.4, NaN)", result.getGeometry()[1].toString());
        Assert.assertEquals("Two responses should return merged RouteResult (geometry[2] = 67.8,34.5,NaN)", "(67.8, 34.5, NaN)", result.getGeometry()[2].toString());
        Assert.assertEquals("Two responses should return merged RouteResult (segments.size = 2)", 2, result.getSegments().size());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].distance = 1452977.2)", 1452977.2, result.getSegments().get(0).getDistance(), 0.0);
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].detourFactor = 0.85)", 0.85, result.getSegments().get(0).getDetourFactor(), 0.0);
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps.size = 2)", 2, result.getSegments().get(0).getSteps().size());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[0].name = 'Instruction 1')", "Instruction 1", result.getSegments().get(0).getSteps().get(0).getName());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[0].type = 11)", 11, result.getSegments().get(0).getSteps().get(0).getType());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[0].maneuver.bearingAfter = 44)", 44, result.getSegments().get(0).getSteps().get(0).getManeuver().getBearingAfter());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[1].name = 'Instruction 2')", "Instruction 2", result.getSegments().get(0).getSteps().get(1).getName());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[1].type = 10)", 10, result.getSegments().get(0).getSteps().get(1).getType());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[0].steps[1].maneuver.bearingAfter = 0)", 0, result.getSegments().get(0).getSteps().get(1).getManeuver().getBearingAfter());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].distance = 1356696.9)", 1356696.9, result.getSegments().get(1).getDistance(), 0.0);
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].detourFactor = 0.83)", 0.83, result.getSegments().get(1).getDetourFactor(), 0.0);
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps.size = 2)", 2, result.getSegments().get(1).getSteps().size());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[0].name = 'Instruction 1')", "Instruction 1", result.getSegments().get(1).getSteps().get(0).getName());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[0].type = 11)", 11, result.getSegments().get(1).getSteps().get(0).getType());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[0].maneuver.bearingAfter = 41)", 41, result.getSegments().get(1).getSteps().get(0).getManeuver().getBearingAfter());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[1].name = 'Instruction 2')", "Instruction 2", result.getSegments().get(1).getSteps().get(1).getName());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[1].type = 10)", 10, result.getSegments().get(1).getSteps().get(1).getType());
        Assert.assertEquals("Two responses should return merged RouteResult (segments[1].steps[1].maneuver.bearingAfter = 0)", 0, result.getSegments().get(1).getSteps().get(1).getManeuver().getBearingAfter());
        Assert.assertEquals("Two responses should return merged RouteResult (extrainfo.size = 1)", 1, result.getExtraInfo().size());
        Assert.assertEquals("Two responses should return merged RouteResult (extrainfo[0].name = 'osmid)", "osmid", result.getExtraInfo().get(0).getName());
        Assert.assertEquals("Two responses should return merged RouteResult (waypointindices.size = 3)", 3, result.getWayPointsIndices().size());

        RoutingRequest modRequest = createRoutingRequest(request1.getCoordinates());
        List<Integer> skipSegments = new ArrayList<>();
        skipSegments.add(1);
        modRequest.setSkipSegments(skipSegments);
        responseList = new ArrayList<>();
        responseList.add(constructResponse(modRequest.getCoordinates()));
        result = builder.createMergedRouteResultFromBestPaths(responseList, modRequest, new List[]{extrasList});
        Assert.assertEquals("Response with SkipSegments should return RouteResult with warning", 1, result.getWarnings().size());
        Assert.assertEquals("Response with SkipSegments should return RouteResult with warning (code 3)", 3, result.getWarnings().get(0).getWarningCode());
    }
}
