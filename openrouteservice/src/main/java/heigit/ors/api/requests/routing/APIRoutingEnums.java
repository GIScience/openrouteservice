package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.UnknownParameterValueException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static heigit.ors.routing.RoutingErrorCodes.INVALID_PARAMETER_VALUE;

public class APIRoutingEnums {
    @ApiModel(value = "Specify which type of border crossing to avoid")
    public enum AvoidBorders {
        ALL ("all"),
        CONTROLLED ("controlled"),
        NONE ("none");

        private final String value;

        AvoidBorders(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AvoidBorders forValue(String v) throws ParameterValueException {
            try {
                return AvoidBorders.valueOf(v);
            } catch (Exception e) {
                throw new ParameterValueException(INVALID_PARAMETER_VALUE, "avoid_borders", v);
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /*@ApiModel(value = "Specify which extra info items to include in the response")
    public enum ExtraInfo {
        @JsonProperty("steepness") STEEPNESS { public String toString() { return "steepness"; }},
        @JsonProperty("surface") SUITABILITY { public String toString() { return "suitability"; }},
        @JsonProperty("surface") SURFACE { public String toString() { return "surface"; }},
        @JsonProperty("waycategory") WAY_CATEGORY { public String toString() { return "waycategory"; }},
        @JsonProperty("waytype") WAY_TYPE { public String toString() { return "waytype"; }},
        @JsonProperty("tollways") TOLLWAYS { public String toString() { return "tollways"; }},
        @JsonProperty("traildifficulty") TRAIL_DIFFICULTY { public String toString() { return "traildifficulty"; }},
        @JsonProperty("osmid") OSM_ID { public String toString() { return "osmid"; }};
    }*/

    @ApiModel(value = "Specify which extra info items to include in the response")
    public enum ExtraInfo {
        STEEPNESS ("steepness"),
        SUITABILITY ("suitability"),
        SURFACE ("surface"),
        WAY_CATEGORY ("waycategory"),
        WAY_TYPE ("waytype"),
        TOLLWAYS ("tollways"),
        TRAIL_DIFFICULTY ("traildifficulty"),
        OSM_ID ("osmid");

        private final String value;

        static private final String parameterName = "extra_info";

        ExtraInfo(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtraInfo forValue(String v) throws ParameterValueException {
            try {
                return ExtraInfo.valueOf(v);
            } catch (Exception e) {
                throw new ParameterValueException(INVALID_PARAMETER_VALUE, parameterName, v);
            }
        }

        @Override
        public String toString() {
            return value;
        }

    }

    /*@ApiModel(value = "Specify which extra info items to include in the response")
    public enum ExtraInfo {
        @JsonProperty("steepness") STEEPNESS ("steepness"),
        @JsonProperty("surface") SUITABILITY ("suitability"),
        @JsonProperty("surface") SURFACE ("surface"),
        @JsonProperty("waycategory") WAY_CATEGORY ("waycategory"),
        @JsonProperty("waytype") WAY_TYPE ("waytype"),
        @JsonProperty("tollways") TOLLWAYS ("tollways"),
        @JsonProperty("traildifficulty") TRAIL_DIFFICULTY ("traildifficulty"),
        @JsonProperty("osmid") OSM_ID ("osmid");

        private static Map<String, ExtraInfo> FORMAT_MAP = Stream
                .of(ExtraInfo.values())
                .collect(Collectors.toMap(s -> s.formatted, Function.identity()));
        private final String formatted;

        ExtraInfo(String formatted) {
            this.formatted = formatted;
        }

        @JsonCreator
        public static ExtraInfo fromString(String string) throws UnknownParameterValueException {
            return Optional
                    .ofNullable(FORMAT_MAP.get(string))
                    .orElseThrow(() -> new UnknownParameterValueException(1, "extra_info", string));
        }
    }*/

    @ApiModel
    public enum RouteResponseGeometryType {
        GEOJSON ("geojson"),
        ENCODED_POLYLINE ("encodedpolyline"),
        GPX ("gpx");

        private final String value;

        RouteResponseGeometryType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RouteResponseGeometryType forValue(String v) throws ParameterValueException {
            try {
                return RouteResponseGeometryType.valueOf(v);
            } catch (Exception e) {
                throw new ParameterValueException(INVALID_PARAMETER_VALUE, "geometry_format", v);
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @ApiModel
    public enum RouteResponseType {
        GPX  ("gpx"),
        JSON  ("json"),
        GEOJSON  ("geojson");

        private final String value;

        RouteResponseType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RouteResponseType forValue(String v) throws ParameterValueException {
            try {
                return RouteResponseType.valueOf(v);
            } catch (Exception e) {
                throw new ParameterValueException(INVALID_PARAMETER_VALUE, "geometry_format", v);
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @ApiModel
    public enum VehicleType {
        HGV ("hgv"),
        BUS ("bus"),
        AGRICULTURAL ("agricultural"),
        DELIVERY ("delivery"),
        FORESTRY ("forestry"),
        GOODS ("goods"),
        @JsonIgnore UNKNOWN ("unknown");

        private final String value;

        VehicleType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static VehicleType forValue(String v) throws ParameterValueException {
            try {
                return VehicleType.valueOf(v);
            } catch (Exception e) {
                throw new ParameterValueException(INVALID_PARAMETER_VALUE, "geometry_format", v);
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @ApiModel
    public enum AvoidFeatures {
        HIGHWAYS { public String toString() { return "highways"; }},
        TOLLWAYS { public String toString() { return "tollways"; }},
        FERRIES { public String toString() { return "ferries"; }},
        TUNNELS { public String toString() { return "tunnels"; }},
        PAVED_ROADS { public String toString() { return "pavedroads"; }},
        UNPAVED_ROADS { public String toString() { return "unpavedroads"; }},
        TRACKS { public String toString() { return "tracks"; }},
        FORDS { public String toString() { return "fords"; }},
        STEPS { public String toString() { return "steps"; }},
        HILLS { public String toString() { return "hills"; }}
    }

    @ApiModel(value = "Preference", description = "Specifies the route preference")
    public enum RoutePreference {
        @JsonProperty("fastest") FASTEST { public String toString() { return "fastest"; }},
        @JsonProperty("shortest") SHORTEST { public String toString() { return "shortest"; }},
        @JsonProperty("recommended") RECOMMENDED { public String toString() { return "recommended"; }}
    }

    public enum RoutingProfile {
        @JsonProperty("driving-car") DRIVING_CAR { public String toString() { return "driving-car"; }},
        @JsonProperty("driving-car") DRIVING_HGV { public String toString() { return "driving-car"; }},
        @JsonProperty("cycling-regular") CYCLING_REGULAR { public String toString() { return "cycling-regular"; }},
        @JsonProperty("cycling-road") CYCLING_ROAD { public String toString() { return "cycling-road"; }},
        @JsonProperty("cycling-safe") CYCLING_SAFE { public String toString() { return "cycling-safe"; }},
        @JsonProperty("cycling-mountain") CYCLING_MOUNTAIN { public String toString() { return "cycling-mountain"; }},
        @JsonProperty("cycling-tour") CYCLING_TOUR { public String toString() { return "cycling-tour"; }},
        @JsonProperty("cycling-electric") CYCLING_ELECTRIC { public String toString() { return "cycling-electric"; }},
        @JsonProperty("foot-walking") FOOT_WALKING { public String toString() { return "foot-walking"; }},
        @JsonProperty("foot-hiking") FOOT_HIKING { public String toString() { return "foot-hiking"; }},
        @JsonProperty("wheelchair") WHEELCHAIR { public String toString() { return "wheelchair"; }},
    }

    public enum Units {
        @JsonProperty("m") METRES { public String toString() { return "m"; }},
        @JsonProperty("km") KILOMETRES { public String toString() { return "km"; }},
        @JsonProperty("mi") MILES { public String toString() { return "mi"; }}
    }

    public enum Languages {
        @JsonProperty("en") EN { public String toString() { return "en"; }},
        @JsonProperty("cn") CN { public String toString() { return "cn"; }},
        @JsonProperty("de") DE { public String toString() { return "de"; }},
        @JsonProperty("es") ES { public String toString() { return "es"; }},
        @JsonProperty("ru") RU { public String toString() { return "ru"; }},
        @JsonProperty("dk") DK { public String toString() { return "dk"; }},
        @JsonProperty("fr") FR { public String toString() { return "fr"; }},
        @JsonProperty("it") IT { public String toString() { return "it"; }},
        @JsonProperty("nl") NL { public String toString() { return "nl"; }},
        @JsonProperty("br") BR { public String toString() { return "br"; }},
        @JsonProperty("se") SE { public String toString() { return "se"; }},
        @JsonProperty("tr") TR { public String toString() { return "tr"; }},
        @JsonProperty("gr") GR { public String toString() { return "gr"; }}
    }

    public enum InstructionsFormat {
        @JsonProperty("html") HTML { public String toString() { return "html"; }},
        @JsonProperty("text") TEXT { public String toString() { return "text"; }}
    }

    public enum Attributes {
        @JsonProperty("avgspeed") AVERAGE_SPEED { public String toString() { return "avgspeed"; }},
        @JsonProperty("detourfactor") DETOUR_FACTOR { public String toString() { return "detourfactor"; }},
        @JsonProperty("percentage") ROUTE_PERCENTAGE { public String toString() { return "percentage"; }}
    }
}
