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

package heigit.ors.api.requests.matrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import heigit.ors.exceptions.ParameterValueException;

import static heigit.ors.matrix.MatrixErrorCodes.INVALID_PARAMETER_VALUE;

public class MatrixRequestEnums {
    public enum Metrics {
        DISTANCE("distance"),
        DURATION("duration");

        private final String value;

        Metrics(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Metrics forValue(String v) throws ParameterValueException {
            v = v.toLowerCase();
            for (Metrics enumItem : Metrics.values()) {
                if (enumItem.value.equals(v))
                    return enumItem;
            }
            throw new ParameterValueException(INVALID_PARAMETER_VALUE, "metrics", v);
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }
}
