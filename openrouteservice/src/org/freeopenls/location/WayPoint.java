/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.location;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p><b>Title: WayPoint</b></p>
 * <p><b>Description:</b> Class for waypoints of a route. </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-04-15
 */
public class WayPoint {
	/** original coordinate of the wayPoint */
	private Coordinate mCoordinate = null;
	private short mCode = 0;

	/**
	 * toString()
	 */
	public String toString(){
	
		return "Waypoint Coordinate: "+mCoordinate;
	}
	
	public WayPoint(Coordinate coodinate){
		mCoordinate = coodinate;
	}
	/**
	 * Constructor
	 * @param coodinate
	 */
	public WayPoint(Coordinate coodinate, short code){
		mCoordinate = coodinate;
		mCode = code;
	}

	public WayPoint(Coordinate coodinate, String streetName){
		mCoordinate = coodinate;
		//mStreetName = streetName;
	}

	/**
	 * @return the coordinate
	 */
	public Coordinate getCoordinate() {
		return mCoordinate;
	}
	
	public short getCode()	{
		return mCode;
	}
}
