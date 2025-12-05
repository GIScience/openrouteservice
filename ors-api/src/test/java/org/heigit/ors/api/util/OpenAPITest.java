package org.heigit.ors.api.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import jakarta.servlet.ServletContext;
import org.heigit.ors.api.config.EndpointsProperties;
import org.heigit.ors.api.config.OpenAPIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("unittest")
class OpenAPITest {
    private final EndpointsProperties endpointsProperties;

    @Autowired
    public OpenAPITest(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    private final ServletContext servletContext = new MockServletContext();

    @Test
    void testGetOpenAPIDocs() {
        OpenAPIConfig oasConfig = new OpenAPIConfig(endpointsProperties);
        OpenAPI api = oasConfig.customOpenAPI(servletContext);
        assertEquals(SpecVersion.V30, api.getSpecVersion());
    }
}
