/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package heigit.ors.routing.graphhopper.extensions.edgefilters;

import java.util.ArrayList;

import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.graphhopper.extensions.VehicleRestrictionCodes;
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
