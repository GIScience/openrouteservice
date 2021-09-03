package org.heigit.ors.fastisochrones.partitioning;

/**
 * Data element for partitioning.
 *
 * @author Hendrik Leuschner
 */
public class FlowEdgeData {
    private boolean flow;
    private int inverse;

    public FlowEdgeData(boolean flow, int inverse) {
        this.flow = flow;
        this.inverse = inverse;
    }

    public boolean isFlow() {
        return flow;
    }

    public void setFlow(boolean flow) {
        this.flow = flow;
    }

    public int getInverse() {
        return inverse;
    }

    public void setInverse(int inverse) {
        this.inverse = inverse;
    }
}
