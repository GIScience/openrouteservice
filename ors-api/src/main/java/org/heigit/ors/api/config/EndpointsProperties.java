package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonIgnore
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
        @JsonProperty("attribution")
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
        @JsonProperty("gpx_name")
        private String gpxName;
        @JsonProperty("gpx_description")
        private String gpxDescription;
        @JsonProperty("gpx_base_url")
        private String gpxBaseUrl;
        @JsonProperty("gpx_support_mail")
        private String gpxSupportMail;
        @JsonProperty("gpx_author")
        private String gpxAuthor;
        @JsonProperty("gpx_content_licence")
        private String gpxContentLicence;
        @JsonProperty("maximum_avoid_polygon_area")
        private double maximumAvoidPolygonArea;
        @JsonProperty("maximum_avoid_polygon_extent")
        private double maximumAvoidPolygonExtent;
        @JsonProperty("maximum_alternative_routes")
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
        @JsonProperty("maximum_routes")
        private int maximumRoutes = 2500;
        @JsonProperty("maximum_routes_flexible")
        private int maximumRoutesFlexible = 25;
        @JsonProperty("maximum_visited_nodes")
        private int maximumVisitedNodes = 100000;
        @JsonProperty("maximum_search_radius")
        private double maximumSearchRadius = 2000;
        // TODO: this parameter is only used in a binary check for infinity (==-1);
        //       Can't we reduce it to a boolean "forbid_u_turns"?
        @JsonProperty("u_turn_costs")
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

        @JsonProperty("u_turn_costs")
        public double getUTurnCost() {
            return uTurnCost;
        }

        public void setUTurnCost(double uTurnCosts) {
            this.uTurnCost = uTurnCosts;
        }
    }

    public static class MaximumRangeProperties {
        @JsonProperty("maximum_range_distance_default")
        private int maximumRangeDistanceDefault;
        @JsonProperty("maximum_range_distance")
        private List<MaximumRangeProperties.MaximumRangePropertiesEntry> maximumRangeDistance;
        @JsonProperty("maximum_range_time_default")
        private int maximumRangeTimeDefault;
        @JsonProperty("maximum_range_time")
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

        @JsonIgnore
        private final Map<Integer, Integer> profileMaxRangeDistances = new HashMap<>();

        public Map<Integer, Integer> getProfileMaxRangeDistances() {
            return profileMaxRangeDistances;
        }

        @JsonIgnore
        private final Map<Integer, Integer> profileMaxRangeTimes = new HashMap<>();

        public Map<Integer, Integer> getProfileMaxRangeTimes() {
            return profileMaxRangeTimes;
        }

        public static class MaximumRangePropertiesEntry {
            @JsonSerialize(using = InlineArraySerializer.class)
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

    @JsonPropertyOrder({"enabled", "attribution", "maximum_locations", "maximum_intervals", "allow_compute_area", "maximum_range_distance_default", "maximum_range_distance", "maximum_range_time_default", "maximum_range_time", "fastisochrones"})
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyMapFilter.class)
    public static class EndpointIsochronesProperties extends MaximumRangeProperties {
        private boolean enabled;
        private String attribution;
        @JsonProperty("maximum_locations")
        private int maximumLocations;
        @JsonProperty("allow_compute_area")
        private boolean allowComputeArea = true;
        @JsonProperty("maximum_intervals")
        private int maximumIntervals = 1;
        @JsonProperty("fastisochornes")
        private MaximumRangeProperties fastisochrones;
        @JsonProperty("statistics_providers")
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

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class StatisticsProviderProperties {
            private Boolean enabled;
            private String attribution;
            @JsonProperty("provider_name")
            private String providerName;
            @JsonProperty("provider_parameters")
            private ProviderParametersProperties providerParameters;
            @JsonProperty("property_mapping")
            private Map<String, String> propertyMapping;

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

            public ProviderParametersProperties getProviderParameters() {
                return providerParameters;
            }

            public void setProviderParameters(ProviderParametersProperties providerParameters) {
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

        public static class ProviderParametersProperties {
            @JsonProperty("host")
            private String host;
            @JsonProperty("port")
            private Integer port;
            @JsonProperty("user")
            private String user;
            @JsonProperty("password")
            private String password;
            @JsonProperty("db_name")
            private String dbName;
            @JsonProperty("table_name")
            private String tableName;
            @JsonProperty("geometry_column")
            private String geometryColumn;
            @JsonProperty("postgis_version")
            private String postgisVersion;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public Integer getPort() {
                return port;
            }

            public void setPort(Integer port) {
                this.port = port;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getDbName() {
                return dbName;
            }

            public void setDbName(String dbName) {
                this.dbName = dbName;
            }

            public String getTableName() {
                return tableName;
            }

            public void setTableName(String tableName) {
                this.tableName = tableName;
            }

            public String getGeometryColumn() {
                return geometryColumn;
            }

            public void setGeometryColumn(String geometryColumn) {
                this.geometryColumn = geometryColumn;
            }

            public String getPostgisVersion() {
                return postgisVersion;
            }

            public void setPostgisVersion(String postgisVersion) {
                this.postgisVersion = postgisVersion;
            }
        }
    }

    public static class EndpointSnapProperties {
        private boolean enabled;
        private String attribution;
        @JsonProperty("maximum_locations")
        private int maximumLocations;

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
    }
}
