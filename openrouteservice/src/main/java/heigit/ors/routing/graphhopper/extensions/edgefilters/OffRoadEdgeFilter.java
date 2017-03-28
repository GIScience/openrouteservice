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
