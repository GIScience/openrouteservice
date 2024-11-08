package org.heigit.ors.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ors.engine")
public class EngineProperties extends org.heigit.ors.config.EngineProperties {
}
