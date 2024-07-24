package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.config.utils.NonEmptyMapFilter;
import org.heigit.ors.routing.RoutingProfileType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;

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
    @JsonIgnore
    private String swaggerDocumentationUrl;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointDefaultProperties {
        @JsonProperty("attribution")
        private String attribution;
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
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
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
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

        public int getMaximumRoutes(boolean flexible) {
            return (flexible ? maximumRoutesFlexible : maximumRoutes);
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

        @Getter
        @Setter(AccessLevel.PACKAGE)
        public static class MaximumRangePropertiesEntry {
            @JsonSerialize(using = InlineArraySerializer.class)
            private List<String> profiles;
            private int value;

        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
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

        @Getter
        @Setter(AccessLevel.PACKAGE)
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
        }

        @Getter
        @Setter(AccessLevel.PACKAGE)
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

        }
    }

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class EndpointSnapProperties {
        private boolean enabled;
        private String attribution;
        @JsonProperty("maximum_locations")
        private int maximumLocations;
    }
}
