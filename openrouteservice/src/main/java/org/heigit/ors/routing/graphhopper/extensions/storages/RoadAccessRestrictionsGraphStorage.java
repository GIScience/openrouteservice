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

package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;
import org.heigit.ors.routing.RouteExtraInfo;
import org.heigit.ors.routing.RouteSegmentItem;
import org.heigit.ors.routing.RouteWarning;
import org.heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;

/**
 * The graph storage for road access restrictions.
 */
public class RoadAccessRestrictionsGraphStorage implements GraphExtension, WarningGraphExtension {
    private static final int NO_ENTRY = -1;
    private final int efRestrictions;

    protected DataAccess edges;
    protected int edgeEntryIndex = 0;
    protected int edgeEntryBytes;
    protected int edgesCount;
    private final byte[] byteData;

    public RoadAccessRestrictionsGraphStorage() {
        efRestrictions = nextBlockEntryIndex(1);
        edgeEntryBytes = edgeEntryIndex;
        edgesCount = 0;
        byteData = new byte[1];
    }

    /**
     * initializes the extended storage to be empty - required for testing purposes as the ext_storage aren't created
     * at the time tests are run
     */
    public void init() {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");
        Directory d = new RAMDirectory();
        this.edges = d.find("");
    }

    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The ext_road_access_restrictions storage must be initialized only once.");

        this.edges = dir.find("ext_road_access_restrictions");
    }

    public void setEdgeValue(int edgeId, int restriction) {
        edgesCount++;
        ensureEdgesIndex(edgeId);
        long edgePointer = (long) edgeId * edgeEntryBytes;
        byteData[0] = (byte) restriction;
        edges.setBytes(edgePointer + efRestrictions, byteData, 1);
    }

    public int getEdgeValue(int edgeId, byte[] buffer) {
        long edgeBase = (long) edgeId * edgeEntryBytes;
        edges.getBytes(edgeBase + efRestrictions, buffer, 1);
        return buffer[0] & 0xFF;
    }

    protected final int nextBlockEntryIndex(int size) {
        int res = edgeEntryIndex;
        edgeEntryIndex += size;
        return res;
    }

    public void setSegmentSize(int bytes) {
        edges.setSegmentSize(bytes);
    }

    public RoadAccessRestrictionsGraphStorage create(long initBytes) {
        edges.create(initBytes * edgeEntryBytes);
        return this;
    }

    public void flush() {
        edges.setHeader(0, edgeEntryBytes);
        edges.setHeader(4, edgesCount);
        edges.flush();
    }

    public void close() {
        edges.close();
    }

    @Override
    public long getCapacity() {
        return edges.getCapacity();
    }

    public int entries() {
        return edgesCount;
    }

    public boolean loadExisting() {
        if (!edges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_road_access_restrictions'. corrupt file or directory? ");

        edgeEntryBytes = edges.getHeader(0);
        edgesCount = edges.getHeader(4);
        return true;
    }

    void ensureEdgesIndex(int edgeIndex) {
        edges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
    }


    @Override
    public boolean isClosed() {
        return false;
    }

    private boolean isUsedForWarnings = false;

    @Override
    public void setIsUsedForWarning(boolean isWarning) {
        isUsedForWarnings = isWarning;
    }

    @Override
    public boolean isUsedForWarning() {
        return isUsedForWarnings;
    }

    @Override
    public String getName() {
        return "roadaccessrestrictions";
    }

    @Override
    public boolean generatesWarning(RouteExtraInfo extra) {
        for (RouteSegmentItem item : extra.getSegments()) {
            if (item.getValue() != NO_ENTRY && item.getValue() != AccessRestrictionType.NONE)
                return true;
        }
        return false;
    }

    @Override
    public RouteWarning getWarning() {
        return new RouteWarning(RouteWarning.ACCESS_RESTRICTION);
    }

}
