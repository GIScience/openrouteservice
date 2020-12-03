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

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import com.vividsolutions.jts.geom.Polygon;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.heigit.ors.routing.parameters.VehicleParameters;
import org.heigit.ors.routing.parameters.WheelchairParameters;
import org.apache.log4j.Logger;

public class ORSEdgeFilterFactory implements EdgeFilterFactory {
    private static final Logger LOGGER = Logger.getLogger(ORSEdgeFilterFactory.class.getName());

    @Override
    public EdgeFilter createEdgeFilter(PMap opts, FlagEncoder flagEncoder, GraphHopperStorage gs) {
        /* Initialize empty edge filter sequence */
        EdgeFilterSequence edgeFilters = new EdgeFilterSequence();

        /* Default edge filter which accepts both directions of the specified vehicle */
        edgeFilters.add(DefaultEdgeFilter.allEdges(flagEncoder));

        try {
            ORSPMap params = (ORSPMap)opts;
            if (params == null) {
                params = new ORSPMap();
            }

            /* Avoid areas */
            if (params.hasObj("avoid_areas")) {
                edgeFilters.add(new AvoidAreasEdgeFilter((Polygon[]) params.getObj("avoid_areas")));
            }
    
            /* Heavy vehicle filter */
            if (params.has("edgefilter_hgv")) {
                edgeFilters.add(new HeavyVehicleEdgeFilter(params.getInt("edgefilter_hgv", 0), (VehicleParameters)params.getObj("routing_profile_params"), gs));
            }

            /* Wheelchair filter */
            else if (params.has("edgefilter_wheelchair")) {
                edgeFilters.add(new WheelchairEdgeFilter((WheelchairParameters)params.getObj("routing_profile_params"), gs));
            }
    
            /* Avoid features */
            if (params.hasObj("avoid_features") && params.has("routing_profile_type")) {
                edgeFilters.add(new AvoidFeaturesEdgeFilter(params.getInt("routing_profile_type", 0), (RouteSearchParameters) params.getObj("avoid_features"), gs));
            }
    
            /* Avoid borders */
            if (params.hasObj("avoid_borders")) {
                edgeFilters.add(new AvoidBordersEdgeFilter((RouteSearchParameters) params.getObj("avoid_borders"), gs));
            }
            
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        return edgeFilters;
    }
}
