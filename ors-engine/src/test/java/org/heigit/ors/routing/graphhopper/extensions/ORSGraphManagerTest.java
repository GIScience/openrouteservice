package org.heigit.ors.routing.graphhopper.extensions;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ORSGraphManagerTest {

    @Spy
    ORSGraphManager orsGraphManager;

    private static final String GRAPHS_REPO_BASE_URL = "https://example.com/";
    private static final String GRAPHS_REPO_NAME = "test-repo";
    private static final String VEHICLE = "car";
    private static final String LOCAL_PATH = "src/test/resources/graphs";
    private static final long EARLIER_DATE = 1692373000111L;
    private static final long LATER_DATE = 1692373000222L;

    String vehicleDirAbsPath, hashDirAbsPath;
    File hashDir, downloadedGraphInfoV1File, localGraphInfoV1File;

    @BeforeEach
    void setUp() {
        File localDir = new File(LOCAL_PATH);
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        new File(vehicleDirAbsPath).mkdir();
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(new File(vehicleDirAbsPath));
    }

    void setupORSGraphManager(String hash) {
        File localDir = new File(LOCAL_PATH);
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        orsGraphManager.setGraphsRepoBaseUrl(GRAPHS_REPO_BASE_URL);
        orsGraphManager.setGraphsRepoName(GRAPHS_REPO_NAME);
        orsGraphManager.setRouteProfileName(VEHICLE);
        orsGraphManager.setHash(hash);
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);
        orsGraphManager.setVehicleGraphDirAbsPath(vehicleDirAbsPath);
        orsGraphManager.setHashDirAbsPath(hashDirAbsPath);
    }

    void setupLocalFiles(String hash, Long osmDateLocal) throws IOException {
        if (hash == null) return;
        hashDir = new File(hashDirAbsPath);
        hashDir.mkdir();
        ORSGraphManager.ORSGraphInfoV1 localOrsGraphInfoV1Object = new ORSGraphManager.ORSGraphInfoV1(new Date(osmDateLocal));
        localGraphInfoV1File = new File(hashDir, hash + ".json");
        new ObjectMapper().writeValue(localGraphInfoV1File, localOrsGraphInfoV1Object);
    }

    void setupNoRemoteFiles() {
        doReturn(null).when(orsGraphManager).findLatestGraphInfoAsset(anyString());
    }

    void setupRemoteFiles(String hash, Long osmDateRemote) throws IOException {
        String graphInfoAssetName = hash + ".json";
        String graphInfoAssetUrl = String.join("/", GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, VEHICLE, graphInfoAssetName);

        ORSGraphManager.ORSGraphInfoV1 downloadedOrsGraphInfoV1Object = new ORSGraphManager.ORSGraphInfoV1(new Date(osmDateRemote));
        downloadedGraphInfoV1File = new File(vehicleDirAbsPath + "/" + graphInfoAssetName);
        new ObjectMapper().writeValue(downloadedGraphInfoV1File, downloadedOrsGraphInfoV1Object);

        AssetXO assetXO = new AssetXO();
        assetXO.setDownloadUrl(graphInfoAssetUrl);

        doReturn(assetXO).when(orsGraphManager).findLatestGraphInfoAsset(graphInfoAssetName);
        lenient().doNothing().when(orsGraphManager).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDataExists_noRemoteData() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, EARLIER_DATE);
        setupNoRemoteFiles();

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, never()).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        setupRemoteFiles(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(2)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate1_remoteDate2() throws IOException {
        String hash = "xyz111";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, EARLIER_DATE);
        setupRemoteFiles(hash, LATER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(2)).downloadAsset(anyString(), any());
        File localGraphDir = new File(hashDirAbsPath);
        File backupDir = new File(hashDirAbsPath + "_bak");
        assertFalse(localGraphDir.exists());
        assertTrue(backupDir.exists());
        assertTrue(backupDir.isDirectory());
    }

    @Test
    void downloadGraphIfNecessary_localDate1_remoteDate1() throws IOException {
        String hash = "xyz222";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, EARLIER_DATE);
        setupRemoteFiles(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate2_remoteDate1() throws IOException {
        String hash = "xyz333";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, LATER_DATE);
        setupRemoteFiles(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void backupExistingGraph_noPreviousBackup() throws IOException {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, LATER_DATE);

        File localGraphDir = new File(hashDirAbsPath);
        assertTrue(localGraphDir.isDirectory());
        File backupDir = new File(hashDirAbsPath + "_bak");
        assertFalse(backupDir.exists());

        orsGraphManager.backupExistingGraph(localGraphDir);

        assertFalse(localGraphDir.exists());
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, hash+".json").exists());
    }

    @Test
    void backupExistingGraph_previousBackupDirIsOverridden() throws IOException {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);
        setupLocalFiles(hash, LATER_DATE);

        File localGraphDir = new File(hashDirAbsPath);
        assertTrue(localGraphDir.isDirectory());
        File backupDir = new File(hashDirAbsPath + "_bak");
        backupDir.mkdir();
        assertTrue(backupDir.exists());

        orsGraphManager.backupExistingGraph(localGraphDir);

        assertFalse(localGraphDir.exists());
        assertTrue(backupDir.exists());
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, hash+".json").exists());
    }

    @Test
    void findLatestGraphInfoInRepository() {
    }

    @Test
    void shouldDownloadGraph() {
    }

    @Test
    void findLatestGraphComponent() {
    }
}