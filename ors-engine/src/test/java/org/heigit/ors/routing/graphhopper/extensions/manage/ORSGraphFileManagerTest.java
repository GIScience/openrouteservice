package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ORSGraphFileManagerTest {

    @Spy
    ORSGraphRepoManager orsGraphRepoManager;
    ORSGraphFileManager orsGraphFileManager;
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
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);

        orsGraphFileManager = new ORSGraphFileManager(hash, hashDirAbsPath, vehicleDirAbsPath, VEHICLE);
//        orsGraphFileManager.setVehicleGraphDirAbsPath(vehicleDirAbsPath);
//        orsGraphFileManager.setHashDirAbsPath(hashDirAbsPath);

        orsGraphRepoManager.setGraphsRepoBaseUrl(GRAPHS_REPO_BASE_URL);
        orsGraphRepoManager.setGraphsRepoName(GRAPHS_REPO_NAME);
        orsGraphRepoManager.setGraphsRepoCoverage(GRAPHS_COVERAGE);
        orsGraphRepoManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphRepoManager.setRouteProfileName(VEHICLE);
        orsGraphRepoManager.setFileManager(orsGraphFileManager);
    }

    void setupLocalGraphDirectory(String hash, Long osmDateLocal) throws IOException {
        if (hash == null) return;
        hashDir = new File(hashDirAbsPath);
        hashDir.mkdir();
        ORSGraphInfoV1 localOrsGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateLocal));
        localGraphInfoV1File = new File(hashDir, hash + ".json");
        new ObjectMapper().writeValue(localGraphInfoV1File, localOrsGraphInfoV1Object);
    }

    void setupNoRemoteFiles() {
        doReturn(null).when(orsGraphRepoManager).findLatestGraphInfoAsset(anyString());
    }

    void simulateFindLatestGraphInfoAsset(String hash, Long osmDateRemote) throws IOException {
        String graphInfoAssetName = hash + ".json";
        String graphInfoAssetUrl = String.join("/", GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, VEHICLE, graphInfoAssetName);

        ORSGraphInfoV1 downloadedOrsGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateRemote));
        downloadedGraphInfoV1File = new File(vehicleDirAbsPath + "/" + graphInfoAssetName);
        new ObjectMapper().writeValue(downloadedGraphInfoV1File, downloadedOrsGraphInfoV1Object);

        AssetXO assetXO = new AssetXO();
        assetXO.setDownloadUrl(graphInfoAssetUrl);

        doReturn(assetXO).when(orsGraphRepoManager).findLatestGraphInfoAsset(graphInfoAssetName);
        lenient().doNothing().when(orsGraphRepoManager).downloadAsset(anyString(), any());
    }

    @Test
    void backupExistingGraph_noPreviousBackup() throws IOException {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, LATER_DATE);

        File localGraphDir = new File(hashDirAbsPath);
        assertTrue(localGraphDir.isDirectory());
        File backupDir = new File(hashDirAbsPath + "_bak");
        assertFalse(backupDir.exists());

        orsGraphFileManager.backupExistingGraph(localGraphDir);

        assertFalse(localGraphDir.exists());
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, hash+".json").exists());
    }

    @Test
    void backupExistingGraph_previousBackupDirIsOverridden() throws IOException {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, LATER_DATE);

        File localGraphDir = new File(hashDirAbsPath);
        assertTrue(localGraphDir.isDirectory());
        File backupDir = new File(hashDirAbsPath + "_bak");
        backupDir.mkdir();
        assertTrue(backupDir.exists());

        orsGraphFileManager.backupExistingGraph(localGraphDir);

        assertFalse(localGraphDir.exists());
        assertTrue(backupDir.exists());
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, hash+".json").exists());
    }

}