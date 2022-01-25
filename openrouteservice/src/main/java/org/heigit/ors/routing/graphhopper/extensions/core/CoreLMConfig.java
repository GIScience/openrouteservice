package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.lm.LMConfig;
import com.graphhopper.routing.weighting.Weighting;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;

public class CoreLMConfig extends LMConfig {
    LMEdgeFilterSequence edgeFilter;

    public CoreLMConfig(String profileName, Weighting weighting, LMEdgeFilterSequence edgeFilter) {
        super(profileName, weighting);
        this.edgeFilter = edgeFilter;
    }

    public LMEdgeFilterSequence getEdgeFilter() {
        return edgeFilter;
    }

    @Override
    public String getName() {
        return super.getName() + "_" + edgeFilter.getName();
    }

    public String getSuperName() {
        return super.getName();
    }
}
