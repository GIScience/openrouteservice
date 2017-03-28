package heigit.ors.routing.graphhopper.extensions.edgefilters;

import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import heigit.ors.routing.WheelchairParameters;
import heigit.ors.routing.graphhopper.extensions.WheelchairRestrictionCodes;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;

public class WheelchairEdgeFilter implements EdgeFilter {

	private WheelchairFlagEncoder encoder;
	private double[] userDefinedRestrictionValues;
	private WheelchairAttributesGraphStorage wheelchairAttributeGraphStorage;
	private byte[] buffer = new byte[5];

	/**
	 * Creates an edges filter which accepts both direction of the specified vehicle.
	 * @throws Exception 
	 */
	public WheelchairEdgeFilter(WheelchairParameters params, WheelchairFlagEncoder encoder, GraphStorage graphStorage) throws Exception
	{
		userDefinedRestrictionValues = new double[5];
		userDefinedRestrictionValues[WheelchairRestrictionCodes.SURFACE] = params.getSurfaceType();
		userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS] = params.getSmoothnessType();
		userDefinedRestrictionValues[WheelchairRestrictionCodes.TRACKTYPE] = params.getTrackType();
		userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE] = params.getMaximumIncline();
		userDefinedRestrictionValues[WheelchairRestrictionCodes.SLOPED_CURB] = params.getMaximumSlopedCurb();
		
		this.encoder = encoder;

		wheelchairAttributeGraphStorage = GraphStorageUtils.getGraphExtension(graphStorage, WheelchairAttributesGraphStorage.class);

		if (wheelchairAttributeGraphStorage ==  null)
			throw new Exception("GraphStorage for wheelchair attributes was not found.");
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

		// double[] wheelchairAttributes = wheelchairAttributeGraphStorage.getWheelchairAttributes(iter.getEdge(), buffer);
		wheelchairAttributeGraphStorage.getWheelchairAttributes(iter.getEdge(), buffer);
		// System.out.println("WheelchairEdgeFilter.accept(), edgeId="+iter.getEdge()+", wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]="+wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]+", userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]="+userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]);
		// System.out.println("WheelchairEdgeFilter.accept(), edgeId="+iter.getEdge()+", wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]="+wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]+", userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]="+userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]);
		if (!(buffer[WheelchairRestrictionCodes.SURFACE] <= userDefinedRestrictionValues[WheelchairRestrictionCodes.SURFACE]))
			return false;
		if (!(buffer[WheelchairRestrictionCodes.SMOOTHNESS] <= userDefinedRestrictionValues[WheelchairRestrictionCodes.SMOOTHNESS]))
			return false;
		if (!(buffer[WheelchairRestrictionCodes.TRACKTYPE] <= userDefinedRestrictionValues[WheelchairRestrictionCodes.TRACKTYPE]))
			return false;

		double value =	(double)(buffer[WheelchairRestrictionCodes.SLOPED_CURB] & 0xff) / 10d;
		if (!(value <= userDefinedRestrictionValues[WheelchairRestrictionCodes.SLOPED_CURB]))
			return false;

		value = (double)(buffer[WheelchairRestrictionCodes.INCLINE] & 0xff) / 10d;
		if (!(value <= userDefinedRestrictionValues[WheelchairRestrictionCodes.INCLINE]))
			return false;
		// System.out.println("WheelchairEdgeFilter.accept(), return=true");
		return true;
	}
}
