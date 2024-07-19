package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.api.converters.PathDeserializer;
import org.heigit.ors.api.converters.PathSerializer;

import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
class ExtendedStorage {

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


/**
 * This extends the ExtendedStorage class and adds a filepath field for the Index storages such as GreenIndex and NoiseIndex.
 */
class ExtendedStorageIndex extends ExtendedStorage {

    private Path filepath = Path.of("");

    public ExtendedStorageIndex() {
    }

    @JsonProperty("filepath")
    @JsonSerialize(using = PathSerializer.class)
    public Path getFilepath() {
        return this.filepath;
    }


    @JsonSetter("filepath")
    @JsonDeserialize(using = PathDeserializer.class)
    // Todo deserializer should output absolute paths to reduce complexity
    private void setFilepath(Path filepath) {
        this.filepath = filepath;
    }
}


