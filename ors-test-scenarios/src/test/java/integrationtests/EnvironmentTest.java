package integrationtests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.restartContainer;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class EnvironmentTest {

    /**
     * build-all-graphs.sh
     * profile-default-enabled-true.sh
     */
    @Order(1)
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testBuildAllImagesAndGraphsWithEnv(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profile_default.enabled", "true");
        // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

        container.start();

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(9, profiles.size());
        List<String> expectedProfiles = List.of("foot-walking", "wheelchair", "foot-hiking", "cycling-electric", "cycling-mountain", "driving-car", "driving-hgv", "cycling-regular", "cycling-road"
                // "public-transport"
        );
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }

        List<String> files = List.of("/home/ors/openrouteservice/ors-config.yml", "/home/ors/openrouteservice/logs/ors.log", "/home/ors/openrouteservice/files/heidelberg.test.pbf", "/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh");
        OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

        List<String> directories = List.of("/home/ors/openrouteservice/graphs/driving-car", "/home/ors/openrouteservice/graphs/driving-hgv", "/home/ors/openrouteservice/graphs/cycling-mountain", "/home/ors/openrouteservice/graphs/cycling-road", "/home/ors/openrouteservice/graphs/foot-walking", "/home/ors/openrouteservice/graphs/foot-hiking", "/home/ors/openrouteservice/graphs/wheelchair");
        OrsContainerFileSystemCheck.assertDirectoriesExist(container, directories.toArray(new String[0]));
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testDefaultProfileActivated(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
        // Get a fresh container
        GenericContainer<?> container = initContainer(targetImage, true);

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-car", profiles.get("profile 1").get("profiles").asText());
        container.stop();
    }

    /**
     * arg-overrides-default-prop.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0} overwrite default config with env")
    void testActivateEachProfileWithEnvAndOverwriteDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);

        List<String> allProfiles = List.of("cycling-electric", "cycling-road", "cycling-mountain", "cycling-regular", "driving-car", "driving-hgv", "foot-hiking", "foot-walking", "wheelchair");

        // Prepare the environment
        container.withEnv(Map.of());
        container.addEnv("ors.engine.profile_default.enabled", "false");
        allProfiles.forEach(profile -> container.addEnv("ors.engine.profiles." + profile + ".enabled", "true"));

        restartContainer(container);

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(9, profiles.size());

        for (JsonNode profile : profiles) {
            Assertions.assertTrue(allProfiles.contains(profile.get("profiles").asText()));
        }
        container.stop();
    }
}