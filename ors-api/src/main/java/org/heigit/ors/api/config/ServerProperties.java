package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerProperties {
    private Integer port;
    private ServletProperties servlet;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ServletProperties getServlet() {
        return servlet;
    }

    public void setServlet(ServletProperties servlet) {
        this.servlet = servlet;
    }

    public static class ServletProperties {
        @JsonProperty("context-path")
        private String contextPath;

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }
    }
}
