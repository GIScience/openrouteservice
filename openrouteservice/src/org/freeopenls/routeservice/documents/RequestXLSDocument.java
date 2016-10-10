/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.routeservice.documents;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.EnvelopeType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AvoidListType;
import net.opengis.xls.DetermineRouteRequestType;
import net.opengis.xls.DetermineRouteResponseType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.ExtendedRoutePreferenceType;
//import net.opengis.xls.ExtendedRoutePreferenceType;
import net.opengis.xls.RequestType;
import net.opengis.xls.RouteGeometryType;
import net.opengis.xls.RoutePlanType;
import net.opengis.xls.RouteSummaryType;

import org.apache.log4j.Logger;
import org.freeopenls.constants.RouteService;
import org.freeopenls.constants.OpenLS.RequestParameter;
import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.location.AvoidList;
import org.freeopenls.location.WayPoint;
import org.freeopenls.location.WayPointList;
import org.freeopenls.routeservice.routing.RouteResult;
import org.freeopenls.routeservice.routing.RoutePlan;
import org.freeopenls.routeservice.routing.Routing;
import org.freeopenls.routeservice.routing.WeightingMethod;
import org.freeopenls.routeservice.RSConfigurator;
import org.freeopenls.routeservice.documents.RouteGeometry;
import org.freeopenls.routeservice.documents.instruction.*;
import org.freeopenls.routeservice.graphhopper.extensions.HeavyVehicleAttributes;
import org.freeopenls.routeservice.graphhopper.extensions.util.VehicleRestrictionCodes;
import org.freeopenls.routeservice.graphhopper.extensions.util.WheelchairRestrictionCodes;
import org.freeopenls.routeservice.routing.RoutePreferenceType;
import org.freeopenls.tools.TimeUtility;

import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


/**
 * <p><b>Title: RequestXLSDocument</b></p>
 * <p><b>Description:</b> Class for read and create XLSDocument (Route Request and Route Response). </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-02
 * @version 1.1 2008-04-24
 */
public class RequestXLSDocument {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLoggerCounter = Logger.getLogger(RequestXLSDocument.class.getName()+".Counter");
	/** RSConfigurator Instance */
	private RSConfigurator mRSConfigurator;
	/** Response Document */
	private ResponseXLSDocument mResponseXLSDocument;	
	
	/** AvoidAreas and AvoidLines Geometry for display in RouteMap */
	private ArrayList<Polygon> m_AvoidAreas = new ArrayList<Polygon>();
	private ArrayList<LineString> m_AvoidLines = new ArrayList<LineString>();

	/** SRS in which the Response should be */
	private String mResponseSRS = null;
	/** Language in which the Response should be */
	private String mResponseLanguage = null;
	
	/**
	 * Constructor - Create new XLSDocument
	 * @param sessionID
	 * @param responseSRS
	 * @param responseLanguage
	 * @throws IOException 
	 */
	public RequestXLSDocument(String sessionID, String responseSRS, String responseLanguage) throws IOException {
		mResponseSRS = responseSRS;
		mResponseLanguage = responseLanguage;
		mRSConfigurator = RSConfigurator.getInstance();

		mResponseXLSDocument = new ResponseXLSDocument(sessionID);
	}

