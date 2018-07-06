/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.ArrayList;

import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import heigit.ors.routing.graphhopper.extensions.storages.EmergencyVehicleAttributesGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class EmergencyVehicleEdgeFilter implements EdgeFilter {

	private EmergencyVehicleAttributesGraphStorage gsAttributes;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private float[] restrictionValues;
	private double[] retValues;
	private Integer[] indexValues;
	private int restCount;
	private byte[] buffer;

	public EmergencyVehicleEdgeFilter(FlagEncoder encoder, VehicleParameters vehicleParams, GraphStorage graphStorage) {
		this(encoder, true, true, vehicleParams, graphStorage);
	}

	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public EmergencyVehicleEdgeFilter(FlagEncoder encoder, boolean in, boolean out, VehicleParameters vehicleParams, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.in = in;
		this.out = out;

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
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			int edgeId = iter.getOriginalEdge();

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
		
		return false;
	}

	@Override
	public String toString() {
		return encoder.toString() + ", in:" + in + ", out:" + out;
	}
}
