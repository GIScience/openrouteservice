package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;

import java.io.File;


public class FlatORSGraphFolderStrategy implements ORSGraphFolderStrategy {

    private final GraphManagementRuntimeProperties managementProperties;

    public FlatORSGraphFolderStrategy(GraphManagementRuntimeProperties managementProperties) {
        this.managementProperties = managementProperties;
    }

    private String getConcatenatedLocalFileName() {
        return String.join("_",
                managementProperties.getRepoName(),
                managementProperties.getRepoProfileGroup(),
                managementProperties.getRepoCoverage(),
                managementProperties.getGraphVersion(),
                managementProperties.getEncoderName()
        );
    }

    private String getConcatenatedLocalFileName(String extension) {
        return getConcatenatedLocalFileName() + "." + extension;
    }

    @Override
    public String getProfileDescriptiveName() {
        return managementProperties.getLocalProfileName();
    }

    @Override
    public String getGraphInfoFileNameInRepository() {
        return managementProperties.getLocalProfileName() + "." + GRAPH_INFO_FILE_EXTENSION;
    }

    @Override
    public String getGraphsRootDirName() {
        return getGraphsRootDirectory().getName();
    }

    @Override
    public String getGraphsRootDirAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath();
    }

    @Override
    public String getProfileGraphsDirName() {
        return getGraphsRootDirName();
    }

    @Override
    public String getProfileGraphsDirAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath();
    }

    @Override
    public String getActiveGraphDirName() {
        return managementProperties.getLocalProfileName();
    }

    @Override
    public String getActiveGraphDirAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath() + File.separator + managementProperties.getLocalProfileName();
    }

    @Override
    public String getActiveGraphInfoFileName() {
        return "graph_info." + GRAPH_INFO_FILE_EXTENSION;
    }

    @Override
    public String getDownloadedGraphInfoFileName() {
        return getConcatenatedLocalFileName(GRAPH_INFO_FILE_EXTENSION);
    }

    @Override
    public String getDownloadedGraphInfoFileAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath() + File.separator + getDownloadedGraphInfoFileName();
    }

    @Override
    public String getDownloadedCompressedGraphFileName() {
        return getConcatenatedLocalFileName(GRAPH_DOWNLOAD_FILE_EXTENSION);
    }

    @Override
    public String getDownloadedCompressedGraphFileAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath() + File.separator + getDownloadedCompressedGraphFileName();
    }

    @Override
    public String getDownloadedExtractedGraphDirName() {
        return managementProperties.getLocalProfileName() + "_" + GRAPH_EXTRACTION_DIRECTORY_EXTENSION;
    }

    @Override
    public String getDownloadedExtractedGraphDirAbsPath() {
        return managementProperties.getLocalGraphsRootAbsPath() + File.separator + getDownloadedExtractedGraphDirName();
    }
}
