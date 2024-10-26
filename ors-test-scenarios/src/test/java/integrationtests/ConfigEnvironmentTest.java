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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.healthyWaitStrategyWithLogMessage;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;

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
    class ConfigEnvTests {
        // @formatter:off
        List<String> allProfiles = List.of("cycling-electric", "cycling-road", "cycling-mountain",
                "cycling-regular", "driving-car", "driving-hgv", "foot-hiking", "foot-walking", "wheelchair");
        // @formatter:on

        /**
         * missing-config-but-required-params-as-env-upper.sh
         * missing-config-but-profile-enabled-as-env-dot-jar-only.sh
         * Even if no yml config file is present, the ors is runnable
         * if at least one routing profile is enabled with an environment variable.
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testMissingConfigButRequiredParamsAsEnvUpperAndLower(ContainerInitializer.ContainerTestImageBare targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testMissingConfigButRequiredParamsAsEnvUpperAndLower");
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration file lookup by default locations.",
                    "Config file './ors-config.yml' not found.",
                    "Config file '/root/.config/openrouteservice/ors-config.yml' not found.",
                    "Config file '/etc/openrouteservice/ors-config.yml' not found.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            container.setCommand(targetImage.getCommand("250M").toArray(new String[0]));
            container.addEnv("ors.engine.profiles.driving-car.enabled", "true");
            container.addEnv("ORS_ENGINE_PROFILES_DRIVING_HGV_ENABLED", "true");
            container.start();

            // Assert ors-config.yml not present for a sane test.
            OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
            // Get active profiles
            OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true, "driving-hgv", true));
            container.stop();
        }

        /**
         * missing-config-but-required-params-as-arg.sh
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testMissingConfigButRequiredParamsAsArg(ContainerInitializer.ContainerTestImageBare targetImage) {
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE)) {
                // This test is not applicable to the WAR container as it does not support command line arguments.
                return;
            }
            GenericContainer<?> container = initContainer(targetImage, false, "testMissingConfigButRequiredParamsAsArg");
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Config file './ors-config.yml' not found.",
                    "Config file '/root/.config/openrouteservice/ors-config.yml' not found.",
                    "Config file '/etc/openrouteservice/ors-config.yml' not found.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            ArrayList<String> command = targetImage.getCommand("250M");
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
                command.add("--ors.engine.profiles.driving-hgv.enabled=true");
                command.add("--ors.engine.profile_default.build.source_file=/home/ors/openrouteservice/files/heidelberg.test.pbf");
            } else if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.MAVEN_CONTAINER_BARE)) {
                command.add("-Dspring-boot.run.arguments=--ors.engine.profile_default.build.source_file=/home/ors/openrouteservice/files/heidelberg.test.pbf --ors.engine.profiles.driving-hgv.enabled=true");
            }
            container.setCommand(command.toArray(new String[0]));
            container.start();

            // Assert ors-config.yml not present for a sane test.
            OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
            // Get active profiles
            OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-hgv", true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testPropertyOverridesDefaultConfig");

            container.addEnv("ors.engine.profiles.driving-car.enabled", "false");
            container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

            container.start();

            OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-hgv", true, "driving-car", false));
            container.stop();
        }


        /**
         * build-all-graphs.sh
         * profile-default-enabled-true.sh
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testBuildAllBareGraphsWithEnv(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
            GenericContainer<?> container = initContainer(targetImage, false, "testBuildAllBareGraphsWithEnv");
            container.addEnv("ors.engine.profile_default.enabled", "true");
            container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
            container.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
            container.setCommand(targetImage.getCommand("500M").toArray(new String[0]));
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Config file './ors-config.yml' not found.",
                    "Config file '/root/.config/openrouteservice/ors-config.yml' not found.",
                    "Config file '/etc/openrouteservice/ors-config.yml' not found.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));

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

        @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testDefaultProfileActivated(ContainerInitializer.ContainerTestImageDefaults targetImage) {
            // Get a fresh container
            GenericContainer<?> container = initContainer(targetImage, false, "testDefaultProfileActivated");
            if (targetImage.equals(ContainerInitializer.ContainerTestImageDefaults.WAR_CONTAINER)) {
                container.setWaitStrategy(orsCorrectConfigLoadedWaitStrategy("/home/ors/openrouteservice/ors-config.yml"));
            } else {
                container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
            }
            container.start();

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
            container.addEnv("ors.engine.profile_default.enabled", "false");
            container.addEnv("JAVA_OPTS", "-Xmx500m");
            allProfiles.forEach(profile -> container.addEnv("ors.engine.profiles." + profile + ".enabled", "true"));

            container.start();

            OrsApiHelper.assertProfilesLoaded(container, allProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));
            container.stop();
        }
    }
}