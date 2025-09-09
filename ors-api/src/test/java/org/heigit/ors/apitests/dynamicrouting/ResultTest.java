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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.*;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "directions")
@VersionAnnotation(version = "v2")
@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResultTest extends ServiceTest {

    public static final RestAssuredConfig JSON_CONFIG_DOUBLE_NUMBERS = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE));

    public ResultTest() {
        JSONArray coordsShort = new JSONArray();
        JSONArray coord1 = new JSONArray();
        coord1.put(8.678613);
        coord1.put(49.411721);
        coordsShort.put(coord1);
        JSONArray coord2 = new JSONArray();
        coord2.put(8.687782);
        coord2.put(49.424597);
        coordsShort.put(coord2);
        addParameter("coordinatesShort", coordsShort);

        JSONArray coordsLong = new JSONArray();
        JSONArray coordLong1 = new JSONArray();

        coordLong1.put(8.678613);
        coordLong1.put(49.411721);
        coordsLong.put(coordLong1);
        JSONArray coordLong2 = new JSONArray();
        coordLong2.put(8.714733);
        coordLong2.put(49.393267);
        coordsLong.put(coordLong2);
        JSONArray coordLong3 = new JSONArray();
        coordLong3.put(8.687782);
        coordLong3.put(49.424597);
        coordsLong.put(coordLong3);
        addParameter("coordinatesLong", coordsLong);

        JSONArray coordsFoot = new JSONArray();
        JSONArray coordFoot1 = new JSONArray();
        coordFoot1.put(8.676023);
        coordFoot1.put(49.416809);
        coordsFoot.put(coordFoot1);
        JSONArray coordFoot2 = new JSONArray();
        coordFoot2.put(8.696837);
        coordFoot2.put(49.411839);
        coordsFoot.put(coordFoot2);

        addParameter("coordinatesWalking", coordsFoot);

        JSONArray coordsFootBridge = new JSONArray();
        JSONArray coordFootBridge1 = new JSONArray();
        coordFootBridge1.put(8.692013);
        coordFootBridge1.put(49.415036);
        coordsFootBridge.put(coordFootBridge1);
        JSONArray coordFootBridge2 = new JSONArray();
        coordFootBridge2.put(8.692765);
        coordFootBridge2.put(49.410540);
        coordsFootBridge.put(coordFootBridge2);

        addParameter("coordinatesWalkingBridge", coordsFootBridge);

        JSONArray coordinatesPT = new JSONArray();
        JSONArray coordinatesPTFlipped = new JSONArray();
        JSONArray coordPT1 = new JSONArray();
        coordPT1.put(8.704433);
        coordPT1.put(49.403378);
        JSONArray coordPT2 = new JSONArray();
        coordPT2.put(8.676101);
        coordPT2.put(49.408324); //
        coordinatesPT.put(coordPT1);
        coordinatesPT.put(coordPT2);
        coordinatesPTFlipped.put(coordPT2);
        coordinatesPTFlipped.put(coordPT1);

        JSONArray coordinatesPT2 = new JSONArray();
        JSONArray coordPT3 = new JSONArray();
        coordPT3.put(8.758935);
        coordPT3.put(49.337371);
        JSONArray coordPT4 = new JSONArray();
        coordPT4.put(8.771123);
        coordPT4.put(49.511863);
        coordinatesPT2.put(coordPT3);
        coordinatesPT2.put(coordPT4);

        addParameter("coordinatesPT", coordinatesPT);
        addParameter("coordinatesPTFlipped", coordinatesPTFlipped);
        addParameter("coordinatesPT2", coordinatesPT2);

        JSONArray coordinatesCustom1 = new JSONArray();
        JSONArray coordinateCustom1 = new JSONArray();
        coordinateCustom1.put(8.689885139465334);
        coordinateCustom1.put(49.40667302975234);
        JSONArray coordinateCustom2 = new JSONArray();
        coordinateCustom2.put(8.7184506654739);
        coordinateCustom2.put(49.41430278032613);
        coordinatesCustom1.put(coordinateCustom1);
        coordinatesCustom1.put(coordinateCustom2);
        addParameter("coordinatesCustom1", coordinatesCustom1);

        JSONArray coordinatesCustom2 = new JSONArray();
        JSONArray coordinateCustom3 = new JSONArray();
        coordinateCustom3.put(8.669232130050661);
        coordinateCustom3.put(49.40850204416985);
        JSONArray coordinateCustom4 = new JSONArray();
        coordinateCustom4.put(8.625125885009767);
        coordinateCustom4.put(49.37098664229148);
        coordinatesCustom2.put(coordinateCustom3);
        coordinatesCustom2.put(coordinateCustom4);
        addParameter("coordinatesCustom2", coordinatesCustom2);

        JSONArray coordinatesCustom3 = new JSONArray();
        JSONArray coordinateCustom5 = new JSONArray();
        coordinateCustom5.put(8.687862753868105);
        coordinateCustom5.put(49.41309522267728);
        JSONArray coordinateCustom6 = new JSONArray();
        coordinateCustom6.put(8.691891431808473);
        coordinateCustom6.put(49.41331858818114);
        coordinatesCustom3.put(coordinateCustom5);
        coordinatesCustom3.put(coordinateCustom6);
        addParameter("coordinatesCustom3", coordinatesCustom3);

        JSONArray unreachableCoords = new JSONArray();
        JSONArray unreachableCoord1 = new JSONArray();
        unreachableCoord1.put(6.929281);
        unreachableCoord1.put(45.707362);
        unreachableCoords.put(unreachableCoord1);
        JSONArray unreachableCoord2 = new JSONArray();
        unreachableCoord2.put(6.92281);
        unreachableCoord2.put(45.507362);
        unreachableCoords.put(unreachableCoord2);
        addParameter("unreachableCoords", unreachableCoords);


        JSONArray extraInfo = new JSONArray();
        extraInfo.put("surface");
        extraInfo.put("suitability");
        extraInfo.put("steepness");
        extraInfo.put("countryinfo");
        addParameter("extra_info", extraInfo);

        addParameter("preference", "recommended");
        addParameter("bikeProfile", "cycling-regular");
        addParameter("carProfile", "driving-car");
        addParameter("footProfile", "foot-walking");
        addParameter("hikeProfile", "foot-hiking");
        addParameter("ptProfile", "public-transport");
        addParameter("carCustomProfile", "driving-car-no-preparations");
    }

   @BeforeAll
    void initializeDatabase() {
        var image = DockerImageName.parse("postgis/postgis:17-3.6-alpine")
                .asCompatibleSubstituteFor("postgres");
        var postgis = new PostgreSQLContainer<>(image)
                .withDatabaseName("featurestore")
                .withUsername("ors")
                .withPassword("hello-postgres");
        postgis.start();

        try {
            Connection connection = DriverManager.getConnection(
                    postgis.getJdbcUrl(),
                    postgis.getUsername(),
                    postgis.getPassword()
            );
            connection.createStatement().execute(
                    """
                    CREATE TABLE features (
                    feature_id SERIAL PRIMARY KEY,
                    dataset_key VARCHAR(255) NOT NULL,
                    value VARCHAR(20),
                    );
                    """
            );
            connection.createStatement().execute(
                    """
                    CREATE TABLE mappings (
                    feature_id INTEGER,
                    graph_time_stamp TIMESTAMP,
                    profile VARCHAR(20),
                    edge_id INTEGER,
                    );
                    """
            );
            connection.createStatement().execute(
                    """
                       INSERT INTO features VALUES
                       (1, "logie_borders","CLOSED"),
                       (2, "logie_bridges","RESTRICTED"),
                       (3, "logie_roads","RESTRICTED");
                       """
            );
            connection.createStatement().execute(
                    """
                       INSERT INTO mappings VALUES
                       (1, 2024-09-08T20:21:00Z, "driving-car", 3239),
                       (2, 2024-09-08T20:21:00Z, "driving-car", 3239),
                       (3, 2024-09-08T20:21:00Z, "driving-car", 3239);
                       (3, 2024-09-08T20:21:00Z, "driving-car", 14409);
                       """
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void testCustomProfileDynamicBorder() {
        final String borderPoint = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "value": "CLOSED",
                  },
                  "geometry": {
                    "coordinates": [
                      8.691470,
                      49.414642
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
        JSONObject borderRequest = new JSONObject().put("key", "logie_borders").put("features", new JSONObject(borderPoint));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(borderRequest.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath("match") + "/{profile}")
                .then()
                .log().ifValidationFails()
                .statusCode(200);

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
        priority.put("if", "logie_borders == CLOSED");
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

    @Test
    void testCustomProfileDynamicBridge() {
        final String bridgePoint = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "value": "RESTRICTED",
                  },
                  "geometry": {
                    "coordinates": [
                      8.691470,
                      49.414642
                    ],
                    "type": "Point"
                  }
                }
              ]
            }
            """;
        JSONObject borderRequest = new JSONObject().put("key", "logie_bridges").put("features", new JSONObject(bridgePoint));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(borderRequest.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath("match") + "/{profile}")
                .then()
                .log().ifValidationFails()
                .statusCode(200);

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
        priority.put("if", "logie_bridges == RESTRICTED");
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

    @Test
    void testCustomProfileDynamicRoad() {
        final String bridgePoint = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "value": "RESTRICTED"
                  },
                  "geometry": {
                    "coordinates": [
                      [
                        8.691958,
                        49.414701
                      ],
                      [
                        8.690789,
                        49.414554
                      ]
                    ],
                    "type": "LineString"
                  }
                }
              ]
            }
            """;
        JSONObject borderRequest = new JSONObject().put("key", "logie_bridges").put("features", new JSONObject(bridgePoint));
        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("carProfile"))
                .body(borderRequest.toString())
                .when()
                .log().ifValidationFails()
                .post(getEndPointPath("match") + "/{profile}")
                .then()
                .log().ifValidationFails()
                .statusCode(200);

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
        priority.put("if", "logie_bridges == RESTRICTED");
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
