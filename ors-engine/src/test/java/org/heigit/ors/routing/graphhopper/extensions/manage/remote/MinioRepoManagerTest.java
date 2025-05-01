package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
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
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MinioRepoManagerTest {
    private static final String LOCAL_PROFILE_NAME = "driving-car";
    private static final String ENCODER_NAME = "driving-car";
    private static final String BUCKET_NAME = "vendor-xyz";
    private static final Path TESTFILE_ROOT = Path.of("src/test/resources/test-filesystem-repos/" + BUCKET_NAME);
    private static Path localGraphsRootPath;
    private static MinIOContainer minioContainer;

    static {
        minioContainer = new MinIOContainer("minio/minio:RELEASE.2025-04-22T22-12-26Z");
        minioContainer.start();
    }

    @BeforeAll
    static void setupRepo() throws Exception {
        try (MinioClient minioClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build()) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());

            try (Stream<Path> stream = Files.walk(TESTFILE_ROOT)) {
                stream.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        minioClient.putObject(PutObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(TESTFILE_ROOT.relativize(path).toString())
                                .stream(new FileInputStream(path.toFile()), path.toFile().length(), -1)
                                .build());
                    } catch (ErrorResponseException | InsufficientDataException | InternalException |
                             InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                             ServerException | XmlParserException e) {
                        throw new RuntimeException(e);
                    }
                });
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
        return new MinioGraphRepoClient(managementProps, repoStrategy, orsGraphFileManager);
    }

    private static GraphManagementRuntimeProperties.Builder managementPropsBuilder() {
        return createGraphManagementRuntimePropertiesBuilder(localGraphsRootPath, LOCAL_PROFILE_NAME, ENCODER_NAME)
                .withRepoBaseUri("minio:" + minioContainer.getS3URL())
                .withRepoUser(minioContainer.getUserName())
                .withRepoPass(minioContainer.getPassword());
    }

    private void setupActiveGraphDirectory(Long osmDateLocal, ORSGraphFileManager orsGraphFileManager) {
        saveActiveGraphBuildInfoFile(orsGraphFileManager.getActiveGraphBuildInfoFile(), osmDateLocal, null);
    }

    @SneakyThrows
    @Test
    void checkRepo() throws Exception {
        try (MinioClient minioClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build()) {
            List<Bucket> buckets = minioClient.listBuckets();
            assertEquals(1, buckets.size());
            assertEquals(BUCKET_NAME, buckets.get(0).name());
            List<String> expected = List.of(
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.ghz",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-car.yml",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.ghz",
                    "fastisochrones/heidelberg/1/fastisochrones_heidelberg_1_driving-hgv.yml"
            );
            List<String> actual = new ArrayList<>();
            for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder().bucket(BUCKET_NAME).prefix("fastisochrones/heidelberg/1/").build())) {
                actual.add(itemResult.get().objectName());
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