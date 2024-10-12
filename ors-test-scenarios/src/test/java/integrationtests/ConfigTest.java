package integrationtests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.ContainerInitializer;
import utils.OrsApiRequests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;
import static utils.OrsConfigHelper.setupConfigFileProfileDefaultFalse;
import static utils.TestContainersHelper.noConfigWaitStrategy;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigTest {

    @TempDir
    Path anotherTempDir;

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
    @Order(1)
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testActivateEachProfileWithConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        Map<String, Boolean> allProfiles = Map.of(
                "cycling-electric", true,
                "cycling-road", true,
                "cycling-mountain", true,
                "cycling-regular", true,
                "driving-car", true,
                "driving-hgv", true,
                "foot-hiking", true,
                "foot-walking", true
        );
        // Create another file in anotherTempDir called ors-config2.yml
        Path testConfig = configWithCustomProfilesActivated(anotherTempDir, "ors-config.yml", allProfiles);

        // Insert the same content as ors-config.yml
        GenericContainer<?> container = initContainer(targetImage, true, false);

        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(8, profiles.size());

        for (JsonNode profile : profiles) {
            Assertions.assertTrue(allProfiles.get(profile.get("profiles").asText()));
        }
    }

    /**
     * missing-config.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithMissingConfigFile(ContainerInitializer.ContainerTestImageBare targetImage) {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigWaitStrategy());
        container.withCommand(targetImage.getCommand().toString());
        container.start();
        container.stop();
    }

    /**
     * profile-default-enabled-false.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithProfileDefaultEnabledFalse(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        // Wait for the log message when running container.start()
        container.waitingFor(noConfigWaitStrategy());

        // Setup the config file
        Path testConfig = setupConfigFileProfileDefaultFalse(anotherTempDir, "ors-config.yml");

        // Add the config file to te container and overwrite the default config
        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");

        // Start the container. Succeeds if the expected log message is found.
        container.start();

        // Shutdown the container
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);

        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());

        List<String> expectedProfiles = List.of("driving-car", "driving-hgv");
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }
    }

    /**
     * specify-yml-prefer-arg-over-env.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testDeclaredYmlPreferredOverOrsConfigLocation(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/home/ors/openrouteservice/ors-config-car.yml"));
        // Setup the config file
        Path testConfigCar = configWithCustomProfilesActivated(anotherTempDir, "ors-config-car.yml", Map.of("driving-car", true));
        Path testConfigHGV = configWithCustomProfilesActivated(anotherTempDir, "ors-config-hgv.yml", Map.of("driving-hgv", true));

        // Mount the config file to the container
        container.withCopyFileToContainer(forHostPath(testConfigHGV), "/home/ors/openrouteservice/ors-config-hgv.yml");
        container.withCopyFileToContainer(forHostPath(testConfigCar), "/home/ors/openrouteservice/ors-config-car.yml");
        // Point the ORS_CONFIG_LOCATION to the testConfigCar
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-hgv.yml");
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            targetImage.getCommand().add("/home/ors/openrouteservice/ors-config-car.yml");
        } else {
            targetImage.getCommand().add("-Dspring-boot.run.arguments=/home/ors/openrouteservice/ors-config-car.yml");
        }
        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        container.start();
        container.stop();
    }
}