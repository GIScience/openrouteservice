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

package org.heigit.ors.api.config;

import com.graphhopper.jackson.geojson.JtsModule;
import org.codehaus.commons.nullanalysis.NotNull;
import org.heigit.ors.api.converters.APIRequestProfileConverter;
import org.heigit.ors.api.converters.APIRequestSingleCoordinateConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new APIRequestSingleCoordinateConverter());
        registry.addConverter(new APIRequestProfileConverter());
    }

    /**
     * Spring no longer auto-registers a JAXB message converter once a Jackson XML converter is
     * present on the classpath - which, since GraphHopper's own (unused-by-ORS) GPX support pulls
     * in Jackson's XML dataformat module transitively, is now always the case here. Our own
     * GPXRouteResponse is JAXB-annotated, not Jackson-XML-annotated, so it needs an explicit,
     * higher-priority JAXB converter to keep serializing application/gpx+xml correctly.
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.withXmlConverter(new Jaxb2RootElementHttpMessageConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * Adds Access Control settings specified in the ors config.
     * Restricts origins and headers to the allowed ones.
     * Defines headers to expose and the expiry of the preflight result cache.
     * Available HTTP Methods are not configurable and default to GET, POST, HEAD and OPTIONS globally.
     * Credentials are turned off.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer(CorsProperties corsProps) {
        final CorsProperties corsProperties = corsProps;

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                String[] allowedMethods = new String[]{"GET", "POST", "HEAD", "OPTIONS"};
                String[] exposedHeaders = new String[]{"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"};

                registry.addMapping("/**")
                        .allowedMethods(allowedMethods)
                        .allowCredentials(false)
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                        .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                        .exposedHeaders(exposedHeaders)
                        .maxAge(corsProperties.getPreflightMaxAge());
            }
        };
    }

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(new JtsModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
