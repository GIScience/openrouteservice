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
package org.heigit.ors.apitests.dynamicrouting;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.apitests.utils.CommonHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

abstract class AbstractContainerBaseTest extends ServiceTest {

    static final PostgreSQLContainer POSTGIS;

    static {
        POSTGIS = new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:17-3.6-alpine")
                .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("featurestore")
                .withUsername("ors")
                .withPassword("hello-postgres")
                .waitingFor(Wait.defaultWaitStrategy());
        POSTGIS.start();

        try {
            Connection connection = DriverManager.getConnection(
                    POSTGIS.getJdbcUrl(),
                    POSTGIS.getUsername(),
                    POSTGIS.getPassword()
            );
            System.setProperty("ors.engine.dynamic_data.store_url", POSTGIS.getJdbcUrl());
            System.setProperty("ors.engine.dynamic_data.store_user", POSTGIS.getUsername());
            System.setProperty("ors.engine.dynamic_data.store_pass", POSTGIS.getPassword());

            connection.createStatement().execute("""
                    CREATE TABLE features (
                        feature_id INTEGER NOT NULL,
                        dataset_key VARCHAR(255) NOT NULL,
                        value VARCHAR(20) NOT NULL,
                        PRIMARY KEY (feature_id, dataset_key)
                    );
                    """);
            connection.createStatement().execute("""
                    CREATE TABLE mappings (
                        feature_id INTEGER NOT NULL,
                        graph_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                        profile VARCHAR(20) NOT NULL,
                        edge_id INTEGER NOT NULL,
                        PRIMARY KEY (feature_id, graph_timestamp, profile, edge_id)
                    );
                    """);
            connection.createStatement().execute("""
                    CREATE VIEW feature_map AS
                    SELECT f.feature_id, f.dataset_key, m.graph_timestamp, m.profile, m.edge_id, f.value
                    FROM features f
                    JOIN mappings m ON f.feature_id = m.feature_id
                    """);
            connection.createStatement().execute("""
                    INSERT INTO features VALUES
                    (1, 'logie_borders','CLOSED'),
                    (2, 'logie_bridges','RESTRICTED'),
                    (3, 'logie_roads','RESTRICTED');
                    """);
            connection.createStatement().execute("""
                    INSERT INTO mappings VALUES
                    (1, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                    (2, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                    (3, '2024-09-08T20:21:00Z', 'driving-car', 3239),
                    (3, '2024-09-08T20:21:00Z', 'driving-car', 14409);
                    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

// Test order is needed to ensure that this test runs first because the
// database from which the dynamic data is fetched must be available before
// ORS is started. If other tests run first they may start ORS too early.
// (see also test/resources/junit-platform.properties)
@Order(0)
@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResultTest extends AbstractContainerBaseTest {

    public static final RestAssuredConfig JSON_CONFIG_DOUBLE_NUMBERS = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));

    public ResultTest() {
        addParameter("preference", "recommended");
        addParameter("carProfile", "driving-car");
    }

    public static Stream<Arguments> dynamicDataTestProvider() {
        return Stream.of(
                Arguments.of("logie_borders == CLOSED"),
                Arguments.of("logie_bridges == RESTRICTED"),
                Arguments.of("logie_roads == RESTRICTED")
        );
    }

    @ParameterizedTest
    @MethodSource("dynamicDataTestProvider")
    void testCustomProfileDynamicData(String condition) {
        JSONObject body = new JSONObject();
        JSONArray coordinates = new JSONArray();
        JSONArray coord1 = new JSONArray();

        coord1.put(8.692134);
        coord1.put(49.414866);
        coordinates.put(coord1);
        JSONArray coord2 = new JSONArray();

        coord2.put(8.688996);
        coord2.put(49.414351);
        coordinates.put(coord2);

        body.put("coordinates", coordinates);

        body.put("preference", getParameter("preference"));
        body.put("instructions", true);
        body.put("elevation", true);

        JSONObject customModel = new JSONObject();
        JSONObject priority = new JSONObject();
        priority.put("if", condition);
        priority.put("multiply_by", 0);
        customModel.put("priority", new JSONArray().put(priority));
        body.put("custom_model", customModel);

        given()
                .config(JSON_CONFIG_DOUBLE_NUMBERS)
                .headers(CommonHeaders.jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}")
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'routes' }", is(true))
                .body("routes[0].summary.distance", is(closeTo(661f, 6f))) // 248m without blocking surface
                .statusCode(200);
    }
}
