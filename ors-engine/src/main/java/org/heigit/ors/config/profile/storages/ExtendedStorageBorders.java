package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@JsonTypeName("Borders")
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

}
