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
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTools;
import org.freeopenls.tools.CoordTransform;

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


import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.ErrorCodeType;
import net.opengis.xls.ReverseGeocodedLocationType;
import net.opengis.xls.SeverityType;

/**
 * <p><b>Title: SearchPosition</b></p>
 * <p><b>Description:</b> Class for search a Position in a PostGIS DB to get an address</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2008-05-08
 */
public class SearchPosition {
	/** Logger, used to log errors(exceptions) and additionally information */
	private final Logger mLogger = Logger.getLogger(SearchPosition.class.getName());
	private final Logger mLoggerCounter = Logger.getLogger("org.freeopenls.location.search.Counter");
	
	/** ArrayList with ReverseGeocodedlocations */
	private ArrayList<ReverseGeocodedLocationType> reverseGeocodedLocation = new ArrayList<ReverseGeocodedLocationType>();

	/** PostGIS Connection for Address search **/
	private ConnectionParameter mConnectionParameter;
	private PGConnection mConnection;
	
	/**
	 * Constructor - Set PostGIS Parameters
	 */
	public SearchPosition(ConnectionParameter connectionParameter, PGConnection connection){
		mConnectionParameter = connectionParameter;
		mConnection = connection;
	}

	/**
	 * Method that search with the delivered values a Location/Point/Address
	 * 
	 * @param sPG_Address_SRS
	 * @param sResponseSRS
	 * @param cLocation
	 * @param maximumResponses
	 * @return ReverseGeocodedLocationType[]
	 * @throws ServiceError
	 */
	public ReverseGeocodedLocationType[] SearchInDB(String sPG_Address_SRS, String sResponseSRS, Coordinate cLocation, int maximumResponses)throws ServiceError{
		//Result Array
		ReverseGeocodedLocationType revgeolocation[] = null;

		Statement statement = null;
		ResultSet resultset = null;
		
	    mLoggerCounter.info("ReverseGeocode;;;Search Position;"+cLocation);
		
		try {
			boolean boolFound = false;
			double distance = 0.0001;
			
			do{
				String query = "Select DISTINCT *, ST_AsText(the_geom) as geom FROM "+mConnectionParameter.getTableName()+
					" WHERE the_geom && ST_setSRID('BOX3D("+ (cLocation.x - distance) +" "+ (cLocation.y - distance) +","+
					(cLocation.x + distance)+" "+(cLocation.y + distance)+")'::box3d,4326) AND "+
					"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+distance+"" +
							" AND strname is not null LIMIT "+maximumResponses;
				
				//TODO
				//Select *, ST_AsText(the_geom), 
				// ST_Distance(the_geom,ST_GeomFromText( 'POINT(8.751945875063665 49.451468274299245)', 4326)) 
				// as dist FROM addressbook WHERE ST_DWithin(the_geom, 
				// ST_GeomFromText( 'POINT(8.751945875063665 49.451468274299245)', 4326),0.001 ) 
				// AND strname is not null order by dist limit 1;
				
				//System.out.println(query);
				// DB Query
//				resultset = mConnection.getStatement().executeQuery(query);
				statement = mConnection.getConnection().createStatement();
				resultset = statement.executeQuery(query);
				
				// Add founded Points/Addresses
				while (resultset.next())
					addLocation(resultset, sPG_Address_SRS, sResponseSRS, cLocation);
				
				if(reverseGeocodedLocation.size() > 0)
					boolFound = true;

				if(!boolFound)
					distance = distance*10;
				
			}while(!boolFound && distance<=0.01);

			if(reverseGeocodedLocation.size() == 0){
				distance = 0.05;
				do{
					String query = "Select DISTINCT *, ST_AsText(the_geom) as geom FROM "+mConnectionParameter.getTableName()+
						" WHERE the_geom && ST_setSRID('BOX3D("+ (cLocation.x - distance) +" "+ (cLocation.y - distance) +","+
						(cLocation.x + distance)+" "+(cLocation.y + distance)+")'::box3d,4326) AND "+
						"ST_Distance(the_geom, ST_GeomFromText( 'POINT("+cLocation.x+" "+cLocation.y+")', 4326)) < "+distance+"" +
							"AND municipal is not null AND strname is null LIMIT "+maximumResponses;
		    		//System.out.println(query);
					// DB Query
//					resultset = mConnection.getStatement().executeQuery(query);
					statement = mConnection.getConnection().createStatement();
					resultset = statement.executeQuery(query);
					// Add founded Points/Addresses
					while (resultset.next())
						addLocation(resultset, sPG_Address_SRS, sResponseSRS, cLocation);

					if(reverseGeocodedLocation.size() > 0)
						boolFound = true;

					if(!boolFound)
						distance = distance*2;
					
				}while(!boolFound && distance<=0.1);
			}

		} catch (Exception ex) {
			mLogger.info(ex);
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addInternalError(ErrorCodeType.UNKNOWN, "SearchPosition", ex);
			throw se;
		} finally {
			// Close statement & resultset
			try { if( null != statement ) statement.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
			try { if( null != resultset ) resultset.close(); } catch( Exception ex ) {ex.printStackTrace(); mLogger.error(ex);}
		}
		
		//Copy ArrayList Values To Array of ReverseGeocodedLocationType
		revgeolocation = new ReverseGeocodedLocationType[reverseGeocodedLocation.size()];
		for(int i=0 ; i<reverseGeocodedLocation.size() ; i++){
			revgeolocation[i] = reverseGeocodedLocation.get(i);
		}

		//*** SORT the Result by distance ***
		ReverseGeocodedLocationType revI = null;
		ReverseGeocodedLocationType revJ = null;
		boolean boolchangeLocation = false;

		for(int i=0 ; i<revgeolocation.length ; i++){
			revI = revgeolocation[i];
			DistanceType distI = revI.getSearchCentreDistance();
			double dDistanceI = distI.getValue().doubleValue();
			if(distI.getUom().equals(DistanceUnitType.KM)){
				dDistanceI = dDistanceI * 1000;
			}
			boolchangeLocation = false;

			for(int j=i ; j<revgeolocation.length ; j++){
				revJ = revgeolocation[j];
				DistanceType distJ = revJ.getSearchCentreDistance();
				double dDistanceJ = distJ.getValue().doubleValue();
				if(distJ.getUom().equals(DistanceUnitType.KM)){
					dDistanceJ = dDistanceJ * 1000;
				}
				
				if(dDistanceI > dDistanceJ){
					ReverseGeocodedLocationType revTMP = revgeolocation[i];
					revgeolocation[i] = revgeolocation[j];
					revgeolocation[j] = revTMP;
					boolchangeLocation = true;
				}
			}
			
			if(boolchangeLocation)
				i=-1;
		}

		mLoggerCounter.info("ReverseGeocode;;;Search Results;"+revgeolocation.length);
		return revgeolocation;
	}
	
	private void addLocation(ResultSet resultSet, String PG_Address_SRS, String responseSRS, Coordinate location)throws Exception{

		Coordinate coordinate = getCoordinate(resultSet.getString("geom"), location);
		if(!PG_Address_SRS.equals(responseSRS)){
			coordinate = CoordTransform.transformGetCoord(PG_Address_SRS, responseSRS, coordinate); 
		}
		ResultAddress address = new ResultAddress(resultSet.getString("countrycode"), resultSet.getString("countrys"),
				resultSet.getString("postalcode"), resultSet.getString("municipal"), resultSet.getString("municipals"),
				//resultSet.getString("strname"), resultSet.getString("housenr"), null, 0.9, coordinate);
				resultSet.getString("strname"), null, null, 0.9, coordinate);
		
		PointType point = PointType.Factory.newInstance();
		DirectPositionType directpos = point.addNewPos();
		directpos.setStringValue(coordinate.x+" "+coordinate.y);
		directpos.setSrsName(responseSRS);

		//DISTANCE
		DistanceType distance = DistanceType.Factory.newInstance();
		double dDistanceToAddress = location.distance(coordinate);
		dDistanceToAddress = getDistance(dDistanceToAddress);
		
		if(dDistanceToAddress > 1000){
			dDistanceToAddress = dDistanceToAddress/1000;
			distance.setUom(DistanceUnitType.KM);
		}
		else
			distance.setUom(DistanceUnitType.M);

		DecimalFormat df = new DecimalFormat("0.0");
		distance.setValue(new BigDecimal(df.format(dDistanceToAddress).replace(",",".")));

		// ReverseGeocodedLocation
		ReverseGeocodedLocationType revgeolocationTMP = ReverseGeocodedLocationType.Factory.newInstance();
		revgeolocationTMP.setAddress(address.getAddress());
		revgeolocationTMP.setPoint(point);
		revgeolocationTMP.setSearchCentreDistance(distance);

		reverseGeocodedLocation.add(revgeolocationTMP);
	}
	
	private Coordinate getCoordinate(String resultset, Coordinate coordinate){
		Point point = null;
		try{
			WKTReader reader = new WKTReader(new GeometryFactory());
			Geometry geom = reader.read(resultset);

			if (geom instanceof MultiPoint)
				point = (Point) ((MultiPoint) geom).getGeometryN(0);
			else if (geom instanceof Point)
				point = (Point) geom;
			else if(geom instanceof MultiLineString){
				LineString lineString = (LineString) ((MultiLineString) geom).getGeometryN(0);
				
				if(lineString.getCoordinates().length == 2){
					if(lineString.getCoordinateN(0).equals(lineString.getCoordinateN(1)))
						return lineString.getCoordinateN(0);
					else
						return pointToLine(coordinate, lineString);
				}
				else
					return pointToLine(coordinate, lineString);
			}
			else if(geom instanceof LineString){
				LineString lineString = (LineString) geom;
				return pointToLine(coordinate, lineString);
			}
			else if (geom instanceof MultiPolygon){
				Polygon polygon = (Polygon) ((MultiPolygon) geom).getGeometryN(0);
				point = polygon.getCentroid();
			}
			else if (geom instanceof Polygon){			
				Polygon polygon = (Polygon) geom;
				point = polygon.getCentroid();
			}
		}catch(ParseException pe){
			mLogger.error(pe);
		}
		return point.getCoordinate();
	}
	
	private double getDistance(double angle){
		return 6378000*angle/(180/Math.PI);
	}
	
	private Coordinate pointToLine(Coordinate p, LineString lineString){
		Coordinate coordinates[] = lineString.getCoordinates();
		double distance = Double.MAX_VALUE;
		Coordinate coordinate = null;
		
		for(int i=0 ; i<coordinates.length-1 ; i++){
			double d = CoordTools.distancePointLine(p, coordinates[i], coordinates[i+1]);
			if(d<distance){
				distance=d;
				coordinate = CoordTools.pointLine(p, coordinates[i], coordinates[i+1]);
			}
		}

		return coordinate;
	}
}