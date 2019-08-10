/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import heigit.ors.exceptions.ParameterValueException;
import io.swagger.annotations.ApiModel;

import static heigit.ors.api.errors.GenericErrorCodes.INVALID_PARAMETER_VALUE;

public class APIEnums {
    @ApiModel(value = "Specify which type of border crossing to avoid")
    public enum AvoidBorders {
        ALL("all"),
        CONTROLLED("controlled"),
        NONE("none");

        private final String value;

        AvoidBorders(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AvoidBorders forValue(String v) throws ParameterValueException {
            for (AvoidBorders enumItem : AvoidBorders.values()) {
                if (enumItem.value.equals(v))
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

    @ApiModel(value = "Specify which extra info items to include in the response")
    public enum ExtraInfo {
        STEEPNESS("steepness"),
        SUITABILITY("suitability"),
        SURFACE("surface"),
        WAY_CATEGORY("waycategory"),
        WAY_TYPE("waytype"),
        TOLLWAYS("tollways"),
        TRAIL_DIFFICULTY("traildifficulty"),
        OSM_ID("osmid"),
        ROAD_ACCESS_RESTRICTIONS("roadaccessrestrictions"),
        COUNTRY_INFO("countryinfo");

        private final String value;

        ExtraInfo(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtraInfo forValue(String v) throws ParameterValueException {
            for (ExtraInfo enumItem : ExtraInfo.values()) {
                if (enumItem.value.equals(v))
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

    @ApiModel
    public enum RouteResponseType {
        GPX("gpx"),
        JSON("json"),
        GEOJSON("geojson");

        private final String value;

        RouteResponseType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RouteResponseType forValue(String v) throws ParameterValueException {
            for (RouteResponseType enumItem : RouteResponseType.values()) {
                if (enumItem.value.equals(v))
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
    public enum MatrixResponseType {
        JSON("json");

        private final String value;

        MatrixResponseType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static MatrixResponseType forValue(String v) throws ParameterValueException {
            for (MatrixResponseType enumItem : MatrixResponseType.values()) {
                if (enumItem.value.equals(v))
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
        HGV("hgv"),
        BUS("bus"),
        AGRICULTURAL("agricultural"),
        DELIVERY("delivery"),
        FORESTRY("forestry"),
        GOODS("goods"),
        @JsonIgnore UNKNOWN("unknown");

        private final String value;

        VehicleType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static VehicleType forValue(String v) throws ParameterValueException {
            for (VehicleType enumItem : VehicleType.values()) {
                if (enumItem.value.equals(v))
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
        HIGHWAYS("highways"),
        TOLLWAYS("tollways"),
        FERRIES("ferries"),
        FORDS("fords"),
        STEPS("steps");

        private final String value;

        AvoidFeatures(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AvoidFeatures forValue(String v) throws ParameterValueException {
            for (AvoidFeatures enumItem : AvoidFeatures.values()) {
                if (enumItem.value.equals(v))
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
        FASTEST("fastest"),
        SHORTEST("shortest"),
        RECOMMENDED("recommended");

        private final String value;

        RoutePreference(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RoutePreference forValue(String v) throws ParameterValueException {
            for (RoutePreference enumItem : RoutePreference.values()) {
                if (enumItem.value.equals(v))
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

    public enum Profile {
        DRIVING_CAR("driving-car"),
        DRIVING_HGV("driving-hgv"),
        CYCLING_REGULAR("cycling-regular"),
        CYCLING_ROAD("cycling-road"),
        CYCLING_MOUNTAIN("cycling-mountain"),
        CYCLING_ELECTRIC("cycling-electric"),
        FOOT_WALKING("foot-walking"),
        FOOT_HIKING("foot-hiking"),
        WHEELCHAIR("wheelchair");

        private final String value;

        Profile(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Profile forValue(String v) throws ParameterValueException {
            for (Profile enumItem : Profile.values()) {
                if (enumItem.value.equals(v))
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
        METRES("m"),
        KILOMETRES("km"),
        MILES("mi");

        private final String value;

        Units(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Units forValue(String v) throws ParameterValueException {

            v = v.toLowerCase();

            for (Units enumItem : Units.values()) {
                if (enumItem.value.equals(v))
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
        DE("de"),
        EN("en"),
        EN_US("en-us"),
        ES("es"),
        FR("fr"),
        GR("gr"),
        HU("hu"),
        IT("it"),
        NE("ne"),
        NL("nl"),
        PT("pt"),
        RU("ru"),
        ZH_CN("zh-cn");

        private final String value;

        Languages(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Languages forValue(String v) throws ParameterValueException {
            for (Languages enumItem : Languages.values()) {
                if (enumItem.value.equalsIgnoreCase(v))
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
        HTML("html"),
        TEXT("text");

        private final String value;

        InstructionsFormat(String value) {
            this.value = value;
        }

        @JsonCreator
        public static InstructionsFormat forValue(String v) throws ParameterValueException {
            for (InstructionsFormat enumItem : InstructionsFormat.values()) {
                if (enumItem.value.equals(v))
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
        AVERAGE_SPEED("avgspeed"),
        DETOUR_FACTOR("detourfactor"),
        ROUTE_PERCENTAGE("percentage");

        private final String value;

        Attributes(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Attributes forValue(String v) throws ParameterValueException {
            for (Attributes enumItem : Attributes.values()) {
                if (enumItem.value.equals(v))
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
