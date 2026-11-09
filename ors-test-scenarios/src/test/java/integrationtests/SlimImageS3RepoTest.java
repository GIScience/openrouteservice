package integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the slim image end to end against an S3 graph repository: builds the apitests profiles in preparation mode,
 * serves the archives from RustFS and checks that a fresh slim container downloads and loads all of them.
 * <p>
 * Unlike the other scenarios this runs the slim target of the root Dockerfile rather than a builder image, because the slim
 * image ships its own jlink'ed Java runtime. The image cannot be built from here: the Dockerfile needs BuildKit,
 * which Testcontainers does not use. Build it first, or point {@code container.slim.image} at an existing one.
 */
@Testcontainers(disabledWithoutDocker = true)
@EnabledIf("slimImageRequestedOrPresent")
class SlimImageS3RepoTest {
    private static final String SLIM_IMAGE_PROPERTY = "container.slim.image";
    private static final String SLIM_IMAGE = System.getProperty(SLIM_IMAGE_PROPERTY, "local/openrouteservice:test-slim");
    private static final DockerImageName RUSTFS_IMAGE = DockerImageName.parse("rustfs/rustfs:1.0.0");

    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path TEST_CONFIG = REPO_ROOT.resolve("ors-api/src/test/resources/application-test.yml");
    private static final Path TEST_FILES = REPO_ROOT.resolve("ors-api/src/test/files");

    private static final String ORS_HOME = "/home/ors";
    private static final int ORS_PORT = 8082;
    private static final String RUSTFS_ALIAS = "rustfs";
    private static final int RUSTFS_PORT = 9000;
    private static final String ACCESS_KEY = "ors-test-access-key";
    private static final String SECRET_KEY = "ors-test-secret-key";
    private static final String BUCKET = "ors-graphs";
    private static final String PROFILE_GROUP = "apitests";
    private static final String GRAPH_EXTENT = "heidelberg";
    // The apitests profiles that are not built and loaded here.
    private static final List<String> DISABLED_PROFILES = List.of("public-transport", "driving-car-no-preparations");

    private static final Network NETWORK = Network.newNetwork();

