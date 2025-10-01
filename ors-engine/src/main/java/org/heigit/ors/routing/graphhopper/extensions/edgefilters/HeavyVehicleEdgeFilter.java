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
import com.graphhopper.routing.querygraph.EdgeIteratorStateHelper;
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

public class HeavyVehicleEdgeFilter implements EdgeFilter {
    private final int vehicleType;
    private final boolean hasHazmat;
    private final HeavyVehicleAttributesGraphStorage gsHeavyVehicles;
    private final float[] restrictionValues;
    private final double[] retValues;
    private final Integer[] indexValues;
    private final Integer[] indexLocs;
    private final int restCount;

    private BooleanEncodedValue agriculturalAccessEnc = null;
    private BooleanEncodedValue busAccessEnc = null;
    private BooleanEncodedValue deliveryAccessEnc = null;
    private BooleanEncodedValue forestryAccessEnc = null;
    private BooleanEncodedValue goodsAccessEnc = null;
    private BooleanEncodedValue hgvAccessEnc = null;

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

        ArrayList<Integer> idx = new ArrayList<>();
        ArrayList<Integer> idxl = new ArrayList<>();

        for (int i = 0; i < VehicleDimensionRestrictions.COUNT; i++) {
            float value = vehicleAttrs[i];
            if (value > 0) {
                idx.add(i);
                idxl.add(i);
            }
        }

        retValues = new double[5];

        this.restrictionValues = vehicleAttrs;
        this.indexValues = idx.toArray(new Integer[0]);
        this.indexLocs = idxl.toArray(new Integer[0]);
        this.restCount = indexValues.length;

        this.vehicleType = vehicleType;

        this.gsHeavyVehicles = hgvStorage;
    }

    @Override
    public boolean accept(EdgeIteratorState iter) {
        // test access restrictions for the given vehicle type
        if (!acceptVehicleType(iter))
            return false;

        int edgeId = EdgeIteratorStateHelper.getOriginalEdge(iter);

        if (hasHazmat && isVehicleType(gsHeavyVehicles.getEdgeVehicleType(edgeId), HeavyVehicleAttributes.HAZMAT)) {
            return false;
        }

        if (restCount != 0) {
            if (restCount == 1) {
                double value = gsHeavyVehicles.getEdgeRestrictionValue(edgeId, indexValues[0]);
                return value <= 0 || value >= restrictionValues[indexLocs[0]];
            } else {
                if (gsHeavyVehicles.getEdgeRestrictionValues(edgeId, retValues)) {
                    for (int i = 0; i < restCount; i++) {
                        double value = retValues[indexLocs[i]];
                        if (value > 0.0f && value < restrictionValues[indexLocs[i]]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean acceptVehicleType(EdgeIteratorState edge) {
        return switch (vehicleType) {
            case HeavyVehicleAttributes.AGRICULTURE -> agriculturalAccessEnc == null || edge.get(agriculturalAccessEnc);
            case HeavyVehicleAttributes.BUS -> busAccessEnc == null || edge.get(busAccessEnc);
            case HeavyVehicleAttributes.DELIVERY -> deliveryAccessEnc == null || edge.get(deliveryAccessEnc);
            case HeavyVehicleAttributes.FORESTRY -> forestryAccessEnc == null || edge.get(forestryAccessEnc);
            case HeavyVehicleAttributes.GOODS -> goodsAccessEnc == null || edge.get(goodsAccessEnc);
            case HeavyVehicleAttributes.HGV -> hgvAccessEnc == null || edge.get(hgvAccessEnc);
            default -> true;
        };
    }

    private boolean isVehicleType(int vt, int vehicleType) {
        return (vt & vehicleType) == vehicleType;
    }
}
