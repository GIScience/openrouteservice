

package org.freeopenls.gml;

import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


import net.opengis.gml.AngleType;
import net.opengis.gml.CircleByCenterPointType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LengthType;

/**
 * Class for read the gml:ArcByCenterPoint Element<br>
 * And to create from the input data a polygon
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-05-15
 * @version 1.1 2008-04-22
 */
public class ArcByCenterPoint {
	/** Radius of the arc/circle*/
	private double mRadius;
	/** Coordinate of the CenterPoint*/
	private Coordinate mCenterPoint = null;
	/** CoordinateArray with all Points*/
	private Coordinate mPoints[] = null;
	/** The bearing of the arc at the start. Default = 0 ; Unit = Deegree*/
	private double mStartAngle = 0;
	/** The bearing of the arc at the end. Default = 360 ; Unit = Deegree*/
	private double mEndAngle = 360;
	
    /**
     * Constructor
     * 
     * @param targetSRS
     * @param circlePoint
     * @throws ServiceError
     */
	public ArcByCenterPoint(String targetSRS, CircleByCenterPointType circlePoint)throws ServiceError{
		
		//SRS in which the point is
		String sPointSRS = "EPSG:4326";

		//Radius
		LengthType length = circlePoint.getRadius();
		mRadius = getAngle(length.getDoubleValue());
		
		//Central Point
		DirectPositionType dpType = circlePoint.getPos();
		
		if(dpType.isSetSrsName())
			sPointSRS = dpType.getSrsName();
		
		mCenterPoint = Pos.getCoord(dpType.getStringValue());
		
		if(!sPointSRS.equals(targetSRS)){
			mCenterPoint = CoordTransform.transformGetCoord(sPointSRS, targetSRS, mCenterPoint);
		}
		
		//Angles
		if(circlePoint.isSetStartAngle()){
			AngleType angle = circlePoint.getStartAngle();
			mStartAngle = angle.getDoubleValue();
		}
		if(circlePoint.isSetEndAngle()){
			AngleType angle = circlePoint.getEndAngle();
			mEndAngle = angle.getDoubleValue();
		}
		
		//Calculation
		double dAngleDiff = mEndAngle - mStartAngle;
		int iNumberOfPoints = (int)mRadius;
		if(iNumberOfPoints < 8)
			iNumberOfPoints=8;
		
		double dAngle = mStartAngle;
		double dAngleIncrement = dAngleDiff / iNumberOfPoints;
		
		if((mEndAngle - mStartAngle) == 360){			// 360 Deegreee = Circle
			mPoints = new Coordinate[iNumberOfPoints+1];	//+1 -> because Start/End of Polygon

			for(int i=0 ; i < iNumberOfPoints ; i++){
				mPoints[i] = CoordTools.calculateBearingPoint(mCenterPoint, dAngle, mRadius);
				dAngle = dAngle + dAngleIncrement;
			}
			mPoints[mPoints.length-1] = mPoints[0];		//For a closed Polygon
		}
		else{
			mPoints = new Coordinate[iNumberOfPoints+2];			//+1 -> because StartPoint Arc
																		//+1 -> because Start/End of Polygon
			for(int i=1 ; i <= iNumberOfPoints+1 ; i++){
				mPoints[i] = CoordTools.calculateBearingPoint(mCenterPoint, dAngle, mRadius);
				dAngle = dAngle + dAngleIncrement;
			}
			mPoints[0] = mCenterPoint;					//For a closed Polygon
			mPoints[mPoints.length-1] = mPoints[0];
		}
	}

    /**
     * Method which return the CenterPoint from the Arc/CircleByCenterPoint given data
     * 
     * @return Coordinate
     * 				Returns the CenterPpoint of the arc/circle
     */
	public Coordinate getCenterPoint(){
		return mCenterPoint;
	}
	
    /**
     * Method which creates a Polygon from the Arc/CircleByCenterPoint given data
     * 
     * @return Polygon
     * 				Returns a created Polygon
     */
	public Polygon createPolygon(){
		GeometryFactory gf = new GeometryFactory();
		LinearRing ring = gf.createLinearRing(mPoints);
		return new Polygon(ring, null, gf);
	}
	
	private double getAngle(double distance){
		return (180/Math.PI)*distance/6378000;
	}
}
