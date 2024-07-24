package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter(AccessLevel.PACKAGE)
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerProperties {
    private Integer port;
    private ServletProperties servlet;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class ServletProperties {
        @JsonProperty("context-path")
        private String contextPath;
    }
}
