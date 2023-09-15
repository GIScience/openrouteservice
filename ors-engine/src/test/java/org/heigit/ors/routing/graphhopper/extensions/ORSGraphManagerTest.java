package org.heigit.ors.routing.graphhopper.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    }

    void setupLocalGraphDirectory(String hash, Long osmDateLocal) throws IOException {
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

    void simulateFindLatestGraphInfoAsset(String hash, Long osmDateRemote) throws IOException {
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
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        setupNoRemoteFiles();

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, never()).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(2)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate1_remoteDate2() throws IOException {
        String hash = "xyz111";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, LATER_DATE);

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
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate2_remoteDate1() throws IOException {
        String hash = "xyz333";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, LATER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphManager.downloadGraphIfNecessary();

        verify(orsGraphManager, times(1)).downloadAsset(anyString(), any());
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

        orsGraphManager.backupExistingGraph(localGraphDir);

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
    void filterLatestAsset() {
        String hash = "abc123";
        setupORSGraphManager(hash);
        List<AssetXO> items = Arrays.asList(
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202201011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202201011200/abc123.json"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202301011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202301011200/abc123.json"),//this one
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202301011200/wrong.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/abc123/202301011200/wrong.json"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/wrong/202301011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/car/wrong/202301011200/abc123.json"),
                new AssetXO().path("https://example.com/test-repo/planet/1/wrong/abc123/202301011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/1/wrong/abc123/202301011200/abc123.json"),
                new AssetXO().path("https://example.com/test-repo/planet/wrong/car/abc123/202301011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/planet/wrong/car/abc123/202301011200/abc123.json"),
                new AssetXO().path("https://example.com/test-repo/wrong/1/car/abc123/202301011200/abc123.ghz"),
                new AssetXO().path("https://example.com/test-repo/wrong/1/car/abc123/202301011200/abc123.json")
                );
        AssetXO filtered = orsGraphManager.filterLatestAsset("abc123.json", items);
        assertEquals("https://example.com/test-repo/planet/1/car/abc123/202301011200/abc123.json", filtered.getPath());
    }

    @ParameterizedTest
    @MethodSource("shouldDownloadGraphMethodSource")
    void shouldDownloadGraph(ORSGraphManager.GraphInfo remoteGraphInfo, ORSGraphManager.GraphInfo localGraphInfo, File persistedDownloadFile, ORSGraphManager.ORSGraphInfoV1 persistedRemoteGraphInfo, boolean expected) {
        assertEquals(expected, orsGraphManager.shouldDownloadGraph(remoteGraphInfo, localGraphInfo, persistedDownloadFile, persistedRemoteGraphInfo));
    }

    public static Stream<Arguments> shouldDownloadGraphMethodSource() {
        ORSGraphManager.ORSGraphInfoV1 earlierOrsGraphInfoV1 = new ORSGraphManager.ORSGraphInfoV1(new Date(EARLIER_DATE));
        ORSGraphManager.ORSGraphInfoV1 middleOrsGraphInfoV1 = new ORSGraphManager.ORSGraphInfoV1(new Date(MIDDLE_DATE));
        ORSGraphManager.ORSGraphInfoV1 laterOrsGraphInfoV1 = new ORSGraphManager.ORSGraphInfoV1(new Date(LATER_DATE));
        ORSGraphManager.GraphInfo missingGraphInfo = new ORSGraphManager.GraphInfo();
        ORSGraphManager.GraphInfo existingGraphInfoEarlier = new ORSGraphManager.GraphInfo().withPersistedInfo(earlierOrsGraphInfoV1);
        ORSGraphManager.GraphInfo existingGraphInfoMiddle = new ORSGraphManager.GraphInfo().withPersistedInfo(middleOrsGraphInfoV1);
        ORSGraphManager.GraphInfo existingGraphInfoLater = new ORSGraphManager.GraphInfo().withPersistedInfo(laterOrsGraphInfoV1);

        File resourcesDir = new File(LOCAL_PATH).getParentFile();
        File nonexistingFile = new File(resourcesDir, "missing.ghz");
        File existingFile = new File(resourcesDir, "some.ghz");

        return Stream.of(
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, existingFile, earlierOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, existingFile, null, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, nonexistingFile, earlierOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, nonexistingFile, laterOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, nonexistingFile, middleOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoEarlier, nonexistingFile, null, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, existingFile, earlierOrsGraphInfoV1, true),    //unlikely to happen
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, existingFile, null, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, nonexistingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, nonexistingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, nonexistingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoLater, nonexistingFile, null, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, existingFile, earlierOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, existingFile, null, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, nonexistingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, nonexistingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, nonexistingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, existingGraphInfoMiddle, nonexistingFile, null, false),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, existingFile, earlierOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, existingFile, null, true),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, nonexistingFile, earlierOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, nonexistingFile, laterOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, nonexistingFile, middleOrsGraphInfoV1, true),
                Arguments.of(existingGraphInfoMiddle, missingGraphInfo, nonexistingFile, null, true),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, existingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, existingFile, null, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, nonexistingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, nonexistingFile, laterOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, nonexistingFile, middleOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, existingGraphInfoMiddle, nonexistingFile, null, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, existingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, existingFile, laterOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, existingFile, middleOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, existingFile, null, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, nonexistingFile, earlierOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, nonexistingFile, laterOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, nonexistingFile, middleOrsGraphInfoV1, false),
                Arguments.of(missingGraphInfo, missingGraphInfo, nonexistingFile, null, false)
        );
    }
    @Test
    void findLatestGraphComponent() {
    }
}