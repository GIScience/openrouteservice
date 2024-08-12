package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;

/**
 * This extends the ExtendedStorage class and adds a filepath field for the Index storages such as GreenIndex and NoiseIndex.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExtendedStorageNoiseIndex.class, name = "NoiseIndex"),
        @JsonSubTypes.Type(value = ExtendedStorageCsvIndex.class, name = "csv"),
        @JsonSubTypes.Type(value = ExtendedStorageGreenIndex.class, name = "GreenIndex"),
        @JsonSubTypes.Type(value = ExtendedStorageShadowIndex.class, name = "ShadowIndex"),
})
@EqualsAndHashCode(callSuper = false)
public abstract class ExtendedStorageIndex extends ExtendedStorage {

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
    private void setFilepath(Object filepath) {
        if (filepath instanceof Path objectCast) {
            this.filepath = objectCast;
        }
    }


    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value == null) {
            return;
        }
        if (value instanceof ExtendedStorageIndex storage) {
            Path emptyPath = Path.of("");
            if (this.getFilepath().equals(emptyPath)) {
                this.setFilepath(storage.getFilepath());
            } else {
                if (!storage.getFilepath().equals(emptyPath) && overwrite) {
                    this.setFilepath(storage.getFilepath());
                }
            }
        }
    }
}
