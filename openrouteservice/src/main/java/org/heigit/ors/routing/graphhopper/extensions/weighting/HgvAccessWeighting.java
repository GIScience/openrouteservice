package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class HgvAccessWeighting extends AbstractAdjustedWeighting {
    EdgeFilter hgvAccessFilter;

    public HgvAccessWeighting(Weighting superWeighting, EdgeFilter hgvAccessFilter) {
        super(superWeighting);
        this.hgvAccessFilter = hgvAccessFilter;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId, long time) {
        if (!hgvAccessFilter.accept(edge))
            return Double.POSITIVE_INFINITY;
        return superWeighting.calcWeight(edge, reverse, prevOrNextEdgeId, time);
    }

    @Override
    public String toString() {
        return "hgv_access|" + this.superWeighting.toString();
    }

    public String getName() {
        return this.superWeighting.getName();
    }
}
