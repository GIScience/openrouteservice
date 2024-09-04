package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;

public class NamedGraphsRepoStrategy implements ORSGraphRepoStrategy {

    private final GraphManagementRuntimeProperties managementProperties;

    public NamedGraphsRepoStrategy(GraphManagementRuntimeProperties managementProperties) {
        this.managementProperties = managementProperties;
    }

    private String getConcatenatedRepoFileName() {
        return String.join("_",
                managementProperties.getRepoProfileGroup(),
                managementProperties.getRepoCoverage(),
                managementProperties.getGraphVersion(),
                managementProperties.getEncoderName()
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
}
