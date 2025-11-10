package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicDataProperties {
    private Boolean enabled;

    @JsonProperty("store_url")
    private String storeUrl = "jdbc:postgresql://localhost:5432/featurestore";
    @JsonProperty("store_user")
    private String storeUser;
    @JsonProperty("store_pass")
    private String storePass;

    public DynamicDataProperties() {
    }

    public DynamicDataProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return enabled == null && storeUrl == null && storeUser == null && storePass == null;
    }

    public void merge(DynamicDataProperties other) {
        enabled = ofNullable(this.enabled).orElse(other.enabled);
        storeUrl = ofNullable(this.storeUrl).orElse(other.storeUrl);
        storeUser = ofNullable(this.storeUser).orElse(other.storeUser);
        storePass = ofNullable(this.storePass).orElse(other.storePass);
    }
}


