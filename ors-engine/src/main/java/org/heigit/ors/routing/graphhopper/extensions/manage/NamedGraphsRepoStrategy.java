package org.heigit.ors.routing.graphhopper.extensions.manage;

public class NamedGraphsRepoStrategy implements ORSGraphRepoStrategy {

    private final String routeProfileName;

    public NamedGraphsRepoStrategy(String routeProfileName) {
        this.routeProfileName = routeProfileName;
    }

    @Override
    public String getRepoCompressedGraphFileName() {
        return routeProfileName + "." + ORSGraphFolderStrategy.GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    @Override
    public String getRepoGraphInfoFileName() {
        return routeProfileName + "." + ORSGraphFolderStrategy.GRAPH_INFO_FILE_EXTENSION;
    }

    public String getAssetFilterPattern(String repository, String coverage, String graphVersion, String profileGroup, String profileName, String fileName){
        return "%s/%s/%s/%s".formatted(
                profileGroup,
                coverage,
                graphVersion,
                fileName);
    }


}
