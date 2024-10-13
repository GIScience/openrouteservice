package integrationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.ContainerInitializer;
import utils.OrsApiHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigProfileTest {

    /**
     * build-graph-cycling-electric.sh
     * build-graph-cycling-mountain.sh
     * build-graph-cycling-regular.sh
     * build-graph-cycling-road.sh
     * build-graph-driving-car.sh
     * build-graph-driving-hgv.sh
     * build-graph-foot-hiking.sh
     * build-graph-foot-walking.sh
     * build-graph-public-transport.sh --> Missing
     * build-graph-wheelchair.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testActivateEachProfileWithConfig(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException {
        Map<String, Boolean> allProfiles = Map.of("cycling-electric", true, "cycling-road", true, "cycling-mountain", true, "cycling-regular", true, "driving-car", true, "driving-hgv", true, "foot-hiking", true, "foot-walking", true);
        // Create another file in anotherTempDir called ors-config2.yml
        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", allProfiles);

        // Insert the same content as ors-config.yml
        GenericContainer<?> container = initContainer(targetImage, false);

        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.start();

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(8, profiles.size());

        for (JsonNode profile : profiles) {
            Assertions.assertTrue(allProfiles.get(profile.get("profiles").asText()));
        }
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);

        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

        container.start();

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());

        List<String> expectedProfiles = List.of("driving-car", "driving-hgv");
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }
        container.stop();
    }
}