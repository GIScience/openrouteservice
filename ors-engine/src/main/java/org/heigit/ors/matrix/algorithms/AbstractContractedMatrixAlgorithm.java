/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.matrix.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.RoutingCHGraph;
import org.heigit.ors.matrix.MatrixRequest;

public abstract class AbstractContractedMatrixAlgorithm extends AbstractMatrixAlgorithm {
    protected RoutingCHGraph chGraph;

    public void init(MatrixRequest req, GraphHopper gh, RoutingCHGraph chGraph, FlagEncoder encoder, Weighting weighting) {
        super.init(req, gh, chGraph.getBaseGraph(), encoder, weighting);
        this.chGraph = chGraph;
    }
}
