package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.heigit.ors.api.converters.PathDeserializer;
import org.heigit.ors.api.converters.PathSerializer;

import java.nio.file.Path;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExtendedStorageWayCategory.class, name = "WayCategory"),
        @JsonSubTypes.Type(value = ExtendedStorageHeavyVehicle.class, name = "HeavyVehicle"),
        @JsonSubTypes.Type(value = ExtendedStorageHereTraffic.class, name = "HereTraffic"),
        @JsonSubTypes.Type(value = ExtendedStorageHillIndex.class, name = "HillIndex"),
        @JsonSubTypes.Type(value = ExtendedStorageOsmId.class, name = "OsmId"),
        @JsonSubTypes.Type(value = ExtendedStorageBorders.class, name = "Borders"),
        @JsonSubTypes.Type(value = ExtendedStorageRoadAccessRestrictions.class, name = "RoadAccessRestrictions"),
        @JsonSubTypes.Type(value = ExtendedStorageTrailDifficulty.class, name = "TrailDifficulty"),
        @JsonSubTypes.Type(value = ExtendedStorageWheelchair.class, name = "Wheelchair"),
        @JsonSubTypes.Type(value = ExtendedStorageWaySurfaceType.class, name = "WaySurfaceType"),
})
public abstract class ExtendedStorage {

    @JsonProperty
    private Boolean enabled = true;

    @JsonCreator
    public ExtendedStorage() {
    }

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonSetter("enabled")
    void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}


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
abstract class ExtendedStorageIndex extends ExtendedStorage {

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
}


