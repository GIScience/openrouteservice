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

import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.PointOfInterestType;

import org.apache.log4j.Logger;
import org.freeopenls.constants.DirectoryService;
import org.freeopenls.database.ConnectionParameter;
import org.freeopenls.database.PGConnection;
import org.freeopenls.error.ServiceError;
import org.freeopenls.location.Location;
import org.freeopenls.location.WayPoint;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * <b>Title: SearchPosition</b>
 * </p>
 * <p>
 * <b>Description:</b> Class for searching a position in a PostGIS DB to get the
 * nearest POI to a given location
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2009
 * </p>
 * <p>
 * <b>Institution:</b> University of Bonn, Department of Geography
 * </p>
 * 
 * @author Johannes Lauer, jlauer@geographie.uni-bonn.de
 * 
 * @version 1.0 2009-03-03
 */

public class SearchPOI_Nearest {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger
			.getLogger(SearchPOI_Nearest.class.getName());
	private static final Logger mLoggerCounter = Logger
			.getLogger(SearchPOI_Nearest.class.getName() + ".Counter");

	/** ArrayList with found locations */
	// private ArrayList<POIWithDistanceType> mFoundLocations = new
	// ArrayList<POIWithDistanceType>();
	// private ArrayList<NearestType> mFoundLocation = new
	// ArrayList<NearestType>();
	private PointOfInterestType mFoundLocation;

	/** PostGIS Connection for POI search **/
	private ConnectionParameter mConnectionParameter;
	private PGConnection mConnection;

	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchPOI_Nearest(ConnectionParameter connectionParameter,
			PGConnection connection) {
		mConnectionParameter = connectionParameter;
		mConnection = connection;
	}


	/**
	 * 
	 * Method that search with the delivered values the nearest POI to a
	 * location
	 * 
	 * @param sDatabeseSRS
	 * @param sResponseSRS
	 * @param cLocation the current location
	 * @param maximumResponses maximum number of responses
	 * @param poiPropertyName property name of the POI to search for
	 * @param poiPropertyValue property value of the POI to search for
	 * @param methodName name of the search method (e.g. WithinDistance, Nearest)
	 * @return POI list
	 * @throws ServiceError
	 */
	public POIWithDistanceType[] findNearestPOI(String sDatabeseSRS,
			String sResponseSRS, Coordinate cLocation, int maximumResponses,
			String poiPropertyName, String poiPropertyValue, String methodName)
			throws ServiceError {

		POIWithDistanceType[] pois = null;
		

		// TODO
		// Schleife zur Abfrage von POIS (ueber SearchPOI_Distance)
		// Radius immer ausweiten 10 - 100 - 1000 km
		// falls eine Liste zurueckkommt: ersten Wert nehmen und ausgeben
		SearchPOI_Distance spd = new SearchPOI_Distance(mConnectionParameter, mConnection);
		
		for (int i = 1; i <= 1000000; i = i * 10) {

			pois = spd.SearchInDB(DirectoryService.DATABASE_SRS, sResponseSRS,
					cLocation, 0, i, maximumResponses, poiPropertyName,
					poiPropertyValue, "Nearest");
			
			mLoggerCounter.error("POIS Length: "+pois.length);
			if (pois.length > 0) {
//				poi = pois[0];
				return pois;
			}
//			if (i == 1000) {
//				System.out.println("nothing found"); 
//				//TODO output when no POI was found
//			}
		}
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
