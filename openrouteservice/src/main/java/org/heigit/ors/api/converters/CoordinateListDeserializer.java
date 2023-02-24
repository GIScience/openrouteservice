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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.api.requests.common.CoordinateListWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoordinateListDeserializer extends StdDeserializer<CoordinateListWrapper> {

    public CoordinateListDeserializer(Class<CoordinateListWrapper> listWrapper) {
        super(listWrapper);
    }

    @Override
    public CoordinateListWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        List<Coordinate> convertedCoords = new ArrayList<>();

        if(node.isArray()) {
            Iterator<JsonNode> coordinates = node.iterator();
            JsonNode coord;
            while(coordinates.hasNext() && ( coord = coordinates.next()) != null) {
                convertedCoords.add(new Coordinate(coord.get(0).asDouble(), coord.get(1).asDouble()));
            }
        }

        try {
            return new CoordinateListWrapper(convertedCoords);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
