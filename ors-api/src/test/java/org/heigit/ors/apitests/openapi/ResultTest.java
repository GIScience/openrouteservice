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
package org.heigit.ors.apitests.openapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springdoc.core.properties.SpringDocConfigProperties.ApiDocs.OpenApiVersion.OPENAPI_3_0;


@EndPointAnnotation(name = "api-docs")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {

    SwaggerParseResult result;
    OpenAPI openAPI;
    String openAPIVersion = String.valueOf(OPENAPI_3_0.getVersion());

    @BeforeEach
    void setUp() {
        Response response = RestAssured.given().get(getEndPointPath());
        result = new OpenAPIParser().readContents(response.getBody().asString(), null, null);
        openAPI = result.getOpenAPI();
    }


    @Test
    void testGetOpenAPI() {
        given()
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .body("openapi", hasToString(openAPIVersion))
                .body("any { it.key == 'openapi' }", is(true))
                .body("any { it.key == 'info' }", is(true))
                .body("any { it.key == 'servers' }", is(true))
                .body("any { it.key == 'tags' }", is(true))
                .body("any { it.key == 'paths' }", is(true))
                .body("any { it.key == 'components' }", is(true))
                .body("info", hasKey("x-ors-version"))
                .body("servers", hasSize(2))
                .body("servers[0].url", hasToString("https://api.openrouteservice.org"))
                .body("servers[1].url", hasToString("http://localhost:{port}{basePath}"))
                .body("servers[1].variables.port", hasKey("description"))
                .body("servers[1].variables.port", hasKey("default"))
                .body("servers[1].variables.basePath.default", hasToString("/ors"))
                .statusCode(200);
    }

    /**
     * Assure that no openapi spec validation errors exist.
     * If you encounter an error here, set a breakpoint in:
     * {@link io.swagger.v3.parser.util.OpenAPIDeserializer.ParseResult} currently around Line 4217 and following
     * depending on the error type and check the `value` which hints the problem location.
     * Or go up the call stack and check the `node` to get the exact location, because the provided location in
     * the error only goes up to the root schema e.g. components.schemas.MatrixRequest
     */
    @Test
    void testOpenAPISpecValidationErrors() {
        if (result.getMessages() != null) {
            result.getMessages().forEach(System.err::println);
        }
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    void testOpenAPIVersion() {
        assertEquals(openAPIVersion, openAPI.getOpenapi());
    }

    @Test
    void testOpenAPIInfo() {
        assertNotNull(openAPI.getInfo());
        assertEquals("Openrouteservice", openAPI.getInfo().getTitle());
        assertEquals("v2", openAPI.getInfo().getVersion());
    }

    @Test
    void testOpenAPIProperties() {
        assertNotNull(openAPI.getTags());
        assertTrue(0 < openAPI.getTags().size());

        assertNotNull(openAPI.getPaths());
        assertTrue(0 < openAPI.getPaths().size());

        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSchemas());
        assertTrue(0 < openAPI.getComponents().getSchemas().size());
    }
}
