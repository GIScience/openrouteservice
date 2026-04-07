package org.heigit.ors.config;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicDataProperties {
    private Boolean enabled;
    private String featureStoreApiUrl;

    public DynamicDataProperties() {
    }

    public DynamicDataProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return enabled == null && featureStoreApiUrl == null;
    }

    public void merge(DynamicDataProperties other) {
        enabled = ofNullable(this.enabled).orElse(other.enabled);
        featureStoreApiUrl = ofNullable(this.featureStoreApiUrl).orElse(other.featureStoreApiUrl);
    }

    // Explicit getter to ensure it's available at runtime (Lombok may not process it in some configurations)
    public String getFeatureStoreApiUrl() {
        return featureStoreApiUrl;
    }
}


