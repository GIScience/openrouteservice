package integrationtests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.restartContainer;

@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigEnvironmentTest {
    @BeforeAll
    void cacheLayers() {
        ContainerInitializer.buildLayers();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class EnvironmentTests {
        // @formatter:off
        List<String> allProfiles = List.of("cycling-electric", "cycling-road", "cycling-mountain",
                "cycling-regular", "driving-car", "driving-hgv", "foot-hiking", "foot-walking", "wheelchair");
        // @formatter:on

        /**
         * build-all-graphs.sh
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testBuildAllBareGraphsWithEnv(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
            GenericContainer<?> container = initContainer(targetImage, false, "testBuildAllBareGraphsWithEnv");
            container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
            container.addEnv("ors.engine.profile_default.enabled", "true");
            container.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
            container.setCommand(targetImage.getCommand("500M").toArray(new String[0]));
            // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

            container.start();

            OrsApiHelper.assertProfilesLoaded(container, allProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));

            // @formatter:off
            List<String> files = List.of("/home/ors/openrouteservice/logs/ors.log",
                    "/home/ors/openrouteservice/files/heidelberg.test.pbf");
            // @formatter:on
            OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

            for (String profile : allProfiles) {
                OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/" + profile, true);
            }
            container.stop();
        }

        /**
         * build-all-graphs.sh
         * profile-default-enabled-true.sh
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testBuildAllDefaultGraphsWithEnv(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
            GenericContainer<?> container = initContainer(targetImage, false, "testBuildAllDefaultGraphsWithEnv");
            container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
            container.addEnv("ors.engine.profile_default.enabled", "true");
            container.addEnv("JAVA_OPTS", "-Xmx500m");
            // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

            container.start();

            OrsApiHelper.assertProfilesLoaded(container, allProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));

            // @formatter:off
            List<String> files = List.of("/home/ors/openrouteservice/ors-config.yml",
                    "/home/ors/openrouteservice/logs/ors.log",
                    "/home/ors/openrouteservice/files/heidelberg.test.pbf");
            // @formatter:on
            OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

            for (String profile : allProfiles) {
                OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/" + profile, true);
            }
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testDefaultProfileActivated(ContainerInitializer.ContainerTestImageDefaults targetImage) {
            // Get a fresh container
            GenericContainer<?> container = initContainer(targetImage, true, "testDefaultProfileActivated");

            OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
            container.stop();
        }

        /**
         * arg-overrides-default-prop.sh
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testActivateEachProfileWithEnvAndOverwriteDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testActivateEachProfileWithEnvAndOverwriteDefaultConfig");

            // Prepare the environment
            container.withEnv(Map.of());
            container.addEnv("ors.engine.profile_default.enabled", "false");
            container.addEnv("JAVA_OPTS", "-Xmx500m");
            allProfiles.forEach(profile -> container.addEnv("ors.engine.profiles." + profile + ".enabled", "true"));

            restartContainer(container);

            OrsApiHelper.assertProfilesLoaded(container, allProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));
            container.stop();
        }
    }
}