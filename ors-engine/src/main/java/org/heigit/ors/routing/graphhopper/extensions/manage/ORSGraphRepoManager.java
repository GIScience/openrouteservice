package org.heigit.ors.routing.graphhopper.extensions.manage;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ORSGraphRepoManager {

    private static final Logger LOGGER = Logger.getLogger(ORSGraphRepoManager.class.getName());
    private int connectionTimeoutMillis = 2000;
    private int readTimeoutMillis = 200000;
    private String graphsRepoBaseUrl;
    private String graphsRepoName;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private String routeProfileName;
    private ORSGraphFileManager fileManager;


    public ORSGraphRepoManager() {}

    public ORSGraphRepoManager(ORSGraphFileManager fileManager, String routeProfileName, String graphsRepoBaseUrl, String graphsRepoName, String graphsRepoCoverage, String graphsRepoGraphVersion) {
        this.fileManager = fileManager;
        this.routeProfileName = routeProfileName;
        this.graphsRepoBaseUrl = graphsRepoBaseUrl;
        this.graphsRepoName = graphsRepoName;
        this.graphsRepoCoverage = graphsRepoCoverage;
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
    }

    public void setGraphsRepoBaseUrl(String graphsRepoBaseUrl) {
        this.graphsRepoBaseUrl = graphsRepoBaseUrl;
    }

    public void setGraphsRepoName(String graphsRepoName) {
        this.graphsRepoName = graphsRepoName;
    }

    public void setGraphsRepoCoverage(String graphsRepoCoverage) {
        this.graphsRepoCoverage = graphsRepoCoverage;
    }

    public void setGraphsRepoGraphVersion(String graphsRepoGraphVersion) {
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
    }

    public void setRouteProfileName(String routeProfileName) {
        this.routeProfileName = routeProfileName;
    }

    public void setFileManager(ORSGraphFileManager fileManager) {
        this.fileManager = fileManager;
    }

    String createDownloadPathFilterPattern() {
        return ".*%s/%s/%s/%s/[0-9]{12,}/.*".formatted(graphsRepoCoverage, graphsRepoGraphVersion, routeProfileName, fileManager.getHash());
    }


    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(graphsRepoBaseUrl) || isNullOrEmpty(graphsRepoName) || isNullOrEmpty(graphsRepoCoverage) || isNullOrEmpty(graphsRepoGraphVersion)) {
            LOGGER.debug("ORSGraphManager for %s is not configured - skipping check".formatted(routeProfileName));
            return;
        }
        if (fileManager.isActive()) {
            LOGGER.info("ORSGraphManager for %s is active - skipping download".formatted(routeProfileName));
            return;
        }

        LOGGER.info("Checking for possible graph update for %s/%s from remote repository...".formatted(routeProfileName, fileManager.getHash()));
        try {
            ORSGraphInfoV1 persistedRemoteGraphInfo = fileManager.getPreviouslyDownloadedRemoteGraphInfo();
            File graphDownloadFile = fileManager.getGraphDownloadFile();
            GraphInfo localGraphInfo = fileManager.getLocalGraphInfo();
            GraphInfo remoteGraphInfo = downloadLatestGraphInfoFromRepository();

            if (!shouldDownloadGraph(remoteGraphInfo, localGraphInfo, graphDownloadFile, persistedRemoteGraphInfo)) {
                return;
            }

            File localDirectory = localGraphInfo.getLocalDirectory();
            if (localDirectory.exists()) {
                fileManager.backupExistingGraph(localDirectory);
            }

            String downloadUrl = fileManager.createGraphUrlFromGraphInfoUrl(remoteGraphInfo);
            LOGGER.info("Downloading %s to file %s".formatted(downloadUrl, graphDownloadFile.getAbsolutePath()));
            downloadAsset(downloadUrl, graphDownloadFile);
        } catch (Exception e) {
            LOGGER.error("Caught an exception during graph download check or graph download:", e);
        }
    }

    public boolean shouldDownloadGraph(GraphInfo remoteGraphInfo, GraphInfo localGraphInfo, File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo) {
        if (!remoteGraphInfo.exists()) {
            LOGGER.info("There is no graph for %s/%s in remote repository - nothing to download.".formatted(routeProfileName, fileManager.getHash()));
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
            LOGGER.info("There is no local graph for %s/%s - should be downloaded.".formatted(routeProfileName, fileManager.getHash()));
            return true;
        }
        if (!remoteGraphInfo.getPersistedGraphInfo().getOsmDate().after(localGraphInfo.getPersistedGraphInfo().getOsmDate())) {
            LOGGER.info("Graph for %s/%s in remote repository is not newer than local graph - keeping local graph".formatted(routeProfileName, fileManager.getHash()));
            return false;
        }
        LOGGER.info("Graph for %s/%s in remote repository is newer than local graph - should be downloaded".formatted(routeProfileName, fileManager.getHash()));
        return true;
    }


    AssetXO findLatestGraphInfoAsset(String fileName) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(graphsRepoBaseUrl);

        AssetsApi assetsApi = new AssetsApi(defaultClient);

        try {
            List<AssetXO> items = new ArrayList<>();
            String continuationToken = null;
            do {
                LOGGER.debug("trying to call nexus api with graphsRepoBaseUrl=%s graphsRepoName=%s graphsRepoCoverage=%s, graphsRepoGraphVersion=%s".formatted(
                        graphsRepoBaseUrl, graphsRepoName, graphsRepoCoverage, graphsRepoGraphVersion));
                PageAssetXO assets = assetsApi.getAssets(graphsRepoName, continuationToken);
                LOGGER.trace("received assets: %s".formatted(assets.toString()));
                if (assets.getItems() != null) {
                    items.addAll(assets.getItems());
                }
                continuationToken = assets.getContinuationToken();
            } while (!isBlank(continuationToken));
            LOGGER.debug("found %d items total".formatted(items.size()));

            return filterLatestAsset(fileName, items);

        } catch (ApiException e) {
            LOGGER.error("Exception when calling AssetsApi#getAssets");
            LOGGER.error("    - Status code           : " + e.getCode());
            LOGGER.error("    - Reason                : " + e.getResponseBody());
            LOGGER.error("    - Response headers      : " + e.getResponseHeaders());
            LOGGER.error("    - graphsRepoBaseUrl     : " + graphsRepoBaseUrl);
            LOGGER.error("    - graphsRepoName        : " + graphsRepoName);
            LOGGER.error("    - graphsRepoCoverage    : " + graphsRepoCoverage);
            LOGGER.error("    - graphsRepoGraphVersion: " + graphsRepoGraphVersion);
        }
        return null;
    }

    GraphInfo downloadLatestGraphInfoFromRepository() {
        GraphInfo latestGraphInfoInRepo = new GraphInfo();
        LOGGER.debug("Checking latest graphInfo for %s in remote repository...".formatted(routeProfileName));

        String fileName = fileManager.createDynamicGraphInfoFileName();
        AssetXO latestGraphInfoAsset = findLatestGraphInfoAsset(fileName);
        if (latestGraphInfoAsset == null) {
            LOGGER.warn("No graphInfo found in remote repository for %s".formatted(routeProfileName));
            return latestGraphInfoInRepo;
        }

        File downloadedFile = new File(fileManager.getVehicleGraphDirAbsPath(), fileName);
        downloadAsset(latestGraphInfoAsset.getDownloadUrl(), downloadedFile);

        try {
            URL url = new URL(latestGraphInfoAsset.getDownloadUrl());
            latestGraphInfoInRepo.setRemoteUrl(url);

            ORSGraphInfoV1 orsGraphInfoV1 = fileManager.readOrsGraphInfoV1(downloadedFile);
            latestGraphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid download URL for graphInfo asset %s".formatted(routeProfileName));
        }

        return latestGraphInfoInRepo;
    }

    AssetXO filterLatestAsset(String fileName, List<AssetXO> items) {
        String downloadPathFilterPattern = createDownloadPathFilterPattern();
        LOGGER.debug("filtering %d assets for pattern '%s' and fileName=%s".formatted(items.size(), downloadPathFilterPattern, fileName));

        // paths like https://repo.heigit.org/ors-graphs-traffic/planet/3/car/5a5af307fbb8019bfb69d4916f55ddeb/202212312359/5a5af307fbb8019bfb69d4916f55ddeb.json
        Optional<AssetXO> first = items.stream()
                .filter(assetXO -> assetXO.getPath().matches(downloadPathFilterPattern))
                .filter(assetXO -> assetXO.getPath().endsWith(fileName))
                .sorted((a1, a2) -> a2.getPath().compareTo(a1.getPath()))//sort reverse: latest date (path parameter) first
                .findFirst();

        return first.orElse(null);
    }

    void downloadAsset(String downloadUrl, File outputFile) {
        File tempDownloadFile = fileManager.asIncompleteFile(outputFile);
        if (StringUtils.isNotBlank(downloadUrl)) {
            try {
                FileUtils.copyURLToFile(
                        new URL(downloadUrl),
                        tempDownloadFile,
                        connectionTimeoutMillis,
                        readTimeoutMillis);
                tempDownloadFile.renameTo(outputFile);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            } finally {
                tempDownloadFile.delete();
            }
        }
    }
}
