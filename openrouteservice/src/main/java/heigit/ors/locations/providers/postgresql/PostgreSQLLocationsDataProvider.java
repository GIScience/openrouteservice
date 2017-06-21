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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgresql.ds.PGSimpleDataSource;

import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import heigit.ors.locations.providers.LocationsDataProvider;
import heigit.ors.util.ArraysUtility;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.jts.JTS;
import heigit.ors.locations.LocationDetailsType;
import heigit.ors.locations.LocationsCategory;
import heigit.ors.locations.LocationsCategoryClassifier;
import heigit.ors.locations.LocationsCategoryGroup;
import heigit.ors.locations.LocationsErrorCodes;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;
import heigit.ors.locations.LocationsResultSortType;
import heigit.ors.locations.LocationsSearchFilter;

public class PostgreSQLLocationsDataProvider implements LocationsDataProvider 
{
	private static final Logger LOGGER = Logger.getLogger(PostgreSQLLocationsDataProvider.class.getName());

	private static Map<Integer, QueryColumnsInfo> COLUMNS_INFO;
	 
	private String _tableName = null;
	private int _geomColumnIndex = 3;
	private HikariDataSource  _dataSource;
	
	static
	{
		COLUMNS_INFO = new HashMap<Integer, QueryColumnsInfo>();
	}
	
	private static ColumnDescription[] getColumnsDescription(int details)
	{
		List<ColumnDescription> res = new ArrayList<ColumnDescription>();
		
		res.add(new ColumnDescription("osm_id", Long.class));
		res.add(new ColumnDescription("category", Integer.class)); 
	    res.add(new ColumnDescription("name", String.class));
	    res.add(new ColumnDescription("geom", Geometry.class));
	    
	    if (LocationDetailsType.isSet(details, LocationDetailsType.ADDRESS))
	    	res.add(new ColumnDescription("address", String.class));
	    
	    if (LocationDetailsType.isSet(details, LocationDetailsType.CONTACT))
	    {
	    	res.add(new ColumnDescription("phone", String.class));
	    	res.add(new ColumnDescription("website", String.class));
	    }
		
	    if (LocationDetailsType.isSet(details, LocationDetailsType.ATTRIBUTES))
	    {
	    	res.add(new ColumnDescription("opening_hours", String.class));
	    	res.add(new ColumnDescription("wheelchair", String.class));
	    	res.add(new ColumnDescription("smoking", String.class));
	    	res.add(new ColumnDescription("fee", String.class));
	    }
	    else
	    {
	    	if ((details & 16) == 16)
	    		res.add(new ColumnDescription("wheelchair", String.class));
	    	if ((details & 32) == 32)
	    		res.add(new ColumnDescription("smoking", String.class));
	    	if ((details & 64) == 64)
	    		res.add(new ColumnDescription("fee", String.class));
	    }
	    
	    res.add(new ColumnDescription("distance", Double.class));
	    
		return  res.toArray(new ColumnDescription[res.size()]);
	}
	
	private static QueryColumnsInfo getQueryColumnsInfo(LocationsRequest request)
	{
		int details = request.getDetails();
		LocationsSearchFilter filter = request.getSearchFilter();
		
		int hash = details;
		if (!Helper.isEmpty(filter.getWheelchair()))
			hash |= 16;
		
		if (!Helper.isEmpty(filter.getSmoking()))
			hash |= 32; 

		if (!Helper.isEmpty(filter.getSmoking()))
			hash |= 64; 

		synchronized(COLUMNS_INFO)
		{
			QueryColumnsInfo res = COLUMNS_INFO.get(hash);
			if (res == null)
			{
				List<String> ignoreColumns = null;
				if (!LocationDetailsType.isSet(details, LocationDetailsType.ATTRIBUTES) && hash >= 16)
				{
					ignoreColumns = new ArrayList<String>(3);
					ignoreColumns.add("wheelchair");
					ignoreColumns.add("smoking");
					ignoreColumns.add("fee");
				}
				
				res = new QueryColumnsInfo(getColumnsDescription(hash), ignoreColumns);
				COLUMNS_INFO.put(hash, res);
			}
			
			return res;
		}
	}

	public void init(Map<String, Object> parameters) throws Exception
	{
		_dataSource = null;
		_tableName = null;

		String value = (String)parameters.get("table_name");
		if (Helper.isEmpty(value))
			throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "'table_name' parameter can not be null or empty.");
		else
			_tableName = value;
		
