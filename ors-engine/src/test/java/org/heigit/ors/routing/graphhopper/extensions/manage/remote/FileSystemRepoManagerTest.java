package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.heigit.ors.config.EngineConfig;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemRepoManagerTest {

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private Path TEMP_DIR;

    private static final long EARLIER_DATE = 1692373111000L; // Fr 18. Aug 17:38:31 CEST 2023
    private static final long MIDDLE_DATE = 1692373222000L;  // Fr 18. Aug 17:40:22 CEST 2023
    private static final long LATER_DATE = 1692373333000L;   // Fr 18. Aug 17:42:13 CEST 2023

    private static final long REPO_HGV_OSM_DATE       = 1706264611000L; // "2024-01-26T10:23:31+0000"
    private static final long REPO_HGV_IMPORT_DATE    = 1719397419000L; // "2024-06-26T10:23:39+0000"

    private static final String REPO_GRAPHS_REPO_NAME = "vendor-xyz";
    private static final String REPO_GRAPHS_PROFILE_GROUP = "fastisochrones";
    private static final String REPO_GRAPHS_COVERAGE = "heidelberg";
    private static final String REPO_GRAPHS_VERSION = "0";
    private static final String REPO_PROFILE_NAME = "hgv";

    private Path testReposPath = Path.of("src/test/resources/test-filesystem-repos");
    private Path tmpGraphPath;

    FileSystemRepoManager fileSystemRepoManager;

    @BeforeEach
    public void setUp() throws IOException {
        tmpGraphPath = TEMP_DIR.resolve("graphs");
        Files.createDirectories(tmpGraphPath);
    }

    void writeORSGraphInfoToGraphPath(String profile, Date osmDate, Date importDate) throws IOException {
        ORSGraphInfoV1 localOrsGraphInfoV1Object = createOrsGraphInfoV1(profile, osmDate, importDate);
        String ymlFileName = profile + "/graph_info.yml";
        Path graphInfoFilePath = tmpGraphPath.resolve(ymlFileName);
        File graphInfoV1File = Files.createFile(graphInfoFilePath).toFile();
        new ObjectMapper(YAMLFactory.builder().build()).writeValue(graphInfoV1File, localOrsGraphInfoV1Object);
    }

    private FileSystemRepoManager createFileSystemRepoManager() {
        return createFileSystemRepoManager(REPO_PROFILE_NAME);
    }
    private FileSystemRepoManager createFileSystemRepoManager(String profileName) {
        EngineConfig engineConfig = createEngineConfig();
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(engineConfig, profileName);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(engineConfig, profileName, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(engineConfig, profileName);
        return new FileSystemRepoManager(engineConfig, profileName, orsGraphRepoStrategy, orsGraphFileManager);
    }

    private EngineConfig createEngineConfig() {
        String graphsRepoPath = testReposPath.toFile().getAbsolutePath();
        return EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(null)
                .setGraphsRepoPath(graphsRepoPath)
                .setGraphsRepoName(REPO_GRAPHS_REPO_NAME)
                .setGraphsProfileGroup(REPO_GRAPHS_PROFILE_GROUP)
                .setGraphsExtent(REPO_GRAPHS_COVERAGE)
                .setGraphVersion(REPO_GRAPHS_VERSION)
                .setGraphsRootPath(tmpGraphPath.toFile().getAbsolutePath())
                .build();
    }

    private static ORSGraphInfoV1 createOrsGraphInfoV1(String profile, Date osmDate, Date importDate) {
        ORSGraphInfoV1 orsGraphInfoV1 = new ORSGraphInfoV1();
        orsGraphInfoV1.setOsmDate(osmDate);
        orsGraphInfoV1.setImportDate(importDate);
        orsGraphInfoV1.setProfileProperties(new ORSGraphInfoV1ProfileProperties(
                profile, true, true, true, true, true, true, true,
                "graphPath",
                Map.of("k1", "v1", "k2", "v2"),
                Map.of("k1", "v1", "k2", "v2"),
                Map.of("k1", "v1", "k2", "v2"),
                Map.of("k1", Map.of("k1", "v1", "k2", "v2"), "k2", Map.of("k1", "v1", "k2", "v2")),
                1d, 2d, 3d, 4d, 5d, 6d,
                1, 2, 3, 4, 5, 6,
                true, "gtfsFile"
        ));
        return orsGraphInfoV1;
    }

    @Test
    void downloadLatestGraphInfoFromRepository() {
        fileSystemRepoManager = createFileSystemRepoManager();
        fileSystemRepoManager.downloadLatestGraphInfoFromRepository();
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() {
        fileSystemRepoManager = createFileSystemRepoManager();
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDataExists_noRemoteData() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager("scooter");
        writeORSGraphInfoToGraphPath("scooter", new Date(EARLIER_DATE), new Date(LATER_DATE));
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_scooter.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_scooter.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_scooter.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_scooter.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_before_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE - 1000000), new Date(REPO_HGV_IMPORT_DATE - 1000000));
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_equals_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE), new Date(REPO_HGV_IMPORT_DATE));
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_after_remoteDate() throws IOException {
        fileSystemRepoManager = createFileSystemRepoManager();
        writeORSGraphInfoToGraphPath(REPO_PROFILE_NAME, new Date(REPO_HGV_OSM_DATE + 1000000), new Date(REPO_HGV_IMPORT_DATE + 1000000));
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
        fileSystemRepoManager.downloadGraphIfNecessary();
        assertTrue(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.yml").toFile().exists());
        assertFalse(tmpGraphPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_hgv.ghz").toFile().exists());
    }
}