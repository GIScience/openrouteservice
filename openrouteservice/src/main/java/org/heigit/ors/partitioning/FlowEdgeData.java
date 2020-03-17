package org.heigit.ors.partitioning;
/**
 * Data element for partitioning.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class FlowEdgeData {
    public boolean flow = false;
    public int inverse = -1;

    public FlowEdgeData(boolean flow, int inverse) {
        this.flow = flow;
        this.inverse = inverse;
    }

}
