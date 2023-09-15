package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.services.GraphService;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
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
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan("org.heigit.ors.api.servlet.listeners")
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        if (args.length > 0 && !StringUtility.isNullOrEmpty(args[0])) {
            System.setProperty(ORSEnvironmentPostProcessor.ORS_CONFIG_LOCATION_PROPERTY, args[0]);
        }
        context = SpringApplication.run(Application.class, args);
        if (RoutingProfileManagerStatus.hasFailed()) {
            System.exit(1);
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

    @Bean("ORSInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties, GraphService graphService) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties, graphService));
        return bean;
    }
}
