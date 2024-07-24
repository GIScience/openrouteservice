package org.heigit.ors.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("HillIndex")
public class ExtendedStorageHillIndex extends ExtendedStorage {

    public ExtendedStorageHillIndex() {
    }

    @JsonCreator
    public ExtendedStorageHillIndex(String ignoredEmpty) {
    }
}
