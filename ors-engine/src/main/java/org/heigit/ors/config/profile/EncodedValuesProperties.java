package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.routing.ev.LogieBorders;
import com.graphhopper.routing.ev.WaySurface;
import com.graphhopper.routing.ev.WayType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncodedValuesProperties {
    @JsonProperty("way_surface")
    private Boolean waySurface;
    @JsonProperty("way_type")
    private Boolean wayType;
    @JsonProperty("logie_borders")
    private Boolean logieBorders;

    public EncodedValuesProperties() {
    }

    public EncodedValuesProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return waySurface == null && wayType == null && logieBorders == null;
    }

    @JsonIgnore
    public String toString() {
        List<String> out = new ArrayList<>();
        if (Boolean.TRUE.equals(waySurface)) {
            out.add(WaySurface.KEY);
        }
        if (Boolean.TRUE.equals(wayType)) {
            out.add(WayType.KEY);
        }
        if (Boolean.TRUE.equals(logieBorders)) {
            out.add(LogieBorders.KEY);
        }
        return String.join(",", out);
    }

    public void merge(EncodedValuesProperties other) {
        waySurface = ofNullable(this.waySurface).orElse(other.waySurface);
        wayType = ofNullable(this.wayType).orElse(other.wayType);
    }
}


