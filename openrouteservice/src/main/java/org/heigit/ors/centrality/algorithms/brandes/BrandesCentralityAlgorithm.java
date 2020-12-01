package org.heigit.ors.centrality.algorithms.brandes;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.centrality.CentralityResult;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;

import java.util.*;

//TODO: ist das ggf. einfach zu klompiziert? kann man da auch direkt 'ne Klasse draus machen, oder braucht es diese
//      Klassenhierarchie für irgendwas?
public class BrandesCentralityAlgorithm implements CentralityAlgorithm {
    protected Graph graph;
    protected Weighting weighting;

    public void init(Graph graph, Weighting weighting)
    {
        this.graph = graph;
        this.weighting = weighting;
    }

    private class QueueElement implements Comparable<QueueElement> {
        public Double dist;
        public Integer pred;
        public Integer v;

        public QueueElement(Double dist, Integer pred, Integer v) {
            this.dist = dist;
            this.pred = pred;
            this.v = v;
        }

        public int compareTo(QueueElement other) {
            return Double.compare(this.dist, other.dist);
        }
    }
    // this implementation follows the code given in
    // "A Faster Algorithm for Betweenness Centrality" by Ulrik Brandes, 2001
    public HashMap<Integer, Double> compute(ArrayList<Integer> nodesInBBox) throws Exception {
        HashMap<Integer, Double> betweenness = new HashMap<>();

        // System.out.println("Entering compute");

        // c_b[v] = 0 forall v in V
        for (int v: nodesInBBox) {
            betweenness.put(v,0.0d);
        }




        // System.out.println("Initiated data structures, starting algorithm.");

        for (int s : nodesInBBox) {
            Stack<Integer> S = new Stack<>();
            HashMap<Integer, List<Integer>> P = new HashMap<>();
            HashMap<Integer, Integer> sigma = new HashMap<>();

            // single source shortest path
            //S, P, sigma = SingleSourceDijkstra(graph, nodesInBBox, s);

            for (int v : nodesInBBox) {
                P.put(v, new ArrayList<>());
                sigma.put(v, 0);
            }
            sigma.put(s, 1);

            HashMap<Integer, Double> D = new HashMap<>();
            HashMap<Integer, Double> seen = new HashMap<>();
            seen.put(s, 0.0d);

            PriorityQueue<QueueElement> Q = new PriorityQueue<>();

            // System.out.println("Initiate data structures for SSSP");

            Q.add(new QueueElement(0d, s, s));

            //let's check that everything has the length it should.
            assert S.empty(); //S should be empty
            assert seen.size() == 1;

            while (Q.peek() != null) {
                QueueElement first = Q.poll();
                Double dist = first.dist;
                Integer pred = first.pred;
                Integer v = first.v;
                // System.out.println("Pop-ed first element from queue");

                if (D.containsKey(v)) {
                    continue;
                }

                sigma.put(v, sigma.get(v) + sigma.get(pred));
                S.push(v);
                D.put(v, dist);

                // System.out.println("Counted shortest paths, mark as visited");

                // iterate all edges connected to v
                EdgeExplorer explorer = graph.createEdgeExplorer();
                EdgeIterator iter = explorer.setBaseNode(v);

                // System.out.println("Created edge explorer");

                while (iter.next()) {
                                        // System.out.println("Iterating through edges");
                    int w = iter.getAdjNode(); // this is the node where this edge state is "pointing to"
                    if (!nodesInBBox.contains(w)) {
                        // System.out.println("Node not in bbox, skipping edge");
                        continue;
                    }

                    if (D.containsKey(w)) { // This is only possible if weights are always bigger than 0, which should be given for real-world examples.
                        // System.out.println("Node already checked, skipping");
                        continue;
                    }

                    Double vw_dist = 0.0d;
                    try {
                        vw_dist = dist + weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);
                        // System.out.printf("Looking at edge (%d,%d) w/ weight %f", v, w, vw_dist);
                    } catch (Exception e) {
                        // System.out.println(e);
                    }
                    // System.out.println("Calculated edge weight");

                    if (seen.containsKey(w) && (Math.abs(vw_dist - seen.get(w)) < 0.00001d)) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        List<Integer> predecessors = P.get(w);
                        predecessors.add(v);
                        P.put(w, predecessors);
                        // System.out.println("Calculations for same path done");
                    } else if (!seen.containsKey(w) || vw_dist < seen.get(w)) {
                        seen.put(w, vw_dist);
                        Q.add(new QueueElement(vw_dist, v, w));
                        sigma.put(w, 0);
                        ArrayList<Integer> predecessors = new ArrayList<>();
                        predecessors.add(v);
                        P.put(w, predecessors);
                        // System.out.println("Calculations for new shorter path done");
                    }
//                    if (!D.containsKey(w) && (!seen.containsKey(w) || vw_dist < seen.get(w))) { // I think we run into problems here, b/c of rounding errors
//                        seen.put(w, vw_dist);
//                        Q.add(new QueueElement(vw_dist, v, w));
//                        sigma.put(w, 0);
//                        ArrayList<Integer> predecessors = new ArrayList<>();
//                        predecessors.add(v);
//                        P.put(w, predecessors);
//                        System.out.println("Calculations for new shorter path done");
//                    } else if (vw_dist.equals(seen.get(w))) {
//                        sigma.put(w, sigma.get(w) + sigma.get(v));
//                        List<Integer> predecessors = P.get(w);
//                        predecessors.add(v);
//                        P.put(w, predecessors);
//                        System.out.println("Calculations for same path done");
//                    }
                }
            }

            // System.out.println("SSSP completed for node");
            // System.out.println(s);

            // accumulate betweenness
            HashMap<Integer, Double> delta = new HashMap<>();

            for (Integer v : S) {
                delta.put(v, 0.0d);
            }

            while (!S.empty()) {
                Integer w = S.pop();
                Double coefficient = (1 + delta.get(w)) / sigma.get(w);
                for (Integer v : P.get(w)) {
                    delta.put(v, delta.get(v) + sigma.get(v) * coefficient);
                }
                if (w != s) {
                    betweenness.put(w, betweenness.get(w) + delta.get(w));
                }
            }

            // System.out.println("Accumulated betweenness for above node");
        }

        System.out.println("Calculated paths.");
        return betweenness;
    }

}