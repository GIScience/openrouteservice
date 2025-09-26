package org.heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.ev.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.routing.AvoidFeatureFlags;

// keep in sync with waycategory.md
//
// This is a transitional class only. Once all extended
// storages are transformed into encoded values and the
// AvoidFeatures are replaced by custom profiles, this
// class can be removed.
public class FormerWayCategory {
    private BooleanEncodedValue highwayEnc = null;
    private BooleanEncodedValue fordEnc = null;
    private EnumEncodedValue<WayType> wayTypeEnc = null;
    private final int avoidFeatureType;

    public FormerWayCategory(GraphHopperStorage graphStorage, int avoidFeatureType) {
        var encodingManager = graphStorage.getEncodingManager();
        if (encodingManager.hasEncodedValue(Ford.KEY))
            fordEnc = encodingManager.getBooleanEncodedValue(Ford.KEY);
        if (encodingManager.hasEncodedValue(Highway.KEY))
            highwayEnc = encodingManager.getBooleanEncodedValue(Highway.KEY);
        if (encodingManager.hasEncodedValue(WayType.KEY))
            wayTypeEnc = encodingManager.getEnumEncodedValue(WayType.KEY, WayType.class);
        this.avoidFeatureType = avoidFeatureType;
    }

    public final int oldStyleEdgeValue(EdgeIteratorState iter) {
        return (acceptHighways(iter) ? 0 : AvoidFeatureFlags.HIGHWAYS)
            | (acceptFords(iter) ? 0 : AvoidFeatureFlags.FORDS)
            | (acceptSteps(iter) ? 0 : AvoidFeatureFlags.STEPS)
            | (acceptFerries(iter) ? 0 : AvoidFeatureFlags.FERRIES);
    }

    public final boolean accept(EdgeIteratorState iter) {
        return avoidFeatureType == 0
                || (acceptHighways(iter)
                && acceptFords(iter)
                && acceptSteps(iter)
                && acceptFerries(iter)
        );
    }

    private boolean acceptFerries(EdgeIteratorState iter) {
        return wayTypeEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.FERRIES) == 0
                || iter.get(wayTypeEnc) != WayType.FERRY;
    }

    private boolean acceptSteps(EdgeIteratorState iter) {
        return wayTypeEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.STEPS) == 0
                || iter.get(wayTypeEnc) != WayType.STEPS;
    }

    private boolean acceptFords(EdgeIteratorState iter) {
        return fordEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.FORDS) == 0
                || !iter.get(fordEnc);
    }

    private boolean acceptHighways(EdgeIteratorState iter) {
        return highwayEnc == null
                || (avoidFeatureType & AvoidFeatureFlags.HIGHWAYS) == 0
                || !iter.get(highwayEnc);
    }
}
