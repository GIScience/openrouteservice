package com.graphhopper.storage;

import com.graphhopper.search.ConditionalIndex;

import java.util.HashMap;
import java.util.Map;

public class ConditionalEdges implements GraphExtension {
    private static final int EF_EDGE_BYTES = 4;
    private static final int EF_CONDITION_BYTES = 4;
    protected final int EF_EDGE, EF_CONDITION;

    protected DataAccess edges;
    protected int edgeEntryIndex = 0;
    protected int edgeEntryBytes;
    protected int edgesCount;

    private static final long START_POINTER = 0;
    private long bytePointer = START_POINTER;

    Map<Integer, Integer> values = new HashMap<>();

    ConditionalIndex conditionalIndex;

    public ConditionalEdges() {
        EF_EDGE = nextBlockEntryIndex(EF_EDGE_BYTES);
        EF_CONDITION = nextBlockEntryIndex(EF_CONDITION_BYTES);

        edgesCount = 0;
    }

    protected final int nextBlockEntryIndex(int size) {
        int res = edgeEntryIndex;
        edgeEntryIndex += size;
        return res;
    }

    public int entries() {
        return edgesCount;
    }

    /**
     * Set the pointer to the conditional index.
     * @param edge    The internal id of the edge in the graph
     * @param value  The index pointing to the conditionals
     */
    public void setEdgeValue(int edge, String value) {
        int conditionalRef = (int) conditionalIndex.put(value);
        if (conditionalRef < 0)
            throw new IllegalStateException("Too many conditionals are stored, currently limited to int pointer");

        edgesCount++;

        edges.ensureCapacity(bytePointer + EF_EDGE_BYTES + EF_CONDITION_BYTES);

        edges.setInt(bytePointer, edge);
        bytePointer += EF_EDGE_BYTES;
        edges.setInt(bytePointer, conditionalRef);
        bytePointer += EF_CONDITION_BYTES;

        values.put(edge, conditionalRef);
    }

    /**
     * Get the pointer to the conditional index.
     * @param edgeId    The internal graph id of the edger
     * @return The index pointing to the conditionals
     */
    public String getEdgeValue(int edgeId) {
        Integer index = values.get(edgeId);

        return (index == null) ? "" : conditionalIndex.get((long) index);
    }

    @Override
    public boolean isRequireNodeField() {
        return false;
    }

    @Override
    public boolean isRequireEdgeField() {
        return false;
    }

    @Override
    public int getDefaultNodeFieldValue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getDefaultEdgeFieldValue() {
        return 0;
    }

    @Override
    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The conditional restrictions storage must be initialized only once.");

        this.edges = dir.find("conditional_edges");
        this.conditionalIndex = new ConditionalIndex(dir);
    }

    @Override
    public GraphExtension create(long byteCount) {
        edges.create(byteCount * edgeEntryBytes);
        conditionalIndex.create(byteCount);
        return this;
    }

    @Override
    public boolean loadExisting() {
        if (!edges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'conditional_edges'. corrupt file or directory? " );

        edgeEntryBytes = edges.getHeader(0);
        edgesCount = edges.getHeader(4);

        for (bytePointer = START_POINTER; bytePointer < edgesCount * (EF_EDGE_BYTES + EF_CONDITION_BYTES);) {
            int edge = edges.getInt(bytePointer);
            bytePointer += EF_EDGE_BYTES;
            int condition = edges.getInt(bytePointer);
            bytePointer += EF_CONDITION_BYTES;

            values.put(edge, condition);
        }


        if (!conditionalIndex.loadExisting())
            throw new IllegalStateException("Cannot load 'conditional_index'. corrupt file or directory? ");

        return true;
    }

    @Override
    public void setSegmentSize(int bytes) {
        edges.setSegmentSize(bytes);
        conditionalIndex.setSegmentSize(bytes);
    }

    @Override
    public void flush() {
        edges.setHeader(0, edgeEntryBytes);
        edges.setHeader(1 * 4, edgesCount);
        edges.flush();
        conditionalIndex.flush();
    }

    @Override
    public void close() {
        edges.close();
        conditionalIndex.close();
    }

    @Override
    public long getCapacity() {
        return edges.getCapacity() + conditionalIndex.getCapacity();
    }

    @Override
    public GraphExtension copyTo(GraphExtension clonedStorage) {

        if (!(clonedStorage instanceof ConditionalEdges)) {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        ConditionalEdges clonedTC = (ConditionalEdges) clonedStorage;

        edges.copyTo(clonedTC.edges);
        clonedTC.edgesCount = edgesCount;

        conditionalIndex.copyTo(clonedTC.conditionalIndex);

        return clonedStorage;
    }

    @Override
    public String toString() {
        return "conditional_edges";
    }

    @Override
    public boolean isClosed() {
        return false;
    }

};


