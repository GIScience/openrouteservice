package org.heigit.ors.api;

import org.apache.commons.logging.Log;
import org.heigit.ors.util.StringUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ORSEnvironmentPostProcessor implements EnvironmentPostProcessor {

    public static final String ORS_CONFIG_LOCATION_ENV = "ORS_CONFIG_LOCATION";
    public static final String ORS_CONFIG_LOCATION_PROPERTY = "ors.config.location";
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    private final Log log;

    public ORSEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        log = logFactory.getLog(ORSEnvironmentPostProcessor.class);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.matchesProfiles("test")) {
            log.info("No additional configuration loaded, test profile is active.");
            return;
        }
        // Override values from application.yml with contents of custom config yml file.
        List<String> configLocations = new ArrayList<>();
        log.info("");
        if (!StringUtility.isNullOrEmpty(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY))) {
            configLocations.add(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY));
            log.info("Configuration file set by program argument.");
        }
        if (configLocations.isEmpty() && !StringUtility.isNullOrEmpty(System.getenv(ORS_CONFIG_LOCATION_ENV))) {
            configLocations.add(System.getenv(ORS_CONFIG_LOCATION_ENV));
            log.info("Configuration file set by environment variable.");
        }
        if (configLocations.isEmpty()) {
            configLocations.add("./ors-config.yml");
            configLocations.add("~/.config/openrouteservice/ors-config.yml");
            configLocations.add("/etc/openrouteservice/ors-config.yml");
            log.info("Configuration file lookup by default locations.");
        }
        for (String path : configLocations) {
            try {
                List<PropertySource<?>> sources = this.loader.load("yml config", new FileSystemResource(path));
                if (!sources.isEmpty()) {
                    environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, sources.get(0));
                    log.info("Loaded file '%s'".formatted(path));
                    break;
                }
            } catch (IllegalStateException | IOException ignored) {
            }
        }
        List<Map.Entry<String, String>> relevantENVs = System.getenv().entrySet()
                .stream().filter(env ->
                        env.getKey().startsWith("ORS_") ||
                        env.getKey().startsWith("LOGGING_") ||
                        env.getKey().startsWith("SPRINGDOC_") ||
                        env.getKey().startsWith("SPRING_") ||
                        env.getKey().startsWith("SERVER_") ||
                        env.getKey().startsWith("ors.") ||
                        env.getKey().startsWith("logging.") ||
                        env.getKey().startsWith("springdoc.") ||
                        env.getKey().startsWith("spring.") ||
                        env.getKey().startsWith("server.")
                ).sorted(Map.Entry.<String, String>comparingByKey()).toList();
        if (!relevantENVs.isEmpty()) {
            log.info("");
            log.info("Environment variables overriding openrouteservice configuration parameters detected: ");
            for (Map.Entry<String, String> env : relevantENVs) {
                log.info("%s=%s".formatted(env.getKey(), env.getValue()));
            }
        }
        log.info("");
    }
}
