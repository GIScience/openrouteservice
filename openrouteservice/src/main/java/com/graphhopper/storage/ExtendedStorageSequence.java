package com.graphhopper.storage;

import java.util.ArrayList;

public class ExtendedStorageSequence  implements GraphExtension {

    private GraphExtension[] extensions;
    private int numExtensions;
    private boolean isRequireNodeField = false;
    private boolean isRequireEdgeField = false;
    private int defaultNodeValue = -1;
    private int defaultEdgeValue = -1;

    public ExtendedStorageSequence(ArrayList<GraphExtension> seq) {
        numExtensions = seq.size();
        extensions = seq.toArray(new GraphExtension[numExtensions]);

        for (int i = 0; i < numExtensions; i++) {
            GraphExtension ge = extensions[i];
            if (ge.isRequireNodeField()) {
                isRequireNodeField = true;
                defaultNodeValue = ge.getDefaultNodeFieldValue();
            }
            if (ge.isRequireEdgeField()) {
                isRequireEdgeField = true;
                defaultEdgeValue = ge.getDefaultEdgeFieldValue();
            }
        }
    }

    public GraphExtension[] getExtensions() {
        return extensions;
    }

    @Override
    public boolean isRequireNodeField() {
        return isRequireNodeField;
    }

    @Override
    public boolean isRequireEdgeField() {
        return isRequireEdgeField;
    }

    @Override
    public int getDefaultNodeFieldValue() {
        return defaultNodeValue;
    }

    @Override
    public int getDefaultEdgeFieldValue() {
        return defaultEdgeValue;
    }

    @Override
    public void init(Graph graph, Directory dir) {
        for (int i = 0; i < numExtensions; i++) {
            extensions[i].init(graph, dir);
        }
    }

    @Override
    public GraphExtension create(long initSize) {
        for (int i = 0; i < numExtensions; i++) {
            extensions[i].create(initSize);
        }

        return extensions[0];
    }

    @Override
    public boolean loadExisting() {
        boolean result = true;
        for (int i = 0; i < numExtensions; i++) {
            if (!extensions[i].loadExisting()) {
                result = false;
                break;
            }
        }

        return result;
    }

    @Override
    public void setSegmentSize(int bytes) {
        for (int i = 0; i < numExtensions; i++) {
            extensions[i].setSegmentSize(bytes);
        }
    }

    @Override
    public void flush() {
        for (int i = 0; i < numExtensions; i++) {
            extensions[i].flush();
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < numExtensions; i++) {
            extensions[i].close();
        }
    }

    @Override
    public long getCapacity() {
        long capacity = 0;

        for (int i = 0; i < extensions.length; i++) {
            capacity += extensions[i].getCapacity();
        }

        return capacity;
    }

    @Override
    public GraphExtension copyTo(GraphExtension extStorage) {
        // noop
        return extStorage;
    }

    @Override
    public String toString() {
        return "ExtSequence";
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }
}

