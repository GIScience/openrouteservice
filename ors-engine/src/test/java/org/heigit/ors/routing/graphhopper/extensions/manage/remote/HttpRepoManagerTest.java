package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphInfoV1;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.nio.file.Files.createDirectories;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
class HttpRepoManagerTest {

    private static final String REPO_NAME = "vendor-xyz";
    private static final String REPO_PROFILE_GROUP = "fastisochrones";
    private static final String REPO_COVERAGE = "heidelberg";
    private static final String GRAPHS_VERSION = "1";
    private static final String GRAPHS_VERSION_NO_REPO_DATA = "0";
    private static final String LOCAL_PROFILE_NAME = "driving-car";
    private static final String ENCODER_NAME = "driving-car";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private static Path TEMP_DIR;
    @Container
    private static NginxContainer nginx;
    private static String nginxUrl;
    private static Path localGraphsRootPath;
    private static long EARLIER_DATE;
    private static long REPO_DATE;
    private static long LATER_DATE;

    private HttpRepoManager orsGraphRepoManager;
    private ORSGraphFileManager orsGraphFileManager;
    private String localGraphPath;
    private File localGraphDir;
    private File localGraphInfoV1File;

    static {
        DockerImageName dockerImageName = DockerImageName.parse("nginx:1.23.4-alpine");
        MountableFile mountableFile = MountableFile.forHostPath("src/test/resources/test-filesystem-repos/");
        nginx = new NginxContainer<>(dockerImageName)
                .withCopyFileToContainer(mountableFile, "/usr/share/nginx/html")
                .waitingFor(new HttpWaitStrategy());
        nginx.start();
        nginxUrl = "http://" + nginx.getHost() + ":" + nginx.getFirstMappedPort() + "/";


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        EARLIER_DATE = ZonedDateTime.parse("2023-08-18T15:38:31+0000", formatter).toInstant().toEpochMilli();
        REPO_DATE = ZonedDateTime.parse("2024-06-26T10:23:31+0000", formatter).toInstant().toEpochMilli();
        LATER_DATE = ZonedDateTime.parse("2025-04-12T20:53:31+0000", formatter).toInstant().toEpochMilli();
    }

    @BeforeEach
    void setUp() throws IOException {
        System.out.println("[ >>> ] nginxUrl: " + this.nginxUrl);
        localGraphsRootPath = TEMP_DIR.resolve("graphs");
        createDirectories(localGraphsRootPath);
        localGraphPath = String.join("/", localGraphsRootPath.toAbsolutePath().toString(), LOCAL_PROFILE_NAME);
        localGraphDir = new File(localGraphPath);
        localGraphDir.mkdirs();
    }

    @AfterEach
    // Delete files of the local graphs root directory, not the files in the repository!
    void deleteFiles() throws IOException {
        FileUtils.deleteDirectory(localGraphsRootPath.toFile());
    }

    // Setup with actual graph version, where data is available in the repository.
    private void setupORSGraphManagerWithRepoData() {
        setupORSGraphManager(GRAPHS_VERSION);
    }

    // Setup with actual graph version, where no data is available in the repository.
    private void setupORSGraphManagerNoRepoData() {
        setupORSGraphManager(GRAPHS_VERSION_NO_REPO_DATA);
    }

    private void setupORSGraphManager(String graphsVersion) {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withLocalProfileName(LOCAL_PROFILE_NAME)
                .withRepoName(REPO_NAME)
                .withRepoProfileGroup(REPO_PROFILE_GROUP)
                .withRepoCoverage(REPO_COVERAGE)
                .withEncoderName(ENCODER_NAME)
                .withLocalGraphsRootAbsPath(localGraphsRootPath.toString())
                .withRepoBaseUri(nginxUrl)
                .withGraphVersion(graphsVersion)
                .build();

        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        ORSGraphRepoStrategy repoStrategy = new NamedGraphsRepoStrategy(managementProps);
        orsGraphRepoManager = new HttpRepoManager(managementProps, repoStrategy, orsGraphFileManager);
    }

