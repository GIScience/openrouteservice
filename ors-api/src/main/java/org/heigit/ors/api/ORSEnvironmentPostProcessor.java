package org.heigit.ors.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.List;

public class ORSEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Override values from application.yml with contents of custom config yml file.
        // Later in array => higher precedence
        String[] configLocations = {"/etc/openrouteservice/ors-config.yml", "~/.openrouteservice/ors-config.yml", "ors-config.yml"};
        for (String path : configLocations) {
            try {
                List<PropertySource<?>> sources = this.loader.load("yml config", new FileSystemResource(path));
                if (!sources.isEmpty()) {
                    environment.getPropertySources().addFirst(sources.get(0));
                }
            } catch (IllegalStateException | IOException ex) {
                // Ignore yml file not present
            }
        }
    }
}
