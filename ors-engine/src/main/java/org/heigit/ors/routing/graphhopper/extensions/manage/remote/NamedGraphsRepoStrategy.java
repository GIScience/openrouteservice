package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;

public class NamedGraphsRepoStrategy implements ORSGraphRepoStrategy {

    private final String repoName;
    private final String profileGroup;
    private final String graphVersion;
    private final String extend;
    private final String routeProfileName;
    private final String encoderName;

    public NamedGraphsRepoStrategy(GraphManagementRuntimeProperties graphManagementRuntimeProperties) {
        this.repoName = graphManagementRuntimeProperties.getRepoName();
        this.profileGroup = graphManagementRuntimeProperties.getRepoProfileGroup();
        this.extend = graphManagementRuntimeProperties.getRepoCoverage();
        this.routeProfileName = graphManagementRuntimeProperties.getLocalProfileName();
        this.graphVersion = graphManagementRuntimeProperties.getGraphVersion();
        this.encoderName = graphManagementRuntimeProperties.getEncoderName();
    }

    private String getConcatenatedRepoFileName() {
        return String.join("_",
                profileGroup,
                extend,
                graphVersion,
                encoderName
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
