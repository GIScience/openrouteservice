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

import org.heigit.ors.api.requests.common.CoordinateListWrapper;
import org.locationtech.jts.geom.Coordinate;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class CoordinateListSerializer extends StdSerializer<CoordinateListWrapper> {
    public CoordinateListSerializer() {
        this(null);
    }

    public CoordinateListSerializer(Class<CoordinateListWrapper> listWrapper) {
        super(listWrapper);
    }

    @Override
    public void serialize(CoordinateListWrapper listWrapper, JsonGenerator jsonGenerator, SerializationContext context) {
        jsonGenerator.writeStartArray();

        for (Coordinate coord : listWrapper.getCoordinates()) {
            jsonGenerator.writeStartArray();

            jsonGenerator.writeNumber(coord.x);
            jsonGenerator.writeNumber(coord.y);

            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndArray();
    }
}
