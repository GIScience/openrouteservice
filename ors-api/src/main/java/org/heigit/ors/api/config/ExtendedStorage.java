package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;


public class ExtendedStorage {

    @JsonProperty
    Boolean enabled = true;

    public ExtendedStorage() {
    }

    @JsonCreator
    // Scenarios where the storage is empty "WayCategory:"
    public ExtendedStorage(String ignoredEmpty) {
        super();
    }

    @JsonCreator
    // Scenarios where the storage is empty with curly braces "WayCategory: {}"
    public ExtendedStorage(LinkedHashMap<String, Object> ignoredMap) {
        super();
    }

   // Getter and setter
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
