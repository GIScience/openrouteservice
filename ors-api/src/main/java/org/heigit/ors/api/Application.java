package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.config.*;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0]) && !args[0].startsWith("-")) {
            System.setProperty(ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        SpringApplication.run(Application.class, args);
        if (RoutingProfileManagerStatus.isShutdown()) {
            System.exit(RoutingProfileManagerStatus.hasFailed() ? 1 : 0);
        }
    }

    @Bean("orsInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties, EndpointsProperties endpointsProperties, CorsProperties corsProperties, SystemMessageProperties systemMessageProperties, LoggingProperties loggingProperties, ServerProperties serverProperties) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties, endpointsProperties, corsProperties, systemMessageProperties, loggingProperties, serverProperties));
        return bean;
    }
}
