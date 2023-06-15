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

package org.heigit.ors.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.heigit.ors.util.FormatUtility;

import java.io.IOException;

public class BBoxSerializer extends StdSerializer<double[][]> {
    private static final int COORDINATE_PRECISION = 6;

    public BBoxSerializer() {
        this(null);
    }

    public BBoxSerializer(Class<double[][]> t) {
        super(t);
    }

    @Override
    public void serialize(double[][] coordinates, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        double[][] formattedCoords = new double[coordinates.length][coordinates[0].length];
        formattedCoords[0][0] = FormatUtility.roundToDecimals(coordinates[0][0], COORDINATE_PRECISION);
        formattedCoords[0][1] = FormatUtility.roundToDecimals(coordinates[0][1], COORDINATE_PRECISION);
        formattedCoords[1][0] = FormatUtility.roundToDecimals(coordinates[1][0], COORDINATE_PRECISION);
        formattedCoords[1][1] = FormatUtility.roundToDecimals(coordinates[1][1], COORDINATE_PRECISION);

        if(coordinates[0].length == 3)
            formattedCoords[0][2] = FormatUtility.roundToDecimals(coordinates[0][2], COORDINATE_PRECISION);
        if(coordinates[1].length == 3)
            formattedCoords[1][2] = FormatUtility.roundToDecimals(coordinates[0][2], COORDINATE_PRECISION);

        jsonGenerator.writeStartArray();
        jsonGenerator.writeArray(formattedCoords[0], 0, formattedCoords[0].length);
        jsonGenerator.writeArray(formattedCoords[1], 0, formattedCoords[1].length);
        jsonGenerator.writeEndArray();
    }
}
