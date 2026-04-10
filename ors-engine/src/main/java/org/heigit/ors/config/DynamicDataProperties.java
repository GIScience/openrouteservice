package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicDataProperties {
    private static final Logger LOGGER = Logger.getLogger(DynamicDataProperties.class.getName());

    private Boolean enabled;

    // Support multiple property names for compatibility:
    // 1. featureStoreApiUrl: traditional YAML property name
    // 2. apiUrl: from environment variable api_url via Spring RelaxedBinding
    @JsonProperty("feature_store_api_url")
    private String featureStoreApiUrl;

    // This field receives the api_url environment variable via Spring's
    // RelaxedBinding
    // RelaxedBinding converts api_url → apiUrl automatically
    @JsonProperty("api_url")
    private String apiUrl; // Alias for featureStoreApiUrl for backward/forward compatibility

    public DynamicDataProperties() {
        LOGGER.debug("DynamicDataProperties constructed (no-arg)");
    }

    public DynamicDataProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
        LOGGER.debug("DynamicDataProperties constructed (with ignored parameter)");
    }

    /**
     * Explicit setter for api_url to ensure Spring's RelaxedBinding can set it.
     * This overrides the Lombok-generated setter to add logging.
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        if (apiUrl != null && !apiUrl.isBlank()) {
            LOGGER.debug("DynamicDataProperties.setApiUrl: received api_url=" + apiUrl);
        }
    }

    /**
     * Called by Spring after properties are set to initialize/validate
     */
    public void logConfiguration() {
        LOGGER.info("DynamicDataProperties.logConfiguration: enabled=" + enabled);
        LOGGER.info("DynamicDataProperties.logConfiguration: featureStoreApiUrl=" + featureStoreApiUrl);
        LOGGER.info("DynamicDataProperties.logConfiguration: apiUrl=" + apiUrl);
        LOGGER.info("DynamicDataProperties.logConfiguration: getFeatureStoreApiUrl()=" + getFeatureStoreApiUrl());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return enabled == null && featureStoreApiUrl == null && apiUrl == null;
    }

    public void merge(DynamicDataProperties other) {
        enabled = ofNullable(this.enabled).orElse(other.enabled);
        featureStoreApiUrl = ofNullable(this.featureStoreApiUrl).orElse(other.featureStoreApiUrl);
        apiUrl = ofNullable(this.apiUrl).orElse(other.apiUrl);
    }

    /**
     * Explicit getter for the FeatureStore API URL.
     * 
     * This getter prioritizes apiUrl (from api_url environment variable) over
     * featureStoreApiUrl
     * because environment variables typically take precedence over YAML
     * configuration.
     * 
     * @return the FeatureStore API URL, or null if not configured
     */
    public String getFeatureStoreApiUrl() {
        // Prefer apiUrl (from api_url environment variable),
        // fallback to featureStoreApiUrl (from YAML)
        return apiUrl != null ? apiUrl : featureStoreApiUrl;
    }
}


