/*********************************************************************
 Copyright (C) 2007 by Pascal Neis

            #########       
   &&&&    ###########		*** i3mainz ***
    &&    ####    ####
   &&&&          ####		University of Applied Sciences FH Mainz,
   &&&&        ######		Department of Geoinformatics and Surveying
   &&&&          #####
   &&&&   ####     ###		Holzstrasse 36
   &&&&   #####   ####		Germany - 55116 Mainz
   &&&&    ##########
            ########

 **********************************************************************
 Mail: neis@geoinform.fh-mainz.de
 **********************************************************************/

package org.freeopenls.routeservice.documents.instruction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.graphhopper.routing.util.RouteSplit;
import com.graphhopper.routing.util.SteepnessUtil;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.RouteService.RouteInstructionRequestParameter;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.documents.instruction.Duration;
import org.freeopenls.routeservice.documents.instruction.InstructionLanguageTags;
import org.freeopenls.routeservice.RSConfigurator;
import org.freeopenls.routeservice.documents.Envelope;
import org.freeopenls.routeservice.routing.RoutePlan;
import org.freeopenls.routeservice.routing.RoutePreferenceType;
import org.freeopenls.routeservice.routing.RouteResult;
import org.freeopenls.tools.FormatUtility;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.LineStringType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.DetermineRouteResponseType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.RouteGeometryType;
import net.opengis.xls.RouteInstructionType;
import net.opengis.xls.RouteInstructionsListType;
import net.opengis.xls.RouteInstructionsRequestType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.WaySteepnessListType;
import net.opengis.xls.WaySteepnessType;
import net.opengis.xls.WaySurfaceListType;
import net.opengis.xls.WaySurfaceType;
import net.opengis.xls.WayTypeListType;
import net.opengis.xls.WayTypeType;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.FinishInstruction;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.PointList;
import com.graphhopper.util.RoundaboutInstruction;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;

/**
 * Class for RouteInstruction.<br>
 * Create the instructions of the route.
 * 
 * @author Pascal Neis, i3mainz, neis@geoinform.fh-mainz.de
 * @version 1.0 2007-08-20
 */
public class RouteInstruction {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(RouteInstruction.class.getName());
	/** RSConfigurator Instance */
	private RSConfigurator mRSConfigurator;

	/** RouteInstruction Language */
	private InstructionLanguageTags m_sLanguage;
	private StringBuffer m_stringBuffer;

