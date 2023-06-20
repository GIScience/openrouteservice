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

import org.heigit.ors.api.InfoProperties;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.springdoc.core.SpringDocConfigProperties.ApiDocs.OpenApiVersion.OPENAPI_3_1;


@EndPointAnnotation(name = "api-docs")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {

    @Autowired
    InfoProperties infoProperties;

    //    The outcommented parts should be introduced once the swagger-parser package uses the latest snakeyaml version.
//    SwaggerParseResult result;
//    OpenAPI openAPI;
    String openAPIVersion = String.valueOf(OPENAPI_3_1.getVersion());

//    @BeforeEach
//    void setUp() {
//        Response response = RestAssured.given().get(getEndPointPath());
//        // parse a openapi description from the petstore and get the result
//        result = new OpenAPIParser().readContents(response.getBody().asString(), null, null);
//        // the parsed POJO
//        openAPI = result.getOpenAPI();
//    }


    @Test
    void testGetSwagger() {
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
                .body("servers", hasSize(1))
                .body("servers[0].url", hasToString(infoProperties.getSwaggerDocumentationUrl()))
                .statusCode(200);
    }

//    @Test
//    void testSwaggerSpecValidationErrors() {
//        if (result.getMessages() != null) {
//            result.getMessages().forEach(System.err::println);
//        }
//        // Assure that no openapi spec validation errors exist.
//        assertTrue(result.getMessages().isEmpty());
//        assertTrue(result.isOpenapi31());
//    }

//    @Test
//    void testSwaggerVersion() {
//        assertEquals(openAPIVersion, openAPI.getOpenapi());
//    }

//    @Test
//    void testSwaggerInfo() {
//        assertNotNull(openAPI.getInfo());
//        assertEquals("Openrouteservice", openAPI.getInfo().getTitle());
//        assertEquals("v2", openAPI.getInfo().getVersion());
//    }

//    @Test
//    void testSwaggerTags() {
//        assertNotNull(openAPI.getTags());
//        assertEquals(7, openAPI.getTags().size());
//    }

//    @Test
//    void testSwaggerPaths() {
//        assertNotNull(openAPI.getPaths());
//        assertEquals(14, openAPI.getPaths().size());
//    }

//    @Test
//    void testSwaggerComponents() {
//        assertNotNull(openAPI.getComponents());
//        assertNotNull(openAPI.getComponents().getSchemas());
//        assertEquals(43, openAPI.getComponents().getSchemas().size());
//    }
}
