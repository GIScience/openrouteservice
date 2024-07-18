package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;

import java.nio.file.Path;
import java.util.LinkedHashMap;

// Ignore properties that are only used for some subclasses
public class ExtendedStorage {

    @JsonProperty
    Boolean enabled = true;

    @JsonIgnore
    @JsonProperty
    Path filepath = Path.of("");


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

    public Path getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        if (filepath != null && !filepath.isEmpty()) {
            this.filepath = Path.of(filepath).toAbsolutePath();
        }
    }
}
