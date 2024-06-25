package org.heigit.ors.routing.graphhopper.extensions.manage;

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

    @Override
    public String getAssetFilterPattern(String repository, String coverage, String graphVersion, String profileGroup, String profileName, String fileName) {
        return ".*%s/%s/%s/%s/%s/%s".formatted(
                coverage,
                graphVersion,
                profileName,
                hash,
                "[0-9]{12,}",
                fileName);
    }
}
