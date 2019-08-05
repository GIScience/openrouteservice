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
/*
import heigit.ors.routing.graphhopper.extensions.storages.MotorcarAttributesGraphStorage;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class OffRoadEdgeFilter implements EdgeFilter {

	private MotorcarAttributesGraphStorage gsMotorcar;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private float[] restrictionValues;
	private Integer[] indexValues;
	private int valuesCount;
	private byte[] buffer;

	public OffRoadEdgeFilter(FlagEncoder encoder, float[] restrictionValues, Integer[] indexValues,
			GraphStorage graphStorage) {
		this(encoder, true, true, restrictionValues, indexValues, graphStorage);
	}


	public OffRoadEdgeFilter(FlagEncoder encoder, boolean in, boolean out, float[] restrictionValues,
			Integer[] indexValues, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.in = in;
		this.out = out;
		this.restrictionValues = restrictionValues;
		this.valuesCount = indexValues.length;
		this.indexValues = indexValues;
		this.buffer = new byte[10];

		setGraphStorage(graphStorage);
	}

	private void setGraphStorage(GraphStorage graphStorage) {
		if (graphStorage != null) {
			if (graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				if (ghs.getExtension() instanceof MotorcarAttributesGraphStorage) {
					this.gsMotorcar = (MotorcarAttributesGraphStorage) ghs.getExtension();
				}
			}
		}
	}

	@Override
	public boolean accept(EdgeIteratorState iter) {
		long flags = iter.getFlags();

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)) {
			if (valuesCount == 1) {
				double value = gsMotorcar.getEdgePassabilityValue(iter.getEdge(), indexValues[0], buffer);
				if (value > 0 && value < restrictionValues[0])
					return false;
				else
					return true;
			} else {
				double[] retValues = gsMotorcar.getEdgePassabilityValues(iter.getEdge(), buffer);

				// track, surface, smoothness
				double value = retValues[0];
				if (value > 0.0f && value < restrictionValues[0])
					return false;

				value = retValues[1];
				if (value > 0.0f && value < restrictionValues[1])
					return false;

				if (valuesCount >= 3) {
					value = retValues[2];
					if (value > 0.0f && value < restrictionValues[2])
						return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return encoder.toString() + ", in:" + in + ", out:" + out;
	}
}*/
