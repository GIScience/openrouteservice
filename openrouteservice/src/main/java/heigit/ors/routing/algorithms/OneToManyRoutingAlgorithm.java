package heigit.ors.routing.algorithms;

import com.graphhopper.storage.SPTEntry;

public interface OneToManyRoutingAlgorithm {
	
    void prepare(int[] from, int[] to);
    
    SPTEntry[] calcPaths(int from, int[] to);
    
    void reset();

    /**
     * Limit the search to numberOfNodes. See #681
     */
    void setMaxVisitedNodes(int numberOfNodes);

    /**
     * @return name of this algorithm
     */
    String getName();

    /**
     * Returns the visited nodes after searching. Useful for debugging.
     */
    int getVisitedNodes();
}
