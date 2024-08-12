package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@JsonTypeName("Borders")
@EqualsAndHashCode(callSuper = false)
public class ExtendedStorageBorders extends ExtendedStorage {
    Logger logger = LoggerFactory.getLogger(ExtendedStorageBorders.class);

    private Path boundaries = Path.of("");

    private Path ids = Path.of("");

    private Path openborders = Path.of("");


    public ExtendedStorageBorders() {
    }

    @JsonCreator
    public ExtendedStorageBorders(String ignoredEmpty) {
        logger.warn("Borders storage is not correctly configured and will be disabled.");
        setEnabled(false);
    }

    @JsonProperty(value = "boundaries")
    @JsonSerialize(using = PathSerializer.class)
    public Path getBoundaries() {
        return boundaries;
    }

    @JsonSetter("boundaries")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setBoundaries(Path boundaries) {
        if (boundaries != null && !boundaries.toString().isEmpty()) this.boundaries = boundaries.toAbsolutePath();
    }

    @JsonProperty(value = "ids")
    @JsonSerialize(using = PathSerializer.class)
    public Path getIds() {
        return ids;
    }

    @JsonSetter("ids")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setIds(Path ref_pattern) {
        if (ref_pattern != null && !ref_pattern.toString().isEmpty()) this.ids = ref_pattern.toAbsolutePath();
    }

    @JsonProperty(value = "openborders")
    @JsonSerialize(using = PathSerializer.class)
    public Path getOpenborders() {
        return openborders;
    }

    @JsonSetter("openborders")
    @JsonDeserialize(using = PathDeserializer.class)
    private void setOpenborders(Path openborders) {
        if (openborders != null && !openborders.toString().isEmpty()) this.openborders = openborders.toAbsolutePath();
    }

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageBorders storage) {
            Path emptyPath = Path.of("");
            if (this.getBoundaries().equals(emptyPath))
                this.setBoundaries(storage.boundaries);
            else if (!storage.getBoundaries().equals(emptyPath) && overwrite)
                this.setBoundaries(storage.boundaries);
            if (this.getIds().equals(emptyPath))
                this.setIds(storage.ids);
            else if (!storage.getIds().equals(emptyPath) && overwrite)
                this.setIds(storage.ids);
            if (this.getOpenborders().equals(emptyPath))
                this.setOpenborders(storage.openborders);
            else if (!storage.getOpenborders().equals(emptyPath) && overwrite)
                this.openborders = storage.openborders;
        }
    }

}
