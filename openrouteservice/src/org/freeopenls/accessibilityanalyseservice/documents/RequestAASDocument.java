/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.freeopenls.accessibilityanalyseservice.AASConfigurator;
import org.freeopenls.accessibilityanalyseservice.Locality;
import org.freeopenls.accessibilityanalyseservice.location.Location;
import org.freeopenls.routeservice.isochrones.IsochroneMap;
import org.freeopenls.routeservice.routing.RouteProfileManager;
import org.freeopenls.tools.TimeUtility;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileWriter;

import de.fhMainz.geoinform.aas.AASDocument;
import de.fhMainz.geoinform.aas.AASType;
import de.fhMainz.geoinform.aas.AbstractBodyType;
import de.fhMainz.geoinform.aas.AbstractHeaderType;
import de.fhMainz.geoinform.aas.AbstractRequestParametersType;
import de.fhMainz.geoinform.aas.AbstractResponseParametersType;
import de.fhMainz.geoinform.aas.AccessibilityPreferenceDistanceType;
import de.fhMainz.geoinform.aas.AccessibilityPreferenceTimeType;
import de.fhMainz.geoinform.aas.AccessibilityPreferenceType;
import de.fhMainz.geoinform.aas.AccessibilityResponseType;
import de.fhMainz.geoinform.aas.AccessibilitySettingsType;
import de.fhMainz.geoinform.aas.AccessibilityType;
import de.fhMainz.geoinform.aas.DetermineAccessibilityRequestType;
import de.fhMainz.geoinform.aas.DistanceUnitType;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.RequestType;
import de.fhMainz.geoinform.aas.ResponseHeaderType;
import de.fhMainz.geoinform.aas.ResponseType;
import de.fhMainz.geoinform.aas.SeverityType;


/**
 * Class for read and create AASDocument (Analyse Request and Analyse Response)
 *  
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 3.0 2006-12-22
 */
public class RequestAASDocument {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(RequestAASDocument.class.getName());
	private static final Logger mLoggerCounter = Logger.getLogger(RequestAASDocument.class.getName()+".Counter");
	
	/** RSConfigurator Instance */
	private AASConfigurator mAASConfigurator;
	/** GraphManager Instance */
	//private GraphManager mGraphManager;

	/** AASDocument for response */
	private AASDocument aasDocOut = null;
	/** AASType from AASDocOut */
	private AASType aasTypeOut = null;
	/** ResponseHeaderType from aasTypeOut */
	private ResponseHeaderType rhTypeOut = null;
	/** ResponseType from aasTypeOut */
	private ResponseType repType = null;
	/** Result List Accessibility Locality **/
	private ArrayList<Locality> listAccessibilityLocality = new ArrayList<Locality>();
	
	/** Isochrone map **/
	private IsochroneMap isochroneMap = null;
	/** Envelope of the Accessibility Polygons**/
	private com.vividsolutions.jts.geom.Envelope envelopeOfPolygons = null;
	
	/** Location Coordinate	**/
	private Coordinate mLocationCoordinate = new Coordinate();
	
	/** Graph SRSName **/
	private String mGraphSRS;
	
	/** Default SRSName **/
	private String defaultSRS;


	/**
	 * Constructor
	 * @param sSessionID
	 */
	public RequestAASDocument(String sSessionID) {
    	mAASConfigurator = AASConfigurator.getInstance();
		//mGraphManager = GraphManager.getInstance();

        ///////////////////////////////////////
		//*** Create New AASDocument ***
		this.aasDocOut = AASDocument.Factory.newInstance();

		this.aasDocOut.documentProperties().setVersion("1.0");
		this.aasDocOut.documentProperties().setEncoding("UTF-8");
		this.aasDocOut.documentProperties().setSourceName("source");
		
		this.aasTypeOut = this.aasDocOut.addNewAAS();
		this.aasTypeOut.setVersion(new BigDecimal("1.0"));

		//*** Header ***
		AbstractHeaderType ahTypeOut = aasTypeOut.addNewHeader();
		this.rhTypeOut = (ResponseHeaderType) ahTypeOut.changeType(ResponseHeaderType.type);
		
		if(sSessionID != null)
			this.rhTypeOut.setSessionID(sSessionID);

	//------
		//For well-formed XML-Doc
		XmlCursor cursor01 = aasDocOut.newCursor();
		if (cursor01.toFirstChild()) {
			cursor01.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance","schemaLocation"), "http://www.geoinform.fh-mainz.de/aas D:/Schemata/AAS1.0/AccessibilityService.xsd");
		}
		cursor01.dispose();
		
		XmlCursor cursor02 = aasTypeOut.newCursor();
		if (cursor02.toChild(new QName("http://www.geoinform.fh-mainz.de/aas", "_Header"))) {
			cursor02.setName(new QName("http://www.geoinform.fh-mainz.de/aas","ResponseHeader"));
		}
		cursor02.dispose();
	//------
	}

