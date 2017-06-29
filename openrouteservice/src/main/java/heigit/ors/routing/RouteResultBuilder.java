/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing;

import java.util.List;

import com.graphhopper.GHResponse;
import com.graphhopper.util.AngleCalc;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.PointList;
import com.graphhopper.util.RoundaboutInstruction;
import com.graphhopper.util.shapes.BBox;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.localization.LocalizationManager;
import heigit.ors.routing.instructions.InstructionTranslator;
import heigit.ors.routing.instructions.InstructionTranslatorsCache;
import heigit.ors.routing.instructions.InstructionType;
import heigit.ors.services.routing.RouteInstructionsFormat;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.util.CardinalDirection;
import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.FormatUtility;
import heigit.ors.util.StringUtility;

public class RouteResultBuilder 
{
	private AngleCalc _angleCalc;
	private DistanceCalc _distCalc;
	private String nameAppendix;
	private static final CardinalDirection _directions[] = {CardinalDirection.North, CardinalDirection.NorthEast, CardinalDirection.East, CardinalDirection.SouthEast, CardinalDirection.South, CardinalDirection.SouthWest, CardinalDirection.West, CardinalDirection.NorthWest};

	public RouteResultBuilder()
	{
		_angleCalc = new AngleCalc();
		_distCalc = new DistanceCalcEarth();
	}

