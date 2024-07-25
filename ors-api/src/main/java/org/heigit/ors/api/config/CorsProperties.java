package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JsonProperty("allowed_origins")
    @JsonSerialize(using = InlineArraySerializer.class)
    private List<String> allowedOrigins = List.of("*");

    @JsonProperty("allowed_headers")
    @JsonSerialize(using = InlineArraySerializer.class)
    private List<String> allowedHeaders = List.of(
            "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"
    );

    @Setter(AccessLevel.PACKAGE)
    @JsonProperty("preflight_max_age")
    private long preflightMaxAge = 600L;

}
