/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package heigit.ors.routing;

import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.ArrivalDirection;
import heigit.ors.common.CardinalDirection;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.routing.instructions.InstructionTranslator;
import heigit.ors.routing.instructions.InstructionTranslatorsCache;
import heigit.ors.routing.instructions.InstructionType;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;

import java.util.List;

class RouteResultBuilder
{
	private AngleCalc angleCalc;
	private DistanceCalc distCalc;
	private static final CardinalDirection[] directions = {CardinalDirection.North, CardinalDirection.NorthEast, CardinalDirection.East, CardinalDirection.SouthEast, CardinalDirection.South, CardinalDirection.SouthWest, CardinalDirection.West, CardinalDirection.NorthWest};
    private int startWayPointIndex = 0;

	RouteResultBuilder()
	{
		angleCalc = new AngleCalc();
		distCalc = new DistanceCalcEarth();
	}

    RouteResult createMergedRouteResultFromBestPaths(List<GHResponse> responses, RoutingRequest request, List<RouteExtraInfo> extras) throws Exception {
        RouteResult result = new RouteResult(request.getExtraInfo());
        result.addExtras(request, extras);

        int allPointsSize = 0;
        for (GHResponse resp : responses) {
            allPointsSize =+ resp.getBest().getPoints().size();
        };
        PointList pointsToAdd = new PointList(allPointsSize, false);

        if (request.getSkipSegments() != null && !request.getSkipSegments().isEmpty()) {
            result.addWarning(new RouteWarning(RouteWarning.SKIPPED_SEGMENTS));
        }

        startWayPointIndex = 0;
        if (request.getIncludeGeometry()) {
            result.addWayPointIndex(0);
        }

        for (int ri = 0; ri < responses.size(); ++ri) {
            GHResponse response = responses.get(ri);
            if (response.hasErrors())
                throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", ri, FormatUtility.formatCoordinate(request.getCoordinates()[ri]), ri + 1, FormatUtility.formatCoordinate(request.getCoordinates()[ri + 1])));

            PathWrapper path = response.getBest();
            pointsToAdd.add(path.getPoints());

            if (request.getIncludeGeometry()) {
                result.addPointsToGeometry(path.getPoints(), ri > 0, request.getIncludeElevation());
                result.addWayPointIndex(result.getGeometry().length - 1);
            }

            result.addSegment(createRouteSegment(path, request, getNextResponseFirstStepPoints(responses, ri)));
        }

        result.addPointlist(pointsToAdd);
        result.calculateRouteSummary(request);
        if (!request.getIncludeInstructions()) {
            result.resetSegments();
        }

        return result;
    }

    private PointList getNextResponseFirstStepPoints(List<GHResponse> routes, int ri) {
        if (ri + 1 >= 0 && ri + 1 < routes.size()) {
            GHResponse resp = routes.get(ri + 1);
            InstructionList instructions = resp.getBest().getInstructions();
            if (!instructions.isEmpty())
                return instructions.get(0).getPoints();
        }
        return null;

    }

