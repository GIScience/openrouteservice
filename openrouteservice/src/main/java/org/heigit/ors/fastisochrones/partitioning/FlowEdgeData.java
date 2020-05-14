package org.heigit.ors.fastisochrones.partitioning;

/**
 * Data element for partitioning.
 *
 * @author Hendrik Leuschner
 */
public class FlowEdgeData {
    public boolean flow;
    public int inverse;

    public FlowEdgeData(boolean flow, int inverse) {
        this.flow = flow;
        this.inverse = inverse;
    }
}
