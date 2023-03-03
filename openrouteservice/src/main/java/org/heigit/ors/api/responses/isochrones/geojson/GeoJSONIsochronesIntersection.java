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

package org.heigit.ors.api.responses.isochrones.geojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Geometry;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums;
import org.heigit.ors.common.Pair;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.isochrones.IsochronesErrorCodes;
import org.heigit.ors.isochrones.IsochronesIntersection;
import org.heigit.ors.util.FormatUtility;

import java.util.*;

public class GeoJSONIsochronesIntersection extends GeoJSONIsochroneBase {
    @JsonIgnore
    private final IsochronesIntersection intersection;

    @JsonProperty("properties")
    private final Map<String, Object> properties;

    public GeoJSONIsochronesIntersection(IsochronesIntersection intersection, IsochronesRequest request) throws InternalServerException {
        this.intersection = intersection;
        properties = fillProperties(intersection, request);
    }

    private Map<String, Object> fillProperties(IsochronesIntersection intersection, IsochronesRequest request) throws InternalServerException  {
        Map<String, Object> props = new HashMap<>();

        List<Integer[]> contours = new ArrayList<>();
        for (Pair<Integer, Integer> ref : intersection.getContourRefs()) {
            Integer[] pair = new Integer[2];
            pair[0] = ref.first;
            pair[1] = ref.second;

            contours.add(pair);
        }

        props.put("contours", contours);

        if (request.hasAttributes()) {
            List<IsochronesRequestEnums.Attributes> attr = new ArrayList<>(Arrays.asList(request.getAttributes()));

            if (attr.contains(IsochronesRequestEnums.Attributes.AREA)) {
                try {
                    double areaValue = 0;
                    if (request.hasAreaUnits())
                        areaValue = intersection.getArea(request.getAreaUnit().toString());
                    else
                        areaValue = intersection.getArea("");

                    props.put("area", FormatUtility.roundToDecimals(areaValue, 4));
                } catch (InternalServerException e) {
                    throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "There was a problem calculating the area of the isochrone");
                }
            }
        }

        return props;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Geometry getIsochroneGeometry() {
        return intersection.getGeometry();
    }
}
