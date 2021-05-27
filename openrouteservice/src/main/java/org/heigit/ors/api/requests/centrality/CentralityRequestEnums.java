package org.heigit.ors.api.requests.centrality;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.heigit.ors.exceptions.ParameterValueException;

import static org.heigit.ors.centrality.CentralityErrorCodes.INVALID_PARAMETER_VALUE;

public class CentralityRequestEnums {
    
    public enum Mode {
        NODES("nodes"),
        EDGES("edges");

        private final String value;

        Mode(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Mode forValue(String v) throws ParameterValueException {
            for (Mode enumItem : Mode.values()) {
                if (enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "mode", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

}
