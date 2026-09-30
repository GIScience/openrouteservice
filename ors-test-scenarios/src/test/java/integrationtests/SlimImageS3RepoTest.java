package integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the slim image end to end against an S3 graph repository: builds the apitests profiles in preparation mode,
 * serves the archives from RustFS and checks that a fresh slim container downloads and loads all of them.
 * <p>
 * Unlike the other scenarios this runs the slim target of the root Dockerfile rather than a builder image, because the slim
 * image ships its own jlink'ed Java runtime. The image cannot be built from here: the Dockerfile needs BuildKit,
 * which Testcontainers does not use. Build it first, or point {@code container.slim.image} at an existing one.
 * Without the property the test is skipped if the default image is missing.
 */
@Testcontainers(disabledWithoutDocker = true)
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

    @Test
    void slimImageLoadsApitestsGraphsFromS3Repository(@TempDir Path tempDir) throws Exception {
        boolean imageExists = imageExists(SLIM_IMAGE);
        String missingImage = "Image %s not found. Build it with `docker build --target slim -t %s .` from the repository root, or set -D%s.".formatted(SLIM_IMAGE, SLIM_IMAGE, SLIM_IMAGE_PROPERTY);
        // Skip a plain -P integrationTests run without the image, but fail when an image was asked for explicitly.
        if (System.getProperty(SLIM_IMAGE_PROPERTY) == null) {
            assumeTrue(imageExists, missingImage);
        } else {
            assertTrue(imageExists, missingImage);
        }

        List<String> expectedProfiles = enabledApitestsProfiles();
        Path elevationCache = Files.createDirectories(tempDir.resolve("elevation_cache"));
        Files.copy(TEST_FILES.resolve("elevation/srtm_38_03.gh"), elevationCache.resolve("srtm_38_03.gh"));
        Path builtGraphs = Files.createDirectories(tempDir.resolve("built-graphs"));
        Path downloadedGraphs = Files.createDirectories(tempDir.resolve("downloaded-graphs"));
        // Run as the host user so the test can clean up what the containers write, and in group 0 like the image expects.
        String user = "%s:0".formatted(Files.getAttribute(tempDir, "unix:uid"));

        buildGraphs(user, builtGraphs, elevationCache);
        List<Path> archives;
        try (Stream<Path> files = Files.list(builtGraphs)) {
            archives = files.filter(p -> p.getFileName().toString().endsWith(".ghz")).toList();
        }
        assertEquals(expectedProfiles.size(), archives.size(), "Expected one graph archive per profile %s, found %s".formatted(expectedProfiles, archives));

        try (Network network = Network.newNetwork();
             GenericContainer<?> rustfs = rustfsContainer(network)) {
            rustfs.start();
            uploadGraphs(rustfs, builtGraphs);

            try (GenericContainer<?> ors = s3OrsContainer(network, user, downloadedGraphs, elevationCache)) {
                ors.start();

                HttpResponse<String> health = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(URI.create("http://%s:%d/ors/v2/health".formatted(ors.getHost(), ors.getMappedPort(ORS_PORT)))).build(),
                        HttpResponse.BodyHandlers.ofString());
                assertEquals(200, health.statusCode());
                assertEquals("ready", new ObjectMapper().readTree(health.body()).path("status").asText());

                // Every profile must come from the repository. A graph built locally from the source file would hide a broken download.
                long downloads = ors.getLogs().lines().filter(l -> l.contains("Download of compressed graph file finished")).count();
                assertEquals(expectedProfiles.size(), downloads, "Expected every profile to be downloaded from the S3 repository");
            }
        }
    }

    private static boolean imageExists(String image) {
        try {
            DockerClientFactory.instance().client().inspectImageCmd(image).exec();
            return true;
        } catch (NotFoundException _) {
            return false;
        }
    }

    private static List<String> enabledApitestsProfiles() throws IOException {
        JsonNode profiles = new ObjectMapper(new YAMLFactory()).readTree(TEST_CONFIG.toFile()).path("ors").path("engine").path("profiles");
        List<String> enabled = new ArrayList<>();
        profiles.properties().forEach(profile -> {
            if (profile.getValue().path("enabled").asBoolean(false) && !DISABLED_PROFILES.contains(profile.getKey())) {
                enabled.add(profile.getKey());
            }
        });
        return enabled;
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
    private static GenericContainer<?> slimContainer(String user, Path graphs, Path elevationCache) {
        return new GenericContainer<>(DockerImageName.parse(SLIM_IMAGE))
                .withCreateContainerCmdModifier(cmd -> cmd.withUser(user))
                .withFileSystemBind(TEST_CONFIG.toString(), ORS_HOME + "/ors-config.yml", BindMode.READ_ONLY)
                .withFileSystemBind(graphs.toString(), ORS_HOME + "/graphs", BindMode.READ_WRITE)
                .withFileSystemBind(elevationCache.toString(), ORS_HOME + "/elevation_cache", BindMode.READ_WRITE)
                .withEnv(commonEnv());
    }

    /**
     * Builds the graphs in preparation mode, which packs each one into a repository-ready .ghz archive plus .yml
     * build info, named {@code <profile group>_<graph extent>_<graph version>_<encoder>}, and exits.
     */
    private static void buildGraphs(String user, Path graphs, Path elevationCache) {
        try (GenericContainer<?> prep = slimContainer(user, graphs, elevationCache)
                // application-test.yml refers to its data files as ./src/test/files/..., relative to ORS_HOME.
                .withFileSystemBind(TEST_FILES.toString(), ORS_HOME + "/src/test/files", BindMode.READ_ONLY)
                .withEnv("ors.engine.preparation_mode", "true")
                .withEnv("ors.engine.preparation_type", "ARCHIVE")
                .withEnv("ors.engine.profile_default.build.profile_group", PROFILE_GROUP)
                .withEnv("ors.engine.profile_default.build.graph_extent", GRAPH_EXTENT)
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofMinutes(10)))) {
            prep.start();
        }
    }

    // The caller closes the container; the with* chain hides that from the compiler's resource analysis.
    @SuppressWarnings("resource")
    private static GenericContainer<?> rustfsContainer(Network network) {
        return new GenericContainer<>(RUSTFS_IMAGE)
                .withNetwork(network)
                .withNetworkAliases(RUSTFS_ALIAS)
                .withExposedPorts(RUSTFS_PORT)
                .withEnv("RUSTFS_ACCESS_KEY", ACCESS_KEY)
                .withEnv("RUSTFS_SECRET_KEY", SECRET_KEY)
                .withEnv("RUSTFS_CONSOLE_ENABLE", "false")
                .withEnv("RUSTFS_OBS_LOG_DIRECTORY", "")
                .waitingFor(Wait.forHttp("/health").forPort(RUSTFS_PORT).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));
    }

    /**
     * Uploads the archives to the layout S3GraphRepoClient reads: {@code <profile group>/<graph extent>/<graph version>/<file>}.
     */
    private static void uploadGraphs(GenericContainer<?> rustfs, Path graphs) throws IOException {
        String prefix = PROFILE_GROUP + "_" + GRAPH_EXTENT + "_";
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://%s:%d".formatted(rustfs.getHost(), rustfs.getMappedPort(RUSTFS_PORT))))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .forcePathStyle(true)
                .build();
             Stream<Path> files = Files.list(graphs)) {
            s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
            files.filter(Files::isRegularFile).forEach(file -> {
                String name = file.getFileName().toString();
                String graphVersion = name.substring(prefix.length()).split("_", 2)[0];
                String key = String.join("/", PROFILE_GROUP, GRAPH_EXTENT, graphVersion, name);
                s3.putObject(PutObjectRequest.builder().bucket(BUCKET).key(key).build(), file);
            });
        }
    }

    /**
     * Starts the slim image with graph management pointed at the S3 repository. The test data files are not
     * mounted, so the container cannot fall back to building graphs itself.
     */
    private static GenericContainer<?> s3OrsContainer(Network network, String user, Path graphs, Path elevationCache) {
        return slimContainer(user, graphs, elevationCache)
                .withNetwork(network)
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
