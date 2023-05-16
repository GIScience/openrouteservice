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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.locationtech.jts.geom.Polygon;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.apache.log4j.Logger;

public class ORSEdgeFilterFactory implements EdgeFilterFactory {
    private static final Logger LOGGER = Logger.getLogger(ORSEdgeFilterFactory.class.getName());

    public EdgeFilter createEdgeFilter(PMap opts, FlagEncoder flagEncoder, GraphHopperStorage gs) {
        return createEdgeFilter(opts, flagEncoder, gs, null);
    }

    public EdgeFilter createEdgeFilter(PMap opts, FlagEncoder flagEncoder, GraphHopperStorage gs, EdgeFilter prependFilter) {
        /* Initialize empty edge filter sequence */
        EdgeFilterSequence edgeFilters = new EdgeFilterSequence();

        if (prependFilter != null)
            edgeFilters.add(prependFilter);

        /* Default edge filter which accepts both directions of the specified vehicle */
        edgeFilters.add(AccessFilter.allEdges(flagEncoder.getAccessEnc()));
        try {
            if (opts == null) {
                opts = new PMap();
            }

            /* Avoid areas */
            if (opts.has("avoid_areas")) {
                edgeFilters.add(new AvoidAreasEdgeFilter(opts.getObject("avoid_areas", new Polygon[]{})));
            }
    
            /* Heavy vehicle filter */
            if (opts.has("edgefilter_hgv")) {
                edgeFilters.add(new HeavyVehicleEdgeFilter(opts.getInt("edgefilter_hgv", 0), opts.getObject("routing_profile_params", new VehicleParameters()), gs));
            }

            /* Wheelchair filter */
            else if (opts.has("edgefilter_wheelchair")) {
                edgeFilters.add(new WheelchairEdgeFilter(opts.getObject("routing_profile_params", new WheelchairParameters()), gs));
            }
    
            /* Avoid features */
            if (opts.has("avoid_features") && opts.has("routing_profile_type")) {
                edgeFilters.add(new AvoidFeaturesEdgeFilter(opts.getInt("routing_profile_type", 0), opts.getObject("avoid_features", new RouteSearchParameters()), gs));
            }
    
            /* Avoid borders */
            if (opts.has("avoid_borders")) {
                edgeFilters.add(new AvoidBordersEdgeFilter(opts.getObject("avoid_borders", new RouteSearchParameters()), gs));
            }
            
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        return edgeFilters;
    }
}
