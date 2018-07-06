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
import java.util.List;

import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.DestinationDependentEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class HeavyVehicleEdgeFilter implements DestinationDependentEdgeFilter {

	public class CustomDijkstra extends Dijkstra
	{
		public CustomDijkstra(Graph g, FlagEncoder encoder, Weighting weighting, TraversalMode tMode)
		{
			super(g, weighting, tMode);
			initCollections(1000);
		}

		public IntObjectMap<SPTEntry> getMap()
		{
			return fromMap;
		} 
	}

	private int vehicleType;
	private boolean hasHazmat; 
	private HeavyVehicleAttributesGraphStorage gsHeavyVehicles;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private float[] restrictionValues;
	private double[] retValues;
	private Integer[] indexValues;
	private Integer[] indexLocs;
	private int restCount;
	private int mode = MODE_CLOSEST_EDGE;
	private	List<Integer> destinationEdges;
	private byte[] buffer;

	private static final int MODE_DESTINATION_EDGES = -1;
	private static final int MODE_CLOSEST_EDGE = -2;
	private static final int MODE_ROUTE = 0;

	public HeavyVehicleEdgeFilter(FlagEncoder encoder, int vehicleType, VehicleParameters vehicleParams, GraphStorage graphStorage) {
		this(encoder, true, true, vehicleType, vehicleParams, graphStorage);
	}

	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public HeavyVehicleEdgeFilter(FlagEncoder encoder, boolean in, boolean out, int vehicleType,
			VehicleParameters vehicleParams, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.in = in;
		this.out = out;

		this.hasHazmat = VehicleLoadCharacteristicsFlags.isSet(vehicleParams.getLoadCharacteristics(), VehicleLoadCharacteristicsFlags.HAZMAT);

		float[] vehicleAttrs = new float[VehicleDimensionRestrictions.Count];

		vehicleAttrs[VehicleDimensionRestrictions.MaxHeight] = (float)vehicleParams.getHeight();
		vehicleAttrs[VehicleDimensionRestrictions.MaxWidth] = (float)vehicleParams.getWidth();
		vehicleAttrs[VehicleDimensionRestrictions.MaxWeight] = (float)vehicleParams.getWeight();
		vehicleAttrs[VehicleDimensionRestrictions.MaxLength] = (float)vehicleParams.getLength();
		vehicleAttrs[VehicleDimensionRestrictions.MaxAxleLoad] = (float)vehicleParams.getAxleload();

		ArrayList<Integer> idx = new ArrayList<Integer>();
		ArrayList<Integer> idxl = new ArrayList<Integer>();

		for (int i = 0; i < VehicleDimensionRestrictions.Count; i++) {
			float value = vehicleAttrs[i];
			if (value > 0) {
				idx.add(i);
				idxl.add(i);
			}
		}

		retValues = new double[5];
		Integer[] indexValues = idx.toArray(new Integer[idx.size()]);
		Integer[] indexLocs = idxl.toArray(new Integer[idxl.size()]);

		this.restrictionValues = vehicleAttrs;
		this.restCount = indexValues == null ? 0 : indexValues.length;
		this.indexValues = indexValues;
		this.indexLocs = indexLocs;

		this.vehicleType = vehicleType;
		this.buffer = new byte[10];
		if (this.encoder instanceof HeavyVehicleFlagEncoder)
		{
			//this.encoder.setVehicleType(vehicleType);
		}

		this.gsHeavyVehicles = GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class);
	}

	public void setDestinationEdge(EdgeIteratorState edge, Graph graph, FlagEncoder encoder, TraversalMode tMode)
	{
		if (edge != null)
		{
			int nodeId = edge.getBaseNode();
			if (nodeId != -1)
			{
				mode = MODE_DESTINATION_EDGES;
				Weighting weighting = new FastestWeighting(encoder);
				CustomDijkstra dijkstraAlg = new CustomDijkstra(graph, encoder, weighting, tMode);
				EdgeFilter edgeFilter = this;
				dijkstraAlg.setEdgeFilter(edgeFilter);
				dijkstraAlg.calcPath(nodeId, Integer.MIN_VALUE);

				IntObjectMap<SPTEntry> destination = dijkstraAlg.getMap();

				destinationEdges = new ArrayList<Integer>(destination.size());
				for (IntObjectCursor<SPTEntry> ee : destination) {
					if (!destinationEdges.contains(ee.value.edge)) // was originalEdge
						destinationEdges.add(ee.value.edge);
				}

				if (!destinationEdges.contains(edge.getOriginalEdge()))
				{
					int vt = gsHeavyVehicles.getEdgeVehicleType(edge.getOriginalEdge(), buffer);
					boolean dstFlag = buffer[1]!=0;// ((buffer[1] >> (vehicleType >> 1)) & 1) == 1;

					if (((vt & vehicleType) == vehicleType) && (dstFlag))
						destinationEdges.add(edge.getOriginalEdge());
				}

				if (destinationEdges.size() == 0)
					destinationEdges = null;
			}
		}

		mode = MODE_ROUTE;
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			int edgeId = iter.getOriginalEdge();

			int vt = gsHeavyVehicles.getEdgeVehicleType(edgeId, buffer);
			boolean dstFlag = buffer[1] != 0; // ((buffer[1] >> (vehicleType >> 1)) & 1) == 1;

			// if edge has some restrictions
			if (vt != HeavyVehicleAttributes.UNKNOWN) {
				if (mode == MODE_CLOSEST_EDGE)
				{
					// current vehicle type is not forbidden
					boolean edgeRestricted = ((vt & vehicleType) == vehicleType);
					if ((edgeRestricted || dstFlag) && (byte)buffer[1] != vehicleType)
						return false;
				}
				else if (mode == MODE_DESTINATION_EDGES)
				{
					// Here we are looking for all edges that have destination 
					return  dstFlag && ((vt & vehicleType) == vehicleType);
				}
				else 
				{
					/*if (mode == 0)
					{
						boolean bForward = encoder.isBool(vehicleType, flags, FlagEncoder.K_FORWARD);
						boolean bBackward = encoder.isBool(vehicleType, flags, FlagEncoder.K_BACKWARD);
						if (bForward != bBackward)
						{
							boolean reverse = iter.getReverse();
							boolean flagReverse = iter.getBaseNode() > iter.getAdjNode();
							if (reverse != flagReverse)
							{
								boolean temp = bForward;
								bForward = bBackward;
								bBackward = temp;
							}

							if (bBackward && reverse == true)
							{
									return false;
							}
							else if (bForward && reverse == false)
							{
									return false;
							}
						}
					}*/

					/*	if ((vt & vehicleType) != vehicleType)
					{
						//if (!(((vt & HeavyVehicleAttributes.Hgv) == HeavyVehicleAttributes.Hgv) && (vehicleType == HeavyVehicleAttributes.Goods || vehicleType == HeavyVehicleAttributes.Delivery)))
							//		return false;
						if ((vt & HeavyVehicleAttributes.Hgv) == HeavyVehicleAttributes.Hgv)
						{

						}
						else
						{
							if (mode != -1)
								return true;
						}
					}*/

					// Check an edge with destination attribute
					if (dstFlag) {
						if ((vt & vehicleType) == vehicleType)
						{
							if (destinationEdges != null)
							{
								if (!destinationEdges.contains(edgeId))
									return false;
							}
							else
								return false;
						}
						else
							return false;
					}
					else if ((vt & vehicleType) == vehicleType)
						return false;
				}
			}
			else
			{
				if (mode == MODE_DESTINATION_EDGES)
				{
					return false;
				}
			}


			if (hasHazmat)
			{
				if ((vt & HeavyVehicleAttributes.HAZMAT) != 0) {
					return false;
				}
			}

			if (restCount != 0) {
				if (restCount == 1) {
					double value = gsHeavyVehicles.getEdgeRestrictionValue(edgeId, indexValues[0], buffer);
					if (value > 0 && value < restrictionValues[indexLocs[0]])
						return false;
					else
						return true;
				} else {
					if (gsHeavyVehicles.getEdgeRestrictionValues(edgeId, buffer, retValues))
					{
						for(int i=0; i<restCount; i++) {
							double value = retValues[indexLocs[i]];
							if(value > 0.0f && value < restrictionValues[indexLocs[i]]) {
								return false;
							}
						}
					}
				}
			}

			//if (mode != MODE_DESTINATION_EDGES)
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return encoder.toString() + ", vehicle:" + vehicleType + ", in:" + in + ", out:" + out;
	}
}
