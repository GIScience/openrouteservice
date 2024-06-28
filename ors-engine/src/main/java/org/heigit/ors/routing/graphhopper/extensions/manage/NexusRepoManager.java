package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
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
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NexusRepoManager implements ORSGraphRepoManager {

    private static final Logger LOGGER = Logger.getLogger(NexusRepoManager.class.getName());
    private int connectionTimeoutMillis = 2000;
    private int readTimeoutMillis = 200000;
    private String routeProfileName;
    private String graphsRepoBaseUrl;
    private String graphsRepoPath;
    private String graphsRepoName;
    private String graphsProfileGroup;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;

    public NexusRepoManager() {
    }

    public NexusRepoManager(EngineConfig engineConfig, String graphsRepoGraphVersion, String routeProfileName, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.graphsRepoBaseUrl = engineConfig.getGraphsRepoUrl();
        this.graphsRepoPath = engineConfig.getGraphsRepoPath();
        this.graphsRepoName = engineConfig.getGraphsRepoName();
        this.graphsRepoCoverage = engineConfig.getGraphsExtent();
        this.graphsProfileGroup = engineConfig.getGraphsProfileGroup();
        this.graphsRepoGraphVersion = graphsRepoGraphVersion;
        this.routeProfileName = routeProfileName;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
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

    public void setOrsGraphFileManager(ORSGraphFileManager orsGraphFileManager) {
        this.orsGraphFileManager = orsGraphFileManager;
    }

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    public String getGraphsRepoPath() {
        return graphsRepoPath;
    }

    public void setGraphsRepoPath(String graphsRepoPath) {
        this.graphsRepoPath = graphsRepoPath;
    }

    public void setRouteProfileName(String routeProfileName) {
        this.routeProfileName = routeProfileName;
    }

    public void setGraphsProfileGroup(String graphsProfileGroup) {
        this.graphsProfileGroup = graphsProfileGroup;
    }

    public void setOrsGraphRepoStrategy(ORSGraphRepoStrategy orsGraphRepoStrategy) {
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
    }

    String createDownloadPathFilterPattern() {
        String assetFilterPattern = orsGraphRepoStrategy.getAssetFilterPattern(
                this.graphsRepoName,
                this.graphsRepoCoverage,
                this.graphsRepoGraphVersion,
                this.graphsProfileGroup,
                this.routeProfileName,
                this.orsGraphRepoStrategy.getRepoGraphInfoFileName()
        );
        LOGGER.trace("assetFilterPattern: " + assetFilterPattern);
        return assetFilterPattern;
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(graphsRepoBaseUrl) || isNullOrEmpty(graphsRepoName) || isNullOrEmpty(graphsRepoCoverage) || isNullOrEmpty(graphsRepoGraphVersion)) {
            LOGGER.debug("[%s] ORSGraphManager is not configured - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

        LOGGER.debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            ORSGraphInfoV1 previouslyDownloadedGraphInfo = orsGraphFileManager.getDownloadedGraphInfo();
            File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
            GraphInfo activeGraphInfo = orsGraphFileManager.getActiveGraphInfo();
            GraphInfo downloadedExtractedGraphInfo = orsGraphFileManager.getDownloadedExtractedGraphInfo();
            GraphInfo newlyDownloadedGraphInfo = downloadLatestGraphInfoFromRepository();

            if (!shouldDownloadGraph(
                    getDateOrEpocStart(newlyDownloadedGraphInfo),
                    getDateOrEpocStart(activeGraphInfo),
                    getDateOrEpocStart(downloadedExtractedGraphInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphInfo))) {
                return;
            }

            String downloadUrl = createGraphUrlFromGraphInfoUrl(newlyDownloadedGraphInfo);
            LOGGER.info("[%s] Downloading %s to file %s".formatted(getProfileDescriptiveName(), downloadUrl, downloadedCompressedGraphFile.getName()));

            long start = System.currentTimeMillis();
            downloadAsset(downloadUrl, downloadedCompressedGraphFile);
            long end = System.currentTimeMillis();
            LOGGER.info("[%s] Download finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), e);
        }
    }

    String createGraphUrlFromGraphInfoUrl(GraphInfo remoteGraphInfo) {
        String url = remoteGraphInfo.getRemoteUrl().toString();
        String urlWithoutFileName = url.substring(0, url.lastIndexOf('/'));
        return urlWithoutFileName + "/" + orsGraphRepoStrategy.getRepoCompressedGraphFileName();
    }

    public boolean shouldDownloadGraph(Date remoteDate, Date activeDate, Date downloadedExtractedDate, Date downloadedCompressedDate) {
        Date newestLocalDate = newestDate(activeDate, downloadedExtractedDate, downloadedCompressedDate);
        return remoteDate.after(newestLocalDate);
    }

    Date getDateOrEpocStart(GraphInfo graphInfo) {
        return Optional.ofNullable(graphInfo)
                .map(GraphInfo::getPersistedGraphInfo)
                .map(ORSGraphInfoV1::getOsmDate)
                .orElse(new Date(0L));
    }

    Date getDateOrEpocStart(File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo) {
        if (persistedDownloadFile==null) {
            return new Date(0L);
        }

        if (persistedDownloadFile.exists()) {
            return Optional.ofNullable(persistedRemoteGraphInfo)
                    .map(ORSGraphInfoV1::getOsmDate)
                    .orElse(new Date(0L));
        }

        return new Date(0L);
    }

    Date newestDate(Date... dates) {
        return Arrays.stream(dates).max(Date::compareTo).orElse(new Date(0L));
    }

    public AssetXO findLatestGraphInfoAsset() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(graphsRepoBaseUrl);

        AssetsApi assetsApi = new AssetsApi(defaultClient);

        try {
            List<AssetXO> items = new ArrayList<>();
            String continuationToken = null;
            do {
                LOGGER.trace("[%s] Trying to call nexus api with graphsRepoBaseUrl=%s graphsRepoName=%s graphsRepoCoverage=%s, graphsRepoGraphVersion=%s, continuationToken=%s".formatted(
                        getProfileDescriptiveName(), graphsRepoBaseUrl, graphsRepoName, graphsRepoCoverage, graphsRepoGraphVersion, continuationToken));
                PageAssetXO assets = assetsApi.getAssets(graphsRepoName, continuationToken);
                LOGGER.trace("[%s] Received assets: %s".formatted(getProfileDescriptiveName(), assets.toString()));
                if (assets.getItems() != null) {
                    items.addAll(assets.getItems());
                }
                continuationToken = assets.getContinuationToken();
            } while (!isBlank(continuationToken));
            LOGGER.trace("[%s] Found %d items total".formatted(getProfileDescriptiveName(), items.size()));

            return filterLatestAsset(items);

        } catch (ApiException e) {
            LOGGER.error("[%s] Exception when calling AssetsApi#getAssets".formatted(getProfileDescriptiveName()));
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
        LOGGER.debug("[%s] Checking latest graphInfo in remote repository...".formatted(getProfileDescriptiveName()));

        AssetXO latestGraphInfoAsset = findLatestGraphInfoAsset();
        if (latestGraphInfoAsset == null) {
            LOGGER.info("[%s] No graphInfo found in remote repository".formatted(getProfileDescriptiveName()));
            return latestGraphInfoInRepo;
        }

        File downloadedFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        downloadAsset(latestGraphInfoAsset.getDownloadUrl(), downloadedFile);

        try {
            URL url = new URL(latestGraphInfoAsset.getDownloadUrl());
            latestGraphInfoInRepo.setRemoteUrl(url);

            ORSGraphInfoV1 orsGraphInfoV1 = orsGraphFileManager.readOrsGraphInfoV1(downloadedFile);
            latestGraphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        } catch (MalformedURLException e) {
            LOGGER.error("[%s] Invalid download URL for graphInfo asset: %s".formatted(getProfileDescriptiveName(), latestGraphInfoAsset.getDownloadUrl()));
        }

        return latestGraphInfoInRepo;
    }

    AssetXO filterLatestAsset(List<AssetXO> items) {
        String downloadPathFilterPattern = createDownloadPathFilterPattern();
        LOGGER.debug("[%s] Filtering %d assets for pattern '%s'".formatted(getProfileDescriptiveName(), items.size(), downloadPathFilterPattern));

        // paths like https://repo.heigit.org/ors-graphs-traffic/planet/3/car/5a5af307fbb8019bfb69d4916f55ddeb/202212312359/5a5af307fbb8019bfb69d4916f55ddeb.json
        Optional<AssetXO> first = items.stream()
                .filter(assetXO -> assetXO.getPath().matches(downloadPathFilterPattern))
                .sorted((a1, a2) -> a2.getPath().compareTo(a1.getPath()))//sort reverse: latest date (path parameter) first
                .findFirst();

        return first.orElse(null);
    }

    public void downloadAsset(String downloadUrl, File outputFile) {
        File tempDownloadFile = orsGraphFileManager.asIncompleteFile(outputFile);
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
