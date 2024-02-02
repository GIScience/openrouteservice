package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
import org.heigit.ors.api.util.AppInfo;
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
    private static final Logger LOG = Logger.getLogger(Application.class.getName());

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0]) && !args[0].startsWith("-")) {
            System.setProperty(ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        SpringApplication.run(Application.class, args);
        LOG.info("openrouteservice %s".formatted(AppInfo.getEngineInfo()));
        if (RoutingProfileManagerStatus.hasFailed()) {
            System.exit(1);
        }
    }

    @Bean("ORSInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties));
        return bean;
    }
}
