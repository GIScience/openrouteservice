package integrationtests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsConfig;
import utils.OrsContainerFileSystemCheck;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.healthyOrsWaitStrategy;
import static utils.TestContainersHelper.waitStrategyWithLogMessage;

@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigFileTest {

    @BeforeAll
    void cacheLayers() {
        ContainerInitializer.buildLayers();
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class ConfigFileTests {


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
        @Execution(ExecutionMode.CONCURRENT)
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
            GenericContainer<?> container = initContainer(targetImage, false, "testActivateEachProfileWithConfig");

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
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testFailStartupWithProfileDefaultEnabledFalse(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            GenericContainer<?> container = initContainer(targetImage, false, "testFailStartupWithProfileDefaultEnabledFalse");
            // Setup the config file
            Path testConfig = OrsConfig.builder()
                    .profileDefaultEnabled(false)
                    .graphManagementEnabled(false)
                    .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
                    .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                    .build().toYAML(tempDir, "ors-config.yml");

            // Add the config file to te container and overwrite the default config
            container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
            container.withCopyFileToContainer(forHostPath(testConfig), "/usr/local/tomcat/ors-config.yml");
            container.waitingFor(waitStrategyWithLogMessage(
                    List.of(
                            "Configuration lookup started.",
                            "Configuration file lookup by default locations.",
                            "Loaded file './ors-config.yml'.",
                            "Configuration lookup finished.",
                            "No profiles configured. Exiting."
                    ).toArray(new String[0])));
            container.setCommand(targetImage.getCommand("200M").toArray(String[]::new));
            // Start the container. Succeeds if the expected log message is found.
            container.start();

            // Shutdown the container
            container.stop();
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
        @Execution(ExecutionMode.CONCURRENT)
        void testConfigYmlPlusEnvPbfFilePath(ContainerInitializer.ContainerTestImageDefaults targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testConfigYmlPlusEnvPbfFilePath");
            container.waitingFor(healthyOrsWaitStrategy());
            container.addEnv("PBF_FILE_PATH", "/home/ors/openrouteservice/i-do-not-exist.osm.pbf");
            container.start();
            // Assert pbf file does not exist
            OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/i-do-not-exist.osm.pbf", false);
            // Assert default profile is loaded
            OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
            container.stop();
        }
    }
}