    // The Testcontainers extension starts and stops it; the with* chain hides that from the compiler's resource analysis.
    @SuppressWarnings("resource")
    @Container
    private static final GenericContainer<?> RUSTFS = new GenericContainer<>(RUSTFS_IMAGE)
            .withNetwork(NETWORK)
            .withNetworkAliases(RUSTFS_ALIAS)
            .withExposedPorts(RUSTFS_PORT)
            .withEnv("RUSTFS_ACCESS_KEY", ACCESS_KEY)
            .withEnv("RUSTFS_SECRET_KEY", SECRET_KEY)
            .withEnv("RUSTFS_CONSOLE_ENABLE", "false")
            .withEnv("RUSTFS_OBS_LOG_DIRECTORY", "")
            .waitingFor(Wait.forHttp("/health").forPort(RUSTFS_PORT).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));

    @TempDir
    static Path tempDir;
    private static Path elevationCache;
    private static Path builtGraphs;
    private static Path downloadedGraphs;
    // The host user in group 0, so the test can clean up what the containers write and the image's group permissions apply.
    private static String containerUser;
    private static Map<String, String> encoderByProfile;
    private static String graphVersion;

    @BeforeAll
    static void buildAndUploadGraphs() throws IOException {
        assertTrue(imageExists(SLIM_IMAGE), "Image %s not found. Build it with `docker build --target slim -t %s .` from the repository root, or set -D%s."
                .formatted(SLIM_IMAGE, SLIM_IMAGE, SLIM_IMAGE_PROPERTY));
        prepareDirectories();
        buildGraphs();
        uploadGraphs();
    }

    @Test
    void slimImageLoadsAllApitestsProfilesFromS3Repository() throws Exception {
        try (GenericContainer<?> ors = s3OrsContainer()) {
            ors.start();
            assertHealthReady(ors);
            assertEveryProfileDownloaded();
        }
    }

    /**
     * Enables the test when an image was asked for explicitly, or when the default image exists locally. A plain
     * -P integrationTests run without the image skips it; an explicitly requested but missing image fails in
     * {@link #buildAndUploadGraphs()}.
     */
    static boolean slimImageRequestedOrPresent() {
        return System.getProperty(SLIM_IMAGE_PROPERTY) != null
                || DockerClientFactory.instance().isDockerAvailable() && imageExists(SLIM_IMAGE);
    }

    private static boolean imageExists(String image) {
        try {
            DockerClientFactory.instance().client().inspectImageCmd(image).exec();
            return true;
        } catch (NotFoundException _) {
            return false;
        }
    }

    private static void prepareDirectories() throws IOException {
        encoderByProfile = enabledApitestsProfiles();
        elevationCache = Files.createDirectories(tempDir.resolve("elevation_cache"));
        Files.copy(TEST_FILES.resolve("elevation/srtm_38_03.gh"), elevationCache.resolve("srtm_38_03.gh"));
        builtGraphs = Files.createDirectories(tempDir.resolve("built-graphs"));
        downloadedGraphs = Files.createDirectories(tempDir.resolve("downloaded-graphs"));
        containerUser = "%s:0".formatted(Files.getAttribute(tempDir, "unix:uid"));
    }

    private static Map<String, String> enabledApitestsProfiles() throws IOException {
        JsonNode profiles = new ObjectMapper(new YAMLFactory()).readTree(TEST_CONFIG.toFile()).path("ors").path("engine").path("profiles");
        Map<String, String> encoders = new LinkedHashMap<>();
        profiles.properties().forEach(profile -> {
            JsonNode config = profile.getValue();
            if (config.path("enabled").asBoolean(false) && !DISABLED_PROFILES.contains(profile.getKey())) {
                encoders.put(profile.getKey(), config.path("encoder_name").asText(profile.getKey()));
            }
        });
        return encoders;
    }

    // Name preparation mode gives a graph's archive and build info, and the S3 client looks up.
    private static String repoFileName(String encoder, String extension) {
        return "%s_%s_%s_%s.%s".formatted(PROFILE_GROUP, GRAPH_EXTENT, graphVersion, encoder, extension);
    }

    private static Map<String, String> commonEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("JDK_JAVA_OPTIONS", "-Xmx4g");
        env.put("logging.level.org.heigit", "DEBUG");
        env.put("ors.engine.profile_default.graph_path", ORS_HOME + "/graphs");
        env.put("ors.engine.elevation.cache_path", ORS_HOME + "/elevation_cache");
        DISABLED_PROFILES.forEach(profile -> env.put("ors.engine.profiles.%s.enabled".formatted(profile), "false"));
        return env;
    }

    // The caller closes the container; the with* chain hides that from the compiler's resource analysis.
    @SuppressWarnings("resource")
    private static GenericContainer<?> slimContainer(Path graphs) {
        return new GenericContainer<>(DockerImageName.parse(SLIM_IMAGE))
                .withCreateContainerCmdModifier(cmd -> cmd.withUser(containerUser))
                .withFileSystemBind(TEST_CONFIG.toString(), ORS_HOME + "/ors-config.yml", BindMode.READ_ONLY)
                .withFileSystemBind(graphs.toString(), ORS_HOME + "/graphs", BindMode.READ_WRITE)
                .withFileSystemBind(elevationCache.toString(), ORS_HOME + "/elevation_cache", BindMode.READ_WRITE)
                .withEnv(commonEnv());
    }

    /**
     * Builds the graphs in preparation mode, which packs each one into a repository-ready .ghz archive plus .yml
     * build info, named {@code <profile group>_<graph extent>_<graph version>_<encoder>}, and exits.
     */
    private static void buildGraphs() throws IOException {
        try (GenericContainer<?> prep = slimContainer(builtGraphs)
                // application-test.yml refers to its data files as ./src/test/files/..., relative to ORS_HOME.
                .withFileSystemBind(TEST_FILES.toString(), ORS_HOME + "/src/test/files", BindMode.READ_ONLY)
                .withEnv("ors.engine.preparation_mode", "true")
                .withEnv("ors.engine.preparation_type", "ARCHIVE")
                .withEnv("ors.engine.profile_default.build.profile_group", PROFILE_GROUP)
                .withEnv("ors.engine.profile_default.build.graph_extent", GRAPH_EXTENT)
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofMinutes(10)))) {
            prep.start();
        }
        List<String> archives;
        try (Stream<Path> files = Files.list(builtGraphs)) {
            archives = files.map(p -> p.getFileName().toString()).filter(name -> name.endsWith(".ghz")).toList();
        }
        assertEquals(encoderByProfile.size(), archives.size(), "Expected one graph archive per profile %s, found %s".formatted(encoderByProfile.keySet(), archives));
        graphVersion = archives.getFirst().substring((PROFILE_GROUP + "_" + GRAPH_EXTENT + "_").length()).split("_", 2)[0];
    }

    /**
     * Uploads the archives to the layout S3GraphRepoClient reads: {@code <profile group>/<graph extent>/<graph version>/<file>}.
     */
    private static void uploadGraphs() throws IOException {
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://%s:%d".formatted(RUSTFS.getHost(), RUSTFS.getMappedPort(RUSTFS_PORT))))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .forcePathStyle(true)
                .build();
             Stream<Path> files = Files.list(builtGraphs)) {
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
            files.filter(Files::isRegularFile).forEach(file -> {
                String key = String.join("/", PROFILE_GROUP, GRAPH_EXTENT, graphVersion, file.getFileName().toString());
                s3.putObject(PutObjectRequest.builder().bucket(BUCKET).key(key).build(), file);
            });
        }
    }

    private static void assertHealthReady(GenericContainer<?> ors) throws IOException, InterruptedException {
        HttpResponse<String> health = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://%s:%d/ors/v2/health".formatted(ors.getHost(), ors.getMappedPort(ORS_PORT)))).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, health.statusCode());
        assertEquals("ready", new ObjectMapper().readTree(health.body()).path("status").asText());
    }

    private static void assertEveryProfileDownloaded() throws IOException {
        for (Map.Entry<String, String> profile : encoderByProfile.entrySet()) {
            Path loadedBuildInfo = downloadedGraphs.resolve(profile.getKey()).resolve("graph_build_info.yml");
            assertTrue(Files.isRegularFile(loadedBuildInfo), "No graph was loaded for profile %s".formatted(profile.getKey()));
            // A graph built locally would carry a new build date, so equal build info proves it came from the repository.
            assertEquals(Files.readString(builtGraphs.resolve(repoFileName(profile.getValue(), "yml"))), Files.readString(loadedBuildInfo),
                    "Graph for profile %s does not come from the S3 repository".formatted(profile.getKey()));
        }
        try (Stream<Path> files = Files.list(downloadedGraphs)) {
            List<String> leftovers = files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".ghz") || name.endsWith(".incomplete") || name.endsWith("_incomplete"))
                    .toList();
            assertEquals(List.of(), leftovers, "Downloads or extractions were not cleaned up");
        }
    }

    /**
     * Starts the slim image with graph management pointed at the S3 repository. The test data files are not
     * mounted, so the container cannot fall back to building graphs itself.
     */
    private static GenericContainer<?> s3OrsContainer() {
        return slimContainer(downloadedGraphs)
                .withNetwork(NETWORK)
                .withExposedPorts(ORS_PORT)
                .withEnv("server.port", String.valueOf(ORS_PORT))
                .withEnv("ors.engine.graph_management.enabled", "true")
                .withEnv("ors.engine.profile_default.repo.repository_uri", "s3:http://%s:%d".formatted(RUSTFS_ALIAS, RUSTFS_PORT))
                .withEnv("ors.engine.profile_default.repo.repository_name", BUCKET)
                .withEnv("ors.engine.profile_default.repo.repository_profile_group", PROFILE_GROUP)
                .withEnv("ors.engine.profile_default.repo.graph_extent", GRAPH_EXTENT)
                .withEnv("ors.engine.profile_default.repo.repository_user", ACCESS_KEY)
                .withEnv("ors.engine.profile_default.repo.repository_password", SECRET_KEY)
                .waitingFor(new HealthyOrExitedWaitStrategy().withStartupTimeout(Duration.ofMinutes(5)));
    }

    /**
     * Waits for the health endpoint to answer 200 like {@code Wait.forHttp}, but gives up as soon as the container
     * stops. ORS exits when graph loading fails, and there is nothing to wait for after that.
     */
    private static final class HealthyOrExitedWaitStrategy extends AbstractWaitStrategy {
        @Override
        protected void waitUntilReady() {
            URI health = URI.create("http://%s:%d/ors/v2/health".formatted(waitStrategyTarget.getHost(), waitStrategyTarget.getMappedPort(ORS_PORT)));
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            Callable<Boolean> healthy = () -> {
                try {
                    return client.send(HttpRequest.newBuilder(health).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
                } catch (IOException _) {
                    // Not listening yet.
                    return false;
                }
            };
            long deadline = System.nanoTime() + startupTimeout.toNanos();
            while (System.nanoTime() < deadline) {
                if (!waitStrategyTarget.isRunning()) {
                    throw new ContainerLaunchException("Container exited before %s answered 200".formatted(health));
                }
                try {
                    // The rate limiter paces the polling.
                    if (getRateLimiter().getWhenReady(healthy)) {
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ContainerLaunchException("Interrupted while waiting for %s".formatted(health), e);
                } catch (Exception e) {
                    throw new ContainerLaunchException("Failed to query %s".formatted(health), e);
                }
            }
            throw new ContainerLaunchException("Timed out after %s waiting for %s to answer 200".formatted(startupTimeout, health));
        }
    }
}
