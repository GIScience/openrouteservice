package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.util.Objects;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HttpRepoManagerTest {

    private static final String LOCAL_PROFILE_NAME = "driving-car";
    private static final String ENCODER_NAME = "driving-car";

    @Container
    private static final NginxContainer<?> NGINX;
    private static final String NGINX_URL;
    private static Path localGraphsRootPath;

    static {
        DockerImageName dockerImageName = DockerImageName.parse("nginx:1.23.4-alpine");
        MountableFile mountableFile = MountableFile.forHostPath("src/test/resources/test-filesystem-repos/");
        NGINX = new NginxContainer<>(dockerImageName)
                .withCopyFileToContainer(mountableFile, "/usr/share/nginx/html")
                .waitingFor(new HttpWaitStrategy());
        NGINX.start();
        NGINX_URL = "http://" + NGINX.getHost() + ":" + NGINX.getFirstMappedPort() + "/";
    }

    @BeforeEach
    void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path tempDir) throws IOException {
        localGraphsRootPath = createLocalGraphsRootDirectory(tempDir);
        createLocalGraphDirectory(localGraphsRootPath, LOCAL_PROFILE_NAME);
    }

    @AfterEach
        // Delete files of the local graphs root directory, not the files in the repository!
    void deleteFiles() throws IOException {
        cleanupLocalGraphsRootDirectory(localGraphsRootPath);
    }


    @AllArgsConstructor
    @Getter
    static class OrsGraphHelper {
        ORSGraphFileManager orsGraphFileManager;
        ORSGraphRepoManager orsGraphRepoManager;

    }

    private OrsGraphHelper setupOrsGraphHelper(GraphManagementRuntimeProperties graphManagementRuntimeProperties, Long timeVariable) {
        ORSGraphFileManager orsGraphFileManager = setupORSGraphFileManager(graphManagementRuntimeProperties);
        if (timeVariable != null)
            setupActiveGraphDirectory(timeVariable, orsGraphFileManager);
        ORSGraphRepoManager orsGraphRepoManager = setupOrsGraphRepoManager(graphManagementRuntimeProperties, orsGraphFileManager);
        return new OrsGraphHelper(orsGraphFileManager, orsGraphRepoManager);
    }

    private ORSGraphFileManager setupORSGraphFileManager(GraphManagementRuntimeProperties managementProps) {
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        return orsGraphFileManager;
    }

    private ORSGraphRepoManager setupOrsGraphRepoManager(GraphManagementRuntimeProperties managementProps, ORSGraphFileManager orsGraphFileManager) {
        ORSGraphRepoStrategy repoStrategy = new NamedGraphsRepoStrategy(managementProps);
        return new HttpRepoManager(managementProps, repoStrategy, orsGraphFileManager);
    }

    private static GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME)
                .withRepoBaseUri(NGINX_URL);
    }

    private void setupActiveGraphDirectory(Long osmDateLocal, ORSGraphFileManager orsGraphFileManager) {
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
        assertNotNull(NGINX_URL);
        URL url = new URL(NGINX_URL + "vendor-xyz/fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.yml");
        printValue("fileUrl", url);
        File file = Files.newTemporaryFile();
        assertEquals(0, file.length());
        FileUtils.copyURLToFile(url, file);
        assertTrue(file.length() > 0);
        printFileContent("downloaded file content", file);
    }

    @Test
    void checkSetupActiveGraphDirectory() throws IOException {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), EARLIER_DATE);
        File activeGraphDirectory = orsGraphHelper.getOrsGraphFileManager().getActiveGraphDirectory();
        printValue("content of activeGraphDirectory:");
        for (File file : Objects.requireNonNull(activeGraphDirectory.listFiles())) {
            printValue(file.getAbsolutePath());
        }
        File activeGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getActiveGraphInfoFile();
        assertTrue(activeGraphInfoFile.exists());
        printFileContent("activeGraphInfoFile", activeGraphInfoFile);
        String content = readFileToString(activeGraphInfoFile, "UTF-8");
        assertTrue(content.contains("graph_build_date: 2023-08-18T15:38:31+0000"));
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDataExists_noRemoteData() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_NONEXISTING_GRAPHS_VERSION).build(), EARLIER_DATE);

        orsGraphHelper.getOrsGraphRepoManager().downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertFalse(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_noLocalData_remoteDataExists() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), null);

        orsGraphHelper.getOrsGraphRepoManager().downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_localDate_before_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), EARLIER_DATE);

        orsGraphHelper.getOrsGraphRepoManager().downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_equals_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), REPO_CAR_GRAPH_BUILD_DATE);

        orsGraphHelper.getOrsGraphRepoManager().downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_after_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), REPO_CAR_GRAPH_BUILD_DATE + 1000000);

        orsGraphHelper.getOrsGraphRepoManager().downloadGraphIfNecessary();

        File downloadedGraphInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

}