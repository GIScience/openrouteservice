package org.heigit.ors.api.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("HeavyVehicle")
public class ExtendedStorageHeavyVehicle extends ExtendedStorage {

    private Boolean restrictions = true;

    @JsonCreator
    public ExtendedStorageHeavyVehicle() {
    }

    @JsonCreator
    public ExtendedStorageHeavyVehicle(String ignoredEmpty) {
    }

    @JsonProperty
    public Boolean getRestrictions() {
        return restrictions;
    }

    @JsonSetter
    private void setRestrictions(String restrictions) {
        this.restrictions = Boolean.parseBoolean(restrictions);
    }
}
