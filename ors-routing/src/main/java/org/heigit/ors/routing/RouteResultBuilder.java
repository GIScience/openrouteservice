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
package org.heigit.ors.routing;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.Trip;
import com.graphhopper.util.*;
import org.heigit.ors.common.ArrivalDirection;
import org.heigit.ors.common.CardinalDirection;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.instructions.InstructionTranslator;
import org.heigit.ors.routing.instructions.InstructionTranslatorsCache;
import org.heigit.ors.routing.instructions.InstructionType;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.FormatUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.heigit.ors.routing.RouteResult.*;

class RouteResultBuilder
{
	private final AngleCalc angleCalc;
	private final DistanceCalc distCalc;
	private static final CardinalDirection[] directions = {CardinalDirection.NORTH, CardinalDirection.NORTH_EAST, CardinalDirection.EAST, CardinalDirection.SOUTH_EAST, CardinalDirection.SOUTH, CardinalDirection.SOUTH_WEST, CardinalDirection.WEST, CardinalDirection.NORTH_WEST};
    private int startWayPointIndex = 0;

	RouteResultBuilder() {
		angleCalc = new AngleCalc();
		distCalc = new DistanceCalcEarth();
	}

    RouteResult[] createRouteResults(List<GHResponse> responses, RoutingRequest request, List<RouteExtraInfo>[] extras) throws Exception {
        if (responses.isEmpty())
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Unable to find a route.");
        if (responses.size() > 1) { // request had multiple segments (route with via points)
            return createMergedRouteResultSetFromBestPaths(responses, request, extras);
        } else
            return createRouteResultSetFromMultiplePaths(responses.get(0), request, extras);
    }

    private RouteResult createInitialRouteResult (RoutingRequest request, List<RouteExtraInfo> extras) {
        RouteResult result = new RouteResult(request.getExtraInfo());

        result.addExtras(request, extras);

        if (request.getSkipSegments() != null && !request.getSkipSegments().isEmpty()) {
            result.addWarning(new RouteWarning(RouteWarning.SKIPPED_SEGMENTS));
        }

        startWayPointIndex = 0;

        if (request.getIncludeGeometry()) {
            result.addWayPointIndex(0);
        }
        return result;
    }

    RouteResult createMergedRouteResultFromBestPaths(List<GHResponse> responses, RoutingRequest request, List<RouteExtraInfo>[] extras) throws Exception {
        RouteResult result = createInitialRouteResult(request, extras[0]);

        for (int ri = 0; ri < responses.size(); ++ri) {
            GHResponse response = responses.get(ri);
            if (response.hasErrors())
                throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", ri, FormatUtility.formatCoordinate(request.getCoordinates()[ri]), ri + 1, FormatUtility.formatCoordinate(request.getCoordinates()[ri + 1])));

            handleResponseWarnings(result, response);

            ResponsePath path = response.getBest();

            result.addPointlist(path.getPoints());

            if (request.getIncludeGeometry()) {
                result.addPointsToGeometry(path.getPoints(), ri > 0, request.getIncludeElevation());
                result.addWayPointIndex(result.getGeometry().length - 1);
            }

            result.addSegment(createRouteSegment(path, request, getNextResponseFirstStepPoints(responses, ri)));
            result.setGraphDate(response.getHints().getString("data.date", "0000-00-00T00:00:00Z"));
        }

        result.calculateRouteSummary(request);

        if (request.getSearchParameters().isTimeDependent()) {
            String timezoneDeparture = responses.get(0).getHints().getString(KEY_TIMEZONE_DEPARTURE, DEFAULT_TIMEZONE);
            String timezoneArrival = responses.get(responses.size()-1).getHints().getString(KEY_TIMEZONE_ARRIVAL, DEFAULT_TIMEZONE);

            setDepartureArrivalTimes(timezoneDeparture, timezoneArrival, request, result);
        }

        if (!request.getIncludeInstructions()) {
            result.resetSegments();
        }

        return result;
    }

    private RouteResult[] createMergedRouteResultSetFromBestPaths(List<GHResponse> responses, RoutingRequest request, List<RouteExtraInfo>[] extras) throws Exception {
        return new RouteResult[]{createMergedRouteResultFromBestPaths(responses, request, extras)};
    }

