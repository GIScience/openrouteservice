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

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import org.heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import org.heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.util.DestinationDependentEdgeFilter;

import java.util.ArrayList;
import java.util.List;

public class HeavyVehicleEdgeFilter implements DestinationDependentEdgeFilter {

	public static class CustomDijkstra extends Dijkstra {
		CustomDijkstra(Graph g, Weighting weighting, TraversalMode tMode) {
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

	public HeavyVehicleEdgeFilter(int vehicleType, VehicleParameters vehicleParams, GraphStorage graphStorage) {

		this.hasHazmat = VehicleLoadCharacteristicsFlags.isSet(vehicleParams.getLoadCharacteristics(), VehicleLoadCharacteristicsFlags.HAZMAT);

		float[] vehicleAttrs = new float[VehicleDimensionRestrictions.COUNT];

		vehicleAttrs[VehicleDimensionRestrictions.MAX_HEIGHT] = (float)vehicleParams.getHeight();
		vehicleAttrs[VehicleDimensionRestrictions.MAX_WIDTH] = (float)vehicleParams.getWidth();
		vehicleAttrs[VehicleDimensionRestrictions.MAX_WEIGHT] = (float)vehicleParams.getWeight();
		vehicleAttrs[VehicleDimensionRestrictions.MAX_LENGTH] = (float)vehicleParams.getLength();
		vehicleAttrs[VehicleDimensionRestrictions.MAX_AXLE_LOAD] = (float)vehicleParams.getAxleload();

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
		this.indexValues = idx.toArray(new Integer[idx.size()]);
		this.indexLocs = idxl.toArray(new Integer[idxl.size()]);
		this.restCount = indexValues.length;

		this.vehicleType = vehicleType;
		this.buffer = new byte[2];

		this.gsHeavyVehicles = GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class);
	}

	public void setDestinationEdge(EdgeIteratorState edge, Graph graph, FlagEncoder encoder, TraversalMode tMode) {
		if (edge != null) {
			int nodeId = edge.getBaseNode();
			if (nodeId != -1) {
				mode = MODE_DESTINATION_EDGES;
				Weighting weighting = new FastestWeighting(encoder);
				CustomDijkstra dijkstraAlg = new CustomDijkstra(graph, weighting, tMode);
				EdgeFilter edgeFilter = this;
				dijkstraAlg.setEdgeFilter(edgeFilter);
				dijkstraAlg.calcPath(nodeId, Integer.MIN_VALUE);

				IntObjectMap<SPTEntry> destination = dijkstraAlg.getMap();

				destinationEdges = new ArrayList<>(destination.size());
				for (IntObjectCursor<SPTEntry> ee : destination) {
					if (!destinationEdges.contains(ee.value.edge)) // was originalEdge
						destinationEdges.add(ee.value.edge);
				}

				if (!destinationEdges.contains(EdgeIteratorStateHelper.getOriginalEdge(edge))) {
					int vt = gsHeavyVehicles.getEdgeVehicleType(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
					boolean dstFlag = buffer[1]!=0; // ((buffer[1] >> (vehicleType >> 1)) & 1) == 1

					if (((vt & vehicleType) == vehicleType) && (dstFlag))
						destinationEdges.add(EdgeIteratorStateHelper.getOriginalEdge(edge));
				}

				if (destinationEdges.isEmpty())
					destinationEdges = null;
			}
		}
		mode = MODE_ROUTE;
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		int edgeId = EdgeIteratorStateHelper.getOriginalEdge(iter);

		int vt = gsHeavyVehicles.getEdgeVehicleType(edgeId, buffer);
		boolean dstFlag = buffer[1] != 0; // ((buffer[1] >> (vehicleType >> 1)) & 1) == 1

		// if edge has some restrictions
		if (vt != HeavyVehicleAttributes.UNKNOWN) {
			if (mode == MODE_CLOSEST_EDGE) {
				// current vehicle type is not forbidden
				boolean edgeRestricted = ((vt & vehicleType) == vehicleType);
				if ((edgeRestricted || dstFlag) && buffer[1] != vehicleType)
					return false;
			} else if (mode == MODE_DESTINATION_EDGES) {
				// Here we are looking for all edges that have destination
				return  dstFlag && ((vt & vehicleType) == vehicleType);
			} else {
				// Check an edge with destination attribute
				if (dstFlag) {
					if ((vt & vehicleType) == vehicleType) {
						if (destinationEdges != null) {
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
		} else {
			if (mode == MODE_DESTINATION_EDGES) {
				return false;
			}
		}

		if (hasHazmat && (vt & HeavyVehicleAttributes.HAZMAT) != 0) {
			return false;
		}

		if (restCount != 0) {
			if (restCount == 1) {
				double value = gsHeavyVehicles.getEdgeRestrictionValue(edgeId, indexValues[0]);
				return value <= 0 || value >= restrictionValues[indexLocs[0]];
			} else {
				if (gsHeavyVehicles.getEdgeRestrictionValues(edgeId, retValues)) {
					for(int i=0; i<restCount; i++) {
						double value = retValues[indexLocs[i]];
						if(value > 0.0f && value < restrictionValues[indexLocs[i]]) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
}
