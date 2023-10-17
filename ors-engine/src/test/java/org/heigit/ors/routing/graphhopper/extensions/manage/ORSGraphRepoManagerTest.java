package org.heigit.ors.routing.graphhopper.extensions.manage;

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
class ORSGraphRepoManagerTest {

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
    void downloadGraphIfNecessary_localDataExists_noRemoteData() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        setupNoRemoteFiles();

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, never()).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_noLocalData_remoteDataExists() throws IOException {
        String hash = "abc123";
        setupORSGraphManager(hash);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(2)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate1_remoteDate2() throws IOException {
        String hash = "xyz111";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, LATER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(2)).downloadAsset(anyString(), any());
        File localGraphDir = new File(hashDirAbsPath);
        File backupDir = new File(hashDirAbsPath + "_bak");
        assertTrue(localGraphDir.exists());
        assertFalse(backupDir.exists());
    }

    @Test
    void downloadGraphIfNecessary_localDate1_remoteDate1() throws IOException {
        String hash = "xyz222";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, EARLIER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(1)).downloadAsset(anyString(), any());
    }

    @Test
    void downloadGraphIfNecessary_localDate2_remoteDate1() throws IOException {
        String hash = "xyz333";
        setupORSGraphManager(hash);
        setupLocalGraphDirectory(hash, LATER_DATE);
        simulateFindLatestGraphInfoAsset(hash, EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        verify(orsGraphRepoManager, times(1)).downloadAsset(anyString(), any());
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
        AssetXO filtered = orsGraphRepoManager.filterLatestAsset("abc123.json", items);
        assertEquals("https://example.com/test-repo/planet/1/car/abc123/202301011200/abc123.json", filtered.getPath());
    }

    @ParameterizedTest
    @MethodSource("shouldDownloadGraphMethodSource")
    void shouldDownloadGraph(GraphInfo remoteGraphInfo, GraphInfo localGraphInfo, File persistedDownloadFile, ORSGraphInfoV1 persistedRemoteGraphInfo, boolean expected) {
        setupORSGraphManager("1243abc");
        assertEquals(expected, orsGraphRepoManager.shouldDownloadGraph(remoteGraphInfo, localGraphInfo, persistedDownloadFile, persistedRemoteGraphInfo));
    }

    public static Stream<Arguments> shouldDownloadGraphMethodSource() {
        ORSGraphInfoV1 earlierOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(EARLIER_DATE));
        ORSGraphInfoV1 middleOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(MIDDLE_DATE));
        ORSGraphInfoV1 laterOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(LATER_DATE));
        GraphInfo missingGraphInfo = new GraphInfo();
        GraphInfo existingGraphInfoEarlier = new GraphInfo().withPersistedInfo(earlierOrsGraphInfoV1);
        GraphInfo existingGraphInfoMiddle = new GraphInfo().withPersistedInfo(middleOrsGraphInfoV1);
        GraphInfo existingGraphInfoLater = new GraphInfo().withPersistedInfo(laterOrsGraphInfoV1);

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