/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.routing;

import com.graphhopper.storage.GraphExtension;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SimpleRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;

public class RouteExtraInfoHolder {
    private RouteExtraInfo extraInfo;
    private RouteExtraInfoBuilder extraInfoBuilder;
    private GraphExtension extension;

    public RouteExtraInfoHolder(int extensionId, ORSGraphHopper graphHopper) {
        switch(extensionId) {
            case RouteExtraInfoFlag.Steepness:
                extraInfo = new RouteExtraInfo("steepness");
                extraInfoBuilder = new SteepnessExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), AccessRestrictionsGraphStorage.class);
                break;
            case RouteExtraInfoFlag.Surface:
                extraInfo = new RouteExtraInfo("surface");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WaySurfaceTypeGraphStorage.class);
                break;
            case RouteExtraInfoFlag.WayType:
                extraInfo = new RouteExtraInfo("waytypes");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WaySurfaceTypeGraphStorage.class);
                break;
            case RouteExtraInfoFlag.WayCategory:
                extraInfo = new RouteExtraInfo("waycategory");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                break;
            case RouteExtraInfoFlag.Suitability:
                extraInfo = new RouteExtraInfo("suitability");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                break;
            case RouteExtraInfoFlag.Green:
                extraInfo = new RouteExtraInfo("green");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), GreenIndexGraphStorage.class);
                break;
            case RouteExtraInfoFlag.Noise:
                extraInfo = new RouteExtraInfo("noise");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), NoiseIndexGraphStorage.class);
                break;
            case RouteExtraInfoFlag.AvgSpeed:
                extraInfo = new RouteExtraInfo("avgspeed");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                break;
            case RouteExtraInfoFlag.Tollways:
                extraInfo = new RouteExtraInfo("tollways");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), TollwaysGraphStorage.class);
                break;
            case RouteExtraInfoFlag.OsmId:
                extraInfo = new RouteExtraInfo("osmid");
                extraInfoBuilder = new SimpleRouteExtraInfoBuilder(extraInfo);
                extension = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), OsmIdGraphStorage.class);
                break;
        }
    }

    public RouteExtraInfo getExtraInfo() {
        return extraInfo;
    }

    public RouteExtraInfoBuilder getBuilder() {
        return extraInfoBuilder;
    }

    public GraphExtension getExtension() {
        return extension;
    }
}
