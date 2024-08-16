package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
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

@Setter
public class NexusRepoManager extends AbstractRepoManager implements ORSGraphRepoManager {

    private static final Logger LOGGER = Logger.getLogger(NexusRepoManager.class.getName());
    private int connectionTimeoutMillis = 2000;
    private int readTimeoutMillis = 200000;
    private String routeProfileName;
    private String graphsRepoBaseUrl;
    private String graphsRepoName;
    private String graphsProfileGroup;
    private String graphsRepoCoverage;
    private String graphsRepoGraphVersion;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;

    public NexusRepoManager() {
    }

    public NexusRepoManager(URL repoUrl, EngineProperties engineProperties, String routeProfileName, String graphVersion, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.graphsRepoBaseUrl = repoUrl.toString();
        this.graphsRepoName = engineProperties.getGraphManagement().getRepositoryName();
        this.graphsRepoCoverage = engineProperties.getGraphManagement().getGraphExtent();
        this.graphsProfileGroup = engineProperties.getGraphManagement().getRepositoryProfileGroup();
        this.graphsRepoGraphVersion = graphVersion;
        this.routeProfileName = routeProfileName;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
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
                LOGGER.info("[%s] No newer graph found in repository.".formatted(getProfileDescriptiveName()));
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
            LOGGER.error("[%s] Exception when calling AssetsApi#getAssets%n - Status code           : %s%n - Response headers      : %s%n - graphsRepoBaseUrl     : %s%n - graphsRepoName        : %s%n - graphsRepoCoverage    : %s%n - graphsRepoGraphVersion: %s".formatted(
                    getProfileDescriptiveName(),
                    e.getCode(),
                    e.getResponseHeaders(),
                    graphsRepoBaseUrl,
                    graphsRepoName,
                    graphsRepoCoverage,
                    graphsRepoGraphVersion));
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

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        downloadAsset(latestGraphInfoAsset.getDownloadUrl(), downloadedGraphInfoFile);//mocked!!!

        try {
            URL url = new URL(latestGraphInfoAsset.getDownloadUrl());
            latestGraphInfoInRepo.setRemoteUrl(url);

            ORSGraphInfoV1 orsGraphInfoV1 = orsGraphFileManager.readOrsGraphInfoV1(downloadedGraphInfoFile);
            latestGraphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        } catch (MalformedURLException e) {
            LOGGER.error("[%s] Invalid download URL for graphInfo asset: %s".formatted(getProfileDescriptiveName(), latestGraphInfoAsset.getDownloadUrl()));
        }

        return latestGraphInfoInRepo;
    }

    public AssetXO filterLatestAsset(List<AssetXO> items) {
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
