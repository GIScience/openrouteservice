package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.config.defaults.DefaultProfilePropertiesBikeElectric;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.*;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.NamedGraphsRepoStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.NexusRepoManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ORSGraphFileManagerTest {

    @Spy
    NexusRepoManager orsGraphRepoManager;

    ORSGraphFolderStrategy orsGraphFolderStrategy;
    ORSGraphFileManager orsGraphFileManager;

    private static final String GRAPHS_REPO_BASE_URL = "https://example.com";
    private static final String GRAPHS_REPO_NAME = "test-repo";
    private static final String GRAPHS_PROFILE_GROUP = "special-profiles";
    private static final String GRAPHS_COVERAGE = "planet";
    private static final String GRAPHS_VERSION = "1";
    private static final String VEHICLE = "car";
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private static Path TEMP_DIR;

    private static final long EARLIER_DATE = 1692373111000L;
    private static final long MIDDLE_DATE = 1692373222000L;
    private static final long LATER_DATE = 1692373333000L;

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

    EngineProperties getEngineProperties() {
        EngineProperties engineProperties = new EngineProperties();
        GraphManagementProperties graphManagementProperties = new GraphManagementProperties();
        graphManagementProperties.setRepositoryUrl(GRAPHS_REPO_BASE_URL);
        graphManagementProperties.setRepositoryName(GRAPHS_REPO_NAME);
        graphManagementProperties.setGraphExtent(GRAPHS_COVERAGE);
        graphManagementProperties.setRepositoryProfileGroup(GRAPHS_PROFILE_GROUP);
        graphManagementProperties.setGraphVersion(GRAPHS_VERSION);
        graphManagementProperties.setMaxBackups(3);
        engineProperties.setGraphManagement(graphManagementProperties);
        engineProperties.initialize();
        return engineProperties;
    }

    void setupORSGraphManager(String hash) {
        EngineProperties engineProperties = getEngineProperties();
        setupORSGraphManager(hash, engineProperties, null);
    }

    void setupORSGraphManager(String hash, EngineProperties engineProperties, String customGraphFolder) {
        File localDir = TEMP_DIR.toFile();
        if (customGraphFolder != null) {
            localDir = Path.of(customGraphFolder).toFile();
        }
        vehicleDirAbsPath = String.join("/", localDir.getAbsolutePath(), VEHICLE);
        hashDirAbsPath = String.join("/", vehicleDirAbsPath, hash);

        orsGraphFolderStrategy = new HashSubDirBasedORSGraphFolderStrategy(localDir.getAbsolutePath(), VEHICLE, hash);
        orsGraphFileManager = new ORSGraphFileManager(engineProperties, VEHICLE, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        orsGraphRepoManager = new NexusRepoManager(engineProperties, VEHICLE, new NamedGraphsRepoStrategy(engineProperties, VEHICLE), orsGraphFileManager);
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
        doReturn(null).when(orsGraphRepoManager).findLatestGraphInfoAsset();
    }

    void simulateFindLatestGraphInfoAsset(String hash, Long osmDateRemote) throws IOException {
        String graphInfoAssetName = orsGraphFileManager.getGraphInfoFileNameInRepository();
        String graphInfoAssetUrl = String.join("/", GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, VEHICLE, graphInfoAssetName);

        ORSGraphInfoV1 downloadedOrsGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateRemote));
        downloadedGraphInfoV1File = new File(vehicleDirAbsPath + "/" + graphInfoAssetName);
        new ObjectMapper().writeValue(downloadedGraphInfoV1File, downloadedOrsGraphInfoV1Object);

        AssetXO assetXO = new AssetXO();
        assetXO.setDownloadUrl(graphInfoAssetUrl);

        doReturn(assetXO).when(orsGraphRepoManager).findLatestGraphInfoAsset();
        lenient().doNothing().when(orsGraphRepoManager).downloadAsset(anyString(), any());
    }

    private static @NotNull ORSGraphInfoV1 createOrsGraphInfoV1() {
        ORSGraphInfoV1 orsGraphInfoV1 = new ORSGraphInfoV1();
        orsGraphInfoV1.setOsmDate(new Date(EARLIER_DATE));
        orsGraphInfoV1.setImportDate(new Date(LATER_DATE));
        ProfileProperties profileProperties = new DefaultProfilePropertiesBikeElectric(true);
        orsGraphInfoV1.setProfileProperties(profileProperties);
        return orsGraphInfoV1;
    }

    @Test
    void writeOrsGraphInfoV1() {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);

        File testFile = new File(vehicleDir, "writeOrsGraphInfoV1.yml");
        ORSGraphInfoV1 orsGraphInfoV1 = createOrsGraphInfoV1();
        assertFalse(testFile.exists());
        orsGraphFileManager.writeOrsGraphInfoV1(orsGraphInfoV1, testFile);
        assertTrue(testFile.exists());
    }

    @Test
    @Disabled //FIXME - serialization changed
    void readOrsGraphInfoV1() {
        String hash = "1a2b3c";
        setupORSGraphManager(hash);

        File writtenTestFile = new File(vehicleDir, "readOrsGraphInfoV1.yml");
        ORSGraphInfoV1 writtenOrsGraphInfoV1 = createOrsGraphInfoV1();
        ORSGraphFileManager.writeOrsGraphInfoV1(writtenOrsGraphInfoV1, writtenTestFile);
        ORSGraphInfoV1 readOrsGraphInfoV1 = orsGraphFileManager.readOrsGraphInfoV1(writtenTestFile);
        assertThat(readOrsGraphInfoV1).usingRecursiveComparison().isEqualTo(writtenOrsGraphInfoV1);
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
        EngineProperties engineProperties = RepoManagerTestHelper.createEngineProperties(null, null,
                GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, GRAPHS_PROFILE_GROUP, GRAPHS_COVERAGE, GRAPHS_VERSION, 0);
        engineProperties.getGraphManagement().setMaxBackups(2);

        setupORSGraphManager(hash, engineProperties, null);
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
        EngineProperties engineProperties = RepoManagerTestHelper.createEngineProperties(null, null,
                GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, GRAPHS_PROFILE_GROUP, GRAPHS_COVERAGE, GRAPHS_VERSION, 0);
        engineProperties.getGraphManagement().setMaxBackups(0);

        setupORSGraphManager(hash, engineProperties, null);
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
        EngineProperties engineConfig = RepoManagerTestHelper.createEngineProperties(null, null,
                GRAPHS_REPO_BASE_URL, GRAPHS_REPO_NAME, GRAPHS_PROFILE_GROUP, GRAPHS_COVERAGE, GRAPHS_VERSION, 0);
        engineConfig.getGraphManagement().setMaxBackups(-5);

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

        setupORSGraphManager("foo", getEngineProperties(), testFolder.toString());
        assertTrue(orsGraphFileManager.getProfileGraphsDirAbsPath().contains(testFolder.toString()));

        // Assert that the folder has no write permissions
        assertFalse(testFolder.toFile().canWrite());
        assertFalse(orsGraphFileManager.hasActiveGraph());

        // Set write permissions
        perms.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(testFolder, perms);

        // Initialize again
        orsGraphFileManager.initialize();
        assertTrue(testFolder.toFile().canWrite());
        assertTrue(orsGraphFileManager.hasActiveGraphDirectory());
    }
}