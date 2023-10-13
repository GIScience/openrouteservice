package org.heigit.ors.api;

import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@Configuration
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    private static final String ORS_HOME_ENV = "ORS_HOME";
    private static final String ORS_LOG_LOCATION_ENV = "ORS_LOG_LOCATION";
    public static final String LOG_PATH_SYS = "logPath";

    private static final Class<Application> applicationClass = Application.class;

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    private static void setSystemProperties() {
        String orsHome = System.getenv(ORS_HOME_ENV);
        if (!Strings.isNullOrEmpty(orsHome)) {
            if (Strings.isNullOrEmpty(System.getenv("ORS_CONFIG")))
                System.setProperty("ors_config", FilenameUtils.concat(orsHome, "config/ors-config.yml"));
            System.setProperty(LOG_PATH_SYS, FilenameUtils.concat(orsHome, "logs/"));
        } else {
            System.setProperty(LOG_PATH_SYS, "./logs/");
        }
        String logPathEnv = System.getenv(ORS_LOG_LOCATION_ENV);
        if (!Strings.isNullOrEmpty(logPathEnv)) {
            System.setProperty(LOG_PATH_SYS, logPathEnv);
        }
    }

    public static void main(String[] args) {
        setSystemProperties();
        SpringApplication.run(applicationClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        setSystemProperties();
        return application.sources(applicationClass);
    }


    /**
     * This is a workaround for the unmaintained springfox-swagger2.
     * Once it's replaced with the openapi specs, remove this override.
     */
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping);
    }

    /**
     * This is a workaround for the unmaintained springfox-swagger2.
     * Once it's replaced with the openapi specs, remove this override.
     */
    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

    @Bean("ORSInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties));
        return bean;
    }
}
