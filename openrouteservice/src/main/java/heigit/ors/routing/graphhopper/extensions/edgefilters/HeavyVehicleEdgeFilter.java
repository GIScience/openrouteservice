/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.map.TIntObjectMap;
import heigit.ors.routing.VehicleParameters;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.VehicleRestrictionCodes;
import heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.DestinationDependentEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public abstract class HeavyVehicleEdgeFilter implements DestinationDependentEdgeFilter {

	public class CustomDijkstra extends Dijkstra
	{
		public CustomDijkstra(Graph g, FlagEncoder encoder, Weighting weighting, TraversalMode tMode)
		{
			super(g, encoder, weighting, tMode);
			initCollections(1000);
		}

		public TIntObjectMap<EdgeEntry> getMap()
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
	private Integer[] indexValues;
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
		this.encoder = (HeavyVehicleFlagEncoder)encoder;
		this.in = in;
		this.out = out;

		this.hasHazmat = VehicleLoadCharacteristicsFlags.isSet(vehicleParams.getLoadCharacteristics(), VehicleLoadCharacteristicsFlags.HAZMAT);

		float[] vehicleAttrs = new float[VehicleRestrictionCodes.Count];

		vehicleAttrs[VehicleRestrictionCodes.MaxHeight] = (float)vehicleParams.getHeight();
		vehicleAttrs[VehicleRestrictionCodes.MaxWidth] = (float)vehicleParams.getWidth();
		vehicleAttrs[VehicleRestrictionCodes.MaxWeight] = (float)vehicleParams.getWeight();
		vehicleAttrs[VehicleRestrictionCodes.MaxLength] = (float)vehicleParams.getLength();
		vehicleAttrs[VehicleRestrictionCodes.MaxAxleLoad] = (float)vehicleParams.getAxleload();

		ArrayList<Integer> idx = new ArrayList<Integer>();

		for (int i = 0; i < VehicleRestrictionCodes.Count; i++) {
			float value = vehicleAttrs[i];
			if (value > 0) {
				idx.add(i);
			}
		}

		Integer[] indexValues = idx.toArray(new Integer[idx.size()]);

		this.restrictionValues = vehicleAttrs;
		this.restCount = indexValues == null ? 0 : indexValues.length;
		this.indexValues = indexValues;

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
				dijkstraAlg.calcPath(nodeId, Integer.MIN_VALUE, -1);

				TIntObjectMap<EdgeEntry> destination = dijkstraAlg.getMap();

				destinationEdges = new ArrayList<Integer>(destination.size());
				for (int key : destination.keys()) {
					EdgeEntry ee = destination.get(key);
					if (!destinationEdges.contains(ee.originalEdge))
						destinationEdges.add(ee.originalEdge);
				}

				if (!destinationEdges.contains(edge.getOriginalEdge()))
				{
					gsHeavyVehicles.getEdgeVehicleTypeFlag(edge.getOriginalEdge(), buffer);
					int vt = buffer[0];
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

			gsHeavyVehicles.getEdgeVehicleTypeFlag(edgeId, buffer);

			int vt = buffer[0];
			boolean dstFlag = buffer[1] != 0; // ((buffer[1] >> (vehicleType >> 1)) & 1) == 1;

			// if edge has some restrictions
			if (vt != HeavyVehicleAttributes.Unknown) {
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
				if ((vt & HeavyVehicleAttributes.Hazmat) != 0) {
					return false;
				}
			}

			if (restCount != 0) {
				if (restCount == 1) {
					double value = gsHeavyVehicles.getEdgeRestrictionValue(edgeId, indexValues[0], buffer);
					if (value > 0 && value < restrictionValues[0])
						return false;
					else
						return true;
				} else {
					double[] retValues = gsHeavyVehicles.getEdgeRestrictionValues(edgeId, buffer);

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
