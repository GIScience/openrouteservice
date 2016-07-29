
package org.freeopenls.location;

import org.freeopenls.error.ServiceError;
import org.freeopenls.gml.MultiPolygon;
import org.freeopenls.gml.Polygon;
import org.freeopenls.gml.Pos;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.CircleByCenterPointType;
import net.opengis.gml.PointType;
import net.opengis.xls.CircularArcType;
import net.opengis.xls.EllipseType;
import net.opengis.xls.PositionType;


/**
 * Class for read the xls:Position element
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, i3mainz, neis@geographie.uni-bonn.de
 * @version 1.0 2007-08-01
 */
public class Position{
	/** WayPoint of the Position **/
	private WayPoint mPosition = null;
	
	/**
	 * Method that set PositionType.<br>
	 * <br>
	 * TODO:<br>
	 * Only Point, Ellipse-CenterPoint, CircleByCenterPoint-CenterPoint,<br>
	 * CircularArc-CenterPoint, Polygon*-CenterPoint, MultiPolygon*-CenterPoint are supported.<br>
	 * *Read polygon and calculate from all points the CenterPoint.<br>
	 * Not supported Ellipse-Axis, CircleByCenterPoint all up to Pos,<br>
	 * CircularArc all up to Pos, QoP (QualityOfPositionType), Speed, Direction and Time.
	 * 
	 * @param sTargetSRS SRS in which the Coordinate should be (EPSG:XXXX) 
	 * @param posType PositionType
	 * @throws ServiceError
	 */
	public void setPosition(String sTargetSRS, PositionType posType)throws ServiceError{
		String sPointSRS = "EPSG:4326";
		
		//Point
		PointType pointType = posType.getPoint();
		if(pointType.isSetSrsName())
			sPointSRS = CoordTransform.getEPSGCode(pointType.getSrsName());
		
		mPosition = new WayPoint(Pos.getCoord(sTargetSRS, sPointSRS, pointType.getPos()));
		
	//CHOICE
		//Ellipse
		if(posType.isSetEllipse()){
			setEllipse(sTargetSRS, posType.getEllipse());
		}
	//---
		//CircleByCenterPoint
		if(posType.isSetCircleByCenterPoint()){
			setSetCircleByCenterPoint(sTargetSRS, posType.getCircleByCenterPoint());
		}
	//---
		//CircularArc
		if(posType.isSetCircularArc()){
			setCircularArc(sTargetSRS, posType.getCircularArc());
		}
	//---
		//Polygon
		if(posType.isSetPolygon()){
			Polygon poly = new Polygon(sTargetSRS, posType.getPolygon());
			mPosition = new WayPoint(poly.calculateCenterPoint());
		}
	//---
		//MultiPolygon
		if(posType.isSetMultiPolygon()){
			MultiPolygon multipoly = new MultiPolygon(sTargetSRS, posType.getMultiPolygon());
			mPosition = new WayPoint(multipoly.calculateCenterPointOfAllPolygons());
		}
	//End

		
		/*TODO	
		if(posType.isSetQoP()){}
		
		if(posType.isSetTime()){}
		if(posType.isSetSpeed()){}
		if(posType.isSetDirection()){}
			
		if(posType.isSetLevelOfConf()){}	//Optional
		*/

	}

	/**
	 * Method that set EllipseType
	 * 
	 * @param sTargetSRS SRS in which the Coordinate should be (EPSG:XXXX) 
	 * @param ellip EllipseType
	 * @throws ServiceError
	 */
	public void setEllipse(String sTargetSRS, EllipseType ellip)throws ServiceError{
		String sEllipSRS = "EPSG:4326";
		
		if(ellip.isSetSrsName())
			sEllipSRS = ellip.getSrsName();
		
		mPosition = new WayPoint(Pos.getCoord(sEllipSRS, sTargetSRS, ellip.getPos()));
		
		/*TODO
		LengthType lMajorAxType = ellip.getMajorAxis();
			double dMajorAxis = lMajorAxType.getDoubleValue();
		LengthType lMinorAxType = ellip.getMinorAxis();
			double dMinorAxis = lMinorAxType.getDoubleValue();
		AngleType angleType = ellip.getRotation();
			double dRotation = angleType.getDoubleValue();			//Unit: DecimalDegrees
		*/
	}

	/**
	 * Method that set CircleByCenterPoint
	 * 
	 * @param sTargetSRS SRS in which the Coordinate should be (EPSG:XXXX) 
	 * @param circlebycenter CircleByCenterPointType
	 * @throws ServiceError
	 */
	public void setSetCircleByCenterPoint(String sTargetSRS, CircleByCenterPointType circlebycenter)throws ServiceError{

		mPosition = new WayPoint(Pos.getCoord(sTargetSRS, null, circlebycenter.getPos()));
		
		/*TODO
		circlebycenter.getStartAngle();
		circlebycenter.getEndAngle();
		circlebycenter.getRadius();
		circlebycenter.getInterpolation();
		circlebycenter.getNumArc();
		*/
	}

	/**
	 * Method that set CircularArc
	 * 
	 * @param sTargetSRS SRS in which the Coordinate should be (EPSG:XXXX) 
	 * @param circulararc CircularArcType
	 * @throws ServiceError
	 */
	public void setCircularArc(String sTargetSRS, CircularArcType circulararc)throws ServiceError{
		String sCircularArcSRS = "EPSG:4326";
		
		if(circulararc.isSetSrsName())
			sCircularArcSRS = circulararc.getSrsName();
		
		mPosition = new WayPoint(Pos.getCoord(sTargetSRS, sCircularArcSRS, circulararc.getPos()));
		
		/*TODO
		circulararcType.getStartAngle();
		circulararcType.getEndAngle();
		circulararcType.getInnerRadius();
		circulararcType.getOuterRadius();
		circulararcType.getInterpolation();
		circulararcType.getNumArc();
		*/
	}

	/**
	 * Method that return WayPoint of the position
	 * @return WayPoint
	 */
	public WayPoint getPosition(){
		return mPosition;
	}
}
