package org.freeopenls.routeservice.graphhopper.extensions.edgefilters;

import org.freeopenls.routeservice.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.freeopenls.routeservice.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.util.WheelchairRestrictionCodes;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class WheelchairEdgeFilter implements EdgeFilter {

	private WheelchairFlagEncoder encoder;
	private double[] userDefinedRestrictionValues;
	private WheelchairAttributesGraphStorage wheelchairAttributeGraphStorage;
	byte[] buffer = new byte[10];
	
	/**
	  * Creates an edges filter which accepts both direction of the specified vehicle.
	  */
	 public WheelchairEdgeFilter(double[] restrictionValues, WheelchairFlagEncoder encoder, GraphStorage graphStorage)
	 {
		 this.userDefinedRestrictionValues = restrictionValues;
		 this.encoder = encoder;
		 setGraphStorage(graphStorage);
	 }

	@Override
	public boolean accept(EdgeIteratorState iter) {
		long flags = iter.getFlags();
		// System.out.println("WheelchairEdgeFilter.accept1()="+accept);
		// System.out.println("WheelchairEdgeFilter.accept(), encoder="+encoder);
		// System.out.println("WheelchairEdgeFilter.accept(), userDefinedRestrictionValues="+userDefinedRestrictionValues);
		/*
		if (!encoder.checkRestriction(flags, WheelchairFlagEncoder.ENCODER_NAME_SURFACE, (int)userDefinedRestrictionValues[WheelchairRestrictionCodes.SURFACE]))
			return false;
		if (!encoder.checkRestriction(flags, WheelchairFlagEncoder.ENCODER_NAME_SMOOTHNESS, (int)userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]))
			return false;
		*/
		
		if (!encoder.checkRestriction(flags, WheelchairFlagEncoder.ENCODER_NAME_SLOPED_CURB, userDefinedRestrictionValues[WheelchairRestrictionCodes.SLOPED_CURB]))
			return false;
		/*
		if (!encoder.checkRestriction(flags, WheelchairFlagEncoder.ENCODER_NAME_TRACKTYPE, (int)userDefinedRestrictionValues[WheelchairRestrictionCodes.TRACKTYPE]))
			return false;
		if (!encoder.checkRestriction(flags, WheelchairFlagEncoder.ENCODER_NAME_INCLINE, userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]))
			return false;
		*/
		
		double[] wheelchairAttributes = wheelchairAttributeGraphStorage.getWheelchairAttributes(iter.getEdge(), buffer);
		// System.out.println("WheelchairEdgeFilter.accept(), edgeId="+iter.getEdge()+", wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]="+wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]+", userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]="+userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]);
		// System.out.println("WheelchairEdgeFilter.accept(), edgeId="+iter.getEdge()+", wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]="+wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]+", userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]="+userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]);
		if (!((byte)wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] <= (byte)userDefinedRestrictionValues[WheelchairRestrictionCodes.SURFACE]))
			return false;
		if (!((byte)wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] <= (byte)userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]))
			return false;
		/*
		if (!(wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] <= userDefinedRestrictionValues[WheelchairRestrictionCodes.SLOPED_CURB]))
			return false;
		*/
		if (!((byte)wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] <= (byte)userDefinedRestrictionValues[WheelchairRestrictionCodes.TRACKTYPE]))
			return false;
		if (!(wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] <= userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]))
			return false;
		// System.out.println("WheelchairEdgeFilter.accept(), return=true");
		return true;
	}
	
	private void setGraphStorage(GraphStorage graphStorage) {
		if (graphStorage != null) {
			if (graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				GraphExtension ge = ghs.getExtension();
				
				if(ge instanceof ExtendedStorageSequence) {
					ExtendedStorageSequence ess = (ExtendedStorageSequence)ge;
					GraphExtension[] exts = ess.getExtensions();
					for (int i = 0; i < exts.length; i++) {
						if (assignExtension(exts[i]))
							break;
					}
				}
				else {
					assignExtension(ge);
				}
			}
		}
	}
	
	private boolean assignExtension(GraphExtension ge)
	{
		if (ge instanceof WheelchairAttributesGraphStorage) {
			this.wheelchairAttributeGraphStorage = (WheelchairAttributesGraphStorage) ge;
			return true;
		} 
		
		return false;
	}
}
