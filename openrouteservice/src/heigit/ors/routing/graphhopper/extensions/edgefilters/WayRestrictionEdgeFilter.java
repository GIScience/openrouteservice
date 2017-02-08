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

public abstract class WayRestrictionEdgeFilter  implements EdgeFilter {

	private int indexValue;
	private MotorcarAttributesGraphStorage gsMotorcar;
	private final boolean in;
	private final boolean out;
	private FlagEncoder encoder;
	private double restrictionValue;
    private byte[] buffer;
	
	/**
	 * Creates an edges filter which accepts both direction of the specified vehicle.
	 */
	public WayRestrictionEdgeFilter(FlagEncoder encoder, boolean in, boolean out, double restrictionValue, int indexValue,  GraphStorage graphStorage)
	{
		this.encoder = encoder;
		this.in = in;
		this.out = out;
		this.restrictionValue = restrictionValue;
		this.indexValue = indexValue;
		this.buffer = new byte[10];

		setGraphStorage(graphStorage);
	}

	private void setGraphStorage(GraphStorage graphStorage)
	{
		if (graphStorage != null)
		{
			if (graphStorage instanceof GraphHopperStorage)
			{
				GraphHopperStorage ghs = (GraphHopperStorage)graphStorage;
				if (ghs.getExtension() instanceof MotorcarAttributesGraphStorage)
				{
					this.gsMotorcar = (MotorcarAttributesGraphStorage)ghs.getExtension();
				}
			}
		}
	}   

	@Override
	public boolean accept(EdgeIteratorState iter)
	{
		long flags = iter.getFlags();

		if (out && iter.isForward(encoder) || in && iter.isBackward(encoder))
		{
			double value = gsMotorcar.getEdgeRestrictionValue(iter.getEdge(), indexValue, buffer);
			return value <= restrictionValue;
		}

		return false;
	}


	@Override
	public String toString()
	{
		return encoder.toString() + ", index:" + indexValue + ", in:" + in + ", out:" + out;
	}
}
