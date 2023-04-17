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

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.heigit.ors.api.converters.APIRequestProfileConverter;
import org.heigit.ors.api.converters.APIRequestSingleCoordinateConverter;
import org.heigit.ors.config.AppConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new APIRequestSingleCoordinateConverter());
        registry.addConverter(new APIRequestProfileConverter());
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
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                List<String> allowedMethods = List.of("GET", "POST", "HEAD", "OPTIONS");

                List<String> allowedOrigins = AppConfig.getGlobal().getStringList("ors.api_settings.cors.allowed.origins");
                if (allowedOrigins.isEmpty()) allowedOrigins.add("*");

                List<String> allowedHeaders = AppConfig.getGlobal().getStringList("ors.api_settings.cors.allowed.headers");
                if (allowedHeaders.isEmpty()) {
                    allowedHeaders = List.of("Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
                }

                List<String> exposedHeaders = AppConfig.getGlobal().getStringList("ors.api_settings.cors.exposed.headers");
                if (exposedHeaders.isEmpty()) {
                    exposedHeaders = List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials");
                }

                long maxAge = (long) AppConfig.getGlobal().getDouble("api_settings.cors.preflight_max_age");
                if (maxAge == 0) {
                    maxAge = 600;
                }
                registry.addMapping("/**")
                        .allowedMethods(allowedMethods.toArray(new String[0]))
                        .allowCredentials(false)
                        .allowedOrigins(allowedOrigins.toArray(new String[0]))
                        .allowedHeaders(allowedHeaders.toArray(new String[0]))
                        .exposedHeaders(exposedHeaders.toArray(new String[0]))
                        .maxAge(maxAge);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.registerModule(new JtsModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
