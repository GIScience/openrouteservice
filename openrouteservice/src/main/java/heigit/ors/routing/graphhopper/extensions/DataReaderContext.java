package heigit.ors.routing.graphhopper.extensions;

import java.util.Collection;

import com.carrotsearch.hppc.LongIndexedContainer;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.util.EdgeIteratorState;

public interface DataReaderContext {
	   public LongIntMap getNodeMap();
	   
	   public double getNodeLongitude(int nodeId);
	   
	   public double getNodeLatitude(int nodeId);

	   Collection<EdgeIteratorState> addWay(final LongIndexedContainer subgraphNodes, final long wayFlags, final long wayId);
}
