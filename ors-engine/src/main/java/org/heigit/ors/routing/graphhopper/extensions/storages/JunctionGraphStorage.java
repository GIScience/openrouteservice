package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class JunctionGraphStorage implements GraphExtension {
    /* pointer for no entry */
    protected final int efJunction;

    protected DataAccess junctionEdges;
    protected int junctionEdgeEntryIndex = 5;
    protected int junctionEdgeEntryBytes;
    protected int junctionEdgesCount;

    public JunctionGraphStorage() {
        efJunction = nextJunctionBlockEntryIndex(1);

        junctionEdgeEntryBytes = junctionEdgeEntryIndex;
        junctionEdgesCount = 0;
    }

    public void init(Graph graph, Directory dir) {
        if (junctionEdgesCount > 0)
            throw new AssertionError("The ext_junction storage must be initialized only once.");

        this.junctionEdges = dir.find("ext_junction");
    }

    protected final int nextJunctionBlockEntryIndex(int size) {
        int res = junctionEdgeEntryIndex;
        junctionEdgeEntryIndex += size;
        return res;
    }

    public void setJunctionSegmentSize(int bytes) {
        junctionEdges.setSegmentSize(bytes);
    }

    public JunctionGraphStorage create(long initBytes) {
        junctionEdges.create(initBytes * junctionEdgeEntryBytes);
        return this;
    }

    public void flush() {
        junctionEdges.setHeader(0, junctionEdgeEntryBytes);
        junctionEdges.setHeader(4, junctionEdgesCount);
        junctionEdges.flush();
    }

    public void close() {
        junctionEdges.close();
    }

    @Override
    public long getCapacity() {
        return junctionEdges.getCapacity();
    }

    public int entries() {
        return junctionEdgesCount;
    }

    public boolean loadExisting() {
        if (!junctionEdges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_junction'. Corrupt file or directory?");

        junctionEdgeEntryBytes = junctionEdges.getHeader(0);
        junctionEdgesCount = junctionEdges.getHeader(4);
        return true;
    }

    void ensureJunctionEdgesIndex(int edgeIndex) {
        junctionEdges.ensureCapacity(((long) edgeIndex + 1) * junctionEdgeEntryBytes);
    }

    public void setJunctionEdgeValue(int edgeId, int value) {
        junctionEdgesCount++;
        ensureJunctionEdgesIndex(edgeId);

        byte byteValue = (byte) value;

        junctionEdges.setByte((long) edgeId * junctionEdgeEntryBytes + efJunction, byteValue);
    }

    public int getJunctionEdgeValue(int edgeId) {
        byte byteValue = junctionEdges.getByte((long) edgeId * junctionEdgeEntryBytes + efJunction);
        return byteValue & 0xFF;
    }

    public boolean isRequireNodeField() {
        return true;
    }

    public boolean isRequireEdgeField() {
        // we require the additional field in the graph to point to the first
        // entry in the node table
        return true;
    }

    public int getDefaultNodeFieldValue() {
        return -1;
    }

    public int getDefaultEdgeFieldValue() {
        return -1;
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}