    private RouteSegment createRouteSegment(PathWrapper path, RoutingRequest request, PointList nextRouteFirstStepPoints) throws Exception {
        RouteSegment seg = new RouteSegment(path, request.getUnits());

        if (request.getIncludeInstructions()) {
            if (request.hasAttribute(RoutingRequest.ATTR_DETOURFACTOR)) {
                seg.setDetourFactor(FormatUtility.roundToDecimals(calculateDetourFactor(path), 2));
            }

            InstructionList instructions = path.getInstructions();

            int nInstructions = instructions.size();
            InstructionTranslator instrTranslator = InstructionTranslatorsCache.getInstance().getTranslator(request.getLanguage());
            for (int ii = 0; ii < nInstructions; ++ii) {
                RouteStep step = new RouteStep();

                Instruction instr = instructions.get(ii);
                InstructionType instrType = getInstructionType(ii == 0, instr);

                PointList currentStepPoints = instr.getPoints();
                PointList nextStepPoints = (ii + 1 < nInstructions) ? instructions.get(ii + 1).getPoints() : nextRouteFirstStepPoints;
                PointList prevStepPoints = ii > 0 ? instructions.get(ii - 1).getPoints() : null;

                step.setName(instr.getName());

                double stepDistance = DistanceUnitUtil.convert(instr.getDistance(), DistanceUnit.Meters, request.getUnits());
                step.setDistance(FormatUtility.roundToDecimalsForUnits(stepDistance, request.getUnits()));

                step.setDuration(FormatUtility.roundToDecimals(instr.getTime() / 1000.0, 1));

                if (request.getIncludeManeuvers() || instrType.isSlightLeftOrRight()) {
                    RouteStepManeuver maneuver = calcManeuver(instrType, prevStepPoints, currentStepPoints, nextStepPoints);
                    if (request.getIncludeManeuvers()) {
                        step.setManeuver(maneuver);
                    }
                    if (instrType.isSlightLeftOrRight() && maneuver.isContinue()) {
                        // see com.graphhopper.routing.InstructionsFromEdges.getTurn(...)
                        // is generating the TurnInformation - for what EVER reason this
                        // is not correct from time to time - so I ADJUST THEM!
                        instrType = InstructionType.CONTINUE;
                    }
                }

                step.setType(instrType.ordinal());

                String instrText;
                String roadName = instr.getName();
                if (request.getInstructionsFormat() == RouteInstructionsFormat.HTML && !Helper.isEmpty(instr.getName()) )
                    roadName = "<b>" + instr.getName() + "</b>";
                if (ii == 0) {
                    double lat;
                    double lon;
                    if (currentStepPoints.size() == 1) {
                        if (nextStepPoints != null) {
                            lat = nextStepPoints.getLat(0);
                            lon = nextStepPoints.getLon(0);
                        } else {
                            lat = currentStepPoints.getLat(0);
                            lon = currentStepPoints.getLon(0);
                        }
                    } else {
                        lat = currentStepPoints.getLat(1);
                        lon = currentStepPoints.getLon(1);
                    }
                    instrText = instrTranslator.getDepart(calcDirection(currentStepPoints.getLat(0), currentStepPoints.getLon(0), lat, lon), roadName);
                } else {
                    if (instr instanceof RoundaboutInstruction) {
                        RoundaboutInstruction raInstr = (RoundaboutInstruction) instr;
                        step.setExitNumber(raInstr.getExitNumber());
                        instrText = instrTranslator.getRoundabout(raInstr.getExitNumber(), roadName);
                    } else {
                        if (isTurnInstruction(instrType)) {
                            instrText = instrTranslator.getTurn(instrType, roadName);
                        } else if (isKeepInstruction(instrType)) {
                            instrText = instrTranslator.getKeep(instrType, roadName);
                        } else if (instrType == InstructionType.CONTINUE) {
                            instrText = instrTranslator.getContinue(instrType, roadName);
                        } else if (instrType == InstructionType.FINISH) {
                            String lastInstrName = instructions.get(ii - 1).getName();
                            instrText = instrTranslator.getArrive(getArrivalDirection(path.getPoints(), request.getDestination()), lastInstrName);
                        } else
                            instrText = "Unknown instruction type!";
                    }
                }
                step.setInstruction(instrText);

                int endWayPointIndex = getEndWayPointIndex(startWayPointIndex, instrType, instr);
                step.setWayPoints(new int[]{startWayPointIndex, endWayPointIndex});
                startWayPointIndex = endWayPointIndex;

                seg.addStep(step);
            }
        }
        return seg;
    }

    private double calculateDetourFactor(PathWrapper path) {
        PointList pathPoints = path.getPoints();
        double lat0 = pathPoints.getLat(0);
        double lon0 = pathPoints.getLon(0);
        double lat1 = pathPoints.getLat(pathPoints.getSize() - 1);
        double lon1 = pathPoints.getLon(pathPoints.getSize() - 1);
        double distanceDirect = distCalc.calcDist(lat0, lon0, lat1, lon1);
        if (distanceDirect == 0) return 0;
        return path.getDistance() / distanceDirect;

    }

    private ArrivalDirection getArrivalDirection(PointList points, Coordinate destination) {
        if (points.size() < 2)
            return ArrivalDirection.Unknown;

		int lastIndex = points.size() - 1;
		double lon0 = points.getLon(lastIndex - 1);
		double lat0 = points.getLat(lastIndex - 1);
		double lon1 = points.getLon(lastIndex);
		double lat1 = points.getLat(lastIndex);

		double dist = distCalc.calcDist(lat1, lon1, destination.y, destination.x);

		if (dist < 1)
			return ArrivalDirection.StraightAhead;
		else
		{
			double sign = Math.signum((lon1 - lon0) * (destination.y - lat0) - (lat1 - lat0) * (destination.x - lon0));
			if (sign == 0)
				return ArrivalDirection.StraightAhead;
			else if (sign == 1)
				return ArrivalDirection.Left;
			else
				return ArrivalDirection.Right;
		}
	}

	private int getEndWayPointIndex(int startIndex, InstructionType instrType, Instruction instr)
	{
		if (instrType == InstructionType.FINISH)
			return startIndex;
		else
			return startIndex + instr.getPoints().size();
	}

