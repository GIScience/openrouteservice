package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;

import java.nio.file.Path;

@JsonTypeName("GreenIndex")
public class ExtendedStorageGreenIndex extends ExtendedStorage {

    public ExtendedStorageGreenIndex() {
    }

    @JsonProperty("filepath")
    public Path getFilepath() {
        return super.getFilepath();
    }

    @JsonSetter("filepath")
    public void setFilepath(String filepath) {
        super.setFilepath(filepath);
    }
}
