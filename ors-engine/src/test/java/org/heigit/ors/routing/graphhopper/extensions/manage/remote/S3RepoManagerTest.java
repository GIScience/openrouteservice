package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFolderStrategy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class S3RepoManagerTest {
    private static final String LOCAL_PROFILE_NAME = "driving-car";
    private static final String ENCODER_NAME = "driving-car";
    private static final String BUCKET_NAME = "vendor-xyz";
    private static final Path TESTFILE_ROOT = Path.of("src/test/resources/test-filesystem-repos/" + BUCKET_NAME);
    private static final String ACCESS_KEY = "ors-test-access-key";
    private static final String SECRET_KEY = "ors-test-secret-key";
    private static final int RUSTFS_S3_PORT = 9000;
    private static Path localGraphsRootPath;

    @Container
    private static final GenericContainer<?> RUSTFS =
            new GenericContainer<>(DockerImageName.parse("rustfs/rustfs:1.0.0"))
                    .withExposedPorts(RUSTFS_S3_PORT)
                    .withEnv("RUSTFS_ACCESS_KEY", ACCESS_KEY)
                    .withEnv("RUSTFS_SECRET_KEY", SECRET_KEY)
                    // no console needed for the S3 API tests; keeps port 9001 unbound
                    .withEnv("RUSTFS_CONSOLE_ENABLE", "false")
                    // empty => entrypoint routes server logs to stdout for Testcontainers to capture
                    .withEnv("RUSTFS_OBS_LOG_DIRECTORY", "")
                    .waitingFor(Wait.forHttp("/health")
                            .forPort(RUSTFS_S3_PORT)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofSeconds(120)));

    private static String s3Url() {
        return "http://%s:%d".formatted(RUSTFS.getHost(), RUSTFS.getMappedPort(RUSTFS_S3_PORT));
    }

    private static S3Client createS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(s3Url()))
                .region(Region.US_EAST_1) // RustFS default region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                        )
                )
                .forcePathStyle(true)
                .build();
    }

    @BeforeAll
    static void setupRepo() throws Exception {
        try (S3Client s3Client = createS3Client()) {

            s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());

            try (Stream<Path> stream = Files.walk(TESTFILE_ROOT)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(BUCKET_NAME)
                                    .key(TESTFILE_ROOT.relativize(path).toString())
                                    .build(),
                            path
                    );
                });
            } catch(RuntimeException r) {
                System.out.println("RE " + r);
            } catch(Exception e) {
                System.out.println("EX " + e);
            }
        }
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
        ORSGraphRepoClient orsGraphRepoClient;
    }

    private OrsGraphHelper setupOrsGraphHelper(GraphManagementRuntimeProperties graphManagementRuntimeProperties, Long timeVariable) {
        ORSGraphFileManager orsGraphFileManager = setupORSGraphFileManager(graphManagementRuntimeProperties);
        if (timeVariable != null)
            setupActiveGraphDirectory(timeVariable, orsGraphFileManager);
        ORSGraphRepoClient orsGraphRepoClient = setupOrsGraphRepoManager(graphManagementRuntimeProperties, orsGraphFileManager);
        return new OrsGraphHelper(orsGraphFileManager, orsGraphRepoClient);
    }

    private ORSGraphFileManager setupORSGraphFileManager(GraphManagementRuntimeProperties managementProps) {
        ORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        orsGraphFileManager.initialize();
        return orsGraphFileManager;
    }

    private ORSGraphRepoClient setupOrsGraphRepoManager(GraphManagementRuntimeProperties managementProps, ORSGraphFileManager orsGraphFileManager) {
        ORSGraphRepoStrategy repoStrategy = new NamedGraphsRepoStrategy(managementProps);
        return new S3GraphRepoClient(managementProps, repoStrategy, orsGraphFileManager);
    }

    private static GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME)
                .withRepoBaseUri("s3:" + s3Url())
                .withRepoUser(ACCESS_KEY)
                .withRepoPass(SECRET_KEY);
    }

    private void setupActiveGraphDirectory(Long osmDateLocal, ORSGraphFileManager orsGraphFileManager) {
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), osmDateLocal, null);
    }

    @SneakyThrows
    @Test
    void checkRepo() throws Exception {
        try (S3Client s3Client = createS3Client()) {
            List<Bucket> buckets = s3Client.listBuckets().buckets();
            assertEquals(1, buckets.size());
            assertEquals(BUCKET_NAME, buckets.get(0).name());
            List<String> expected = List.of(
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.ghz",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.yml",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.ghz",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.yml"
            );

            List<String> actual = new ArrayList<>();
            for (S3Object itemResult : s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(BUCKET_NAME).build()).contents()) {
                actual.add(itemResult.key());
            }
            assertTrue(actual.containsAll(expected) && expected.containsAll(actual));
        }
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDataExists_noRemoteData() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_NONEXISTING_GRAPHS_VERSION).build(), EARLIER_DATE);

        orsGraphHelper.getOrsGraphRepoClient().downloadGraphIfNecessary();

        File downloadedGraphBuildInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertFalse(downloadedGraphBuildInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_noLocalData_remoteDataExists() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), null);

        orsGraphHelper.getOrsGraphRepoClient().downloadGraphIfNecessary();

        File downloadedGraphBuildInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphBuildInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_downloadWhen_localDate_before_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), EARLIER_DATE);

        orsGraphHelper.getOrsGraphRepoClient().downloadGraphIfNecessary();

        File downloadedGraphBuildInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphBuildInfoFile.exists());
        assertTrue(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_equals_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), REPO_CAR_GRAPH_BUILD_DATE);

        orsGraphHelper.getOrsGraphRepoClient().downloadGraphIfNecessary();

        File downloadedGraphBuildInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphBuildInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }

    @Test
    void downloadGraphIfNecessary_noDownloadWhen_localDate_after_remoteDate() {
        OrsGraphHelper orsGraphHelper = setupOrsGraphHelper(managementPropsBuilder().withGraphVersion(REPO_GRAPHS_VERSION).build(), REPO_CAR_GRAPH_BUILD_DATE + 1000000);

        orsGraphHelper.getOrsGraphRepoClient().downloadGraphIfNecessary();

        File downloadedGraphBuildInfoFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedGraphBuildInfoFile();
        File downloadedCompressedGraphFile = orsGraphHelper.getOrsGraphFileManager().getDownloadedCompressedGraphFile();
        assertTrue(downloadedGraphBuildInfoFile.exists());
        assertFalse(downloadedCompressedGraphFile.exists());
    }
}