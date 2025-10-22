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
    @JsonProperty("road_environment")
    private Boolean roadEnvironment;
    @JsonProperty("agricultural_access")
    private Boolean agriculturalAccess;
    @JsonProperty("bus_access")
    private Boolean busAccess;
    @JsonProperty("delivery_access")
    private Boolean deliveryAccess;
    @JsonProperty("forestry_access")
    private Boolean forestryAccess;
    @JsonProperty("goods_access")
    private Boolean goodsAccess;
    @JsonProperty("hgv_access")
    private Boolean hgvAccess;
    @JsonProperty("hazmat_access")
    private Boolean hazmatAccess;
    @JsonProperty("max_axle_load")
    private Boolean maxAxleLoad;
    @JsonProperty("max_height")
    private Boolean maxHeight;
    @JsonProperty("max_length")
    private Boolean maxLength;
    @JsonProperty("max_weight")
    private Boolean maxWeight;
    @JsonProperty("max_width")
    private Boolean maxWidth;

    public EncodedValuesProperties() {
    }

    public EncodedValuesProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return osmWayId == null &&
                ford == null && highway == null &&
                waySurface == null && wayType == null &&
                roadEnvironment == null &&
                agriculturalAccess == null && busAccess == null && deliveryAccess == null && forestryAccess == null && goodsAccess == null && hgvAccess == null &&
                hazmatAccess == null &&
                maxAxleLoad == null && maxHeight == null && maxLength == null && maxWeight == null && maxWidth == null;
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
        if (Boolean.TRUE.equals(roadEnvironment)) {
            out.add(RoadEnvironment.KEY);
        }
        if (Boolean.TRUE.equals(agriculturalAccess)) {
            out.add(AgriculturalAccess.KEY);
        }
        if (Boolean.TRUE.equals(busAccess)) {
            out.add(BusAccess.KEY);
        }
        if (Boolean.TRUE.equals(deliveryAccess)) {
            out.add(DeliveryAccess.KEY);
        }
        if (Boolean.TRUE.equals(forestryAccess)) {
            out.add(ForestryAccess.KEY);
        }
        if (Boolean.TRUE.equals(goodsAccess)) {
            out.add(GoodsAccess.KEY);
        }
        if (Boolean.TRUE.equals(hgvAccess)) {
            out.add(HgvAccess.KEY);
        }
        if (Boolean.TRUE.equals(hazmatAccess)) {
            out.add(HazmatAccess.KEY);
        }
        if (Boolean.TRUE.equals(maxAxleLoad)) {
            out.add(MaxAxleLoad.KEY);
        }
        if (Boolean.TRUE.equals(maxHeight)) {
            out.add(MaxHeight.KEY);
        }
        if (Boolean.TRUE.equals(maxLength)) {
            out.add(MaxLength.KEY);
        }
        if (Boolean.TRUE.equals(maxWeight)) {
            out.add(MaxWeight.KEY);
        }
        if (Boolean.TRUE.equals(maxWidth)) {
            out.add(MaxWidth.KEY);
        }
        return String.join(",", out);
    }

    public void merge(EncodedValuesProperties other) {
        ford = ofNullable(this.ford).orElse(other.ford);
        highway = ofNullable(this.highway).orElse(other.highway);
        osmWayId = ofNullable(this.osmWayId).orElse(other.osmWayId);
        waySurface = ofNullable(this.waySurface).orElse(other.waySurface);
        wayType = ofNullable(this.wayType).orElse(other.wayType);
        roadEnvironment = ofNullable(this.roadEnvironment).orElse(other.roadEnvironment);
        agriculturalAccess = ofNullable(this.agriculturalAccess).orElse(other.agriculturalAccess);
        busAccess = ofNullable(this.busAccess).orElse(other.busAccess);
        deliveryAccess = ofNullable(this.deliveryAccess).orElse(other.deliveryAccess);
        forestryAccess = ofNullable(this.forestryAccess).orElse(other.forestryAccess);
        goodsAccess = ofNullable(this.goodsAccess).orElse(other.goodsAccess);
        hgvAccess = ofNullable(this.hgvAccess).orElse(other.hgvAccess);
        hazmatAccess = ofNullable(this.hazmatAccess).orElse(other.hazmatAccess);
        maxAxleLoad = ofNullable(this.maxAxleLoad).orElse(other.maxAxleLoad);
        maxHeight = ofNullable(this.maxHeight).orElse(other.maxHeight);
        maxLength = ofNullable(this.maxLength).orElse(other.maxLength);
        maxWeight = ofNullable(this.maxWeight).orElse(other.maxWeight);
        maxWidth = ofNullable(this.maxWidth).orElse(other.maxWidth);
    }
}


