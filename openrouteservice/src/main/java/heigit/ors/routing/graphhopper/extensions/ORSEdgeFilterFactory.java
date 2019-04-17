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
import heigit.ors.routing.ProfileWeighting;
import heigit.ors.routing.RouteSearchContext;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.routing.graphhopper.extensions.edgefilters.*;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.parameters.WheelchairParameters;
import heigit.ors.routing.traffic.RealTrafficDataProvider;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class ORSEdgeFilterFactory implements EdgeFilterFactory {

    @Override
    public EdgeFilter createEdgeFilter(AlgorithmOptions opts) {

        FlagEncoder flagEncoder = opts.getWeighting().getFlagEncoder();

        /* Initialize empty edge filter sequence */

        EdgeFilterSequence edgeFilters = new EdgeFilterSequence();

        /* Default edge filter which accepts both directions of the specified vehicle */

        edgeFilters.add(DefaultEdgeFilter.allEdges(flagEncoder));

        /* Avoid areas */

        if (searchParams.hasAvoidAreas()) {
            props.put("avoid_areas", true);
            edgeFilters.add(new AvoidAreasEdgeFilter(searchParams.getAvoidAreas()));
        }

        /* Heavy vehicle filter */

        if (RoutingProfileType.isDriving(profileType)) {
            if (RoutingProfileType.isHeavyVehicle(profileType) && searchParams.hasParameters(VehicleParameters.class)) {
                VehicleParameters vehicleParams = (VehicleParameters) profileParams;

                if (vehicleParams.hasAttributes()) {

                    if (profileType == RoutingProfileType.DRIVING_HGV)
                        edgeFilters.add(new HeavyVehicleEdgeFilter(flagEncoder, searchParams.getVehicleType(), vehicleParams, gs));
                    else if (profileType == RoutingProfileType.DRIVING_EMERGENCY)
                        edgeFilters.add(new EmergencyVehicleEdgeFilter(vehicleParams, gs));
                }
            }
        }

        /* Wheelchair filter */

        else if (profileType == RoutingProfileType.WHEELCHAIR && searchParams.hasParameters(WheelchairParameters.class)) {
            edgeFilters.add(new WheelchairEdgeFilter((WheelchairParameters) profileParams, gs));
        }

        /* Avoid features */

        if (searchParams.hasAvoidFeatures()) {
            props.put("avoid_features", searchParams.getAvoidFeatureTypes());
            edgeFilters.add(new AvoidFeaturesEdgeFilter(profileType, searchParams, gs));
        }

        /* Avoid borders of some form */

        if (searchParams.hasAvoidBorders() || searchParams.hasAvoidCountries()) {
            if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)) {
                edgeFilters.add(new AvoidBordersEdgeFilter(searchParams, gs));
                if(searchParams.hasAvoidCountries())
                    props.put("avoid_countries", Arrays.toString(searchParams.getAvoidCountries()));
            }
        }


        if (profileParams != null && profileParams.hasWeightings()) {
            props.put("custom_weightings", true);
            Iterator<ProfileWeighting> iterator = profileParams.getWeightings().getIterator();
            while (iterator.hasNext()) {
                ProfileWeighting weighting = iterator.next();
                if (!weighting.getParameters().isEmpty()) {
                    String name = ProfileWeighting.encodeName(weighting.getName());
                    for (Map.Entry<String, String> kv : weighting.getParameters().toMap().entrySet())
                        props.put(name + kv.getKey(), kv.getValue());
                }
            }
        }

        /* Live traffic filter - currently disabled */

//        if (searchParams.getConsiderTraffic()) {
//            RealTrafficDataProvider trafficData = RealTrafficDataProvider.getInstance();
//            if (RoutingProfileType.isDriving(profileType) && searchParams.getWeightingMethod() != WeightingMethod.SHORTEST && trafficData.isInitialized()) {
//                props.put("weighting_traffic_block", true);
//                edgeFilters.add(new BlockedEdgesEdgeFilter(flagEncoder, trafficData.getBlockedEdges(gs), trafficData.getHeavyVehicleBlockedEdges(gs)));
//            }
//        }

        RouteSearchContext searchCntx = new RouteSearchContext(mGraphHopper, edgeFilters, flagEncoder);    }
}