    private void setupActiveGraphDirectory(Long osmDateLocal) throws IOException {
        ORSGraphInfoV1 activeGraphInfoV1Object = new ORSGraphInfoV1(new Date(osmDateLocal));
        localGraphInfoV1File = orsGraphFileManager.getActiveGraphInfoFile();
        ORSGraphFileManager.writeOrsGraphInfoV1(activeGraphInfoV1Object, localGraphInfoV1File);
    }

    private static void printFileContent(String label, File file) throws IOException {
        String content = readFileToString(file, "UTF-8");
        System.out.printf("[ >>> ] %s:%n>>>>>>>>>>>>>>>>>>%n%s%n<<<<<<<<<<<<<<<<<<<<%n", label, content);
    }

    private static void printValue(Object value) {
        System.out.printf("[ >>> ] %s%n", value);
    }

    private static void printValue(String label, Object value) {
        System.out.printf("[ >>> ] %s: %s%n", label, value);
    }

    @Test
    void concatenateToUrl() {
        assertEquals("https://my.domain.com/repo/group1/germany/0/graph.ghz",
                HttpRepoManager.concatenateToUrlPath("https://my.domain.com", "repo", "/group1", "/germany/", "0", "./", "graph.ghz/"));
    }

    @Test
    void createDownloadUrl() {
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withRepoBaseUri("http://localhost:8080/")
                .withRepoName("repo")
                .withRepoProfileGroup("group1")
                .withRepoCoverage("germany")
                .withGraphVersion("0")
                .build();

        FlatORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        ORSGraphRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        HttpRepoManager httpRepoManager = new HttpRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager);
        URL downloadUrl = httpRepoManager.createDownloadUrl("graph.ghz");
        assertEquals("http://localhost:8080/repo/group1/germany/0/graph.ghz", downloadUrl.toString());
    }

    @Test
    void checkNginx() throws IOException {
        Path path = Path.of("src/test/resources/test-filesystem-repos");
        printValue("path to test-filesystem-repos", path.toAbsolutePath());
        assertNotNull(this.nginxUrl);
        URL url = new URL(this.nginxUrl + "vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.yml");
        printValue("fileUrl", url);
        File file = Files.newTemporaryFile();
        assertEquals(0, file.length());
        FileUtils.copyURLToFile(url, file);
        assertTrue(file.length() > 0);
        printFileContent("downloaded file content", file);
    }

    @Test
    void checkSetupActiveGraphDirectory() throws IOException {
        setupORSGraphManagerWithRepoData();
        setupActiveGraphDirectory(EARLIER_DATE);
        File activeGraphDirectory = orsGraphFileManager.getActiveGraphDirectory();
        printValue("content of activeGraphDirectory:");
        for (File file : activeGraphDirectory.listFiles()) {
            printValue(file.getAbsolutePath());
        }
        File activeGraphInfoFile = orsGraphFileManager.getActiveGraphInfoFile();
        assertTrue(activeGraphInfoFile.exists());
        printFileContent("activeGraphInfoFile", activeGraphInfoFile);
        String content = readFileToString(activeGraphInfoFile, "UTF-8");
        assertTrue(content.contains("importDate: 2023-08-18T15:38:31+0000"));
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDataExists_noRemoteData() throws IOException {
        setupORSGraphManagerNoRepoData();
        setupActiveGraphDirectory(EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertFalse(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_noLocalData_remoteDataExists() {
        setupORSGraphManagerWithRepoData();

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_localDate_before_remoteDate() throws IOException {
        setupORSGraphManagerWithRepoData();
        setupActiveGraphDirectory(EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_equals_remoteDate() throws IOException {
        setupORSGraphManagerWithRepoData();
        setupActiveGraphDirectory(REPO_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_after_remoteDate() throws IOException {
        setupORSGraphManagerWithRepoData();
        setupActiveGraphDirectory(LATER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

}