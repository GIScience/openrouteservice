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

    String getProfileWithHash() {return fileManager.getProfileWithHash();}

    String createDownloadPathFilterPattern() {
        return ".*%s/%s/%s/%s/[0-9]{12,}/.*".formatted(graphsRepoCoverage, graphsRepoGraphVersion, routeProfileName, fileManager.getHash());
    }

    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(graphsRepoBaseUrl) || isNullOrEmpty(graphsRepoName) || isNullOrEmpty(graphsRepoCoverage) || isNullOrEmpty(graphsRepoGraphVersion)) {
            LOGGER.debug("[%s] ORSGraphManager is not configured - skipping check".formatted(getProfileWithHash()));
            return;
        }
        if (fileManager.isActive()) {
            LOGGER.info("[%s] ORSGraphManager is active - skipping download".formatted(getProfileWithHash()));
            return;
        }

        LOGGER.info("[%s] Checking for possible graph update from remote repository...".formatted(getProfileWithHash()));
        try {
            ORSGraphInfoV1 persistedRemoteGraphInfo = fileManager.getPreviouslyDownloadedRemoteGraphInfo();
            File graphDownloadFile = fileManager.getGraphDownloadFile();
            GraphInfo localGraphInfo = fileManager.getLocalGraphInfo();
            GraphInfo remoteGraphInfo = downloadLatestGraphInfoFromRepository();

            if (!shouldDownloadGraph(remoteGraphInfo, localGraphInfo, graphDownloadFile, persistedRemoteGraphInfo)) {
                return;
            }

            String downloadUrl = fileManager.createGraphUrlFromGraphInfoUrl(remoteGraphInfo);
            LOGGER.info("[%s] Downloading %s to file %s".formatted(getProfileWithHash(), downloadUrl, graphDownloadFile.getAbsolutePath()));
            downloadAsset(downloadUrl, graphDownloadFile);
        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileWithHash()), e);
        }
    }

    public boolean shouldDownloadGraph(GraphInfo remoteGraphInfo, GraphInfo localGraphInfo, File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo) {
        if (!remoteGraphInfo.exists()) {
            LOGGER.info("[%s] There is no graph in remote repository - nothing to download.".formatted(getProfileWithHash()));
            return false;
        }
        if (persistedDownloadFile.exists() && persistedRemoteGraphInfo != null) {
            if (remoteGraphInfo.getPersistedGraphInfo().getOsmDate().after(persistedRemoteGraphInfo.getOsmDate())) {
                LOGGER.info("[%s] Found local file %s from previous download but downloading newer version from repository.".formatted(getProfileWithHash(), persistedDownloadFile.getAbsolutePath()));
                return true;
            } else {
                LOGGER.info("[%s] Found local file %s from previous download, there is no newer version in the repository.".formatted(getProfileWithHash(), persistedDownloadFile.getAbsolutePath()));
                return false;
            }
        }
        if (!localGraphInfo.exists()) {
            LOGGER.info("[%s] There is no local graph - should be downloaded.".formatted(getProfileWithHash()));
            return true;
        }
        if (!remoteGraphInfo.getPersistedGraphInfo().getOsmDate().after(localGraphInfo.getPersistedGraphInfo().getOsmDate())) {
            LOGGER.info("[%s] Graph in remote repository is not newer than local graph - keeping local graph".formatted(getProfileWithHash()));
            return false;
        }
        LOGGER.info("[%s] Graph in remote repository is newer than local graph - should be downloaded".formatted(getProfileWithHash()));
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
                LOGGER.debug("[%s] Trying to call nexus api with graphsRepoBaseUrl=%s graphsRepoName=%s graphsRepoCoverage=%s, graphsRepoGraphVersion=%s, continuationToken=%s".formatted(
                        getProfileWithHash(), graphsRepoBaseUrl, graphsRepoName, graphsRepoCoverage, graphsRepoGraphVersion, continuationToken));
                PageAssetXO assets = assetsApi.getAssets(graphsRepoName, continuationToken);
                LOGGER.trace("[%s] Received assets: %s".formatted(getProfileWithHash(), assets.toString()));
                if (assets.getItems() != null) {
                    items.addAll(assets.getItems());
                }
                continuationToken = assets.getContinuationToken();
            } while (!isBlank(continuationToken));
            LOGGER.debug("[%s] Found %d items total".formatted(getProfileWithHash(), items.size()));

            return filterLatestAsset(fileName, items);

        } catch (ApiException e) {
            LOGGER.error("[%s] Exception when calling AssetsApi#getAssets".formatted(getProfileWithHash()));
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
        LOGGER.debug("[%s] Checking latest graphInfo in remote repository...".formatted(getProfileWithHash()));

        String fileName = fileManager.createGraphInfoFileName();
        AssetXO latestGraphInfoAsset = findLatestGraphInfoAsset(fileName);
        if (latestGraphInfoAsset == null) {
            LOGGER.warn("[%s] No graphInfo found in remote repository".formatted(getProfileWithHash()));
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
            LOGGER.error("[%s] Invalid download URL for graphInfo asset: %s".formatted(getProfileWithHash(), latestGraphInfoAsset.getDownloadUrl()));
        }

        return latestGraphInfoInRepo;
    }

    AssetXO filterLatestAsset(String fileName, List<AssetXO> items) {
        String downloadPathFilterPattern = createDownloadPathFilterPattern();
        LOGGER.debug("[%s] Filtering %d assets for pattern '%s' and fileName=%s".formatted(getProfileWithHash(), items.size(), downloadPathFilterPattern, fileName));

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
