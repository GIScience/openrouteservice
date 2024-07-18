package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.api.converters.PathDeserializer;
import org.heigit.ors.api.converters.PathSerializer;

import java.nio.file.Path;

@JsonTypeName("GreenIndex")
public class ExtendedStorageGreenIndex extends ExtendedStorage {

    private Path filepath = Path.of("");

    public ExtendedStorageGreenIndex() {
    }

    @JsonSerialize(using = PathSerializer.class)
    @JsonProperty("filepath")
    public Path getFilepath() {
        return this.filepath;
    }


    @JsonSetter("filepath")
    @JsonDeserialize(using = PathDeserializer.class)
    // Todo deserializer should output absolute paths to reduce complexity
    public void setFilepath(Path filepath) {
        this.filepath = filepath;
    }

}

