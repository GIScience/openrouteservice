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

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.EdgeIteratorState;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.plugins.PluginManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.HereTrafficGraphStorageBuilder;
import org.heigit.ors.util.ProfileTools;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GraphProcessContext {
    private static final Logger LOGGER = Logger.getLogger(GraphProcessContext.class.getName());
    private List<GraphStorageBuilder> storageBuilders;
    private GraphStorageBuilder[] arrStorageBuilders;
    private int trafficArrStorageBuilderLocation = -1;
    private final double maximumSpeedLowerBound;
    private boolean getElevationFromPreprocessedData;

    private boolean useSidewalks;

    public GraphProcessContext(ProfileProperties profile) throws Exception {
        PluginManager<GraphStorageBuilder> mgrGraphStorageBuilders = PluginManager.getPluginManager(GraphStorageBuilder.class);
        if (profile.getBuild().getExtStorages() != null) {
            if (profile.getBuild().getExtStorages().containsKey("HereTraffic")) {
                ExtendedStorageProperties parameters;
                try {
                    parameters = profile.getBuild().getExtStorages().get("HereTraffic");
                    parameters.setGhProfile(ProfileTools.makeProfileName(RoutingProfileType.getEncoderName(RoutingProfileType.getFromString(profile.getEncoderName().toString())), "fastest", Boolean.TRUE.equals(profile.getBuild().getEncoderOptions().getTurnCosts())));
                } catch (ClassCastException e) {
                    throw new UnsupportedOperationException("GraphStorageBuilder configuration object is malformed.");
                }
            }
            storageBuilders = mgrGraphStorageBuilders.createInstances(profile.getBuild().getExtStorages());
        }
        maximumSpeedLowerBound = profile.getBuild().getMaximumSpeedLowerBound() == null ? 80 : profile.getBuild().getMaximumSpeedLowerBound();
        useSidewalks = EncoderNameEnum.isPedestrian(profile.getEncoderName().getValue());
    }

    public void initArrays() {
        if (storageBuilders != null && !storageBuilders.isEmpty()) {
            arrStorageBuilders = new GraphStorageBuilder[storageBuilders.size()];
            arrStorageBuilders = storageBuilders.toArray(arrStorageBuilders);
        }
    }

    public void processWay(ReaderWay way) {
        try {
            if (arrStorageBuilders != null) {
                for (GraphStorageBuilder builder : arrStorageBuilders) {
                    builder.processWay(way);
                }
            }
        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
        }
    }

    /**
     * Pass the way read along with its geometry (a LineString) to the graph storage builders.
     *
     * @param way      The OSM data for the way (including tags)
     * @param coords   Coordinates of the linestring
     * @param nodeTags Tags for nodes found on the way
     */
    public void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String, String>> nodeTags, Coordinate[] allCoordinates) {
        try {
            if (arrStorageBuilders != null) {
                int nStorages = arrStorageBuilders.length;
                if (nStorages > 0) {
                    for (int i = 0; i < nStorages; ++i) {
                        if (trafficArrStorageBuilderLocation == -1 && arrStorageBuilders[i].getName().equals(HereTrafficGraphStorageBuilder.BUILDER_NAME)) {
                            trafficArrStorageBuilderLocation = i;
                        }
                        arrStorageBuilders[i].processWay(way, coords, nodeTags);
                    }
                    if (trafficArrStorageBuilderLocation >= 0) {
                        arrStorageBuilders[trafficArrStorageBuilderLocation].processWay(way, allCoordinates, nodeTags);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage() + ". Way id = " + way.getId());
        }
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        if (arrStorageBuilders != null) {
            for (GraphStorageBuilder builder : arrStorageBuilders) {
                builder.processEdge(way, edge);
            }
        }
    }

    public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
        if (arrStorageBuilders != null) {
            for (GraphStorageBuilder builder : arrStorageBuilders) {
                builder.processEdge(way, edge, coords);
            }
        }
    }

    public void finish() {
        if (arrStorageBuilders != null) {
            for (GraphStorageBuilder builder : arrStorageBuilders) {
                builder.finish();
            }
        }
    }

    public List<GraphStorageBuilder> getStorageBuilders() {
        return storageBuilders;
    }

    public double getMaximumSpeedLowerBound() {
        return maximumSpeedLowerBound;
    }

    public void setGetElevationFromPreprocessedData(boolean getElevationFromPreprocessedData) {
        this.getElevationFromPreprocessedData = getElevationFromPreprocessedData;
    }

    public boolean getElevationFromPreprocessedData() {
        return getElevationFromPreprocessedData;
    }

    public boolean isUseSidewalks() {
        return useSidewalks;
    }
}
