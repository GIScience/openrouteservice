

package org.freeopenls.location;


import org.freeopenls.constants.DirectoryService;
import org.freeopenls.directoryservice.DSConfigurator;
import org.freeopenls.error.ServiceError;

import net.opengis.xls.AbstractLocationType;
import net.opengis.xls.AddressType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.PositionType;
import net.opengis.xls.SeverityType;



/**
 * Class for read the WayPointList<br>
 * Location can be one of four things:<br>
 * 1. Position<br>
 * Point, Ellipse, CircleByCenterPoint, CircularArc, Polygon, MultiPolygon<br>
 * 2. POI<br>
 * Point, Address, POIAttributList, PhoneNumber, Name<br>
 * 3. Address<br>
 * FreeFormAddress, StreetAddress<br>
 * 4. Point<br>
 * 
 * @author Pascal Neis
 * @version 1.0 2007-07-24
 */
public class Location {
	/** Default SpatialReferenceSystem in which the Coordinate transform to*/
	private String m_sTargetSRS = null;
	/** WayPoint Location */
	private WayPoint mWayPoint = null;
	
	/** DSConfigurator */
	private DSConfigurator mDSConfigurator;

	/**
	 * Constructor
	 * 
	 * @throws ServiceError
	 */
	public Location(AbstractLocationType location, DSConfigurator dsConfigurator)throws ServiceError{
		
		mDSConfigurator = dsConfigurator;
		m_sTargetSRS = DirectoryService.DATABASE_SRS;
		
		//Position
		if (location instanceof PositionType){
			Position pos = new Position();
			pos.setPosition(m_sTargetSRS, (PositionType) location.changeType(PositionType.type));
			mWayPoint = pos.getPosition();
		}
		//PointOfInterest
		else if(location instanceof PointOfInterestType){
			PointOfInterest poi = new PointOfInterest(mDSConfigurator.getOpenLSLocationUtilityServicePath(), 
					mDSConfigurator.getOpenLSDirectoryServicePath());
			poi.setPointOfInterest(m_sTargetSRS, (PointOfInterestType) location.changeType(PointOfInterestType.type));
			mWayPoint =  poi.getPosition();
		}
		//Address
		else if(location instanceof AddressType){
			GeocodeAddress address = new GeocodeAddress(mDSConfigurator.getOpenLSLocationUtilityServicePath());
			mWayPoint = address.geocode((AddressType) location.changeType(AddressType.type));
		}
		else{
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.VALUE_NOT_RECOGNIZED,"Location",
					"Problem in Location: No AbstractLocationType!");
			throw se;
		}
	}

	/**
	 * Method that returns Source/StartCoordinate
	 * 
	 * @return WayPoint
	 */
	public WayPoint getWayPoint(){
		return mWayPoint;
	}
}