

package org.freeopenls.location;


import java.math.BigInteger;

import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;

import net.opengis.xls.AbstractLocationType;
import net.opengis.xls.AddressType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.PositionType;
import net.opengis.xls.RoutePlanType;
import net.opengis.xls.SeverityType;
import net.opengis.xls.WayPointListType;
import net.opengis.xls.WayPointType;


/**
 * Class for read the WayPointList<br>
 * Location can be one of four things:<br>
 * 1. Position<br>
 * Point, Ellipese, CircleByCenterPoint, CircularArc, Polygon, MultiPolygon<br>
 * 2. POI<br>
 * Point, Address, POIAttributList, PhoneNumber, Name<br>
 * 3. Address<br>
 * FreeFormAddress, StreetAddress<br>
 * 4. Point<br>
 * 
 * @author Pascal Neis, i3mainz, neis@geoinform.fh-mainz.de
 * @version 1.0 2007-07-24
 */
public class WayPointList {
	/** Default SpatialReferenceSystem in which the Coordinate transform to*/
	private String m_sTargetSRS = null;
	/** WayPoint Start */
	private WayPoint mSource = null;
	/** WayPoint Destination */
	private WayPoint mDestination = null;
	/** CoordinateArray of ViaPoints */
	private WayPoint[] mArrayViaPoints = null;

	/**
	 * Constructor - read the WayPointList<br>
	 * - StartLocation<br>
	 * - EndLocation<br>
	 * - if set: ViaLocation
	 * 
	 * @param routeplanType
	 * 			RoutePlanType
	 * @throws ServiceError
	 */
	public WayPointList(RoutePlanType routeplanType, String openlsLUSPath, String openlsDSPath)throws ServiceError{
		m_sTargetSRS = RouteService.GRAPH_SRS;
		//Location can be one of four things: Position, POI, Address or Point
		//Get WayPointList
		WayPointListType wplType = routeplanType.getWayPointList();
		WayPointType wpStart = wplType.getStartPoint();			//StartPoint
		WayPointType wpEnd = wplType.getEndPoint();				//EndPoint
		WayPointType wplArray[] = wplType.getViaPointArray();	//ViaPoint(s)
		
		//Get ViaLocations
		Integer iNumberViaPoints = wplArray.length;
		if (iNumberViaPoints > 0) {
			mArrayViaPoints = new WayPoint[iNumberViaPoints];
			for (int i = 0; i < iNumberViaPoints; i++){
				mArrayViaPoints[i] = checkWhichLocationType(wplArray[i], openlsLUSPath, openlsDSPath);
			}
		}
		
		mSource = checkWhichLocationType(wpStart, openlsLUSPath, openlsDSPath);
		mDestination = checkWhichLocationType(wpEnd, openlsLUSPath, openlsDSPath);
	}

	/**
	 * Method that returns a coordinate for the requested Location.<br>
	 * Checks what kind of Location it is:<br>
	 * -Position<br>
	 * -POI<br>
	 * -Address
	 * 
	 * @param alType
	 * 			AbstractLocationType
	 * @return Coordinate
	 * @throws ServiceError
	 */
	private WayPoint checkWhichLocationType(WayPointType wPoint, String openlsLUSPath, String openlsDSPath)throws ServiceError{
		AbstractLocationType alType = wPoint.getLocation();
		//Position
		if (alType instanceof PositionType){
			Position pos = new Position();
			pos.setPosition(m_sTargetSRS, (PositionType) alType.changeType(PositionType.type));
			return new WayPoint(pos.getPosition().getCoordinate(), wPoint.getCode().shortValue());
		}
		//PointOfInterest
		else if(alType instanceof PointOfInterestType){
			PointOfInterest poi = new PointOfInterest(openlsLUSPath, openlsDSPath);
			poi.setPointOfInterest(m_sTargetSRS, (PointOfInterestType) alType.changeType(PointOfInterestType.type));
			return new WayPoint(poi.getPosition().getCoordinate(), wPoint.getCode().shortValue());
		}
		//Address
		else if(alType instanceof AddressType){
			GeocodeAddress address = new GeocodeAddress(openlsLUSPath);
			return address.geocode((AddressType) alType.changeType(AddressType.type));
		}
		else{
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,"WayPointList",
					"Problem in WayPoint: No AbstractLocationType!");
			throw se;
		}
	}

	/**
	 * Method that returns Source/StartCoordinate
	 * 
	 * @return WayPoint
	 */
	public WayPoint getSource(){
		return mSource;
	}

	/**
	 * Method that returns Destination/EndCoordinate
	 * 
	 * @return WayPoint
	 */
	public WayPoint getDestination(){
		return mDestination;
	}

	/**
	 * Method that returns ViaPoints-WayPoints
	 * 
	 * @return WayPoint[]
	 */
	public WayPoint[] getArrayViaPoints(){
		return mArrayViaPoints;
	}
}