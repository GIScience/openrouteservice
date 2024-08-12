package org.heigit.ors.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public void copyProperties(ElevationProperties source, boolean overwrite) {
        if (source == null) {
            return;
        }

        if (this.getPreprocessed() == null) {
            this.setPreprocessed(source.getPreprocessed());
        } else {
            if (source.getPreprocessed() != null && overwrite) {
                this.setPreprocessed(source.getPreprocessed());
            }
        }

        if (this.getDataAccess() == null) {
            this.setDataAccess(source.getDataAccess());
        } else {
            if (source.getDataAccess() != null && overwrite) {
                this.setDataAccess(source.getDataAccess());
            }
        }

        if (this.getCacheClear() == null) {
            this.setCacheClear(source.getCacheClear());
        } else {
            if (source.getCacheClear() != null && overwrite) {
                this.setCacheClear(source.getCacheClear());
            }
        }

        if (this.getProvider() == null) {
            this.setProvider(source.getProvider());
        } else {
            if (source.getProvider() != null && overwrite) {
                this.setProvider(source.getProvider());
            }
        }

        if (this.getCachePath() == null) {
            this.setCachePath(source.getCachePath());
        } else {
            if (source.getCachePath() != null && overwrite) {
                this.setCachePath(source.getCachePath());
            }
        }
    }
}