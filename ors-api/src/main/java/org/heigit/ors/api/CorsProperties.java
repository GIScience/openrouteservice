package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "ors.cors")
public class CorsProperties {

    public static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of("*");

    public static final List<String> DEFAULT_ALLOWED_HEADERS = List.of(
            "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"
    );

    public static final long DEFAULT_MAX_PREFLIGHT_AGE = 600L;

    private List<String> allowedOrigins = DEFAULT_ALLOWED_ORIGINS;

    private List<String> allowedHeaders = DEFAULT_ALLOWED_HEADERS;

    private long preflightMaxAge = DEFAULT_MAX_PREFLIGHT_AGE;

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = Arrays.stream(allowedHeaders.split(",")).map(String::trim).toList();
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public long getPreflightMaxAge() {
        return preflightMaxAge;
    }

    public void setPreflightMaxAge(long preflightMaxAge) {
        this.preflightMaxAge = preflightMaxAge;
    }

    public void setAllowedOriginsList(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowedHeadersList(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }
}
