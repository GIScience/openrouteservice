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
package heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EdgeFilterFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.Polygon;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.parameters.WheelchairParameters;
import org.apache.log4j.Logger;

public class ORSEdgeFilterFactory implements EdgeFilterFactory {
    private static final Logger LOGGER = Logger.getLogger(ORSEdgeFilterFactory.class.getName());

    @Override
    public EdgeFilter createEdgeFilter(AlgorithmOptions opts, GraphHopperStorage gs) {
        FlagEncoder flagEncoder = opts.getWeighting().getFlagEncoder();

        /* Initialize empty edge filter sequence */
        EdgeFilterSequence edgeFilters = new EdgeFilterSequence();

        /* Default edge filter which accepts both directions of the specified vehicle */
        edgeFilters.add(DefaultEdgeFilter.allEdges(flagEncoder));

        try {
            ORSPMap params = (ORSPMap) opts.getHints();
        
            /* Avoid areas */
            if (params.hasObj("avoid_areas")) {
                edgeFilters.add(new AvoidAreasEdgeFilter((Polygon[]) params.getObj("avoid_areas")));
            }
    
            /* Heavy vehicle filter */
            if (params.hasObj("hgv_params") && params.hasObj("hgv_type")) {
                edgeFilters.add(new HeavyVehicleEdgeFilter(flagEncoder, params.getInt("hgv_type", 0), (VehicleParameters)params.getObj("hgv_params"), gs));
            }
            else if (params.hasObj("emergency_params")) {
                edgeFilters.add(new EmergencyVehicleEdgeFilter((VehicleParameters)params.getObj("emergency_params"), gs));
            }

            /* Wheelchair filter */
            else if (params.hasObj("wheelchair_params")) {
                edgeFilters.add(new WheelchairEdgeFilter((WheelchairParameters)params.getObj("wheelchair_params"), gs));
            }
    
            /* Avoid features */
            if (params.hasObj("avoid_features") && params.hasObj("avoid_features_type")) {
                edgeFilters.add(new AvoidFeaturesEdgeFilter(params.getInt("avoid_features_type", 0), (RouteSearchParameters) params.getObj("avoid_features"), gs));
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
