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
package org.heigit.ors.apitests.swagger;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.config.AppConfig;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "api-docs")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {
    String expected_swagger_documentation_url = AppConfig.getGlobal().getParameter("info", "swagger_documentation_url");

    @Test
    void testGetSwagger() {
        given()
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'swagger' }", is(true))
                .body("any { it.key == 'info' }", is(true))
                .body("any { it.key == 'host' }", is(true))
                .body("any { it.key == 'tags' }", is(true))
                .body("any { it.key == 'paths' }", is(true))
                .body("any { it.key == 'definitions' }", is(true))
                .body("swagger", hasToString("2.0"))
                .body("host", hasToString(expected_swagger_documentation_url))
                .statusCode(200);
    }
}
