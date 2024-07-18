package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.stereotype.Component;

@JsonTypeName("HeavyVehicle")
public class ExtendedStorageHeavyVehicle extends ExtendedStorage {

    public ExtendedStorageHeavyVehicle() {
    }
}
