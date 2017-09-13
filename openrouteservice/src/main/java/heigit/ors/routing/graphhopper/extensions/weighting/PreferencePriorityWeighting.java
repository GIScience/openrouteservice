package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

/**
 * Special weighting for (motor)bike
 * <p>
 * @author Maxim Rylov
 */
public class PreferencePriorityWeighting extends FastestWeighting
{
	private static final Double THRESHOLD_AVOID_IF_POSSIBLE = (double) (PriorityCode.AVOID_IF_POSSIBLE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private static final Double THRESHOLD_REACH_DEST = (double) (PriorityCode.REACH_DEST.getValue() / (double)PriorityCode.BEST
			.getValue());
	
	private Double THRESHOLD_PREFER = (double) (PriorityCode.PREFER.getValue() / (double)PriorityCode.BEST
			.getValue());

	private Double THRESHOLD_VERY_NICE = (double) (PriorityCode.VERY_NICE.getValue() / (double)PriorityCode.BEST
			.getValue());
	
    /**
     * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
     */
    public static final int KEY = 101;
    private int encoderIndex = -1;

    public PreferencePriorityWeighting(FlagEncoder encoder, PMap map)
    {
        super(encoder, map);
        encoderIndex = encoder.getIndex();
    }

    @Override
    public double calcWeight( EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    {
    	double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			weight = 0.0; 

    	double priority = getFlagEncoder().getDouble(edgeState.getFlags(encoderIndex), KEY);

		if (priority <= THRESHOLD_REACH_DEST)
			priority /= 1.5;
		else if (priority <= THRESHOLD_AVOID_IF_POSSIBLE)
			priority /= 1.25;
		else if (priority == THRESHOLD_PREFER)
			priority *= 1.5;
		else if (priority >= THRESHOLD_VERY_NICE)
			priority *= 2.2;
		
		 return weight / (0.5 + priority);
    }
    
    @Override
    public String getName() {
        return "priority";
    } 
}
