package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

public class DistanceWeighting implements Weighting
{
    protected final FlagEncoder flagEncoder;
    
    public DistanceWeighting( FlagEncoder encoder, PMap pMap )
    {
        if (!encoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + encoder + " to an EncodingManager before using it elsewhere");

        this.flagEncoder = encoder;
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
    public double getMinWeight( double distance )
    {
        return distance;
    }

    @Override
    public double calcWeight( EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId )
    {
        double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags()) : flagEncoder.getSpeed(edge.getFlags());
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

       return edge.getDistance();
    }

    @Override
    public FlagEncoder getFlagEncoder()
    {
        return flagEncoder;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + toString().hashCode();
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DistanceWeighting other = (DistanceWeighting) obj;
        return toString().equals(other.toString());
    }

    @Override
    public String toString()
    {
        return "DISTANCE|" + flagEncoder;
    }
}
