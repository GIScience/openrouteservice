package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import java.io.File;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0])) {
            System.setProperty(ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        SpringApplication.run(Application.class, args);
    }

    @Bean("ORSInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties));
        return bean;
    }
}
