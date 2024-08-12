package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;

@JsonTypeName("Wheelchair")
@EqualsAndHashCode(callSuper = false)
public class ExtendedStorageWheelchair extends ExtendedStorage {

    private Boolean kerbs_on_crossings = true;

    public ExtendedStorageWheelchair() {
    }

    public ExtendedStorageWheelchair(Boolean kerbs_on_crossings) {
        this.kerbs_on_crossings = kerbs_on_crossings;
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

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageWheelchair storage) {
            if (this.getKerbsOnCrossings() == null || (storage.getKerbsOnCrossings() != null && overwrite)) {
                this.setKerbsOnCrossings(storage.getKerbsOnCrossings());
            }
        }
    }
}
