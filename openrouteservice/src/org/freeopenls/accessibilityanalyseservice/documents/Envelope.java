/*********************************************************************
 Copyright (C) 2007 by Pascal Neis

            #########       
   &&&&    ###########		*** i3mainz ***
    &&    ####    ####
   &&&&          ####		University of Applied Sciences FH Mainz,
   &&&&        ######		Department of Geoinformatics and Surveying
   &&&&          #####
   &&&&   ####     ###		Holzstrasse 36
   &&&&   #####   ####		Germany - 55116 Mainz
   &&&&    ##########
            ########

 **********************************************************************
 Mail: neis@geoinform.fh-mainz.de
 **********************************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.text.DecimalFormat;

import org.freeopenls.constants.RouteService;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Class for create EnvelopeType
 *
 * @author Pascal Neis, i3mainz, neis@geoinform.fh-mainz.de
 * @version 1.0 2006-10-30
 */
public class Envelope {
  /** EnvelopeType **/
  private EnvelopeType m_envType;
  /** Minimum X value of the envelope **/
  private double dMinX = Double.MAX_VALUE;
  /** Minimum y value of the envelope **/
  private double dMinY = Double.MAX_VALUE;
  /** Maximum X value of the envelope **/
  private double dMaxX = Double.MIN_VALUE;
  /** Minimum Y value of the envelope **/
  private double dMaxY = Double.MIN_VALUE;
  /** Coordinate of the upper-right corner of the envelope **/
  private Coordinate cUpperCorner = null;
  /** Coordinate of the lower-left corner of the envelope **/
  private Coordinate cLowerCorner = null;
  /** Number of Points from which the envelope is created **/
  private int iIndexOfPoints = 0;
  /** Radius in Meters [m] */
  private double mRadiusInMeters = 0;

  /**
   * Constructor for create EnvelopeType
   * 
   * @param sSRS
   * @param cLower
   * @param cUpper
   */
	public Envelope(String sSRS, Coordinate cLower, Coordinate cUpper){
		//Set number of points
		this.iIndexOfPoints = 2;
				
		//Set Upper/Lower Corner
		this.cLowerCorner = cLower;
		this.cUpperCorner = cUpper;

		//Create EnvelopeType
		m_envType = EnvelopeType.Factory.newInstance();
		DirectPositionType positionLowerCorner = m_envType.addNewPos();
		positionLowerCorner.setStringValue(cLowerCorner.x+" "+cLowerCorner.y);
		DirectPositionType positionUpperCorner = m_envType.addNewPos();
		positionUpperCorner.setStringValue(cUpperCorner.x+" "+cUpperCorner.y);
		
		//Set Radius
		if(!sSRS.equals("EPSG:4326")){
			double dRadiusX = (cUpperCorner.x - cLowerCorner.x)/2;
			double dRadiusY = (cUpperCorner.y - cLowerCorner.y)/2;
			
			if(dRadiusX > dRadiusY)
				mRadiusInMeters = dRadiusX;
			else
				mRadiusInMeters = dRadiusY;
		}
		else{
			DefaultGeographicCRS GDC = DefaultGeographicCRS.WGS84;
			mRadiusInMeters =  GDC.distance(new double[]{cUpperCorner.x , cUpperCorner.y}, new double[]{cLowerCorner.x , cLowerCorner.y}).doubleValue();
		}
			
		
		//Set SRS
		m_envType.setSrsName(sSRS);
	}

     /**
     * Construktor for create EnvelopeType
     * 
     * @param c Coordinate Array from which the EvnelopeType is created
     */
	public Envelope(String sSRS, Coordinate[] c){
		//Set number of points
		this.iIndexOfPoints = c.length;
		
		//Sort, to find Min & Max values
		for (int i = 0; i < c.length; i++) {
			if (this.dMinX > c[i].x)
				this.dMinX = c[i].x;
			if (this.dMaxX < c[i].x)
				this.dMaxX = c[i].x;
			if (this.dMinY > c[i].y)
				this.dMinY = c[i].y;
			if (this.dMaxY < c[i].y)
				this.dMaxY = c[i].y;
		}
		
		//Set Upper/Lower Corner
		this.cUpperCorner = new Coordinate(dMaxX, dMaxY);
		this.cLowerCorner = new Coordinate(dMinX, dMinY);

		//For Nice Output!?!
		DecimalFormat df = new DecimalFormat( "0.000" );

		//Create EnvelopeType
		m_envType = EnvelopeType.Factory.newInstance();
		DirectPositionType positionLowerCorner = m_envType.addNewPos();
		positionLowerCorner.setStringValue(df.format(this.dMinX).replace(",",".")+" "+df.format(this.dMinY).replace(",","."));
		DirectPositionType positionUpperCorner = m_envType.addNewPos();
		positionUpperCorner.setStringValue(df.format(this.dMaxX).replace(",",".")+" "+df.format(this.dMaxY).replace(",","."));
		
		//Set Radius
		if(!sSRS.equals("EPSG:4326")){
			double dRadiusX = (cUpperCorner.x - cLowerCorner.x)/2;
			double dRadiusY = (cUpperCorner.y - cLowerCorner.y)/2;
			
			if(dRadiusX > dRadiusY)
				mRadiusInMeters = dRadiusX;
			else
				mRadiusInMeters = dRadiusY;
		}
		else{
			DefaultGeographicCRS GDC = DefaultGeographicCRS.WGS84;
			mRadiusInMeters =  GDC.distance(new double[]{cUpperCorner.x , cUpperCorner.y}, new double[]{cLowerCorner.x , cLowerCorner.y}).doubleValue();
		}
		
		//Set SRS
		m_envType.setSrsName(sSRS);
	}