	/**
	 * Method that do the requested Analyse
	 * 
	 * @param sResponseSRS SRS in which the Response should be
	 * @param reqType RequestType
	 * @throws ServiceError
	 * @throws Exception
	 */
	public void addAnalyseRequest(String sResponseSRS, RequestType reqType)throws ServiceError, Exception{

        ///////////////////////////////////////
		//CLEAR
		this.listAccessibilityLocality.clear();

        ///////////////////////////////////////
		//*** SRSs ***
		//GeaphSRS
		this.mGraphSRS = this.mAASConfigurator.getGraphSRS();
		//DefaultSRS
		this.defaultSRS = "EPSG:4326";
		
		//Check SRS
		if(sResponseSRS.equals(this.mGraphSRS)){
			//Do nothing!
		}
		else if(sResponseSRS == null){
			sResponseSRS = this.mGraphSRS;
		}
		
        ///////////////////////////////////////
		//*** GetRequestParameters ***
		String sMethodName = reqType.getMethodName();		//Mandatory
		String sRequestID = reqType.getRequestID();			//Mandatory
		String sVersion = reqType.getVersion();				//Mandatory

		//Check MethodName
		if (!sMethodName.equalsIgnoreCase("AccessibilityRequest")) {
        	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.OTHER_XML,
                                 "MethodName Analyse",
                                 "The required value of the mandatory parameter 'methodname'" 
                                 + " must be 'AccessibilityRequest'. Delivered value was: " + sMethodName);
            throw se;
        }
		//Check RequestID
		if (sRequestID.equalsIgnoreCase("")) {
        	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED, "requestID",
                                 "The required value of the mandatory parameter 'requestID'" 
                                 + " is missing");
            throw se;
        }
		//Check Version
		if (sVersion.equalsIgnoreCase("")) {
        	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED, "version",
                                 "The required value of the mandatory parameter 'version'" 
                                 + " is missing");
            throw se;
        }
		if (!sVersion.equalsIgnoreCase("1.0")) {
        	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.REQUEST_VERSION_MISMATCH, "version",
                                 "Version of Request Schema not supported."
                                 +"The value of the mandatory parameter 'version'"
                                 + "must be '1.0'. Delivered value was: " + sVersion);
            throw se;
        }

        ///////////////////////////////////////
		// *** GetDetermineAccessibilityRequest ***
		AbstractRequestParametersType abreqparType = reqType.getRequestParameters();
		DetermineAccessibilityRequestType darType = (DetermineAccessibilityRequestType) abreqparType.changeType(DetermineAccessibilityRequestType.type);

		///////////////////////////////////////
		//*** A N A L Y S E ***
        ///////////////////////////////////////
		doAnalyse(darType);			
		
		
        ///////////////////////////////////////
		//*** Create Response ***
		//--- Body ---
		AbstractBodyType abTypeOut = this.aasTypeOut.addNewBody();
		this.repType = (ResponseType) abTypeOut.changeType(ResponseType.type);
		this.repType.setRequestID(sRequestID);
		this.repType.setVersion(sVersion);

		//--- AnalyseResponse ---
		AbstractResponseParametersType arespparamType = this.repType.addNewResponseParameters();
		AccessibilityResponseType arrespType = (AccessibilityResponseType) arespparamType.changeType(AccessibilityResponseType.type);
		
		//--- AnalyseSummary ---
		Envelope envAnalyse = new Envelope(sResponseSRS, envelopeOfPolygons);
		AccessibilitySummary.getAnalyseSummary(arrespType.addNewAccessibilitySummary(), this.mAASConfigurator, mGraphSRS, sResponseSRS, envAnalyse, this.listAccessibilityLocality.size());
		//arrespType.setAccessibilitySummary(ast);
		
		//--- AnalyseOutput --
		if(darType.isSetAccessibilityOutputRequest() && this.listAccessibilityLocality.size() > 0){
			arrespType.setAccessibilityOutputList(AccessibilityOutputList.getAnalyseOutputList(this.mAASConfigurator, darType.getAccessibilityOutputRequest(), mGraphSRS, sResponseSRS, this.listAccessibilityLocality));
		}
		//--- AnalyseGeometry ---
		if(darType.isSetAccessibilityGeometryRequest()){
			AccessibilityGeometry.getAnalyseGeometry(arrespType.addNewAccessibilityGeometry(), this.mAASConfigurator, darType.getAccessibilityGeometryRequest(), mGraphSRS, sResponseSRS, this.isochroneMap);
		}
		//--- AnalyseMap ---
		if(darType.isSetAccessibilityMapRequest()){
			Long lTime = System.currentTimeMillis();
			String sAnalyseRequestID = lTime.toString();
			AccessibilityMap.getAnalyseMaps(arrespType, this.mAASConfigurator, darType, mGraphSRS, sResponseSRS, envAnalyse.getEnvelopeType(), sAnalyseRequestID, mLocationCoordinate, this.isochroneMap);
		}

	// ---- For well formed XML-Doc
		XmlCursor cursor01 = this.aasTypeOut.newCursor();
		XmlCursor cursor02 = this.repType.newCursor();

		if (cursor01.toChild(new QName("http://www.geoinform.fh-mainz.de/aas", "_Body"))) {
			cursor01.setName(new QName("http://www.geoinform.fh-mainz.de/aas","Response"));
		}
		if (cursor02.toChild(new QName("http://www.geoinform.fh-mainz.de/aas", "_ResponseParameters"))) {
			cursor02.setName(new QName("http://www.geoinform.fh-mainz.de/aas","AccessibilityResponse"));
		}
		cursor01.dispose();
		cursor02.dispose();
	// ----
	}


	/**
	 * Method that do the Accessibility Analyse
	 * 
	 * @param drrType DetermineAccssibilityRequest
	 * @throws ServiceError
	 * @throws Exception
	 */
    private void doAnalyse(DetermineAccessibilityRequestType drrType)throws ServiceError, Exception{
		long startTime = System.currentTimeMillis();

    	///////////////////////////////////////
		//*** AccessibilityPreference ***
    	AccessibilityType accessType = drrType.getAccessibility();		// Mandatory
    	AccessibilityPreferenceType accesspreferenceType = accessType.getAccessibilityPreference();
		String sAnalysePreference = "";
		double dAnalyseValue = 0;
		double dDistanceWithin = 0;

		//Distance
		if(accesspreferenceType.isSetDistance()){
			AccessibilityPreferenceDistanceType prefdistType = accesspreferenceType.getDistance();
			if(prefdistType.isSetDistanceUnit()){
				DistanceUnitType.Enum distanceUnit = prefdistType.getDistanceUnit();
				dAnalyseValue = DistanceUnit.getDistanceInMeter(distanceUnit, prefdistType.getDistance().doubleValue());
			}
			else
				dAnalyseValue = prefdistType.getDistance().intValue();
			
			//dDistanceWithin = dDistance*2;
			dDistanceWithin = dAnalyseValue*1.5;
			sAnalysePreference = "Distance";
			
			if(dAnalyseValue < this.mAASConfigurator.getMinAnalysisValue()*17){
	        	ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
	            						"AnalysePreference Value",
	            						"The required value must be at least "+this.mAASConfigurator.getMinAnalysisValue()*17+" Meters!!");
	            throw se;
			}
			if(dAnalyseValue > this.mAASConfigurator.getMinAnalysisValue()*17){
	        	ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
	            						"AnalysePreference Value",
	            						"The required value may not be larger then "+this.mAASConfigurator.getMinAnalysisValue()*17/1000+" Kilometers!!");
	            throw se;
			}
		}
		//Time
		if(accesspreferenceType.isSetTime()){
			AccessibilityPreferenceTimeType preftimeType = accesspreferenceType.getTime();
			dAnalyseValue = Duration.getTimeInSeconds(preftimeType.getDuration());
			
			//dDistanceWithin = iTime * 40;	//144km/h
			dDistanceWithin = dAnalyseValue * 25;
			sAnalysePreference = "Time";
			
			if(dAnalyseValue < this.mAASConfigurator.getMinAnalysisValue()){
	        	ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
	            						"AnalysePreference Value",
	            						"The required value must be at least "+this.mAASConfigurator.getMinAnalysisValue()/60+" Minutes!");
	            throw se;
			}
			if(dAnalyseValue > this.mAASConfigurator.getMaxAnalysisValue()){
	        	ServiceError se = new ServiceError(SeverityType.ERROR);
	            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
	            						"AnalysePreference Value",
	            						"The required value may not be larger "+this.mAASConfigurator.getMaxAnalysisValue()/60+" Minutes!");
	            throw se;
			}
		}

		if(dAnalyseValue == 0 ){
        	ServiceError se = new ServiceError(SeverityType.ERROR);
            se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,
            						"AnalysePreference Value",
            						"The required value of the mandatory parameter is missing!!");
            throw se;
		}

		///////////////////////////////////////
		//*** GetLocationPoint ***
		Location loc = new Location(this.defaultSRS, this.mGraphSRS, accessType);
		this.mLocationCoordinate = loc.getCoordLocation();
		
		AccessibilitySettingsType settings = accessType.getAccessibilitySettings();
		String prefType = (settings != null && settings.getRoutePreference() != null && !Helper.isEmpty(settings.getRoutePreference().toString())) ? settings.getRoutePreference().toString() : "Car";
	
		String method = (settings != null && settings.isSetMethod() && settings.getMethod() != null) ? settings.getMethod().getStringValue() : null;
		double interval = (settings != null && settings.isSetInterval() && settings.getInterval() != null) ? settings.getInterval().getDoubleValue() : 0;
		
		this.isochroneMap = RouteProfileManager.getInstance().buildIsochroneMap(mLocationCoordinate.y, mLocationCoordinate.x, dAnalyseValue, prefType, method, interval, mAASConfigurator.getGridSize());
		if (isochroneMap != null && !isochroneMap.isEmpty())
		{
			this.envelopeOfPolygons = isochroneMap.getExtent();
		}
		else
		{
			this.envelopeOfPolygons = new com.vividsolutions.jts.geom.Envelope(0, 0, 0, 0);
		}
		
		mLoggerCounter.info(" doAnalyse | " + TimeUtility.getElapsedTime(startTime, true) + " | Location" + mLocationCoordinate);
    }
    
    public static void writeToShapefile(String fileName, ArrayList<Locality> localities)
    {
    	FeatureSchema featschema = new FeatureSchema();
		featschema.addAttribute("ID", AttributeType.INTEGER);
		featschema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
		featschema.addAttribute("TIME", AttributeType.DOUBLE);
		
	 	FeatureCollection featcoll = new FeatureDataset(featschema);
    	
	 	
		for(int i=0 ; i<localities.size() ; i++){
           BasicFeature featNew = new BasicFeature(featschema);
           Locality loc = localities.get(i);
           
           boolean bBreak = false;
           for(int j=0 ; j<localities.size() ; j++){
        	   if (i != j)
        	   {
        		   Locality loc2 = localities.get(j);
        		   if (loc2.cLocation.equals2D(loc.cLocation))
        		   {
        			   bBreak = true;
        			   break;
        		   }
        	   }
           }
           
           if (bBreak)
        	   continue;
        	   
           Geometry geom = new Point(loc.cLocation, null, 4326);
		   featNew.setGeometry(geom);
		   featNew.setAttribute("TIME", loc.dTime);
	       featNew.setAttribute("ID", i);
	       featcoll.add(featNew);			
	    }

	 	
		ShapefileWriter sfWriter2 = new ShapefileWriter();
		DriverProperties dpOut2 = new DriverProperties();
		dpOut2.set("File", fileName);
		dpOut2.set("ShapeType", "xy");
		try {
			sfWriter2.write(featcoll, dpOut2);
		} catch (Exception e) {
			System.out.println("ERROR: " + e);
		}

    }
	
	/**
	 * Method that returns AASDocument
	 * @return AASDocument
	 */
	public AASDocument getAASDoc() {
		return this.aasDocOut;
	}

}