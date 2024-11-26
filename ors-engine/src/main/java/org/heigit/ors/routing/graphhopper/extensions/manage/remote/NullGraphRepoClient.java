package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

public class NullGraphRepoClient implements ORSGraphRepoClient {
    @Override
    public void downloadGraphIfNecessary() {
        // The NullGraphRepoClient does nothing at this point.
    }
}
