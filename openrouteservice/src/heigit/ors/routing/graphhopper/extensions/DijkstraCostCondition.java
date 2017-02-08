package org.freeopenls.routeservice.graphhopper.extensions;

import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;

import gnu.trove.map.TIntObjectMap;

/**
 * Implements a single source shortest path algorithm
 * http://en.wikipedia.org/wiki/Dijkstra's_algorithm
 * <p/>
 * @author Peter Karich
 */
public class DijkstraCostCondition extends Dijkstra
{
    public DijkstraCostCondition(Graph g, FlagEncoder encoder, Weighting weighting, double maxCost, TraversalMode tMode)
    {
        super(g, encoder, weighting, tMode);

        initCollections(1000);
        this.weightLimit = maxCost;
    }

    public void computePath(int from, int to )
    {
        checkAlreadyRun();
        this.to = to;
        currEdge = createEdgeEntry(from, 0);
        fromMap.put(from, currEdge);
        runAlgo(-1);
    }
    
    public TIntObjectMap<EdgeEntry> getMap()
    {
    	return fromMap;
    }
    
    public EdgeEntry getCurrentEdge()
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
