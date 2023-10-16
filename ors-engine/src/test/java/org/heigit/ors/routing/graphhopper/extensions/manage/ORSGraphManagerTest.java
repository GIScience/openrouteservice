package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ORSGraphManagerTest {

    @Spy
    ORSGraphManager orsGraphManager;

    private static final String GRAPHS_REPO_BASE_URL = "https://example.com";
    private static final String GRAPHS_REPO_NAME = "test-repo";
    private static final String GRAPHS_COVERAGE = "planet";
    private static final String GRAPHS_VERSION = "1";
    private static final String VEHICLE = "car";
    private static final String LOCAL_PATH = "src/test/resources/graphs";
    private static final long EARLIER_DATE = 1692373000111L;
    private static final long MIDDLE_DATE = 1692373000222L;
    private static final long LATER_DATE = 1692373000333L;

    String vehicleDirAbsPath, hashDirAbsPath;
    File localDir, vehicleDir, hashDir, downloadedGraphInfoV1File, localGraphInfoV1File;


    @BeforeEach
    void setUp() {
        localDir = new File(LOCAL_PATH);
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        vehicleDir = new File(vehicleDirAbsPath);
        vehicleDir.mkdir();
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(vehicleDir);
    }

    void setupORSGraphManager(String hash) {
        File localDir = new File(LOCAL_PATH);
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);

        orsGraphManager.setGraphsRepoBaseUrl(GRAPHS_REPO_BASE_URL);
        orsGraphManager.setGraphsRepoName(GRAPHS_REPO_NAME);
        orsGraphManager.setGraphsRepoCoverage(GRAPHS_COVERAGE);
        orsGraphManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphManager.setRouteProfileName(VEHICLE);
        orsGraphManager.setHash(hash);
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);
        orsGraphManager.setVehicleGraphDirAbsPath(vehicleDirAbsPath);
        orsGraphManager.setHashDirAbsPath(hashDirAbsPath);
        orsGraphManager.initialize();
    }

    @Test
    public void testSomething() {

    }

}