    private RouteResult[] createRouteResultSetFromMultiplePaths(GHResponse response, RoutingRequest request, List<RouteExtraInfo>[] extras) throws Exception {
        if (response.hasErrors())
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", 0, FormatUtility.formatCoordinate(request.getCoordinates()[0]), 1, FormatUtility.formatCoordinate(request.getCoordinates()[1])));

        RouteResult[] resultSet = new RouteResult[response.getAll().size()];

        int pathIndex = 0;
        for (ResponsePath path : response.getAll()) {
            List<RouteExtraInfo> extraList = extras.length == response.getAll().size() ? extras[pathIndex] : extras[0];
            RouteResult result = createInitialRouteResult(request, extraList);

            handleResponseWarnings(result, response);

            result.addPointlist(path.getPoints());
            if (request.getIncludeGeometry()) {
                result.addPointsToGeometry(path.getPoints(), false, request.getIncludeElevation());
                result.addWayPointIndex(result.getGeometry().length - 1);
            }

            result.addSegment(createRouteSegment(path, request, null));

            if (request.getSearchParameters().getProfileType() == RoutingProfileType.PUBLIC_TRANSPORT) {
                addLegsToRouteResult(result, request, path.getLegs(), response);
            }

            result.calculateRouteSummary(request, path);
            if (!request.getIncludeInstructions()) {
                result.resetSegments();
            }

            result.setGraphDate(response.getHints().getString("data.date", "0000-00-00T00:00:00Z"));
            resultSet[response.getAll().indexOf(path)] = result;

            if (request.getSearchParameters().isTimeDependent()) {
                String timezoneDeparture = response.getHints().getString(KEY_TIMEZONE_DEPARTURE, DEFAULT_TIMEZONE);
                String timezoneArrival = response.getHints().getString(KEY_TIMEZONE_ARRIVAL, DEFAULT_TIMEZONE);

                setDepartureArrivalTimes(timezoneDeparture, timezoneArrival, request, result);
            }

            pathIndex++;
        }

        return resultSet;
    }

    private void setDepartureArrivalTimes(String timezoneDeparture, String timezoneArrival, RoutingRequest request, RouteResult result) {
        ZonedDateTime departure;
        ZonedDateTime arrival;

        long duration = (long) result.getSummary().getDuration();
        if (request.getSearchParameters().hasDeparture()) {
            ZonedDateTime zonedDateTime = request.getSearchParameters().getDeparture().atZone(ZoneId.of(timezoneDeparture));
            departure = zonedDateTime;
            arrival = zonedDateTime.plusSeconds(duration);
        } else {
            ZonedDateTime zonedDateTime = request.getSearchParameters().getArrival().atZone(ZoneId.of(timezoneArrival));
            arrival = zonedDateTime;
            departure = zonedDateTime.minusSeconds(duration);
        }

        result.setDeparture(departure);
        result.setArrival(arrival);
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

    private RouteSegment createRouteSegment(ResponsePath path, RoutingRequest request, PointList nextRouteFirstStepPoints) throws Exception {
        RouteSegment seg = new RouteSegment(path, request.getUnits());

        if (request.getIncludeInstructions()) {
            if (request.hasAttribute(RoutingRequest.ATTR_DETOURFACTOR)) {
                seg.setDetourFactor(FormatUtility.roundToDecimals(calculateDetourFactor(path), 2));
            }
            seg.addSteps(convertRouteSteps(path.getInstructions(), path.getPoints(), request, nextRouteFirstStepPoints));
        }
        return seg;
    }

    private void addLegsToRouteResult(RouteResult result, RoutingRequest request, List<Trip.Leg> legs, GHResponse response) throws Exception {
        for (Trip.Leg leg : legs) {
            startWayPointIndex = 0;
            List<RouteStep> instructions = leg instanceof Trip.WalkLeg ? convertRouteSteps(((Trip.WalkLeg)leg).instructions, PointList.from((LineString)leg.geometry), request, null) : null;
            result.addLeg(new RouteLeg(leg, instructions, response, request));
        }
    }

    private List<RouteStep> convertRouteSteps(InstructionList instructions, PointList points, RoutingRequest request, PointList nextRouteFirstStepPoints) throws Exception{
        List<RouteStep> result = new ArrayList<>();
        int nInstructions = instructions.size();
        InstructionTranslator instrTranslator = InstructionTranslatorsCache.getInstance().getTranslator(request.getLanguage());
        for (int ii = 0; ii < nInstructions; ++ii) {
            RouteStep step = new RouteStep();

            Instruction instr = instructions.get(ii);
            if (instr instanceof ViaInstruction && request.isRoundTripRequest()) {
                // if this is a via instruction, then we don't want to process it in the case of a round trip
                continue;
            }
            InstructionType instrType = getInstructionType(ii == 0, instr);

            PointList currentStepPoints = instr.getPoints();
            PointList nextStepPoints = (ii + 1 < nInstructions) ? instructions.get(ii + 1).getPoints() : nextRouteFirstStepPoints;
            PointList prevStepPoints = ii > 0 ? instructions.get(ii - 1).getPoints() : null;

            step.setName(instr.getName());

            double stepDistance = DistanceUnitUtil.convert(instr.getDistance(), DistanceUnit.METERS, request.getUnits());
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
            if (request.getInstructionsFormat() == RouteInstructionsFormat.HTML && !Helper.isEmpty(instr.getName()))
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
                    } else if (instrType == InstructionType.PT_ENTER) {
                        instrText = instrTranslator.getPt(instrType, roadName, instr.getHeadsign());
                    } else if (instrType == InstructionType.PT_TRANSFER) {
                        instrText = instrTranslator.getPt(instrType, roadName, instr.getHeadsign());
                    } else if (instrType == InstructionType.PT_EXIT) {
                        instrText = instrTranslator.getPt(instrType, roadName);
                    } else if (instrType == InstructionType.CONTINUE) {
                        instrText = instrTranslator.getContinue(instrType, roadName);
                    } else if (instrType == InstructionType.FINISH) {
                        String lastInstrName = instructions.get(ii - 1).getName();
                        instrText = instrTranslator.getArrive(getArrivalDirection(points, request.getDestination()), lastInstrName);
                    } else
                        instrText = "Unknown instruction type!";
                }
            }
            step.setInstruction(instrText);

