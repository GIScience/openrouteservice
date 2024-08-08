package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("RoadAccessRestrictions")
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
}
