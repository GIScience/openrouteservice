package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.ch.CHPreparationGraph;
import com.graphhopper.routing.ch.PrepareGraphEdgeExplorer;

public class CorePreparationGraph extends CHPreparationGraph {
    public static CorePreparationGraph nodeBased(int nodes, int edges) {
        return new CorePreparationGraph(nodes, edges, false, (in, via, out) -> 0);
    }

    public static CorePreparationGraph edgeBased(int nodes, int edges, TurnCostFunction turnCostFunction) {
        return new CorePreparationGraph(nodes, edges, true, turnCostFunction);
    }

    /**
     * @param nodes (fixed) number of nodes of the graph
     * @param edges the maximum number of (non-shortcut) edges in this graph. edges-1 is the maximum edge id that may
     *              be used.
     */
    private CorePreparationGraph(int nodes, int edges, boolean edgeBased, TurnCostFunction turnCostFunction) {
        super(nodes, edges, edgeBased, turnCostFunction);
    }

    public int addShortcut(int from, int to, int skipped1, int skipped2, double weight, int time, int origEdgeCount) {
        this.checkReady();
        PrepareEdge prepareEdge = new PrepareCoreShortcut(this.nextShortcutId, from, to, weight, time, skipped1, skipped2, origEdgeCount);
        this.addOutEdge(from, prepareEdge);
        if (from != to) {
            this.addInEdge(to, prepareEdge);
        }

        return this.nextShortcutId++;
    }


    public void addEdge(int from, int to, int edge, double weightFwd, double weightBwd, int timeFwd, int timeBwd) {
        checkNotReady();
        boolean fwd = Double.isFinite(weightFwd);
        boolean bwd = Double.isFinite(weightBwd);
        if (!fwd && !bwd)
            return;
        PrepareBaseEdge prepareEdge = new PrepareCoreBaseEdge(edge, from, to, (float) weightFwd, (float) weightBwd, timeFwd, timeBwd);
        if (fwd) {
            addOutEdge(from, prepareEdge);
            addInEdge(to, prepareEdge);
        }
        if (bwd && from != to) {
            addOutEdge(to, prepareEdge);
            addInEdge(from, prepareEdge);
        }
    }

    @Override
    public PrepareGraphEdgeExplorer createOutEdgeExplorer() {
        checkReady();
        return new PrepareCoreGraphEdgeExplorerImpl(getPrepareEdgesOut(), false);
    }

    @Override
    public PrepareGraphEdgeExplorer createInEdgeExplorer() {
        checkReady();
        return new PrepareCoreGraphEdgeExplorerImpl(getPrepareEdgesIn(), true);
    }

    public static class PrepareCoreBaseEdge extends PrepareBaseEdge {
        private final int timeAB;
        private final int timeBA;

        public PrepareCoreBaseEdge(int prepareEdge, int nodeA, int nodeB, float weightAB, float weightBA, int timeAB, int timeBA) {
            super(prepareEdge, nodeA, nodeB, weightAB, weightBA);
            this.timeAB = timeAB;
            this.timeBA = timeBA;
        }

        public int getTimeAB() {
            return timeAB;
        }

        public int getTimeBA() {
            return timeBA;
        }

    }

    public static class PrepareCoreShortcut extends PrepareShortcut {
        private int time;

        private PrepareCoreShortcut(int prepareEdge, int from, int to, double weight, int time, int skipped1, int skipped2, int origEdgeCount) {
            super(prepareEdge, from, to, weight, skipped1, skipped2, origEdgeCount);
            this.time = time;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }

    public static class PrepareCoreGraphEdgeExplorerImpl extends PrepareGraphEdgeExplorerImpl {

        public PrepareCoreGraphEdgeExplorerImpl(PrepareEdge[] prepareEdges, boolean reverse) {
            super(prepareEdges, reverse);
        }

        @Override
        public int getTime() {
            if (isShortcut()) {
                return ((PrepareCoreShortcut) currEdge).getTime();
            }
            else {
                PrepareCoreBaseEdge baseEdge = (PrepareCoreBaseEdge) currEdge;
                if (nodeAisBase()) {
                    return reverse ? baseEdge.getTimeBA() : baseEdge.getTimeAB();
                } else {
                    return reverse ? baseEdge.getTimeAB() : baseEdge.getTimeBA();
                }
            }
        }

    }

}
