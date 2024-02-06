package org.heigit.ors.api;

import org.heigit.ors.routing.RoutingProfileType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;

@Configuration
@ConfigurationProperties(prefix = "ors.endpoints")
public class EndpointsProperties {
    private EndpointDefaultProperties defaults;
    private EndpointRoutingProperties routing;
    private EndpointMatrixProperties matrix;
    private EndpointIsochronesProperties isochrones;
    private EndpointSnapProperties snap;
    private String swaggerDocumentationUrl;

    public void setSwaggerDocumentationUrl(String swaggerDocumentationUrl) {
        this.swaggerDocumentationUrl = swaggerDocumentationUrl;
    }

    public String getSwaggerDocumentationUrl() {
        return swaggerDocumentationUrl;
    }

    public EndpointDefaultProperties getDefaults() {
        return defaults;
    }

    public void setDefaults(EndpointDefaultProperties defaults) {
        this.defaults = defaults;
    }

    public EndpointRoutingProperties getRouting() {
        return routing;
    }

    public void setRouting(EndpointRoutingProperties routing) {
        this.routing = routing;
    }

    public EndpointMatrixProperties getMatrix() {
        return matrix;
    }

    public void setMatrix(EndpointMatrixProperties matrix) {
        this.matrix = matrix;
    }

    public EndpointIsochronesProperties getIsochrones() {
        return isochrones;
    }

    public void setIsochrones(EndpointIsochronesProperties isochrones) {
        this.isochrones = isochrones;
    }

    public EndpointSnapProperties getSnap() {
        return snap;
    }

    public void setSnap(EndpointSnapProperties snap) {
        this.snap = snap;
    }

