package integrationtests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.OrsApiRequests;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static utils.OrsApiRequests.checkAvoidAreaRequest;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentChecks {
    private final Map<String, String> defaultEnv = Map.of(
            "logging.level.org.heigit", "INFO",
            "ors.engine.graphs_data_access", "MMAP"
    );

    private GenericContainer<?> warContainer;
    private GenericContainer<?> jarContainer;

    static Stream<Object[]> data() {
        return Stream.of(
                new Object[]{"ors-test-scenarios-war", "3.12"},
                new Object[]{"ors-test-scenarios-jar", "3.13"}
        );
    }

    private GenericContainer<?> initContainer(String baseImage) {
        WaitStrategy waitStrategy = new HttpWaitStrategy()
                .forPort(8080)
                .forStatusCode(200)
                .forPath("/ors/v2/health")
                .withStartupTimeout(Duration.ofSeconds(80));

        GenericContainer<?> container = new GenericContainer<>(
                new ImageFromDockerfile(baseImage, false)
                        .withFileFromPath("ors-api", Path.of("../ors-api"))
                        .withFileFromPath("ors-engine", Path.of("../ors-engine"))
                        .withFileFromPath("ors-report-aggregation", Path.of("../ors-report-aggregation"))
                        .withFileFromPath("pom.xml", Path.of("../pom.xml"))
                        .withFileFromPath("ors-config.yml", Path.of("../ors-config.yml"))
                        .withFileFromPath("Dockerfile", Path.of("../ors-test-scenarios/src/test/resources/Dockerfile"))
                        .withFileFromPath(".dockerignore", Path.of("../.dockerignore"))
                        .withTarget(baseImage)
        )
                .withEnv(defaultEnv)
                .withFileSystemBind("./graphs-integrationtests", "/home/ors/openrouteservice/graphs", BindMode.READ_WRITE)
                .withExposedPorts(8080)
                .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                .waitingFor(waitStrategy);

        if (baseImage.equals("ors-test-scenarios-war")) {
            if (warContainer == null || !warContainer.isRunning()) {
                warContainer = container;
                warContainer.start();
            }
            return warContainer;
        } else {
            if (jarContainer == null || !jarContainer.isRunning()) {
                jarContainer = container;
                jarContainer.start();
            }
            return jarContainer;
        }
    }

    @AfterEach
    public void resetEnv() {
        if (warContainer != null) {
            warContainer.withEnv(defaultEnv);
        }
        if (jarContainer != null) {
            jarContainer.withEnv(defaultEnv);
        }
    }

    @Order(1)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testBuildAllImagesAndGraphs(String targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage);
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profile_default.enabled", "true");
        // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

        container.stop();
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(9, profiles.size());
        List<String> expectedProfiles = List.of(
                "foot-walking", "wheelchair", "foot-hiking", "cycling-electric",
                "cycling-mountain", "driving-car", "driving-hgv", "cycling-regular", "cycling-road"
                // "public-transport"
        );
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }

        List<String> files = List.of(
                "/home/ors/openrouteservice/ors-config.yml",
                "/home/ors/openrouteservice/logs/ors.log",
                "/home/ors/openrouteservice/files/heidelberg.test.pbf",
                "/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh"
        );
        OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

        List<String> directories = List.of(
                "/home/ors/openrouteservice/graphs/driving-car",
                "/home/ors/openrouteservice/graphs/driving-hgv",
                "/home/ors/openrouteservice/graphs/cycling-mountain",
                "/home/ors/openrouteservice/graphs/cycling-road",
                "/home/ors/openrouteservice/graphs/foot-walking",
                "/home/ors/openrouteservice/graphs/foot-hiking",
                "/home/ors/openrouteservice/graphs/wheelchair"
        );
        OrsContainerFileSystemCheck.assertDirectoriesExist(container, directories.toArray(new String[0]));
    }

    @Order(2)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testAvoidAreaRequestAndGeoToolsPopulation(String targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage);

        String geoToolsPath;
        if (container.getDockerImageName().contains("ors-test-scenarios-war"))
            geoToolsPath = "/usr/local/tomcat/temp/GeoTools";
        else geoToolsPath = "/tmp/GeoTools";

        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, false);
        checkAvoidAreaRequest("http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/ors/v2/directions/driving-car/geojson", 200);
        OrsContainerFileSystemCheck.assertDirectoryExists(container, geoToolsPath, true);
    }

    @Order(3)
    @MethodSource("data")
    @ParameterizedTest(name = "{0}")
    void testTwoProfilesActivatedByEnv(String targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage);

        // Activate two new profiles alongside the default driving-car profile
        container.addEnv("ors.engine.profile_default.enabled", "false");
        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        container.addEnv("ors.engine.profiles.cycling-regular.enabled", "true");
        container.stop();
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(3, profiles.size());

        List<String> loadedProfiles = List.of("driving-car", "driving-hgv", "cycling-regular");
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(loadedProfiles.contains(profile.get("profiles").asText()));
        }
    }
}