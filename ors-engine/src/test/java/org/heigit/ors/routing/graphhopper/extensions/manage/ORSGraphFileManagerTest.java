package org.heigit.ors.routing.graphhopper.extensions.manage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import org.openapitools.client.model.AssetXO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
        vehicleDir.mkdirs();
    }

    @AfterEach
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(vehicleDir);
    }

    private File createBackupDirectory(String hash, String dateString) throws IOException {
        File oldBackupDir = setupLocalGraphDirectory(hash, EARLIER_DATE);
        oldBackupDir.renameTo(new File(oldBackupDir.getAbsolutePath()+"_"+dateString));
        return oldBackupDir;
    }

    private void assertCorrectBackupDir(File backupDir, String hash) {
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, hash+".json").exists());
    }

    EngineConfig getEngineConfig() {
        return EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .setMaxNumberOfGraphBackups(3)
                .buildWithAppConfigOverride();
    }

    void setupORSGraphManager(String hash) {
        EngineConfig engineConfig = getEngineConfig();
        setupORSGraphManager(hash, engineConfig, null);
    }

    void setupORSGraphManager(String hash, EngineConfig engineConfig, String customGraphFolder) {
        File localDir = TEMP_DIR.toFile();
        if (customGraphFolder != null) {
            localDir = Path.of(customGraphFolder).toFile();
        }
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);

        orsGraphFileManager = new ORSGraphFileManager(engineConfig, hash, hashDirAbsPath, vehicleDirAbsPath, VEHICLE);
        orsGraphFileManager.initialize();
        orsGraphRepoManager.initialize(engineConfig);
        orsGraphRepoManager.setGraphsRepoGraphVersion(GRAPHS_VERSION);
        orsGraphRepoManager.setRouteProfileName(VEHICLE);
        orsGraphRepoManager.setFileManager(orsGraphFileManager);
    }

    File setupLocalGraphDirectory(String hash, Long osmDateLocal) throws IOException {
        if (hash == null) return null;
        hashDir = new File(hashDirAbsPath);
        hashDir.mkdirs();
        ORSGraphInfoV1 localOrsGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateLocal));
        localGraphInfoV1File = new File(hashDir, hash + ".json");
        new ObjectMapper().writeValue(localGraphInfoV1File, localOrsGraphInfoV1Object);

        return hashDir;
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
        assertEquals(0, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        assertFalse(localGraphDir.exists());
        assertEquals(1, orsGraphFileManager.findGraphBackupsSortedByName().size());
        orsGraphFileManager.findGraphBackupsSortedByName().forEach(dir -> assertCorrectBackupDir(dir, hash));
    }

    @Test
    void backupExistingGraph_withPreviousBackup() throws IOException {
        String hash = "2a2b3c";
        setupORSGraphManager(hash);
        createBackupDirectory(hash, "2022-12-31_235959");
        File localGraphDir = setupLocalGraphDirectory(hash, MIDDLE_DATE);

        assertTrue(localGraphDir.exists());
        assertEquals(1, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        assertFalse(localGraphDir.exists());
        assertEquals(2, orsGraphFileManager.findGraphBackupsSortedByName().size());
        orsGraphFileManager.findGraphBackupsSortedByName().forEach(dir -> assertCorrectBackupDir(dir, hash));
    }

    @Test
    void backupExistingGraph_withMaxNumOfPreviousBackups() throws IOException {
        String hash = "2a2b3c";
        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .setMaxNumberOfGraphBackups(2)
                .buildWithAppConfigOverride();

        setupORSGraphManager(hash, engineConfig, null);
        createBackupDirectory(hash, "2022-12-31_235959");
        createBackupDirectory(hash, "2023-01-01_060000");
        File localGraphDir = setupLocalGraphDirectory(hash, MIDDLE_DATE);

        assertTrue(localGraphDir.exists());
        assertEquals(2, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        assertFalse(localGraphDir.exists());
        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(2, backups.size());
        backups.forEach(dir -> assertCorrectBackupDir(dir, hash));
        assertEquals("2a2b3c_2023-01-01_060000", backups.get(0).getName());
        assertNotEquals("2a2b3c_2022-12-31_235959", backups.get(1).getName());
    }

    @Test
    public void deleteOldestBackups() throws IOException {
        String hash = "2a2b3c";
        setupORSGraphManager(hash);
        createBackupDirectory(hash, "2023-01-01_060000");
        createBackupDirectory(hash, "2023-01-02_060000");
        createBackupDirectory(hash, "2023-01-03_060000");
        createBackupDirectory(hash, "2023-01-04_060000");
        createBackupDirectory(hash, "2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(3, backups.size());
        backups.forEach(dir -> assertCorrectBackupDir(dir, hash));
        assertEquals(Arrays.asList("2a2b3c_2023-01-03_060000","2a2b3c_2023-01-04_060000","2a2b3c_2023-01-05_060000"), backups.stream().map(File::getName).toList());
    }

    @Test
    public void deleteOldestBackups_maxNumberOfGraphBackupsIsZero() throws IOException {
        String hash = "2a2b3c";
        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .setMaxNumberOfGraphBackups(0)
                .buildWithAppConfigOverride();

        setupORSGraphManager(hash, engineConfig, null);
        createBackupDirectory(hash, "2023-01-01_060000");
        createBackupDirectory(hash, "2023-01-02_060000");
        createBackupDirectory(hash, "2023-01-03_060000");
        createBackupDirectory(hash, "2023-01-04_060000");
        createBackupDirectory(hash, "2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(0, backups.size());
    }

    @Test
    public void deleteOldestBackups_maxNumberOfGraphBackupsIsNegative() throws IOException {
        String hash = "2a2b3c";
        EngineConfig engineConfig = EngineConfig.EngineConfigBuilder.init()
                .setGraphsRepoUrl(GRAPHS_REPO_BASE_URL)
                .setGraphsRepoName(GRAPHS_REPO_NAME)
                .setGraphsExtent(GRAPHS_COVERAGE)
                .setMaxNumberOfGraphBackups(-5)
                .buildWithAppConfigOverride();

        setupORSGraphManager(hash, engineConfig, null);
        createBackupDirectory(hash, "2023-01-01_060000");
        createBackupDirectory(hash, "2023-01-02_060000");
        createBackupDirectory(hash, "2023-01-03_060000");
        createBackupDirectory(hash, "2023-01-04_060000");
        createBackupDirectory(hash, "2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(0, backups.size());
    }

    @Test
    void testInitialize() throws IOException {
        Path testFolder = Files.createDirectory(TEMP_DIR.resolve("noWritePermissionFolder"));

        // This code to remove write permissions is POSIX-specific (Unix-like OSes)
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(testFolder, perms);

        setupORSGraphManager("foo", getEngineConfig(), testFolder.toString());
        assertTrue(orsGraphFileManager.getVehicleGraphDirAbsPath().contains(testFolder.toString()));

        // Assert that the folder has no write permissions
        assertFalse(testFolder.toFile().canWrite());
        assertFalse(orsGraphFileManager.hasLocalGraph());

        // Set write permissions
        perms.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(testFolder, perms);

        // Initialize again
        orsGraphFileManager.initialize();
        assertTrue(testFolder.toFile().canWrite());
        assertTrue(orsGraphFileManager.hasLocalGraph());
    }
}