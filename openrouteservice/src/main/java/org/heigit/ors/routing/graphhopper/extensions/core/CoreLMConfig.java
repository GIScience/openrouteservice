package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.lm.LMConfig;
import com.graphhopper.routing.weighting.Weighting;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;

import java.util.Objects;

public class CoreLMConfig extends LMConfig {
    LMEdgeFilterSequence edgeFilter;

    public CoreLMConfig(String profileName, Weighting weighting) {
        super(profileName, weighting);
    }

    public CoreLMConfig setEdgeFilter(LMEdgeFilterSequence edgeFilter) {
        this.edgeFilter = edgeFilter;
        return this;
    }

    public LMEdgeFilterSequence getEdgeFilter() {
        return edgeFilter;
    }

    @Override
    public String getName() {
        return getSuperName() + "_" + edgeFilter.getName();
    }

    public String getSuperName() {
        return super.getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        } else if (o != null && this.getClass() == o.getClass()) {
            CoreLMConfig lmConfig = (CoreLMConfig) o;
            return Objects.equals(this.edgeFilter, lmConfig.edgeFilter);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (getSuperName() + edgeFilter.toString()).hashCode();
    }
}
