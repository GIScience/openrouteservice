package org.heigit.ors.centrality.algorithms.brandes;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.heigit.ors.centrality.algorithms.CentralityAlgorithm;

import java.util.*;

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

        // c_b[v] = 0 forall v in V
        for (int v: nodesInBBox) {
            betweenness.put(v,0.0d);
        }


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

            Q.add(new QueueElement(0d, s, s));

            // check that everything has the length it should.
            assert S.empty(); //S should be empty
            assert seen.size() == 1;

            while (Q.peek() != null) {
                QueueElement first = Q.poll();
                Double dist = first.dist;
                Integer pred = first.pred;
                Integer v = first.v;

                if (D.containsKey(v)) {
                    continue;
                }

                sigma.put(v, sigma.get(v) + sigma.get(pred));
                S.push(v);
                D.put(v, dist);

                // iterate all edges connected to v
                EdgeExplorer explorer = graph.createEdgeExplorer();
                EdgeIterator iter = explorer.setBaseNode(v);

                while (iter.next()) {
                    int w = iter.getAdjNode(); // this is the node this edge state is "pointing to"
                    if (!nodesInBBox.contains(w)) {
                        // Node not in bbox, skipping edge
                        continue;
                    }

                    if (D.containsKey(w)) { // This is only possible if weights are always bigger than 0, which should be given for real-world examples.
                        // Node already checked, skipping edge
                        continue;
                    }

                    Double vw_dist = dist + weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);

                    if (seen.containsKey(w) && (Math.abs(vw_dist - seen.get(w)) < 0.000001d)) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        List<Integer> predecessors = P.get(w);
                        predecessors.add(v);
                        P.put(w, predecessors);
                    } else if (!seen.containsKey(w) || vw_dist < seen.get(w)) {
                        seen.put(w, vw_dist);
                        Q.add(new QueueElement(vw_dist, v, w));
                        sigma.put(w, 0);
                        ArrayList<Integer> predecessors = new ArrayList<>();
                        predecessors.add(v);
                        P.put(w, predecessors);
                    }
                }
            }


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
        }

        return betweenness;
    }
}