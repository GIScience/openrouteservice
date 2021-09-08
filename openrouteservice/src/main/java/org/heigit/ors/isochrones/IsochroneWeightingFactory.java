package org.heigit.ors.isochrones;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.ORSWeightingFactory;

public class IsochroneWeightingFactory {
    private IsochroneWeightingFactory() {}

    public static Weighting createIsochroneWeighting(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
        PMap hintsMap;
        if (travelRangeType == TravelRangeType.TIME) {
            hintsMap = new PMap("weighting=fastest").putObject("isochroneWeighting", "true");
        } else {
            hintsMap = new PMap("weighting=shortest").putObject("isochroneWeighting", "true");
        }
        return new ORSWeightingFactory().createWeighting(hintsMap, searchContext.getEncoder(), searchContext.getGraphHopper().getGraphHopperStorage());
    }
}
