package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.EngineConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);

        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .buildWithAppConfigOverride();

        orsGraphFileManager = new ORSGraphFileManager(engineConfig, hash, hashDirAbsPath, vehicleDirAbsPath, VEHICLE);
        orsGraphRepoManager.initialize(engineConfig);
        orsGraphRepoManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphRepoManager.setRouteProfileName(VEHICLE);
        orsGraphRepoManager.setFileManager(orsGraphFileManager);
    }

    void setupLocalGraphDirectory(String hash, Long osmDateLocal) throws IOException {
        if (hash == null) return;
        hashDir = new File(hashDirAbsPath);
        hashDir.mkdirs();
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
    void shouldDownloadGraph(Date remoteDate, Date activeDate, Date downloadedExtractedDate, Date downloadedCompressedDate, boolean expected) {
        setupORSGraphManager("1243abc");
        assertEquals(expected, orsGraphRepoManager.shouldDownloadGraph(remoteDate, activeDate, downloadedExtractedDate, downloadedCompressedDate));
    }

    public static Stream<Arguments> shouldDownloadGraphMethodSource() {
        Date earlierDate = new Date(EARLIER_DATE);
        Date laterDate = new Date(LATER_DATE);

        return Stream.of(           //  downloaded        remote                   active
                Arguments.of(laterDate, earlierDate, earlierDate, earlierDate, true),
                Arguments.of(earlierDate, laterDate, earlierDate, earlierDate, false),
                Arguments.of(earlierDate, earlierDate, laterDate, earlierDate, false),
                Arguments.of(earlierDate, earlierDate, earlierDate, laterDate, false),
                Arguments.of(earlierDate, earlierDate, earlierDate, earlierDate, false)
        );
    }

    @ParameterizedTest
    @MethodSource("comparisonDates")
    public void comparisonDate(Date expectedDate, GraphInfo graphInfo) {
        assertEquals(expectedDate, orsGraphRepoManager.comparisonDate(graphInfo));
    }

    public static Stream<Arguments> comparisonDates() throws MalformedURLException {
        Date osmDate = new Date();
        return Stream.of(
                Arguments.of(new Date(0), null),
                Arguments.of(new Date(0), new GraphInfo()),
                Arguments.of(new Date(0), new GraphInfo().withLocalDirectory(new File(LOCAL_PATH))),
                Arguments.of(new Date(0), new GraphInfo().withRemoteUrl(new URL("http://some.url.ors/"))),
                Arguments.of(new Date(0), new GraphInfo().withPersistedInfo(null)),
                Arguments.of(new Date(0), new GraphInfo().withPersistedInfo(new ORSGraphInfoV1())),
                Arguments.of(new Date(0), new GraphInfo().withPersistedInfo(new ORSGraphInfoV1(null))),
                Arguments.of(osmDate, new GraphInfo().withPersistedInfo(new ORSGraphInfoV1(osmDate)))
        );
    }

    @ParameterizedTest
    @MethodSource("comparisonDatesForDownloadFiles")
    public void comaprisonDate(Date expectedDate, File downloadFile, ORSGraphInfoV1 orsGraphInfoV1) {
        assertEquals(expectedDate, orsGraphRepoManager.comparisonDate(downloadFile, orsGraphInfoV1));
    }

    public static Stream<Arguments> comparisonDatesForDownloadFiles() throws MalformedURLException {
        Date osmDate = new Date();
        File resourcesDir = new File(LOCAL_PATH).getParentFile();
        File nonexistingFile = new File(resourcesDir, "missing.ghz");
        File existingFile = new File(resourcesDir, "some.ghz");
        return Stream.of(
                Arguments.of(new Date(0), null, null),
                Arguments.of(new Date(0), null, new ORSGraphInfoV1()),
                Arguments.of(new Date(0), null, new ORSGraphInfoV1(null)),
                Arguments.of(new Date(0), null, new ORSGraphInfoV1(osmDate)),
                Arguments.of(new Date(0), nonexistingFile, null),
                Arguments.of(new Date(0), nonexistingFile, new ORSGraphInfoV1()),
                Arguments.of(new Date(0), nonexistingFile, new ORSGraphInfoV1(null)),
                Arguments.of(new Date(0), nonexistingFile, new ORSGraphInfoV1(osmDate)),
                Arguments.of(new Date(0), existingFile, null),
                Arguments.of(new Date(0), existingFile, new ORSGraphInfoV1()),
                Arguments.of(new Date(0), existingFile, new ORSGraphInfoV1(null)),
                Arguments.of(osmDate, existingFile, new ORSGraphInfoV1(osmDate))
        );
    }

    @Test
    void newestDate() {

    }
}