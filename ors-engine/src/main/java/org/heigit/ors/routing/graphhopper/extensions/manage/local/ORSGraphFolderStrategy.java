package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import java.io.File;

public interface ORSGraphFolderStrategy {

    String GRAPH_DOWNLOAD_FILE_EXTENSION = "ghz";
    String GRAPH_BUILD_INFO_FILE_EXTENSION = "yml";
    String GRAPH_EXTRACTION_DIRECTORY_EXTENSION = "new";
    String INCOMPLETE_EXTENSION = "incomplete";

    String getProfileDescriptiveName();

    String getGraphBuildInfoFileNameInRepository();

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
     * including its ActiveGraphBuildInfoFile.
     */
    String getActiveGraphDirName();

    String getActiveGraphDirAbsPath();

    default File getActiveGraphDirectory() {
        return new File(getActiveGraphDirAbsPath());
    }

    String getActiveGraphBuildInfoFileName();

    default File getActiveGraphBuildInfoFile() {
        return new File(getActiveGraphDirectory(), getActiveGraphBuildInfoFileName());
    }

    /**
     * DownloadedGraphBuildInfoFile is the file with info about the latest graph at the repository.
     */
    String getDownloadedGraphBuildInfoFileName();

    String getDownloadedGraphBuildInfoFileAbsPath();

    default File getDownloadedGraphBuildInfoFile() {
        return new File(getDownloadedGraphBuildInfoFileAbsPath());
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

    default String getDownloadedExtractedGraphBuildInfoFileName() {
        return getActiveGraphBuildInfoFileName();
    }

    default File getDownloadedExtractedGraphBuildInfoFile() {
        return new File(getDownloadedExtractedGraphDirectory(), getDownloadedExtractedGraphBuildInfoFileName());
    }
}
