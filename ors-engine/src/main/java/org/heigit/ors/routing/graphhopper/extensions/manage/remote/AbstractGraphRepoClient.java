package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

abstract class AbstractGraphRepoClient implements ORSGraphRepoClient {

    abstract ORSGraphFileManager getOrsGraphFileManager();
    abstract ORSGraphRepoStrategy getOrsGraphRepoStrategy();
    abstract GraphManagementRuntimeProperties getGraphManagementRuntimeProperties();
    abstract Logger getLogger();

    abstract boolean isValidRepoConfig();
    abstract GraphBuildInfo downloadGraphBuildInfoFromRepository();
    abstract void downloadCompressedGraphFromRepository();

     public void downloadGraphIfNecessary() {
        if (! isValidRepoConfig()) {
            getLogger().debug("[%s] ORSGraphManager has no valid repo config - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }
        if (getOrsGraphFileManager().isBusy()) {
            getLogger().debug("[%s] ORSGraphManager is busy - skipping check".formatted(getProfileDescriptiveName()));
            return;
        }

         getLogger().debug("[%s] Checking for possible graph update from remote repository...".formatted(getProfileDescriptiveName()));
        try {
            GraphBuildInfo newlyDownloadedGraphBuildInfo = downloadGraphBuildInfoFromRepository();

            if (!shouldDownloadGraph(newlyDownloadedGraphBuildInfo)) {
                return;
            }

            long start = System.currentTimeMillis();
            downloadCompressedGraphFromRepository();
            long end = System.currentTimeMillis();

            if (getOrsGraphFileManager().getDownloadedCompressedGraphFile().exists()) {
                getLogger().info("[%s] Download of compressed graph file finished after %d ms".formatted(getProfileDescriptiveName(), end - start));
            } else {
                getLogger().info("[%s] Compressed graph file not found in remote repository.".formatted(getProfileDescriptiveName()));
            }
        } catch (Exception exception) {
            getLogger().error("[%s] Caught an exception during graph download check or graph download:".formatted(getProfileDescriptiveName()), exception);
        }
    }

    String getGraphVersion() {
        return getGraphManagementRuntimeProperties().getGraphVersion();
    }

    String getRepoCoverage() {
        return getGraphManagementRuntimeProperties().getRepoCoverage();
    }

    String getRepoProfileGroup() {
        return getGraphManagementRuntimeProperties().getRepoProfileGroup();
    }
    String getRepoPass() {
        return getGraphManagementRuntimeProperties().getRepoPass();
    }

    String getRepoUser() {
        return getGraphManagementRuntimeProperties().getRepoUser();
    }

    URL getDerivedRepoBaseUrl() {
        return getGraphManagementRuntimeProperties().getDerivedRepoBaseUrl();
    }

    String getRepoName() {
        return getGraphManagementRuntimeProperties().getRepoName();
    }

    String getRepoBaseUri() {
        return getGraphManagementRuntimeProperties().getRepoBaseUri();
    }

    String getRepoPath() {
        return getGraphManagementRuntimeProperties().getDerivedRepoPath().toAbsolutePath().toString();
    }

    File getIncompleteFile(File outputFile) {
        return getOrsGraphFileManager().asIncompleteFile(outputFile);
    }

    PersistedGraphBuildInfo getPersistedGraphBuildInfo(File downloadedGraphBuildInfoFile) {
        return getOrsGraphFileManager().readOrsGraphBuildInfo(downloadedGraphBuildInfoFile);
    }

    String getProfileDescriptiveName() {
        return getOrsGraphFileManager().getProfileDescriptiveName();
    }

    String getRepoGraphBuildInfoFileName() {
        return getOrsGraphRepoStrategy().getRepoGraphBuildInfoFileName();
    }

    String getRepoCompressedGraphFileName() {
        return getOrsGraphRepoStrategy().getRepoCompressedGraphFileName();
    }

    boolean shouldDownloadGraph(GraphBuildInfo newlyDownloadedGraphBuildInfo){
        return shouldDownloadGraph(
                newlyDownloadedGraphBuildInfo,
                getOrsGraphFileManager().getActiveGraphBuildInfo(),
                getOrsGraphFileManager().getDownloadedExtractedGraphBuildInfo(),
                getOrsGraphFileManager().getDownloadedCompressedGraphFile(),
                getOrsGraphFileManager().getDownloadedGraphBuildInfo(),
                getOrsGraphFileManager().getProfileDescriptiveName()
                );
    }

    public boolean shouldDownloadGraph(GraphBuildInfo newlyDownloadedGraphBuildInfo,
                                       GraphBuildInfo activeGraphBuildInfo,
                                       GraphBuildInfo downloadedExtractedGraphBuildInfo,
                                       File downloadedCompressedGraphFile,
                                       PersistedGraphBuildInfo previouslyDownloadedGraphBuildInfo,
                                       String profileDescriptiveName) {
        getLogger().trace(("[%s] Comparing dates, downloading if first is after the others:%n" +
                "                         repo=%s%n" +
                "              activeGraphBuildInfo=%s%n" +
                " downloadedExtractedGraphBuildInfo=%s%n" +
                "previouslyDownloadedGraphBuildInfo=%s").formatted(profileDescriptiveName,
                getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                getDateOrEpocStart(activeGraphBuildInfo),
                getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo)));

        boolean shouldDownload = shouldDownloadGraph(
                getDateOrEpocStart(newlyDownloadedGraphBuildInfo),
                getDateOrEpocStart(activeGraphBuildInfo),
                getDateOrEpocStart(downloadedExtractedGraphBuildInfo),
                getDateOrEpocStart(downloadedCompressedGraphFile, previouslyDownloadedGraphBuildInfo));

        if (!shouldDownload)
            getLogger().info("[%s] No newer graph found in repository.".formatted(profileDescriptiveName));

        return shouldDownload;
    }

    public boolean shouldDownloadGraph(Date remoteDate, Date activeDate, Date downloadedExtractedDate, Date downloadedCompressedDate) {
        Date newestLocalDate = newestDate(activeDate, downloadedExtractedDate, downloadedCompressedDate);
        return remoteDate.after(newestLocalDate);
    }

    public Date getDateOrEpocStart(GraphBuildInfo graphBuildInfo) {
        return Optional.ofNullable(graphBuildInfo)
                .map(GraphBuildInfo::getPersistedGraphBuildInfo)
                .map(PersistedGraphBuildInfo::getGraphBuildDate)
                .orElse(new Date(0L));
    }

    public Date getDateOrEpocStart(File persistedDownloadFile, PersistedGraphBuildInfo persistedRemoteGraphBuildInfo) {
        if (persistedDownloadFile == null) {
            return new Date(0L);
        }

        if (persistedDownloadFile.exists()) {
            return Optional.ofNullable(persistedRemoteGraphBuildInfo)
                    .map(PersistedGraphBuildInfo::getGraphBuildDate)
                    .orElse(new Date(0L));
        }

        return new Date(0L);
    }

    Date newestDate(Date... dates) {
        return Arrays.stream(dates).max(Date::compareTo).orElse(new Date(0L));
    }

    void deleteFileWithLogging(File file, String successMessage, String errorMessage) {
        try {
            if (Files.deleteIfExists(file.toPath()))
                getLogger().debug(successMessage.formatted(getProfileDescriptiveName(), file.getAbsolutePath()));
        } catch (IOException e) {
            getLogger().error(errorMessage.formatted(e.getMessage()));
        }
    }

}
