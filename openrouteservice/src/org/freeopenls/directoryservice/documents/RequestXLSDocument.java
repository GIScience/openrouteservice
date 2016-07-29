/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.directoryservice.documents;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LinearRingType;
import net.opengis.xls.AbstractPOISelectionCriteriaType;
import net.opengis.xls.AbstractRequestParametersType;
import net.opengis.xls.AreaOfInterestType;
import net.opengis.xls.DirectoryRequestType;
import net.opengis.xls.DirectoryResponseType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.POILocationType;
import net.opengis.xls.POIPropertiesType;
import net.opengis.xls.POIPropertyType;
import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.RequestType;
import net.opengis.xls.SortDirectionType;
import net.opengis.xls.WithinBoundaryType;
import net.opengis.xls.WithinDistanceType;

import org.apache.log4j.Logger;
import org.freeopenls.constants.DirectoryService;
import org.freeopenls.constants.OpenLS.RequestParameter;
import org.freeopenls.database.PGConnection;
import org.freeopenls.directoryservice.DSConfigurator;
import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.location.Location;
import org.freeopenls.location.WayPoint;
import org.freeopenls.location.search.DistanceUnit;
import org.freeopenls.location.search.SearchPOI_Distance;
import org.freeopenls.location.search.SearchPOI_Name;
import org.freeopenls.location.search.SearchPOI_Nearest;
import org.freeopenls.location.search.SearchPOI_WithinBoundary;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferBuilder;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * <b>Title: RequestXLSDocument</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for read and create XLSDocument (Directory Request
 * and Directory Response).
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2008
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-08-02
 * @version 1.1 2008-04-24
 */
public class RequestXLSDocument {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger
			.getLogger(RequestXLSDocument.class.getName());
	/** DSConfigurator Instance */
	private DSConfigurator mDSConfigurator;
	/** Response Document */
	private ResponseXLSDocument mResponseXLSDocument;

	/** SRS in which the Response should be */
	private String mResponseSRS = null;
	/** Language in which the Response should be */
	private String mResponseLanguage = null;

	/** Number of maximum Responses */
	private int mMaximumResponses = 50;

	/**
	 * Constructor - Create new XLSDocument
	 * 
	 * @param sessionID
	 * @param responseSRS
	 * @param responseLanguage
	 */
	public RequestXLSDocument(String sessionID, String responseSRS,
			String responseLanguage) {
		mResponseSRS = responseSRS;
		mResponseLanguage = responseLanguage;
		mDSConfigurator = DSConfigurator.getInstance();

		mResponseXLSDocument = new ResponseXLSDocument(sessionID);
	}

