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
package org.heigit.ors.apitests.cors;

import io.restassured.response.Response;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;


@EndPointAnnotation(name = "status")
@VersionAnnotation(version = "v2")
class CORSSettingsTest extends ServiceTest {
    List<String> allowedOrigins = AppConfig.getGlobal().getStringList("ors.api_settings.cors.allowed.origins");
    List<String> allowedHeaders = AppConfig.getGlobal().getStringList("ors.api_settings.cors.allowed.headers");

    @Test
    @DisplayName("Requests without 'Origin' header pass with 200 response")
    void testNoOrigin() {
        given()
                .options(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat().statusCode(200);
    }

    @DisplayName("No CORS Issue for all default base endpoints")
    @ParameterizedTest(name = "No Issue for {0} base endpoint")
    @ValueSource(strings = {"directions", "isochrones", "matrix"})
    void testNoCORSIssueForDefaultEndpoints(String endpoint) {
        given()
                .header("Origin", allowedOrigins.get(0))
                .header("Access-Control-Request-Method", "POST")
                .options(getEndPointPath() + endpoint + "/driving-car")
                .then().log().ifValidationFails()
                .assertThat().statusCode(200);
    }


    @DisplayName("Unsupported methods are rejected")
    @ParameterizedTest(name = "Method {0} is rejected")
    @ValueSource(strings = {"PUT", "DELETE", "TRACE", "PATCH"})
    void testUnsupportedMethods(String method) {
        Response r = given()
                .header("Origin", allowedOrigins.get(0))
                .header("Access-Control-Request-Method", method)
                .options(getEndPointPath());
        String msg = r.body().asString();
        r.then().log().ifValidationFails()
                .assertThat().statusCode(403);
        assertEquals("Invalid CORS request", msg);
    }

    @DisplayName("Supported methods are allowed")
    @ParameterizedTest(name = "Method {0} is allowed")
    @ValueSource(strings = {"GET", "POST", "HEAD", "OPTIONS"})
    void testAllowedMethods(String method) {
        given()
                .header("Origin", allowedOrigins.get(0))
                .header("Access-Control-Request-Method", method)
                .options(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat().statusCode(200);
    }

    @Test
    @DisplayName("Defined origins are allowed")
    void testAllowedOrigin() {
        for (String origin : allowedOrigins) {
            // OPTIONS
            given()
                    .header("Origin", origin)
                    .header("Access-Control-Request-Method", "GET")
                    .options(getEndPointPath())
                    .then().log().ifValidationFails()
                    .assertThat().header("Access-Control-Allow-Origin", origin)
                    .statusCode(200);
            // GET
            given()
                    .header("Origin", origin)
                    .get(getEndPointPath())
                    .then().log().ifValidationFails()
                    .assertThat().header("Access-Control-Allow-Origin", origin)
                    .statusCode(200);
        }
    }

    @Test
    @DisplayName("Undefined origins are rejected")
    void testNotAllowedOrigin() {
        // OPTIONS
        Response rOptions = given()
                .header("Origin", "https://notAllowed.com")
                .header("Access-Control-Request-Method", "GET")
                .options(getEndPointPath());
        String msgOptions = rOptions.body().asString();
        rOptions.then().log().ifValidationFails()
                .assertThat().statusCode(403);
        assertEquals("Invalid CORS request", msgOptions);
        // GET
        Response rGet = given().header("Origin", "https://notAllowed.com")
                .get(getEndPointPath());
        String msgGet = rGet.body().asString();
        rGet.then().log().ifValidationFails()
                .assertThat().statusCode(403);
        assertEquals("Invalid CORS request", msgGet);
    }

    @Test
    @DisplayName("MaxAge of OPTIONS request is set correctly")
    void testMaxAge() {
        long preflightMaxAge = (long) AppConfig.getGlobal().getDouble("api_settings.cors.preflight_max_age");
        given()
                .header("Origin", allowedOrigins.get(0))
                .header("Access-Control-Request-Method", "POST")  // needed to return maxAge header
                .options(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat().statusCode(200)
                .header("Access-Control-Max-Age", String.valueOf(preflightMaxAge));
    }

    @Test
    @DisplayName("Exposed Headers are provided in OPTIONS response")
    void testExposedHeaders() {
        List<String> exposedHeaders = AppConfig.getGlobal().getStringList("ors.api_settings.cors.exposed.headers");
        Response res = given()
                .header("Origin", allowedOrigins.get(0))
                .options(getEndPointPath());
        res.then().log().ifValidationFails()
                .assertThat().statusCode(200);
        String responseExposedHeaders = res.getHeaders().getValue("Access-Control-Expose-Headers");
        for (String header :
                exposedHeaders) {
            assertThat(responseExposedHeaders, containsString(header));
        }
    }

    @DisplayName("Defined headers are allowed")
    @ParameterizedTest(name = "Header {0} with value {1} can be passed")
    @CsvSource({
            "Content-Type, application/geo+json",
            "X-Tested-With, test",
            "accept, */*",
            "Access-Control-Request-Headers, Location",
            "Authorization, test_token"})
    void testAllowedHeaders(String header, String value) {
        assertThat(allowedHeaders, hasItems(header));
        given()
                .header(header, value)
                .header("Origin", allowedOrigins.get(0))
                .options(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat().statusCode(200);
    }
}
