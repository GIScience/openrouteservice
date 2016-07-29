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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.freeopenls.constants.LocationUtilityService;
import org.freeopenls.database.ConnectionParameter;
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
public class SearchFreeFormAddress {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(SearchFreeFormAddress.class.getName());
	private final Logger mLoggerCounter = Logger.getLogger("org.freeopenls.location.search.Counter");
	
	/** Results of founded Address **/
	private ArrayList<ResultAddress> mResults = new ArrayList<ResultAddress>();
	
	/** PostGIS Connection for Address search **/
	private ConnectionParameter mConnectionParameter;
	private PGConnection mConnection;
	/** PostGIS Result */
	private Statement statement = null;
	private ResultSet resultset = null;
	
	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchFreeFormAddress(ConnectionParameter connectionParameter, PGConnection connection){
		mConnectionParameter = connectionParameter;
		mConnection = connection;
	}

	/**
	 * Method that search with the delivered values an address.<br>
	 */
	public ArrayList<ResultAddress> search(String freeform, int maximumResponses)throws ServiceError{

	    mLoggerCounter.info("Geocode;;;Search FreeForm;"+freeform);

	    String[] searchWordsTMP = null;

	    if(freeform.length() <=1)
	    	return mResults;
	    else{
	    	freeform = freeform.replaceAll("\'", "");
	    	freeform = freeform.replaceAll("\\\\", "");
	    	freeform = freeform.replaceAll("/", "");

	    	freeform = freeform.replace("str.", "straße");
	    	freeform = freeform.replace("Str.", "Straße");
	    	
	    	freeform = freeform.trim();
	    	
	    	Pattern pattern = Pattern.compile( "[.,\' ]" );
	    	searchWordsTMP = pattern.split(freeform);
	    	
	    	if(searchWordsTMP.length == 1)
	    		freeform = searchWordsTMP[0];
	    }
	    
	    ArrayList<String> words = new ArrayList<String>();
	    for(int i=0 ; i<searchWordsTMP.length ; i++){
		    if(searchWordsTMP[i].matches("\\d+") && searchWordsTMP[i].length()<8){ //Important: if length > 9 -> NumberFormatException
		    	int number = Integer.parseInt(searchWordsTMP[i]);
		    	if(number > 9999 && number < 100000)
		    		words.add(searchWordsTMP[i]);
		    }
		    else if(searchWordsTMP[i].matches("\\d+\\p{Punct}*\\p{Lower}*\\p{Upper}*")){
		    	//System.out.println("NOPE: xX"+searchWordsTMP[i]+"Xx");
		    }
		    else
		    	words.add(searchWordsTMP[i]);
		}

	    String[] searchWords = new String[words.size()];
	    for(int i=0 ; i<words.size() ; i++){
	    	searchWords[i] = words.get(i);
	    	//System.out.println("xX"+words.get(i)+"Xx");
	    }
	    //System.out.println("Free: xX"+freeform+"Xx");
	    
	    
	    try {
	    	//* If free-form == CountrySubdivision or PostalCode or Municipality or ...
	    	//PostalCode
	    	if(getResult("postalcode", freeform, "strname", maximumResponses))
	    		return mResults;
		    //Municipality
	    	if(getResult("municipal", freeform, "strname", maximumResponses))
	    		return mResults;
	    	//StreetName
	    	if(getResult("strname", freeform, null, maximumResponses))
	    		return mResults;
		    //CountrySubdivision
	    	if(getResult("countrys", freeform, "municipal", maximumResponses))
	    		return mResults;
	    	
	    	if(searchWords.length == 2)
		    	if(getResult(freeform, searchWords, maximumResponses))
		    		return mResults;
	        
	    	//* If results empty and free-form == more words, e.g. Municipality and StreetName or ...
	    	boolean foundSomething = false;
	    	boolean moreThenOne = false;
	    	String query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE ";
		    for(int i=0 ; i<searchWords.length ; i++){
		    	
		    	if(!searchWords[i].equals(" ")){
			    	String row = "";
				   	for(int j=0 ; j<LocationUtilityService.AddressBookRowNames.values().length ; j++){
				   		String selectQuery = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE ";
				   		String rowQuery = "(LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+searchWords[i]+"')";
				   		//System.out.println(j+" "+LocationUtilityService.AddressBookRowNames.values()[j].name());
				   		if(searchWords[i].contains("trasse") || searchWords[i].contains("traße"))
				   			rowQuery = "(LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+searchWords[i].replace("traße", "trasse")+"')" +
				   					" OR LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+searchWords[i].replace("trasse", "traße")+"')";
				   		
					   	for(int k=i+1 ; k<searchWords.length ; k++){
					   		if(!searchWords[i].equals(" ")){
					   			String temp = "";
						   		for(int l=i ; l<=k ; l++){
						   			if(l==i)
						   				temp+=searchWords[l];
						   			else
						   				temp+=" "+searchWords[l];
						   		}

						   		if(temp.contains("trasse") || temp.contains("traße"))
						   			rowQuery+=" OR LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+temp.replace("traße", "trasse")+"')" +
						   					" OR LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+temp.replace("trasse", "traße")+"')";
						   		else
						   			rowQuery+=" OR LOWER("+LocationUtilityService.AddressBookRowNames.values()[j].name()+")= LOWER('"+temp+"')";
						   		
						   		String limitQuery = ") LIMIT 1;";
						   		//System.out.println(selectQuery+rowQuery+limitQuery);
//						   		mResultSet = mConnection.getStatement().executeQuery( selectQuery+rowQuery+limitQuery );
								statement = mConnection.getConnection().createStatement();
								resultset = statement.executeQuery(query);
								
						   		if(resultset.next()){
						   			if(moreThenOne) row+=") AND "+rowQuery;
						   			else {row+=rowQuery;moreThenOne=true;}
						   			i++;
						   			foundSomething = true;
						   			break;
						   		}
					   		}
					   	}
					   	
					   	if((i+1)==searchWords.length && j==LocationUtilityService.AddressBookRowNames.values().length-1){
					   		String limitQuery = ") LIMIT 1;";
					   		//System.out.println(selectQuery+rowQuery+limitQuery);
//					   		mResultSet = mConnection.getStatement().executeQuery( selectQuery+rowQuery+limitQuery );
							statement = mConnection.getConnection().createStatement();
							resultset = statement.executeQuery(query);
					   		if(resultset.next()){
					   			if(moreThenOne) row+=") OR "+rowQuery;
					   			else {row+=rowQuery;moreThenOne=true;}
					   			foundSomething = true;
					   		}
					   	}
					   	
				   	}
				   	query+=row;
		    	}
			}
		    query+= ") LIMIT "+maximumResponses;

		    if(foundSomething){
			    //System.out.println("QUERY: "+query);
//		   		mResultSet = mConnection.getStatement().executeQuery( query );
				statement = mConnection.getConnection().createStatement();
				resultset = statement.executeQuery(query);
		   		while(resultset.next()){
		   			mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
		   					resultset.getString("postalcode"), resultset.getString("municipal"), resultset.getString("municipals"),
							resultset.getString("strname"), resultset.getString("housenr"), null, 1.0, getCoordinate(resultset.getString("point"))));
		   		}
		   		mLoggerCounter.info("Geocode;;;Search Results;"+mResults.size());
		   		return mResults;
		    }
		    else{
	    		//No Results?!
		    	mLoggerCounter.info("Geocode;;;Search Results;"+mResults.size());
	    		return mResults;
		    }

    	} catch( Exception ex ) {
			 ServiceError se = new ServiceError(SeverityType.ERROR);
			 mLogger.error(ex);
			 se.addInternalError(ErrorCodeType.UNKNOWN, "Search Free From Address", ex);
			 throw se;
		} finally {
			// Close statement & resultset
			try { if( null != statement ) statement.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
			try { if( null != resultset ) resultset.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
		}
	}
	
	private boolean getResult(String rowName, String searchWord, String rowNameNull, int maximumResponses)throws SQLException{
		String query = "SELECT * FROM "+mConnectionParameter.getTableName()+" WHERE " +
			"LOWER("+rowName+")= LOWER('"+searchWord+"')";
		
		if(rowName.equals("municipal"))
			query = "SELECT * FROM "+mConnectionParameter.getTableName()+" WHERE " +
				"(LOWER(municipal)= LOWER('"+searchWord+"') OR municipal ILIKE '"+searchWord+" %')";
		
		if(rowName.equals("strname"))
			query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE " +
				"LOWER(strname)= LOWER('"+searchWord+"')";
		
		if(rowName.equals("strname") && (searchWord.contains("trasse") || searchWord.contains("traße")))
			query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE " +
				"(LOWER(strname)= LOWER('"+searchWord.replace("traße", "trasse")+"')" +
						" OR LOWER(strname)= LOWER('"+searchWord.replace("trasse", "traße")+"'))";
		
		String addon="";
		if(rowName.equals("municipal"))
			addon = "ORDER BY type ASC";
		
		if(rowNameNull != null)
			query = query + " AND "+rowNameNull+" is null "+addon+" LIMIT "+maximumResponses;
		else
			query = query + " "+addon+" LIMIT "+maximumResponses;

		//System.out.println(query);

//		mResultSet = mConnection.getStatement().executeQuery( query );
		statement = mConnection.getConnection().createStatement();
		resultset = statement.executeQuery(query);

		while(resultset.next()){
			if(rowName.equals("countrys"))
				mResults.add(new ResultAddress(resultset.getString("countrycode"),resultset.getString("countrys"),
					null, null, null, null, null, null, 1.0, getCoordinate(resultset.getString("point"))));
			else if(rowName.equals("postalcode"))
				mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
						resultset.getString("postalcode"), resultset.getString("municipal"), null, null, null, null, 1.0, 
					getCoordinate(resultset.getString("point"))));
			else if(rowName.equals("municipal"))
				mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
						resultset.getString("postalcode"), resultset.getString("municipal"), null, null, null, null, 1.0, 
					getCoordinate(resultset.getString("point"))));
			else if(rowName.equals("strname"))
				mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
						resultset.getString("postalcode"), resultset.getString("municipal"), resultset.getString("municipals"),
						resultset.getString("strname"), resultset.getString("housenr"), null, 1.0, getCoordinate(resultset.getString("point"))));
		}

		if(mResults.size() > 0){
			mLoggerCounter.info("Geocode;;;Search Results;"+mResults.size());
			return true;
		}
		else
			return false;
	}
	
	private boolean getResult(String searchWord, String[] searchWords, int maximumResponses)throws SQLException{
		String query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE " +
			"LOWER(municipal)= LOWER('"+searchWord+"') " +
			"OR LOWER(strname)= LOWER('"+searchWord.replace("traße", "trasse")+"') OR LOWER(strname)= LOWER('"+searchWord.replace("trasse", "traße")+"') " +
			"OR (LOWER(municipal)= LOWER('"+searchWords[0]+"') AND LOWER(strname)= LOWER('"+searchWords[1]+"')) " +
			"OR (LOWER(municipal)= LOWER('"+searchWords[1]+"') AND LOWER(strname)= LOWER('"+searchWords[0]+"')) " +
			"OR (LOWER(postalcode)= LOWER('"+searchWords[0]+"') AND LOWER(strname)= LOWER('"+searchWords[1]+"')) " +
			"OR (LOWER(postalcode)= LOWER('"+searchWords[1]+"') AND LOWER(strname)= LOWER('"+searchWords[0]+"')) " +
			"LIMIT "+maximumResponses;
    	//System.out.println(query);
//    	mResultSet = mConnection.getStatement().executeQuery( query );
		statement = mConnection.getConnection().createStatement();
		resultset = statement.executeQuery(query);
    	while(resultset.next()){
    		mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
    				resultset.getString("postalcode"), resultset.getString("municipal"), resultset.getString("municipals"),
    				resultset.getString("strname"), resultset.getString("housenr"), null, 0.9, getCoordinate(resultset.getString("point"))));
    	}

    	if(mResults.size() == 0){
			query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE " +
				"((LOWER(postalcode)= LOWER('"+searchWords[0]+"') AND LOWER(municipal)= LOWER('"+searchWords[1]+"')) " +
				"OR (LOWER(postalcode)= LOWER('"+searchWords[1]+"') AND LOWER(municipal)= LOWER('"+searchWords[0]+"'))) " +
				"AND strname is null LIMIT "+maximumResponses;
//	    	mResultSet = mConnection.getStatement().executeQuery( query );
			statement = mConnection.getConnection().createStatement();
			resultset = statement.executeQuery(query);
	    	//System.out.println(query);
	    	while(resultset.next()){
	    		mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
	    				resultset.getString("postalcode"), resultset.getString("municipal"), resultset.getString("municipals"),
	    				null, null, null, 0.9, getCoordinate(resultset.getString("point"))));
	    	}
    	}
    	
    	if(mResults.size() == 0){
			query = "SELECT distinct on (municipal) * FROM "+mConnectionParameter.getTableName()+" WHERE " +
			"LOWER(municipal)= LOWER('"+searchWord+"') OR municipal ILIKE '"+searchWords[0]+" %' " +
			"OR LOWER(strname)= LOWER('"+searchWord.replace("traße", "trasse")+"') OR LOWER(strname)= LOWER('"+searchWord.replace("trasse", "traße")+"') " +
			"OR (LOWER(municipal)= LOWER('"+searchWords[0]+"') AND LOWER(strname)= LOWER('"+searchWords[1]+"')) " +
			"OR (LOWER(municipal)= LOWER('"+searchWords[1]+"') AND LOWER(strname)= LOWER('"+searchWords[0]+"')) " +
			"OR (LOWER(postalcode)= LOWER('"+searchWords[0]+"') AND LOWER(strname)= LOWER('"+searchWords[1]+"')) " +
			"OR (LOWER(postalcode)= LOWER('"+searchWords[1]+"') AND LOWER(strname)= LOWER('"+searchWords[0]+"')) " +
			"LIMIT "+maximumResponses;
			//System.out.println(query);
//			mResultSet = mConnection.getStatement().executeQuery( query );
			statement = mConnection.getConnection().createStatement();
			resultset = statement.executeQuery(query);
			while(resultset.next()){
				mResults.add(new ResultAddress(resultset.getString("countrycode"), resultset.getString("countrys"),
						resultset.getString("postalcode"), resultset.getString("municipal"), resultset.getString("municipals"),
						resultset.getString("strname"), null, null, 0.9, getCoordinate(resultset.getString("point"))));
			}
    	}
    	
		if(mResults.size() > 0){
			mLoggerCounter.info("Geocode;;;Search Results;"+mResults.size());
			return true;
		}
		else
			return false;
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