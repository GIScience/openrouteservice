package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.routing.ev.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicDataProperties {
    @JsonProperty("logie_borders")
    private Boolean logieBorders;
    @JsonProperty("logie_bridges")
    private Boolean logieBridges;
    @JsonProperty("logie_roads")
    private Boolean logieRoads;

    public DynamicDataProperties() {
    }

    public DynamicDataProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return logieBorders == null && logieBridges == null && logieRoads == null;
    }

    public void merge(DynamicDataProperties other) {
        logieBorders = ofNullable(this.logieBorders).orElse(other.logieBorders);
        logieBridges = ofNullable(this.logieBridges).orElse(other.logieBridges);
        logieRoads = ofNullable(this.logieRoads).orElse(other.logieRoads);
    }

    public List<String> getEnabledDynamicDatasets() {
        List<String> res = new ArrayList<>();
        if (Boolean.TRUE.equals(logieBorders))
            res.add(LogieBorders.KEY);
        if (Boolean.TRUE.equals(logieBridges))
            res.add(LogieBridges.KEY);
        if (Boolean.TRUE.equals(logieRoads))
            res.add(LogieRoads.KEY);
        return res;
    }
}


