package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.commons.io.FileUtils;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class HttpGraphRepoClient extends AbstractGraphRepoClient {

    public HttpGraphRepoClient(GraphManagementRuntimeProperties managementProps, ORSGraphRepoStrategy orsGraphRepoStrategy, ORSGraphFileManager orsGraphFileManager) {
        super(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
    }

    @Override
    public boolean hasValidRepoConfig() {
        return isNotBlank(getManagementProps().getRepoName()) &&
                isNotBlank(getManagementProps().getRepoProfileGroup()) &&
                isNotBlank(getManagementProps().getRepoCoverage()) &&
                isNotBlank(getManagementProps().getGraphVersion()) &&
                isNotBlank(getManagementProps().getDerivedRepoBaseUrl().toString());
    }

    @Override
    GraphBuildInfo downloadGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        GraphBuildInfo graphBuildInfoInRepo = new GraphBuildInfo();
        //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
        getLogger().debug("[%s] Checking latest graphBuildInfo in remote repository..."
                .formatted(getGraphFileManager().getProfileDescriptiveName()));

        URL downloadUrl = createDownloadUrl(getGraphRepoStrategy().getRepoGraphBuildInfoFileName());
        if (downloadUrl == null) {
            return graphBuildInfoInRepo;
        }
        File downloadedGraphBuildInfoFile = getGraphFileManager().getDownloadedGraphBuildInfoFile();
        deleteFileWithLogging(downloadedGraphBuildInfoFile, "[%s] Deleted old downloaded graphBuildInfo file: %s", "[%s] Could not delete old downloaded graphBuildInfo file: %s");
        downloadFile(downloadUrl, downloadedGraphBuildInfoFile);
        if (!downloadedGraphBuildInfoFile.exists()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] No graphBuildInfo found in remote repository."
                    .formatted(getGraphFileManager().getProfileDescriptiveName()));
            return graphBuildInfoInRepo;
        }

        graphBuildInfoInRepo.withRemoteUrl(downloadUrl);
        PersistedGraphBuildInfo persistedGraphBuildInfo = getGraphFileManager().readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
        graphBuildInfoInRepo.setPersistedGraphBuildInfo(persistedGraphBuildInfo);
        return graphBuildInfoInRepo;
    }

    @Override
    protected void downloadCompressedGraphFromRepository() {
        URL downloadUrl = createDownloadUrl(getGraphRepoStrategy().getRepoCompressedGraphFileName());
        if (downloadUrl == null) {
            return;
        }
        File downloadedFile = getGraphFileManager().getDownloadedCompressedGraphFile();
        deleteFileWithLogging(downloadedFile, "[%s] Deleted old downloaded compressed graph file: %s", "[%s] Could not delete old downloaded compressed graph file: %s");

        long start = System.currentTimeMillis();
        downloadFile(downloadUrl, downloadedFile);//mocked!!!
        long end = System.currentTimeMillis();

        if (getGraphFileManager().getDownloadedCompressedGraphFile().exists()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] Download of compressed graph file finished after %d ms"
                    .formatted(getGraphFileManager().getProfileDescriptiveName(), end - start));
        } else {
            getLogger().info("[%s] Compressed graph file not found in remote repository."
                    .formatted(getGraphFileManager().getProfileDescriptiveName()));
        }
    }

    public URL createDownloadUrl(String fileName) {
        String urlString = concatenateToUrlPath(getManagementProps().getDerivedRepoBaseUrl().toString(),
                getManagementProps().getRepoName(),
                getManagementProps().getRepoProfileGroup(),
                getManagementProps().getRepoCoverage(),
                getManagementProps().getGraphVersion(),
                fileName);
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            getLogger().debug("[%s] Generated invalid download URL for graphBuildInfo file: %s"
                    .formatted(getGraphFileManager().getProfileDescriptiveName(), urlString));
            return null;
        }
    }

    public void downloadFile(URL downloadUrl, File outputFile) {
        File tempDownloadFile = getGraphFileManager().asIncompleteFile(outputFile);
        if (getLogger().isTraceEnabled()) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().trace("[%s] Downloading %s to local file %s...".formatted(
                    getGraphFileManager().getProfileDescriptiveName(),
                    downloadUrl,
                    tempDownloadFile.getAbsolutePath()));
        } else {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] Downloading %s...".formatted(
                    getGraphFileManager().getProfileDescriptiveName(),
                    downloadUrl));
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
                getLogger().debug("[%s] Renamed temp file to %s".formatted(
                        getGraphFileManager().getProfileDescriptiveName(),
                        outputFile.getAbsolutePath()));
            } else {
                getLogger().error("[%s] Could not rename temp file to %s".formatted(
                        getGraphFileManager().getProfileDescriptiveName(),
                        outputFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            getLogger().warn("[%s] Caught %s when trying to download %s".formatted(
                    getGraphFileManager().getProfileDescriptiveName(),
                    e.getClass().getName(),
                    downloadUrl));
        } finally {
            deleteFileWithLogging(tempDownloadFile, "[%s] Deleted temp download file: %s", "[%s] Could not delete temp download file: %s");
        }
    }
}
