package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;

import java.io.File;


public class HashSubDirBasedORSGraphFolderStrategy implements ORSGraphFolderStrategy {

    private final String hash;
    private final String graphsRootAbsPath;
    private final String routeProfileName;

    public HashSubDirBasedORSGraphFolderStrategy(EngineProperties engineProperties, String routeProfileName, String hash) {//todo GRC consider changing typ graphsRootPath to Path and get rid of
        this(engineProperties.getGraphsRootPath().toAbsolutePath().toString(), routeProfileName, hash);
    }

    public HashSubDirBasedORSGraphFolderStrategy(GraphManagementRuntimeProperties graphManagementRuntimeProperties, String hash) {
        this.graphsRootAbsPath = graphManagementRuntimeProperties.getLocalGraphsRootAbsPath();
        this.routeProfileName = graphManagementRuntimeProperties.getLocalProfileName();
        this.hash = hash;
    }

    public HashSubDirBasedORSGraphFolderStrategy(String graphsRootAbsPath, String routeProfileName, String hash) {
        this.graphsRootAbsPath = graphsRootAbsPath;
        this.routeProfileName = routeProfileName;
        this.hash = hash;
    }

    @Override
    public String getProfileDescriptiveName() {
        return routeProfileName + File.separator + hash;
    }

    @Override
    public String getGraphInfoFileNameInRepository() {
        return hash + "." + GRAPH_INFO_FILE_EXTENSION;
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
        return routeProfileName;
    }

    @Override
    public String getProfileGraphsDirAbsPath() {
        return getGraphsRootDirAbsPath() + File.separator + routeProfileName;
    }

    @Override
    public String getActiveGraphDirName() {
        return hash;
    }

    @Override
    public String getActiveGraphDirAbsPath() {
        return getProfileGraphsDirAbsPath() + File.separator + getActiveGraphDirName();
    }

    @Override
    public String getActiveGraphInfoFileName() {
        return getGraphInfoFileNameInRepository();
    }

    @Override
    public String getDownloadedGraphInfoFileName() {
        return getGraphInfoFileNameInRepository();
    }

    @Override
    public String getDownloadedGraphInfoFileAbsPath() {
        return getProfileGraphsDirAbsPath() + File.separator + getDownloadedGraphInfoFileName();
    }

    @Override
    public String getDownloadedCompressedGraphFileName() {
        return hash + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    @Override
    public String getDownloadedCompressedGraphFileAbsPath() {
        return getProfileGraphsDirAbsPath() + File.separator + getDownloadedCompressedGraphFileName();
    }

    @Override
    public String getDownloadedExtractedGraphDirName() {
        return hash + "_" + GRAPH_EXTRACTION_DIRECTORY_EXTENSION;
    }

    @Override
    public String getDownloadedExtractedGraphDirAbsPath() {
        return getProfileGraphsDirAbsPath() + File.separator + getDownloadedExtractedGraphDirName();
    }

    @Override
    public String getDownloadedExtractedGraphInfoFileName() {
        return getGraphInfoFileNameInRepository();
    }
}