	/**
	 * Constructor
	 * 
	 * @param sLanguage
	 *            Language for the instructions
	 * @throws ServiceError
	 */
	public RouteInstruction(String sLanguage) throws ServiceError {
		mRSConfigurator = RSConfigurator.getInstance();

		m_stringBuffer = new StringBuffer();
		// Set Language for RouteInstruction
		m_sLanguage = mRSConfigurator.getHashMapLanguageCodeToInstrunsTags().get(sLanguage);
		if (m_sLanguage == null) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.NOT_SUPPORTED, "Language",
					"The value of the parameter 'xls:lang' is not supported. Please choose an other language!");
			throw se;
		}
	}

	/**
	 * Method that create and returns the requested RouteInstructionResponse<br>
	 * 
	 * @param drrType
	 *            DetermineRouteRequestType, to read variables for
	 *            RouteInstructionResponse
	 * @throws ServiceError
	 */
	public void createRouteInstruction(DetermineRouteResponseType resp, DetermineRouteRequestType drrType, RoutePlan routePlan,
			RouteResult routeResult) throws ServiceError {

		RouteInstructionsListType RouteInstrucList = resp.addNewRouteInstructionsList();
		// Set Parameter for change the Length in the requested Unit
		double dUnitParameter = DistanceUnit.getUnitParameter(routeResult.getDistanceUnit());

		// Set variables for RouteInstructionsRequest
		RouteInstructionsRequestType routeinstrucreqType = null; // Optional
		String sRouteFormat = "text/plain"; // Optional
		Boolean bProvideGeometry = false; // Optional
		Boolean bProvideBoundingBox = false; // Optional

		String sInstructionBegin = m_sLanguage.DRIVE;
		if (routePlan.getRoutePreference() == RoutePreferenceType.PEDESTRIAN)
			sInstructionBegin = m_sLanguage.GO;

		// Get RouteInstructionRequest
		routeinstrucreqType = drrType.getRouteInstructionsRequest();
		if (routeinstrucreqType.isSetFormat())
			sRouteFormat = routeinstrucreqType.getFormat();

		if (!sRouteFormat.equals("text/plain") || sRouteFormat.equals("")) {
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.OTHER_XML, RouteInstructionRequestParameter.format.toString(),
					"The value of the parameter '" + RouteInstructionRequestParameter.format.name()
					+ "' must be 'text/plain'. Delivered value was: " + sRouteFormat);
			throw se;
		}

		if (routeinstrucreqType.isSetProvideGeometry())
			bProvideGeometry = routeinstrucreqType.getProvideGeometry();
		if (routeinstrucreqType.isSetProvideBoundingBox())
			bProvideBoundingBox = routeinstrucreqType.getProvideBoundingBox();

		// /////////////////////////////////////
		// *** Create new RouteInstruction Type ***
		RouteInstrucList.setLang(m_sLanguage.LANGUAGE_CODE);

		boolean bChinese = m_sLanguage.LANGUAGE_CODE.startsWith("cn");

		String sInstruction = m_sLanguage.DIRECTION_STRAIGHTFORWARD;
		double dDistance = 0;
		int sign;
		int exitNumber = -1;
		int time = 0;
		String streetName = "";
		String annotation = "";
		int iActionNr = 1;
		GDuration gdurationTotal = routeResult.getDuration();
		List<GHResponse> listRoute = routeResult.getRouteSegments();
		String wordSpace = bChinese ? "" : " ";

		WaySurfaceListType waySurfaceList = null;
		WayTypeListType wayTypeList = null;
		WaySteepnessListType waySteepnessList = null;
		List<RouteSplit> splits = null;
		DistanceCalc dc = new DistanceCalcEarth();

		Boolean bSurfaceInfo = routePlan.getSurfaceInformation();
		if (bSurfaceInfo)
		{
			wayTypeList = resp.addNewWayTypeList();
			waySurfaceList = resp.addNewWaySurfaceList();	
			waySteepnessList = resp.addNewWaySteepnessList();
			splits = new ArrayList<RouteSplit>();
		}

		int prevWayType = -1;
		int prevSurfaceType = -1;
		int startWayType = 0;
		int startSurfaceType = 0;
		int segIndex = 0;
		
		double maxAltitude = Double.MIN_VALUE;
		double minAltitude = Double.MAX_VALUE;
		double prevMinAltitude, prevMaxAltitude;
		double x0  = 0,x1, y0 = 0,y1,z0  = 0,z1;
		
		int startIndex = 0;
		double splitLength = 0;
		double cumElev = 0.0;
		RouteSplit prevSplit = null;
		int prevGC = 0;
		int iPoints = 0;

		int nSize = listRoute.size();
		// Adds Instructions
		for (int i = 0; i < nSize; i++) {
			GHResponse seg = listRoute.get(i);

			int nInstructions = seg.getInstructions().getSize(); 
			if (nInstructions > 1) // last is finishinstruction
				nInstructions -= 1;
			
			for (int j = 0; j < nInstructions; j++) {
				Instruction instr = seg.getInstructions().get(j);

				sign = instr.getSign();
				streetName = instr.getName();
				if (streetName == null || streetName == " ")
					streetName = "n/a";

				InstructionAnnotation instrAnnotation = instr.getAnnotation();
				Boolean hasAnnotation = instrAnnotation != null && !Helper.isEmpty(instrAnnotation.getMessage());

				annotation = RoutePreferenceType.supportMessages(routePlan.getRoutePreference()) ?  (hasAnnotation ? instrAnnotation.getMessage(): "") : "";
				dDistance = Math.round(instr.getDistance());
				String sDistance = FormatUtility.formatDistance(dDistance * dUnitParameter,
						routeResult.getDistanceUnit().toString(), m_stringBuffer).replaceAll(",", ".");
				time = (int)(instr.getTime() / 1000);

				if (bSurfaceInfo)
				{
					boolean bLast = (i == nSize -1 && j == nInstructions - 1); //(j == nInstructions - 1);
					boolean bStopover = (sign == 100);

					int wayType = -1;
					int surfaceType = -1;

					if (!bStopover)
					{
						WaySurfaceDescription desc = instr.getWaySurfaceDescription();
						if (desc != null)
						{
							wayType = desc.WayType;
							surfaceType = desc.SurfaceType;
						}
					}

					if ((wayType != prevWayType && prevWayType != -1) || bStopover)
					{
						WayTypeType wt	= wayTypeList.addNewWayType();
						wt.setFrom(BigInteger.valueOf(startWayType));
						wt.setTo(BigInteger.valueOf(segIndex - 1));
						wt.setType(BigInteger.valueOf(prevWayType));

						startWayType = bStopover ? segIndex + 1 : segIndex;
					}

					if ((surfaceType != prevSurfaceType && prevSurfaceType != -1) || bStopover)
					{
						WaySurfaceType wst	= waySurfaceList.addNewWaySurface();
						wst.setFrom(BigInteger.valueOf(startSurfaceType));
						wst.setTo(BigInteger.valueOf(segIndex - 1));
						wst.setType(BigInteger.valueOf(prevSurfaceType));

						startSurfaceType = bStopover ? segIndex + 1 : segIndex;
					}

					if (bLast && wayType != -1)
					{
						WayTypeType wt	= wayTypeList.addNewWayType();
						wt.setFrom(BigInteger.valueOf(startWayType));
						wt.setTo(BigInteger.valueOf(segIndex));
						wt.setType(BigInteger.valueOf(wayType));

						WaySurfaceType wst	= waySurfaceList.addNewWaySurface();
						wst.setFrom(BigInteger.valueOf(startSurfaceType));
						wst.setTo(BigInteger.valueOf(segIndex));
						wst.setType(BigInteger.valueOf(surfaceType));
					}

					prevWayType = wayType;
					prevSurfaceType = surfaceType;
				}

				String instructionText = "";

				if (!Helper.isEmpty(streetName))
					streetName = "<b>" + streetName + "</b>";

				PointList nextPoints = null;
				
				if (j < seg.getInstructions().getSize() - 1)
					nextPoints = seg.getInstructions().get(j + 1).getPoints();
				
				if (i ==0 && j == 0) {
					Coordinate c0 = getCoordinate(instr.getPoints(), 0);
					Coordinate c1 = c0;
					if (instr.getPoints().size() == 1 && nextPoints != null)
						c1 = getCoordinate(nextPoints, 0);
						
					//getCoordinate(instr.getPoints().size() == 1 ? nextPoints: instr.getPoints(), 1)
					String startDirection = CoordTools.getDirectionByAngle(c0, c1, m_sLanguage.DIRECTION_INITIAL_HEADING);
					if (!(streetName.equals("n/a") || Helper.isEmpty(streetName)))
						instructionText = m_sLanguage.START + wordSpace + "(" + startDirection + ")" + wordSpace
						+ m_sLanguage.FILLWORD_ON + " " + streetName;
					else
						instructionText = m_sLanguage.START + wordSpace + "(" + startDirection + ")";
				} else {
					
					if (instr instanceof RoundaboutInstruction && !Helper.isEmpty(m_sLanguage.DIRECTION_ROUNDABOUT))
					{
						RoundaboutInstruction raInstr = (RoundaboutInstruction)instr;
						exitNumber = raInstr.getExitNumber();
						
						instructionText = String.format(m_sLanguage.DIRECTION_ROUNDABOUT, exitNumber, streetName);
					}
					else
					{
						sInstruction = getInstructionTranslation(sign);
						exitNumber = -1;
					
						String strBegin = isTurnInstruction(sign) ? m_sLanguage.TURN : sInstructionBegin;
						
						instructionText = (bChinese ? (sInstruction + wordSpace + strBegin) : strBegin + wordSpace
								+ sInstruction);
						
						if (!(streetName.equals("n/a") || Helper.isEmpty(streetName)))
							instructionText += wordSpace + m_sLanguage.FILLWORD_ON + " " + streetName;
					}
				}

				createRouteInstruction(RouteInstrucList.addNewRouteInstruction(), sign,exitNumber, RouteService.GRAPH_SRS,
						routeResult.getResponseSRS(), instr.getPoints(), nextPoints, iActionNr, instructionText, annotation,
						sDistance, routeResult.getDistanceUnit(), bProvideGeometry, bProvideBoundingBox,
						routePlan.getExpectedDateTime(), routePlan.getCalendarDateTime(), gdurationTotal, time);

				routeResult.setTotalTime(routeResult.getTotalTime() + time);
				routeResult.setTotalDistance(routeResult.getTotalDistance() + dDistance);

				if (instr.getAnnotation() != null && instr.getAnnotation().getWayType() != 1) // Ferry, Steps as pushing sections
					routeResult.setActualDistance(routeResult.getActualDistance() + dDistance);

				dDistance = 0;
				time = 0;
				iActionNr++;
				
				segIndex++;
			}
			
			if (bSurfaceInfo)
			{
				PointList points = seg.getPoints();

				if (i == 0)
				{
					x0 = points.getLon(0);
					y0 = points.getLat(0);
					z0 = points.getEle(0);
					
					iPoints++;
				}
				
				int nPoints = points.size();
				//int ji = (i == 0) ? 1: 0;
				
				for (int j = 1; j < nPoints; j++) {
					x1 = points.getLon(j);
					y1 = points.getLat(j);
					z1 = points.getEle(j);
					
					double elevDiff = z1 - z0;
					double length = dc.calcDist(y0, x0, y1, x1);
					cumElev += elevDiff;
					
					prevMinAltitude = minAltitude;
					prevMaxAltitude = maxAltitude;
					if (z1 > maxAltitude)
						maxAltitude = z1;
					if (z1 < minAltitude)
						minAltitude = z1;
					
					if (maxAltitude - z1 > SteepnessUtil.ELEVATION_THRESHOLD || z1 - minAltitude > SteepnessUtil.ELEVATION_THRESHOLD)
					{
						boolean bApply = true;
						int elevSign = cumElev > 0 ? 1 : -1;
						double gradient = elevSign*100*(prevMaxAltitude - prevMinAltitude) / splitLength;
						
						if (prevGC != 0 )
						{
							double zn= Double.MIN_NORMAL;
							
							if (j + 1 < nPoints)
							{
								zn = points.getEle(j + 1);
							}
							else
							{
								int k = i+1;
								if (k < nSize)
								{
									GHResponse resp2 = listRoute.get(k);
									if (resp2.getPoints().size() == 0)
										k++;
									if (k < nSize)
									{
										resp2 = listRoute.get(k);
										if (resp2.getPoints().size() >= 2)
											zn = resp2.getPoints().getEle(0);
									}
								}
							}

							if (zn != Double.MIN_VALUE)
							{	
								double elevGap = length/30;
								if (elevSign > 0 /* && Math.Abs(prevSplit.Gradient - gradient) < gradientDiff)//*/ && prevGC > 0)
								{
									if (Math.abs(zn - z1) < elevGap)
										bApply = false;
								}
								else if(/*Math.Abs(prevSplit.Gradient - gradient) < gradientDiff)//*/prevGC < 0)
								{
									if (Math.abs(z1 - zn) < elevGap)
										bApply = false;
								}
							}
						}
						
						if (bApply)
						{
							int gc = SteepnessUtil.getCategory(gradient);
							if (prevSplit != null && gc == prevGC)
							{
								prevSplit.End = iPoints - 1;
							}
							else{
								RouteSplit split = new RouteSplit();
								split.Start = startIndex;
								split.End = iPoints - 1;
								split.Value = gc;
								split.Gradient = gradient;
								splits.add(split);
								
								prevGC = gc;
								prevSplit = split;
							}
							
							startIndex = iPoints - 1;
							minAltitude = Math.min(z0, z1);
							maxAltitude = Math.max(z0, z1);
							splitLength = 0.0;
							
							cumElev= elevDiff;
						}
					}
					
					splitLength += length;
					
					x0 = x1;
					y0 = y1;
					z0 = z1;
					iPoints++;
				}
			}
			
			routeResult.setTotalAscent(routeResult.getTotalAscent() + seg.getAscent());
			routeResult.setTotalDescent(routeResult.getTotalDescent() + seg.getDescent());
		}
		
		if (bSurfaceInfo)
		{
			if (splitLength > 0)
			{
				double gradient = (cumElev > 0 ? 1: -1)*100*(maxAltitude - minAltitude) / splitLength;
				int gc = SteepnessUtil.getCategory(gradient);
				if (prevSplit != null && (prevSplit.Value == gc || splitLength < 25))
				{
					prevSplit.End = iPoints - 1;
				}
				else
				{
					RouteSplit lastSplit = new RouteSplit();
					lastSplit.Start = startIndex;
					lastSplit.End = iPoints - 1;
					lastSplit.Value = gc;
					lastSplit.Gradient = gradient;
					splits.add(lastSplit);
				}
			}
			
			for(RouteSplit split : splits)
			{
				WaySteepnessType steep = waySteepnessList.addNewWaySteepness();
				steep.setFrom(BigInteger.valueOf(split.Start));
				steep.setTo(BigInteger.valueOf(split.End));
				steep.setType(BigInteger.valueOf(split.Value));
			}
		}
	}
	
	private Coordinate getCoordinate(PointList points, int index)
	{
		return new Coordinate(points.getLongitude(index), points.getLatitude(index));
	}

	private String getInstructionTranslation(int sign) {
		switch (sign) {
		case Instruction.CONTINUE_ON_STREET:
			return m_sLanguage.DIRECTION_STRAIGHTFORWARD;
		case Instruction.TURN_LEFT:
			return m_sLanguage.DIRECTION_LEFT;
		case Instruction.TURN_SHARP_LEFT:
			return m_sLanguage.DIRECTION_SHARP_LEFT;
		case Instruction.TURN_SLIGHT_LEFT:
			return m_sLanguage.DIRECTION_HALFLEFT;
		case Instruction.TURN_RIGHT:
			return m_sLanguage.DIRECTION_RIGHT;
		case Instruction.TURN_SHARP_RIGHT:
			return m_sLanguage.DIRECTION_SHARP_RIGHT;
		case Instruction.TURN_SLIGHT_RIGHT:
			return m_sLanguage.DIRECTION_HALFRIGHT;
		case Instruction.FINISH:
			return m_sLanguage.FINSIH;
		case 5:
			return m_sLanguage.START;
		}

		return null;
	}

	private boolean isTurnInstruction(int sign) {
		if (sign == Instruction.TURN_LEFT || sign == Instruction.TURN_SLIGHT_LEFT
				|| sign == Instruction.TURN_SHARP_LEFT || sign == Instruction.TURN_RIGHT
				|| sign == Instruction.TURN_SLIGHT_RIGHT || sign == Instruction.TURN_SHARP_RIGHT)
			return true;
		else
			return false;
	}

	/**
	 * Method that create RouteInstruction
	 * 
	 * @param routeinstrucType
	 * @param sFeatSRS
	 * @param sResponseSRS
	 * @param c
	 * @param iActionNr
	 * @param sInstruction
	 * @param sDistance
	 * @param distUnit
	 * @param boolProvideGeom
	 * @param boolProvideBBox
	 * @param sExpectedTimeType
	 * @param calendarDateTime
	 * @param gdurationTotal
	 * @param iDuration
	 * @throws ServiceError
	 */
	private void createRouteInstruction(RouteInstructionType routeinstrucType, int directionCode, int exitNumber, String sFeatSRS,
			String sResponseSRS, PointList points, PointList nextPoints, int iActionNr, String sInstruction, String annotation,
			String sDistance, DistanceUnitType.Enum distUnit, boolean bProvideGeom, boolean bProvideBBox,
			String sExpectedTimeType, Calendar calendarDateTime, GDuration gdurationTotal, int iDuration)
			throws ServiceError {
		
		routeinstrucType.setDirectionCode(BigInteger.valueOf(directionCode));
		// setDescription

		routeinstrucType.setDescription(m_sLanguage.ACTION_NR + " " + iActionNr);

		if (points == null || points.getSize() == 0)
			return;

		if (exitNumber != -1)
			routeinstrucType.setExitNumber(BigInteger.valueOf(exitNumber));

		// setDuration
		routeinstrucType.setDuration(Duration.getGDuration(iDuration));
		// setInstruction
		routeinstrucType.setInstruction(sInstruction + " "
				+ getDateTimeString(sExpectedTimeType, calendarDateTime, new GDuration(), gdurationTotal, iDuration));
		
		if (!Helper.isEmpty(annotation))
			routeinstrucType.setMessage(annotation);
		// setDistance
		DistanceType distance = routeinstrucType.addNewDistance();
		distance.setValue(new BigDecimal(sDistance));
		distance.setUom(distUnit);

		// Geometry & BBOx
		// addGeometry
		if (bProvideGeom)
			addGeometry(routeinstrucType.addNewRouteInstructionGeometry(), sFeatSRS, sResponseSRS, points, nextPoints);
		// addBBox
		if (bProvideBBox)
			addBoundingBox(routeinstrucType.addNewBoundingBox(), sFeatSRS, sResponseSRS, points);
	}

	/**
	 * Method that creates Geometry element in RouteInstruction
	 * 
	 * @param rtTMP
	 * @param sLineStringSRS
	 * @param sResponseSRS
	 * @param c
	 * @throws ServiceError
	 */
	private void addGeometry(RouteGeometryType rtTMP, String sLineStringSRS, String sResponseSRS, PointList points, PointList nextPoints)
			throws ServiceError {
		LineStringType linestring = rtTMP.addNewLineString();
		linestring.setSrsName(sResponseSRS);

		boolean sameSRS = sLineStringSRS.equals(sResponseSRS);
		for (int i = 0; i < points.getSize(); i++) {
			addPoint(linestring, sLineStringSRS, sResponseSRS, sameSRS, points, i);
		}
		
		if (nextPoints != null && nextPoints.size() > 0)
			addPoint(linestring, sLineStringSRS, sResponseSRS, sameSRS, nextPoints, 0);
	}
	
	private void addPoint(LineStringType linestring, String sLineStringSRS, String sResponseSRS, boolean sameSRS, PointList points, int index) throws ServiceError
	{
		DirectPositionType direct = linestring.addNewPos();
		if (!sameSRS) {
			Coordinate cTMP = CoordTransform.transformGetCoord(sLineStringSRS, sResponseSRS, new Coordinate(points.getLongitude(index), points.getLatitude(index)));
			direct.setStringValue(FormatUtility.formatCoordinate(cTMP, m_stringBuffer));
		} else
			direct.setStringValue(FormatUtility.formatCoordinate(points.getLongitude(index), points.getLatitude(index),Double.NaN, false, m_stringBuffer));
	}

	/**
	 * Method that creates BoundingBox element in RouteInstruction
	 * 
	 * @param env
	 * @param sBBoxSRS
	 * @param sResponseSRS
	 * @param c
	 * @throws ServiceError
	 */
	private void addBoundingBox(EnvelopeType env, String sBBoxSRS, String sResponseSRS, PointList points)
			throws ServiceError {
		Envelope bb = new Envelope(sBBoxSRS, points);
		DirectPositionType positionLowerCornerTMP = env.addNewPos();
		Coordinate cTMP = bb.getLowerCorner();
		if (!sBBoxSRS.equals(sResponseSRS))
			cTMP = CoordTransform.transformGetCoord(sBBoxSRS, sResponseSRS, cTMP);
		positionLowerCornerTMP.setStringValue(FormatUtility.formatCoordinate(cTMP, m_stringBuffer));
		DirectPositionType positionUpperCornerTMP = env.addNewPos();
		cTMP = bb.getUpperCorner();
		if (!sBBoxSRS.equals(sResponseSRS))
			cTMP = CoordTransform.transformGetCoord(sBBoxSRS, sResponseSRS, cTMP);
		positionUpperCornerTMP.setStringValue(FormatUtility.formatCoordinate(cTMP, m_stringBuffer));
	}

	/**
	 * Method that add, if requests, Start-/EndTime to Instruction
	 * 
	 * @param sExpectedTime
	 *            String e.g. "StartTime"
	 * @param calendarDateTime
	 *            Calendar with Expected e.g. StartTime
	 * @param gduration
	 *            Duration to add or subtract
	 * @param gdurationTotal
	 *            TotalDuration of Route, only important for
	 *            sExpectedtTime="EndTime"
	 * @param iSecondsForDuration
	 *            Duration in seconds, only important if sExpectedTime !=
	 *            "EndTime" or "StartTime"
	 * @return String DateTime
	 */
	private String getDateTimeString(String sExpectedTime, Calendar calendarDateTime, GDuration gduration,
			GDuration gdurationTotal, int iSecondsForDuration) {
		String sDateTime = "";

		if (sExpectedTime.equals("StartTime")) {
			calendarDateTime.add(Calendar.YEAR, gduration.getYear());
			calendarDateTime.add(Calendar.MONTH, gduration.getMonth());
			calendarDateTime.add(Calendar.DAY_OF_MONTH, gduration.getDay());
			calendarDateTime.add(Calendar.HOUR_OF_DAY, gduration.getHour());
			calendarDateTime.add(Calendar.MINUTE, gduration.getMinute());
			calendarDateTime.add(Calendar.SECOND, gduration.getSecond());
			sDateTime = " - " + calendarDateTime.getTime();
		} else if (sExpectedTime.equals("EndTime")) {
			calendarDateTime.add(Calendar.YEAR, -gdurationTotal.getYear());
			calendarDateTime.add(Calendar.MONTH, -gdurationTotal.getMonth());
			calendarDateTime.add(Calendar.DAY_OF_MONTH, -gdurationTotal.getDay());
			calendarDateTime.add(Calendar.HOUR_OF_DAY, -gdurationTotal.getHour());
			calendarDateTime.add(Calendar.MINUTE, -gdurationTotal.getMinute());
			calendarDateTime.add(Calendar.SECOND, -gdurationTotal.getSecond());
			sDateTime = " - " + calendarDateTime.getTime();
		} else if (iSecondsForDuration == 0) {
			sDateTime = "";
		} else {
			// sDateTime = " - "+m_sLanguage.TIME_APPROX+" "
			// +Duration.getTimeString(iSecondsForDuration,m_sLanguage.TIME_DAY,m_sLanguage.TIME_HOUR,m_sLanguage.TIME_MINUTE);
			sDateTime = "";
		}
		return sDateTime;
	}
}
