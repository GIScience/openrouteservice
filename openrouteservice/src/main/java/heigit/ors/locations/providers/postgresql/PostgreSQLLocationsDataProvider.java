/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.locations.providers.postgresql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgresql.ds.PGSimpleDataSource;

import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;

public class PostgreSQLLocationsDataProvider implements LocationsDataProvider 
{
	private static final Logger LOGGER = Logger.getLogger(PostgreSQLLocationsDataProvider.class.getName());

	private String _query;
	private String _geomColumn;
	private int _geomSRID = 4326;
	private String[] _queryColumns;
	private int _geomColumnIndex;
	private HikariDataSource _dataSource;
	
	//private int kk =0;

	@SuppressWarnings("deprecation")
	public void init(Map<String, Object> parameters) throws Exception
	{
		_query = null;
		String value = (String)parameters.get("query");
		if (Helper.isEmpty(value))
			throw new Exception("'query' parameter can not be null or empty.");
		else
			_query = value;

		_geomColumn = null;
		value = (String)parameters.get("geometry_column");
		if (Helper.isEmpty(value))
			throw new Exception("'geometry_column' parameter can not be null or empty.");
		else
			_geomColumn = value;

		if (parameters.containsKey("geometry_srid"))
			_geomSRID = (Integer)parameters.get("geometry_srid");

		_query = _query.replace("!geometry_column!", _geomColumn) + (_query.toLowerCase().indexOf("where") > 0 ? " AND ": " WHERE ");
		
		HikariConfig config = new HikariConfig();

		config.setDataSourceClassName(PGSimpleDataSource.class.getName());
		config.addDataSourceProperty("databaseName", parameters.get("db_name"));
		config.addDataSourceProperty("user", parameters.get("user"));
		config.addDataSourceProperty("password", parameters.get("password"));
		config.addDataSourceProperty("serverName", parameters.get("host"));
		config.addDataSourceProperty("portNumber", parameters.get("port"));
		if (parameters.containsKey("max_pool_size"))
			config.setMaximumPoolSize((Integer)parameters.get("max_pool_size"));
		config.setMinimumIdle(1);
		config.setConnectionTestQuery("SELECT 1");

		_dataSource = new HikariDataSource(config);

		// retrieve meta data
		Connection connection = _dataSource.getConnection();
		if (connection == null)
			throw new Exception("Connection has not been established.");
		LocationsRequest request = new LocationsRequest();
		request.setGeometry(new Point(new Coordinate(0.0, 0.0), new PrecisionModel(), 4326));
		request.setRadius(10);

		PreparedStatement statement = createStatement(request, connection);
		ResultSetMetaData rs = statement.getMetaData();
		_queryColumns = new String[rs.getColumnCount()];

		for (int i = 1; i <= rs.getColumnCount(); i++) 
		{
			String clmName = rs.getColumnName(i);

			_queryColumns[i - 1] = clmName;
			if (clmName.equalsIgnoreCase(_geomColumn))
				_geomColumnIndex = i;
		}

		connection.close();
	}
	/*
	private void copyData() throws SQLException, ParseException
	{
		if (kk > 0)
			return;
		kk++;
		Connection destConn =  DriverManager.getConnection("jdbc:mysql://129.206.7.158/OSM?" +     "user=root&password=");
		destConn.setAutoCommit(false);
		
		Connection connection = _dataSource.getConnection();
		connection.setAutoCommit(false);

		PreparedStatement  ws = destConn.prepareStatement("INSERT INTO planet_osm_pois (osm_id, category, category_type, name, phone, website, opening_hours, access, smoking, way) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GEOGRAPHY_POINT (?, ?))");

		Statement statement = connection.createStatement();
		statement.setFetchSize(20000);
		ResultSet resSet = statement.executeQuery("SELECT osm_id, category, category_type, name, phone, website, opening_hours, access, smoking, ST_AsBinary(ST_Transform(way, 4326)) as way FROM planet_osm_pois WHERE way && ST_Transform(ST_MakeEnvelope(5.866240, 15.042050, 47.270210, 55.058140, 4326), 900913)");
		
		WKBReader wkbreader = new WKBReader();
		int i = 0;
		while (resSet.next()) 
		{
			long osmid = resSet.getLong(1);
			String cat = resSet.getString(2);
			String cattype = resSet.getString(3);
			String name = resSet.getString(4);
			String phone = resSet.getString(5);
			String web = resSet.getString(6);
			String ophours = resSet.getString(7);
			String access = resSet.getString(8);
			String smoking = resSet.getString(9);
			
			Geometry geom = wkbreader.read(resSet.getBytes(10));
			Point p = null;
			if (geom != null)
				p = (Point)geom;
			
		    ws.setLong(1, osmid);
		    ws.setString(2, cat);
		    ws.setString(3, cattype);
		    ws.setString(4, name);
		    ws.setString(5, phone);
		    ws.setString(6, web);
		    ws.setString(7, ophours);
		    ws.setString(8, access);
		    ws.setString(9, smoking);
		    
		    if (p != null)
		    {
		    	ws.setDouble(10, p.getX());
		    	ws.setDouble(11, p.getY());
		    }
		    else
		    {
		    	ws.setDouble(10, 0);
	    	    ws.setDouble(11, 0);
		    }
		    
		    
			ws.addBatch();
            i++;

            if (i % 20000 == 0) {
            	ws.executeBatch(); // Execute every 1000 items.
            	ws.clearBatch();//clear the batch after execution
     		   
            	System.out.println(i);
            }
		}

		ws.executeBatch();
		destConn.commit();

		ws.close();
	
		statement.close();
		connection.close();
	}
*/
	public LocationsResult[] findLocations(LocationsRequest request) throws Exception
	{
		List<LocationsResult> results = new ArrayList<LocationsResult>();
		
		//copyData();

		Connection connection = null;
		Exception exception = null;

		try
		{
			connection = _dataSource.getConnection();
			if (connection == null)
				throw new Exception("Connection has not been established.");

			StopWatch sw = null;
			if (LOGGER.isDebugEnabled())
			{
				sw = new StopWatch();
				sw.start();
			}

			PreparedStatement statement = null;
			ResultSet resSet = null;

			if (LOGGER.isDebugEnabled())
			{
				StopWatch sw2 = new StopWatch();
				sw2.start();

				statement = createStatement(request, connection);

				sw2.stop();

				LOGGER.debug(String.format("Preparing query took %.2f sec.", sw2.getSeconds()));

				sw2.start();
				resSet = statement.executeQuery();

				sw2.stop();
				LOGGER.debug(String.format("Executing query took %.2f sec.", sw2.getSeconds()));
			}
			else
			{
				statement = createStatement(request, connection);
				resSet = statement.executeQuery();
			}

			WKBReader reader = new WKBReader();
			int nColumns = _queryColumns.length;

			while (resSet.next()) 
			{
				try {
					LocationsResult lr = new LocationsResult();

					for(int i = 1; i <= nColumns; i++)
					{
						if (i != _geomColumnIndex)
							lr.addProperty(_queryColumns[i - 1], resSet.getString(i));		
					}

					Geometry geom = reader.read(resSet.getBytes(_geomColumnIndex));
					lr.setGeometry(geom);

					results.add(lr);
				} catch (Exception ex) {
					LOGGER.error(ex);
					throw new IOException(ex.getMessage());
				}
			}

			resSet.close();
			statement.close();

			if (LOGGER.isDebugEnabled())
			{
				sw.stop();
				LOGGER.debug(String.format("Find %d locations in %.2f sec.", results.size(), sw.getSeconds()));
			}
		}
		catch(Exception ex)
		{
			LOGGER.error(ex);
			exception = ex;
		}
		finally
		{
			if (connection != null)
				connection.close();
		}

		if (exception != null)
			throw exception;

		return results.toArray(new LocationsResult[results.size()]);
	}

