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

package heigit.ors.api.responses.matrix.JSONMatrixResponseObjects;

import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.api.responses.matrix.IndividualMatrixResponse;
import heigit.ors.matrix.MatrixResult;

//TODO Construct location results here if the "resolve_locations" parameter is true
public class JSONBasedIndividualMatrixResponse extends IndividualMatrixResponse {


    public JSONBasedIndividualMatrixResponse(MatrixResult result, MatrixRequest request) {
        super(result, request);
    }

    /*protected List<JSONSegment> constructSegments(RouteResult routeResult, RouteRequest request) {
        List segments = new ArrayList<>();
        for (RouteSegment routeSegment : routeResult.getSegments()) {
            segments.add(new JSONSegment(routeSegment, request, routeResult.getSummary().getDistance()));
        }

        return segments;
    }*/
}
