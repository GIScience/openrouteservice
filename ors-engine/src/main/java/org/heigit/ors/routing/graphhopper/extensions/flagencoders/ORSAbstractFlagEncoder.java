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
    public long handleNodeTags(ReaderNode node) {
        long encoderBit = getEncoderBit();
        boolean blockFords = isBlockFords();

        boolean blockByDefault = node.hasTag("barrier", blockByDefaultBarriers);
        if (blockByDefault || node.hasTag("barrier", passByDefaultBarriers)) {
            boolean locked = false;
            if (node.hasTag("locked", "yes"))
                locked = true;

            for (String res : restrictions) {
                if (!locked && node.hasTag(res, intendedValues))
                    return 0;

                if (node.hasTag(res, restrictedValues))
                    return encoderBit;
            }

            if (blockByDefault || blockBarriers)
                return encoderBit;
            return 0;
        }

        if ((node.hasTag("highway", "ford") || node.hasTag("ford", "yes"))
                && (blockFords && !node.hasTag(restrictions, intendedValues) || node.hasTag(restrictions, restrictedValues))) {
            return encoderBit;
        }

        return 0;
    }

}
