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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.freeopenls.database.PGConnection;
import org.freeopenls.error.ServiceError;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.SeverityType;

/**
 * <p><b>Title: SearchFreeFormAddress</b></p>
 * <p><b>Description:</b> Class for search an address in a PostGIS DB</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-05-08
 */
public class SearchStructAddress {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(SearchStructAddress.class.getName());
	private final Logger mLoggerCounter = Logger.getLogger("org.freeopenls.location.search.Counter");

	/** PostGIS Connection for Address search **/
	private PGConnection mConnection;
	
	/**
	 * Constructor
	 */
	public SearchStructAddress(PGConnection connection){
		mConnection = connection;
	}

	/**
	 * Method that search with the delivered values an address / coordinate.
	 * 
	 */
	public ArrayList<ResultAddress> search(String countryCode, String countrySubdivision, String postalCode, String municipality, 
			String municipalitySubdivision, String streetName, String houseNr, String subdivision)throws ServiceError{

		ArrayList<ResultAddress> mResults = new ArrayList<ResultAddress>();
		
		//PostGIS Result
		Statement statement = null;
		ResultSet resultset = null;
		
	    mLoggerCounter.info("Geocode;;;Search StructAddress;"+countryCode+" "+countrySubdivision+" "+postalCode+" "+municipality+
	    		" "+municipalitySubdivision+" "+streetName+" "+houseNr+" "+subdivision);
		
		try {
			if (countryCode != null && !countryCode.equals("")) countryCode="'"+countryCode.replaceAll("'", "")+"'";
			else countryCode = null;
			if (countrySubdivision!= null && countrySubdivision.equals("")) countrySubdivision="'"+countrySubdivision.replaceAll("'", "")+"'";
			else countrySubdivision = null;
			if (postalCode != null && !postalCode.equals("")) postalCode="'"+postalCode.replaceAll("'", "")+"'";
			else postalCode = null;
			if (municipality !=null && !municipality.equals("")) municipality="'"+municipality.replaceAll("'", "")+"'";
			else municipality = null;
			if (municipalitySubdivision != null && !municipalitySubdivision.equals("")) municipalitySubdivision="'"+municipalitySubdivision.replaceAll("'", "")+"'";
			else municipalitySubdivision = null;
			if (streetName != null && !streetName.equals("")) streetName="'"+streetName.replaceAll("'", "")+"'";
			else streetName = null;
			if (houseNr != null && !houseNr.equals(0)) houseNr="'"+houseNr.replaceAll("'", "")+"'";
			else houseNr = null;
			if (subdivision != null && !subdivision.equals("")) subdivision="'"+subdivision.replaceAll("'", "")+"'";
			else subdivision = null;
			
			if(municipality == null && postalCode == null && streetName == null){
				mLoggerCounter.info("Geocode;;;Search Results StructAddress; municipality == null && postalCode == null && streetName == null");
				return mResults;
			}

			String query = "SELECT * FROM (SELECT DISTINCT ON (municipal,strname,postalcode) * from"+
					" struct_search("+countryCode+","+countrySubdivision+","+
					postalCode+","+municipality+","+municipalitySubdivision+","+
					streetName+","+houseNr+","+subdivision+") ) as x order by geocode_quality desc;";
			//System.out.println("Query: "+query);

			// DB Query
//			resultset = mConnection.getStatement().executeQuery(query);
			statement = mConnection.getConnection().createStatement();
			resultset = statement.executeQuery(query);

			// Add founded Addresses
			while (resultset.next()) {
	    		String countrycode = null;
	    		if(resultset.getString("countrycode") != null && !resultset.getString("countrycode").equals("")) countrycode = resultset.getString("countrycode");
	    		String countrys = null;
	    		if(resultset.getString("countrys") != null && !resultset.getString("countrys").equals("")) countrys = resultset.getString("countrys");
	    		String postalcode = null;
	    		if(resultset.getString("postalcode") != null && !resultset.getString("postalcode").equals("")) postalcode = resultset.getString("postalcode");
	    		String municipal = null;
	    		if(resultset.getString("municipal") != null && !resultset.getString("municipal").equals("")) municipal = resultset.getString("municipal");	    		
	    		String municipals = null;
	    		if(resultset.getString("municipals") != null && !resultset.getString("municipals").equals("")) municipals = resultset.getString("municipals");	
	    		String strname = null;
	    		if(resultset.getString("strname") != null && !resultset.getString("strname").equals("")) strname = resultset.getString("strname");
	    		String housenr = null;
	    		if(resultset.getString("housenr") != null && !resultset.getString("housenr").equals("")) housenr = resultset.getString("housenr");
	    		
	    		double geocode_quality = 0.11;
	    		if(resultset.getString("geocode_quality") != null)
	    			if(!resultset.getString("geocode_quality").equals(""))
	    				geocode_quality = Double.parseDouble(resultset.getString("geocode_quality"));
	    			
	    		mResults.add(new ResultAddress(countrycode, countrys, postalcode, municipal, municipals,
	    				strname, housenr, null, geocode_quality, 
	    						getCoordinate(resultset.getString("point"))));
			}
			
			mLoggerCounter.info("Geocode;;;Search Results StructAddress;"+mResults.size());
			
		} catch (Exception ex) {
			mLogger.error("*** Error for Search: \"struct_search("+countryCode+","+countrySubdivision+","+
					postalCode+","+municipality+","+municipalitySubdivision+","+
					streetName+","+houseNr+","+subdivision+")+\" \n"+ex );
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "AddressSearchDB", ex);
			throw se;
		} finally {
			// Close statement & resultset
			try { if( null != statement ) statement.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
			try { if( null != resultset ) resultset.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
		}
		
		return mResults;
	}

	private Coordinate getCoordinate(String resultset){
		Point pTMP = null;
		try{
			WKTReader reader = new WKTReader(new GeometryFactory());
			Geometry geom = reader.read(resultset);

			if (geom instanceof MultiPoint)
				pTMP = (Point) ((MultiPoint) geom).getGeometryN(0);
			else if (geom instanceof Point)
				pTMP = (Point) geom;
			else if(geom instanceof MultiLineString){
				LineString lineString = (LineString) ((MultiLineString) geom).getGeometryN(0);
				pTMP = lineString.getPointN(lineString.getNumPoints()/2);
			}
			else if(geom instanceof LineString){
				LineString lineString = (LineString) geom;
				pTMP = lineString.getPointN(lineString.getNumPoints()/2);
			}
			else if (geom instanceof MultiPolygon){
				Polygon poly = (Polygon) ((MultiPolygon) geom).getGeometryN(0);
				pTMP = poly.getCentroid();
			}
			else if (geom instanceof Polygon){			
				Polygon poly = (Polygon) geom;
				pTMP = poly.getCentroid();
			}
		}catch(ParseException pe){
			mLogger.error(pe);
		}
		return pTMP.getCoordinate();
	}
}