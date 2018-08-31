package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
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
            for(AvoidBorders enumItem : AvoidBorders.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }

            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "avoid_borders", v);
        }

        @Override
        @JsonValue
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

        ExtraInfo(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtraInfo forValue(String v) throws ParameterValueException {
            for(ExtraInfo enumItem : ExtraInfo.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "extra_info", v);
        }

        @Override
        @JsonValue
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
            for(RouteResponseGeometryType enumItem : RouteResponseGeometryType.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "geometry_format", v);
        }

        @Override
        @JsonValue
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
            for(RouteResponseType enumItem : RouteResponseType.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "format", v);
        }

        @Override
        @JsonValue
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
            for(VehicleType enumItem : VehicleType.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "vehicle_type", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    @ApiModel
    public enum AvoidFeatures {
        HIGHWAYS ("highways"),
        TOLLWAYS ("tollways"),
        FERRIES ("ferries"),
        TUNNELS ("tunnels"),
        PAVED_ROADS ("pavedroads"),
        UNPAVED_ROADS ("unpavedroads"),
        TRACKS ("tracks"),
        FORDS ("fords"),
        STEPS ("steps"),
        HILLS ("hills");

        private final String value;

        AvoidFeatures(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AvoidFeatures forValue(String v) throws ParameterValueException {
            for(AvoidFeatures enumItem : AvoidFeatures.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "avoid_features", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    @ApiModel(value = "Preference", description = "Specifies the route preference")
    public enum RoutePreference {
        FASTEST ("fastest"),
        SHORTEST ("shortest"),
        RECOMMENDED ("recommended");

        private final String value;

        RoutePreference(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RoutePreference forValue(String v) throws ParameterValueException {
            for(RoutePreference enumItem : RoutePreference.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "preference", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum RoutingProfile {
        DRIVING_CAR ("driving-car"),
        DRIVING_HGV ("driving-hgv"),
        CYCLING_REGULAR ("cycling-regular"),
        CYCLING_ROAD ("cycling-road"),
        CYCLING_SAFE ("cycling-safe"),
        CYCLING_MOUNTAIN ("cycling-mountain"),
        CYCLING_TOUR ("cycling-tour"),
        CYCLING_ELECTRIC ("cycling-electric"),
        FOOT_WALKING ("foot-walking"),
        FOOT_HIKING ("foot-hiking"),
        WHEELCHAIR ("wheelchair");

        private final String value;

        RoutingProfile(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RoutingProfile forValue(String v) throws ParameterValueException {
            for(RoutingProfile enumItem : RoutingProfile.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "profile", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum Units {
        METRES ("m"),
        KILOMETRES ("km"),
        MILES ("mi");

        private final String value;

        Units(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Units forValue(String v) throws ParameterValueException {
            for(Units enumItem : Units.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "units", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum Languages {
        EN ("en"),
        CN ("cn"),
        DE ("de"),
        ES ("es"),
        RU ("ru"),
        DK ("dk"),
        FR ("fr"),
        IT ("it"),
        NL ("nl"),
        BR ("br"),
        SE ("se"),
        TR ("tr"),
        GR ("gr");

        private final String value;

        Languages(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Languages forValue(String v) throws ParameterValueException {
            for(Languages enumItem : Languages.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "language", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum InstructionsFormat {
        HTML ("html"),
        TEXT ("text");

        private final String value;

        InstructionsFormat(String value) {
            this.value = value;
        }

        @JsonCreator
        public static InstructionsFormat forValue(String v) throws ParameterValueException {
            for(InstructionsFormat enumItem : InstructionsFormat.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "instructions_format", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum Attributes {
        AVERAGE_SPEED ("avgspeed"),
        DETOUR_FACTOR ("detourfactor"),
        ROUTE_PERCENTAGE ("percentage");

        private final String value;

        Attributes(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Attributes forValue(String v) throws ParameterValueException {
            for(Attributes enumItem : Attributes.values()) {
                if(enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "attributes", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }
}
