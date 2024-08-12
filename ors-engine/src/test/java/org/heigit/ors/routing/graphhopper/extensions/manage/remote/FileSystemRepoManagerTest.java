package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.defaults.DefaultProfilePropertiesWheelchair;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemRepoManagerTest {

    private static final long EARLIER_DATE = 1692373111000L; // Fr 18. Aug 17:38:31 CEST 2023
    private static final long MIDDLE_DATE = 1692373222000L;  // Fr 18. Aug 17:40:22 CEST 2023
    private static final long LATER_DATE = 1692373333000L;   // Fr 18. Aug 17:42:13 CEST 2023

    private static final long REPO_HGV_OSM_DATE       = 1706264611000L; // "2024-01-26T10:23:31+0000"
    private static final long REPO_HGV_IMPORT_DATE    = 1719397419000L; // "2024-06-26T10:23:39+0000"

    private static final String REPO_GRAPHS_REPO_NAME = "vendor-xyz";
    private static final String REPO_GRAPHS_PROFILE_GROUP = "fastisochrones";
    private static final String REPO_GRAPHS_COVERAGE = "heidelberg";
    private static final String REPO_GRAPHS_VERSION = "1";
    private static final String REPO_PROFILE_NAME = "hgv";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private Path TEMP_DIR;

    private Path testReposPath = Path.of("src/test/resources/test-filesystem-repos");
    private Path localGraphsRootPath;

    FileSystemRepoManager fileSystemRepoManager;

    @BeforeEach
    public void setUp() throws IOException {
        localGraphsRootPath = TEMP_DIR.resolve("graphs");
        Files.createDirectories(localGraphsRootPath);
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(localGraphsRootPath.toFile());
    }

    void writeORSGraphInfoToGraphPath(String profile, Date osmDate, Date importDate) throws IOException {
        ORSGraphInfoV1 localOrsGraphInfoV1Object = createOrsGraphInfoV1(profile, osmDate, importDate);
        String ymlFileName = profile + "/graph_info.yml";
        Path graphInfoFilePath = localGraphsRootPath.resolve(ymlFileName);
        Files.createDirectories(graphInfoFilePath.getParent());
        File graphInfoV1File = Files.createFile(graphInfoFilePath).toFile();
        new ObjectMapper(YAMLFactory.builder().build()).writeValue(graphInfoV1File, localOrsGraphInfoV1Object);
    }

    private FileSystemRepoManager createFileSystemRepoManager() {
        return createFileSystemRepoManager(REPO_PROFILE_NAME);
    }
    private FileSystemRepoManager createFileSystemRepoManager(String profileName) {
        EngineProperties engineProperties = RepoManagerTestHelper.createEngineProperties(localGraphsRootPath, testReposPath, null,
                REPO_GRAPHS_REPO_NAME, REPO_GRAPHS_PROFILE_GROUP, REPO_GRAPHS_COVERAGE, REPO_PROFILE_NAME, 0);
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(engineProperties, profileName, REPO_GRAPHS_VERSION);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(engineProperties, profileName, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(engineProperties, profileName, REPO_GRAPHS_VERSION);
        return new FileSystemRepoManager(engineProperties, profileName, REPO_GRAPHS_VERSION, orsGraphRepoStrategy, orsGraphFileManager);
    }

    private static ORSGraphInfoV1 createOrsGraphInfoV1(String profile, Date osmDate, Date importDate) {
        ORSGraphInfoV1 orsGraphInfoV1 = new ORSGraphInfoV1();
        orsGraphInfoV1.setOsmDate(osmDate);
        orsGraphInfoV1.setImportDate(importDate);
        ProfileProperties profileProperties = new DefaultProfilePropertiesWheelchair(true);
        orsGraphInfoV1.setProfileProperties(profileProperties);
        return orsGraphInfoV1;
    }

    @Test
    void downloadLatestGraphInfoFromRepository() {
        fileSystemRepoManager = createFileSystemRepoManager();
        fileSystemRepoManager.downloadLatestGraphInfoFromRepository();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() {
        fileSystemRepoManager = createFileSystemRepoManager();
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDataExists_noRemoteData() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager("scooter");
        writeORSGraphInfoToGraphPath("wheelchair", new Date(EARLIER_DATE), new Date(LATER_DATE));
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_wheelchair.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_wheelchair.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_wheelchair.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_wheelchair.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_before_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE - 1000000), new Date(REPO_HGV_IMPORT_DATE - 1000000));
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_equals_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE), new Date(REPO_HGV_IMPORT_DATE));
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_after_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE + 1000000), new Date(REPO_HGV_IMPORT_DATE + 1000000));
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_hgv.ghz").toFile().exists());
    }
}