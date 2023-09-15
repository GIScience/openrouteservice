package org.heigit.ors.routing.graphhopper.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.api.AssetsApi;
import org.openapitools.client.model.AssetXO;
import org.openapitools.client.model.PageAssetXO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ORSGraphManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphManager.class.getName());
    private static final String GRAPH_DOWNLOAD_FILE_EXTENSION = "ghz";
    private String graphsRepoBaseUrl;
    private String graphsRepoName;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private int connectionTimeoutMillis = 2000;
    private int readTimeoutMillis = 200000;
    private String hash;
    private String hashDirAbsPath;
    private String vehicleGraphDirAbsPath;
    private String routeProfileName;

    public ORSGraphManager() {
    }

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

        //TODO: move the following code to a proper place
        Properties prop = new Properties();
        String graphVersion = "0";
        try (InputStream in = this.getClass().getResourceAsStream("/engine.properties")) {
            prop.load(in);
            String engineVersion = prop.getProperty("engineVersion", "0.0");
            graphVersion = engineVersion.split("\\.")[0];
        } catch (Exception e) {
            LOGGER.error("Initialization ERROR: cannot read graph version. " + e.getMessage());
        }
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

    public String getGraphsRepoGraphVersion() {
        return graphsRepoGraphVersion;
    }

    public void setGraphsRepoGraphVersion(String graphsRepoGraphVersion) {
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
    }

    public static class GraphInfo {
        public GraphInfo() {
        }

        private URL remoteUrl = null;
        private File localDirectory = null;
        private ORSGraphInfoV1 persistedGraphInfo;

        public URL getRemoteUrl() {
            return remoteUrl;
        }

        public void setRemoteUrl(URL remoteUrl) {
            this.remoteUrl = remoteUrl;
        }

        public File getLocalDirectory() {
            return localDirectory;
        }

        public void setLocalDirectory(File localDirectory) {
            this.localDirectory = localDirectory;
        }

        public ORSGraphInfoV1 getPersistedGraphInfo() {
            return persistedGraphInfo;
        }

        public void setPersistedGraphInfo(ORSGraphInfoV1 persistedGraphInfo) {
            this.persistedGraphInfo = persistedGraphInfo;
        }

        public boolean exists() {
            return !Objects.isNull(persistedGraphInfo);
        }

        public boolean isRemote() {
            return remoteUrl != null;
        }

        GraphInfo withRemoteUrl(URL url) {
            this.remoteUrl = url;
            return this;
        }

        GraphInfo withLocalDirectory(File directory) {
            this.localDirectory = directory;
            return this;
        }

        GraphInfo withPersistedInfo(ORSGraphInfoV1 persistedGraphInfo) {
            this.persistedGraphInfo = persistedGraphInfo;
            return this;
        }
    }

    public static class ORSGraphInfoV1 {

        private Date osmDate;

        public ORSGraphInfoV1() {
        }

        public ORSGraphInfoV1(Date osmDate) {
            this.osmDate = osmDate;
        }

        public Date getOsmDate() {
            return osmDate;
        }

        public void setOsmDate(Date osmDate) {
            this.osmDate = osmDate;
        }
    }

    public void downloadGraphIfNecessary() {
        if (Strings.isNullOrEmpty(graphsRepoBaseUrl) || Strings.isNullOrEmpty(graphsRepoName)) {
            return;
        }

        LOGGER.info("Checking for possible graph update for %s/%s from remote repository...".formatted(routeProfileName, hash));
        ORSGraphInfoV1 persistedRemoteGraphInfo = getPreviouslyDownloadedRemoteGraphInfo();
        String downloadFileName = createDynamicGraphDownloadFileName();
        File downloadFile = new File(vehicleGraphDirAbsPath, downloadFileName);
        GraphInfo localGraphInfo = getLocalGraphInfo();
        GraphInfo remoteGraphInfo = downloadLatestGraphInfoFromRepository();

        if (!shouldDownloadGraph(remoteGraphInfo, localGraphInfo, downloadFile, persistedRemoteGraphInfo)) {
            return;
        }

        File localDirectory = localGraphInfo.getLocalDirectory();
        if (localDirectory.exists()) {
            backupExistingGraph(localDirectory);
        }

        String downloadUrl = createGraphUrlFromGraphInfoUrl(remoteGraphInfo);
        downloadAsset(downloadUrl, downloadFile);
    }

    void backupExistingGraph(File hashDirectory) {
        String origAbsPath = hashDirectory.getAbsolutePath();
        String newAbsPath = hashDirectory.getAbsolutePath() + "_bak";
        if (hashDirectory.renameTo(new File(newAbsPath))) {
            LOGGER.info("renamed old local graph directory %s to %s".formatted(origAbsPath, newAbsPath));
        } else {
            LOGGER.error("could not backup local graph directory %s to %s".formatted(origAbsPath, newAbsPath));
        }
    }

    private String createGraphUrlFromGraphInfoUrl(GraphInfo remoteGraphInfo) {
        String url = remoteGraphInfo.getRemoteUrl().toString();
        String urlWithoutExtension = url.substring(0, url.lastIndexOf('.'));
        return createDynamicGraphDownloadFileName(urlWithoutExtension);
    }

    GraphInfo downloadLatestGraphInfoFromRepository() {
        GraphInfo latestGraphInfoInRepo = new GraphInfo();
        LOGGER.debug("Checking latest graph for %s in remote repository...".formatted(routeProfileName));

        String fileName = createDynamicGraphInfoFileName();
        AssetXO latestGraphInfoAsset = findLatestGraphInfoAsset(fileName);
        if (latestGraphInfoAsset == null) {
            LOGGER.debug("No graph for %s found in remote repository".formatted(routeProfileName));
            return latestGraphInfoInRepo;
        }

        File downloadedFile = new File(vehicleGraphDirAbsPath, fileName);
        downloadAsset(latestGraphInfoAsset.getDownloadUrl(), downloadedFile);

        try {
            URL url = new URL(latestGraphInfoAsset.getDownloadUrl());
            latestGraphInfoInRepo.setRemoteUrl(url);

            ORSGraphInfoV1 orsGraphInfoV1 = readOrsGraphInfoV1(downloadedFile);
            latestGraphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        } catch (MalformedURLException e) {
            LOGGER.error("invalid URL in asset");
        }

        return latestGraphInfoInRepo;
    }

    private String createDynamicGraphInfoFileName() {
        return hash + ".json";
    }

    private String createDynamicGraphDownloadFileName() {
        return createDynamicGraphDownloadFileName(hash);
    }

    private String createDynamicGraphDownloadFileName(String basename) {
        return basename + "." + GRAPH_DOWNLOAD_FILE_EXTENSION;
    }

    GraphInfo getLocalGraphInfo() {
        LOGGER.debug("Checking local graph info for %s...".formatted(routeProfileName));
        File localDir = new File(hashDirAbsPath);

        if (!localDir.exists()) {
            LOGGER.debug("No local graph directory for %s found.".formatted(routeProfileName));
            return new GraphInfo().withLocalDirectory(localDir);
        }

        if (!localDir.isDirectory()) {
            throw new IllegalArgumentException("GraphHopperLocation cannot be an existing file. Has to be either non-existing or a folder.");
        }

        File graphInfoFile = new File(localDir, createDynamicGraphInfoFileName());
        if (!graphInfoFile.exists() || !graphInfoFile.isFile()) {
            LOGGER.debug("No %s found in %s".formatted(graphInfoFile.getName(), hashDirAbsPath));
            return new GraphInfo().withLocalDirectory(localDir);
        }

        ORSGraphInfoV1 graphInfoV1 = readOrsGraphInfoV1(graphInfoFile);
        LOGGER.debug("Found local graph info for %s with osmDate=%s".formatted(routeProfileName, graphInfoV1.osmDate));
        return new GraphInfo().withLocalDirectory(localDir).withPersistedInfo(graphInfoV1);
    }

    ORSGraphInfoV1 getPreviouslyDownloadedRemoteGraphInfo() {
        LOGGER.debug("Checking graph info for %s of previous check ...".formatted(routeProfileName));
        String fileName = createDynamicGraphInfoFileName();
        File persistedGraphInfoFile = new File(vehicleGraphDirAbsPath, fileName);
        if (persistedGraphInfoFile.exists()) {
            return readOrsGraphInfoV1(persistedGraphInfoFile);
        }
        return null;
    }

    private void writeOrsGraphInfoV1(ORSGraphInfoV1 orsGraphInfoV1, File outputFile) {
        try {
            new ObjectMapper().writeValue(outputFile, orsGraphInfoV1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ORSGraphInfoV1 readOrsGraphInfoV1(File inputFile) {
        try {
            return new ObjectMapper().readValue(inputFile, ORSGraphInfoV1.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldDownloadGraph(GraphInfo remoteGraphInfo, GraphInfo localGraphInfo, File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo) {
        if (!remoteGraphInfo.exists()) {
            LOGGER.info("There is no graph for %s/%s in remote repository - nothing to download.".formatted(routeProfileName, hash));
            return false;
        }
        if (persistedDownloadFile.exists() && persistedRemoteGraphInfo != null) {
            if (remoteGraphInfo.getPersistedGraphInfo().getOsmDate().after(persistedRemoteGraphInfo.getOsmDate())) {
                LOGGER.info("Found local file %s from previous download but downloading newer version from repository.".formatted(persistedDownloadFile.getAbsolutePath()));
                return true;
            } else {
                LOGGER.info("Found local file %s from previous download, there is no newer version in the repository.".formatted(persistedDownloadFile.getAbsolutePath()));
                return false;
            }
        }
        if (!localGraphInfo.exists()) {
            LOGGER.info("There is no local graph for %s/%s - should be downloaded.".formatted(routeProfileName, hash));
            return true;
        }
        if (!remoteGraphInfo.getPersistedGraphInfo().getOsmDate().after(localGraphInfo.getPersistedGraphInfo().getOsmDate())) {
            LOGGER.info("OSM date of graph for %s/%s in remote repository is not newer than local graph - keeping local graph".formatted(routeProfileName, hash));
            return false;
        }
        LOGGER.info("OSM date of graph for %s/%s in remote repository is newer than local graph - should be downloaded".formatted(routeProfileName, hash));
        return true;
    }

    String createDownloadPathFilterPattern(){
        return ".*/%s/%s/%s/%s/[0-9]{12}/.*".formatted(graphsRepoCoverage, graphsRepoGraphVersion, routeProfileName, hash);
    }

    AssetXO findLatestGraphInfoAsset(String fileName) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(graphsRepoBaseUrl);

        AssetsApi assetsApi = new AssetsApi(defaultClient);

        try {
            List<AssetXO> items = new ArrayList<>();
            String continuationToken = null;
            do {
                LOGGER.debug("trying to call nexus api");
                PageAssetXO assets = assetsApi.getAssets(graphsRepoName, continuationToken);
                LOGGER.debug("received assets: %s".formatted(assets.toString()));
                if (assets.getItems() != null) {
                    items.addAll(assets.getItems());
                }
                continuationToken = assets.getContinuationToken();
            } while (!isBlank(continuationToken));
            LOGGER.debug("found %d items".formatted(items.size()));

            return filterLatestAsset(fileName, items);

        } catch (ApiException e) {
            LOGGER.error("Exception when calling AssetsApi#getAssets");
            LOGGER.error("Status code: " + e.getCode());
            LOGGER.error("Reason: " + e.getResponseBody());
            LOGGER.error("Response headers: " + e.getResponseHeaders());
        }
        return null;
    }

    AssetXO filterLatestAsset(String fileName, List<AssetXO> items) {
        String downloadPathFilterPattern = createDownloadPathFilterPattern();
        LOGGER.debug("filtering assets for %s".formatted(items.size(), downloadPathFilterPattern));

        // paths like https://repo.heigit.org/ors-graphs-traffic/planet/3/car/5a5af307fbb8019bfb69d4916f55ddeb/202212312359/5a5af307fbb8019bfb69d4916f55ddeb.json
        Optional<AssetXO> first = items.stream()
                .filter(assetXO -> assetXO.getPath().matches(downloadPathFilterPattern))
                .filter(assetXO -> assetXO.getPath().endsWith(fileName))
                .sorted((a1, a2) -> a2.getPath().compareTo(a1.getPath()))//sort reverse: latest date (path parameter) first
                .findFirst();

        return first.orElse(null);
    }

    void downloadAsset(String downloadUrl, File outputFile) {
        if (StringUtils.isNotBlank(downloadUrl)) {
            try {
                LOGGER.info("Downloading %s to file %s".formatted(downloadUrl, outputFile.getAbsolutePath()));
                FileUtils.copyURLToFile(
                        new URL(downloadUrl),
                        outputFile,
                        connectionTimeoutMillis,
                        readTimeoutMillis);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
