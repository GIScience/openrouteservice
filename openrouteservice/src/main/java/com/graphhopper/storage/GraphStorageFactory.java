package com.graphhopper.storage;

import com.graphhopper.GraphHopper;

public interface GraphStorageFactory {

	public GraphHopperStorage createStorage(GHDirectory dir, GraphHopper gh); 
}
