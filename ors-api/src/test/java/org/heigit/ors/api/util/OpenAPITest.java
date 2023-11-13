package org.heigit.ors.api.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import jakarta.servlet.ServletContext;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.OpenAPIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("unittest")
class OpenAPITest {
    @Autowired
    private final EndpointsProperties endpointsProperties = new EndpointsProperties();

    @Autowired
    private final ServletContext servletContext = new MockServletContext();

    @Test
    void testGetOpenAPIDocs() {
        OpenAPIConfig oas_config = new OpenAPIConfig(endpointsProperties);
        OpenAPI api = oas_config.customOpenAPI(servletContext);
        assertEquals(SpecVersion.V30, api.getSpecVersion());
    }
}
