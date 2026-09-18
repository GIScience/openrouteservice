package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

abstract class AbstractGraphRepoClient {

    abstract ORSGraphFileManager getOrsGraphFileManager();
    abstract ORSGraphRepoStrategy getOrsGraphRepoStrategy();
    abstract GraphManagementRuntimeProperties getGraphManagementRuntimeProperties();
    abstract Logger getLogger();

    String getGraphVersion() {
        return getGraphManagementRuntimeProperties().getGraphVersion();
    }

    String getRepoCoverage() {
        return getGraphManagementRuntimeProperties().getRepoCoverage();
    }

    String getRepoProfileGroup() {
        return getGraphManagementRuntimeProperties().getRepoProfileGroup();
    }

    String getRepoName() {
        return getGraphManagementRuntimeProperties().getRepoName();
    }

    String getRepoPath() {
        return getGraphManagementRuntimeProperties().getDerivedRepoPath().toAbsolutePath().toString();
    }

    String getProfileDescriptiveName() {
        return getOrsGraphFileManager().getProfileDescriptiveName();
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
