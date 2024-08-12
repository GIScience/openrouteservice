package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;

public class NamedGraphsRepoStrategy implements ORSGraphRepoStrategy {

    private final String repoName;
    private final String profileGroup;
    private final String graphVersion;
    private final String extend;
    private final String routeProfileName;

    public NamedGraphsRepoStrategy(EngineProperties engineProperties, String routeProfileName, String graphVersion) {
        this.repoName = engineProperties.getGraphManagement().getRepositoryName();
        this.profileGroup = engineProperties.getGraphManagement().getRepositoryProfileGroup();
        this.extend = engineProperties.getGraphManagement().getGraphExtent();
        this.routeProfileName = routeProfileName;
        this.graphVersion = graphVersion;
    }

    private String getConcatenatedRepoFileName() {
        //todo add repoName?
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
