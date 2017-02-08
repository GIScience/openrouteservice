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

package org.freeopenls.routeservice.graphhopper.extensions.edgefilters;

import org.freeopenls.routeservice.graphhopper.extensions.storages.MotorcarAttributesGraphStorage;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class WayMultipleRestrictionsEdgeFilter implements EdgeFilter {

	private MotorcarAttributesGraphStorage gsMotorcar;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private float[] restrictionValues;
	private Integer[] indexValues;
	private int restCount;
	private byte[] buffer;

	public WayMultipleRestrictionsEdgeFilter(FlagEncoder encoder, float[] restrictionValues, Integer[] indexValues,
			GraphStorage graphStorage) {
		this(encoder, true, true, restrictionValues, indexValues, graphStorage);
	}

	/**
	 * Creates an edges filter which accepts both direction of the specified
	 * vehicle.
	 */
	public WayMultipleRestrictionsEdgeFilter(FlagEncoder encoder, boolean in, boolean out, float[] restrictionValues,
			Integer[] indexValues, GraphStorage graphStorage) {
		this.encoder = encoder;
		this.in = in;
		this.out = out;
		this.restrictionValues = restrictionValues;
		this.restCount = indexValues.length;
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

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder)){
			if (restCount == 1) {
				double value = gsMotorcar.getEdgeRestrictionValue(iter.getEdge(), indexValues[0], buffer);
				if (value > 0 && value < restrictionValues[0])
					return false;
				else
					return true;
			} else {
				double[] retValues = gsMotorcar.getEdgeRestrictionValues(iter.getEdge(), buffer);

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

		return true;
	}

	@Override
	public String toString() {
		return encoder.toString() + ", in:" + in + ", out:" + out;
	}
}