	/**
	 * Method that read RouteRequest and create the RouteResponse
	 * NOT supported and TODO:<br>
	 * RequestType: - MaximumResponses<br>
	 * RoutePlan: - boolUseRealTimeTraffic<br>
	 * 
	 * @param requestType
	 * @throws ServiceError
	 * @throws Exception
	 */
	public void doRouteRequest(RequestType requestType)throws ServiceError, Exception{
		
		RoutePlan routePlan = new RoutePlan();
		Long lTime = System.currentTimeMillis();
		RouteResult routeResult = new RouteResult(mResponseSRS, mResponseLanguage, lTime.toString());

        ///////////////////////////////////////
		//*** GetRequestParameters ***
		//Parameter MaximumResponses is Optional 		//TODO
		//if(reqType.isSetMaximumResponses()) sMaxResponses = reqType.getMaximumResponses().toString();
		//Parameter MethodName is Mandatory
		if (!requestType.getMethodName().equalsIgnoreCase(RouteService.METHODNAME)) 
			throw ErrorTypes.methodNameError(requestType.getMethodName(), RouteService.METHODNAME);
		//Parameter RequestID is Mandatory
		if (requestType.getRequestID().equalsIgnoreCase(""))
        	throw ErrorTypes.parameterMissing(RequestParameter.requestID.toString());
		//Parameter Version is Mandatory
		if (requestType.getVersion().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());
		//Parameter Version is Mandatory
		if (!requestType.getVersion().equalsIgnoreCase(RouteService.SERVICE_VERSION))
			throw ErrorTypes.parameterMissing(RequestParameter.version.toString());


        ///////////////////////////////////////
		// *** GetDetermineRouteRequest ***
		AbstractRequestParametersType abreqparType = requestType.getRequestParameters();
		DetermineRouteRequestType determineRouteRequest = (DetermineRouteRequestType) abreqparType.changeType(DetermineRouteRequestType.type);

		if(determineRouteRequest.isSetDistanceUnit())	//Optional
			routeResult.setDistanceUnit(determineRouteRequest.getDistanceUnit());
		//Is set RouteHandle
		if(determineRouteRequest.isSetRouteHandle()){	//Optional
			RouteHandle routehandle = new RouteHandle();
			determineRouteRequest = routehandle.readRouteHandle(determineRouteRequest, requestType.getRequestID(), requestType.getVersion());
		}

		RoutePlanType routePlanType = determineRouteRequest.getRoutePlan();
		///////////////////////////////////////
		//*** GetRoutePlan and do routing ***
		doRoutePlan(routePlan, routeResult, routePlanType);

		
		///////////////////////////////////////
		//*** Create Response ***
		mResponseXLSDocument.createResponse(requestType.getRequestID(), requestType.getVersion(), new BigInteger("1"));
		DetermineRouteResponseType determineRouteResponse = mResponseXLSDocument.addResponseParameters();
		//RouteHandle
		if(determineRouteRequest.isSetProvideRouteHandle()) //Optional	Default=false
			if(determineRouteRequest.getProvideRouteHandle()){
				RouteHandle routehandle = new RouteHandle();
				routehandle.saveRouteHandle(determineRouteResponse.addNewRouteHandle(), routeResult.getRouteRequestID(), determineRouteRequest);
			}

		//RouteGeometry
		if(determineRouteRequest.isSetRouteGeometryRequest()){
			RouteGeometryType geomType = determineRouteResponse.addNewRouteGeometry();
			RouteGeometry routegeom = new RouteGeometry();
			routegeom.createRouteGeometry(determineRouteRequest, routePlan, routeResult, geomType);
		}
		//RouteInstruction
		if(determineRouteRequest.isSetRouteInstructionsRequest()){
			RouteInstruction routeinstruc = new RouteInstruction(routeResult.getResponseLanguage());
			routeinstruc.createRouteInstruction(determineRouteResponse, determineRouteRequest, routePlan, routeResult);
		}
		else
		{
			routeResult.computeSummary();
		}			
		
		//RouteSummary
		routeResult.setDuration(Duration.getGDuration((int)routeResult.getTotalTime()));
		routeResult.setEnvelopeRoute(new Envelope(routeResult.getResponseSRS(), routeResult.getRouteEnvelope(), routeResult.getFeatCollSRS()));
		RouteSummaryType routeSumType = createRouteSummary(routeResult,  determineRouteResponse.addNewRouteSummary());

		//RouteMap
		if(determineRouteRequest.isSetRouteMapRequest()){
//			RouteMap routemap = new RouteMap();
//			determineRouteResponse.setRouteMapArray(routemap.createRouteMaps(determineRouteRequest, routePlan, routeResult, m_AvoidAreas, m_AvoidLines));
		}
			
		mLoggerCounter.info("doRouting | time: "+TimeUtility.getElapsedTime(lTime, true) + "; dist: " + routeResult.getTotalDistance()+"; profile: "+routePlanType.getRoutePreference().toString());
			
		mResponseXLSDocument.doWellFormedDetermineRouteResponse();
	}
	
