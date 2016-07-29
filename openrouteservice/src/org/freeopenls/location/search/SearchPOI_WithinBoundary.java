/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geopgraphy                             *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|     (C) 2009                                             *
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
import org.freeopenls.directoryservice.osmparser.DSOSMKeyValueParser;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * <p>
 * <b>Title: TODO Enter class name</b>
 * </p>
 * <p>
 * <b>Description:</b> TODO Enter a description for this class
 * </p>
 * 
 * @author "Johannes Lauer" created 25.03.2009 / 16:47:04
 * @version $Revision: 237 $ $LastChangedDate: 2013-07-30 14:41:38 +0200 (Di, 30
 *          Jul 2013) $
 * @author last edited by: $Author: esteiger $
 * 
 */
public class SearchPOI_WithinBoundary {
	/** Logger, used to log errors(exceptions) and additionally information */
	private static final Logger mLogger = Logger.getLogger(SearchPOI_WithinBoundary.class.getName());
	private static final Logger mLoggerCounter = Logger
			.getLogger(SearchPOI_WithinBoundary.class.getName() + ".Counter");

	/** PostGIS Connection for POI search **/
	private ConnectionParameter mConnectionParameter;
	private PGConnection mConnection;
	private WKTReader mWktReader;

	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchPOI_WithinBoundary(ConnectionParameter connectionParameter, PGConnection connection) {
		mConnectionParameter = connectionParameter;
		mConnection = connection;
		mWktReader = new WKTReader(new GeometryFactory());
	}

	/**
	 * Method that search POIs within a given polygon
	 * 
	 * @param sDatabaseSRS
	 * @param sResponseSRS
	 * @param cLocation
	 *            the current location
	 * @param boundary
	 *            boundary polygon where to search for POIs
	 * @param maximumResponses
	 *            maximum number of responses
	 * @param poiPropertyName
	 *            property name of the POI to search for
	 * @param poiPropertyValue
	 *            property value of the POI to search for
	 * @param methodName
	 * @return POI list
	 * @throws ServiceError
	 */
	public POIWithDistanceType[] SearchInDB(String sDatabaseSRS, String sResponseSRS, Polygon boundary,
			int maximumResponses, String poiPropertyName, String poiPropertyValue, String methodName)
			throws ServiceError {

		// Result Array
		POIWithDistanceType[] pois = null;

		ArrayList<POIWithDistanceType> foundLocations = new ArrayList<POIWithDistanceType>();

		Statement statement = null;
		ResultSet resultset = null;

		// convert the jts polygon to wkt
		String searchPolygon = boundary.toText();

		int srid = 0;
		if (boundary.getSRID() > 0) {
			srid = boundary.getSRID();
		} else {
			// set SRID default to EPSG:4326
			srid = 4326;
		}

		try {

			String query = "";

			if (poiPropertyName.equalsIgnoreCase("OSM_KEYS_VALUES")) {
				DSOSMKeyValueParser dsosm = new DSOSMKeyValueParser(poiPropertyValue, ";");

				query = "Select DISTINCT *, ST_AsText(the_geom) as geom" + " FROM "
						+ mConnectionParameter.getTableName() + " WHERE ST_Contains(ST_GeometryFromText('"
						+ searchPolygon + "', " + srid + "), the_geom) " + " AND " + dsosm.getSQLString() + " LIMIT "
						+ maximumResponses;
			} else {
				query = "Select DISTINCT *, ST_AsText(the_geom) as geom" + " FROM "
						+ mConnectionParameter.getTableName() + " WHERE ST_Contains(ST_GeometryFromText('"
						+ searchPolygon + "', " + srid + "), the_geom) " + " AND " + poiPropertyName + "='"
						+ poiPropertyValue + "' LIMIT " + maximumResponses;
			}

			// TODO
			// Select *, ST_AsText(the_geom),
			// ST_Distance(the_geom,ST_GeomFromText( 'POINT(8.751945875063665
			// 49.451468274299245)', 4326))
			// as dist FROM addressbook WHERE ST_DWithin(the_geom,
			// ST_GeomFromText( 'POINT(8.751945875063665 49.451468274299245)',
			// 4326),0.001 )
			// AND strname is not null order by dist limit 1;

			// DB Query
			// resultset = mConnection.getStatement().executeQuery(query);
			statement = mConnection.getConnection().createStatement();
			resultset = statement.executeQuery(query);
			// Add founded POI
			while (resultset.next()) {
				POIWithDistanceType loc = SearchUtility.addLocation(resultset, sDatabaseSRS, sResponseSRS, null, true,
						mWktReader);
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
			try {
				if (null != statement)
					statement.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				mLogger.error(ex);
			}
			try {
				if (null != resultset)
					resultset.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				mLogger.error(ex);
			}
		}

		// Copy ArrayList Values To Array of POIWithDistanceType
  	    pois = foundLocations.toArray(new POIWithDistanceType[foundLocations.size()]);
		
		/*
		 * //*** SORT the Result by distance *** Wird nicht mehr benoetigt, da
		 * DB eine sortierte Ausgabe liefert POIWithDistanceType poiI = null;
		 * POIWithDistanceType poiJ = null; boolean boolchangePOI = false;
		 * 
		 * for(int i=0 ; i<pois.length ; i++){ poiI = pois[i]; DistanceType
		 * distI = poiI.getDistance(); double dDistanceI =
		 * distI.getValue().doubleValue();
		 * if(distI.getUom().equals(DistanceUnitType.KM)){ dDistanceI =
		 * dDistanceI * 1000; } boolchangePOI = false;
		 * 
		 * for(int j=i ; j<pois.length ; j++){ poiJ = pois[j]; DistanceType
		 * distJ = poiJ.getDistance(); double dDistanceJ =
		 * distJ.getValue().doubleValue();
		 * if(distJ.getUom().equals(DistanceUnitType.KM)){ dDistanceJ =
		 * dDistanceJ * 1000; }
		 * 
		 * if(dDistanceI > dDistanceJ){ POIWithDistanceType poiTMP = pois[i];
		 * pois[i] = pois[j]; pois[j] = poiTMP; boolchangePOI = true; } }
		 * 
		 * if(boolchangePOI) i=-1; }
		 */

		mLoggerCounter.info(" DirectoryRequest ; ; ; ; " + methodName + " ; " + boundary.toText() + " ; "
				+ poiPropertyName + " ; " + poiPropertyValue + " ; Results: " + (pois == null ? 0 : pois.length));

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
