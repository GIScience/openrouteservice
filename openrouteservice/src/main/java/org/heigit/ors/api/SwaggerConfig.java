/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api;

import org.heigit.ors.config.AppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelKey;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ModelNamesRegistry;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ModelNamesRegistryFactoryPlugin;
import springfox.documentation.spi.service.contexts.ModelSpecificationRegistry;
import springfox.documentation.spring.web.paths.DefaultPathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.scanners.DefaultModelNamesRegistryFactory;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    String swagger_documentation_url = AppConfig.getGlobal().getParameter("info", "swagger_documentation_url");

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Openrouteservice")
                .description("This is the openrouteservice API documentation")
                .license("MIT")
                .licenseUrl("https://github.com/swagger-api/swagger-ui/blob/master/LICENSE")
                .contact(new Contact("", "", "enquiry@openrouteservice.heigit.org"))
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host(swagger_documentation_url)
                .pathProvider(new DefaultPathProvider())
                .directModelSubstitute(Duration.class, String.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.heigit.ors.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }
    @Bean
    @Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
    public ModelNamesRegistryFactoryPlugin swaggerFixReqResPostfix() {
        return new DefaultModelNamesRegistryFactory() {
            @Override
            public ModelNamesRegistry modelNamesRegistry(ModelSpecificationRegistry registry) {
                return super.modelNamesRegistry(hackModelSpecificationRegistry(registry));
            }

            private ModelSpecificationRegistry hackModelSpecificationRegistry(ModelSpecificationRegistry delegate) {
                return new ModelSpecificationRegistry() {
                    @Override
                    public ModelSpecification modelSpecificationFor(ModelKey key) {
                        return delegate.modelSpecificationFor(key);
                    }

                    @Override
                    public boolean hasRequestResponsePairs(ModelKey test) {
                        return false;
                    }

                    @Override
                    public Collection<ModelKey> modelsDifferingOnlyInValidationGroups(ModelKey test) {
                        return delegate.modelsDifferingOnlyInValidationGroups(test);
                    }

                    @Override
                    public Collection<ModelKey> modelsWithSameNameAndDifferentNamespace(ModelKey test) {
                        return delegate.modelsWithSameNameAndDifferentNamespace(test);
                    }

                    @Override
                    public Set<ModelKey> modelKeys() {
                        return delegate.modelKeys();
                    }
                };
            }

        };
    }
}
