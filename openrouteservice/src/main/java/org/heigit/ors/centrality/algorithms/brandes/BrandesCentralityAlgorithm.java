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
        public final Double dist;
        public final Integer pred;
        public final Integer v;

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
    public Map<Integer, Double> compute(List<Integer> nodesInBBox) throws Exception {
        Map<Integer, Double> betweenness = new HashMap<>();

        // c_b[v] = 0 forall v in V
        for (int v: nodesInBBox) {
            betweenness.put(v,0.0d);
        }


        for (int s : nodesInBBox) {
            Stack<Integer> stack = new Stack<>();
            Map<Integer, List<Integer>> p = new HashMap<>();
            Map<Integer, Integer> sigma = new HashMap<>();

            // single source shortest path
            //S, P, sigma = SingleSourceDijkstra(graph, nodesInBBox, s);

            for (int v : nodesInBBox) {
                p.put(v, new ArrayList<>());
                sigma.put(v, 0);
            }
            sigma.put(s, 1);

            Map<Integer, Double> d = new HashMap<>();
            Map<Integer, Double> seen = new HashMap<>();
            seen.put(s, 0.0d);

            PriorityQueue<QueueElement> q = new PriorityQueue<>();

            q.add(new QueueElement(0d, s, s));

            // check that everything has the length it should.
            assert stack.empty(); //S should be empty
            assert seen.size() == 1;

            while (q.peek() != null) {
                QueueElement first = q.poll();
                Double dist = first.dist;
                Integer pred = first.pred;
                Integer v = first.v;

                if (d.containsKey(v)) {
                    continue;
                }

                sigma.put(v, sigma.get(v) + sigma.get(pred));
                stack.push(v);
                d.put(v, dist);

                // iterate all edges connected to v
                EdgeExplorer explorer = graph.createEdgeExplorer();
                EdgeIterator iter = explorer.setBaseNode(v);

                while (iter.next()) {
                    int w = iter.getAdjNode(); // this is the node this edge state is "pointing to"
                    if (!nodesInBBox.contains(w)) {
                        // Node not in bbox, skipping edge
                        continue;
                    }

                    if (d.containsKey(w)) { // This is only possible if weights are always bigger than 0, which should be given for real-world examples.
                        // Node already checked, skipping edge
                        continue;
                    }

                    Double vwDist = dist + weighting.calcWeight(iter, false, EdgeIterator.NO_EDGE);

                    if (seen.containsKey(w) && (Math.abs(vwDist - seen.get(w)) < 0.000001d)) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        List<Integer> predecessors = p.get(w);
                        predecessors.add(v);
                        p.put(w, predecessors);
                    } else if (!seen.containsKey(w) || vwDist < seen.get(w)) {
                        seen.put(w, vwDist);
                        q.add(new QueueElement(vwDist, v, w));
                        sigma.put(w, 0);
                        ArrayList<Integer> predecessors = new ArrayList<>();
                        predecessors.add(v);
                        p.put(w, predecessors);
                    }
                }
            }


            // accumulate betweenness
            Map<Integer, Double> delta = new HashMap<>();

            for (Integer v : stack) {
                delta.put(v, 0.0d);
            }

            while (!stack.empty()) {
                Integer w = stack.pop();
                Double coefficient = (1 + delta.get(w)) / sigma.get(w);
                for (Integer v : p.get(w)) {
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