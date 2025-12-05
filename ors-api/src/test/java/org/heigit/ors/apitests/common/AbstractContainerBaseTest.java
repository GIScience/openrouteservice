package org.heigit.ors.apitests.common;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractContainerBaseTest extends ServiceTest {

    static final PostgreSQLContainer POSTGIS;
    static {
        POSTGIS = new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:17-3.6-alpine")
                .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("featurestore")
                .withUsername("ors")
                .withPassword("hello-postgres")
                .waitingFor(Wait.defaultWaitStrategy()
                );
        POSTGIS.start();

        System.setProperty("ors.engine.dynamic_data.store_url", POSTGIS.getJdbcUrl());
        System.setProperty("ors.engine.dynamic_data.store_user", POSTGIS.getUsername());
        System.setProperty("ors.engine.dynamic_data.store_pass", POSTGIS.getPassword());
        // We also need the DynamicDataService's static method getGraphDate to return a fixed date for testing
        System.setProperty("GRAPH_DATE_OVERRIDE", "2024-09-08T20:21:00Z");

        try (Connection connection = DriverManager.getConnection(POSTGIS.getJdbcUrl(), POSTGIS.getUsername(), POSTGIS.getPassword())) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""                      
                        CREATE TABLE features (
                            feature_id INTEGER NOT NULL,
                            dataset_key VARCHAR(255) NOT NULL,
                            value VARCHAR(20) NOT NULL,
                            geom GEOMETRY(Geometry, 4326) NOT NULL,
                            geojson JSONB,
                            timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            deleted BOOLEAN DEFAULT FALSE,
                            PRIMARY KEY (feature_id, dataset_key)
                        );
                        """);
                stmt.executeUpdate("""        
                        CREATE TABLE mappings (
                            feature_id INTEGER NOT NULL,
                            graph_timestamp TIMESTAMP NOT NULL,
                            profile VARCHAR(20) NOT NULL,
                            edge_id INTEGER NOT NULL,
                            timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            deleted BOOLEAN DEFAULT FALSE,
                            PRIMARY KEY (feature_id, graph_timestamp, profile, edge_id)
                        );
                        """);
                stmt.executeUpdate("""
                        CREATE VIEW feature_map AS
                        SELECT f.feature_id, f.dataset_key, m.graph_timestamp, m.profile, m.edge_id, f.value, m.deleted, m.timestamp
                        FROM features f
                        JOIN mappings m ON f.feature_id = m.feature_id
                        """);
                stmt.executeUpdate("""
                        INSERT INTO features VALUES
                        (1, 'logie_borders', 'CLOSED', 'POINT(0 0)'),
                        (2, 'logie_bridges', 'RESTRICTED', 'POINT(0 0)'),
                        (3, 'logie_roads', 'RESTRICTED', 'POINT(0 0)')
                        """);
                stmt.executeUpdate("""
                        INSERT INTO mappings VALUES
                        (1, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                        (2, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                        (3, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                        (3, '2024-09-08T20:21:00Z', 'driving-car', 14409);
                        """);
                stmt.executeUpdate("""
                        CREATE TABLE stats_import (
                            dataset_key VARCHAR(255) NOT NULL,
                            last_import TIMESTAMP WITHOUT TIME ZONE,
                            next_import TIMESTAMP WITHOUT TIME ZONE,
                            count_imported INTEGER,
                            count_features INTEGER,
                            count_unmatched INTEGER,
                            PRIMARY KEY (dataset_key)
                        );
                        """);
                stmt.executeUpdate("""
                        INSERT INTO stats_import VALUES
                        ('logie_borders', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z', 0, 1, 0),
                        ('logie_bridges', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z', 0, 1, 0),
                        ('logie_roads', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z', 1, 1, 0)
                        """);
                stmt.executeUpdate("""
                        CREATE TABLE stats_match (
                            dataset_key VARCHAR(255) NOT NULL,
                            profile VARCHAR(255) NOT NULL,
                            last_match  TIMESTAMP WITHOUT TIME ZONE,
                            next_match  TIMESTAMP WITHOUT TIME ZONE,
                            PRIMARY KEY (dataset_key, profile)
                        );
                        """);
                stmt.executeUpdate("""
                        INSERT INTO stats_match VALUES
                        ('logie_borders', 'driving-car', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z'),
                        ('logie_bridges', 'driving-car', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z'),
                        ('logie_roads', 'driving-car', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z'),
                        ('logie_borders', 'driving-hgv', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z'),
                        ('logie_bridges', 'driving-hgv', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z'),
                        ('logie_roads', 'driving-hgv', '2024-09-08T20:21:00Z', '2024-09-08T20:22:00Z')
                        """);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