	public RouteResult createRouteResult(List<GHResponse> routes, RoutingRequest request, List<RouteExtraInfo> extras) throws Exception
	{
		RouteResult result = new RouteResult(request.getExtraInfo());

		if (routes.isEmpty())
			return result;

		if(!LocalizationManager.getInstance().isLanguageSupported(request.getLanguage()))
			throw new Exception("Specified language '" +  request.getLanguage() + "' is not supported.");

		InstructionTranslator instrTranslator = InstructionTranslatorsCache.getInstance().getTranslator(request.getLanguage());

		boolean formatInstructions = request.getInstructionsFormat() == RouteInstructionsFormat.HTML;
		int nRoutes = routes.size();
		double distance = 0.0;
		double duration = 0.0;
		double ascent = 0.0;
		double descent = 0.0;
		double distanceActual = 0.0;
		double durationTraffic = 0.0;

		double lon0 = 0, lat0 = 0, lat1 = 0, lon1 = 0;
		boolean includeDetourFactor = request.hasAttribute("detourfactor");
		boolean includeElev = request.getIncludeElevation();
		DistanceUnit units = request.getUnits();
		int unitDecimals = FormatUtility.getUnitDecimals(units);
		
		BBox bbox = null; 
		int[] routeWayPoints = null;

		if (request.getIncludeGeometry())
		{
			routeWayPoints = new int[nRoutes + 1]; 
			routeWayPoints[0] = 0;
		}
		
		if (extras != null)
			result.addExtraInfo(extras);

		for (int ri = 0; ri < nRoutes; ++ri)
		{
			GHResponse resp = routes.get(ri);

			if (resp.hasErrors())
				throw new InternalServerException(RoutingErrorCodes.UNKNOWN, String.format("Unable to find a route between points %d (%s) and %d (%s)", ri, FormatUtility.formatCoordinate(request.getCoordinates()[ri]), ri + 1, FormatUtility.formatCoordinate(request.getCoordinates()[ri+1])));

			PointList routePoints = resp.getPoints();

			if (bbox == null)
				bbox = new BBox(routePoints.getLon(0), routePoints.getLon(0), routePoints.getLat(0), routePoints.getLat(0));
			bbox = resp.calcRouteBBox(bbox);

			if (request.getIncludeGeometry())
			{
				result.addPoints(routePoints, ri > 0, includeElev);

				routeWayPoints[ri + 1] = result.getGeometry().length - 1;

				if (request.getIncludeInstructions())
				{
					int startWayPointIndex = routeWayPoints[ri];
					int nInstructions = resp.getInstructions().getSize(); 
					if (nInstructions > 1) // last is finishinstruction
						nInstructions -= 1;

					Instruction instr, prevInstr = null;
					InstructionType instrType, prevInstrType = InstructionType.UNKNOWN;
					RouteSegment seg = new RouteSegment(resp, units);
					
					if (includeDetourFactor)
					{
                        lat0 = routePoints.getLat(0);
                        lon0 = routePoints.getLon(0);
                        
                        lat1 = routePoints.getLat(routePoints.getSize() - 1);
                        lon1 = routePoints.getLon(routePoints.getSize() - 1);
                        
						seg.setDetourFactor(FormatUtility.roundToDecimals(_distCalc.calcDist(lat0, lon0, lat1, lon1)/ resp.getDistance(), 2));
					}
					
					RouteStep prevStep = null;
					String instrText = "";
					double stepDistance, stepDuration;

					for (int ii = 0; ii < nInstructions; ++ii) 
					{
						instr = resp.getInstructions().get(ii);
						InstructionAnnotation instrAnnotation = instr.getAnnotation();
						instrType = getInstructionType(instr);
						PointList segPoints = instr.getPoints();
						String roadName = formatInstructions && !Helper.isEmpty(instr.getName()) ? "<b>" + instr.getName() + "</b>" : instr.getName();
						instrText = "";

						stepDistance = FormatUtility.roundToDecimals(DistanceUnitUtil.convert(instr.getDistance(), DistanceUnit.Meters, units), unitDecimals);
						stepDuration = FormatUtility.roundToDecimals(instr.getTime()/1000.0, 1); 

						RouteStep step = new RouteStep();

						if (ii == 0)
						{
							if (segPoints.size() == 1)
							{
                                if (ii + 1 < nInstructions)
                                {
                                	PointList nextSegPoints = resp.getInstructions().get(ii+1).getPoints();
                                	lat1 = nextSegPoints.getLat(0);
    								lon1 = nextSegPoints.getLon(0);
                                }
                                else
                                {
                                	lat1 = segPoints.getLat(ii);
                                	lon1 = segPoints.getLon(ii);
                                }
							}
							else
							{
								lat1 = segPoints.getLat(ii+1);
								lon1 = segPoints.getLon(ii+1);
							}
							
							CardinalDirection dir = calcDirection(segPoints.getLat(ii), segPoints.getLon(ii), lat1, lon1);
							instrText = instrTranslator.getDepart(dir, roadName);
						}
						else
						{
							if (instr instanceof RoundaboutInstruction)
							{
								RoundaboutInstruction raInstr = (RoundaboutInstruction)instr;
								step.setExitNumber(raInstr.getExitNumber());
								instrText = instrTranslator.getRoundabout(raInstr.getExitNumber(), roadName);
							}
							else
							{
								if (isTurnInstruction(instrType))
									instrText = instrTranslator.getTurn(instrType, roadName);
								else if (instrType == InstructionType.CONTINUE)
									instrText = instrTranslator.getContinue(instrType, roadName);
								else
									instrText = "Oops! Fix me";
							}
						}


						// merge route steps with similar names 
						// example: http://localhost:8082/openrouteservice-4.0.0/routes?profile=driving-car&coordinates=8.690614,49.38365|8.7007,49.411699|8.7107,49.4516&prettify_instructions=true
						if (prevStep != null &&  instrType == InstructionType.CONTINUE && instrType == prevInstrType && canMergeInstructions(instr.getName(), prevInstr.getName()))
						{
							roadName = mergeInstructions(instr.getName(), prevInstr.getName());
							if (nameAppendix != null)
								roadName += " ("+ nameAppendix + ")";
							if (formatInstructions)
								roadName = "<b>" + roadName + "</b>";

							int[] wayPoints = prevStep.getWayPoints();
							wayPoints[1] = wayPoints[1] + instr.getPoints().size();

							
							stepDuration = FormatUtility.roundToDecimals(instr.getTime()/1000.0, 1); 

							prevStep.setDistance(FormatUtility.roundToDecimals(DistanceUnitUtil.convert(prevStep.getDistance() +  stepDistance, DistanceUnit.Meters, units), unitDecimals));
							prevStep.setDuration(FormatUtility.roundToDecimals(prevStep.getDuration() +  stepDuration, 1));
							prevStep.setInstruction(instrTranslator.getContinue(instrType, roadName));
						}
						else
						{
							nameAppendix = null;

							step.setDistance(stepDistance);
							step.setDuration(stepDuration);
							step.setInstruction(instrText);
							//step.setName(instr.getName());
							step.setType(instrType.ordinal());
							step.setWayPoints(new int[] { startWayPointIndex, startWayPointIndex + instr.getPoints().size()});

							seg.addStep(step);

							prevStep = step;
						}

						// step.setMessage(message);
						// add message and message type

						startWayPointIndex += instr.getPoints().size();
						//step.setMode // walking, cycling, etc. for multimodal routing


						if (instrAnnotation != null && instrAnnotation.getWayType() != 1) // Ferry, Steps as pushing sections
							distanceActual += stepDistance;

						prevInstr = instr;
						prevInstrType = instrType;
					}

					result.getSegments().add(seg);

					distance += seg.getDistance();
					duration += seg.getDuration();
				}
				else
				{
					distance += FormatUtility.roundToDecimals(DistanceUnitUtil.convert(resp.getDistance(), DistanceUnit.Meters, units), FormatUtility.getUnitDecimals(units));
					duration += FormatUtility.roundToDecimals(resp.getTime()/1000.0, 1);
				}
			}
			else
			{
				int nInstructions = resp.getInstructions().getSize(); 
				if (nInstructions > 1) 
					nInstructions -= 1;

				for (int j = 0; j < nInstructions; ++j) 
				{
					Instruction instr = resp.getInstructions().get(j);
					InstructionAnnotation instrAnnotation = instr.getAnnotation();

					if (instrAnnotation != null && instrAnnotation.getWayType() != 1) // Ferry, Steps as pushing sections
						distanceActual += FormatUtility.roundToDecimals(DistanceUnitUtil.convert(instr.getDistance(), DistanceUnit.Meters, units), unitDecimals);
				}

				distance += FormatUtility.roundToDecimals(DistanceUnitUtil.convert(resp.getDistance(), DistanceUnit.Meters, units), unitDecimals);
				duration += FormatUtility.roundToDecimals(resp.getTime()/1000.0, 1);
			}

			ascent += resp.getAscent();
			descent += resp.getDescent();
			durationTraffic += resp.getRouteWeight();
		}

		result.getSummary().setDistance(FormatUtility.roundToDecimals(distance, unitDecimals));
		result.getSummary().setDistanceActual(FormatUtility.roundToDecimals(distanceActual, unitDecimals));
		result.getSummary().setAscent(FormatUtility.roundToDecimals(ascent, 1));
		result.getSummary().setDescent(FormatUtility.roundToDecimals(descent, 1));
		
		if (request.getSearchParameters().getConsiderTraffic())
			result.getSummary().setDuration(durationTraffic);
		 else
			result.getSummary().setDuration(duration);
		

		if (routeWayPoints != null)
			result.setWayPointsIndices(routeWayPoints);

		if (bbox != null)
			result.getSummary().setBBox(bbox);

		return result;
	}