	private RouteStepManeuver calcManeuver(InstructionType instrType, PointList prevSegPoints, PointList segPoints, PointList nextSegPoints)
	{
		RouteStepManeuver maneuver = new RouteStepManeuver();
        maneuver.setBearingBefore(0);
        maneuver.setBearingAfter(0);
		if (nextSegPoints == null) {
            return maneuver;
		}
        if (instrType == InstructionType.DEPART) {
            double lon0 = segPoints.getLon(0);
            double lat0 = segPoints.getLat(0);
            maneuver.setLocation(new Coordinate(lon0, lat0));
            double lon1;
            double lat1;
            if (segPoints.size() == 1) {
                lon1  = nextSegPoints.getLon(0);
                lat1  = nextSegPoints.getLat(0);
            } else {
                lon1  = segPoints.getLon(1);
                lat1  = segPoints.getLat(1);
            }
            maneuver.setBearingAfter((int)Math.round(angleCalc.calcAzimuth(lat0, lon0, lat1, lon1)));
        } else {
            int locIndex = prevSegPoints.size() - 1;
            double lon0 = prevSegPoints.getLon(locIndex);
            double lat0 = prevSegPoints.getLat(locIndex);
            double lon1 = segPoints.getLon(0);
            double lat1 = segPoints.getLat(0);
            maneuver.setLocation(new Coordinate(lon1, lat1));
            maneuver.setBearingBefore((int)Math.round(angleCalc.calcAzimuth(lat0, lon0, lat1, lon1)));
            if (instrType != InstructionType.FINISH) {
                double lon2;
                double lat2;
                if (segPoints.size() == 1) {
                    lon2 = nextSegPoints.getLon(0);
                    lat2 = nextSegPoints.getLat(0);
                } else {
                    lon2 = segPoints.getLon(1);
                    lat2 = segPoints.getLat(1);
                }
                maneuver.setBearingAfter((int)Math.round(angleCalc.calcAzimuth(lat1, lon1, lat2, lon2)));
            }
        }
        return maneuver;
	}


	private boolean isTurnInstruction(InstructionType instrType) {
		return instrType == InstructionType.TURN_LEFT || instrType == InstructionType.TURN_SLIGHT_LEFT
				|| instrType == InstructionType.TURN_SHARP_LEFT || instrType == InstructionType.TURN_RIGHT
				|| instrType == InstructionType.TURN_SLIGHT_RIGHT || instrType == InstructionType.TURN_SHARP_RIGHT;
	}

	private boolean isKeepInstruction(InstructionType instrType){
	    return instrType == InstructionType.KEEP_LEFT || instrType == InstructionType.KEEP_RIGHT;
    }

    private InstructionType getInstructionType(boolean isDepart, Instruction instr)
	{
		if (isDepart) {
			return InstructionType.DEPART;
		}

		switch (instr.getSign()){
			case Instruction.CONTINUE_ON_STREET:
				return InstructionType.CONTINUE;
			case Instruction.TURN_LEFT:
				return InstructionType.TURN_LEFT;
			case Instruction.TURN_RIGHT:
				return InstructionType.TURN_RIGHT;
			case Instruction.TURN_SHARP_LEFT:
				return InstructionType.TURN_SHARP_LEFT;
			case Instruction.TURN_SHARP_RIGHT:
				return InstructionType.TURN_SHARP_RIGHT;
			case Instruction.TURN_SLIGHT_LEFT:
				return InstructionType.TURN_SLIGHT_LEFT;
			case Instruction.TURN_SLIGHT_RIGHT:
				return InstructionType.TURN_SLIGHT_RIGHT;
			case Instruction.USE_ROUNDABOUT:
				return InstructionType.ENTER_ROUNDABOUT;
			case Instruction.LEAVE_ROUNDABOUT:
				return InstructionType.EXIT_ROUNDABOUT;
			case Instruction.FINISH:
				return InstructionType.FINISH;
			case Instruction.KEEP_LEFT:
				return InstructionType.KEEP_LEFT;
			case Instruction.KEEP_RIGHT:
				return InstructionType.KEEP_RIGHT;
			default:
				return InstructionType.CONTINUE;
		}
	}

	private CardinalDirection calcDirection(double lat1, double lon1, double lat2, double lon2 )
	{
		double orientation = - angleCalc.calcOrientation(lat1, lon1, lat2, lon2);
		orientation = Helper.round4(orientation + Math.PI / 2);
		if (orientation < 0)
			orientation += 2 * Math.PI;

		double degree = Math.toDegrees(orientation);
		return directions[(int)Math.floor(((degree+ 22.5) % 360) / 45)];
	}
}
