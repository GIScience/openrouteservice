package org.heigit.ors.api;

import org.heigit.ors.util.StringUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ORSEnvironmentPostProcessor implements EnvironmentPostProcessor {

    public static final String ORS_CONFIG_LOCATION_ENV = "ORS_CONFIG_LOCATION";
    public static final String ORS_CONFIG_LOCATION_PROPERTY = "ors.config.location";
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Override values from application.yml with contents of custom config yml file.
        List<String> configLocations = new ArrayList<>();
        String output = "";
        if (!StringUtility.isNullOrEmpty(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY))) {
            configLocations.add(System.getProperty(ORS_CONFIG_LOCATION_PROPERTY));
            output = output.concat("Configuration file set by program argument.");
        }
        if (configLocations.isEmpty() && !StringUtility.isNullOrEmpty(System.getenv(ORS_CONFIG_LOCATION_ENV))) {
            configLocations.add(System.getenv(ORS_CONFIG_LOCATION_ENV));
            output = output.concat("Configuration file set by environment variable.");
        }
        if (configLocations.isEmpty()) {
            configLocations.add("./ors-config.yml");
            configLocations.add("./ors-api/ors-config.yml");
            configLocations.add("~/.config/openrouteservice/ors-config.yml");
            configLocations.add("/etc/openrouteservice/ors-config.yml");
            output = output.concat("Configuration file lookup by default locations.");
        }
        for (String path : configLocations) {
            try {
                List<PropertySource<?>> sources = this.loader.load("yml config", new FileSystemResource(path));
                if (!sources.isEmpty()) {
                    environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, sources.get(0));
                    output = output.concat(" Loaded file '%s'".formatted(path));
                    break;
                }
            } catch (IllegalStateException | IOException ignored) {
            }
        }
        System.setProperty(ORS_CONFIG_LOCATION_PROPERTY, output);
    }
}
