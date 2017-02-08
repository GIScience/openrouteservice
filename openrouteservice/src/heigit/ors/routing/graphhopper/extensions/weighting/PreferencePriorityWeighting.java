package org.freeopenls.routeservice.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Special weighting for (motor)bike
 * <p>
 * @author Maxim Rylov
 */
public class PreferencePriorityWeighting extends FastestWeighting
{
	private Double THRESHOLD_AVOID_IF_POSSIBLE = (double) (PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private Double THRESHOLD_REACH_DEST = (double) (PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST
			.getValue());

	private Double THRESHOLD_VERY_NICE = (double) (PriorityCode.VERY_NICE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
    /**
     * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
     */
    public static final int KEY = 101;

    public PreferencePriorityWeighting(double maxSpeed, FlagEncoder encoder)
    {
        super(maxSpeed, encoder);
    }

    @Override
    public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId )
    {
    	double priority = getFlagEncoder().getDouble(edgeState.getFlags(), KEY);

    	double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			weight = 0.0;
		
		if (priority <= THRESHOLD_REACH_DEST)
			priority /= 2.0;
		else if (priority <= THRESHOLD_AVOID_IF_POSSIBLE)
			priority /= 1.5;
		else if (priority >= THRESHOLD_VERY_NICE)
			priority *= 3.0;
		
		 return weight / (0.5 + priority);
		//return weight/10000.0 + 1.0/(0.5 + priority);
    }
}
