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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.dem.ElevationProvider;
import lombok.Getter;
import org.apache.log4j.Logger;

@Getter
public class PbfElevationProvider implements ElevationProvider {
    private static final Logger LOGGER = Logger.getLogger(PbfElevationProvider.class);
    private boolean errorLogged = false;

    @Override
    public double getEle(ReaderNode node) {
        double ele = node.getEle();
        if (Double.isNaN(ele)) {
            if (!errorLogged) {
                LOGGER.warn("'ors.engine.elevation.preprocessed: true' set in ors config, but found a node with invalid 'ele' tag! Set this flag only if you use a preprocessed pbf file! Node ID: " + node.getId());
                errorLogged = true;
            }
            ele = 0;
        }
        return ele;
    }

    @Override
    public double getEle(double lat, double lon) {
        throw new UnsupportedOperationException("PbfElevationProvider does not support resolving elevation from coordinates.");
    }

    @Override
    public boolean canInterpolate() {
        return false;
    }

    @Override
    public void release() {
        // there are no resources to release in this implementation
    }
}
