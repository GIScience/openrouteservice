package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.EngineConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class ORSGraphManagerTest {

    @Spy
    ORSGraphManager orsGraphManager;

    private static final String GRAPHS_REPO_BASE_URL = "https://example.com";
    private static final String GRAPHS_REPO_NAME = "test-repo";
    private static final String GRAPHS_COVERAGE = "planet";
    private static final String GRAPHS_VERSION = "1";
    private static final String VEHICLE = "car";
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private static Path TEMP_DIR;
    private static final long EARLIER_DATE = 1692373000111L;
    private static final long MIDDLE_DATE = 1692373000222L;
    private static final long LATER_DATE = 1692373000333L;

    String vehicleDirAbsPath, hashDirAbsPath;
    File localDir, vehicleDir, hashDir, downloadedGraphInfoV1File, localGraphInfoV1File;


    @BeforeEach
    void setUp() {
        localDir = TEMP_DIR.toFile();
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        vehicleDir = new File(vehicleDirAbsPath);
        vehicleDir.mkdir();
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(vehicleDir);
    }

    void setupORSGraphManager(String hash) {
        File localDir = TEMP_DIR.toFile();
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);

        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .buildWithAppConfigOverride();

        orsGraphManager = new ORSGraphManager();
        orsGraphManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphManager.setRouteProfileName(VEHICLE);
        orsGraphManager.setHash(hash);
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);
        orsGraphManager.setVehicleGraphDirAbsPath(vehicleDirAbsPath);
        orsGraphManager.setHashDirAbsPath(hashDirAbsPath);
        orsGraphManager.initialize(engineConfig);
    }

    @Test
    public void testSomething() {

    }

}