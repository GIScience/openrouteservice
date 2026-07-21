package org.heigit.ors.routing.algorithms;

import com.graphhopper.util.EdgeIterator;

public class MultiLabelSPTEntry implements Cloneable, Comparable<com.graphhopper.routing.SPTEntry> {
    public int edge;
    // ORS-GH MOD START
    // add field
    public int originalEdge;
    // ORS-GH MOD END
    public int adjNode;
    public double weight;
    public long time; // ORS-GH MOD additional field
    public com.graphhopper.routing.SPTEntry parent;

    public MultiLabelSPTEntry(int edgeId, int adjNode, double weight) {
        this.edge = edgeId;
        // ORS-GH MOD START
        this.originalEdge = edgeId;
        // ORS-GH MOD END
        this.adjNode = adjNode;
        this.weight = weight;
    }

    public MultiLabelSPTEntry(int node, double weight) {
        this(EdgeIterator.NO_EDGE, node, weight);
    }

    /**
     * This method returns the weight to the origin e.g. to the start for the forward SPT and to the
     * destination for the backward SPT. Where the variable 'weight' is used to let heap select
     * smallest *full* weight (from start to destination).
     */
    public double getWeightOfVisitedPath() {
        return weight;
    }

    public com.graphhopper.routing.SPTEntry getParent() {
        return parent;
    }

    @Override
    public com.graphhopper.routing.SPTEntry clone() {
        return new com.graphhopper.routing.SPTEntry(edge, adjNode, weight);
    }

    public com.graphhopper.routing.SPTEntry cloneFull() {
        com.graphhopper.routing.SPTEntry de = clone();
        com.graphhopper.routing.SPTEntry tmpPrev = parent;
        com.graphhopper.routing.SPTEntry cl = de;
        while (tmpPrev != null) {
            cl.parent = tmpPrev.clone();
            cl = cl.parent;
            tmpPrev = tmpPrev.parent;
        }
        return de;
    }

    @Override
    public int compareTo(com.graphhopper.routing.SPTEntry o) {
        if (weight < o.weight)
            return -1;

        // assumption no NaN and no -0
        return weight > o.weight ? 1 : 0;
    }

    @Override
    public String toString() {
        return adjNode + " (" + edge + ") weight: " + weight;
    }
}
