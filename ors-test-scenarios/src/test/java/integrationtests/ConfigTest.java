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
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;
import static utils.OrsConfigHelper.setupConfigFileProfileDefaultFalse;
import static utils.TestContainersHelper.*;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigTest {

    @TempDir
    Path tempDir;

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
        Map<String, Boolean> allProfiles = Map.of("cycling-electric", true, "cycling-road", true, "cycling-mountain", true, "cycling-regular", true, "driving-car", true, "driving-hgv", true, "foot-hiking", true, "foot-walking", true);
        // Create another file in anotherTempDir called ors-config2.yml
        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", allProfiles);

        // Insert the same content as ors-config.yml
        GenericContainer<?> container = initContainer(targetImage, true, false);

        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.start();

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
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
        container.waitingFor(noConfigFailWaitStrategy());
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
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
        container.waitingFor(noConfigFailWaitStrategy());

        // Setup the config file
        Path testConfig = setupConfigFileProfileDefaultFalse(tempDir, "ors-config.yml");

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

        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
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
    void testSpecificYmlPreferredOverOrsConfigLocationEnv(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/home/ors/openrouteservice/ors-config-car.yml"));
        // Setup the config file
        Path testConfigCar = configWithCustomProfilesActivated(tempDir, "ors-config-car.yml", Map.of("driving-car", true));
        Path defaultConfig = setupConfigFileProfileDefaultFalse(tempDir, "ors-config-wrong.yml");

        // Mount the config file to the container
        container.withCopyFileToContainer(forHostPath(defaultConfig), "/home/ors/openrouteservice/ors-config-wrong.yml");
        container.withCopyFileToContainer(forHostPath(testConfigCar), "/home/ors/openrouteservice/ors-config-car.yml");
        // Point the ORS_CONFIG_LOCATION to the testConfigCar
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-wrong.yml");
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            targetImage.getCommand().add("/home/ors/openrouteservice/ors-config-car.yml");
        } else {
            targetImage.getCommand().add("-Dspring-boot.run.arguments=/home/ors/openrouteservice/ors-config-car.yml");
        }
        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        container.start();
        container.stop();
    }

    /**
     * specify-yml-prefer-arg-over-lookup.sh
     **/
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testSpecificYamlPreferredOverConfigWorkingDirectoryLookup(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/home/ors/openrouteservice/ors-config-hgv.yml"));
        // Setup the config file
        Path testConfigHGV = configWithCustomProfilesActivated(tempDir, "ors-config-hgv.yml", Map.of("driving-hgv", true));
        Path defaultConfig = setupConfigFileProfileDefaultFalse(tempDir, "ors-config.yml");

        // Mount the config file to the container
        container.withCopyFileToContainer(forHostPath(testConfigHGV), "/home/ors/openrouteservice/ors-config-hgv.yml");
        container.withCopyFileToContainer(forHostPath(defaultConfig), "/home/ors/openrouteservice/ors-config.yml");

        // Construct the command
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            targetImage.getCommand().add("/home/ors/openrouteservice/ors-config-hgv.yml");
        } else {
            targetImage.getCommand().add("-Dspring-boot.run.arguments=/home/ors/openrouteservice/ors-config-hgv.yml");
        }
        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        container.start();
        // Assert ors-config.yml is present and the test is sane
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", true);
        container.stop();
    }

    /**
     * specify-yml-prefer-env-over-lookup.sh
     **/
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testSpecificEnvYamlPreferredOverWorkingDirectoryLookup(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/home/ors/openrouteservice/ors-config-hgv.yml"));
        // Setup the config file
        Path testConfigHGV = configWithCustomProfilesActivated(tempDir, "ors-config-hgv.yml", Map.of("driving-hgv", true));
        Path defaultConfig = setupConfigFileProfileDefaultFalse(tempDir, "ors-config.yml");

        // Prepare the container
        container.withCopyFileToContainer(forHostPath(testConfigHGV), "/home/ors/openrouteservice/ors-config-hgv.yml");
        container.withCopyFileToContainer(forHostPath(defaultConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-hgv.yml");
        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        // Set the env variables
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-hgv.yml");

        container.start();
        // Assert ors-config.yml is present and the test is sane
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", true);
        container.stop();
    }

    /**
     * missing-config-but-required-params-as-env-upper.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButRequiredParamsAsEnvUpperAndLower(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
        container.addEnv("ors.engine.profiles.driving-car.enabled", "true");
        container.addEnv("ORS_ENGINE_PROFILES_DRIVING_HGV_ENABLED", "true");
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(List.of("driving-car", "driving-hgv").contains(profile.get("profiles").asText()));
        }
        container.stop();
    }

    /**
     * missing-config-but-required-params-as-arg.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButRequiredParamsAsArg(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));

        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            targetImage.getCommand().add("--ors.engine.profiles.driving-hgv.enabled=true");
        } else {
            targetImage.getCommand().add("-Dspring-boot.run.arguments=--ors.engine.profiles.driving-hgv.enabled=true");
        }
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-hgv", profiles.get("profile 1").get("profiles").asText());
        container.stop();
    }

    /**
     * missing-config-but-profile-enabled-as-env-dot-jar-only.sh
     * Even if no yml config file is present, the ors is runnable
     * if at least one routing profile is enabled with a environment variable.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButProfileEnabledAsEnvDot(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-hgv", profiles.get("profile 1").get("profiles").asText());
        container.stop();
    }

    /**
     * ors-config-location-to-nonexisting-file.sh
     * The profile configured as run argument should be preferred over environment variable.
     * The default yml file should not be used when ORS_CONFIG_LOCATION is set,
     * even if the file does not exist. Fallback to default ors-config.yml is not desired!
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testOrsConfigLocationToNonExistingFile(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        if (targetImage.equals(ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER)) {
            return;
        }
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigFailWaitStrategy());
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-that-does-not-exist.yml");
        container.start();
    }

    /**
     * config-yml-plus-env-pbf-file-path.sh
     * <p>
     * This test asserts that the environment variable PBF_FILE_PATH
     * IS NOT EVALUATED when a YAML config is used.
     * Here, the yml config contains a valid path to an existing OSM file
     * and PBF_FILE_PATH contains a wrong path.
     * The expectation is, that the correct path from the yml survives
     * and openrouteservice starts up with the expected routing profile.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testConfigYmlPlusEnvPbfFilePath(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(healthyOrsWaitStrategy());
        // Start with fresh graphs
        container.addEnv("ors.engine.profile_default.graph_path", "/tmp/graphs");

        container.addEnv("PBF_FILE_PATH", "/home/ors/openrouteservice/i-do-not-exist.osm.pbf");
        container.start();
        // Assert pbf file does not exist
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/i-do-not-exist.osm.pbf", false);
        // Assert default profile is loaded
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-car", profiles.get("profile 1").get("profiles").asText());
    }


}