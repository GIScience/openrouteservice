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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
        log.info("Configuration lookup started.");
        if (!StringUtility.isNullOrEmpty(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY))) {
            configLocations.add(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY));
            log.info("Configuration file set by program argument.");
        }
        if (configLocations.isEmpty() && !StringUtility.isNullOrEmpty(System.getenv(ORS_CONFIG_LOCATION_ENV))) {
            String configPath = System.getenv(ORS_CONFIG_LOCATION_ENV);
            File configFile = new File(configPath);
            // Check if the file exists and log the appropriate message
            if (configFile.exists()) {
                log.info("ORS config file found at: " + configFile.getAbsolutePath());
                configLocations.add(configPath);
                log.info("Configuration file set by environment variable.");
            } else {
                // Add it anyway to provoke error and stop ORS
                configLocations.add(configPath);
                log.error("ORS config file not found at: " + configPath);
            }
        }
        if (configLocations.isEmpty()) {
            String home = System.getProperty("user.home");
            configLocations.add("./ors-config.yml");
            configLocations.add(home + "/.config/openrouteservice/ors-config.yml");
            configLocations.add("/etc/openrouteservice/ors-config.yml");
            log.info("Configuration file lookup by default locations.");
        }
        for (String path : configLocations) {
            try {
                List<PropertySource<?>> sources = this.loader.load("yml config", new FileSystemResource(path));
                if (!sources.isEmpty()) {
                    environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, sources.get(0));
                    System.setProperty(ORS_CONFIG_LOCATION_PROPERTY, path);
                    log.info("Loaded file '%s'.".formatted(path));
                    break;
                }
            } catch (IllegalStateException | IOException ignored) {
                log.debug("Config file '%s' not found.".formatted(path));
            }
        }
        var relevantPrefixes = List.of(
                "ORS_",
                "ors.",
                "LOGGING_",
                "logging.",
                "SPRINGDOC_",
                "springdoc.",
                "SPRING_",
                "spring.",
                "SERVER_",
                "server.");
        var relevantENVs = environment.getSystemProperties().entrySet().stream()
                .filter(env -> relevantPrefixes.stream().anyMatch(env.getKey()::startsWith))
                .sorted(Entry.comparingByKey()).toList();
        if (!relevantENVs.isEmpty()) {
            log.info("");
            log.info("Environment variables overriding openrouteservice configuration parameters detected: ");
            relevantENVs.forEach(env -> log.info("%s=%s".formatted(env.getKey(), env.getValue())));
        }
        log.info("Configuration lookup finished.");
        log.info("");
    }
}