	private PreparedStatement createStatement(LocationsRequest request, Connection conn) throws SQLException, IOException
	{
		PreparedStatement statement = null;
		//SELECT osm_id, amenity, name, ST_AsBinary(ST_Transform(way, 4326)) as geom FROM planet_osm_pois WHERE way IS NOT NULL

		String stateText = _query;

		if (request.getQuery() != null)
			stateText+=  "(" + request.getQuery() + ")" + " AND ";

		Envelope bbox = request.getBBox();
		if (bbox != null)
		{
			if (_geomSRID != 4326)
				stateText += String.format("(%s && ST_Transform(ST_MakeEnvelope(%.1f,%.1f,%.1f,%.1f, 4326), %d)) AND ", _geomColumn, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), _geomSRID);
			else
				stateText += String.format("(%s && ST_MakeEnvelope(%.1f,%.1f,%.1f,%.1f,4326)) AND ", _geomColumn, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
		}

		Geometry reqGeom = request.getGeometry();
		if (reqGeom instanceof Point)
		{
			Point p = (Point)reqGeom;
			if (_geomSRID != 4326)
				stateText += String.format("ST_Distance(%s, ST_Transform(ST_SetSRID(ST_MakePoint(%.1f, %.1f), 4326), %d)) <= %.1f", _geomColumn, p.getCoordinate().x, p.getCoordinate().y, _geomSRID, request.getRadius());
			else
				stateText += String.format("ST_Distance(%s, ST_SetSRID(ST_MakePoint(%.1f, %.1f), 4326)) <= %.1f", _geomColumn, p.getCoordinate().x, p.getCoordinate().y, request.getRadius());
			
			if (request.getLimit() > 0)
				stateText += " LIMIT " + request.getLimit();

			statement = conn.prepareStatement(stateText);	
		} 
		else 
		{
			WKBWriter wkbWriter = new WKBWriter();
			ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
			wkbWriter.write(reqGeom, new OutputStreamOutStream(bytesStream));

			if (reqGeom instanceof LineString)
			{
				if (_geomSRID != 4326)
					stateText += String.format("ST_DWithin(%s, ST_Transform(ST_GeomFromWKB(?, 4326), %d), %.1f)", _geomColumn, _geomSRID,  request.getRadius());
				else
					stateText += String.format("ST_DWithin(%s, ST_GeomFromWKB(?, 4326), %.1f)", _geomColumn, request.getRadius());
			}
			else
			{
				if (_geomSRID != 4326)
					stateText += String.format("ST_Contains(%s, ST_Transform(ST_GeomFromWKB(?, 4326), %d))", _geomColumn, _geomSRID);
				else
					stateText += String.format("ST_Contains(%s, ST_GeomFromWKB(?, 4326))", _geomColumn);
			}

			if (request.getLimit() > 0)
				stateText += " LIMIT " + request.getLimit();

			statement = conn.prepareStatement(stateText);                                
			statement.setBytes(1, bytesStream.toByteArray());

			bytesStream.close();
		}

		return statement;
	}

	public void close()
	{
		if (_dataSource != null)
		{
			_dataSource.close();
			_dataSource= null;
		}
	}

	public String getName()
	{
		return "postgresql";
	}
}
