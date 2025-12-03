package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.routing.ev.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncodedValuesProperties {
    @JsonProperty(Ford.KEY)
    private Boolean ford;
    @JsonProperty(Highway.KEY)
    private Boolean highway;
    @JsonProperty(OsmWayId.KEY)
    private Boolean osmWayId;
    @JsonProperty(WaySurface.KEY)
    private Boolean waySurface;
    @JsonProperty(WayType.KEY)
    private Boolean wayType;
    @JsonProperty(RoadEnvironment.KEY)
    private Boolean roadEnvironment;
    @JsonProperty(AgriculturalAccess.KEY)
    private Boolean agriculturalAccess;
    @JsonProperty(BusAccess.KEY)
    private Boolean busAccess;
    @JsonProperty(DeliveryAccess.KEY)
    private Boolean deliveryAccess;
    @JsonProperty(ForestryAccess.KEY)
    private Boolean forestryAccess;
    @JsonProperty(GoodsAccess.KEY)
    private Boolean goodsAccess;
    @JsonProperty(HgvAccess.KEY)
    private Boolean hgvAccess;
    @JsonProperty(HazmatAccess.KEY)
    private Boolean hazmatAccess;
    @JsonProperty(MaxAxleLoad.KEY)
    private Boolean maxAxleLoad;
    @JsonProperty(MaxHeight.KEY)
    private Boolean maxHeight;
    @JsonProperty(MaxLength.KEY)
    private Boolean maxLength;
    @JsonProperty(MaxWeight.KEY)
    private Boolean maxWeight;
    @JsonProperty(MaxWidth.KEY)
    private Boolean maxWidth;

    public EncodedValuesProperties() {
    }

    public EncodedValuesProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    private Map<String, Boolean> getProperties() {
        Map<String, Boolean> properties = new HashMap<>();

        properties.put(Ford.KEY, ford);
        properties.put(Highway.KEY, highway);
        properties.put(OsmWayId.KEY, osmWayId);
        properties.put(WaySurface.KEY, waySurface);
        properties.put(WayType.KEY, wayType);
        properties.put(RoadEnvironment.KEY, roadEnvironment);
        properties.put(AgriculturalAccess.KEY, agriculturalAccess);
        properties.put(BusAccess.KEY, busAccess);
        properties.put(DeliveryAccess.KEY, deliveryAccess);
        properties.put(ForestryAccess.KEY, forestryAccess);
        properties.put(GoodsAccess.KEY, goodsAccess);
        properties.put(HgvAccess.KEY, hgvAccess);
        properties.put(HazmatAccess.KEY, hazmatAccess);
        properties.put(MaxAxleLoad.KEY, maxAxleLoad);
        properties.put(MaxHeight.KEY, maxHeight);
        properties.put(MaxLength.KEY, maxLength);
        properties.put(MaxWeight.KEY, maxWeight);
        properties.put(MaxWidth.KEY, maxWidth);

        return properties;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return getProperties().values().stream().allMatch(Objects::isNull);
    }

    @JsonIgnore
    public String toString() {
        return getProperties().entrySet().stream()
                .filter(e -> Boolean.TRUE.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(","));
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
