package org.heigit.ors.api.util;

import org.heigit.ors.api.SwaggerConfig;
import org.junit.Assert;
import org.junit.Test;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


public class SwaggerTest {
    @Test
    public void testGetSwaggerDocs() {
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");
        SwaggerConfig swagger_config = new SwaggerConfig();
        Docket api = swagger_config.api();
        Assert.assertEquals(DocumentationType.SWAGGER_2, api.getDocumentationType());
        Assert.assertTrue(api.isEnabled());
    }
}
