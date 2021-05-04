/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.isochrones;

import com.graphhopper.util.Helper;
import org.heigit.ors.isochrones.builders.IsochroneMapBuilder;
import org.heigit.ors.isochrones.builders.concaveballs.ConcaveBallsIsochroneMapBuilder;
import org.heigit.ors.isochrones.builders.fast.FastIsochroneMapBuilder;
import org.heigit.ors.isochrones.builders.grid.GridBasedIsochroneMapBuilder;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;

public class IsochroneMapBuilderFactory {
    private final RouteSearchContext searchContext;

    public IsochroneMapBuilderFactory(RouteSearchContext searchContext) {
        this.searchContext = searchContext;
    }


    public IsochroneMap buildMap(IsochroneSearchParameters parameters) throws Exception {
        IsochroneMapBuilder isochroneBuilder = createIsochroneMapBuilder(parameters);

        isochroneBuilder.initialize(searchContext);
        return isochroneBuilder.compute(parameters);
    }
    
    private IsochroneMapBuilder createIsochroneMapBuilder(IsochroneSearchParameters parameters) throws IllegalArgumentException{
        IsochroneMapBuilder isochroneBuilder;
        String method = parameters.getCalcMethod();
        boolean canUseFastIsochrones = !(parameters.getRouteParameters().requiresDynamicPreprocessedWeights() || parameters.getRouteParameters().requiresFullyDynamicWeights() || parameters.getReverseDirection());
        if (Helper.isEmpty(method) || "FastIsochrone".equalsIgnoreCase(method) || "Default".equalsIgnoreCase(method)) {
            if (canUseFastIsochrones &&
                    ((ORSGraphHopper) searchContext.getGraphHopper()).isFastIsochroneAvailable(searchContext, parameters.getRangeType()))
                isochroneBuilder = new FastIsochroneMapBuilder();
            else
                isochroneBuilder = new ConcaveBallsIsochroneMapBuilder();
        } else if ("ConcaveBalls".equalsIgnoreCase(method)) {
            isochroneBuilder = new ConcaveBallsIsochroneMapBuilder();
        } else if ("grid".equalsIgnoreCase(method)) {
            isochroneBuilder = new GridBasedIsochroneMapBuilder();
        } else {
            throw new IllegalArgumentException("Unknown method.");
        }
        return isochroneBuilder;
    }
}
