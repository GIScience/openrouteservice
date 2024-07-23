package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("WaySurfaceType")
public class ExtendedStorageWaySurfaceType extends ExtendedStorage {

    public ExtendedStorageWaySurfaceType() {
    }

    @JsonCreator
    public ExtendedStorageWaySurfaceType(String ignoredEmpty) {
    }
}
