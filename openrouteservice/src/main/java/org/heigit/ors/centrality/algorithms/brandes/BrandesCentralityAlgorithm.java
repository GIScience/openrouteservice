package org.heigit.ors.centrality.algorithms.brandes;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.centrality.CentralityRequest;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;

import java.util.*;

//TODO: ist das ggf. einfach zu klompiziert? kann man da auch direkt 'ne Klasse draus machen, oder braucht es diese
//      Klassenhierarchie für irgendwas?
public class BrandesCentralityAlgorithm implements CentralityAlgorithm {
    protected CentralityRequest request;
    protected GraphHopper graphHopper;
    protected Graph graph;
    protected FlagEncoder encoder;
    protected Weighting weighting;

    public void init(CentralityRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
    {
        this.request = req;
        this.graphHopper = gh;
        this.graph = graph;
        this.encoder = encoder;
        this.weighting = weighting;
    }

    private class QueueElement implements Comparable<QueueElement> {
        public Float dist;
        public Integer pred;
        public Integer v;

        public QueueElement(Float dist, Integer pred, Integer v) {
            this.dist = dist;
            this.pred = pred;
            this.v = v;
        }

        public int compareTo(QueueElement other) {
            return Float.compare(this.dist, other.dist);
        }
    }
    // this implementation follows the code given in
    // "A Faster Algorithm for Betweenness Centrality" by Ulrik Brandes, 2001
    public CentralityResult compute(int metrics) throws Exception {
        HashMap<Integer, Float> betweenness = new HashMap<>();

        System.out.println("Entering compute");
        //TODO: how to handle working on full / incomplete graphs for testing purposes
        //int nodeNumber = graph.getNodes();
        LocationIndex index = graphHopper.getLocationIndex();
        final ArrayList<Integer> nodesInBBox = new ArrayList<>();
        index.query(this.request.getBoundingBox(), new LocationIndex.Visitor() {
            @Override
            public void onNode(int nodeId) {
                nodesInBBox.add(nodeId);
            }
        });
        System.out.println("Number of Nodes: ");
        System.out.println(nodesInBBox.size());

        // c_b[v] = 0 forall v in V
        for (int v: nodesInBBox) {
            betweenness.put(v,0.0f);
        }


        Stack<Integer> S = new Stack<>();
        HashMap<Integer, List<Integer>> P = new HashMap<>();
        HashMap<Integer, Integer> sigma = new HashMap<>();

        System.out.println("Initiated data structures, starting algorithm.");

        for (int s : nodesInBBox) {
            // single source shortest path
            //S, P, sigma = SingleSourceDijkstra(graph, nodesInBBox, s);

            for (int v : nodesInBBox) {
                P.put(v, new ArrayList<>());
                sigma.put(v, 0);
            }
            sigma.put(s, 1);

            HashMap<Integer, Float> D = new HashMap<>();
            HashMap<Integer, Float> seen = new HashMap<>();
            seen.put(s, 0.0f);
            PriorityQueue<QueueElement> Q = new PriorityQueue<>();

            System.out.println("Initiate data structures for SSSP");

            Q.add(new QueueElement(0f, s, s));
            while (Q.peek() != null) {
                QueueElement first = Q.poll();
                Float dist = first.dist;
                Integer pred = first.pred;
                Integer v = first.v;
                System.out.println("Pop-ed first element from queue");

                if (D.containsKey(v)) {
                    continue;
                }

                sigma.put(v, sigma.get(v) + sigma.get(pred));
                S.push(v);
                D.put(v, dist);

                System.out.println("Counted shortest paths, mark as visited");

                // iterate all edges connected to v
                EdgeExplorer explorer = graph.createEdgeExplorer();
                EdgeIterator iter = explorer.setBaseNode(v);

                System.out.println("Created edge explorer");

                while (iter.next()) {
                                        System.out.println("Iterating through edges");
                    int w = iter.getAdjNode(); // this is the node where this edge state is "pointing to"
                    if (!nodesInBBox.contains(w)) {
                        System.out.println("Skipping edge");
                        continue;
                    }

                    Float vw_dist = 0.0f;
                    try {
                        vw_dist = (float) weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    System.out.println("Calculated edge weight");

                    if (!D.containsKey(w) && (!seen.containsKey(w) || vw_dist < seen.get(w))) {
                        seen.put(w, vw_dist);
                        Q.add(new QueueElement(vw_dist, v, w));
                        sigma.put(w, 0);
                        P.put(w, new ArrayList<>(v));
                        System.out.println("Calculations for new shorter path done");
                    } else if (vw_dist.equals(seen.get(w))) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        List<Integer> predecessors = P.get(w);
                        predecessors.add(v);
                        P.put(w, predecessors);
                        System.out.println("Calculations for same path done");
                    }
                }
            }

            System.out.println("SSSP completed for node");
            System.out.println(s);

            // accumulate betweenness
            HashMap<Integer, Float> delta = new HashMap<>();

            for (Integer v : S) {
                delta.put(v, 0.0f);
            }

            while (!S.empty()) {
                Integer w = S.pop();
                Float coefficient = (1 + delta.get(w)) / sigma.get(w);
                for (Integer v : P.get(w)) {
                    delta.put(v, delta.get(v) + sigma.get(v) * coefficient);
                }
                if (w != s) {
                    betweenness.put(w, betweenness.get(w) + delta.get(w));
                }
            }

            System.out.println("Accumulated betweenness for above node");
        }

        System.out.println("Calculated paths.");

        NodeAccess nodeAccess = graph.getNodeAccess();
        HashMap<Coordinate, Float> centralityScores = new HashMap<>();
        for (int v : nodesInBBox) {
           Coordinate coord = new Coordinate(nodeAccess.getLon(v), nodeAccess.getLat(v));
            // centralityScores.put(coord, (float) pathCount[v]);
            centralityScores.put(coord, betweenness.get(v));
        }

        System.out.println("Generated centralityScores.");
        System.out.println(centralityScores.size());

        return new CentralityResult(centralityScores);
    }

}