	public static RouteSummaryType createRouteSummary(RouteResult routeResult, RouteSummaryType rsType)throws ServiceError{
		//Set Parameter for change the Length in the requested Unit
		double dUnitParameter = DistanceUnit.getUnitParameter(routeResult.getDistanceUnit());
		double dTotalDistance = routeResult.getTotalDistance() * dUnitParameter;
		double dActualDistance = routeResult.getActualDistance() * dUnitParameter;
		
		rsType.setTotalTime(routeResult.getDuration());
		DistanceType distanceType = rsType.addNewTotalDistance();
		distanceType.setUom(routeResult.getDistanceUnit());
		distanceType.setValue(new BigDecimal(String.valueOf(dTotalDistance)).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		
		if (dActualDistance != dTotalDistance)
		{
			DistanceType distanceType2 = rsType.addNewActualDistance();
			distanceType2.setUom(routeResult.getDistanceUnit());
			distanceType2.setValue(new BigDecimal(String.valueOf(dActualDistance)).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		}
		
		if (routeResult.getTotalAscent() != 0.0 || routeResult.getTotalDescent() != 0.0)
		{
			DistanceType ascent = rsType.addNewAscent();
			ascent.setUom(routeResult.getDistanceUnit());
			ascent.setValue(new BigDecimal(String.valueOf(routeResult.getTotalAscent())).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));

			DistanceType descent = rsType.addNewDescent();
			descent.setUom(routeResult.getDistanceUnit());
			descent.setValue(new BigDecimal(String.valueOf(routeResult.getTotalDescent())).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		}
		
		//SetBoundingBox
		EnvelopeType envType = rsType.addNewBoundingBox();
		routeResult.getEnvelopeRoute().setValue(envType, routeResult.getResponseSRS());
		
		return rsType;
	}

    /**
     * Method that read RoutePlan and calculate the Route.
     * 
     * @throws ServiceError
     * @throws Exception
     */
    private void doRoutePlan(RoutePlan routePlan , RouteResult routeResult, RoutePlanType routePlanType)throws ServiceError, Exception{
		routeResult.setFeatCollSRS(RouteService.GRAPH_SRS);
		
    	 String prefType = routePlanType.getRoutePreference().toString();
    	 routePlan.setRoutePreference(RoutePreferenceType.getFromString(prefType));
		//TODO
		if(routePlanType.isSetUseRealTimeTraffic()) // Optional
			routePlan.setUseRealTimeTraffic(routePlanType.getUseRealTimeTraffic());
		
		//ExpectedStartTime
		if(routePlanType.isSetExpectedStartTime()){		//Optional
			routePlan.setCalendarDateTime(routePlanType.getExpectedStartTime());
			routePlan.setExpectedDateTime("StartTime");
		}
		else if(routePlanType.isSetExpectedEndTime()){	//Optional
			routePlan.setCalendarDateTime(routePlanType.getExpectedEndTime());
			routePlan.setExpectedDateTime("EndTime");
		}
		else{
			routePlan.setCalendarDateTime(null);
			routePlan.setExpectedDateTime("");
		}
	    ///////////////////////////////////////
		//*** GetWayPoints ***
		WayPointList wpl = new WayPointList(routePlanType, 
				mRSConfigurator.getOpenLSLocationUtilityServicePath(),
				mRSConfigurator.getOpenLSDirectoryServicePath());
		//Source
		routePlan.setSourceWayPoint(wpl.getSource());
		//Destination
		routePlan.setDestinationWayPoint(wpl.getDestination());
		//Via Points
		WayPoint cViaPoints[] = wpl.getArrayViaPoints();
			
		//Create ArrayList with WayNodes
		//Search for every WayPoint the append node
		ArrayList<WayPoint> wayPoints = new ArrayList<WayPoint>();
		if(cViaPoints != null){
			wayPoints.add(routePlan.getSourceWayPoint());
			for(int i=0 ; i<cViaPoints.length ; i++){
				routePlan.addViaWayPoint(cViaPoints[i]);
				wayPoints.add(cViaPoints[i]);
			}
			wayPoints.add(routePlan.getDestinationWayPoint());
		}else{
			wayPoints.add(routePlan.getSourceWayPoint());
			wayPoints.add(routePlan.getDestinationWayPoint());
		}

		ExtendedRoutePreferenceType erpt = routePlanType.getExtendedRoutePreference();
		if (erpt != null)
		{
			if (erpt.isSetWeightingMethod())
			{
				routePlan.setWeightingMethod(WeightingMethod.getFromString(erpt.getWeightingMethod()));
			}
			
			if (erpt.isSetMaxSpeed())
			{
				// TODO add support of units (km/h, m/h)
				routePlan.setMaxSpeed(erpt.getMaxSpeed().getBigDecimalValue().doubleValue());
			}
			
			if (erpt.isSetTurnRestrictions())
			{
				routePlan.setSupportTurnRestrictions(erpt.getTurnRestrictions());
			}
			
			if (erpt.isSetSurfaceInformation())
			{
				routePlan.setSurfaceInformation(erpt.getSurfaceInformation());
			}
			
			if (erpt.isSetElevationInformation())
			{
				routePlan.setElevationInformation(erpt.getElevationInformation());
			}
			
			if (erpt.isSetVehicleType())
			{
				int vt = HeavyVehicleAttributes.getTypeFromString(erpt.getVehicleType());
				if (vt != HeavyVehicleAttributes.Unknown)
					routePlan.setVehicleType(vt);
				else
					throw new Exception("Vehicle type is unknown.");
			}
			
			float[] vehicleAttrs = new float[5];
			boolean bHasAttrs = false;

			ExtendedRoutePreferenceType.Height restrHeight = erpt.getHeight();
			if (restrHeight != null)
			{
				vehicleAttrs[VehicleRestrictionCodes.MaxHeight] = getRestrictionValue(restrHeight.getStringValue(), restrHeight.getUnit());
				bHasAttrs = true;
			}
			
			ExtendedRoutePreferenceType.Length restrLength = erpt.getLength();
			if (restrLength != null)
			{
				vehicleAttrs[VehicleRestrictionCodes.MaxLength] = getRestrictionValue(restrLength.getStringValue(), restrLength.getUnit());
				bHasAttrs = true;
			}
		
			ExtendedRoutePreferenceType.Width restrWidth = erpt.getWidth();
			if (restrWidth != null)
			{
				vehicleAttrs[VehicleRestrictionCodes.MaxWidth] = getRestrictionValue(restrWidth.getStringValue(), restrWidth.getUnit());
				bHasAttrs = true;
			}
		
			ExtendedRoutePreferenceType.Weight restrWeight = erpt.getWeight();
			if (restrWeight != null)
			{
				vehicleAttrs[VehicleRestrictionCodes.MaxWeight] = getRestrictionValue(restrWeight.getStringValue(), restrWeight.getUnit());
				bHasAttrs = true;
			}
			
			
			ExtendedRoutePreferenceType.AxleLoad restrAxleLoad= erpt.getAxleLoad();
			if (restrAxleLoad != null)
			{
				vehicleAttrs[VehicleRestrictionCodes.MaxAxleLoad] = getRestrictionValue(restrAxleLoad.getStringValue(), restrAxleLoad.getUnit());
				bHasAttrs = true;
			}
			
			if (bHasAttrs)
			   routePlan.setVehicleAttributes(vehicleAttrs);
			
			ExtendedRoutePreferenceType.LoadCharacteristics loadCharacteristics = erpt.getLoadCharacteristics();
			
			if (loadCharacteristics != null)
			{
				routePlan.setLoadCharacteristics(loadCharacteristics.getLoadCharacteristicList());
			}
			
			if (erpt.isSetDifficultyLevel())
				routePlan.setSteepnessDifficultyLevel(erpt.getDifficultyLevel().getBigDecimalValue().intValue());
			
			if (erpt.isSetMaxSteepness())
				routePlan.setSteepnessMaxValue(erpt.getMaxSteepness().getBigDecimalValue().doubleValue());

			double[] wheelchairAttributes = new double[5];
			ExtendedRoutePreferenceType.SurfaceTypes surfaceTypes = erpt.getSurfaceTypes();
			try {
				wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = WheelchairRestrictionCodes.SURFACE_MAP.get(surfaceTypes.getSurfaceTypeArray(0));
			}
			catch (NullPointerException e) {
				wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = WheelchairRestrictionCodes.SURFACE_WORST;
			}
			
			ExtendedRoutePreferenceType.SmoothnessTypes smoothnessType = erpt.getSmoothnessTypes();
			try {
				wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = WheelchairRestrictionCodes.SMOOTHNESS_MAP.get(smoothnessType.getSmoothnessTypeArray(0));
			}
			catch (NullPointerException e) {
				wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = WheelchairRestrictionCodes.SMOOTHNESS_IMPASSABLE;
			}
			
			ExtendedRoutePreferenceType.TrackTypes trackType = erpt.getTrackTypes();
			try {
				wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = WheelchairRestrictionCodes.TRACKTYPE_MAP.get(trackType.getTrackTypeArray(0));
			}
			catch (NullPointerException e) {
				wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = WheelchairRestrictionCodes.TRACKTYPE_GRADE5;
			}
			
			ExtendedRoutePreferenceType.Incline incline = erpt.getIncline();
			try {
				wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = incline.getBigDecimalValue().doubleValue();
			}
			catch (NullPointerException e) {
				wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = (double)WheelchairRestrictionCodes.INCLINE_MAXIMUM;
			}
			
			ExtendedRoutePreferenceType.SlopedCurb slopedCurb = erpt.getSlopedCurb();
			try {
				wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = slopedCurb.getBigDecimalValue().doubleValue();
			}
			catch (NullPointerException e) {
				wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = WheelchairRestrictionCodes.SLOPED_CURB_MAXIMUM;
			}
			routePlan.setWheelchairAttributes(wheelchairAttributes);
		}
		
		
		//routePlanType.getAvoidList().getAvoidFeatureArray()
		///////////////////////////////////////
		//*** Get AvoidList ***
		if(routePlanType.isSetAvoidList()){
			AvoidListType avoidListType = routePlanType.getAvoidList();
			routePlan.setAvoidAreas(AvoidList.getAvoidPolygons(avoidListType));
			
			routePlan.setAvoidFeatureTypes(AvoidList.getAvoidFeatureTypes(avoidListType));
		}
		
		Routing rout = new Routing();

		///////////////////////////////////////
		//*** ROUTING ***
		
		PMap props = new PMap();
		boolean isDirectSegment = false;
		int nWayPoints = wayPoints.size();
		for(int i=0 ; i< nWayPoints-1 ; i++){
			WayPoint wpStart = wayPoints.get(i);
			WayPoint wpEnd = wayPoints.get(i+1);

			isDirectSegment = (wpStart.getCode() == 1);

			if (isDirectSegment)
			{
				props.put("direct_segment", true);

				if (i > 0)
				{
					if (wayPoints.get(i-1).getCode() != 1)
						props.put("snapped_point_start", true);
				}

				if (i+1 < nWayPoints-1)
				{
					if (wayPoints.get(i+1).getCode() != 1)
						props.put("snapped_point_end", true);
				}
			}

			rout.doRouting(routePlan, routeResult, wpStart, wpEnd, props);
			
			if (isDirectSegment)
                props.clear();
			
			if (nWayPoints > 2 && i < nWayPoints - 2)
			{
				rout.addStopOver(routePlan, routeResult);
			}
		}
    }
    
    private float getRestrictionValue(String value, String unit)
    {
		float res = 0.0f;
		
		if (value != null)
			res = Float.parseFloat(value);

    	if (unit == null)
    	{
    		return res;
    	}
    	else
    	{
    		switch(unit)
    		{
    		 case "t":
    			 return res;
    		 case "kg":
    			 return res/1000.0f;
    		 case "m":
    			 return res;
    		}
    		
    		return res;
    	}
    }
	
	/**
	 * Method that returns response XLSDocument
	 * @return XLSDocument
	 */
	public ResponseXLSDocument getResponseXLSDocument() {
		return mResponseXLSDocument;
	}
}
