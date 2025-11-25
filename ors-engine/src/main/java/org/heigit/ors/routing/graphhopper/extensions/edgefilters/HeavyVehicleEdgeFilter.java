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

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.parameters.VehicleParameters;

import java.util.HashMap;
import java.util.Map;

public class HeavyVehicleEdgeFilter implements EdgeFilter {
    Map<DecimalEncodedValue, Double> vehicleRestrictions = new HashMap<>();
    private BooleanEncodedValue vehicleAccessEnc = null;
    private BooleanEncodedValue hazmatAccessEnc = null;
    private boolean hasHazmat = false;

    public HeavyVehicleEdgeFilter(int vehicleType, VehicleParameters vehicleParams, GraphHopperStorage graphStorage) {
        var encodingManager = graphStorage.getEncodingManager();

        String vehicleAccessKey = switch (vehicleType) {
            case HeavyVehicleAttributes.AGRICULTURE -> AgriculturalAccess.KEY;
            case HeavyVehicleAttributes.BUS -> BusAccess.KEY;
            case HeavyVehicleAttributes.DELIVERY -> DeliveryAccess.KEY;
            case HeavyVehicleAttributes.FORESTRY -> ForestryAccess.KEY;
            case HeavyVehicleAttributes.GOODS -> GoodsAccess.KEY;
            case HeavyVehicleAttributes.HGV -> HgvAccess.KEY;
            default -> throw new IllegalArgumentException("Unsupported vehicle type for HeavyVehicleEdgeFilter.");
        };
        if (encodingManager.hasEncodedValue(vehicleAccessKey))
            vehicleAccessEnc = encodingManager.getBooleanEncodedValue(vehicleAccessKey);

        if (vehicleParams == null || !vehicleParams.hasAttributes())
            return;

        hasHazmat = VehicleLoadCharacteristicsFlags.isSet(vehicleParams.getLoadCharacteristics(), VehicleLoadCharacteristicsFlags.HAZMAT);
        if (encodingManager.hasEncodedValue(HazmatAccess.KEY))
            hazmatAccessEnc = encodingManager.getBooleanEncodedValue(HazmatAccess.KEY);

        if (vehicleParams.hasAxleload() && encodingManager.hasEncodedValue(MaxAxleLoad.KEY))
            vehicleRestrictions.put(encodingManager.getDecimalEncodedValue(MaxAxleLoad.KEY), vehicleParams.getAxleload());
        if (vehicleParams.hasHeight() && encodingManager.hasEncodedValue(MaxHeight.KEY))
            vehicleRestrictions.put(encodingManager.getDecimalEncodedValue(MaxHeight.KEY), vehicleParams.getHeight());
        if (vehicleParams.hasLength() && encodingManager.hasEncodedValue(MaxLength.KEY))
            vehicleRestrictions.put(encodingManager.getDecimalEncodedValue(MaxLength.KEY), vehicleParams.getLength());
        if (vehicleParams.hasWeight() && encodingManager.hasEncodedValue(MaxWeight.KEY))
            vehicleRestrictions.put(encodingManager.getDecimalEncodedValue(MaxWeight.KEY), vehicleParams.getWeight());
        if (vehicleParams.hasWidth() && encodingManager.hasEncodedValue(MaxWidth.KEY))
            vehicleRestrictions.put(encodingManager.getDecimalEncodedValue(MaxWidth.KEY), vehicleParams.getWidth());
    }

    @Override
    public boolean accept(EdgeIteratorState iter) {
        // test access restrictions for the given vehicle type
        if (!acceptVehicleType(iter))
            return false;
        // test hazmat restriction
        if (hasHazmat && hazmatAccessEnc != null && !iter.get(hazmatAccessEnc)) {
            return false;
        }
        // test dimension restrictions
        for (Map.Entry<DecimalEncodedValue, Double> entry : vehicleRestrictions.entrySet()) {
            double value = iter.get(entry.getKey());
            if (value > 0.0 && value < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptVehicleType(EdgeIteratorState edge) {
        return vehicleAccessEnc == null || edge.get(vehicleAccessEnc);
    }
}
