package heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.storage.Graph;

public class DijkstraCostCondition extends Dijkstra
{
	private double weightLimit = -1;
    public DijkstraCostCondition(Graph g, Weighting weighting, double maxCost, boolean reverseDirection, TraversalMode tMode)
    {
        super(g, weighting, tMode, -1);

        initCollections(1000);
        this.weightLimit = maxCost;
        setReverseDirection(reverseDirection);
    }

    @Override
    protected boolean finished() {
        return  super.finished() || currEdge.weight > weightLimit;
    }
    
    public IntObjectMap<SPTEntry> getMap()
    {
    	return fromMap;
    }
    
    public SPTEntry getCurrentEdge()
    {
    	if (currEdge == null || !finished())
    		return  null;
    	else
    		return currEdge;
    }

    @Override
    public String getName()
    {
        return "dijkstracc";
    }
}