	/**
	 * Method that read DirectoryRequest and create the DirectoryResponse
	 * 
	 * @param requestType
	 *            RequestType - RouteRequest
	 * @throws ServiceError
	 * @throws Exception
	 */
	public void doDirectoryRequest(RequestType requestType)
			throws ServiceError, Exception {

		// *** GetRequestParameters ***
		// Parameter MaximumResponses is Optional
		if (requestType.isSetMaximumResponses())
			if (requestType.getMaximumResponses().intValue() <= 0)
				throw ErrorTypes
						.parameterMissing(RequestParameter.maximumResponses
								.toString());
			else
				mMaximumResponses = requestType.getMaximumResponses()
						.intValue();
		// Parameter MethodName is Mandatory
		if (!requestType.getMethodName().equalsIgnoreCase(
				DirectoryService.METHODNAME))
			throw ErrorTypes.methodNameError(requestType.getMethodName(),
					DirectoryService.METHODNAME);
		// Parameter RequestID is Mandatory
		if (requestType.getRequestID().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.requestID
					.toString());
		// Parameter Version is Mandatory
		if (requestType.getVersion().equalsIgnoreCase(""))
			throw ErrorTypes.parameterMissing(RequestParameter.version
					.toString());
		// Parameter Version is Mandatory
		if (!requestType.getVersion().equalsIgnoreCase(
				DirectoryService.SERVICE_VERSION))
			throw ErrorTypes.parameterMissing(RequestParameter.version
					.toString());

		// *** GetDetermineRouteRequest ***
		AbstractRequestParametersType abreqparType = requestType
				.getRequestParameters();
		DirectoryRequestType directoryRequest = (DirectoryRequestType) abreqparType
				.changeType(DirectoryRequestType.type);

		POIWithDistanceType[] pois = null;
		DistanceUnitType.Enum distanceUnit = DistanceUnitType.M;
		String sortCriteria = "Distance";
		SortDirectionType.Enum sortDirection = SortDirectionType.ASCENDING;
		DistanceType minDistance = null;
		DistanceType maxDistance = null;
		WayPoint wayPoint = null;
		String poiPropertyName = null;
		String poiPropertyValue = null;

		// Optional
		if (directoryRequest.isSetDistanceUnit()) {
			distanceUnit = directoryRequest.getDistanceUnit();
		}
		if (directoryRequest.isSetSortCriteria()) {
			if (directoryRequest.getSortCriteria().equalsIgnoreCase("Distance"))
				sortCriteria = "Distance";
			else if (directoryRequest.getSortCriteria()
					.equalsIgnoreCase("Name"))
				sortCriteria = "Name";
			else {
				mLogger.error("Not Supported SortCriteria: "
						+ directoryRequest.getSortCriteria());
				throw ErrorTypes.notSupported("sortCriteria",
						"'Distance' or 'Name'. " + "Your Value was: "
								+ directoryRequest.getSortCriteria());
			}
		}
		if (directoryRequest.isSetSortDirection()) {
			sortDirection = directoryRequest.getSortDirection();
		}

		// Mandatory
		String directoryName = "OSM"; // TODO
		AbstractPOISelectionCriteriaType abstractPOISelectionCriteria = directoryRequest
				.getPOISelectionCriteria();
		POIPropertiesType poiProperties = (POIPropertiesType) abstractPOISelectionCriteria
				.changeType(POIPropertiesType.type);
		if (poiProperties.isSetDirectoryType()) {
			directoryName = poiProperties.getDirectoryType();
			if (!directoryName.equalsIgnoreCase("OSM"))
				throw ErrorTypes.notSupported("DirectoryType",
						"Only 'OSM' is supported!");
			else
				directoryName = "OSM";
		}

		for (int index = 0; index < poiProperties.sizeOfPOIPropertyArray(); index++) {
			POIPropertyType poiProperty = (POIPropertyType) poiProperties
					.getPOIPropertyArray(index);
			poiPropertyName = poiProperty.getName().toString();
			poiPropertyValue = poiProperty.getValue();
			// System.out.println("name:"+poiProperty.getName()+" value:"+poiProperty.getValue());

			if (poiPropertyName.equals(""))
				throw ErrorTypes.parameterMissing("POIProperty name=\"\"");
			if (poiPropertyValue.equals(""))
				throw ErrorTypes.parameterMissing("POIProperty value=\"\"");

			if (poiPropertyName.equalsIgnoreCase("keyword")) {
				if (!poiPropertyValue.equals("public_tran")
						&& !poiPropertyValue.equals("amenity")
						&& !poiPropertyValue.equals("shop")
						&& !poiPropertyValue.equals("tourism")
						&& !poiPropertyValue.equals("sport")
						&& !poiPropertyValue.equals("leisure")
						&& !poiPropertyValue.equals("historic")
						&& !poiPropertyValue.equals("disabled")){
					
					throw ErrorTypes
							.notSupported(
									"POIProperty - Value",
									"Only 'public_tran', "
											+ "'amenity', 'shop', 'tourism', 'sport', 'leisure', 'historic' or 'disabled' are supported! Delivered value was: '"
											+ poiPropertyValue + "'");
				}else if(poiPropertyValue.equals("disabled")){
					poiPropertyName = poiPropertyValue;
					poiPropertyValue = "wheelchair:yes";
				}
				else {
					poiPropertyName = poiPropertyValue;
					poiPropertyValue = "y";
				}
			}else if (poiPropertyName.equalsIgnoreCase("NAICS_type")) {
				poiPropertyName = "type";
			} else if (poiPropertyName.equalsIgnoreCase("NAICS_subType")) {
				poiPropertyName = "type"; // TODO Zuweisung falsch
			} else if (poiPropertyName.equalsIgnoreCase("POIName")) {
				poiPropertyName = "name"; // TODO Zuweisung falsch
			}else if (poiPropertyName.equalsIgnoreCase("OSM_KEYS_VALUES")) {
				//do nothing and call key value parser in search method
			}else
				throw ErrorTypes.notSupported("POIProperty - Name",
						"Only 'keyword' or 'NAICS_type' are supported!");
		}
		
		

		// for Use Case 3, 4
		if (directoryRequest.isSetPOILocation()) {
			POILocationType poiLocation = directoryRequest.getPOILocation();

			// if(poiLocation.isSetAddress()){} //TODO
			if (poiLocation.isSetWithinDistance()) {
				WithinDistanceType within = poiLocation.getWithinDistance();

				Location location = new Location(within.getLocation(),	mDSConfigurator);
				wayPoint = location.getWayPoint();
				
				if(within.isSetMinimumDistance()&& !within.isSetMaximumDistance()){
					minDistance = within.getMinimumDistance();
					maxDistance = DistanceType.Factory.newInstance();
					maxDistance.setValue(minDistance.getValue().add(new BigDecimal(500))); // add 500 to minDistance if maxDistance is not set
					//System.out.println("Min: "+minDistance.getValue()+" !Max: "+maxDistance.getValue());
				
				}else if(!within.isSetMinimumDistance()&&within.isSetMaximumDistance()){
					minDistance = DistanceType.Factory.newInstance();
					minDistance.setValue(new BigDecimal(0.0)); // set default value for minDistance
					maxDistance = within.getMaximumDistance();
					//System.out.println("!Min: "+minDistance.getValue()+" Max: "+maxDistance.getValue());
				
				}else if(within.isSetMinimumDistance()&&within.isSetMaximumDistance()){
					minDistance = within.getMinimumDistance();
					maxDistance = within.getMaximumDistance();
					//System.out.println("Min: "+minDistance.getValue()+" Max: "+maxDistance.getValue());
				
				}else{ // get default values from config file
					minDistance = DistanceType.Factory.newInstance();
					minDistance.setValue(new BigDecimal(mDSConfigurator.getMinDistance()));
					maxDistance = DistanceType.Factory.newInstance();
					maxDistance.setValue(new BigDecimal(mDSConfigurator.getMaxDistance()));
					//System.out.println("!Min: "+minDistance.getValue()+" !Max: "+maxDistance.getValue());
				}
				
// ***				
//				if (within.isSetMinimumDistance())
//					minDistance = within.getMinimumDistance();
//				else {
//					minDistance = DistanceType.Factory.newInstance();
//					minDistance.setValue(new BigDecimal(0.0)); // set default value for minDistance		
//				}
//				
//				if (within.isSetMinimumDistance()
//						&& !within.isSetMaximumDistance()) {
//					// set maxDistance default (5 km > minDistance) if it isn't set (to
//					// prevent a very large result list)
//					maxDistance = DistanceType.Factory.newInstance();
//					maxDistance.setValue(minDistance.getValue().add(new BigDecimal(5000.0)));
//					maxDistance.setUom(DistanceUnitType.M);
//				}
//				
//				if (within.isSetMaximumDistance())
//					maxDistance = within.getMaximumDistance();
//				else {
//					// set default value for max Distance
//					maxDistance = DistanceType.Factory.newInstance();
//					maxDistance.setValue(new BigDecimal(300.0)); // default value for maxDistance
//					// set unit for distance
//					minDistance.setUom(DistanceUnitType.M);
//					maxDistance.setUom(DistanceUnitType.M);
//				}
// ***
				
				// check , if inputs are between interval borders 
				double maxDistanceInMeter = maxDistance.getValue()
						.doubleValue()
						* DistanceUnit.getUnitParameter(maxDistance.getUom());

				if (maxDistanceInMeter > mDSConfigurator.getMaxDistance())
					throw ErrorTypes.notSupported("Distance", "Distance < '"
							+ mDSConfigurator.getMaxDistance() + "' m");

				double minDistanceInMeter = minDistance.getValue()
						.doubleValue()
						* DistanceUnit.getUnitParameter(minDistance.getUom());

				if (minDistanceInMeter < mDSConfigurator.getMinDistance())
					throw ErrorTypes.notSupported("Distance", "Distance > '"
							+ mDSConfigurator.getMinDistance() + "' m");


				PGConnection tmp = mDSConfigurator.getConnectionManager().getFreeConnection();
				SearchPOI_Distance searchPOI = new SearchPOI_Distance(mDSConfigurator.getConnectionManager().getConnParamterDB(), tmp);

				pois = searchPOI.SearchInDB(DirectoryService.DATABASE_SRS,
						mResponseSRS, wayPoint.getCoordinate(), minDistanceInMeter,
						maxDistanceInMeter, mMaximumResponses, 
						poiPropertyName.toLowerCase(), poiPropertyValue.toLowerCase(), "WithinDistance");
				mDSConfigurator.getConnectionManager().enableConnection(tmp.getConnectionNumber());

			}else if (poiLocation.isSetWithinBoundary()) {
				//TODO Fill new method
				
				WithinBoundaryType wbtype = poiLocation.getWithinBoundary();

				AreaOfInterestType aoi = wbtype.getAOI();
				
				Polygon searchPolygon = null;
				
				// get geometry from AOI
				if(aoi.isSetEnvelope()){
					
					//convert an envelope to a bbox polygon
					GeometryFactory geomfac = new GeometryFactory();
					DirectPositionType[] dpt = aoi.getEnvelope().getPosArray();
					
					ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
					
					for (int i=0; i < dpt.length; i++){
						String[] tmppos = dpt[i].getStringValue().split(" ");
						Coordinate tmp = new Coordinate(new Double(tmppos[0]), new Double(tmppos[1]));
						coords.add(tmp);
					}
					
					// Build search polygon from envelope coordinates
					ArrayList<Coordinate> polygonCoords = new ArrayList<Coordinate>();
					polygonCoords.add(new Coordinate(coords.get(0).x, coords.get(0).y));
					polygonCoords.add(new Coordinate(coords.get(1).x, coords.get(0).y));
					polygonCoords.add(new Coordinate(coords.get(1).x, coords.get(1).y));
					polygonCoords.add(new Coordinate(coords.get(0).x, coords.get(1).y));
					polygonCoords.add(new Coordinate(coords.get(0).x, coords.get(0).y));

					LinearRing lr = geomfac.createLinearRing(polygonCoords.toArray(new Coordinate[polygonCoords.size()]));
					searchPolygon = geomfac.createPolygon(lr, null);
					
					if(aoi.getEnvelope().isSetSrsName()){
						searchPolygon.setSRID(new Integer(CoordTransform.getEPSGCodeNumber(aoi.getEnvelope().getSrsName())));
					}else{
						//TODO Exception SRID is not set
					}
					
				}else if(aoi.isSetPolygon()){
					//converts an envelope to a bbox polygon
					GeometryFactory geomfac = new GeometryFactory();
					
						LinearRingType lrt = (LinearRingType)aoi.getPolygon().getExterior().getRing();
						DirectPositionType[] dpt = lrt.getPosArray();
						
					ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
					
					for (int i=0; i < dpt.length; i++){
						String[] tmppos = dpt[i].getStringValue().split(" ");
						Coordinate tmp = new Coordinate(new Double(tmppos[0]), new Double(tmppos[1]));
						coords.add(tmp);
					}

					LinearRing lr = geomfac.createLinearRing(coords.toArray(new Coordinate[coords.size()]));
					searchPolygon = geomfac.createPolygon(lr, null);
					if(aoi.getPolygon().isSetSrsName()){
						searchPolygon.setSRID(new Integer(CoordTransform.getEPSGCodeNumber(aoi.getPolygon().getSrsName())));
					}else{
						//TODO Exception SRID is not set
					}
					
					
				}else if(aoi.isSetCircleByCenterPoint()){
					
					DirectPositionType dpt = aoi.getCircleByCenterPoint().getPos();
					String[] tmppos = dpt.getStringValue().split(" ");
					Coordinate coord = new Coordinate(new Double(tmppos[0]), new Double(tmppos[1]));
					GeometryFactory geomfac = new GeometryFactory();
					Point centerpoint = geomfac.createPoint(coord);
					
					double radius = aoi.getCircleByCenterPoint().getRadius().getDoubleValue();
					
					
					// only supported m and km
					//TODO implement function to convert from miles, yard, ... to m 
					if(aoi.getCircleByCenterPoint().getRadius().getUom().compareToIgnoreCase("km")==0){
						radius = radius*360/(2*Math.PI*6371);

					}else if(aoi.getCircleByCenterPoint().getRadius().getUom().compareToIgnoreCase("m")==0){
						radius = radius*360/(2*Math.PI*6371000);

					}
					if(aoi.getCircleByCenterPoint().getPos().isSetSrsName()){
						centerpoint.setSRID(new Integer(CoordTransform.getEPSGCodeNumber(aoi.getCircleByCenterPoint().getPos().getSrsName())));	
					}else{
						centerpoint.setSRID(4326);
					}
					
					// create circle buffer
					BufferParameters bufParams = new BufferParameters();
					BufferBuilder bb = new BufferBuilder(bufParams);
					Coordinate[] spcoords = bb.buffer(centerpoint, radius).getBoundary().getCoordinates();
					
					// converts buffer to polygon
					LinearRing lr = geomfac.createLinearRing(spcoords);
					searchPolygon = geomfac.createPolygon(lr, null);

					if(aoi.getCircleByCenterPoint().getPos().isSetSrsName()){
						searchPolygon.setSRID(new Integer(CoordTransform.getEPSGCodeNumber(aoi.getCircleByCenterPoint().getPos().getSrsName())));
						
					}else{
						centerpoint.setSRID(4326);
					}
					
					
				}else{
					//TODO Exception: "no AOI set"
				}
				
		
				PGConnection tmp = mDSConfigurator.getConnectionManager()
				.getFreeConnection();
				
				SearchPOI_WithinBoundary searchPOI = new SearchPOI_WithinBoundary(
						mDSConfigurator.getConnectionManager()
								.getConnParamterDB(), tmp);
				
				
				if(searchPolygon.isValid()){
					pois = searchPOI.SearchInDB(DirectoryService.DATABASE_SRS,
							mResponseSRS, searchPolygon,
							mMaximumResponses, poiPropertyName.toLowerCase(), poiPropertyValue.toLowerCase(),
							"WithinBoundary");	
				}else{
					//TODO Exception: Polygon is not valid
				}
				
				
				
				mDSConfigurator.getConnectionManager().enableConnection(
						tmp.getConnectionNumber());
				
			}else if (poiLocation.isSetNearest()) {
				net.opengis.xls.NearestType nearest = poiLocation.getNearest();

				Location location = new Location(nearest.getLocationArray(0),
						mDSConfigurator);
				wayPoint = location.getWayPoint();

				PGConnection tmp = mDSConfigurator.getConnectionManager()
						.getFreeConnection();

				SearchPOI_Distance searchPOI = new SearchPOI_Distance(
						mDSConfigurator.getConnectionManager()
								.getConnParamterDB(), tmp);

				SearchPOI_Nearest spn = new SearchPOI_Nearest(mDSConfigurator
						.getConnectionManager().getConnParamterDB(), tmp);

				pois = spn.findNearestPOI(
						DirectoryService.DATABASE_SRS, mResponseSRS, wayPoint
								.getCoordinate(), mMaximumResponses,
						poiPropertyName.toLowerCase(), poiPropertyValue
								.toLowerCase(), "Nearest");

//				pois = new POIWithDistanceType[1];
//				pois[0] = poi;
				mDSConfigurator.getConnectionManager().enableConnection(
						tmp.getConnectionNumber());

			}
			// else if(poiLocation.isSetWithinBoundary()){} //TODO
			else
				throw ErrorTypes.notSupported("POILocation",
						"Only 'WithinDistance' and 'Nearest' are supported!");
		} else {
			// TODO POILocation ist nicht gesetzt -> allgemeine Suche, ohne
			// Standort
			
			// TODO Was ist hier sinnvoll? z.B. Beschränkung auf BBox des Anzeigefensters?
			PGConnection tmp = mDSConfigurator.getConnectionManager()
					.getFreeConnection();
			SearchPOI_Name searchPOIn = new SearchPOI_Name(mDSConfigurator
					.getConnectionManager().getConnParamterDB(), tmp);
			pois = searchPOIn.SearchInDB(DirectoryService.DATABASE_SRS,
					mResponseSRS, mMaximumResponses, poiPropertyName
							.toLowerCase(), poiPropertyValue.toLowerCase(), "SearchByName");

			
			mDSConfigurator.getConnectionManager().enableConnection(
					tmp.getConnectionNumber());
			
			/*
			 * throw ErrorTypes .notSupported("POILocation is missing!",
			 * "Please use the optional 'POILocation WithinDistance' element!");
			 */
		}

		// /////////////////////////////////////
		// *** Create Response ***
		mResponseXLSDocument.createResponse(requestType.getRequestID(),
				requestType.getVersion(), new BigInteger("" + pois.length));
		DirectoryResponseType directoryResponse = mResponseXLSDocument
				.addResponseParameters();
		directoryResponse.setPOIContextArray(pois);
		mResponseXLSDocument.doWellFormedDirectoryResponse();

	}

	// private double getDistance(double angle){
	// return 6378000*angle/(180/Math.PI);
	// }

	// private double getAngle(double distance){
	// return (180/Math.PI)*distance/6378000;
	// }

	/**
	 * Method that returns response XLSDocument
	 * 
	 * @return XLSDocument
	 */
	public ResponseXLSDocument getResponseXLSDocument() {
		return mResponseXLSDocument;
	}
}
