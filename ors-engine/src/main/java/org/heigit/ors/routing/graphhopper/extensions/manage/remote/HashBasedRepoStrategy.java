package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;

public class HashBasedRepoStrategy implements ORSGraphRepoStrategy {

    private String hash;

    public HashBasedRepoStrategy(String hash) {
        this.hash = hash;
    }

    @Override
    public String getRepoCompressedGraphFileName() {
        return hash + "." + ORSGraphFolderStrategy.GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    @Override
    public String getRepoGraphInfoFileName() {
        return hash + "." + ORSGraphFolderStrategy.GRAPH_INFO_FILE_EXTENSION;
    }

}
