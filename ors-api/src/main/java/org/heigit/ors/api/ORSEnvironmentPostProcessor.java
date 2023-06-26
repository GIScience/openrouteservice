package org.heigit.ors.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class ORSEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String path = "systemmessage.yml";
        try {
            environment.getPropertySources().addLast(this.loader.load("yml config", new ClassPathResource(path)).get(0));
        } catch (IllegalStateException | IOException ex) {
            // Ignore yml file not present
        }
    }
}
