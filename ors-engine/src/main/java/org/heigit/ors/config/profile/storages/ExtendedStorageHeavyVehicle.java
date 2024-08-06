package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
}
