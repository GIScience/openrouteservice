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
import org.heigit.ors.apitests.common.AbstractContainerBaseTest;
import org.heigit.ors.apitests.common.EndPointAnnotation;
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
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

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
