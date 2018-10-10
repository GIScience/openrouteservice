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
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import heigit.ors.routing.graphhopper.extensions.storages.EmergencyVehicleAttributesGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.parameters.VehicleParameters;

import java.util.ArrayList;

public class EmergencyVehicleEdgeFilter implements EdgeFilter {

	private EmergencyVehicleAttributesGraphStorage gsAttributes;
	private float[] restrictionValues;
	private double[] retValues;
	private Integer[] indexValues;
	private int restCount;
	private byte[] buffer;

	public EmergencyVehicleEdgeFilter(VehicleParameters vehicleParams, GraphStorage graphStorage) {
		float[] vehicleAttrs = new float[VehicleDimensionRestrictions.Count];

		vehicleAttrs[VehicleDimensionRestrictions.MaxHeight] = (float)vehicleParams.getHeight();
		vehicleAttrs[VehicleDimensionRestrictions.MaxWidth] = (float)vehicleParams.getWidth();
		vehicleAttrs[VehicleDimensionRestrictions.MaxWeight] = (float)vehicleParams.getWeight();
		vehicleAttrs[VehicleDimensionRestrictions.MaxLength] = (float)vehicleParams.getLength();
		vehicleAttrs[VehicleDimensionRestrictions.MaxAxleLoad] = (float)vehicleParams.getAxleload();

		ArrayList<Integer> idx = new ArrayList<Integer>();

		for (int i = 0; i < VehicleDimensionRestrictions.Count; i++) {
			float value = vehicleAttrs[i];
			if (value > 0) {
				idx.add(i);
			}
		}

		retValues = new double[5];
		Integer[] indexValues = idx.toArray(new Integer[idx.size()]);

		this.restrictionValues = vehicleAttrs;
		this.restCount = indexValues == null ? 0 : indexValues.length;
		this.indexValues = indexValues;

		this.buffer = new byte[10];

		this.gsAttributes = GraphStorageUtils.getGraphExtension(graphStorage, EmergencyVehicleAttributesGraphStorage.class);
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		int edgeId = EdgeIteratorStateHelper.getOriginalEdge(iter);

		if (restCount != 0 && gsAttributes != null) {
			if (restCount == 1) {
				double value = gsAttributes.getEdgeRestrictionValue(edgeId, indexValues[0], buffer);
				if (value > 0 && value < restrictionValues[0])
					return false;
				else
					return true;
			} else {
				if (gsAttributes.getEdgeRestrictionValues(edgeId, buffer, retValues))
				{
					double value = retValues[0];
					if (value > 0.0f && value < restrictionValues[0])
						return false;

					value = retValues[1];
					if (value > 0.0f && value < restrictionValues[1])
						return false;

					if (restCount >= 3) {
						value = retValues[2];
						if (value > 0.0f && value < restrictionValues[2])
							return false;
					}

					if (restCount >= 4) {
						value = retValues[3];
						if (value > 0.0f && value < restrictionValues[3])
							return false;
					}

					if (restCount == 5) {
						value = retValues[4];
						if (value > 0.0f && value < restrictionValues[4])
							return false;
					}
				}
			}
		}

		return true;

	}

}
