package integrationtests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsConfig;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.noConfigFailWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigFileTest {


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
    void testActivateEachProfileWithConfig(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) {
        HashMap<String, Boolean> allProfiles = new HashMap<>(Map.of("cycling-electric", true, "cycling-road", true, "cycling-mountain", true, "cycling-regular", true, "driving-car", true, "driving-hgv", true, "foot-hiking", true, "foot-walking", true));
        // Setup the config file
        Path testConfig = OrsConfig.builder()
                .profileDefaultEnabled(false)
                .graphManagementEnabled(false)
                .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
                .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                .profiles(allProfiles)
                .build().toYAML(tempDir, "ors-config.yml");
        // Insert the same content as ors-config.yml
        GenericContainer<?> container = initContainer(targetImage, false);

        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.withStartupTimeout(Duration.ofSeconds(200));
        container.addEnv("JAVA_OPTS", "-Xmx500m");
        container.start();

        OrsApiHelper.assertProfilesLoaded(container, allProfiles);
        container.stop();
    }


    /**
     * profile-default-enabled-false.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithProfileDefaultEnabledFalse(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) {
        GenericContainer<?> container = initContainer(targetImage, false);
        // Wait for the log message when running container.start()
        container.waitingFor(noConfigFailWaitStrategy());

        // Setup the config file
        Path testConfig = OrsConfig.builder()
                .profileDefaultEnabled(false)
                .graphManagementEnabled(false)
                .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
                .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                .build().toYAML(tempDir, "ors-config.yml");

        // Add the config file to te container and overwrite the default config
        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");

        // Start the container. Succeeds if the expected log message is found.
        container.start();

        // Shutdown the container
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
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigFailWaitStrategy());
        container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-that-does-not-exist.yml");
        container.start();
        container.stop();
    }
}