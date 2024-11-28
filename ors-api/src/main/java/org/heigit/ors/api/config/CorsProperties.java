package org.heigit.ors.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter(AccessLevel.PACKAGE)
@Configuration
@ConfigurationProperties(prefix = "ors.cors")
public class CorsProperties {
    private List<String> allowedOrigins;
    private List<String> allowedHeaders;
    private long preflightMaxAge;
}
