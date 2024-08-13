package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

abstract class AbstractRepoManager {

    public boolean shouldDownloadGraph(Date remoteDate, Date activeDate, Date downloadedExtractedDate, Date downloadedCompressedDate) {
        Date newestLocalDate = newestDate(activeDate, downloadedExtractedDate, downloadedCompressedDate);
        return remoteDate.after(newestLocalDate);
    }

    public Date getDateOrEpocStart(GraphInfo graphInfo) {
        return Optional.ofNullable(graphInfo)
                .map(GraphInfo::getPersistedGraphInfo)
                .map(ORSGraphInfoV1::getImportDate)
                .orElse(new Date(0L));
    }

    public Date getDateOrEpocStart(File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo) {
        if (persistedDownloadFile==null) {
            return new Date(0L);
        }

        if (persistedDownloadFile.exists()) {
            return Optional.ofNullable(persistedRemoteGraphInfo)
                    .map(ORSGraphInfoV1::getImportDate)
                    .orElse(new Date(0L));
        }

        return new Date(0L);
    }

    Date newestDate(Date... dates) {
        return Arrays.stream(dates).max(Date::compareTo).orElse(new Date(0L));
    }

}
