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

package org.heigit.ors.api.responses.common.boundingbox;

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.routing.gpx.GPXBounds;
import org.heigit.ors.api.responses.routing.json.JSON3DBoundingBox;
import org.heigit.ors.api.responses.routing.json.JSONBoundingBox;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RoutingErrorCodes;

public class BoundingBoxFactory {
    private BoundingBoxFactory() {
        throw new IllegalStateException("Factory class - should not be instantiated");
    }

    public static BoundingBox constructBoundingBox(BBox bounds, RouteRequest request) throws ParameterValueException {
        switch (request.getResponseType()) {
            case GEOJSON:
            case JSON:
                if (request.hasUseElevation() && request.getUseElevation())
                    return new JSON3DBoundingBox(bounds);
                return new JSONBoundingBox(bounds);
            case GPX:
                return new GPXBounds(bounds);
            default:
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", request.getResponseType().toString());
        }
    }

    public static BoundingBox constructBoundingBox(BBox bounds, IsochronesRequest request) throws ParameterValueException {
        if (request.getResponseType() == APIEnums.RouteResponseType.GEOJSON) {
            return new JSONBoundingBox(bounds);
        } else {
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", request.getResponseType().toString());
        }
    }
}
