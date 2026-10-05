package org.heigit.ors.routing.graphhopper.extensions.manage;

import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.FileSystemGraphRepoClient;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.NamedGraphsRepoStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.ORSGraphRepoStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;

class ORSGraphManagerTest {

    private static final String LOCAL_PROFILE_NAME = "truck";
    private static final String ENCODER_NAME = "driving-hgv";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private Path tempDir;

    private Path localGraphsRootPath;
    private FileSystemGraphRepoClient fileSystemGraphRepoClient;
    private ORSGraphFolderStrategy orsGraphFolderStrategy;
    private ORSGraphRepoStrategy orsGraphRepoStrategy;
    private ORSGraphFileManager orsGraphFileManager;
    private ORSGraphManager orsGraphManager;

    @BeforeEach
    public void setUp() throws IOException {
        localGraphsRootPath = createLocalGraphsRootDirectory(tempDir);
    }

    @AfterEach
    void deleteFiles() throws IOException {
        cleanupLocalGraphsRootDirectory(localGraphsRootPath);
    }

    private GraphManagementRuntimeProperties.Builder defaultProps() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME);
    }

    private void createContext(GraphManagementRuntimeProperties.Builder managementPropsBuilder) {
        GraphManagementRuntimeProperties managementProps = managementPropsBuilder.build();

        orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();

        orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);
        fileSystemGraphRepoClient = new FileSystemGraphRepoClient(managementProps, orsGraphRepoStrategy, orsGraphFileManager);

        orsGraphManager = new ORSGraphManager(managementProps, orsGraphFileManager, fileSystemGraphRepoClient);
    }

    private void createActiveGraph(String graphVersion, Long importDate) throws IOException {
        createLocalGraph(orsGraphFileManager.getActiveGraphDirName(),
                ofNullable(importDate).orElse(LATER_DATE),
                EARLIER_DATE,
                graphVersion);
    }
    private void createExtractedGraph(String graphVersion, Long importDate) throws IOException {
        createLocalGraph(orsGraphFileManager.getDownloadedExtractedGraphDirName(),
                ofNullable(importDate).orElse(LATER_DATE),
                EARLIER_DATE,
                graphVersion);
    }
    private void createLocalGraph(String dirName, Long importDate, Long osmDate, String graphVersion) throws IOException {
        createLocalGraphDirectoryWithGraphBuildInfoFile(
                localGraphsRootPath,
                dirName,
                orsGraphFolderStrategy.getActiveGraphBuildInfoFileName(),
                importDate,
                osmDate,
                graphVersion
        );
    }

    @Test
    void manageStartup_disabled_noLocalGraphs(){
        createContext(defaultProps().withEnabled(false).withGraphVersion(REPO_GRAPHS_VERSION));
        assertThat(activeGraphExists()).isFalse();
        assertThat(downloadedGraphExists()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isFalse();
        assertThat(oldActiveGraphWasBackedUp()).isFalse();
        assertThat(activeGraphExists()).isFalse();
        assertThat(downloadedGraphExists()).isFalse();
    }

    @Test
    void manageStartup_enabled_noLocalGraphs(){
        createContext(defaultProps().withEnabled(true).withGraphVersion(REPO_GRAPHS_VERSION));
        assertThat(activeGraphExists()).isFalse();
        assertThat(downloadedGraphExists()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isTrue();
        assertThat(oldActiveGraphWasBackedUp()).isFalse();
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void manageStartup_graphsWithSameGraphVersion(boolean graphManagementEnabled) throws IOException {
        createContext(defaultProps().withGraphVersion(REPO_GRAPHS_VERSION).withEnabled(graphManagementEnabled));
        createActiveGraph(REPO_GRAPHS_VERSION, EARLIER_DATE);
        createExtractedGraph(REPO_GRAPHS_VERSION, EARLIER_DATE);
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isTrue();
        assertThat(repoLookupOccurred()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isFalse();
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isFalse();
    }

    @Test
    void manageStartup_enabled_graphsWithDifferentGraphVersion() throws IOException {
        createContext(defaultProps().withEnabled(true).withGraphVersion(REPO_GRAPHS_VERSION).withMaxNumberOfGraphBackups(1));
        createActiveGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        createExtractedGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isTrue();
        assertThat(repoLookupOccurred()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isTrue();
        assertThat(oldActiveGraphWasBackedUp()).isTrue();
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isFalse();
    }

    @Test
    void manageStartup_disabled_graphsWithDifferentGraphVersion() throws IOException {
        createContext(defaultProps().withEnabled(false).withGraphVersion(REPO_GRAPHS_VERSION).withMaxNumberOfGraphBackups(1));
        createActiveGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        createExtractedGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isTrue();
        assertThat(repoLookupOccurred()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isFalse();
        assertThat(oldActiveGraphWasBackedUp()).isTrue();
        assertThat(activeGraphExists()).isFalse();
        assertThat(downloadedGraphExists()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void manageStartup_incompatibleActiveAndCompatibleDownloadedLocalGraphs(boolean graphManagementEnabled) throws IOException {
        createContext(defaultProps().withEnabled(graphManagementEnabled).withGraphVersion(REPO_GRAPHS_VERSION).withMaxNumberOfGraphBackups(1));
        createActiveGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        createExtractedGraph(REPO_GRAPHS_VERSION, EARLIER_DATE);
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isTrue();
        assertThat(repoLookupOccurred()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isFalse();
        assertThat(oldActiveGraphWasBackedUp()).isTrue();
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isFalse();
    }

    // Very unlikely that this ever happens...
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void manageStartup_compatibleActiveAndIncompatibleDownloadedLocalGraphs(boolean graphManagementEnabled) throws IOException {
        createContext(defaultProps().withEnabled(graphManagementEnabled).withGraphVersion(REPO_GRAPHS_VERSION).withMaxNumberOfGraphBackups(1));
        createActiveGraph(REPO_GRAPHS_VERSION, EARLIER_DATE);
        createExtractedGraph(REPO_NONEXISTING_GRAPHS_VERSION, EARLIER_DATE);
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isTrue();
        assertThat(repoLookupOccurred()).isFalse();

        orsGraphManager.manageStartup();

        assertThat(repoLookupOccurred()).isFalse();
        assertThat(oldActiveGraphWasBackedUp()).isFalse();
        assertThat(activeGraphExists()).isTrue();
        assertThat(downloadedGraphExists()).isFalse();
    }

    private boolean oldActiveGraphWasBackedUp() {
        return !orsGraphFileManager.findGraphBackupsSortedByName().isEmpty();
    }
    private boolean activeGraphExists() {
        return orsGraphFileManager.hasActiveGraph();
    }
    private boolean repoLookupOccurred() {
        return orsGraphFileManager.getDownloadedGraphBuildInfoFile().exists();
    }
    private boolean downloadedGraphExists() {
        return orsGraphFileManager.hasDownloadedExtractedGraph();
    }

    @ParameterizedTest
    @CsvSource({
            "true,  true,  repoName, http://my.domain.com",
            "false, true,  repoName,                     ",
            "false, true,          , http://my.domain.com",
            "false, false, repoName, http://my.domain.com",
    })
    void useGraphRepository(boolean expectUseRepo, boolean enable, String repoName, String baseUri) {
        createContext(GraphManagementRuntimeProperties.Builder.empty()
                .withEnabled(enable)
                .withRepoName(repoName)
                .withRepoBaseUri(baseUri)
                .withLocalGraphsRootAbsPath("target/test-output/graphs")
                .withLocalProfileName("useGraphRepository"));
        assertThat(orsGraphManager).isNotNull();
        assertThat(expectUseRepo).isEqualTo(orsGraphManager.useGraphRepository());
    }

    @ParameterizedTest
    @CsvSource({
            "HttpGraphRepoClient, http://my.domain.com",
            "HttpGraphRepoClient, https://my.domain.com/",
            "NullGraphRepoClient, file:relative/path",
            "NullGraphRepoClient, file://relative/path",
            "NullGraphRepoClient, file://relative/path.txt",
            "FileSystemGraphRepoClient, file:///absolute/path",
            "FileSystemGraphRepoClient, file:///absolute/path.txt",
            "FileSystemGraphRepoClient, relative/path",
            "FileSystemGraphRepoClient, relative/path.txt",
            "FileSystemGraphRepoClient, /absolute/path",
            "FileSystemGraphRepoClient, /absolute/path.txt",
            "FileSystemGraphRepoClient, ~/absolute/path",
            "FileSystemGraphRepoClient, ~/absolute/path.txt",
            "S3GraphRepoClient, s3:http://my.domain.com",
            "S3GraphRepoClient, s3:https://my.domain.com/",
            //Still supported for backwards compatibility:
            "S3GraphRepoClient, minio:http://my.domain.com",
            "S3GraphRepoClient, minio:https://my.domain.com/",
    })
    void getOrsGraphRepoClient(String className, String repoUri) {
        GraphManagementRuntimeProperties.Builder managementPropsBuilder = GraphManagementRuntimeProperties.Builder.empty()
                .withLocalGraphsRootAbsPath("graphs")
                .withRepoName("myS3Repo")
                .withRepoCoverage("lummerland")
                .withRepoBaseUri(repoUri)
                .withGraphVersion("1")
                .withRepoUser("user")
                .withRepoPass("pw")
                .withLocalProfileName("driving-car");
        createContext(managementPropsBuilder);
        assertThat(ORSGraphManager.getOrsGraphRepoClient(managementPropsBuilder.build(), orsGraphRepoStrategy, orsGraphFileManager).getClass().getSimpleName())
                .isEqualTo(className);
    }
}