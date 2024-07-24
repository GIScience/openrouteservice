package org.heigit.ors.api.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ShadowIndex")
public class ExtendedStorageShadowIndex extends ExtendedStorageIndex {

    public ExtendedStorageShadowIndex() {
    }

    @JsonCreator
    public ExtendedStorageShadowIndex(String ignoredEmpty) {
    }

}
