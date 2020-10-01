package org.heigit.ors.centrality.algorithms.floydwarshall;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;

import javax.validation.constraints.Max;
import java.util.HashMap;

//TODO: ist das ggf. einfach zu klompiziert? kann man da auch direkt 'ne Klasse draus machen, oder braucht es diese
//      Klassenhierarchie für irgendwas?
public class FloydWarshallCentralityAlgorithm implements CentralityAlgorithm {
    protected GraphHopper graphHopper;
    protected Graph graph;
    protected FlagEncoder encoder;
    protected Weighting weighting;

    public void init(CentralityRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
    {
        this.graphHopper = gh;
        this.graph = graph;
        this.encoder = encoder;
        this.weighting = weighting;
    }

    public CentralityResult compute(int metrics) throws Exception {
        System.out.println("Entering compute");
        int nodeNumber = graph.getNodes();
        System.out.println("Number of Nodes: ");
        System.out.println(nodeNumber);
//        float[][] distances = new float[nodeNumber][nodeNumber];
//
//        for (int u = 0; u < nodeNumber; u++ ) {
//            for (int v = u+1; v < nodeNumber; v++) {
//                EdgeIteratorState edge = graph.getEdgeIteratorState(u,v);
//                if (edge == null) {
//                    distances[u][v] = Float.MAX_VALUE;
//                } else {
//                    //distances[u][v] = edge.getDistance(); // this is just the distance between the two nodes in meter
//                    distances[u][v] = (float) weighting.calcWeight(edge, false, EdgeIterator.NO_EDGE);
//                }
//            }
//        }
//        System.out.println("Initialized distances.");
//
//        int[] pathCount = new int[nodeNumber];
//
//        for (int r = 0; r < nodeNumber; r++ ) {
//            for (int u = r+1; u < nodeNumber; u++) {
//                for (int v = u+1; v < nodeNumber; v++) {
//                    if (distances[u][v] > distances[u][r] + distances[r][v]) { //it's quicker to go via r
//                        distances[u][v] = distances[u][r] + distances[r][v];
//                        pathCount[r] += 1;
//                    }
//                }
//            }
//        }

        System.out.println("Calculated paths.");

        NodeAccess nodeAccess = graph.getNodeAccess();
        HashMap<Coordinate, Float> centralityScores = new HashMap<Coordinate, Float>();
        for (int v = 0; v < nodeNumber; v++ ) {
            Coordinate coord = new Coordinate(nodeAccess.getLon(v), nodeAccess.getLat(v));
            // centralityScores.put(coord, (float) pathCount[v]);
            centralityScores.put(coord, 0.0f);
        }

        return new CentralityResult(centralityScores);
    }

}
