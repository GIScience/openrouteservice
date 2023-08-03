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
import com.graphhopper.storage.Graph;
import org.heigit.ors.exceptions.MaxVisitedNodesExceededException;
import org.heigit.ors.matrix.MatrixRequest;

public abstract class AbstractMatrixAlgorithm implements MatrixAlgorithm {
    protected GraphHopper graphHopper;
    protected Graph graph;
    protected FlagEncoder encoder;
    protected Weighting weighting;
    protected int visitedNodes = 0;
    protected int maxVisitedNodes = Integer.MAX_VALUE;

    public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting) {
        graphHopper = gh;
        this.graph = graph;
        this.encoder = encoder;
        this.weighting = weighting;
        this.maxVisitedNodes = req.getMaximumVisitedNodes();
    }

    protected boolean isMaxVisitedNodesExceeded() {
        if (visitedNodes > maxVisitedNodes)
            throw new MaxVisitedNodesExceededException();
        return false;
    }
}
