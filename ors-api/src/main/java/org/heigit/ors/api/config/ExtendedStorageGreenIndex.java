package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.nio.file.Path;

@JsonTypeName("GreenIndex")
public class ExtendedStorageGreenIndex extends ExtendedStorage {

    public ExtendedStorageGreenIndex() {
    }

    @JsonProperty("filepath")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Path getFilepath() {
        return super.getFilepath();
    }

    @JsonSetter("filepath")
    public void setFilepath(String filepath) {
        super.setFilepath(filepath);
    }
}

