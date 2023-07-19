//package org.heigit.ors.api.util;
//
//import org.heigit.ors.api.OpenApiConfiguration;
//import org.junit.jupiter.api.Test;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//
//class OpenAPITest {
//    @Test
//    void testGetOpenAPIDocs() {
//        OpenAPIConfig oas_config = new OpenAPIConfig();
//        Docket api = oas_config.api();
//        assertEquals(DocumentationType.SWAGGER_2, api.getDocumentationType());
//        assertTrue(api.isEnabled());
//    }
//}
