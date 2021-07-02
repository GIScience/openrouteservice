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
package org.heigit.ors.v2.services.apiSettings;

import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.heigit.ors.v2.services.config.AppConfig;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


@EndPointAnnotation(name = "status")
@VersionAnnotation(version = "v2")
public class CORSHeaderTest extends ServiceTest {
    List<String> configuredOrigins = AppConfig.Global().getStringList("ors.api_settings.allowed_origins");

    @Test
    public void testNoOrigin() {
        given()
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testAllowedOrigin() {
        for (String origin :
                configuredOrigins) {
            given()
                    .header("Origin", origin)
                    .get(getEndPointPath())
                    .then().log().ifValidationFails()
                    .assertThat()
                    .header("Access-Control-Allow-Origin", origin)
                    .statusCode(200);
        }
    }

    @Test
    public void testNotAllowedOrigin() {
        given()
                .header("Origin", "https://notAllowed.com")
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .statusCode(403);
    }
}
