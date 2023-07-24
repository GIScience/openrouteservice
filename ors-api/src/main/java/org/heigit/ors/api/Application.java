package org.heigit.ors.api;

import jakarta.servlet.ServletContextListener;
import org.heigit.ors.api.servlet.listeners.ORSInitContextListener;
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
        SpringApplication.run(Application.class, args);
    }

    @Bean("ORSInitContextListenerBean")
    public ServletListenerRegistrationBean<ServletContextListener> createORSInitContextListenerBean(EngineProperties engineProperties) {
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ORSInitContextListener(engineProperties));
        return bean;
    }
}
