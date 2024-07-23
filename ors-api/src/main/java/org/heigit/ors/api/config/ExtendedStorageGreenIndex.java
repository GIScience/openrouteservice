package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("GreenIndex")
public class ExtendedStorageGreenIndex extends ExtendedStorageIndex {

    public ExtendedStorageGreenIndex() {
    }

    @JsonCreator
    public ExtendedStorageGreenIndex(String ignoredEmpty) {
    }

}

