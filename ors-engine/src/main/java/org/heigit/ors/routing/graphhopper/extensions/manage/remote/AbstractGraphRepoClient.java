package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

abstract class AbstractGraphRepoClient {

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

}
