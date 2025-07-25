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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.routing.graphhopper.extensions.SurfaceType;
import org.heigit.ors.routing.graphhopper.extensions.WayType;
import org.heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import org.heigit.ors.routing.util.WaySurfaceDescription;

import java.util.HashSet;

import static org.heigit.ors.routing.graphhopper.extensions.reader.osmfeatureprocessors.OSMAttachedSidewalkProcessor.*;

public class WaySurfaceTypeGraphStorageBuilder extends AbstractGraphStorageBuilder {
    public static final String TAG_HIGHWAY = "highway";
    public static final String TAG_SURFACE = "surface";
    public static final String TAG_ROUTE = "route";

    private WaySurfaceTypeGraphStorage storage;
    protected final HashSet<String> ferries;

    private final WaySurfaceDescription waySurfaceDescription = new WaySurfaceDescription();
    private SurfaceType sidewalkLeftSurface = SurfaceType.UNKNOWN;
    private SurfaceType sidewalkRightSurface = SurfaceType.UNKNOWN;

    @Getter
    @Setter
    private boolean useSidewalks = false;

    public WaySurfaceTypeGraphStorageBuilder() {
        ferries = new HashSet<>(5);
        ferries.add("shuttle_train");
        ferries.add("ferry");
    }

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        var flagEncoders = graphhopper.getEncodingManager().fetchEdgeEncoders();
        var flagEncoder = EncoderNameEnum.getFromEncoderName(flagEncoders.get(0).toString());
        useSidewalks = flagEncoders.size() == 1 && EncoderNameEnum.isPedestrian(flagEncoder);

        storage = new WaySurfaceTypeGraphStorage();
        return storage;
    }

    public void processWay(ReaderWay way) {
        waySurfaceDescription.reset();
        sidewalkLeftSurface = SurfaceType.UNKNOWN;
        sidewalkRightSurface = SurfaceType.UNKNOWN;

        int wayType;
        if (way.hasTag(TAG_ROUTE, ferries)) {
            wayType = WayType.FERRY;
        } else if (way.hasTag(TAG_HIGHWAY)) {
            wayType = WayType.getFromString(way.getTag(TAG_HIGHWAY));
        } else {
            return;
        }
        waySurfaceDescription.setWayType(wayType);

        SurfaceType waySurface = way.hasTag(TAG_SURFACE) ? SurfaceType.getFromString(way.getTag(TAG_SURFACE)) : SurfaceType.UNKNOWN;
        waySurfaceDescription.setSurfaceType(waySurface);

        if (useSidewalks) {
            // obtain default surface type for sidewalks
            SurfaceType sidewalkSurface = way.hasTag("sidewalk:surface") ?
                    SurfaceType.getFromString(way.getTag("sidewalk:surface")) : SurfaceType.UNKNOWN;

            sidewalkSurface = way.hasTag("sidewalk:both:surface") ?
                    SurfaceType.getFromString(way.getTag("sidewalk:both:surface")) : sidewalkSurface;

            sidewalkLeftSurface = way.hasTag("sidewalk:left:surface") ?
                    SurfaceType.getFromString(way.getTag("sidewalk:left:surface")) : sidewalkSurface;

            sidewalkRightSurface = way.hasTag("sidewalk:right:surface") ?
                    SurfaceType.getFromString(way.getTag("sidewalk:right:surface")) : sidewalkSurface;
        }
    }

    /**
     * Process an individual edge which has been derived from the way and then store it in the storage.
     *
     * @param way  The parent way feature
     * @param edge The specific edge to be processed
     */
    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        storage.setEdgeValue(edge.getEdge(), getStoredValue(way));
    }

    public WaySurfaceDescription getStoredValue(ReaderWay way) {
        var value = new WaySurfaceDescription();

        value.setWayType(waySurfaceDescription.getWayType());
        value.setSurfaceType(waySurfaceDescription.getSurfaceType());

        if (useSidewalks && way.hasTag(KEY_ORS_SIDEWALK_SIDE)) {
            value.setWayType(WayType.FOOTWAY);
            String side = way.getTag(KEY_ORS_SIDEWALK_SIDE);
            if (side.equals(VAL_LEFT)) {
                value.setSurfaceType(sidewalkLeftSurface);
            }
            else if (side.equals(VAL_RIGHT)) {
                value.setSurfaceType(sidewalkRightSurface);
            }
        }

        return value;
    }

    @Override
    public String getName() {
        return "WaySurfaceType";
    }
}
