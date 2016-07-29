/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.location.search;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.freeopenls.database.ConnectionParameter;
import org.freeopenls.database.PGConnection;
import org.freeopenls.directoryservice.osmparser.DSOSMKeyValueParser;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;



import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.SeverityType;

/**
 * <p><b>Title: SearchPosition</b></p>
 * <p><b>Description:</b> Class for search a Position in a PostGIS DB to get an POI</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-05-08
 */
public class SearchPOI_Distance {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(SearchPOI_Distance.class.getName());
	private static final Logger mLoggerCounter = Logger.getLogger(SearchPOI_Distance.class.getName()+".Counter");
	
	/** PostGIS Connection for POI search **/
	private ConnectionParameter mConnectionParameter;
	private PGConnection mConnection;
	private WKTReader mWktReader;
	
	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchPOI_Distance(ConnectionParameter connectionParameter, PGConnection connection){
		mConnectionParameter = connectionParameter;
		mConnection = connection;
		mWktReader =  new WKTReader(new GeometryFactory()); 
	}

	/**
	 * Method that search with the delivered values a POI
	 * 
	 * @param sDatabaseSRS
	 * @param sResponseSRS
	 * @param cLocation the current location
	 * @param minDistanceInMeter minumum search distance
	 * @param maxDistanceInMeter maximum search distance
	 * @param maximumResponses maximum number of responses
	 * @param poiPropertyName property name of the POI to search for 
	 * @param poiPropertyValue property value of the POI to search for
	 * @param methodName
	 * @return POI list
	 * @throws ServiceError
	 */
	public POIWithDistanceType[] SearchInDB(String sDatabaseSRS, String sResponseSRS, 
			Coordinate cLocation, double minDistanceInMeter, double maxDistanceInMeter, int maximumResponses, 
			String poiPropertyName, String poiPropertyValue, String methodName)throws ServiceError{
		
		//Result Array
		POIWithDistanceType[] pois = null;
		ArrayList<POIWithDistanceType> foundLocations = new ArrayList<POIWithDistanceType>();

		Statement statement = null;
		ResultSet resultset = null;
		
		try {
			double maxAngle = getAngle(maxDistanceInMeter);
			double minAngle = getAngle(minDistanceInMeter);
			String query = "";
			
			if(poiPropertyName.equalsIgnoreCase("OSM_KEYS_VALUES")){
				
				System.out.println("poiPropertyValue = "+poiPropertyValue);
				DSOSMKeyValueParser dsosm = new DSOSMKeyValueParser(poiPropertyValue, ";");
				
				if (minAngle == 0.0){
				query = "Select DISTINCT *, ST_AsText(the_geom) as geom, ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) as dist" +
				" FROM "+mConnectionParameter.getTableName()+
				" WHERE the_geom && ST_SetSRID('BOX3D("+ (cLocation.x - maxAngle) +" "+ (cLocation.y - maxAngle) +","+
				(cLocation.x + maxAngle)+" "+(cLocation.y + maxAngle)+")'::box3d,4326) AND "+
				"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+maxAngle+"" +
				" AND "+dsosm.getSQLString()+" order by dist LIMIT "+maximumResponses;
				
				System.out.println(query);
				
				}else{
					query = "Select DISTINCT *, ST_AsText(the_geom) as geom, ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) as dist" +
					" FROM "+mConnectionParameter.getTableName()+
					" WHERE the_geom && ST_SetSRID('BOX3D("+ (cLocation.x - maxAngle) +" "+ (cLocation.y - maxAngle) +","+
					(cLocation.x + maxAngle)+" "+(cLocation.y + maxAngle)+")'::box3d,4326) AND "+
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+maxAngle+" AND " +
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) > "+minAngle+"" +
					" AND "+dsosm.getSQLString()+" order by dist LIMIT "+maximumResponses;
				
					System.out.println(query);
//					System.out.println(minDistanceInMeter);
				}
				
			}else{
				if (minAngle == 0.0){
					query = "Select DISTINCT *, ST_AsText(the_geom) as geom, ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) as dist" +
					" FROM "+mConnectionParameter.getTableName()+
					" WHERE the_geom && ST_SetSRID('BOX3D("+ (cLocation.x - maxAngle) +" "+ (cLocation.y - maxAngle) +","+
					(cLocation.x + maxAngle)+" "+(cLocation.y + maxAngle)+")'::box3d,4326) AND "+
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+maxAngle+"" +
					" AND "+poiPropertyName+"='"+poiPropertyValue+"' order by dist LIMIT "+maximumResponses;
				
					System.out.println(query);
				}else {
					query = "Select DISTINCT *, ST_AsText(the_geom) as geom, ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) as dist" +
					" FROM "+mConnectionParameter.getTableName()+
					" WHERE the_geom && ST_SetSRID('BOX3D("+ (cLocation.x - maxAngle) +" "+ (cLocation.y - maxAngle) +","+
					(cLocation.x + maxAngle)+" "+(cLocation.y + maxAngle)+")'::box3d,4326) AND "+
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+maxAngle+" AND " +
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) > "+minAngle+"" +
					" AND "+poiPropertyName+"='"+poiPropertyValue+"' order by dist LIMIT "+maximumResponses;
				
					System.out.println(query);
//					System.out.println(minDistanceInMeter);
				}				
			}

			
			
			//TODO
			// Select *, ST_AsText(the_geom), 
			// ST_Distance(the_geom,ST_GeomFromText( 'POINT(8.751945875063665 49.451468274299245)', 4326)) 
			// as dist FROM addressbook WHERE ST_DWithin(the_geom, 
			// ST_GeomFromText( 'POINT(8.751945875063665 49.451468274299245)', 4326),0.001 ) 
			// AND strname is not null order by dist limit 1;

			// DB Query
//			resultset = mConnection.getStatement().executeQuery(query);
			statement = mConnection.getConnection().createStatement();
			resultset = statement.executeQuery(query);
			// Add founded POI
			while (resultset.next())
			{
				POIWithDistanceType loc = SearchUtility.addLocation(resultset, sDatabaseSRS, sResponseSRS, cLocation, true, mWktReader);
				if (loc != null)
				  foundLocations.add(loc);
			}

		} catch (Exception ex) {
			mLogger.info(ex);
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "SearchPOI_Distance", ex);
			throw se;
		} finally {
			// Close statement & resultset
			try { if( null != statement ) statement.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
			try { if( null != resultset ) resultset.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
		}
		
		//Copy ArrayList Values To Array of POIWithDistanceType
	    pois = foundLocations.toArray(new POIWithDistanceType[foundLocations.size()]);
		
		mLoggerCounter.info("DS | "+methodName+" | "+cLocation+" | "+minDistanceInMeter+" m |" +
				maxDistanceInMeter+" m | "+poiPropertyName+" | "+poiPropertyValue+" | Results: "+(pois == null ? 0 : pois.length));

		return pois;
	}
	
		
	/**
	 * calculates the distance for a given angle on the earth surface
	 * 
	 * @param angle
	 * @return distance that equates to the given angle
	 */
	private double getDistance(double angle) {
		return 6378000 * angle / (180 / Math.PI);
	}

	/**
	 * calculates the angle for a a given distance on the earth surface
	 * 
	 * @param distance
	 * @return angle that equates to the given distance
	 */
	private double getAngle(double distance) {
		return (180 / Math.PI) * distance / 6378000;
	}
}