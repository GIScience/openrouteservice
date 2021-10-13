package org.heigit.ors.routing.graphhopper.extensions.storages;

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
}
