package heigit.ors.routing.graphhopper.extensions;

import java.util.Collection;

import com.carrotsearch.hppc.LongIndexedContainer;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.util.EdgeIteratorState;

public class OSMDataReaderContext implements DataReaderContext {

	private OSMReader osmReader;
	
	public OSMDataReaderContext(OSMReader osmReader) {
		this.osmReader = osmReader;
	}
	
	@Override
	public LongIntMap getNodeMap() {
		return osmReader.getNodeMap();
	}

	@Override
	public double getNodeLongitude(int nodeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNodeLatitude(int nodeId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<EdgeIteratorState> addWay(LongIndexedContainer subgraphNodes, long wayFlags, long wayId) {
		// TODO Auto-generated method stub
		return null;
	}
   
}
