package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.heigit.ors.api.services.EngineService;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.heigit.ors.api.ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY;
import static org.heigit.ors.api.services.EngineService.SHUTDOWN_IMMEDIATELY;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer {
    private static final Logger LOG = Logger.getLogger(Application.class.getName());

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0]) && !args[0].startsWith("-")) {
            System.setProperty(ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        SpringApplication.run(Application.class, args);
        LOG.info("openrouteservice %s".formatted(AppInfo.getEngineInfo()));
        if (System.getProperty(SHUTDOWN_IMMEDIATELY, "").equals("true")) {
            System.exit(0);
        }
    }

    @Bean("orsInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineService engineService) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(engineService);
        return bean;
    }
}
