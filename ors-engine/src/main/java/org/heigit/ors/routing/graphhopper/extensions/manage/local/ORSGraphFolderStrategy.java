package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import java.io.File;

public interface ORSGraphFolderStrategy {

    String GRAPH_DOWNLOAD_FILE_EXTENSION = "ghz";
    String GRAPH_INFO_FILE_EXTENSION = "yml";
    String GRAPH_EXTRACTION_DIRECTORY_EXTENSION = "new";
    String INCOMPLETE_EXTENSION = "incomplete";

    String getProfileDescriptiveName();

    String getGraphInfoFileNameInRepository();

    /**
     * GraphsRootDir is the root directory for all routing profiles.
     */
    String getGraphsRootDirName();

    String getGraphsRootDirAbsPath();

    default File getGraphsRootDirectory() {
        return new File(getGraphsRootDirAbsPath());
    }

    /**
     * ProfileGraphsDir ist the root directory for all graphs
     * and files of the current routing profile.
     */
    String getProfileGraphsDirName();

    String getProfileGraphsDirAbsPath();

    default File getProfileGraphsDirectory() {
        return new File(getProfileGraphsDirAbsPath());
    }

    /**
     * ActiveGraphDir is the directory with the active graph
     * including its ActiveGraphInfoFile.
     */
    String getActiveGraphDirName();

    String getActiveGraphDirAbsPath();

    default File getActiveGraphDirectory() {
        return new File(getActiveGraphDirAbsPath());
    }

    String getActiveGraphInfoFileName();

    default File getActiveGraphInfoFile() {
        return new File(getActiveGraphDirectory(), getActiveGraphInfoFileName());
    }

    /**
     * DownloadedGraphInfoFile is the file with info about the latest graph at the repository.
     */
    String getDownloadedGraphInfoFileName();

    String getDownloadedGraphInfoFileAbsPath();

    default File getDownloadedGraphInfoFile() {
        return new File(getDownloadedGraphInfoFileAbsPath());
    }

    /**
     * DownloadedCompressedGraphFile is the compressed graph downloaded from the repository.
     * It will be extracted to DownloadedExtractedGraphDir.
     */
    String getDownloadedCompressedGraphFileName();

    String getDownloadedCompressedGraphFileAbsPath();

    default File getDownloadedCompressedGraphFile() {
        return new File(getDownloadedCompressedGraphFileAbsPath());
    }

    /**
     * DownloadedExtractedGraphDir is the extracted downloaded graph
     * including its DownloadedExtractedGrapnInfoFile.
     * It will be activated by moving to ActiveGraphDir.
     */
    String getDownloadedExtractedGraphDirName();

    String getDownloadedExtractedGraphDirAbsPath();

    default File getDownloadedExtractedGraphDirectory() {
        return new File(getDownloadedExtractedGraphDirAbsPath());
    }

    default String getDownloadedExtractedGraphInfoFileName() {
        return getActiveGraphInfoFileName();
    }

    default File getDownloadedExtractedGraphInfoFile() {
        return new File(getDownloadedExtractedGraphDirectory(), getDownloadedExtractedGraphInfoFileName());
    }
}