    public static class EndpointDefaultProperties {
        private String attribution;

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }
    }

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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public String getGpxName() {
            return gpxName;
        }

        public void setGpxName(String gpxName) {
            this.gpxName = gpxName;
        }

        public String getGpxDescription() {
            return gpxDescription;
        }

        public void setGpxDescription(String gpxDescription) {
            this.gpxDescription = gpxDescription;
        }

        public String getGpxBaseUrl() {
            return gpxBaseUrl;
        }

        public void setGpxBaseUrl(String gpxBaseUrl) {
            this.gpxBaseUrl = gpxBaseUrl;
        }

        public String getGpxSupportMail() {
            return gpxSupportMail;
        }

        public void setGpxSupportMail(String gpxSupportMail) {
            this.gpxSupportMail = gpxSupportMail;
        }

        public String getGpxAuthor() {
            return gpxAuthor;
        }

        public void setGpxAuthor(String gpxAuthor) {
            this.gpxAuthor = gpxAuthor;
        }

        public String getGpxContentLicence() {
            return gpxContentLicence;
        }

        public void setGpxContentLicence(String gpxContentLicence) {
            this.gpxContentLicence = gpxContentLicence;
        }

        public double getMaximumAvoidPolygonArea() {
            return maximumAvoidPolygonArea;
        }

        public void setMaximumAvoidPolygonArea(double maximumAvoidPolygonArea) {
            this.maximumAvoidPolygonArea = maximumAvoidPolygonArea;
        }

        public double getMaximumAvoidPolygonExtent() {
            return maximumAvoidPolygonExtent;
        }

        public void setMaximumAvoidPolygonExtent(double maximumAvoidPolygonExtent) {
            this.maximumAvoidPolygonExtent = maximumAvoidPolygonExtent;
        }


        public int getMaximumAlternativeRoutes() {
            return maximumAlternativeRoutes;
        }

        public void setMaximumAlternativeRoutes(Integer maximumAlternativeRoutes) {
            this.maximumAlternativeRoutes = maximumAlternativeRoutes;
        }


    }

    public static class EndpointMatrixProperties {
        private boolean enabled;
        private String attribution;
        private int maximumRoutes = 2500;
        private int maximumRoutesFlexible = 25;
        private int maximumVisitedNodes = 100000;
        private double maximumSearchRadius = 2000;
        // TODO: this parameter is only used in a binary check for infinity (==-1);
        //       Can't we reduce it to a boolean "forbid_u_turns"?
        private double uTurnCost = INFINITE_U_TURN_COSTS;


        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public int getMaximumRoutes(boolean flexible) {
            return (flexible ? maximumRoutesFlexible : maximumRoutes);
        }

        public void setMaximumRoutes(int maximumRoutes) {
            this.maximumRoutes = maximumRoutes;
        }

        public void setMaximumRoutesFlexible(int maximumRoutesFlexible) {
            this.maximumRoutesFlexible = maximumRoutesFlexible;
        }

        public int getMaximumVisitedNodes() {
            return maximumVisitedNodes;
        }

        public void setMaximumVisitedNodes(int maximumVisitedNodes) {
            this.maximumVisitedNodes = maximumVisitedNodes;
        }

        public double getMaximumSearchRadius() {
            return maximumSearchRadius;
        }

        public void setMaximumSearchRadius(double maximumSearchRadius) {
            this.maximumSearchRadius = maximumSearchRadius;
        }

        public double getUTurnCost() {
            return uTurnCost;
        }

        public void setUTurnCost(double uTurnCosts) {
            this.uTurnCost = uTurnCosts;
        }
    }

    public static class MaximumRangeProperties {
        private int maximumRangeDistanceDefault;
        private List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeDistance;
        private int maximumRangeTimeDefault;
        private List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeTime;

        public int getMaximumRangeDistanceDefault() {
            return maximumRangeDistanceDefault;
        }

        public void setMaximumRangeDistanceDefault(int maximumRangeDistanceDefault) {
            this.maximumRangeDistanceDefault = maximumRangeDistanceDefault;
        }

        public List<MaximumRangeProperties.MaximumRangePropertiesEntry> getMaximumRangeDistance() {
            return maximumRangeDistance;
        }

        public void setMaximumRangeDistance(List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeDistance) {
            this.maximumRangeDistance = maximumRangeDistance;
            for (MaximumRangeProperties.MaximumRangePropertiesEntry maximumRangePropertiesEntry : maximumRangeDistance)
                for (String profile : maximumRangePropertiesEntry.getProfiles())
                    profileMaxRangeDistances.put(RoutingProfileType.getFromString(profile), maximumRangePropertiesEntry.getValue());
        }

        public int getMaximumRangeTimeDefault() {
            return maximumRangeTimeDefault;
        }

        public void setMaximumRangeTimeDefault(int maximumRangeTimeDefault) {
            this.maximumRangeTimeDefault = maximumRangeTimeDefault;
        }

        public List<MaximumRangeProperties.MaximumRangePropertiesEntry> getMaximumRangeTime() {
            return maximumRangeTime;
        }

        public void setMaximumRangeTime(List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeTime) {
            this.maximumRangeTime = maximumRangeTime;
            for (MaximumRangeProperties.MaximumRangePropertiesEntry maximumRangePropertiesEntry : maximumRangeTime)
                for (String profile : maximumRangePropertiesEntry.getProfiles())
                    profileMaxRangeTimes.put(RoutingProfileType.getFromString(profile), maximumRangePropertiesEntry.getValue());
        }

        private final Map<Integer, Integer> profileMaxRangeDistances = new HashMap<>();

        public Map<Integer, Integer> getProfileMaxRangeDistances() {
            return profileMaxRangeDistances;
        }

        private final Map<Integer, Integer> profileMaxRangeTimes = new HashMap<>();

        public Map<Integer, Integer> getProfileMaxRangeTimes() {
            return profileMaxRangeTimes;
        }

        public static class MaximumRangePropertiesEntry {
            private List<String> profiles;
            private int value;

            public List<String> getProfiles() {
                return profiles;
            }

            public void setProfiles(List<String> profiles) {
                this.profiles = profiles;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }

    public static class EndpointIsochronesProperties extends MaximumRangeProperties {
        private boolean enabled;
        private String attribution;
        private int maximumLocations;
        private boolean allowComputeArea = true;
        private int maximumIntervals = 1;
        private MaximumRangeProperties fastisochrones;
        private Map<String, StatisticsProviderProperties> statisticsProviders = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

        public int getMaximumLocations() {
            return maximumLocations;
        }

        public void setMaximumLocations(int maximumLocations) {
            this.maximumLocations = maximumLocations;
        }

        public boolean isAllowComputeArea() {
            return allowComputeArea;
        }

        public void setAllowComputeArea(boolean allowComputeArea) {
            this.allowComputeArea = allowComputeArea;
        }

        public int getMaximumIntervals() {
            return maximumIntervals;
        }

        public void setMaximumIntervals(int maximumIntervals) {
            this.maximumIntervals = maximumIntervals;
        }

        public MaximumRangeProperties getFastisochrones() {
            return fastisochrones;
        }

        public void setFastisochrones(MaximumRangeProperties fastisochrones) {
            this.fastisochrones = fastisochrones;
        }

        public Map<String, StatisticsProviderProperties> getStatisticsProviders() {
            return statisticsProviders;
        }

        public void setStatisticsProviders(Map<String, StatisticsProviderProperties> statisticsProviders) {
            this.statisticsProviders = statisticsProviders;
        }

        public static class StatisticsProviderProperties {
            private boolean enabled;
            private String providerName;
            private Map<String, Object> providerParameters;
            private Map<String, String> propertyMapping;
            private String attribution;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getProviderName() {
                return providerName;
            }

            public void setProviderName(String providerName) {
                this.providerName = providerName;
            }

            public Map<String, Object> getProviderParameters() {
                return providerParameters;
            }

            public void setProviderParameters(Map<String, Object> providerParameters) {
                this.providerParameters = providerParameters;
            }

            public Map<String, String> getPropertyMapping() {
                return propertyMapping;
            }

            public void setPropertyMapping(Map<String, String> propertyMapping) {
                this.propertyMapping = propertyMapping;
            }

            public String getAttribution() {
                return attribution;
            }

            public void setAttribution(String attribution) {
                this.attribution = attribution;
            }
        }
    }

    public static class EndpointSnapProperties {
        private boolean enabled;
        private String attribution;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAttribution() {
            return attribution;
        }

        public void setAttribution(String attribution) {
            this.attribution = attribution;
        }

    }
}
