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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapdb.Engine;
import org.mockito.*;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        MockitoAnnotations.openMocks(this);
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
        orsGraphRepoManager = new ORSGraphRepoManager(engineConfig, orsGraphFileManager, VEHICLE, GRAPHS_VERSION);
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
        orsGraphRepoManager = spy(orsGraphRepoManager);
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
        orsGraphRepoManager = spy(orsGraphRepoManager);
        doReturn(assetXO).when(orsGraphRepoManager).findLatestGraphInfoAsset(graphInfoAssetName);
        lenient().doNothing().when(orsGraphRepoManager).downloadAsset(anyString(), any());
    }

    @ParameterizedTest
    @MethodSource("initializeMethodSource")
    void ORSGraphRepoManager_initialize(EngineConfig engineConfig, ORSGraphFileManager orsGraphFileManager, String routeProfileName, String graphsRepoGraphVersion, boolean isValid) {
        ORSGraphRepoManager localOrsGraphRepoManager = new ORSGraphRepoManager(engineConfig, orsGraphFileManager, routeProfileName, graphsRepoGraphVersion);
        assertEquals(engineConfig.getGraphsRepoUrl() == null ? "" : engineConfig.getGraphsRepoUrl(), localOrsGraphRepoManager.getGraphsRepoUrl());
        assertEquals(engineConfig.getGraphsRepoName() == null ? "" : engineConfig.getGraphsRepoName(), localOrsGraphRepoManager.getGraphsRepoName());
        assertEquals(engineConfig.getGraphsExtent() == null ? "" : engineConfig.getGraphsExtent(), localOrsGraphRepoManager.getGraphsRepoCoverage());
        assertEquals(routeProfileName == null ? "" : routeProfileName, localOrsGraphRepoManager.getRouteProfileName());
        assertEquals(graphsRepoGraphVersion == null ? "" : graphsRepoGraphVersion, localOrsGraphRepoManager.getGraphsRepoGraphVersion());
        assertEquals(isValid, localOrsGraphRepoManager.isValid());
    }

    /**
     * Provides a set of test cases for parametrized tests.
     * This method source will generate test arguments by creating permutations
     * of provided URLs, names, and coverages, combined with several hard-coded test cases.
     *
     * <p>Each argument set includes:
     * 1. An instance of EngineConfig.
     * 2. An instance of ORSGraphFileManager or null.
     * 3. A string representing a car name or null.
     * 4. A string representing an id or null.
     * 5. A boolean indicating if the case is expected to be a "perfect" one.
     * </p>
     *
     * @return A stream of {@link Arguments} instances for parameterized testing.
     */
    public static Stream<Arguments> initializeMethodSource() {
        List<String> urls = Arrays.asList("", null);
        List<String> names = Arrays.asList("", null);
        List<String> coverages = Arrays.asList("", null);

        List<Arguments> argsList = new ArrayList<>();

        for (String url : urls) {
            for (String name : names) {
                for (String coverage : coverages) {
                    EngineConfig localEngineConfig = EngineConfig.EngineConfigBuilder.init()
                            .setGraphsRepoUrl(url)
                            .setGraphsRepoName(name)
                            .setGraphsExtent(coverage)
                            .buildWithAppConfigOverride();
                    argsList.add(Arguments.of(localEngineConfig, null, null, null, false));
                    argsList.add(Arguments.of(localEngineConfig, null, "car", null, false));
                    argsList.add(Arguments.of(localEngineConfig, null, null, "1", false));
                    argsList.add(Arguments.of(localEngineConfig, null, "car", "1", false));
                    argsList.add(Arguments.of(localEngineConfig, new ORSGraphFileManager(localEngineConfig, "abc123", "abc123", "abc123", "car"), null, null, false));
                    argsList.add(Arguments.of(localEngineConfig, new ORSGraphFileManager(localEngineConfig, "abc123", "abc123", "abc123", "car"), "car", null, false));
                    argsList.add(Arguments.of(localEngineConfig, new ORSGraphFileManager(localEngineConfig, "abc123", "abc123", "abc123", "car"), null, "1", false));
                    argsList.add(Arguments.of(localEngineConfig, new ORSGraphFileManager(localEngineConfig, "abc123", "abc123", "abc123", "car"), "car", "1", false));
                }
            }
        }
        // Perfect case
        EngineConfig perfectEngineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .buildWithAppConfigOverride();
        ORSGraphFileManager perfectOrsGraphFileManager = new ORSGraphFileManager(perfectEngineConfig, "abc123", "abc123", "abc123", "car");

        argsList.add(Arguments.of(perfectEngineConfig, perfectOrsGraphFileManager, "bar", "1", true));
        return argsList.stream();
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

    public static Stream<Arguments> shouldDownloadGraphMethodSource() throws IOException {
        ORSGraphInfoV1 earlierOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(EARLIER_DATE));
        ORSGraphInfoV1 middleOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(MIDDLE_DATE));
        ORSGraphInfoV1 laterOrsGraphInfoV1 = new ORSGraphInfoV1(new Date(LATER_DATE));
        GraphInfo missingGraphInfo = new GraphInfo();
        GraphInfo existingGraphInfoEarlier = new GraphInfo().withPersistedInfo(earlierOrsGraphInfoV1);
        GraphInfo existingGraphInfoMiddle = new GraphInfo().withPersistedInfo(middleOrsGraphInfoV1);
        GraphInfo existingGraphInfoLater = new GraphInfo().withPersistedInfo(laterOrsGraphInfoV1);

        Path resourcesDir = Files.createTempDirectory(TEMP_DIR, "ghz");

        File nonexistingFile = new File(resourcesDir.toAbsolutePath().toFile(), "missing.ghz");
        File existingFile = Files.createTempFile(resourcesDir, "some", ".ghz").toFile();

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

    @ParameterizedTest
    @CsvSource(value = {
            "https://example.com/file.jpg",
            "http://example.com//file.jpg",
            "http://example.com/test/file.jpg",
            "'  http://example.com/test/file.jpg'"
    })
    void testCopyFromUrlToTempFile_ValidUrl_ShouldNotThrow(String validUrl) throws IOException {
        setupORSGraphManager("1243abc");
        Path downloadAssetScenarios = TEMP_DIR.resolve("downloadAssetScenarios");
        downloadAssetScenarios.toFile().mkdirs();

        File outputFile = Files.createTempFile(downloadAssetScenarios, "outputFile", ".jpg").toFile();

        // Test that the method returns true and not an exception
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            // Mock the static method
            mockedFileUtils.when(() -> FileUtils.copyURLToFile(
                    new URI(validUrl.trim()).toURL(),
                    outputFile,
                    orsGraphRepoManager.getConnectionTimeoutMillis(),
                    orsGraphRepoManager.getReadTimeoutMillis()
            )).thenAnswer(DoesNothing.doesNothing());

            assertDoesNotThrow(() -> orsGraphRepoManager.copyFromUrlToFile(validUrl, outputFile));
            assertTrue(outputFile.exists());
        }

    }

    @ParameterizedTest
    @CsvSource(value = {
            "invalid_url",
            "example.com/test/file.jpg",
            "htp://example.com/test/file.jpg",
            "https://example.com/tes#t/file.jpg"
    })
    void testCopyFromUrlToTempFile_InvalidUrl_ShouldThrowURISyntaxException(String invalidUrl) {
        setupORSGraphManager("1243abc");
        File tempFile = new File("temp.txt");

        assertThrows(IllegalArgumentException.class, () -> orsGraphRepoManager.copyFromUrlToFile(invalidUrl, tempFile));
    }

    @Test
    void testRenameTempFileToOutputFile_SuccessfulRename() throws IOException {
        setupORSGraphManager("1243abc");
        Path downloadAssetScenarios = TEMP_DIR.resolve("downloadAssetScenarios");
        downloadAssetScenarios.toFile().mkdirs();
        File tempFile = Files.createTempFile(downloadAssetScenarios, "tempFile", ".jpg").toFile();
        File outputFile = downloadAssetScenarios.resolve("outputFile.jpg").toFile();

        assertFalse(outputFile.exists());
        assertDoesNotThrow(() -> orsGraphRepoManager.renameTempFileToOutputFile(tempFile, outputFile));
        assertTrue(outputFile.exists());
    }

    @Test
    void testRenameTempFileToOutputFile_FailedRename() {
        setupORSGraphManager("1243abc");

        File tempFile = mock(File.class);
        File outputFile = new File("output.txt");
        when(tempFile.renameTo(outputFile)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> orsGraphRepoManager.renameTempFileToOutputFile(tempFile, outputFile));
    }

    @ParameterizedTest
    @MethodSource("downloadAssetTestCases")
    void testDownloadAsset_ExceptionCases(String downloadUrl, File outputFile, Class<? extends Throwable> expectedException) {
        setupORSGraphManager("1243abc");
        assertThrows(expectedException, () -> orsGraphRepoManager.downloadAsset(downloadUrl, outputFile));
    }

    private static Stream<Arguments> downloadAssetTestCases() throws IOException {
        Path downloadAssetScenarios = TEMP_DIR.resolve("downloadAssetScenarios");
        downloadAssetScenarios.toFile().mkdirs();
        return Stream.of(
                Arguments.of(null, new File("output.txt"), IllegalArgumentException.class),
                Arguments.of("http://valid.url", null, IllegalArgumentException.class),
                Arguments.of("   ", new File("output.txt"), IllegalArgumentException.class),
                Arguments.of("http://valid.url", new File("output.txt"), IllegalArgumentException.class)
        );
    }

    @Test
    void findLatestGraphComponent() {
    }
}