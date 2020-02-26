package com.graphhopper.storage;

import com.graphhopper.routing.util.*;
import com.graphhopper.search.ConditionalIndex;

import java.util.*;

public class ConditionalEdges implements GraphExtension {
    Map<Integer, Integer> values = new HashMap<>();
    private final Map<String, ConditionalEdgesMap> conditionalEdgesMaps = new LinkedHashMap<>();

    ConditionalIndex conditionalIndex;
    EncodingManager encodingManager;

    private String encoderName;

    public ConditionalEdges(EncodingManager encodingManager, String encoderName) {
        this.encodingManager = encodingManager;
        this.encoderName = encoderName;
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
        if (this.conditionalIndex != null || !conditionalEdgesMaps.isEmpty())
            throw new AssertionError("The conditional restrictions storage must be initialized only once.");

        this.conditionalIndex = new ConditionalIndex(dir, encoderName);

        for (FlagEncoder encoder : encodingManager.fetchEdgeEncoders()) {
            String name = encodingManager.getKey(encoder, encoderName);
            if (encodingManager.hasEncodedValue(name)) {
                ConditionalEdgesMap conditionalEdgesMap = new ConditionalEdgesMap(encoderName + "_" + encoder.toString(), conditionalIndex);
                conditionalEdgesMap.init(graph, dir);
                conditionalEdgesMaps.put(encoder.toString(), conditionalEdgesMap);
            }
        }
    }

    public ConditionalEdgesMap getConditionalEdgesMap(String encoder) {
        return conditionalEdgesMaps.get(encoder);
    }

    @Override
    public GraphExtension create(long byteCount) {
        conditionalIndex.create(byteCount);
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            conditionalEdgesMap.create(byteCount);
        return this;
    }

    @Override
    public boolean loadExisting() {
        if (!conditionalIndex.loadExisting())
            throw new IllegalStateException("Cannot load 'conditionals'. corrupt file or directory? ");
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            if (!conditionalEdgesMap.loadExisting())
                throw new IllegalStateException("Cannot load 'conditional_edges_map'. corrupt file or directory? ");
        return true;
    }

    @Override
    public void setSegmentSize(int bytes) {
        conditionalIndex.setSegmentSize(bytes);
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            conditionalEdgesMap.setSegmentSize(bytes);
    }

    @Override
    public void flush() {
        conditionalIndex.flush();
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            conditionalEdgesMap.flush();
    }

    @Override
    public void close() {
        conditionalIndex.close();
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            conditionalEdgesMap.close();
    }

    @Override
    public long getCapacity() {
        long capacity = conditionalIndex.getCapacity();
        for (ConditionalEdgesMap conditionalEdgesMap: conditionalEdgesMaps.values())
            capacity += conditionalEdgesMap.getCapacity();
        return capacity;
    }

    @Override
    public GraphExtension copyTo(GraphExtension clonedStorage) {

        if (!(clonedStorage instanceof ConditionalEdges)) {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        ConditionalEdges clonedTC = (ConditionalEdges) clonedStorage;

        throw new IllegalStateException("NOT IMPLEMENTED");

        //conditionalIndex.copyTo(clonedTC.conditionalIndex);

        //return clonedStorage;
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


