package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class DistanceWeighting extends AbstractWeighting
{
    protected final FlagEncoder flagEncoder;
    private int encoderIndex = -1;
    
    public DistanceWeighting( FlagEncoder encoder, PMap pMap )
    {
        super(encoder);

        this.flagEncoder = encoder;
        this.encoderIndex = encoder.getIndex();
    }

    public DistanceWeighting( FlagEncoder encoder )
    {
        this(encoder, new PMap(0));
    }

    public DistanceWeighting(double userMaxSpeed, FlagEncoder encoder)
    {
    	this(encoder);
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId)
    {
        double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags(encoderIndex)) : flagEncoder.getSpeed(edge.getFlags(encoderIndex));
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

       return edge.getDistance();
    }

	@Override
	public double getMinWeight(double distance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return "distance";
	}
}
