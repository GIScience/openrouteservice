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
import org.heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.heigit.ors.routing.parameters.VehicleParameters;

import java.util.ArrayList;
import java.util.List;

public class HeavyVehicleEdgeFilter implements EdgeFilter {
    private final int vehicleType;
    private final boolean hasHazmat;
    private final float[] restrictionValues;
    private final Integer[] indexValues;
    private final int restCount;

    private BooleanEncodedValue agriculturalAccessEnc = null;
    private BooleanEncodedValue busAccessEnc = null;
    private BooleanEncodedValue deliveryAccessEnc = null;
    private BooleanEncodedValue forestryAccessEnc = null;
    private BooleanEncodedValue goodsAccessEnc = null;
    private BooleanEncodedValue hgvAccessEnc = null;
    private BooleanEncodedValue hazmatAccessEnc = null;
    private DecimalEncodedValue maxAxleLoadEnc = null;
    private DecimalEncodedValue maxHeightEnc = null;
    private DecimalEncodedValue maxLengthEnc = null;
    private DecimalEncodedValue maxWeightEnc = null;
    private DecimalEncodedValue maxWidthEnc = null;


    public HeavyVehicleEdgeFilter(int vehicleType, VehicleParameters vehicleParams, GraphHopperStorage graphStorage) {
        this(vehicleType, vehicleParams, GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class));
        var encodingManager = graphStorage.getEncodingManager();
        if (encodingManager.hasEncodedValue(AgriculturalAccess.KEY))
            agriculturalAccessEnc = encodingManager.getBooleanEncodedValue(AgriculturalAccess.KEY);
        if (encodingManager.hasEncodedValue(BusAccess.KEY))
            busAccessEnc = encodingManager.getBooleanEncodedValue(BusAccess.KEY);
        if (encodingManager.hasEncodedValue(DeliveryAccess.KEY))
            deliveryAccessEnc = encodingManager.getBooleanEncodedValue(DeliveryAccess.KEY);
        if (encodingManager.hasEncodedValue(ForestryAccess.KEY))
            forestryAccessEnc = encodingManager.getBooleanEncodedValue(ForestryAccess.KEY);
        if (encodingManager.hasEncodedValue(GoodsAccess.KEY))
            goodsAccessEnc = encodingManager.getBooleanEncodedValue(GoodsAccess.KEY);
        if (encodingManager.hasEncodedValue(HgvAccess.KEY))
            hgvAccessEnc = encodingManager.getBooleanEncodedValue(HgvAccess.KEY);
        if (encodingManager.hasEncodedValue(HazmatAccess.KEY))
            hazmatAccessEnc = encodingManager.getBooleanEncodedValue(HazmatAccess.KEY);
        if (encodingManager.hasEncodedValue(MaxAxleLoad.KEY))
            maxAxleLoadEnc = encodingManager.getDecimalEncodedValue(MaxAxleLoad.KEY);
        if (encodingManager.hasEncodedValue(MaxHeight.KEY))
            maxHeightEnc = encodingManager.getDecimalEncodedValue(MaxHeight.KEY);
        if (encodingManager.hasEncodedValue(MaxLength.KEY))
            maxLengthEnc = encodingManager.getDecimalEncodedValue(MaxLength.KEY);
        if (encodingManager.hasEncodedValue(MaxWeight.KEY))
            maxWeightEnc = encodingManager.getDecimalEncodedValue(MaxWeight.KEY);
        if (encodingManager.hasEncodedValue(MaxWidth.KEY))
            maxWidthEnc = encodingManager.getDecimalEncodedValue(MaxWidth.KEY);
    }

    public HeavyVehicleEdgeFilter(int vehicleType, VehicleParameters vehicleParams, HeavyVehicleAttributesGraphStorage hgvStorage) {
        float[] vehicleAttrs = new float[VehicleDimensionRestrictions.COUNT];

        if (vehicleParams != null) {
            this.hasHazmat = VehicleLoadCharacteristicsFlags.isSet(vehicleParams.getLoadCharacteristics(), VehicleLoadCharacteristicsFlags.HAZMAT);

            vehicleAttrs[VehicleDimensionRestrictions.MAX_HEIGHT] = (float) vehicleParams.getHeight();
            vehicleAttrs[VehicleDimensionRestrictions.MAX_WIDTH] = (float) vehicleParams.getWidth();
            vehicleAttrs[VehicleDimensionRestrictions.MAX_WEIGHT] = (float) vehicleParams.getWeight();
            vehicleAttrs[VehicleDimensionRestrictions.MAX_LENGTH] = (float) vehicleParams.getLength();
            vehicleAttrs[VehicleDimensionRestrictions.MAX_AXLE_LOAD] = (float) vehicleParams.getAxleload();
        } else {
            this.hasHazmat = false;
        }

        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < VehicleDimensionRestrictions.COUNT; i++) {
            float value = vehicleAttrs[i];
            if (value > 0) {
                idx.add(i);
            }
        }

        this.restrictionValues = vehicleAttrs;
        this.indexValues = idx.toArray(new Integer[0]);
        this.restCount = indexValues.length;

        this.vehicleType = vehicleType;
    }

    @Override
    public boolean accept(EdgeIteratorState iter) {
        // test access restrictions for the given vehicle type
        if (!acceptVehicleType(iter))
            return false;

        if (hasHazmat && !(hazmatAccessEnc == null || iter.get(hazmatAccessEnc))) {
            return false;
        }

        for (int i = 0; i < restCount; i++) {
            double value = getEdgeRestrictionValue(iter, indexValues[i]);
            if (value > 0.0f && value < restrictionValues[indexValues[i]]) {
                return false;
            }
        }

        return true;
    }

    private double getEdgeRestrictionValue(EdgeIteratorState iter, int valueIndex) {
        DecimalEncodedValue decimalEncodedValue = switch (valueIndex) {
            case VehicleDimensionRestrictions.MAX_AXLE_LOAD -> maxAxleLoadEnc;
            case VehicleDimensionRestrictions.MAX_HEIGHT -> maxHeightEnc;
            case VehicleDimensionRestrictions.MAX_LENGTH -> maxLengthEnc;
            case VehicleDimensionRestrictions.MAX_WEIGHT -> maxWeightEnc;
            case VehicleDimensionRestrictions.MAX_WIDTH -> maxWidthEnc;
            default -> null;
        };
        if (decimalEncodedValue == null) {
            throw new IllegalStateException("Encoded value for vehicle restriction not found.");
        }
        return iter.get(decimalEncodedValue);
    }

    private boolean acceptVehicleType(EdgeIteratorState edge) {
        BooleanEncodedValue vehicleTypeAccessEnc = switch (vehicleType) {
            case HeavyVehicleAttributes.AGRICULTURE -> agriculturalAccessEnc;
            case HeavyVehicleAttributes.BUS -> busAccessEnc;
            case HeavyVehicleAttributes.DELIVERY -> deliveryAccessEnc;
            case HeavyVehicleAttributes.FORESTRY -> forestryAccessEnc;
            case HeavyVehicleAttributes.GOODS -> goodsAccessEnc;
            case HeavyVehicleAttributes.HGV -> hgvAccessEnc;
            default -> null;
        };
        return vehicleTypeAccessEnc == null || edge.get(vehicleTypeAccessEnc);
    }
}
