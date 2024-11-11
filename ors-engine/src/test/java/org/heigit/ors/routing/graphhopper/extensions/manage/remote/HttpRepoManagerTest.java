package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
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

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
class HttpRepoManagerTest {

    private static final String LOCAL_PROFILE_NAME = "driving-car";
    private static final String ENCODER_NAME = "driving-car";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private static Path tempDir;
    @Container
    private static NginxContainer nginx;
    private static String nginxUrl;
    private static Path localGraphsRootPath;

    private HttpRepoManager orsGraphRepoManager;
    private ORSGraphFileManager orsGraphFileManager;

    static {
        DockerImageName dockerImageName = DockerImageName.parse("nginx:1.23.4-alpine");
        MountableFile mountableFile = MountableFile.forHostPath("src/test/resources/test-filesystem-repos/");
        nginx = new NginxContainer<>(dockerImageName)
                .withCopyFileToContainer(mountableFile, "/usr/share/nginx/html")
                .waitingFor(new HttpWaitStrategy());
        nginx.start();
        nginxUrl = "http://" + nginx.getHost() + ":" + nginx.getFirstMappedPort() + "/";
    }

    @BeforeEach
    void setUp() throws IOException {
        localGraphsRootPath = createLocalGraphsRootDirectory(tempDir);
        createLocalGraphDirectory(localGraphsRootPath, LOCAL_PROFILE_NAME);
    }

    @AfterEach
        // Delete files of the local graphs root directory, not the files in the repository!
    void deleteFiles() throws IOException {
        cleanupLocalGraphsRootDirectory(localGraphsRootPath);
    }

    private void setupORSGraphManager(GraphManagementRuntimeProperties managementProps) {
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        ORSGraphRepoStrategy repoStrategy = new NamedGraphsRepoStrategy(managementProps);
        orsGraphRepoManager = new HttpRepoManager(managementProps, repoStrategy, orsGraphFileManager);
    }

    private static GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME)
                .withRepoBaseUri(nginxUrl);
    }

    private void setupActiveGraphDirectory(Long osmDateLocal) {
        saveActiveGraphInfoFile(orsGraphFileManager.getActiveGraphInfoFile(), osmDateLocal, null);
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
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
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
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
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
        assertTrue(content.contains("graph_build_date: 2023-08-18T15:38:31+0000"));
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDataExists_noRemoteData() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_NONEXISTING_GRAPHS_VERSION).build());
        setupActiveGraphDirectory(EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertFalse(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_noLocalData_remoteDataExists() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_localDate_before_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        setupActiveGraphDirectory(EARLIER_DATE);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_equals_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        setupActiveGraphDirectory(repoCarGraphBuildDate);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_after_remoteDate() {
        setupORSGraphManager(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build());
        setupActiveGraphDirectory(repoCarGraphBuildDate + 1000000);

        orsGraphRepoManager.downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphFileManager.getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphFileManager.getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

}