package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.ArrayUtil;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;

public class MultiLabelPathExtractor  {
    public static Path extract(Graph graph, Weighting weighting, Label entry) {
        Path path = new Path(graph);
        if (entry == null) {
            // path not found
            return path;
        }
        path.setFound(true);
        path.setWeight(entry.weight);
        path.setEndNode(entry.nodeId);
        path.setFromNode(extractPath(graph, weighting, entry, path));
        return path;
    }

    private static int extractPath(Graph graph, Weighting weighting, Label entry, Path path) {
        Label currentLabel = followParentsUntilRoot(entry, graph, weighting, path);
        ArrayUtil.reverse(path.getEdges());
        return currentLabel.nodeId;
    }

    private static Label followParentsUntilRoot(Label entry, Graph graph, Weighting weighting, Path path) {
        Label parentEntry = entry.parent;
        while (EdgeIterator.Edge.isValid(entry.edgeId)) {
            onEdge(entry.edgeId, entry.nodeId, parentEntry.edgeId, graph, weighting, path);
            entry = entry.parent;
            parentEntry = entry.parent;
        }
        return entry;
    }

    protected static void onEdge(int edge, int adjNode, int prevEdge, Graph graph, Weighting weighting, Path path) {
        EdgeIteratorState edgeState = graph.getEdgeIteratorState(edge, adjNode);
        path.addDistance(edgeState.getDistance());
        path.addTime(GHUtility.calcMillisWithTurnMillis(weighting, edgeState, false, prevEdge));
        path.addEdge(edge);
    }
}
