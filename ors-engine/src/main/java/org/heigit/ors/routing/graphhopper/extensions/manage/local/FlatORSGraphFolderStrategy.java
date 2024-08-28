package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;

import java.io.File;


public class FlatORSGraphFolderStrategy implements ORSGraphFolderStrategy {

    private final String graphsRootAbsPath;
    private final String routeProfileName;
    private final String repoName;
    private final String extend;
    private final String graphVersion;
    private final String profileGroup;

    public FlatORSGraphFolderStrategy(GraphManagementRuntimeProperties managementProperties) {
        this.graphsRootAbsPath = managementProperties.getLocalGraphsRootAbsPath();
        this.profileGroup = managementProperties.getRepoProfileGroup();
        this.extend = managementProperties.getRepoCoverage();
        this.graphVersion = managementProperties.getGraphVersion();
        this.repoName = managementProperties.getRepoName();
        this.routeProfileName = managementProperties.getLocalProfileName();
    }

    public FlatORSGraphFolderStrategy(EngineProperties engineProperties, String routeProfileName, String graphVersion) {//todo GRC consider changing typ graphsRootPath to Path and get rid of
        this(engineProperties.getGraphManagement(), routeProfileName, graphVersion, engineProperties.getGraphsRootPath().toAbsolutePath().toString());
    }

    public FlatORSGraphFolderStrategy(GraphManagementProperties graphManagementProperties, String routeProfileName, String graphVersion, String graphsRootAbsPath) {
        this.graphsRootAbsPath = graphsRootAbsPath;
        this.profileGroup = graphManagementProperties.getRepositoryProfileGroup();
        this.extend = graphManagementProperties.getGraphExtent();
        this.graphVersion = graphVersion;
        this.repoName = graphManagementProperties.getRepositoryName();
        this.routeProfileName = routeProfileName;
    }

    private String getConcatenatedLocalFileName() {
        return String.join("_",
                repoName,
                profileGroup,
                extend,
                graphVersion,
                routeProfileName
        );
    }

    private String getConcatenatedLocalFileName(String extension) {
        return getConcatenatedLocalFileName() + "." + extension;
    }

    @Override
    public String getProfileDescriptiveName() {
        return routeProfileName;
    }

    @Override
    public String getGraphInfoFileNameInRepository() {
        return routeProfileName + "." + GRAPH_INFO_FILE_EXTENSION;
    }

    @Override
    public String getGraphsRootDirName() {
        return getGraphsRootDirectory().getName();
    }

    @Override
    public String getGraphsRootDirAbsPath() {
        return graphsRootAbsPath;
    }

    @Override
    public String getProfileGraphsDirName() {
        return getGraphsRootDirName();
    }

    @Override
    public String getProfileGraphsDirAbsPath() {
        return graphsRootAbsPath;
    }

    @Override
    public String getActiveGraphDirName() {
        return routeProfileName;
    }

    @Override
    public String getActiveGraphDirAbsPath() {
        return graphsRootAbsPath + File.separator + routeProfileName;
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
        return graphsRootAbsPath + File.separator + getDownloadedGraphInfoFileName();
    }

    @Override
    public String getDownloadedCompressedGraphFileName() {
        return getConcatenatedLocalFileName(GRAPH_DOWNLOAD_FILE_EXTENSION);
    }

    @Override
    public String getDownloadedCompressedGraphFileAbsPath() {
        return graphsRootAbsPath + File.separator + getDownloadedCompressedGraphFileName();
    }

    @Override
    public String getDownloadedExtractedGraphDirName() {
        return routeProfileName + "_" + GRAPH_EXTRACTION_DIRECTORY_EXTENSION;
    }

    @Override
    public String getDownloadedExtractedGraphDirAbsPath() {
        return graphsRootAbsPath + File.separator + getDownloadedExtractedGraphDirName();
    }

    @Override
    public String getDownloadedExtractedGraphInfoFileName() {
        return getGraphInfoFileNameInRepository();
    }
}
