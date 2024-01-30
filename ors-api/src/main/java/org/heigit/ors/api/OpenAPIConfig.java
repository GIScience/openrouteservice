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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import jakarta.servlet.ServletContext;
import org.heigit.ors.api.util.AppConfigMigration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.heigit.ors.api.util.AppInfo.VERSION;


@Configuration
public class OpenAPIConfig {

    private static final String SERVICE_NAME = "Openrouteservice";

    @Value("${server.port}")
    private int serverPort;
    final
    EndpointsProperties endpointsProperties;

    public OpenAPIConfig(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = AppConfigMigration.overrideEndpointsProperties(endpointsProperties);
    }


    @Bean
    public OpenAPI customOpenAPI(ServletContext servletContext) {
        return new OpenAPI()
                .servers(generateServers(servletContext))
                .info(apiInfo());
    }

    private List<Server> generateServers(ServletContext servletContext) {
        ArrayList<Server> listOfServers = new ArrayList<>();
        // live API server
        listOfServers.add(new Server().url("https://api.openrouteservice.org").description("Openrouteservice API"));

        // dynamic local instances
        ServerVariable ports = new ServerVariable();
        ports.setDescription("Port the local openrouteservice instance runs on");
        ports.setDefault(String.valueOf(serverPort));
        ServerVariable basePath = new ServerVariable();
        basePath.setDescription("Base path of the local openrouteservice instance");
        basePath.setDefault(servletContext.getContextPath());

        // in case we or someone get rid of the /ors context
        ServerVariables variables = new ServerVariables();
        variables.addServerVariable("port", ports);
        variables.addServerVariable("basePath", basePath);

        Server devServer = new Server();
        devServer.url("http://localhost:{port}{basePath}");
        devServer.description("Development Server");
        devServer.setVariables(variables);

        listOfServers.add(devServer);

        if (endpointsProperties.getSwaggerDocumentationUrl() != null) {
            Server customApi = new Server().url(endpointsProperties.getSwaggerDocumentationUrl()).description("Custom server url");
            listOfServers.add(customApi);
        }
        return listOfServers;
    }

    private Info apiInfo() {
        return new Info()
                .title(SERVICE_NAME)
                .description("This is the openrouteservice API documentation for ORS Core-Version " + VERSION + ". Documentations for [older Core-Versions](https://github.com/GIScience/openrouteservice-docs/releases) can be rendered with the [Swagger-Editor](https://editor-next.swagger.io/).")
                .version("v2")
                .contact(apiContact())
                .license(apiLicence())
                .extensions(Map.of("x-ors-version", VERSION));
    }

    private License apiLicence() {
        return new License()
                .name("GNU General Public License v3.0")
                .url("https://github.com/GIScience/openrouteservice/blob/main/LICENSE");
    }

    private Contact apiContact() {
        return new Contact()
                .name(SERVICE_NAME)
                .email("support@smartmobility.heigit.org")
                .url("https://github.com/GIScience/openrouteservice");
    }
}

