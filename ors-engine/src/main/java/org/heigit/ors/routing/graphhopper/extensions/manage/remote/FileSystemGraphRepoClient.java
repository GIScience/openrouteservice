package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Strings.isNullOrEmpty;

public class FileSystemGraphRepoClient extends AbstractGraphRepoClient implements ORSGraphRepoClient {

    private static final Logger LOGGER = Logger.getLogger(FileSystemGraphRepoClient.class.getName());
    private final String graphsRepoPath;
    private final String graphsRepoName;
    private final String graphsProfileGroup;
    private final String graphsRepoCoverage;
    private final String graphsRepoGraphVersion;
    private final ORSGraphFileManager orsGraphFileManager;
    private final ORSGraphRepoStrategy orsGraphRepoStrategy;

    public FileSystemGraphRepoClient(GraphManagementRuntimeProperties graphManagementRuntimeProperties, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.graphsRepoPath = graphManagementRuntimeProperties.getDerivedRepoPath().toAbsolutePath().toString();
        this.graphsRepoName = graphManagementRuntimeProperties.getRepoName();
        this.graphsProfileGroup = graphManagementRuntimeProperties.getRepoProfileGroup();
        this.graphsRepoCoverage = graphManagementRuntimeProperties.getRepoCoverage();
        this.graphsRepoGraphVersion = graphManagementRuntimeProperties.getGraphVersion();
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(graphsRepoPath) || isNullOrEmpty(graphsRepoName) || isNullOrEmpty(graphsRepoCoverage) || isNullOrEmpty(graphsRepoGraphVersion)) {
            LOGGER.debug("[%s] ORSGraphManager is not configured - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

        LOGGER.debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            PersistedGraphBuildInfo previouslyDownloadedGraphBuildInfo = orsGraphFileManager.getDownloadedGraphBuildInfo();
            File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
            GraphBuildInfo activeGraphBuildInfo = orsGraphFileManager.getActiveGraphBuildInfo();
            GraphBuildInfo downloadedExtractedGraphBuildInfo = orsGraphFileManager.getDownloadedExtractedGraphBuildInfo();
            GraphBuildInfo newlyDownloadedGraphBuildInfo = downloadLatestGraphBuildInfoFromRepository();
            LOGGER.trace(("[%s] Comparing dates, downloading if first is after the others:%n" +
                    "                         repo=%s%n" +
                    "              activeGraphBuildInfo=%s%n" +
                    " downloadedExtractedGraphBuildInfo=%s%n" +
                    "previouslyDownloadedGraphBuildInfo=%s").formatted(getProfileDescriptiveName(),
                    getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                    getDateOrEpocStart(activeGraphBuildInfo),
                    getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo)));

            if (!shouldDownloadGraph(
                    getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                    getDateOrEpocStart(activeGraphBuildInfo),
                    getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo))) {
                LOGGER.info("[%s] No newer graph found in repository.".formatted(getProfileDescriptiveName()));
                return;
            }

            Path latestCompressedGraphInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoCompressedGraphFileName());
            long start = System.currentTimeMillis();
            downloadFile(latestCompressedGraphInRepoPath, downloadedCompressedGraphFile);

            long end = System.currentTimeMillis();
            if (downloadedCompressedGraphFile.exists()) {
                LOGGER.info("[%s] Download of compressed graph file finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
            } else {
                LOGGER.error("[%s] Invalid download path for compressed graph file: %s".formatted(getProfileDescriptiveName(), latestCompressedGraphInRepoPath));
            }
        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), e);
        }
    }

    GraphBuildInfo downloadLatestGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo latestGraphBuildInfoInRepo = new GraphBuildInfo();
        LOGGER.debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        Path latestGraphBuildInfoInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoGraphBuildInfoFileName());
        if (!latestGraphBuildInfoInRepoPath.toFile().exists()) {
            LOGGER.info("[%s] No graphBuildInfo found in remote repository: %s".formatted(getProfileDescriptiveName(), latestGraphBuildInfoInRepoPath.toFile().getAbsolutePath()));
            return latestGraphBuildInfoInRepo;
        }

        File downloadedGraphBuildInfoFile = orsGraphFileManager.getDownloadedGraphBuildInfoFile();
        downloadFile(latestGraphBuildInfoInRepoPath, downloadedGraphBuildInfoFile);

        if (downloadedGraphBuildInfoFile.exists()) {
            Path latestCompressedGraphInRepoPath = Path.of(graphsRepoPath, graphsRepoName, graphsProfileGroup, graphsRepoCoverage, graphsRepoGraphVersion, orsGraphRepoStrategy.getRepoCompressedGraphFileName());
            URI uri = latestCompressedGraphInRepoPath.toUri();
            latestGraphBuildInfoInRepo.setRemoteUri(uri);

            PersistedGraphBuildInfo persistedGraphBuildInfo = orsGraphFileManager.readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
            latestGraphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        } else {
            LOGGER.error("[%s] Invalid download path for graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), latestGraphBuildInfoInRepoPath));
        }

        return latestGraphBuildInfoInRepo;
    }

    public void downloadFile(Path repoPath, File localPath) {
        if (repoPath == null || localPath == null) {
            LOGGER.warn("[%s] Invalid download or local path: %s or %s".formatted(getProfileDescriptiveName(), repoPath, localPath));
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), repoPath.toFile().getAbsolutePath(), localPath.getAbsolutePath()));
        } else {
            LOGGER.info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), repoPath.toFile().getName()));
        }
        try {
            Files.copy(repoPath, localPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("[%s] Caught %s when trying to download %s".formatted(getProfileDescriptiveName(), e, repoPath.toFile().getAbsolutePath()));
            throw new IllegalArgumentException(e);
        }
    }
}
