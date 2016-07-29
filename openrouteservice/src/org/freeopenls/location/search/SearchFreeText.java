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
 * <p><b>Description:</b> Class for search an address "FreeForm" in a PostGIS DB</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-05-08
 */
public class SearchFreeText {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(SearchFreeText.class.getName());
	private final Logger mLoggerCounter = Logger.getLogger("org.freeopenls.location.search.Counter");
	
	/** Results of founded Address **/
	private ArrayList<ResultAddress> mResults = new ArrayList<ResultAddress>();
	
	/** PostGIS Connection for Address search **/
	private PGConnection mConnection;
	/** PostGIS Result */
	private Statement statement = null;
	private ResultSet resultset = null;
	
	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchFreeText(PGConnection connection){
		mConnection = connection;
	}

	/**
	 * Method that search with the delivered values an address.<br>
	 */
	public ArrayList<ResultAddress> search(String freeform, int maximumResponses)throws ServiceError{

	    mLoggerCounter.info("Geocode;;;Search FreeText;"+freeform);

	    //String[] searchWordsTMP = null;

	    if(freeform.length() <=1)
	    	return mResults;
	    else{
	    	freeform = freeform.replaceAll("\'", "");
	    	freeform = freeform.replaceAll("\\\\", "");
	    	freeform = freeform.replaceAll("/", "");

	    	freeform = freeform.replace("str.", "straße");
	    	freeform = freeform.replace("Str.", "Straße");
	    	
	    	freeform = freeform.trim();
	    }

		try{
			
	    	//Pattern pattern = Pattern.compile( "[.,\' ]" );
	    	//saarchWordsTMP = pattern.split(freeform);
	    	
	    	String query = null;
	    	//if(searchWordsTMP.length == 1)
	    		query = "select * from freetext_search('"+freeform+"') AS x order by geocode_quality desc LIMIT "+maximumResponses;
//	    	else
//	    		query = "select * from ( select distinct on (municipal,strname,postalcode) * from freetext_search('"+freeform+"') ) "
//	    			+ "AS x order by geocode_quality desc LIMIT "+maximumResponses;

	    	//System.out.println(query);
//	    	mResultSet = mConnection.getStatement().executeQuery( query );
	    		statement = mConnection.getConnection().createStatement();
	    		resultset = statement.executeQuery(query);
	    		
	    	while(resultset.next()){

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
	    				strname, housenr, null, geocode_quality, getCoordinate(resultset.getString("point"))));
	    	}
	    	
			mLoggerCounter.info("Geocode;;;Search Results FreeText;"+mResults.size());
 
		} catch( Exception ex ) {
			mLogger.error("*** Error for Search: \"freetext_search('"+freeform+"')\" \n"+ex );
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "Search FreeText", ex);
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