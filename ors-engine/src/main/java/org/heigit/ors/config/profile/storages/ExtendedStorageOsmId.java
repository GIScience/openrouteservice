package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("OsmId")
public class ExtendedStorageOsmId extends ExtendedStorage {

    public ExtendedStorageOsmId() {
    }

    @JsonCreator
    public ExtendedStorageOsmId(String ignoredEmpty) {
    }
}