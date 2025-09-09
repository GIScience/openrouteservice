package org.heigit.ors.config.profile;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static java.util.Optional.ofNullable;

@Getter
@Setter
public class ServiceProperties {
    private Double maximumDistance;
    private Double maximumDistanceDynamicWeights;
    private Double maximumDistanceAvoidAreas;
    private Double maximumDistanceAlternativeRoutes;
    private Double maximumDistanceRoundTripRoutes;
    private Integer maximumWayPoints;
    private Integer maximumSnappingRadius;
    private Integer maximumVisitedNodes;
    private Boolean forceTurnCosts;
    private Boolean allowCustomModels;
    private ExecutionProperties execution = new ExecutionProperties();
    @JsonProperty("dynamic_data")
    private DynamicDataProperties dynamicData = new DynamicDataProperties();

    public ServiceProperties() {
    }

    public ServiceProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    public void merge(ServiceProperties other) {
        maximumDistance = ofNullable(this.maximumDistance).orElse(other.maximumDistance);
        maximumDistanceDynamicWeights = ofNullable(this.maximumDistanceDynamicWeights).orElse(other.maximumDistanceDynamicWeights);
        maximumDistanceAvoidAreas = ofNullable(this.maximumDistanceAvoidAreas).orElse(other.maximumDistanceAvoidAreas);
        maximumDistanceAlternativeRoutes = ofNullable(this.maximumDistanceAlternativeRoutes).orElse(other.maximumDistanceAlternativeRoutes);
        maximumDistanceRoundTripRoutes = ofNullable(this.maximumDistanceRoundTripRoutes).orElse(other.maximumDistanceRoundTripRoutes);
        maximumWayPoints = ofNullable(this.maximumWayPoints).orElse(other.maximumWayPoints);
        maximumSnappingRadius = ofNullable(this.maximumSnappingRadius).orElse(other.maximumSnappingRadius);
        maximumVisitedNodes = ofNullable(this.maximumVisitedNodes).orElse(other.maximumVisitedNodes);
        forceTurnCosts = ofNullable(forceTurnCosts).orElse(other.forceTurnCosts);
        allowCustomModels = ofNullable(allowCustomModels).orElse(other.allowCustomModels);
        execution.merge(other.execution);
        dynamicData.merge(other.dynamicData);
    }
}
