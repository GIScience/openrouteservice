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

/**
 * This class handles the population statistic queries. It generates internal api calls to SQL statements that are
 * queried against the set PostgreSQL-Server holding the population data.
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class PostgresSQLStatisticsProvider extends AbstractStatisticsProvider {
    private static final Logger LOGGER = Logger.getLogger(PostgresSQLStatisticsProvider.class.getName());

    private String _tableName = null;
    private String _geomColumn = null;
    private HikariDataSource _dataSource;

    /**
     * This function initializes the connection to the server according to the settings in the app.config.
     * The connection is established using a {@link HikariDataSource} object with the configuration data from the app.config.
     *
     * @param parameters {@link Map} holding the server configuration data from the app.config.
     * @throws Exception
     */
    @Override
    public void init(Map<String, Object> parameters) throws Exception {
        _dataSource = null;
        _tableName = null;
        _geomColumn = null;

        String value = (String) parameters.get("table_name");
        if (Helper.isEmpty(value))
            throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "'table_name' parameter can not be null or empty.");
        else
            _tableName = value;

        value = (String) parameters.get("geometry_column");
        if (Helper.isEmpty(value))
            throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "'geometry_column' parameter can not be null or empty.");
        else
            _geomColumn = value;


        //https://github.com/pgjdbc/pgjdbc/pull/772
        org.postgresql.Driver.isRegistered();

        HikariConfig config = new HikariConfig();
        String port = "5432";
        if (parameters.containsKey("port"))
            port = Integer.toString((Integer) parameters.get("port"));
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s", parameters.get("host"), port, parameters.get("db_name")));
        config.setDataSourceClassName(PGSimpleDataSource.class.getName());
        config.addDataSourceProperty("databaseName", parameters.get("db_name"));
        config.addDataSourceProperty("user", parameters.get("user"));
        if (parameters.containsKey("password"))
            config.addDataSourceProperty("password", parameters.get("password"));
        config.addDataSourceProperty("serverName", parameters.get("host"));
        config.addDataSourceProperty("portNumber", parameters.get("port"));
        if (parameters.containsKey("max_pool_size"))
            config.setMaximumPoolSize((Integer) parameters.get("max_pool_size"));
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");

        _dataSource = new HikariDataSource(config);
    }

    /**
     * This function closes the {@link HikariDataSource} connection.
     *
     * @throws Exception
     */
    @Override
    public void close() {
        if (_dataSource != null) {
            _dataSource.close();
            _dataSource = null;
        }
    }

    /**
     * The function takes an {@link Isochrone} as an input along with a {@link String}[] holding the attributes parameters set in the api attributes variable.
     * For now only pop_area and pop_total can be asked. Together or as single values.
     *
     * @param isochrone  {@link Isochrone} as input.
     * @param properties {@link String}[] as input holding the attributes parameters.
     * @return Returns a double[] holding the desired values in the order that was asked for in the attributes.
     * @throws Exception If the query doesn't return any values or the sql is corrupt, an {@link Exception} will be thrown.
     */
    @Override
    public double[] getStatistics(Isochrone isochrone, String[] properties) throws Exception {
        int nProperties = properties.length;
        double[] res = new double[nProperties];
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        try {
            String sql = null;
            for (String property : properties) {
                String polyGeom = isochrone.getGeometry().toText();
                switch (property) {
                    case "total_area_km":
                    case "total_pop":
                        sql = "SELECT ST_Area(poly) / 1000000 AS total_area_km, ROUND(SUM((ST_SummaryStats(ST_Clip(" + _geomColumn + ", poly))).sum)) AS total_pop FROM " + _tableName + ", ST_Simplify(ST_Transform(ST_GeomFromText('" + polyGeom + "', 4326), 954009), 125) AS poly WHERE ST_Intersects(poly, " + _geomColumn + ") GROUP BY poly;";
                        break;
                    default:
                        break;
                }

            }
            connection = _dataSource.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);

            resultSet = preparedStatement.executeQuery();
            // check if the resultSet contains values
            if (resultSet.next()) {
                // check for each property if the result contains a value
                int propertyCounter = 0;
                for (String property : properties) {
                    int i = 0;
                    // check each value
                    while (i < nProperties) {
                        // Get the column name
                        String columnName = resultSet.getMetaData().getColumnName(i + 1);
                        // If a value fits the current property it is set in the correct place in the result[]
                        if (columnName.equals(property))
                            res[propertyCounter] = resultSet.getDouble(i + 1);
                        i++;
                    }
                    propertyCounter++;
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(LocationsErrorCodes.UNKNOWN, "Unable to retrieve data from the data source.");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
        return res;

    }


    @Deprecated
    public double[] getStatisticsOld(Isochrone isochrone, String[] properties) throws Exception {
        int nProperties = properties.length;
        double[] res = new double[nProperties];

        Connection connection = null;
        PreparedStatement statement = null;
        Exception exception = null;

        try {
            String strParams = "";
            String strRatioParams = "";
            for (int i = 0; i < nProperties; i++) {
                strRatioParams += "SUM(overlap_ratio * \"" + properties[i] + "\")";
                strParams += "\"" + properties[i] + "\"";
                if (i < nProperties - 1) {
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

            if (resSet.next()) {
                for (int i = 0; i < nProperties; i++)
                    res[i] = resSet.getDouble(i + 1);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            exception = new InternalServerException(LocationsErrorCodes.UNKNOWN, "Unable to retrieve data from the data source.");
        } finally {
            if (statement != null)
                statement.close();

            if (connection != null)
                connection.close();
        }

        if (exception != null)
            throw exception;

        return res;

    }

    /**
     * This function translates a {@link Geometry} object into a byte[] representation.
     * It was used by getStatisticsOld().
     *
     * @param geom {@link Geometry} object holding the Geometry (e.g. LINESTRING...).
     * @return byte[] holding the byte representation of the {@link Geometry} object.
     * @throws IOException
     */
    @Deprecated
    private byte[] geometryToWKB(Geometry geom) throws IOException {
        WKBWriter wkbWriter = new WKBWriter();
        ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
        wkbWriter.write(geom, new OutputStreamOutStream(bytesStream));
        byte[] geomBytes = bytesStream.toByteArray();
        bytesStream.close();

        return geomBytes;
    }

    /**
     * Returns the driver name to be queried against the {@link heigit.ors.isochrones.statistics.StatisticsProviderFactory}.
     *
     * @return Returns the provider name in a {@link String} format.
     */
    @Override
    public String getName() {
        return "postgresql";
    }
}
