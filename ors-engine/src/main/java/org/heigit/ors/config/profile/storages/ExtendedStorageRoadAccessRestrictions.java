package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;

@JsonTypeName("RoadAccessRestrictions")
@EqualsAndHashCode(callSuper = false)
public class ExtendedStorageRoadAccessRestrictions extends ExtendedStorage {

    private Boolean use_for_warnings = true;

    public ExtendedStorageRoadAccessRestrictions() {
    }

    public ExtendedStorageRoadAccessRestrictions(Boolean use_for_warnings) {
        this.use_for_warnings = use_for_warnings;
    }

    @JsonCreator
    public ExtendedStorageRoadAccessRestrictions(String ignoredEmpty) {
    }

    @JsonProperty("use_for_warnings")
    public Boolean getUseForWarnings() {
        return use_for_warnings;
    }

    @JsonSetter("use_for_warnings")
    public void setUseForWarnings(Boolean use_for_warnings) {
        this.use_for_warnings = use_for_warnings;
    }

    @JsonIgnore
    @Override
    public void copyProperties(ExtendedStorage value, boolean overwrite) {
        super.copyProperties(value, overwrite);
        if (value instanceof ExtendedStorageRoadAccessRestrictions storage) {
            if (this.getUseForWarnings() == null || (storage.getUseForWarnings() != null && overwrite)) {
                this.setUseForWarnings(storage.getUseForWarnings());
            }
        }
    }
}
