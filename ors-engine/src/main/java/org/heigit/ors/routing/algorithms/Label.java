package org.heigit.ors.routing.algorithms;

import com.graphhopper.util.EdgeIterator;

public class Label implements Cloneable, Comparable<Label> {
    public int edgeId;
    public int nodeId;
    public double weight;
    public double sinceRest;
    public Label parent;

    public Label(int edgeId, int nodeId, double weight, double sinceRest) {
        this.edgeId = edgeId;
        this.nodeId = nodeId;
        this.weight = weight;
        this.sinceRest = sinceRest;
    }

    public static Label createStartLabel(int nodeId) {
        return new Label(EdgeIterator.NO_EDGE, nodeId, 0, 0);
    }

    /**
     * This method returns the weight to the origin e.g. to the start for the forward SPT and to the
     * destination for the backward SPT. Where the variable 'weight' is used to let heap select
     * smallest *full* weight (from start to destination).
     */
    public double getWeightOfVisitedPath() {
        return weight;
    }

    public Label getParent() {
        return parent;
    }

    @Override
    public Label clone() {
        return new Label(edgeId, nodeId, weight, sinceRest);
    }

    public Label cloneFull() {
        Label de = clone();
        Label tmpPrev = parent;
        Label cl = de;
        while (tmpPrev != null) {
            cl.parent = tmpPrev.clone();
            cl = cl.parent;
            tmpPrev = tmpPrev.parent;
        }
        return de;
    }

    @Override
    public int compareTo(Label o) {
        if (weight < o.weight)
            return -1;

        // assumption no NaN and no -0
        return weight > o.weight ? 1 : 0;
    }

    @Override
    public String toString() {
        return nodeId + " (" + edgeId + ") weight: " + weight;
    }
}
