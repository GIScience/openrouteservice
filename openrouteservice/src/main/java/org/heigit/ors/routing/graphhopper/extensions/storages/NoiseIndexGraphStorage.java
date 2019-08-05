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
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

/**
 * Created by ZWang on 13/06/2017.
 */
public class NoiseIndexGraphStorage implements GraphExtension {
    /* pointer for no entry */
    protected final int NO_ENTRY = -1;
    private final int EF_noiseIndex;

    private DataAccess orsEdges;
    private int edgeEntryBytes;
    private int edgesCount; // number of edges with custom values

    private byte[] byteValues;

    public NoiseIndexGraphStorage() {
        EF_noiseIndex = 0;

        int edgeEntryIndex = 0;
        edgeEntryBytes = edgeEntryIndex + 1;
        edgesCount = 0;
        byteValues = new byte[1];
    }

    public void setEdgeValue(int edgeId, byte noiseLevel) {
        edgesCount++;
        ensureEdgesIndex(edgeId);

        // add entry
        long edgePointer = (long) edgeId * edgeEntryBytes;
        byteValues[0] = noiseLevel;
        orsEdges.setBytes(edgePointer + EF_noiseIndex, byteValues, 1);
    }

    private void ensureEdgesIndex(int edgeId) {
        orsEdges.ensureCapacity(((long) edgeId + 1) * edgeEntryBytes);
    }

    public int getEdgeValue(int edgeId, byte[] buffer) {
    	
        long edgePointer = (long) edgeId * edgeEntryBytes;
        orsEdges.getBytes(edgePointer + EF_noiseIndex, buffer, 1);

        return buffer[0];
    }

    /**
     * @return true, if and only if, if an additional field at the graphs node storage is required
     */
    @Override
    public boolean isRequireNodeField() {
        return true;
    }

    /**
     * @return true, if and only if, if an additional field at the graphs edge storage is required
     */
    @Override
    public boolean isRequireEdgeField() {
        return true;
    }

    /**
     * @return the default field value which will be set for default when creating nodes
     */
    @Override
    public int getDefaultNodeFieldValue() {
        return -1;
    }

    /**
     * @return the default field value which will be set for default when creating edges
     */
    @Override
    public int getDefaultEdgeFieldValue() {
        return -1;
    }

    /**
     * initializes the extended storage by giving the base graph
     *
     * @param graph
     * @param dir
     */
    @Override
    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");

        this.orsEdges = dir.find("ext_noiselevel");
    }

    /**
     * sets the segment size in all additional data storages
     *
     * @param bytes
     */
    @Override
    public void setSegmentSize(int bytes) { orsEdges.setSegmentSize(bytes); }

    /**
     * creates a copy of this extended storage
     *
     * @param clonedStorage
     */
    @Override
    public GraphExtension copyTo(GraphExtension clonedStorage) {
        if (!(clonedStorage instanceof NoiseIndexGraphStorage)) {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        NoiseIndexGraphStorage clonedTC = (NoiseIndexGraphStorage) clonedStorage;

        orsEdges.copyTo(clonedTC.orsEdges);
        clonedTC.edgesCount = edgesCount;

        return clonedStorage;
    }

    /**
     * @return true if successfully loaded from persistent storage.
     */
    @Override
    public boolean loadExisting() {
        if (!orsEdges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_noiselevel'. corrupt file or directory?");

        edgeEntryBytes = orsEdges.getHeader(0);
        edgesCount = orsEdges.getHeader(4);
        return true;
    }

    /**
     * Creates the underlying storage. First operation if it cannot be loaded.
     *
     * @param initBytes
     */
    @Override
    public GraphExtension create(long initBytes) {
        orsEdges.create((long) initBytes * edgeEntryBytes);
        return this;
    }

    /**
     * This method makes sure that the underlying data is written to the storage. Keep in mind that
     * a disc normally has an IO cache so that flush() is (less) probably not save against power
     * loses.
     */
    @Override
    public void flush() {
        orsEdges.setHeader(0, edgeEntryBytes);
        orsEdges.setHeader(1 * 4, edgesCount);
        orsEdges.flush();
    }

    /**
     * This method makes sure that the underlying used resources are released. WARNING: it does NOT
     * flush on close!
     */
    @Override
    public void close() { orsEdges.close(); }

    @Override
    public boolean isClosed() {
        return false;
    }

    /**
     * @return the allocated storage size in bytes
     */
    @Override
    public long getCapacity() {
        return orsEdges.getCapacity();
    }
}
