package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.services.GraphService;
import org.apache.log4j.Logger;
import org.heigit.ors.api.config.*;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application extends SpringBootServletInitializer {
    private static final Logger LOG = Logger.getLogger(Application.class.getName());

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0]) && !args[0].startsWith("-")) {
            System.setProperty(ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        SpringApplication.run(Application.class, args);
        if (RoutingProfileManagerStatus.isShutdown()) {
            System.exit(RoutingProfileManagerStatus.hasFailed() ? 1 : 0);
        }
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(Application.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }

    @Bean("orsInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EndpointsProperties endpointsProperties, CorsProperties corsProperties, SystemMessageProperties systemMessageProperties, LoggingProperties loggingProperties, ServerProperties serverProperties, GraphService graphService) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(endpointsProperties, corsProperties, systemMessageProperties, loggingProperties, serverProperties, graphService));
        return bean;
    }
}
