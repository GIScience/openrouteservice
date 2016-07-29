

package org.freeopenls.gml;

import org.freeopenls.error.ServiceError;

import com.vividsolutions.jts.geom.Coordinate;


import net.opengis.gml.MultiPolygonType;
import net.opengis.gml.PolygonPropertyType;

/**
 * Class for read the gml:MultiPolygon Element
 * 
 *  Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-05-18
 * @version 1.1 2008-04-22
 */
public class MultiPolygon {
	/** Array of Coordinate CenterPoints of the MultiPoygons */
	private Coordinate mCenterPoints[] = null;
	/** Array of Polygons of the MultiPolygons */
	private Polygon mPolygons[] = null;
	
	/**
	 * Constructor<br>
     * NOT Supported:<br>
     * AbstractGMLType - gid<br>
     * AbstractGeometryType - gml:id<br>
     * 
	 * @param targetSRS
	 * @param multipolygonType
	 * @throws ServiceError
	 */
	public MultiPolygon(String targetSRS, MultiPolygonType multipolygonType)throws ServiceError{
		PolygonPropertyType ppType[] = multipolygonType.getPolygonMemberArray();
		
		mCenterPoints = new Coordinate[ppType.length];
		mPolygons = new Polygon[ppType.length];
		
		for(int i=0 ; i < ppType.length ; i++){
			mPolygons[i] = new Polygon(targetSRS, ppType[i].getPolygon());
			mCenterPoints[i] = mPolygons[i].calculateCenterPoint();
		}
	}

	/**
     * Method which return Polygon Array.
     * 
     * @return Polygon[]
     * 				Returns Polygon Array
     */
	public Polygon[] getPolygons(){
		return mPolygons;
	}

	/**
     * Method which calculate a CenterPoint from ALL CenterPoints.
     * 
     * @return Coordinate
     * 				Returns Coordinate CenterPoint
     */
	public Coordinate calculateCenterPointOfAllPolygons(){
		Coordinate cCenterPoint = new Coordinate();
		double dXTMP = 0;
		double dYTMP = 0;
		Coordinate cTMP[] = mCenterPoints;
		
		for(int i=0 ; i<cTMP.length ; i++){
			dXTMP = dXTMP + cTMP[i].x;
			dYTMP = dYTMP + cTMP[i].y;
		}
		cCenterPoint.x = dXTMP / cTMP.length;
		cCenterPoint.y = dYTMP / cTMP.length;
		
		return cCenterPoint;
	}
}