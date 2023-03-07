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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.heigit.ors.routing.parameters.WheelchairParameters;

public class WheelchairEdgeFilter implements EdgeFilter {
    private static final Logger LOGGER = Logger.getLogger(WheelchairEdgeFilter.class.getName());
    private byte[] buffer;
    private WheelchairAttributesGraphStorage storage;
    private WheelchairAttributes attributes;
    private WheelchairParameters params;

    public WheelchairEdgeFilter(WheelchairParameters params, GraphHopperStorage graphStorage) throws Exception {
        storage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);
        if (storage == null)
            throw new Exception("ExtendedGraphStorage for wheelchair attributes was not found.");
        this.params = params;
        if (this.params == null) {
            this.params = new WheelchairParameters();
        }
        attributes = new WheelchairAttributes();
        buffer = new byte[WheelchairAttributesGraphStorage.BYTE_COUNT];
    }

    @Override
    public boolean accept(EdgeIteratorState iter) {
        storage.getEdgeValues(iter.getEdge(), attributes, buffer);
        LOGGER.debug("edge: " + iter + (attributes.hasValues() ? " suitable: " + attributes.isSuitable() + " surfaceQualityKnown: " + attributes.isSurfaceQualityKnown() : " no wheelchair attributes"));
        return !attributes.hasValues() || !(
                checkSurfaceType()
                        || checkSmoothnessType()
                        || checkTrackType()
                        || checkMaximumIncline()
                        || checkMaximumSlopedKerb()
                        || checkMinimumWidth()
                        || checkSurfaceQualityKnown()
                        || checkUnsuitable()
        );
    }

    private boolean checkSurfaceType() {
        return params.getSurfaceType() > 0
                && params.getSurfaceType() < attributes.getSurfaceType();
    }

    private boolean checkSmoothnessType() {
        return params.getSmoothnessType() > 0
                && params.getSmoothnessType() < attributes.getSmoothnessType();
    }

    private boolean checkTrackType() {
        return params.getTrackType() > 0 && attributes.getTrackType() != 0
                && params.getTrackType() <= attributes.getTrackType();
    }

    private boolean checkMaximumIncline() {
        return params.getMaximumIncline() > (Float.MAX_VALUE * -1.0f)
                && params.getMaximumIncline() < attributes.getIncline();
    }

    private boolean checkMaximumSlopedKerb() {
        return params.getMaximumSlopedKerb() >= 0.0
                && params.getMaximumSlopedKerb() * 100.0 < attributes.getSlopedKerbHeight();// Stored in storage in cm
    }

    private boolean checkMinimumWidth() {
        return params.getMinimumWidth() > 0.0
                && attributes.getWidth() > 0.0 // if the attribute value is 0, this signifies that no data is available
                && params.getMinimumWidth() * 100.0 > attributes.getWidth(); // stored in storage in cm
    }

    private boolean checkSurfaceQualityKnown() {
        return params.isRequireSurfaceQualityKnown() && !attributes.isSurfaceQualityKnown();
    }

    private boolean checkUnsuitable() {
        return !params.allowUnsuitable() && !attributes.isSuitable();
    }
}