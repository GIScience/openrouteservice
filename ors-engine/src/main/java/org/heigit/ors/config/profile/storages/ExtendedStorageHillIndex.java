package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("HillIndex")
public class ExtendedStorageHillIndex extends ExtendedStorage {
    @JsonProperty("maximum_slope")
    private Integer maximumSlope;

    public ExtendedStorageHillIndex() {
    }

    @JsonCreator
    public ExtendedStorageHillIndex(String ignoredEmpty) {
    }
}
