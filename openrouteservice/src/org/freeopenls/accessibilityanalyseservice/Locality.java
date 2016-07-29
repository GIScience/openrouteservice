/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.accessibilityanalyseservice;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;


/**
 * <p><b>Title: Locality</b></p>
 * <p><b>Description:</b> </p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version @version 1.0 2007-01-05
 */
public class Locality{
	/** Time in which the Location is accessible **/
	public double dTime;
	/** Distance in which the Location is accessible **/
	public double dDistance;
	/** Coordinate of the Location **/
	public Coordinate cLocation;
	/** LineString of the Route to the Location **/
	public LineString lsRouteToLocation;
	/** Name of the Location **/
	public String sName;
	/** ID of the Location **/
	public String sID;
	
	public Locality(double dTime, double dDistance, Coordinate cLocation)
	{
		this(dTime, dDistance, cLocation, null , null , null);
	}

	/**
	 * Constructor
	 * 
	 * @param dTime - Time in which the Location is accessible
	 * @param dDistance - Distance in which the Location is accessible
	 * @param cLocation - Coordinate of the Location
	 * @param lsRouteToLocation - LineString of the Route to the Location
	 * @param sName - Name of the Location
	 * @param sID -ID of the Location
	 */
	public Locality(double dTime, double dDistance, Coordinate cLocation, LineString lsRouteToLocation, String sName, String sID) {
		this.dTime = dTime;
		this.dDistance = dDistance;
		this.cLocation = cLocation;
		this.lsRouteToLocation = lsRouteToLocation;
		this.sName = sName;
		this.sID = sID;
	}
}
