/*+-------------+----------------------------------------------------------*
 *|        /\   |   Rheinische Friedrichs-Wilhelms-Universit�t Bonn        *
 *|       |  |  |     Geographisches Institut                              *
 *|      _|  |_ |     Lehrstuhl f�r Kartographie                           *
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

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.DistanceType; 
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.PointOfInterestType;
import net.opengis.xls.SeverityType;

import org.apache.log4j.Logger;
import org.freeopenls.database.ConnectionParameter;
import org.freeopenls.database.PGConnection;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author "Johannes Lauer"
 * created 04.03.2009 / 18:23:34
 * @version
 */
public class SearchPOI_Name {
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
	public SearchPOI_Name(ConnectionParameter connectionParameter, PGConnection connection){
		mConnectionParameter = connectionParameter;
		mConnection = connection;
		mWktReader =  new WKTReader(new GeometryFactory()); 
	}

	/**
	 * Method that search with the delivered values a POI
	 * 
	 * @throws ServiceError
	 */
	public POIWithDistanceType[] SearchInDB(String sDatabaseSRS, String sResponseSRS, 
			int maximumResponses, String poiPropertyName, String poiPropertyValue, String methodName)throws ServiceError{
		//Result Array
		POIWithDistanceType[] pois = null;
		ArrayList<POIWithDistanceType> foundLocations = new ArrayList<POIWithDistanceType>();

		Statement statement = null;
		ResultSet resultset = null;
		
		try {
			
			String query = "Select *, ST_AsText(the_geom) as geom" +
					" FROM "+mConnectionParameter.getTableName()+
					" WHERE "+poiPropertyName+"~*'"+poiPropertyValue+"' order by "+poiPropertyName+" LIMIT "+maximumResponses;
	    	//System.out.println(query);
			
			//TODO
			// Select *, ST_AsText(the_geom), 
			// ST_Distance(the_geom,GeomFromText( 'POINT(8.751945875063665 49.451468274299245)', 4326)) 
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
				POIWithDistanceType loc = SearchUtility.addLocation(resultset, sDatabaseSRS, sResponseSRS, null, false, mWktReader);
				if (loc != null)
				  foundLocations.add(loc);
			}

		} catch (Exception ex) {
			mLogger.info(ex);
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "SearchPOI_Name", ex);
			throw se;
		} finally {
			// Close statement & resultset
			try { if( null != statement ) statement.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
			try { if( null != resultset ) resultset.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
		}
		
		//Copy ArrayList Values To Array of POIWithDistanceType
		pois = foundLocations.toArray(new POIWithDistanceType[foundLocations.size()]);

		mLoggerCounter.info(" DirectoryRequest ; ; ; ; "+methodName+"  ; ; m ; " +
				""+poiPropertyName+" ; "+poiPropertyValue+" ; Results: "+(pois == null ? 0 : pois.length));
		
		return pois;
	}
}
