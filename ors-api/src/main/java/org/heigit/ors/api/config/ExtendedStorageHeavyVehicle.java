package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("HeavyVehicle")
public class ExtendedStorageHeavyVehicle extends ExtendedStorage {

    private Boolean restrictions = true;


    public ExtendedStorageHeavyVehicle() {
    }

    @JsonProperty(value = "restrictions")
    public Boolean getRestrictions() {
        return restrictions;
    }

    @JsonSetter("restrictions")
    private void setRestrictions(Boolean restrictions) {
        this.restrictions = restrictions;
    }
}
