package org.heigit.ors.api.requests.isochrones;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.heigit.ors.exceptions.ParameterValueException;

import static org.heigit.ors.isochrones.IsochronesErrorCodes.INVALID_PARAMETER_VALUE;

public class IsochronesRequestEnums {

    public enum Attributes {
        AREA("area"),
        REACH_FACTOR("reachfactor"),
        TOTAL_POPULATION("total_pop");

        private final String value;

        Attributes(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Attributes forValue(String v) throws ParameterValueException {
            v = v.toLowerCase();
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


    public enum RangeType {
        TIME("time"),
        DISTANCE("distance");

        private final String value;

        RangeType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RangeType forValue(String v) throws ParameterValueException {
            for (RangeType enumItem : RangeType.values()) {
                if (enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "range_type", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum LocationType {
        START("start"),
        DESTINATION("destination");

        private final String value;

        LocationType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static LocationType forValue(String v) throws ParameterValueException {
            for (LocationType enumItem : LocationType.values()) {
                if (enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "location_type", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    public enum CalculationMethod {
        GRID("grid"),
        CONCAVE_BALLS("concaveballs"),
        FASTISOCHRONE("fastisochrone");

        private final String value;

        CalculationMethod(String value) {
            this.value = value;
        }

        @JsonCreator
        public static CalculationMethod forValue(String v) throws ParameterValueException {
            for (CalculationMethod enumItem : CalculationMethod.values()) {
                if (enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "calc_method", v);
        }
    }


}
