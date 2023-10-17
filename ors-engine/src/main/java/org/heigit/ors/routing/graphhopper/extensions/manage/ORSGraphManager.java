package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.apache.log4j.Logger;

public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());
    private String graphsRepoBaseUrl;
    private String graphsRepoName;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private String hash;
    private String hashDirAbsPath;
    private String vehicleGraphDirAbsPath;
    private String routeProfileName;

    private ORSGraphFileManager fileManager;
    private ORSGraphRepoManager repoManager;

    public ORSGraphManager() {}

    public ORSGraphManager(String graphsRepoBaseUrl, String graphsRepoName, String graphsRepoCoverage, String graphsRepoGraphVersion,
                           String routeProfileName, String hash, String localPath, String vehicleGraphDirAbsPath) {
        this.graphsRepoBaseUrl = graphsRepoBaseUrl;
        this.graphsRepoName = graphsRepoName;
        this.graphsRepoCoverage = graphsRepoCoverage;
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
        this.hash = hash;
        this.hashDirAbsPath = localPath;
        this.routeProfileName = routeProfileName;
        this.vehicleGraphDirAbsPath = vehicleGraphDirAbsPath;
        initialize();
    }

    void initialize() {
        fileManager = new ORSGraphFileManager(hash, hashDirAbsPath, vehicleGraphDirAbsPath, routeProfileName);
        repoManager = new ORSGraphRepoManager(fileManager, routeProfileName, graphsRepoBaseUrl, graphsRepoName, graphsRepoCoverage, graphsRepoGraphVersion);
    }

    String getProfileWithHash() {return fileManager.getProfileWithHash();}

    public boolean isActive() {
        return fileManager.isActive();
    }

    boolean hasLocalGraph() {
        return fileManager.hasLocalGraph();
    }

    boolean hasGraphDownloadFile() {
        return fileManager.hasGraphDownloadFile();
    }

    public boolean hasDownloadedExtractedGraph() {
        return fileManager.hasDownloadedExtractedGraph();
    }

    public void setGraphsRepoBaseUrl(String graphsRepoBaseUrl) {
        this.graphsRepoBaseUrl = graphsRepoBaseUrl;
    }

    public void setGraphsRepoName(String graphsRepoName) {
        this.graphsRepoName = graphsRepoName;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setHashDirAbsPath(String hashDirAbsPath) {
        this.hashDirAbsPath = hashDirAbsPath;
    }

    public void setVehicleGraphDirAbsPath(String vehicleGraphDirAbsPath) {
        this.vehicleGraphDirAbsPath = vehicleGraphDirAbsPath;
    }

    public void setRouteProfileName(String routeProfileName) {
        this.routeProfileName = routeProfileName;
    }

    public String getGraphsRepoCoverage() {
        return graphsRepoCoverage;
    }

    public void setGraphsRepoCoverage(String graphsRepoCoverage) {
        this.graphsRepoCoverage = graphsRepoCoverage;
    }

    public String getRouteProfileName() {
        return routeProfileName;
    }

    public String getGraphsRepoGraphVersion() {
        return graphsRepoGraphVersion;
    }

    public void setGraphsRepoGraphVersion(String graphsRepoGraphVersion) {
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
    }

    public void manageStartup() {
        fileManager.cleanupIncompleteFiles();

        boolean hasLocalGraph = fileManager.hasLocalGraph();
        boolean hasDownloadedExtractedGraph = fileManager.hasDownloadedExtractedGraph();

        if (!hasLocalGraph && !hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] No local graph or extracted downloaded graph found - trying to download and extract graph from repository".formatted(getProfileWithHash()));
            downloadAndExtractLatestGraphIfNecessary();
            fileManager.activateNewGraph();
        }
        if (!hasLocalGraph && hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found extracted downloaded graph only".formatted(getProfileWithHash()));
            fileManager.activateNewGraph();
        }
        if (hasLocalGraph && hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found local graph and extracted downloaded graph".formatted(getProfileWithHash()));
            fileManager.backupExistingGraph();
            fileManager.activateNewGraph();
        }
        if (hasLocalGraph && !hasDownloadedExtractedGraph) {
            LOGGER.info("[%s] Found local graph only".formatted(getProfileWithHash()));
        }
    }

    public void downloadAndExtractLatestGraphIfNecessary() {
        if (fileManager.isActive()) {
            LOGGER.info("[%s] ORSGraphManager is active - skipping download".formatted(getProfileWithHash()));
            return;
        }
        repoManager.downloadGraphIfNecessary();
        fileManager.extractDownloadedGraph();
    }
}
