/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.isochrones.statistics.postgresql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgresql.ds.PGSimpleDataSource;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.statistics.AbstractStatisticsProvider;
import heigit.ors.locations.LocationsErrorCodes;

public class PostgresSQLStatisticsProvider extends AbstractStatisticsProvider 
{
	private static final Logger LOGGER = Logger.getLogger(PostgresSQLStatisticsProvider.class.getName());

	private String _tableName = null;
	private String _geomColumn = null;
	private HikariDataSource  _dataSource;

	@Override
	public void init(Map<String, Object> parameters) throws Exception {
		_dataSource = null;
		_tableName = null;
		_geomColumn = null;

		String value = (String)parameters.get("table_name");
		if (Helper.isEmpty(value))
			throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "'table_name' parameter can not be null or empty.");
		else
			_tableName = value;

		value = (String)parameters.get("geometry_column");
		if (Helper.isEmpty(value))
			throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "'geometry_column' parameter can not be null or empty.");
		else
			_geomColumn = value;


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

	@Override
	public void close() throws Exception 
	{
		if (_dataSource != null)
		{
			_dataSource.close();
			_dataSource= null;
		}
	}

	@Override
	public double[] getStatistics(Isochrone isochrone, String[] properties) throws Exception
	{
		int nProperties = properties.length;
		double[] res = new double[nProperties];

		Connection connection = null;
		PreparedStatement statement = null;
		Exception exception = null;

		try
		{
			String strParams = "";
			String strRatioParams = "";
			for (int i = 0; i < nProperties; i++)
			{
				strRatioParams += "SUM(overlap_ratio * \"" + properties[i] + "\")";
				strParams += "\"" + properties[i] + "\"";
				if (i < nProperties - 1)
				{
					strRatioParams += ", ";
					strParams += ", ";
				}
			}
			
			String sql = "SELECT " + strRatioParams + " FROM " +
			        "(" + 
					  "SELECT" + strParams + "," + String.format("ST_Area(ST_Intersection(%s, ?)) / ST_Area(%s) overlap_ratio FROM %s WHERE ST_Intersects(%s, ?)", _geomColumn, _geomColumn, _tableName, _geomColumn) +
					") AS tbl";
			
			byte[] geomBytes = geometryToWKB(isochrone.getGeometry());

			connection = _dataSource.getConnection();
			connection.setAutoCommit(false);

			statement = connection.prepareStatement(sql);
			statement.setBytes(1, geomBytes);
			statement.setBytes(2, geomBytes);

			ResultSet resSet = statement.executeQuery();
			
			if (resSet.next())
			{
				for (int i = 0; i < nProperties; i++)
					res[i] = resSet.getDouble(i + 1);
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

		return res;

	}

	private byte[] geometryToWKB(Geometry geom) throws IOException
	{
		WKBWriter wkbWriter = new WKBWriter();
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
		wkbWriter.write(geom, new OutputStreamOutStream(bytesStream));
		byte[] geomBytes = bytesStream.toByteArray();
		bytesStream.close();

		return geomBytes;
	}

	@Override
	public String getName() {
		return "postgresql";
	}
}
