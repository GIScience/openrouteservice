package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@NoArgsConstructor
public class HttpGraphRepoClient extends AbstractGraphRepoClient implements ORSGraphRepoClient {

    private static final Logger LOGGER = Logger.getLogger(HttpGraphRepoClient.class.getName());
    private GraphManagementRuntimeProperties managementProps;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;

    public HttpGraphRepoClient(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.managementProps = managementProps;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    @Override
    ORSGraphFileManager getOrsGraphFileManager() {
        return orsGraphFileManager;
    }

    @Override
    ORSGraphRepoStrategy getOrsGraphRepoStrategy() {
        return orsGraphRepoStrategy;
    }

    @Override
    GraphManagementRuntimeProperties getGraphManagementRuntimeProperties() {
        return managementProps;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    public static String concatenateToUrlPath(String... values) {
        return Stream.of(values)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(s -> s.replaceAll("^/", ""))
                .map(s -> s.replaceAll("/$", ""))
                .filter(s -> !s.equals("."))
                .collect(Collectors.joining("/"));
    }

    public URL createDownloadUrl(String fileName) {
        String urlString = concatenateToUrlPath(this.managementProps.getDerivedRepoBaseUrl().toString(),
                this.managementProps.getRepoName(),
                this.managementProps.getRepoProfileGroup(),
                this.managementProps.getRepoCoverage(),
                this.managementProps.getGraphVersion(),
                fileName);

        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.debug("[%s] Generated invalid download URL for graphBuildInfo file: %s".formatted(getProfileDescriptiveName(), urlString));
            return null;
        }
    }

    boolean isValidRepoConfig() {
        return isNotBlank(managementProps.getRepoName()) &&
                isNotBlank(managementProps.getRepoCoverage()) &&
                isNotBlank(managementProps.getGraphVersion()) &&
                isNotBlank(managementProps.getDerivedRepoBaseUrl().toString());
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (! isValidRepoConfig()) {
            LOGGER.debug("[%s] ORSGraphManager has no valid repo config - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (orsGraphFileManager.isBusy()) {
            LOGGER.debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

        LOGGER.debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            GraphBuildInfo newlyDownloadedGraphBuildInfo = downloadGraphBuildInfoFromRepository();
            if (!shouldDownloadGraph(newlyDownloadedGraphBuildInfo)) {
                return;
            }

            downloadCompressedGraphFromRepository();

        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), e);
        }
    }

    GraphBuildInfo downloadGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo graphBuildInfoInRepo = new GraphBuildInfo();
        LOGGER.debug("[%s] Checking latest graphBuildInfo in remote repository...".formatted(getProfileDescriptiveName()));

        URL downloadUrl = createDownloadUrl(orsGraphRepoStrategy.getRepoGraphBuildInfoFileName());
        if (downloadUrl == null) {
            return graphBuildInfoInRepo;
        }
        File downloadedGraphBuildInfoFile = orsGraphFileManager.getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile, "[%s] Deleted old downloaded graphBuildInfo file: %s", "[%s] Could not delete old downloaded graphBuildInfo file: %s");
        downloadFile(downloadUrl, downloadedGraphBuildInfoFile);
        if (!downloadedGraphBuildInfoFile.exists()) {
            LOGGER.info("[%s] No graphBuildInfo found in remote repository.".formatted(getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUrl(downloadUrl);
        PersistedGraphBuildInfo persistedGraphBuildInfo = orsGraphFileManager.readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
        graphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        return graphBuildInfoInRepo;
    }

    void downloadCompressedGraphFromRepository() {
        URL downloadUrl = createDownloadUrl(orsGraphRepoStrategy.getRepoCompressedGraphFileName());
        if (downloadUrl == null) {
            return;
        }
        File downloadedFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        deleteFileWithLogging(downloadedFile, "[%s] Deleted old downloaded compressed graph file: %s", "[%s] Could not delete old downloaded compressed graph file: %s");

        long start = System.currentTimeMillis();
        downloadFile(downloadUrl, downloadedFile);//mocked!!!
        long end = System.currentTimeMillis();

        if (orsGraphFileManager.getDownloadedCompressedGraphFile().exists()) {
            LOGGER.info("[%s] Download of compressed graph file finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
        } else {
            LOGGER.info("[%s] Compressed graph file not found in remote repository.".formatted(getProfileDescriptiveName()));
        }
    }

    public void downloadFile(URL downloadUrl, File outputFile) {
        File tempDownloadFile = orsGraphFileManager.asIncompleteFile(outputFile);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[%s] Downloading %s to local file %s...".formatted(getProfileDescriptiveName(), downloadUrl, tempDownloadFile.getAbsolutePath()));
        } else {
            LOGGER.info("[%s] Downloading %s...".formatted(getProfileDescriptiveName(), downloadUrl));
        }
        try {
            int connectionTimeoutMillis = 2000;
            int readTimeoutMillis = 200000;
            FileUtils.copyURLToFile(
                    downloadUrl,
                    tempDownloadFile,
                    connectionTimeoutMillis,
                    readTimeoutMillis);
            if (tempDownloadFile.renameTo(outputFile)) {
                LOGGER.debug("[%s] Renamed temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            } else {
                LOGGER.error("[%s] Could not rename temp file to %s".formatted(getProfileDescriptiveName(), outputFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            LOGGER.warn("[%s] Caught %s when trying to download %s".formatted(getProfileDescriptiveName(), e.getClass().getName(), downloadUrl));
        } finally {
            deleteFileWithLogging(tempDownloadFile, "[%s] Deleted temp download file: %s", "[%s] Could not delete temp download file: %s");
        }
    }
}
