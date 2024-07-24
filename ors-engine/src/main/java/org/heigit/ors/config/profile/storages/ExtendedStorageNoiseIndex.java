package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("NoiseIndex")
public class ExtendedStorageNoiseIndex extends ExtendedStorageIndex {

    public ExtendedStorageNoiseIndex() {
    }

    @JsonCreator
    public ExtendedStorageNoiseIndex(String ignoredEmpty) {
    }

}

