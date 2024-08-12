package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

@Getter
@JsonTypeName("HeavyVehicle")
public class ExtendedStorageHeavyVehicle extends ExtendedStorage {

    @JsonProperty
    private Boolean restrictions = true;

    @JsonCreator
    public ExtendedStorageHeavyVehicle() {
    }

    public ExtendedStorageHeavyVehicle(Boolean restrictions) {
        this.restrictions = restrictions;
    }

    @JsonCreator
    public ExtendedStorageHeavyVehicle(String ignoredEmpty) {
    }

    @JsonSetter
    private void setRestrictions(String restrictions) {
        this.restrictions = Boolean.parseBoolean(restrictions);
    }

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageHeavyVehicle storage) {
            if (this.getRestrictions() == null || (storage.getRestrictions() != null && overwrite)) {
                this.setRestrictions(storage.getRestrictions().toString());
            }
        }
    }
}