	private boolean canMergeInstructions(String name, String prevName)
	{
		if (prevName == null)
			return false;

		if (name.length() > prevName.length())
		{
			int pos = name.indexOf(prevName);
			if (pos >= 0 && !Helper.isEmpty(prevName))
				return true;
		}
		else
		{
			int pos = prevName.indexOf(name);
			if (pos >= 0 && !Helper.isEmpty(name))
				return true;
		}

		return false;
	}

	private String mergeInstructionsOrdered(String name, String prevName)
	{
		int pos = name.indexOf(prevName);
		if (pos >= 0)
		{
			pos = pos + prevName.length() + 1;
			if (pos < name.length())
			{
				String appendix = name.substring(pos, name.length());

				if (appendix.length() > 1 && appendix.startsWith(","))
					appendix = appendix.substring(1);

				appendix = appendix.trim();

				if (isValidAppendix(appendix))
				{
					if (nameAppendix != null)
						nameAppendix += ", ";
					else
						nameAppendix = "";

					nameAppendix += appendix;
				}

				return prevName;
			}
		}

		return name;
	}

	private String mergeInstructions(String name, String prevName)
	{
		if (name.length() > prevName.length())
			return mergeInstructionsOrdered(name, prevName);
		else
			return mergeInstructionsOrdered(prevName, name);
	}

	private boolean isValidAppendix(String name)
	{
		if (name == null)
			return false;

		if (nameAppendix == null)
			return StringUtility.containsDigit(name);
		else
			return nameAppendix.indexOf(name) == -1 && StringUtility.containsDigit(name);   
	}


	private boolean isTurnInstruction(InstructionType instrType) {
		if (instrType == InstructionType.TURN_LEFT || instrType == InstructionType.TURN_SLIGHT_LEFT
				|| instrType == InstructionType.TURN_SHARP_LEFT || instrType == InstructionType.TURN_RIGHT
				|| instrType == InstructionType.TURN_SLIGHT_RIGHT || instrType == InstructionType.TURN_SHARP_RIGHT)
			return true;
		else
			return false;
	}

	private InstructionType getInstructionType(Instruction instr)
	{
		int sign = instr.getSign();
		if (sign == Instruction.CONTINUE_ON_STREET)
			return InstructionType.CONTINUE;
		else if (sign == Instruction.TURN_LEFT)
			return InstructionType.TURN_LEFT;
		else if (sign == Instruction.TURN_RIGHT)
			return InstructionType.TURN_RIGHT;
		else if (sign == Instruction.TURN_SHARP_LEFT)
			return InstructionType.TURN_SHARP_LEFT;
		else if (sign == Instruction.TURN_SHARP_RIGHT)
			return InstructionType.TURN_SHARP_RIGHT;
		else if (sign == Instruction.TURN_SLIGHT_LEFT)
			return InstructionType.TURN_SLIGHT_LEFT;
		else if (sign == Instruction.TURN_SLIGHT_RIGHT)
			return InstructionType.TURN_SLIGHT_RIGHT;
		else if (sign == Instruction.TURN_SLIGHT_RIGHT)
			return InstructionType.TURN_SLIGHT_RIGHT;
		else if (sign == Instruction.USE_ROUNDABOUT)
			return InstructionType.ENTER_ROUNDABOUT;
		else if (sign == Instruction.LEAVE_ROUNDABOUT)
			return InstructionType.EXIT_ROUNDABOUT;
		else if (sign == Instruction.FINISH)
			return InstructionType.FINISH;			

		return InstructionType.CONTINUE;
	}

	private CardinalDirection calcDirection(double lat1, double lon1, double lat2, double lon2 )
	{
		double orientation = - _angleCalc.calcOrientation(lat1, lon1, lat2, lon2);
		orientation = Helper.round4(orientation + Math.PI / 2);
		if (orientation < 0)
			orientation += 2 * Math.PI;

		double degree = Math.toDegrees(orientation);
		return _directions[(int)Math.floor(((degree+ 22.5) % 360) / 45)];
	}
}
