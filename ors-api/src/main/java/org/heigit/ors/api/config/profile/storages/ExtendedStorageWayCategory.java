package org.heigit.ors.api.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("WayCategory")
public class ExtendedStorageWayCategory extends ExtendedStorage {

    public ExtendedStorageWayCategory() {
    }

    @JsonCreator
    public ExtendedStorageWayCategory(String ignoredEmpty) {
    }
}