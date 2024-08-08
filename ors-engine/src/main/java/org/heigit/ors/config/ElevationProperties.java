package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.config.utils.PathDeserializer;
import org.heigit.ors.config.utils.PathSerializer;

import java.nio.file.Path;

@Getter
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ElevationProperties {
    private Boolean preprocessed;
    @JsonProperty("data_access")
    private DataAccessEnum dataAccess;
    @JsonProperty("cache_clear")
    private Boolean cacheClear;
    @JsonProperty("provider")
    private String provider;
    @JsonProperty("cache_path")
    @JsonDeserialize(using = PathDeserializer.class)
    @JsonSerialize(using = PathSerializer.class)
    private Path cachePath;

    public Boolean isPreprocessed() {
        return preprocessed;
    }
}