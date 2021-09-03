/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.isochrones.statistics.postgresql;

import com.graphhopper.util.Helper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.isochrones.Isochrone;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.apache.log4j.Logger;
import org.heigit.ors.isochrones.statistics.StatisticsProvider;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * This class handles the population statistic queries. It generates internal api calls to SQL statements that are
 * queried against the set PostgreSQL-Server holding the population data.
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class PostgresSQLStatisticsProvider implements StatisticsProvider {
    private static final Logger LOGGER = Logger.getLogger(PostgresSQLStatisticsProvider.class.getName());

    private static final String PARAM_KEY_PASS = "password";

    private String tableName = null;
    private String geomColumn = null;
    private HikariDataSource dataSource;
    /**
     * This function initializes the connection to the server according to the settings in the ors-config.json.
     * The connection is established using a {@link HikariDataSource} object with the configuration data from the ors-config.json.
     *
     * @param parameters {@link Map} holding the server configuration data from the ors-config.json.
     * @throws Exception
     */
    @Override
    public void init(Map<String, Object> parameters) throws Exception {
        dataSource = null;
        tableName = null;
        geomColumn = null;

        String value = (String) parameters.get("table_name");
        if (Helper.isEmpty(value))
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "'table_name' parameter can not be null or empty.");
        else
            tableName = value;

        value = (String) parameters.get("geometry_column");
        if (Helper.isEmpty(value))
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "'geometry_column' parameter can not be null or empty.");
        else
            geomColumn = value;


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
        if (parameters.containsKey(PARAM_KEY_PASS))
            config.addDataSourceProperty(PARAM_KEY_PASS, parameters.get(PARAM_KEY_PASS));
        config.addDataSourceProperty("serverName", parameters.get("host"));
        config.addDataSourceProperty("portNumber", parameters.get("port"));
        if (parameters.containsKey("max_pool_size"))
            config.setMaximumPoolSize((Integer) parameters.get("max_pool_size"));
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
    }

    /**
     * This function closes the {@link HikariDataSource} connection.
     *
     * @throws Exception
     */
    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
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
        try {
            String sql = null;
            for (String property : properties) {
                String polyGeom = isochrone.getGeometry().toText();
                if ("total_pop".equals(property)) {
                    sql = "SELECT ROUND(SUM((ST_SummaryStats(ST_Clip(" + geomColumn + ", poly))).sum)) AS total_pop FROM " + tableName + ", ST_Transform(ST_GeomFromText('" + polyGeom + "', 4326), 954009) AS poly WHERE ST_Intersects(poly, " + geomColumn + ") GROUP BY poly;";
                }
            }
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "Unable to retrieve data from the data source.");
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

    /**
     * Returns the driver name to be queried against the {@link org.heigit.ors.isochrones.statistics.StatisticsProviderFactory}.
     *
     * @return Returns the provider name in a {@link String} format.
     */
    @Override
    public String getName() {
        return "postgresql";
    }
}
