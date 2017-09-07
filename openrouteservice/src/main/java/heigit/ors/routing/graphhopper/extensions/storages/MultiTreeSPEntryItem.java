package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.util.EdgeIterator;

public class MultiTreeSPEntryItem {
	public int edge = EdgeIterator.NO_EDGE;
	public double weight = 0.0;
	public boolean update =  false;
	public MultiTreeSPEntry parent = null;
	
	public void assignFrom(MultiTreeSPEntryItem item)
	{
		edge = item.edge;
		weight = item.weight;
		update = item.update;
		parent = item.parent;
	}
}
