package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProfileDefaultProperties extends ProfileProperties {

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("graph_path")
    private Path graphPath;
}