            int endWayPointIndex = getEndWayPointIndex(startWayPointIndex, instrType, instr);
            step.setWayPoints(new int[]{startWayPointIndex, endWayPointIndex});
            startWayPointIndex = endWayPointIndex;
            result.add(step);
        }
        return result;
    }

    private double calculateDetourFactor(ResponsePath path) {
        PointList pathPoints = path.getPoints();
        double lat0 = pathPoints.getLat(0);
        double lon0 = pathPoints.getLon(0);
        double lat1 = pathPoints.getLat(pathPoints.size() - 1);
        double lon1 = pathPoints.getLon(pathPoints.size() - 1);
        double distanceDirect = distCalc.calcDist(lat0, lon0, lat1, lon1);
        if (distanceDirect == 0) return 0;
        return path.getDistance() / distanceDirect;

    }

    private ArrivalDirection getArrivalDirection(PointList points, Coordinate destination) {
        if (points.size() < 2)
            return ArrivalDirection.UNKNOWN;

		int lastIndex = points.size() - 1;
		double lon0 = points.getLon(lastIndex - 1);
		double lat0 = points.getLat(lastIndex - 1);
		double lon1 = points.getLon(lastIndex);
		double lat1 = points.getLat(lastIndex);

		double dist = distCalc.calcDist(lat1, lon1, destination.y, destination.x);

		if (dist < 1)
			return ArrivalDirection.STRAIGHT_AHEAD;
		else
		{
			double sign = Math.signum((lon1 - lon0) * (destination.y - lat0) - (lat1 - lat0) * (destination.x - lon0));
			if (sign == 0)
				return ArrivalDirection.STRAIGHT_AHEAD;
			else if (sign == 1)
				return ArrivalDirection.LEFT;
			else
				return ArrivalDirection.RIGHT;
		}
	}

	private int getEndWayPointIndex(int startIndex, InstructionType instrType, Instruction instr) {
		if (instrType == InstructionType.FINISH
                // "empty" departure instruction means start and end coordinates are the same, index should not increase
                || (instrType == InstructionType.DEPART && instr.getDistance() == 0.0 && instr.getPoints().size() == 1)
            )
			return startIndex;
		else
			return startIndex + instr.getPoints().size();
	}

	private RouteStepManeuver calcManeuver(InstructionType instrType, PointList prevSegPoints, PointList segPoints, PointList nextSegPoints) {
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
        } else if (prevSegPoints.size() > 0) {
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

    private InstructionType getInstructionType(boolean isDepart, Instruction instr)	{
		if (isDepart) {
			return InstructionType.DEPART;
		}

		switch (instr.getSign()){
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
            case Instruction.PT_START_TRIP:
                return InstructionType.PT_ENTER;
            case Instruction.PT_TRANSFER:
                return InstructionType.PT_TRANSFER;
            case Instruction.PT_END_TRIP:
                return InstructionType.PT_EXIT;
            case Instruction.CONTINUE_ON_STREET:
            default:
				return InstructionType.CONTINUE;
		}
	}

	private CardinalDirection calcDirection(double lat1, double lon1, double lat2, double lon2 ) {
		double orientation = - angleCalc.calcOrientation(lat1, lon1, lat2, lon2);
		orientation = Helper.round4(orientation + Math.PI / 2);
		if (orientation < 0)
			orientation += 2 * Math.PI;

		double degree = Math.toDegrees(orientation);
		return directions[(int)Math.floor(((degree+ 22.5) % 360) / 45)];
	}

    private void handleResponseWarnings(RouteResult result, GHResponse response) {
        String skippedExtras = response.getHints().getString("skipped_extra_info", "");
        if (!skippedExtras.isEmpty()) {
            result.addWarning(new RouteWarning(RouteWarning.SKIPPED_EXTRAS, skippedExtras));
        }
    }
}
