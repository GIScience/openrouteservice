package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemGraphRepoClientTest {

    private static final String PROFILE_NAME = "truck";
    private static final String ENCODER_NAME = "driving-hgv";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private Path tempDir;

    private Path localGraphsRootPath;
    private FileSystemGraphRepoClient fileSystemGraphRepoClient;
    private ORSGraphFileManager orsGraphFileManager;

    @BeforeEach
    public void setUp() throws IOException {
        localGraphsRootPath = createLocalGraphsRootDirectory(tempDir);
    }

    @AfterEach
    void deleteFiles() throws IOException {
        cleanupLocalGraphsRootDirectory(localGraphsRootPath);
    }

    private void setupORSGraphManager(GraphManagementRuntimeProperties managementProps) {
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        fileSystemGraphRepoClient = new FileSystemGraphRepoClient(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
    }

    private GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, PROFILE_NAME, ENCODER_NAME);
    }

    @Test
    void downloadLatestGraphBuildInfoFromRepository() throws ORSGraphFileManagerException {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        fileSystemGraphRepoClient.downloadLatestGraphBuildInfoFromRepository();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
        fileSystemGraphRepoClient.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDataExists_noRemoteData() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_NONEXISTING_GRAPHS_VERSION).build());
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), LATER_DATE, EARLIER_DATE);
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_driving-hgv.ghz").toFile().exists());
        fileSystemGraphRepoClient.downloadGraphIfNecessary();
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_0_driving-hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_before_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), REPO_HGV_OSM_DATE - 1000000, REPO_HGV_GRAPH_BUILD_DATE - 1000000);
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
        fileSystemGraphRepoClient.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_equals_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), REPO_HGV_GRAPH_BUILD_DATE, REPO_HGV_OSM_DATE);
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
        fileSystemGraphRepoClient.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate_after_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), REPO_HGV_GRAPH_BUILD_DATE + 1000000, REPO_HGV_OSM_DATE + 1000000);
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
        fileSystemGraphRepoClient.downloadGraphIfNecessary();
        assertTrue(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.yml").toFile().exists());
        assertFalse(localGraphsRootPath.resolve("vendor-xyz_fastisochrones_heidelberg_1_driving-hgv.ghz").toFile().exists());
    }
}