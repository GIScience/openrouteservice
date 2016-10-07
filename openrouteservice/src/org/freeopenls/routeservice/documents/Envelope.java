
package org.freeopenls.routeservice.documents;

import java.text.DecimalFormat;

import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;
import org.freeopenls.tools.FormatUtility;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;


/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for FileDelete<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
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
	public Envelope(String sSRS, PointList c){
		//Set number of points
		this.iIndexOfPoints = c.getSize();
		
		//Sort, to find Min & Max values
		for (int i = 0; i < c.getSize(); i++) {
			double x = c.getLongitude(i);
			double y = c.getLatitude(i);
			
			if (this.dMinX > x)
				this.dMinX = x;
			if (this.dMaxX < x)
				this.dMaxX = x;
			if (this.dMinY > y)
				this.dMinY = y;
			if (this.dMaxY < y)
				this.dMaxY = y;
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
	
	public void setValue(EnvelopeType envType, String sSRS)
	{
		DirectPositionType positionLowerCorner = envType.addNewPos();
		positionLowerCorner.setStringValue(FormatUtility.formatValues(this.dMinX, this.dMinY));
		DirectPositionType positionUpperCorner = envType.addNewPos();
		positionUpperCorner.setStringValue(FormatUtility.formatValues(this.dMaxX, this.dMaxY));
		
		//Set SRS
		envType.setSrsName(sSRS);
	}

     /**
     * Constructor for create EnvelopeType
     * 
     * @param env Envelope from which the EvnelopeType is created
     */
	public Envelope(String responseSRS, com.vividsolutions.jts.geom.Envelope env, String envelopeSRS)throws ServiceError{
		//Set number of points
		this.iIndexOfPoints = 4;

		//Create EnvelopeType
		m_envType = EnvelopeType.Factory.newInstance();
		//Set SRS
		m_envType.setSrsName(responseSRS);
		
		if(envelopeSRS.equals(responseSRS)){
			DirectPositionType positionLowerCorner = m_envType.addNewPos();
			positionLowerCorner.setStringValue(FormatUtility.formatValues(env.getMinX(), env.getMinY()));
			DirectPositionType positionUpperCorner = m_envType.addNewPos();
			positionUpperCorner.setStringValue(FormatUtility.formatValues(env.getMaxX(), env.getMaxY()));
		}
		else{
			DirectPositionType positionLowerCorner = m_envType.addNewPos();
			Coordinate min = CoordTransform.transformGetCoord(envelopeSRS, responseSRS, new Coordinate(env.getMinX(),env.getMinY()));
			//positionLowerCorner.setStringValue(CoordTransform.transformGetString(envelopeSRS, responseSRS, new Coordinate(env.getMinX(),env.getMinY())));
			positionLowerCorner.setStringValue(min.x+" "+min.y);
			DirectPositionType positionUpperCorner = m_envType.addNewPos();
			Coordinate max = CoordTransform.transformGetCoord(envelopeSRS, responseSRS, new Coordinate(env.getMaxX(),env.getMaxY()));
			//positionUpperCorner.setStringValue(CoordTransform.transformGetString(envelopeSRS, responseSRS, new Coordinate(env.getMaxX(),env.getMaxY())));
			positionUpperCorner.setStringValue(max.x+" "+max.y);
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
