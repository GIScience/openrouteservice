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
package org.heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;

import java.util.ArrayList;
import java.util.List;


public class HeavyVehicleCoreEdgeFilter implements EdgeFilter {
    private final List<BooleanEncodedValue> accessEncodedValues = new ArrayList<>();
    private final List<DecimalEncodedValue> paramsEncodedValues = new ArrayList<>();

    public HeavyVehicleCoreEdgeFilter(GraphHopperStorage graphStorage) {
        EncodingManager encodingManager = graphStorage.getEncodingManager();

        // Boolean access restrictions
        List.of(
                AgriculturalAccess.KEY,
                BusAccess.KEY,
                DeliveryAccess.KEY,
                ForestryAccess.KEY,
                GoodsAccess.KEY,
                HgvAccess.KEY,
                HazmatAccess.KEY
        ).forEach(key -> {
            if (encodingManager.hasEncodedValue(key))
                accessEncodedValues.add(encodingManager.getBooleanEncodedValue(key));
        });

        // Decimal parameter restrictions
        List.of(
                MaxAxleLoad.KEY,
                MaxHeight.KEY,
                MaxLength.KEY,
                MaxWeight.KEY,
                MaxWidth.KEY
        ).forEach(key -> {
            if (encodingManager.hasEncodedValue(key))
                paramsEncodedValues.add(encodingManager.getDecimalEncodedValue(key));
        });
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {
        return !hasEdgeRestriction(iter);
    }

    private boolean hasEdgeRestriction(EdgeIteratorState iter) {
        for (BooleanEncodedValue encodedValue : accessEncodedValues) {
            if (!iter.get(encodedValue)) {
                return true;
            }
        }
        for (DecimalEncodedValue encodedValue : paramsEncodedValues) {
            if (!Double.isInfinite(iter.get(encodedValue))) {
                return true;
            }
        }
        return false;
    }
}
