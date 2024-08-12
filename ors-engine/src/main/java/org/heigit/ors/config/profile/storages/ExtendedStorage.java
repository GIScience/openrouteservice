package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * This the base class for the extended storage configuration. It contains a boolean field to enable or disable the storage.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode
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
        @JsonSubTypes.Type(value = ExtendedStorageTollways.class, name = "Tollways"),
})
public abstract class ExtendedStorage {

    @JsonProperty
    private Boolean enabled = true;

    @JsonCreator
    public ExtendedStorage() {
    }

    @JsonIgnore
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        if (value == null) {
            return;
        }

        if (this.getEnabled() == null) {
            this.setEnabled(value.enabled);
        } else {
            if (value.getEnabled() != null && overwrite) {
                this.setEnabled(value.enabled);
            }
        }
    }
}


