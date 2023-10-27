package org.heigit.ors.api;

import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1ProfileProperties;

import java.util.Objects;

import static java.util.Optional.ofNullable;

public class ORSGraphInfoV1ProfilePropertiesBuilder {

    private ORSGraphInfoV1ProfileProperties orsGraphInfoV1ProfileProperties;

    private ORSGraphInfoV1ProfilePropertiesBuilder() {}

    public static ORSGraphInfoV1ProfilePropertiesBuilder from(EngineProperties.ProfileProperties profileProperties) {
        Objects.requireNonNull(profileProperties);
        ORSGraphInfoV1ProfilePropertiesBuilder builder = new ORSGraphInfoV1ProfilePropertiesBuilder();
        builder.orsGraphInfoV1ProfileProperties = profileProperties.asORSGraphInfoV1ProfileProperties();
        return builder;
    }

    public ORSGraphInfoV1ProfilePropertiesBuilder overrideWith(EngineProperties.ProfileProperties profileProperties) {
        Objects.requireNonNull(profileProperties);
        ORSGraphInfoV1ProfileProperties overrideProps = profileProperties.asORSGraphInfoV1ProfileProperties();
        orsGraphInfoV1ProfileProperties = new ORSGraphInfoV1ProfileProperties(
                ofNullable(overrideProps.profile()).orElse(orsGraphInfoV1ProfileProperties.profile()),
                ofNullable(overrideProps.enabled()).orElse(orsGraphInfoV1ProfileProperties.enabled()),
                ofNullable(overrideProps.elevation()).orElse(orsGraphInfoV1ProfileProperties.elevation()),
                ofNullable(overrideProps.elevationSmoothing()).orElse(orsGraphInfoV1ProfileProperties.elevationSmoothing()),
                ofNullable(overrideProps.traffic()).orElse(orsGraphInfoV1ProfileProperties.traffic()),
                ofNullable(overrideProps.interpolateBridgesAndTunnels()).orElse(orsGraphInfoV1ProfileProperties.interpolateBridgesAndTunnels()),
                ofNullable(overrideProps.instructions()).orElse(orsGraphInfoV1ProfileProperties.instructions()),
                ofNullable(overrideProps.optimize()).orElse(orsGraphInfoV1ProfileProperties.optimize()),
                ofNullable(overrideProps.graphPath()).orElse(orsGraphInfoV1ProfileProperties.graphPath()),
                ofNullable(overrideProps.encoderOptions()).orElse(orsGraphInfoV1ProfileProperties.encoderOptions()),
                ofNullable(overrideProps.preparation()).orElse(orsGraphInfoV1ProfileProperties.preparation()),
                ofNullable(overrideProps.execution()).orElse(orsGraphInfoV1ProfileProperties.execution()),
                ofNullable(overrideProps.extStorages()).orElse(orsGraphInfoV1ProfileProperties.extStorages()),
                ofNullable(overrideProps.maximumDistance()).orElse(orsGraphInfoV1ProfileProperties.maximumDistance()),
                ofNullable(overrideProps.maximumDistanceDynamicWeights()).orElse(orsGraphInfoV1ProfileProperties.maximumDistanceDynamicWeights()),
                ofNullable(overrideProps.maximumDistanceAvoidAreas()).orElse(orsGraphInfoV1ProfileProperties.maximumDistanceAvoidAreas()),
                ofNullable(overrideProps.maximumDistanceAlternativeRoutes()).orElse(orsGraphInfoV1ProfileProperties.maximumDistanceAlternativeRoutes()),
                ofNullable(overrideProps.maximumDistanceRoundTripRoutes()).orElse(orsGraphInfoV1ProfileProperties.maximumDistanceRoundTripRoutes()),
                ofNullable(overrideProps.maximumSpeedLowerBound()).orElse(orsGraphInfoV1ProfileProperties.maximumSpeedLowerBound()),
                ofNullable(overrideProps.maximumWayPoints()).orElse(orsGraphInfoV1ProfileProperties.maximumWayPoints()),
                ofNullable(overrideProps.maximumSnappingRadius()).orElse(orsGraphInfoV1ProfileProperties.maximumSnappingRadius()),
                ofNullable(overrideProps.maximumVisitedNodes()).orElse(orsGraphInfoV1ProfileProperties.maximumVisitedNodes()),
                ofNullable(overrideProps.encoderFlagsSize()).orElse(orsGraphInfoV1ProfileProperties.encoderFlagsSize()),
                ofNullable(overrideProps.locationIndexResolution()).orElse(orsGraphInfoV1ProfileProperties.locationIndexResolution()),
                ofNullable(overrideProps.locationIndexSearchIterations()).orElse(orsGraphInfoV1ProfileProperties.locationIndexSearchIterations()),
                ofNullable(overrideProps.forceTurnCosts()).orElse(orsGraphInfoV1ProfileProperties.forceTurnCosts()),
                ofNullable(overrideProps.gtfsFile()).orElse(orsGraphInfoV1ProfileProperties.gtfsFile())
                );

        return this;
    }

    public ORSGraphInfoV1ProfileProperties build() {
        return orsGraphInfoV1ProfileProperties;
    }
}
