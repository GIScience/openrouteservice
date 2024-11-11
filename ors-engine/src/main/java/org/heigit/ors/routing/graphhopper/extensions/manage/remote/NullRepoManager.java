package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

public class NullRepoManager implements ORSGraphRepoManager {
    @Override
    public void downloadGraphIfNecessary() {
        // The NullRepoManager does nothing at this point.
    }
}
