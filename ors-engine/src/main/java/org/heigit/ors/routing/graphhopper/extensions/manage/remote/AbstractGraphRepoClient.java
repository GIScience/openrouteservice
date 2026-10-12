package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGraphRepoClient implements ORSGraphRepoClient {

    private final ORSGraphFileManager orsGraphFileManager;
    private final ORSGraphRepoStrategy orsGraphRepoStrategy;
    private final GraphManagementRuntimeProperties managementProps;
    private final Logger logger;

    public abstract boolean hasValidRepoConfig();
    abstract GraphBuildInfo downloadGraphBuildInfoFromRepository();
    abstract void downloadCompressedGraphFromRepository();

    AbstractGraphRepoClient(GraphManagementRuntimeProperties managementProps,
                            ORSGraphRepoStrategy repoStrategy,
                            ORSGraphFileManager graphFileManager ) {
        this.managementProps = managementProps;
        this.orsGraphRepoStrategy = repoStrategy;
        this.orsGraphFileManager = graphFileManager;
        this.logger = Logger.getLogger(getClass().getName());
    }

    ORSGraphFileManager getGraphFileManager() {
        return this.orsGraphFileManager;
    }

    ORSGraphRepoStrategy getGraphRepoStrategy() {
        return this.orsGraphRepoStrategy;
    }

    GraphManagementRuntimeProperties getManagementProps() {
        return this.managementProps;
    }

    Logger getLogger() {
        return this.logger;
    }

    public void downloadGraphIfNecessary() {
        if (! hasValidRepoConfig()) {
            getLogger().debug("[%s] ORSGraphManager has no valid repo config - skipping check"
                    .formatted(getGraphFileManager().getProfileDescriptiveName()));
            return;
        }
        if (getGraphFileManager().isBusy()) {
            getLogger().debug("[%s] ORSGraphManager is busy - skipping check"
                    .formatted(getGraphFileManager().getProfileDescriptiveName()));
            return;
        }

        //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
        getLogger().debug("[%s] Checking for possible graph update from remote repository..."
                .formatted(getGraphFileManager().getProfileDescriptiveName()));
        try {
            GraphBuildInfo newlyDownloadedGraphBuildInfo = downloadGraphBuildInfoFromRepository();

            if (!shouldDownloadGraph(newlyDownloadedGraphBuildInfo)) {
                return;
            }

            long start = System.currentTimeMillis();
            downloadCompressedGraphFromRepository();
            long end = System.currentTimeMillis();

            if (getGraphFileManager().getDownloadedCompressedGraphFile().exists()) {
                //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
                getLogger().info("[%s] Download of compressed graph file finished after %d ms"
                        .formatted(getGraphFileManager().getProfileDescriptiveName(), end - start));
            } else {
                getLogger().info("[%s] Compressed graph file not found in remote repository."
                        .formatted(getGraphFileManager().getProfileDescriptiveName()));
            }
        } catch (Exception exception) {
            getLogger().error("[%s] Caught an exception during graph download check or graph download:"
                    .formatted(getGraphFileManager().getProfileDescriptiveName()), exception);
        }
    }

    String getRepoPath() {
        return getManagementProps().getDerivedRepoPath().toAbsolutePath().toString();
    }

    boolean shouldDownloadGraph(GraphBuildInfo newlyDownloadedGraphBuildInfo){
        return shouldDownloadGraph(
                newlyDownloadedGraphBuildInfo,
                getGraphFileManager().getActiveGraphBuildInfo(),
                getGraphFileManager().getDownloadedExtractedGraphBuildInfo(),
                getGraphFileManager().getDownloadedCompressedGraphFile(),
                getGraphFileManager().getDownloadedGraphBuildInfo(),
                getGraphFileManager().getProfileDescriptiveName()
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

        if (!shouldDownload) {
            //Log message is asserted in GraphRepoTest/TestContainersHelper - change with care!
            getLogger().info("[%s] No newer graph found in repository.".formatted(profileDescriptiveName));
        }
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
                getLogger().debug(successMessage.formatted(
                        getGraphFileManager().getProfileDescriptiveName(),
                        file.getAbsolutePath())
                );
        } catch (IOException e) {
            getLogger().error(errorMessage.formatted(e.getMessage()));
        }
    }

    public String concatenateToUrlPath(String... values) {
        return Stream.of(values)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(s -> s.replaceAll("^/", ""))
                .map(s -> s.replaceAll("/$", ""))
                .filter(s -> !s.equals("."))
                .collect(Collectors.joining("/"));
    }

}
