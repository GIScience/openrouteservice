package org.heigit.ors.api.config.profile.storages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Tollways")
public class ExtendedStorageTollways extends ExtendedStorage {

    public ExtendedStorageTollways() {
    }

    @JsonCreator
    public ExtendedStorageTollways(String ignoredEmpty) {
    }
}
