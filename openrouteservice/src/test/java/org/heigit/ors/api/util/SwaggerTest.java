package org.heigit.ors.api.util;

import org.heigit.ors.api.SwaggerConfig;
import org.junit.jupiter.api.Test;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SwaggerTest {
    @Test
    void testGetSwaggerDocs() {
        SwaggerConfig swagger_config = new SwaggerConfig();
        Docket api = swagger_config.api();
        assertEquals(DocumentationType.SWAGGER_2, api.getDocumentationType());
        assertTrue(api.isEnabled());
    }
}