     /**
     * Constructor for create EnvelopeType
     * 
     * @param env Envelope from which the EvnelopeType is created
     */
	public Envelope(String responseSRS, com.vividsolutions.jts.geom.Envelope env)throws ServiceError{
		//Set number of points
		this.iIndexOfPoints = 4;

		//For Nice Output!?!
		DecimalFormat df = new DecimalFormat( "0.0000000" );

		//Create EnvelopeType
		m_envType = EnvelopeType.Factory.newInstance();
		//Set SRS
		m_envType.setSrsName(responseSRS);
		
		if(RouteService.GRAPH_SRS.equals(responseSRS)){
			DirectPositionType positionLowerCorner = m_envType.addNewPos();
			positionLowerCorner.setStringValue(df.format(env.getMinX()).replace(",",".")+" "+df.format(env.getMinY()).replace(",","."));
			DirectPositionType positionUpperCorner = m_envType.addNewPos();
			positionUpperCorner.setStringValue(df.format(env.getMaxX()).replace(",",".")+" "+df.format(env.getMaxY()).replace(",","."));
		}
		else{
			DirectPositionType positionLowerCorner = m_envType.addNewPos();
			positionLowerCorner.setStringValue(CoordTransform.transformGetString(RouteService.GRAPH_SRS, responseSRS, new Coordinate(env.getMinX(),env.getMinY())));
			DirectPositionType positionUpperCorner = m_envType.addNewPos();
			positionUpperCorner.setStringValue(CoordTransform.transformGetString(RouteService.GRAPH_SRS, responseSRS, new Coordinate(env.getMaxX(),env.getMaxY())));
		}

		//Set Min & Max values
		this.dMinX = env.getMinX();
		this.dMinY = env.getMinY();
		this.dMaxX = env.getMaxX();
		this.dMaxY = env.getMaxY();
		
		//Set Upper/Lower Corner
		this.cUpperCorner = new Coordinate(dMaxX, dMaxY);
		this.cLowerCorner = new Coordinate(dMinX, dMinY);
		
		//Set Radius
		if(!responseSRS.equals("EPSG:4326")){
			double dRadiusX = (cUpperCorner.x - cLowerCorner.x)/2;
			double dRadiusY = (cUpperCorner.y - cLowerCorner.y)/2;
			
			if(dRadiusX > dRadiusY)
				mRadiusInMeters = dRadiusX;
			else
				mRadiusInMeters = dRadiusY;
		}
		else{
			DefaultGeographicCRS GDC = DefaultGeographicCRS.WGS84;
			mRadiusInMeters =  GDC.distance(new double[]{cUpperCorner.x , cUpperCorner.y}, new double[]{cLowerCorner.x , cLowerCorner.y}).doubleValue();
		}
	}

     /**
     * Method to get the UpperRight Corner
     *
     * @return Coordinate of the UpperRight Corner
     */
	public Coordinate getUpperCorner(){
		return this.cUpperCorner;
	}

     /**
     * Method to get the UpperRight Corner X value
     *
     * @return double X value of the UpperRight Corner
     */
	public double getUpperCornerX(){
		return this.cUpperCorner.x;
	}

     /**
     * Method to get the UpperRight Corner Y value
     *
     * @return double Y value of the UpperRight Corner
     */
	public double getUpperCornerY(){
		return this.cUpperCorner.y;
	}

     /**
     * Method to get the LowerLeft Corner
     *
     * @return Coordinate of the LowerLeft Corner
     */
	public Coordinate getLowerCorner(){
		return this.cLowerCorner;
	}

     /**
     * Method to get the LowerLeft Corner X value
     *
     * @return double X value of the LowerLeft Corner
     */
	public double getLowerCornerX(){
		return this.cLowerCorner.x;
	}

     /**
     * Method to get the LowerLeft Corner Y value
     *
     * @return double Y value of the LowerLeft Corner
     */
	public double getLowerCornerY(){
		return this.cLowerCorner.y;
	}

     /**
     * Method to get the Number of Points from which the EnvelopeType is created
     *
     * @return int Number of Points
     */
	public int getNumOfPoints(){
		return this.iIndexOfPoints;
	}

     /**
     * Method to calculate the CenterPoint of the EnvelopeType
     *
     * @return Coordinate of the CenterPoint
     */
	public Coordinate calculateCenterPoint(){
		Coordinate cCenterPoint = new Coordinate();
		
		cCenterPoint.x = (this.dMinX + this.dMaxX) / 2;
		cCenterPoint.y = (this.dMinY + this.dMaxY) / 2;
		
		return cCenterPoint;
	}

     /**
     * Method to get the EnvelopeType
     *
     * @return EnvelopeType the created EnvelopeType
     */
	public EnvelopeType getEnvelopeType(){
		return m_envType;
	}

	/**
	 * @return the RadiusInMeters
	 */
	public double getRadiusInMeters() {
		return mRadiusInMeters;
	}
}
