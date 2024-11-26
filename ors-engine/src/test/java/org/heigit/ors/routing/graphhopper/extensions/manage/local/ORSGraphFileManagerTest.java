package org.heigit.ors.routing.graphhopper.extensions.manage.local;

import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ORSGraphFileManagerTest {

    private static final String LOCAL_PROFILE_NAME = "truck";
    private static final String ENCODER_NAME = "driving-hgv";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private Path tempDir;

    private Path localGraphsRootPath;
    private Path localGraphPath;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphFolderStrategy orsGraphFolderStrategy;

    @BeforeEach
    public void setUp() throws IOException {
        localGraphsRootPath = RepoManagerTestHelper.createLocalGraphsRootDirectory(tempDir);
    }

    @AfterEach
    void deleteFiles() throws IOException {
        cleanupLocalGraphsRootDirectory(localGraphsRootPath);
    }

    private void setupORSGraphManager(GraphManagementRuntimeProperties managementProps) throws IOException {
        orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        localGraphPath = createLocalGraphDirectoryWithGraphBuildInfoFile(
                localGraphsRootPath,
                LOCAL_PROFILE_NAME,
                orsGraphFolderStrategy.getActiveGraphBuildInfoFileName(),
                LATER_DATE, EARLIER_DATE);
    }

    private void createBackupDirectory(String dateString) throws IOException {
        RepoManagerTestHelper.createLocalGraphDirectoryWithGraphBuildInfoFile(
                localGraphsRootPath,
                LOCAL_PROFILE_NAME + "_" + dateString,
                orsGraphFolderStrategy.getActiveGraphBuildInfoFileName(),
                null, null);
    }

    private void assertCorrectBackupDir(File backupDir) {
        assertTrue(backupDir.isDirectory());
        assertTrue(new File(backupDir, orsGraphFolderStrategy.getActiveGraphBuildInfoFileName()).exists());
    }

    private static PersistedGraphBuildInfo createOrsGraphBuildInfoV1() {
        PersistedGraphBuildInfo persistedGraphBuildInfo = new PersistedGraphBuildInfo();
        persistedGraphBuildInfo.setOsmDate(new Date(EARLIER_DATE));
        persistedGraphBuildInfo.setGraphBuildDate(new Date(LATER_DATE));
        ProfileProperties profileProperties = new ProfileProperties();
        persistedGraphBuildInfo.setProfileProperties(profileProperties);
        return persistedGraphBuildInfo;
    }

    private GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME);
    }

    @Test
    void writeOrsGraphBuildInfo() throws IOException {
        setupORSGraphManager(managementPropsBuilder().build());
        File testFile = new File(localGraphPath.toFile(), "writeOrsGraphBuildInfoV1.yml");
        PersistedGraphBuildInfo persistedGraphBuildInfo = createOrsGraphBuildInfoV1();
        assertFalse(testFile.exists());

        ORSGraphFileManager.writeOrsGraphBuildInfo(persistedGraphBuildInfo, testFile);

        assertTrue(testFile.exists());
    }

    @Test
    void readOrsGraphBuildInfo() throws IOException, ORSGraphFileManagerException {
        setupORSGraphManager(managementPropsBuilder().build());
        File writtenTestFile = new File(localGraphPath.toFile(), "readOrsGraphBuildInfoV1.yml");
        PersistedGraphBuildInfo writtenPersistedGraphBuildInfo = createOrsGraphBuildInfoV1();
        ORSGraphFileManager.writeOrsGraphBuildInfo(writtenPersistedGraphBuildInfo, writtenTestFile);

        PersistedGraphBuildInfo readPersistedGraphBuildInfo = orsGraphFileManager.readOrsGraphBuildInfo(writtenTestFile);

        assertThat(readPersistedGraphBuildInfo).usingRecursiveComparison().isEqualTo(writtenPersistedGraphBuildInfo);
    }

    @Test
    void backupExistingGraph_noPreviousBackup() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(3).build());
        File localGraphDir = localGraphPath.toFile();
        assertTrue(localGraphDir.isDirectory());
        assertEquals(0, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        localGraphDir = localGraphPath.toFile();
        assertFalse(localGraphDir.exists());
        assertEquals(1, orsGraphFileManager.findGraphBackupsSortedByName().size());
        orsGraphFileManager.findGraphBackupsSortedByName().forEach(this::assertCorrectBackupDir);
    }

    @Test
    void backupExistingGraph_withPreviousBackup() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(3).build());
        createBackupDirectory("2022-12-31_235959");
        assertTrue(localGraphPath.toFile().exists());
        assertEquals(1, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        assertFalse(localGraphPath.toFile().exists());
        assertEquals(2, orsGraphFileManager.findGraphBackupsSortedByName().size());
        orsGraphFileManager.findGraphBackupsSortedByName().forEach(this::assertCorrectBackupDir);
    }

    @Test
    void backupExistingGraph_withMaxNumOfPreviousBackups() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(2).build());
        createBackupDirectory("2022-12-31_235959");
        createBackupDirectory("2023-01-01_060000");
        assertTrue(localGraphPath.toFile().exists());
        assertEquals(2, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.backupExistingGraph();

        assertFalse(localGraphPath.toFile().exists());
        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(2, backups.size());
        backups.forEach(this::assertCorrectBackupDir);
        assertEquals("truck_2023-01-01_060000", backups.get(0).getName());
        assertNotEquals("truck_2022-12-31_235959", backups.get(1).getName());
    }

    @Test
    void deleteOldestBackups() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(3).build());
        createBackupDirectory("2023-01-01_060000");
        createBackupDirectory("2023-01-02_060000");
        createBackupDirectory("2023-01-03_060000");
        createBackupDirectory("2023-01-04_060000");
        createBackupDirectory("2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(3, backups.size());
        backups.forEach(this::assertCorrectBackupDir);
        assertEquals(Arrays.asList("truck_2023-01-03_060000", "truck_2023-01-04_060000", "truck_2023-01-05_060000"), backups.stream().map(File::getName).toList());
    }

    @Test
    void deleteOldestBackups_maxNumberOfGraphBackupsIsZero() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(0).build());
        createBackupDirectory("2023-01-01_060000");
        createBackupDirectory("2023-01-02_060000");
        createBackupDirectory("2023-01-03_060000");
        createBackupDirectory("2023-01-04_060000");
        createBackupDirectory("2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(0, backups.size());
    }

    @Test
    void deleteOldestBackups_maxNumberOfGraphBackupsIsNegative() throws IOException {
        setupORSGraphManager(managementPropsBuilder().withMaxNumberOfGraphBackups(-5).build());
        createBackupDirectory("2023-01-01_060000");
        createBackupDirectory("2023-01-02_060000");
        createBackupDirectory("2023-01-03_060000");
        createBackupDirectory("2023-01-04_060000");
        createBackupDirectory("2023-01-05_060000");
        assertEquals(5, orsGraphFileManager.findGraphBackupsSortedByName().size());

        orsGraphFileManager.deleteOldestBackups();

        List<File> backups = orsGraphFileManager.findGraphBackupsSortedByName();
        assertEquals(0, backups.size());
    }

}