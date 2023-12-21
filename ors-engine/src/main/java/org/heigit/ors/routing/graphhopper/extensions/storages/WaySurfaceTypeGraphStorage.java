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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;
import org.heigit.ors.routing.util.WaySurfaceDescription;

public class WaySurfaceTypeGraphStorage implements GraphExtension {
    /* pointer for no entry */
    protected final int efWaytype;

    protected DataAccess orsEdges;
    protected int edgeEntryIndex = 0;
    protected int edgeEntryBytes;
    protected int edgesCount; // number of edges with custom values

    private final byte[] byteValues;

    public WaySurfaceTypeGraphStorage() {
        efWaytype = 0;

        edgeEntryBytes = edgeEntryIndex + 1;
        edgesCount = 0;
        byteValues = new byte[10];
    }

    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");

        this.orsEdges = dir.find("ext_waysurface");
    }

    protected final int nextBlockEntryIndex(int size) {
        edgeEntryIndex += size;
        return edgeEntryIndex;
    }

    public WaySurfaceTypeGraphStorage create(long initBytes) {
        orsEdges.create(initBytes * edgeEntryBytes);
        return this;
    }

    public void flush() {
        orsEdges.setHeader(0, edgeEntryBytes);
        orsEdges.setHeader(4, edgesCount);
        orsEdges.flush();
    }

    public void close() {
        orsEdges.close();
    }

    @Override
    public long getCapacity() {
        return orsEdges.getCapacity();
    }

    public int entries() {
        return edgesCount;
    }

    public boolean loadExisting() {
        if (!orsEdges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_waysurface'. corrupt file or directory? ");

        edgeEntryBytes = orsEdges.getHeader(0);
        edgesCount = orsEdges.getHeader(4);
        return true;
    }

    void ensureEdgesIndex(int edgeIndex) {
        orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
    }

    public void setEdgeValue(int edgeId, WaySurfaceDescription wayDesc) {
        edgesCount++;
        ensureEdgesIndex(edgeId);

        // add entry
        long edgePointer = (long) edgeId * edgeEntryBytes;
        byteValues[0] = (byte) ((wayDesc.getWayType() << 4) | wayDesc.getSurfaceType() & 0xff);
        orsEdges.setBytes(edgePointer + efWaytype, byteValues, 1);
    }


    public WaySurfaceDescription getEdgeValue(int edgeId, byte[] buffer) {
        long edgePointer = (long) edgeId * edgeEntryBytes;
        orsEdges.getBytes(edgePointer + efWaytype, buffer, 1);

        byte compValue = buffer[0];
        WaySurfaceDescription res = new WaySurfaceDescription();
        res.setWayType((compValue & 0b11110000) >> 4);
        res.setSurfaceType(compValue & 0b00001111);

        return res;
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
