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

package org.heigit.ors.api.responses.matrix.json;

import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.ResolvedLocation;

import java.util.ArrayList;
import java.util.List;

class JSONBasedIndividualMatrixResponse {

    private boolean includeResolveLocations = false;

    JSONBasedIndividualMatrixResponse(MatrixRequest request) {
        if (request.hasResolveLocations() && request.getResolveLocations())
            includeResolveLocations = true;
    }

    List<JSON2DDestinations> constructDestinations(MatrixResult matrixResult) {
        List<JSON2DDestinations> destinations = new ArrayList<>();
        for (ResolvedLocation location : matrixResult.getDestinations()) {
            if (location != null)
                destinations.add(new JSON2DDestinations(location, includeResolveLocations));
            else
                destinations.add(null);
        }
        return destinations;
    }

    List<JSON2DSources> constructSources(MatrixResult matrixResult) {
        List<JSON2DSources> sources = new ArrayList<>();
        for (ResolvedLocation location : matrixResult.getSources()) {
            if (location != null)
                sources.add(new JSON2DSources(location, includeResolveLocations));
            else
                sources.add(null);
        }
        return sources;
    }
}
