package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.config.EngineConfig;

import java.io.File;


public class FlatORSGraphFolderStrategy implements ORSGraphFolderStrategy {

    private final String graphsRootAbsPath;
    private final String routeProfileName;
    private final String repoName;
    private final String extend;
    private final String graphVersion;
    private final String profileGroup;

    public FlatORSGraphFolderStrategy(EngineConfig engineConfig, String routeProfileName) {
        this.graphsRootAbsPath = engineConfig.getGraphsRootPath();
        this.routeProfileName = routeProfileName;
        this.profileGroup = engineConfig.getGraphsProfileGroup();
        this.extend = engineConfig.getGraphsExtent();
        this.graphVersion = EngineConfig.GRAPH_VERSION;
        this.repoName = engineConfig.getGraphsRepoName();
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
