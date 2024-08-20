package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;

@Setter
public class HttpRepoManager extends AbstractRepoManager implements ORSGraphRepoManager {

    private static final Logger LOGGER = Logger.getLogger(HttpRepoManager.class.getName());
    private int connectionTimeoutMillis = 2000;
    private int readTimeoutMillis = 200000;
    private GraphManagementRuntimeProperties managementProps;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;

    public HttpRepoManager() {
    }

    public HttpRepoManager(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        this.managementProps = managementProps;
        this.orsGraphRepoStrategy = orsGraphRepoStrategy;
        this.orsGraphFileManager = orsGraphFileManager;
    }

    String getProfileDescriptiveName() {
        return orsGraphFileManager.getProfileDescriptiveName();
    }

    public URL createDownloadUrl(String fileName) {
        String urlString = concatenateToUrlPath(this.managementProps.getDerivedRepoBaseUrl().toString(),
                this.managementProps.getRepoName(),
                this.managementProps.getRepoProfileGroup(),
                this.managementProps.getRepoCoverage(),
                this.managementProps.getLocalGraphVersion(),
                fileName);

        try { return new URL(urlString); }
        catch (MalformedURLException e) {
            LOGGER.debug("[%s] Generated invalid download URL for graphInfo file: %s".formatted(getProfileDescriptiveName(), urlString));
            return null;
        }
    }

    public static String concatenateToUrlPath(String... values) {
        String urlString = Stream.of(values)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(s -> s.replaceAll("^/", ""))
                .map(s -> s.replaceAll("/$", ""))
                .filter(s -> !s.equals("."))
                .collect(Collectors.joining("/"));
        return urlString;
    }

    @Override
    public void downloadGraphIfNecessary() {
        if (isNullOrEmpty(this.managementProps.getDerivedRepoBaseUrl().toString()) || isNullOrEmpty(this.managementProps.getRepoName()) || isNullOrEmpty(this.managementProps.getRepoCoverage()) || isNullOrEmpty(this.managementProps.getLocalGraphVersion())) {
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
            GraphInfo newlyDownloadedGraphInfo = downloadGraphInfoFromRepository();

            if (!shouldDownloadGraph(
                    getDateOrEpocStart(newlyDownloadedGraphInfo),
                    getDateOrEpocStart(activeGraphInfo),
                    getDateOrEpocStart(downloadedExtractedGraphInfo),
                    getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphInfo))) {
                LOGGER.info("[%s] No newer graph found in repository.".formatted(getProfileDescriptiveName()));
                return;
            }

            downloadCompressedGraphFromRepository();

        } catch (Exception e) {
            LOGGER.error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), e);
        }
    }

    GraphInfo downloadGraphInfoFromRepository() {
        GraphInfo graphInfoInRepo = new GraphInfo();
        LOGGER.debug("[%s] Checking latest graphInfo in remote repository...".formatted(getProfileDescriptiveName()));

        URL downloadUrl = createDownloadUrl(orsGraphRepoStrategy.getRepoGraphInfoFileName());
        if (downloadUrl == null) {
            return graphInfoInRepo;
        }
        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        if (downloadedGraphInfoFile.exists()) {
            downloadedGraphInfoFile.delete();
        }
        downloadFile(downloadUrl, downloadedGraphInfoFile);//mocked!!!
        if (!downloadedGraphInfoFile.exists()) {
            LOGGER.info("[%s] No graphInfo found in remote repository".formatted(getProfileDescriptiveName()));
            return graphInfoInRepo;
        }

        graphInfoInRepo.setRemoteUrl(downloadUrl);
        ORSGraphInfoV1 orsGraphInfoV1 = orsGraphFileManager.readOrsGraphInfoV1(downloadedGraphInfoFile);
        graphInfoInRepo.withPersistedInfo(orsGraphInfoV1);
        return graphInfoInRepo;
    }

    void downloadCompressedGraphFromRepository() {
        URL downloadUrl = createDownloadUrl(orsGraphRepoStrategy.getRepoCompressedGraphFileName());
        if (downloadUrl == null) {
            return;
        }
        File downloadedFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        if (downloadedFile.exists()) {
            downloadedFile.delete();
        }

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
        try {
            FileUtils.copyURLToFile(
                    downloadUrl,
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
