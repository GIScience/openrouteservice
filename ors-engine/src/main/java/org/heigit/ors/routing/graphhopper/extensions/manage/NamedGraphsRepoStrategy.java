package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.config.EngineConfig;

public class NamedGraphsRepoStrategy implements ORSGraphRepoStrategy {

    private final String repoName;
    private final String profileGroup;
    private final String graphVersion;
    private final String extend;
    private final String routeProfileName;

    public NamedGraphsRepoStrategy(EngineConfig engineConfig, String routeProfileName) {
        this.repoName = engineConfig.getGraphsRepoName();
        this.profileGroup = engineConfig.getGraphsProfileGroup();
        this.graphVersion = EngineConfig.GRAPH_VERSION;
        this.extend = engineConfig.getGraphsExtent();
        this.routeProfileName = routeProfileName;
    }

    private String getConcatenatedRepoFileName() {
        return String.join("_",
                profileGroup,
                extend,
                graphVersion,
                routeProfileName
        );
    }

    private String getConcatenatedRepoFileName(String extension) {
        return getConcatenatedRepoFileName() + "." + extension;
    }

    @Override
    public String getRepoCompressedGraphFileName() {
        return getConcatenatedRepoFileName(ORSGraphFolderStrategy.GRAPH_DOWNLOAD_FILE_EXTENSION);
    }

    @Override
    public String getRepoGraphInfoFileName() {
        return getConcatenatedRepoFileName(ORSGraphFolderStrategy.GRAPH_INFO_FILE_EXTENSION);
    }

    public String getAssetFilterPattern(String repository, String coverage, String graphVersion, String profileGroup, String profileName, String fileName){
        return "%s/%s/%s/%s".formatted(
                profileGroup,
                coverage,
                graphVersion,
                fileName);
    }


}