		//https://github.com/pgjdbc/pgjdbc/pull/772
		org.postgresql.Driver.isRegistered();
	
		HikariConfig config = new HikariConfig();
		String port = "5432";
		if (parameters.containsKey("port"))
			port = Integer.toString((Integer)parameters.get("port"));
		config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s",parameters.get("host"), port, parameters.get("db_name")));
		config.setDataSourceClassName(PGSimpleDataSource.class.getName());
		config.addDataSourceProperty("databaseName", parameters.get("db_name"));
		config.addDataSourceProperty("user", parameters.get("user"));
		if (parameters.containsKey("password"))
			config.addDataSourceProperty("password", parameters.get("password"));
		config.addDataSourceProperty("serverName", parameters.get("host"));
		config.addDataSourceProperty("portNumber", parameters.get("port"));
		if (parameters.containsKey("max_pool_size"))
			config.setMaximumPoolSize((Integer)parameters.get("max_pool_size"));
		config.setMinimumIdle(1);
		config.setConnectionTestQuery("SELECT 1");

		_dataSource = new HikariDataSource(config);
	}

	public List<LocationsResult> findLocations(LocationsRequest request) throws Exception
	{
		List<LocationsResult> results = new ArrayList<LocationsResult>();

		Connection connection = null;
		PreparedStatement statement = null;
		Exception exception = null;

		try
		{
			connection = _dataSource.getConnection();
			connection.setAutoCommit(false);

			StopWatch sw = null;
			if (LOGGER.isDebugEnabled())
			{
				sw = new StopWatch();
				sw.start();
			}

			ResultSet resSet = null;
			QueryColumnsInfo queryColumns = getQueryColumnsInfo(request);

			if (LOGGER.isDebugEnabled())
			{
				StopWatch sw2 = new StopWatch();
				sw2.start();

				statement = createLocationsStatement(request, connection, queryColumns);

				sw2.stop();

				LOGGER.debug(String.format("Preparing query took %.3f sec.", sw2.getSeconds()));

				sw2.start();
				resSet = statement.executeQuery();

				sw2.stop();
				LOGGER.debug(String.format("Executing query took %.3f sec.", sw2.getSeconds()));
			}
			else
			{
				statement = createLocationsStatement(request, connection, queryColumns);
				resSet = statement.executeQuery();
			}

			int nColumns = queryColumns.getReturnColumnsCount();
			if (request.getGeometry() instanceof Polygon || request.getGeometry() == null)
				nColumns--; // skip distance column for polygons
			
			WKBReader wkbReader = new WKBReader();
			
			while (resSet.next()) 
			{
				try {
					LocationsResult lr = new LocationsResult();

					for(int i = 1; i <= nColumns; i++)
					{
						if (i - 1 != _geomColumnIndex)
						{
							Object value =  queryColumns.getType(i - 1, resSet);
							if (value != null)
								lr.addProperty(queryColumns.getName(i - 1), value);
						}
						else
						{
							byte[] bytes = resSet.getBytes(_geomColumnIndex + 1);
							if (bytes != null)
								lr.setGeometry(wkbReader.read(bytes));
						}
					}

					results.add(lr);
				} catch (Exception ex) {
					LOGGER.error(ex);
					throw new IOException(ex.getMessage());
				}
			}

			resSet.close();

			if (LOGGER.isDebugEnabled())
			{
				sw.stop();
				LOGGER.debug(String.format("Found %d locations in %.3f sec.", results.size(), sw.getSeconds()));
			}
		}
		catch(Exception ex)
		{
			LOGGER.error(ex);
			exception = new InternalServerException(LocationsErrorCodes.UNKNOWN, "Unable to retrieve data from the data source.");
		}
		finally
		{
			if (statement != null)
				statement.close();

			if (connection != null)
				connection.close();
		}

		if (exception != null)
			throw exception;

		return results;
	}

	private String buildSearchFilter(LocationsSearchFilter filter) throws Exception
	{
		String cmdText = "";
		
		if (filter != null)
		{
			if (filter.getCategoryGroupIds() != null)
				cmdText = addConditions(cmdText, "(" + buildCategoryGroupIdsFilter(filter.getCategoryGroupIds()) + ")");
			else if (filter.getCategoryIds() != null)
				cmdText = addConditions(cmdText, "(" + buildCategoryIdsFilter(filter.getCategoryIds()) + ")");

			if (filter.getName() != null)
			{
				if (filter.getName().contains("*"))
					cmdText = addConditions(cmdText, "(name IS NOT NULL AND (lower(name) LIKE ''" + filter.getName().replace("*", "%%").toLowerCase() + "''))");
				else
					cmdText = addConditions(cmdText, "(lower(name) = ''" + filter.getName().toLowerCase() + "'')");
			}

			if (!Helper.isEmpty(filter.getWheelchair()))
			{
				if (filter.getWheelchair().indexOf(',') > 0)
					cmdText = addConditions(cmdText, "(wheelchair IN ("+ fixStringValues(filter.getWheelchair(), true) +"))");
				else
					cmdText = addConditions(cmdText, "(wheelchair = "+ fixStringValues(filter.getWheelchair(), false) +")");
			}
			if (!Helper.isEmpty(filter.getSmoking()))
			{
				if (filter.getSmoking().indexOf(',') > 0)
					cmdText = addConditions(cmdText, "(smoking IN (" + fixStringValues(filter.getSmoking(), true) + "))");
				else
					cmdText = addConditions(cmdText, "(smoking = "+ fixStringValues(filter.getSmoking(), false) +")");
			}

			if (filter.getFee() != null)
				cmdText = addConditions(cmdText, "(fee =" + (filter.getFee() == true ? "1": "0") + ")");
		}

		return cmdText;
	}

	private String addConditions(String condition1, String condition2)
	{
		if (!Helper.isEmpty(condition1))
			return condition1 + " AND " + condition2;
		else
			return condition2;
	}

	private PreparedStatement createLocationsStatement(LocationsRequest request, Connection conn, QueryColumnsInfo queryInfo) throws Exception
	{
		Geometry geom = request.getGeometry();
		Envelope bbox = request.getBBox();

		byte[] geomBytes = geometryToWKB(geom, bbox);

		// at the end, we add virtual column to store the exact distance. 
		String query = "SELECT " + queryInfo.getQuery1Columns() + " FROM " + _tableName;
		
		String whereCondition = "";
		
		String searchCondition = buildSearchFilter(request.getSearchFilter());
		if (!Helper.isEmpty(searchCondition))
			whereCondition += searchCondition;
		
		String stateText = String.format("SELECT %s FROM ORS_FindLocations('(%s) as tmp', '%s', ?, %.3f, %d) AS %s", queryInfo.getQuery2Columns(), query, whereCondition, request.getRadius(), request.getLimit(), queryInfo.getReturnTable());

		if (request.getSortType() != LocationsResultSortType.NONE)
		{
			if (request.getSortType() == LocationsResultSortType.CATEGORY)
				stateText += " ORDER BY category";
			else if (request.getSortType() == LocationsResultSortType.DISTANCE)
				stateText += " ORDER BY distance";
		}

		PreparedStatement statement = conn.prepareStatement(stateText);    
		statement.setMaxRows(request.getLimit());
		if (geomBytes != null)
			statement.setBytes(1, geomBytes);

		return statement;
	}

	public List<LocationsCategory> findCategories(LocationsRequest request) throws Exception
	{
		List<LocationsCategory> results = new ArrayList<LocationsCategory>();

		Connection connection = null;
		Exception exception = null;

		try
		{
			connection = _dataSource.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement statement = createCategoriesStatement(request, connection);			
			ResultSet resSet = statement.executeQuery();

			Map<Integer, Map<Integer, Long>> groupsStats = new HashMap<Integer, Map<Integer, Long>>();
			long[] groupCount = new long[LocationsCategoryClassifier.getGroupsCount()];

			while (resSet.next()) 
			{
				// "SELECT category, COUNT(category) FROM planet_osm_pois_test GROUP BY category"
				int catIndex = resSet.getInt(1);
				int groupIndex = LocationsCategoryClassifier.getGroupIndex(catIndex);
				long count = resSet.getLong(2);

				Map<Integer, Long> stats = groupsStats.get(groupIndex);
				if (stats == null)
				{
					stats = new HashMap<Integer, Long>();
					groupsStats.put(groupIndex, stats);
				}

				groupCount[groupIndex] += count;
				stats.put(catIndex, count);
			}

			resSet.close();
			statement.close();

			for (Map.Entry<Integer, Map<Integer, Long>> stats : groupsStats.entrySet())
			{
				int groupIndex = stats.getKey();
				LocationsCategory lc = new LocationsCategory(LocationsCategoryClassifier.getGroupId(groupIndex), LocationsCategoryClassifier.getGroupName(groupIndex), stats.getValue(), groupCount[groupIndex]);

				results.add(lc);
			}
		}
		catch(Exception ex)
		{
			LOGGER.error(ex);
			exception = new InternalServerException(LocationsErrorCodes.UNKNOWN, "Unable to retrieve data from the data source.");
		}
		finally
		{
			if (connection != null)
				connection.close();
		}

		if (exception != null)
			throw exception;

		return results;
	}

	private PreparedStatement createCategoriesStatement(LocationsRequest request, Connection conn) throws Exception 
	{
		String cmdFilter = buildSearchFilter(request.getSearchFilter()); 

		byte[] 	geomBytes = null;
		Geometry geom = request.getGeometry();
		Envelope bbox = request.getBBox();
		
		if (geom != null)
		   geomBytes = geometryToWKB(geom, bbox);

		if (bbox != null)
			cmdFilter = addConditions(cmdFilter, buildBboxFilter(bbox));

		String stateText = null;
		
		if (geom == null)
			stateText = String.format("SELECT category, COUNT(category) AS count FROM %s WHERE (%s) GROUP BY category ORDER BY category", _tableName, cmdFilter);
		else
			stateText = String.format("SELECT * FROM ORS_FindLocationCategories('%s', '%s', ?, %.3f) AS categories(category smallint, count bigint)", _tableName, cmdFilter, request.getRadius());

		PreparedStatement statement = conn.prepareStatement(stateText);
		if (geomBytes != null)
			statement.setBytes(1, geomBytes);
		
		return statement;
	}
	
	private byte[] geometryToWKB(Geometry geom, Envelope bbox) throws IOException
	{
		if (geom == null)
			geom = JTS.toGeometry(bbox);
		
		WKBWriter wkbWriter = new WKBWriter();
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
		wkbWriter.write(geom, new OutputStreamOutStream(bytesStream));
		byte[] geomBytes = bytesStream.toByteArray();
		bytesStream.close();
		
		return geomBytes;
	}

	private String buildBboxFilter(Envelope bbox)
	{
		return  String.format(" (geom && ST_Transform(ST_MakeEnvelope(%.7f,%.7f,%.7f,%.7f,4326), 900913))", bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
	}

	private String buildCategoryIdsFilter(int[] ids)
	{
		if (ids == null)
			return "";

		if (ids.length == 1)
			return "category = " + ids[0];
		else
			return "category IN (" + ArraysUtility.toString(ids, ", ") + ")";
	}

	private String buildCategoryGroupIdsFilter(int[] ids) throws Exception
	{
		if (ids == null)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append("(");

		if (ids.length > 1)
		{
			int nValues = ids.length;

			for (int i = 0; i < nValues; i++)
			{
				int groupId = ids[i];
				LocationsCategoryGroup group = LocationsCategoryClassifier.getGroupById(groupId);
				if (group == null)
					throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "category_group_id", Integer.toString(groupId));

				sb.append("(");
				sb.append(group.getMinCategoryId());
				sb.append(" <= category AND category <= ");
				sb.append(group.getMaxCategoryId());
				sb.append(")");

				if (i < nValues - 1)
					sb.append(" OR ");
			}
		}
		else
		{
			int groupId = ids[0];

			LocationsCategoryGroup group = LocationsCategoryClassifier.getGroupById(groupId);
			if (group == null)
				throw new UnknownParameterValueException(LocationsErrorCodes.INVALID_PARAMETER_VALUE, "category_group_id", Integer.toString(groupId));

			sb.append(group.getMinCategoryId());
			sb.append(" <= category AND category <= ");
			sb.append(group.getMaxCategoryId());
		}

		sb.append(")");

		return sb.toString();
	}

	private String fixStringValues(String value, Boolean multipeValues)
	{
		if (multipeValues)
		{
			String result = "";

			String[] values = value.split(",");
			int nValues = values.length;
			for(int i = 0; i < nValues; i++)
			{
				result += "''" + values[i].trim() + "''";
				if (i < nValues - 1)
					result += ",";
			}

			return result;
		}
		else
		{
			if (value.indexOf('\'') > 0)
				return value;
			else
				return "''"+ value+"''";
		}
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
