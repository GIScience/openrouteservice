package org.heigit.ors.isochrones;

import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;

public class IsochroneWeightingFactory {
    public static Weighting createIsochroneWeighting(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
        HintsMap hintsMap;
        if (travelRangeType == TravelRangeType.TIME) {
            hintsMap = new HintsMap("fastest").put("isochroneWeighting", "true");
        } else {
            hintsMap = new HintsMap("shortest").put("isochroneWeighting", "true");
        }
        return new ORSWeightingFactory().createWeighting(hintsMap, searchContext.getEncoder(), searchContext.getGraphHopper().getGraphHopperStorage());
    }
}
