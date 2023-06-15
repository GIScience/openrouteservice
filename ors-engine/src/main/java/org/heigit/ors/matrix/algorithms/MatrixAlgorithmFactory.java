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
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.dijkstra.DijkstraMatrixAlgorithm;
import org.heigit.ors.matrix.algorithms.rphast.RPHASTMatrixAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;

public class MatrixAlgorithmFactory {
    private MatrixAlgorithmFactory() {
    }

    public static MatrixAlgorithm createAlgorithm(MatrixRequest req, GraphHopper gh) {
        if (!req.getFlexibleMode() && gh.getCHPreparationHandler().isEnabled())
            return new RPHASTMatrixAlgorithm();
        if (gh instanceof ORSGraphHopper) {
            if (req.getSearchParameters().getDynamicSpeeds() && ((ORSGraphHopper) gh).isCoreEnabled()) {
                return new CoreMatrixAlgorithm();
            }
        }
        return new DijkstraMatrixAlgorithm();
    }
}
