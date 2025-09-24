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
public class EncodedValuesProperties {
    @JsonProperty("ford")
    private Boolean ford;
    @JsonProperty("highway")
    private Boolean highway;
    @JsonProperty("osm_way_id")
    private Boolean osmWayId;
    @JsonProperty("way_surface")
    private Boolean waySurface;
    @JsonProperty("way_type")
    private Boolean wayType;

    public EncodedValuesProperties() {
    }

    public EncodedValuesProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return osmWayId == null && ford == null && highway == null && waySurface == null && wayType == null;
    }

    @JsonIgnore
    public String toString() {
        List<String> out = new ArrayList<>();
        if (Boolean.TRUE.equals(ford)) {
            out.add(Ford.KEY);
        }
        if (Boolean.TRUE.equals(highway)) {
            out.add(Highway.KEY);
        }
        if (Boolean.TRUE.equals(osmWayId)) {
            out.add(OsmWayId.KEY);
        }
        if (Boolean.TRUE.equals(waySurface)) {
            out.add(WaySurface.KEY);
        }
        if (Boolean.TRUE.equals(wayType)) {
            out.add(WayType.KEY);
        }
        return String.join(",", out);
    }

    public void merge(EncodedValuesProperties other) {
        waySurface = ofNullable(this.waySurface).orElse(other.waySurface);
        wayType = ofNullable(this.wayType).orElse(other.wayType);
    }
}


