package org.heigit.ors.routing.algorithms;

import com.graphhopper.routing.SPTEntry;
import com.graphhopper.util.EdgeIterator;

public class Label extends SPTEntry {
    public double sinceRest;
    private boolean active = true;

    public Label(int edgeId, int nodeId, double weight, double sinceRest) {
        super(edgeId, nodeId, weight);
        this.sinceRest = sinceRest;
    }

    public static Label createStartLabel(int nodeId) {
        return new Label(EdgeIterator.NO_EDGE, nodeId, 0, 0);
    }

    @Override
    public Label getParent() {
        return (Label) super.parent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
