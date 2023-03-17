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
package org.heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.routing.util.AbstractFlagEncoder;

public abstract class ORSAbstractFlagEncoder extends AbstractFlagEncoder {
    /* This is just a temporary class to ease the transition from GH0.10 to 0.12 */

    protected ORSAbstractFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);
    }

    public abstract double getMeanSpeed();

    private  boolean blockBarriers = false;
    /**
     * Should barriers block when no access limits are given?
     */
    public void blockBarriers(boolean blockBarriers) {
        this.blockBarriers = blockBarriers;
    }

    @Override
    public boolean isBarrier(ReaderNode node) {
        // note that this method will be only called for certain nodes as defined by OSMReader!
        String firstValue = node.getFirstPriorityTag(restrictions);
        boolean blockByDefault = node.hasTag("barrier", barriers);

        if (blockByDefault || node.hasTag("barrier", passByDefaultBarriers)) {
            boolean locked = restrictedValues.contains(firstValue) || node.hasTag("locked", "yes");
            for (String res : restrictions) {
                if (!locked && node.hasTag(res, intendedValues))
                    return false;
                if (node.hasTag(res, restrictedValues))
                    return true;
            }
            return blockByDefault || blockBarriers;
        }
        else return (node.hasTag("highway", "ford") || node.hasTag("ford", "yes")) && (isBlockFords() && !node.hasTag(restrictions, intendedValues) || node.hasTag(restrictions, restrictedValues));
    }
}
