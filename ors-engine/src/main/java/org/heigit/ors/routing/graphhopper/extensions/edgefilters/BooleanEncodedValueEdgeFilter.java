package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIteratorState;

public class BooleanEncodedValueEdgeFilter implements EdgeFilter {
    BooleanEncodedValue booleanEncodedValue;


    public BooleanEncodedValueEdgeFilter(BooleanEncodedValue booleanEncodedValue) {
        this.booleanEncodedValue = booleanEncodedValue;
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        return !booleanEncodedValue.getBool(false, edgeState.getFlags());
    }
}
