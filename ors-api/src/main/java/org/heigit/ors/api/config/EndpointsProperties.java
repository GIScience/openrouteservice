package org.heigit.ors.api.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.routing.RoutingProfileType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter(AccessLevel.PACKAGE)
@Configuration
@ConfigurationProperties(prefix = "ors.endpoints")
public class EndpointsProperties {
    private EndpointDefaultProperties defaults;
    private EndpointRoutingProperties routing;
    private EndpointMatrixProperties matrix;
    private EndpointIsochronesProperties isochrones;
    private EndpointSnapProperties snap;
    private EndpointMatchProperties match;
    private String swaggerDocumentationUrl;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointDefaultProperties {
        private String attribution;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointRoutingProperties {
        private boolean enabled;
        private String attribution;
        private String gpxName;
        private String gpxDescription;
        private String gpxBaseUrl;
        private String gpxSupportMail;
        private String gpxAuthor;
        private String gpxContentLicence;
        private double maximumAvoidPolygonArea;
        private double maximumAvoidPolygonExtent;
        private int maximumAlternativeRoutes;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointMatrixProperties {
        private boolean enabled;
        private String attribution;
        private int maximumRoutes;
        private int maximumRoutesFlexible;
        private int maximumVisitedNodes;
        private double maximumSearchRadius;
        private double uTurnCosts;

        public int getMaximumRoutes(boolean flexible) {
            return (flexible ? maximumRoutesFlexible : maximumRoutes);
        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class MaximumRangeProperties {
        private int maximumRangeDistanceDefault;
        private List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeDistance;
        private int maximumRangeTimeDefault;
        private List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeTime;

        public void setMaximumRangeDistance(List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeDistance) {
            this.maximumRangeDistance = maximumRangeDistance;
            for (MaximumRangeProperties.MaximumRangePropertiesEntry maximumRangePropertiesEntry : maximumRangeDistance)
                for (String profile : maximumRangePropertiesEntry.getProfiles())
                    profileMaxRangeDistances.put(RoutingProfileType.getFromString(profile), maximumRangePropertiesEntry.getValue());
        }

        public void setMaximumRangeTime(List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeTime) {
            this.maximumRangeTime = maximumRangeTime;
            for (MaximumRangeProperties.MaximumRangePropertiesEntry maximumRangePropertiesEntry : maximumRangeTime)
                for (String profile : maximumRangePropertiesEntry.getProfiles())
                    profileMaxRangeTimes.put(RoutingProfileType.getFromString(profile), maximumRangePropertiesEntry.getValue());
        }

        private final Map<Integer, Integer> profileMaxRangeDistances = new HashMap<>();
        private final Map<Integer, Integer> profileMaxRangeTimes = new HashMap<>();

        @Getter
        @Setter(AccessLevel.PACKAGE)
        public static class MaximumRangePropertiesEntry {
            private List<String> profiles;
            private int value;

        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointIsochronesProperties extends MaximumRangeProperties {
        private boolean enabled;
        private String attribution;
        private int maximumLocations;
        private boolean allowComputeArea;
        private int maximumIntervals;
        private MaximumRangeProperties fastisochrones;
        private Map<String, StatisticsProviderProperties> statisticsProviders = new HashMap<>();

        @Getter
        @Setter(AccessLevel.PACKAGE)
        public static class StatisticsProviderProperties {
            private Boolean enabled;
            private String attribution;
            private String providerName;
            private ProviderParametersProperties providerParameters;
            private Map<String, String> propertyMapping;
        }

        @Getter
        @Setter(AccessLevel.PACKAGE)
        public static class ProviderParametersProperties {
            private String host;
            private Integer port;
            private String user;
            private String password;
            private String dbName;
            private String tableName;
            private String geometryColumn;
            private String postgisVersion;

        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointSnapProperties {
        private boolean enabled;
        private String attribution;
        private int maximumLocations;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointMatchProperties {
        private boolean enabled;
        private int maximumSearchRadius;
        private String attribution;
    }
}
