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

    @JsonProperty("filepath")
    @JsonSerialize(using = PathSerializer.class)
    public Path getFilepath() {
        return this.filepath;
    }


    @JsonSetter("filepath")
    @JsonDeserialize(using = PathDeserializer.class)
    // Todo deserializer should output absolute paths to reduce complexity
    public void setFilepath(Path filepath) {
        // Todo: Decide if we want to use realpath or absolute path only. Realpath could check if the file exists.
        if (filepath != null && !filepath.toString().isEmpty())
            this.filepath = filepath.toAbsolutePath();
    }

}

