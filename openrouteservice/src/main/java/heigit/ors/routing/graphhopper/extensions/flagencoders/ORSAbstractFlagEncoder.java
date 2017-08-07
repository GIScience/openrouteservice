package heigit.ors.routing.graphhopper.extensions.flagencoders;

import com.graphhopper.routing.util.AbstractFlagEncoder;

public abstract class ORSAbstractFlagEncoder extends AbstractFlagEncoder {
    protected SpeedLimitHandler _speedLimitHandler;

	protected ORSAbstractFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
		super(speedBits, speedFactor, maxTurnCosts);
	}
	
	
}
