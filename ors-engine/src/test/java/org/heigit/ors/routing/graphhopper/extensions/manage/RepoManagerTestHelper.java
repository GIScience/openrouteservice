package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/*
 * Helper class for testing against the files in ors-engine/src/test/resources/test-filesystem-repos
 * reflecting a graph repository with content for car and hgv profiles.
 */
public class RepoManagerTestHelper {

    public static final String TESTREPO_PATH = "src/test/resources/test-filesystem-repos";
    public static final String REPO_GRAPHS_REPO_NAME = "vendor-xyz";
    public static final String REPO_GRAPHS_PROFILE_GROUP = "fastisochrones";
    public static final String REPO_GRAPHS_COVERAGE = "heidelberg";
    public static final String REPO_GRAPHS_VERSION = "1";
    public static final String REPO_NONEXISTING_GRAPHS_VERSION = "0";

    public static final long EARLIER_DATE = 1692373111000L; // Fr 18. Aug 17:38:31 CEST 2023
    public static final long MIDDLE_DATE = 1692373222000L;  // Fr 18. Aug 17:40:22 CEST 2023
    public static final long LATER_DATE = 1692373333000L;   // Fr 18. Aug 17:42:13 CEST 2023

    public static final long REPO_CAR_OSM_DATE;
    public static final long REPO_HGV_OSM_DATE;
    public static final long REPO_CAR_GRAPH_BUILD_DATE;
    public static final long REPO_HGV_GRAPH_BUILD_DATE;

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        REPO_CAR_OSM_DATE = ZonedDateTime.parse("2024-06-26T10:23:31+0000", formatter).toInstant().toEpochMilli();
        REPO_HGV_OSM_DATE = ZonedDateTime.parse("2024-01-26T10:23:31+0000", formatter).toInstant().toEpochMilli();
        REPO_CAR_GRAPH_BUILD_DATE = ZonedDateTime.parse("2024-06-26T10:23:31+0000", formatter).toInstant().toEpochMilli();
        REPO_HGV_GRAPH_BUILD_DATE = ZonedDateTime.parse("2024-06-26T10:23:39+0000", formatter).toInstant().toEpochMilli();
    }

    public static Path createLocalGraphsRootDirectory(Path tempDir) throws IOException {
        Path localGraphsRootPath = tempDir.resolve("graphs");
        Files.createDirectories(localGraphsRootPath);
        return localGraphsRootPath;
    }

    public static Path createLocalGraphDirectory(Path localGraphsRootPath, String name) throws IOException {
        Path localGraphDir = localGraphsRootPath.resolve(name);
        Files.createDirectories(localGraphDir);
        return localGraphDir;
    }

    public static Path createLocalGraphDirectoryWithGraphBuildInfoFile(Path localGraphsRootPath, String name, String infoFileName, Long importDate, Long osmDate) throws IOException {
        Path localGraphDir = createLocalGraphDirectory(localGraphsRootPath, name);
        saveActiveGraphBuildInfoFile(
                localGraphDir.resolve(infoFileName).toFile(),
                Optional.ofNullable(importDate).orElse(LATER_DATE),
                Optional.ofNullable(osmDate).orElse(EARLIER_DATE));
        return localGraphDir;
    }

    public static void saveActiveGraphBuildInfoFile(File activeGraphBuildInfoFile, Long graphBuildDate, Long osmDate) {
        PersistedGraphBuildInfo activeGraphBuildInfoObject = new PersistedGraphBuildInfo();
        if (graphBuildDate != null) activeGraphBuildInfoObject.setGraphBuildDate(new Date(graphBuildDate));
        if (osmDate != null) activeGraphBuildInfoObject.setOsmDate(new Date(osmDate));
        activeGraphBuildInfoObject.setProfileProperties(new ProfileProperties());
        ORSGraphFileManager.writeOrsGraphBuildInfo(activeGraphBuildInfoObject, activeGraphBuildInfoFile);
    }

    public static void cleanupLocalGraphsRootDirectory(Path localGraphsRootPath) throws IOException {
        FileUtils.deleteDirectory(localGraphsRootPath.toFile());
    }

    public static GraphManagementRuntimeProperties.Builder createGraphManagementRuntimePropertiesBuilder(
            Path localGraphsRootPath,
            String profileName,
            String encoderName
    ) {
        return GraphManagementRuntimeProperties.Builder.empty()
                .withLocalGraphsRootAbsPath(localGraphsRootPath.toString())
                .withLocalProfileName(profileName)
                .withEncoderName(encoderName)
                .withRepoBaseUri(TESTREPO_PATH)
                .withRepoName(REPO_GRAPHS_REPO_NAME)
                .withRepoProfileGroup(REPO_GRAPHS_PROFILE_GROUP)
                .withRepoCoverage(REPO_GRAPHS_COVERAGE)
                .withGraphVersion(REPO_GRAPHS_VERSION)
                ;
    }
}
