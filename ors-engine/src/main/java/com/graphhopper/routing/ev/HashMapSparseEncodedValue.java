package com.graphhopper.routing.ev;

import java.util.HashMap;
import java.util.Map;

public class HashMapSparseEncodedValue<E> implements SparseEncodedValue<E> {
    private final String name;
    private final Map<Integer, E> edgeValues = new HashMap<>();

    public HashMapSparseEncodedValue(String name) {
        this.name = name;
    }

    @Override
    public E get(int edgeID) {
        return edgeValues.get(edgeID);
    }

    @Override
    public void set(int edgeID, Object value) {
        if (value != null) {
            edgeValues.put(edgeID, (E) value);
        } else {
            edgeValues.remove(edgeID);
        }
    }

    @Override
    public int init(EncodedValue.InitializerConfig init) {
        // Nothing to initialize
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isStoreTwoDirections() {
        return false;
    }

    public String toString() {
        return name;
    }
}
