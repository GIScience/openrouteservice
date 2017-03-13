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
package heigit.ors.locations.providers.memsql;

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

import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;

public class MemSQLLocationsDataProvider implements LocationsDataProvider 
{
	private static final Logger LOGGER = Logger.getLogger(MemSQLLocationsDataProvider.class.getName());

	private String _query;
	private String _geomColumn;
	private String[] _queryColumns;
	private int _geomColumnIndex = -1;
	private int _latitudeColumnIndex = -1;
	private int _longitudeColumnIndex = -1;
	private HikariDataSource  _dataSource;
	private GeometryFactory _geomFactory;
	
	public void init(Map<String, Object> parameters) throws Exception
	{
		_query = null;
		boolean queryHasWhere = false;
		String value = (String)parameters.get("query");
		if (Helper.isEmpty(value))
			throw new Exception("'query' parameter can not be null or empty.");
		else
		{
			_query = value;
		    queryHasWhere = _query.toLowerCase().indexOf("where") > 0;
		}
		
		_geomColumn = null;
		value = (String)parameters.get("geometry_column");
		if (Helper.isEmpty(value))
			throw new Exception("'geometry_column' parameter can not be null or empty.");
		else
			_geomColumn = value;
		
		String latitudeColumn = (String)parameters.get("latitude_column");
		String longitudeColumn = (String)parameters.get("longitude_column");

		_query = _query.replace("!geometry_column!", _geomColumn) +  String.format(" with (index = %s, resolution = 8) ", _geomColumn) + (queryHasWhere ? " AND ": " WHERE ");

		_geomFactory = new GeometryFactory();

		HikariConfig config = new HikariConfig();
		config.setDriverClassName("com.mysql.jdbc.Driver");
		String port = "3306";
		if (parameters.containsKey("port"))
			port = Integer.toString((Integer)parameters.get("port"));
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s",parameters.get("host"), port, parameters.get("db_name")) + "?max_allowed_packet=104857600");
		config.addDataSourceProperty("user", parameters.get("user"));
		if (parameters.containsKey("password"))
			config.addDataSourceProperty("password", parameters.get("password"));
		if (parameters.containsKey("max_pool_size"))
			config.setMaximumPoolSize((Integer)parameters.get("max_pool_size"));
		config.setMinimumIdle(1);
		config.setConnectionTestQuery("SELECT 1");
		config.setAutoCommit(false);

		_dataSource = new HikariDataSource(config);

		// retrieve meta data
		Connection connection = _dataSource.getConnection();
		if (connection == null)
			throw new Exception("Connection has not been established.");
		
		LocationsRequest request = new LocationsRequest();
		request.setGeometry(_geomFactory.createPoint(new Coordinate(0,0)));
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
			else if (latitudeColumn != null && clmName.equalsIgnoreCase(latitudeColumn))
				_latitudeColumnIndex = i;
			else if (longitudeColumn != null && clmName.equalsIgnoreCase(longitudeColumn))
				_longitudeColumnIndex = i;
		}

		connection.close();
	}
	
	public LocationsResult[] findLocations(LocationsRequest request) throws Exception
	{
		List<LocationsResult> results = new ArrayList<LocationsResult>();
		
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

			int nColumns = _queryColumns.length;

			while (resSet.next()) 
			{
				try {
					LocationsResult lr = new LocationsResult();

					for(int i = 1; i <= nColumns; i++)
					{
						if (i != _geomColumnIndex && i != _latitudeColumnIndex && i != _longitudeColumnIndex)
							lr.addProperty(_queryColumns[i - 1], resSet.getString(i));		
					}

					if (_geomColumnIndex != -1)
					{
						String strGeom = resSet.getString(_geomColumnIndex);
						if (strGeom != null)
						{
							int pos1 = strGeom.indexOf('(');
							int pos2 = strGeom.indexOf(' ');
							String value = strGeom.substring(pos1 + 1, pos2);
							double x = Double.parseDouble(value);

							pos1 =  strGeom.indexOf(')');
							value = strGeom.substring(pos2+1, pos1);
							double y = Double.parseDouble(value);
							lr.setGeometry(_geomFactory.createPoint(new Coordinate(x,y)));
						}
					}
					else
					{
						double x = resSet.getDouble(_longitudeColumnIndex);
						double y = resSet.getDouble(_latitudeColumnIndex);
						lr.setGeometry(_geomFactory.createPoint(new Coordinate(x,y)));
					}

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

		String stateText = _query;

		if (request.getQuery() != null)
			stateText +=  "(" + request.getQuery() + ")" + " AND ";

		Envelope bbox = request.getBBox();
		if (bbox != null)
		{
		/*	if (_geomSRID != 4326)
				stateText += String.format("(%s && ST_Transform(ST_MakeEnvelope(%.1f,%.1f,%.1f,%.1f, 4326), %d)) AND ", _geomColumn, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), _geomSRID);
			else
				stateText += String.format("(%s && ST_MakeEnvelope(%.1f,%.1f,%.1f,%.1f,4326)) AND ", _geomColumn, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
		*/}

		Geometry reqGeom = request.getGeometry();
		if (reqGeom instanceof Point)
		{
			Point p = (Point)reqGeom;
			stateText += String.format("GEOGRAPHY_WITHIN_DISTANCE(%s, GEOGRAPHY_POINT(%.1f, %.1f), %.1f)", _geomColumn, p.getCoordinate().x, p.getCoordinate().y, request.getRadius());
		} 
		else 
		{
			WKTWriter wktWriter = new WKTWriter();
			stateText += String.format("GEOGRAPHY_WITHIN_DISTANCE(%s, \"%s\", %.1f)", _geomColumn, wktWriter.write(reqGeom), request.getRadius());
		}

		if (request.getLimit() > 0)
			stateText += " LIMIT 0, " + request.getLimit() + ";";

		statement = conn.prepareStatement(stateText);    
		statement.setMaxRows(request.getLimit());

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
		return "memsql";
	}
}
