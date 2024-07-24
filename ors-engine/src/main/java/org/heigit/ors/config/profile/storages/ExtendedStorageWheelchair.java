package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Wheelchair")
public class ExtendedStorageWheelchair extends ExtendedStorage {

    private Boolean kerbs_on_crossings = true;

    public ExtendedStorageWheelchair() {
    }

    @JsonCreator
    public ExtendedStorageWheelchair(String ignoredEmpty) {
    }

    @JsonProperty(value = "KerbsOnCrossings", defaultValue = "true")
    public Boolean getKerbsOnCrossings() {
        return kerbs_on_crossings;
    }

    @JsonSetter("KerbsOnCrossings")
    private void setKerbsOnCrossings(Boolean kerbs_on_crossings) {
        this.kerbs_on_crossings = kerbs_on_crossings;
    }
}
