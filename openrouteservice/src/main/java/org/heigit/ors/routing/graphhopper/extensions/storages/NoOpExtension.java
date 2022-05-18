package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

import java.io.IOException;

// TODO: can we get rid of this class?
public class NoOpExtension implements GraphExtension {
    public NoOpExtension() {
        // do nothing
    }
    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public GraphExtension create(long l) {
        return this;
    }

    @Override
    public void init(Graph graph, Directory directory) {
        // do nothing
    }

    @Override
    public boolean loadExisting() {
        return true;
    }

    @Override
    public void flush() {
        // do nothing
    }
}
