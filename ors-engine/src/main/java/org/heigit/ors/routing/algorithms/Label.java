package org.heigit.ors.routing.algorithms;

import com.graphhopper.util.EdgeIterator;
import lombok.Getter;
import lombok.Setter;

public class Label implements Comparable<Label> {
    public int edgeId;
    public int nodeId;
    public double weight;
    public double sinceRest;
    @Getter
    public Label parent;
    @Getter
    @Setter
    private boolean active = true;

    public Label(int edgeId, int nodeId, double weight, double sinceRest) {
        this.edgeId = edgeId;
        this.nodeId = nodeId;
        this.weight = weight;
        this.sinceRest = sinceRest;
    }


    public static Label createStartLabel(int nodeId) {
        return new Label(EdgeIterator.NO_EDGE, nodeId, 0, 0);
    }

    @SuppressWarnings("java:S1210")
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
