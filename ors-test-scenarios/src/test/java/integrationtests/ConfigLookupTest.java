package integrationtests;

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

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.TestContainersHelper.*;

@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ConfigLookupTest extends ContainerInitializer {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class configLookupTests {

        private static final String CONFIG_FILE_PATH_ETC = "/etc/openrouteservice/ors-config.yml";
        private static final String CONFIG_FILE_PATH_USERCONF = "/root/.config/openrouteservice/ors-config.yml";
        private static final String CONFIG_FILE_PATH_WORKDIR = "/home/ors/openrouteservice/ors-config.yml";
        private static final String CONFIG_FILE_PATH_WORKDIR_WAR = "/usr/local/tomcat/ors-config.yml";
        private static final String CONFIG_FILE_PATH_TMP = "/tmp/ors-config-env.yml";
        private static final String CONFIG_FILE_PATH_ARG = "/tmp/ors-config-arg.yml";

        /**
         * These tests run first if all are executed in parallel.
         * They test for the correct behavior of the container when no config file is present.
         * They also build the container images if they are not already built for the other tests in this file as well.
         * If all executed in parallel, the intermediate images would be build multiple times.
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testFailStartupWithMissingConfigFile(ContainerInitializer.ContainerTestImageBare targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testFailStartupWithMissingConfigFile");
            container.waitingFor(waitStrategyWithLogMessage(List.of(
                    "Configuration file lookup by default locations.",
                    "Config file './ors-config.yml' not found.",
                    "Config file '/root/.config/openrouteservice/ors-config.yml' not found.",
                    "Config file '/etc/openrouteservice/ors-config.yml' not found.",
                    "Configuration lookup finished.",
                    "No profiles configured. Exiting."
            ).toArray(new String[0])));
            container.setCommand(targetImage.getCommand("100M").toArray(new String[0]));
            container.start();
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void lookupYmlInEtc(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            GenericContainer<?> container = initContainer(targetImage, false, "lookupYmlInEtc");
            ArrayList<String> command = targetImage.getCommand("200M");
            container.setCommand(command.toArray(new String[0]));

            String testProfile = "cycling-regular";
            Path testConfig = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of(testProfile, true)))
                    .build().toYAML(tempDir, "ors-config.yml");

            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration file lookup by default locations.",
                    "Config file './ors-config.yml' not found.",
                    "Config file '/root/.config/openrouteservice/ors-config.yml' not found.",
                    "Loaded file '" + CONFIG_FILE_PATH_ETC + "'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            container.start();

            OrsApiHelper.assertProfilesLoaded(container, Map.of(testProfile, true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void lookupYmlInUserConfAndOverwriteEtc(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            GenericContainer<?> container = initContainer(targetImage, false, "lookupYmlInUserConfAndOverwriteUserConf");
            ArrayList<String> command = targetImage.getCommand("200M");
            container.setCommand(command.toArray(new String[0]));

            Path testConfigCyclingRegular = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of("cycling-regular", true)))
                    .build().toYAML(tempDir, "ors-config.yml");

            container.withCopyFileToContainer(forHostPath(testConfigCyclingRegular), CONFIG_FILE_PATH_ETC);
            container.withCopyFileToContainer(forHostPath(testConfigCyclingRegular), CONFIG_FILE_PATH_USERCONF);
            container.waitingFor(orsCorrectConfigLoadedWaitStrategy(CONFIG_FILE_PATH_USERCONF));
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration file lookup by default locations.",
                    "Config file './ors-config.yml' not found.",
                    "Loaded file '" + CONFIG_FILE_PATH_USERCONF + "'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));

            container.start();
            OrsApiHelper.assertProfilesLoaded(container, Map.of("cycling-regular", true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void lookupYmlInWorkdirOverUserConfAndEtc(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            GenericContainer<?> container = initContainer(targetImage, false, "lookupYmlInWorkdirOverUserConfAndEtc");
            ArrayList<String> command = targetImage.getCommand("200M");
            container.setCommand(command.toArray(new String[0]));

            Path testConfig = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of("cycling-regular", true)))
                    .build().toYAML(tempDir, "ors-config.yml");
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE)) {
                // Tomcat has another workdir in /usr/local/tomcat
                container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR_WAR);
            } else {
                container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
            }
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration file lookup by default locations.",
                    "Loaded file './ors-config.yml'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            container.start();
            OrsApiHelper.assertProfilesLoaded(container, Map.of("cycling-regular", true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void specifyYmlPreferEnvOverLookup(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            GenericContainer<?> container = initContainer(targetImage, false, "specifyYmlPreferEnvOverLookup");
            ArrayList<String> command = targetImage.getCommand("200M");
            container.setCommand(command.toArray(new String[0]));

            Path testConfig = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of("cycling-regular", true)))
                    .build().toYAML(tempDir, "ors-config.yml");
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
            container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_TMP);
            container.withEnv(Map.of("ORS_CONFIG_LOCATION", CONFIG_FILE_PATH_TMP));
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration lookup started.",
                    "Loaded file '" + CONFIG_FILE_PATH_TMP + "'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            container.withStartupTimeout(Duration.ofSeconds(200)); // mö
            container.start();
            OrsApiHelper.assertProfilesLoaded(container, Map.of("cycling-regular", true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void specifyYmlPreferArgOverLookup(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE)) {
                // This test is not applicable to the WAR container as it does not support command line arguments.
                return;
            }
            GenericContainer<?> container = initContainer(targetImage, false, "specifyYmlPreferArgOverLookup");
            ArrayList<String> command = targetImage.getCommand("200M");

            Path wrongTestConfig = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of("cycling-regular", true)))
                    .build().toYAML(tempDir, "ors-config.yml");

            container.withCommand(command.toArray(new String[0]));
            Path correctTestConfig = OrsConfig.builder()
                    .profiles(new HashMap<>(Map.of("cycling-road", true)))
                    .build().toYAML(tempDir, "ors-config2.yml");
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_ETC);
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_USERCONF);
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_WORKDIR);
            container.withCopyFileToContainer(forHostPath(correctTestConfig), CONFIG_FILE_PATH_ARG);
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration lookup started.",
                    "Configuration file set by program argument.",
                    "Loaded file '" + CONFIG_FILE_PATH_ARG + "'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
                command.add(CONFIG_FILE_PATH_ARG);
            } else if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.MAVEN_CONTAINER_BARE)) {
                command.add("-Dspring-boot.run.arguments=" + CONFIG_FILE_PATH_ARG);
            }

            container.setCommand(command.toArray(new String[0]));
            container.start();
            OrsApiHelper.assertProfilesLoaded(container, Map.of("cycling-road", true));
            container.stop();
        }

        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void specifyYmlPreferArgOverEnv(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) {
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.WAR_CONTAINER_BARE)) {
                // This test is not applicable to the WAR container as it does not support command line arguments.
                return;
            }
            GenericContainer<?> container = initContainer(targetImage, false, "specifyYmlPreferArgOverEnv");
            ArrayList<String> command = targetImage.getCommand("200M");
            container.setCommand(command.toArray(new String[0]));

            Path wrongTestConfig = OrsConfig.builder()
                    .profileDefaultEnabled(false)
                    .graphManagementEnabled(false)
                    .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
                    .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                    .profiles(new HashMap<>(Map.of("cycling-regular", true)))
                    .build().toYAML(tempDir, "ors-config.yml");
            if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
                command.add(CONFIG_FILE_PATH_ARG);
            } else if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.MAVEN_CONTAINER_BARE)) {
                command.add("-Dspring-boot.run.arguments=" + CONFIG_FILE_PATH_ARG);
            }
            container.withCommand(command.toArray(new String[0]));
            Path correctTestConfig = OrsConfig.builder()
                    .profileDefaultEnabled(false)
                    .graphManagementEnabled(false)
                    .ProfileDefaultBuildSourceFile("/home/ors/openrouteservice/files/heidelberg.test.pbf")
                    .profileDefaultGraphPath("/home/ors/openrouteservice/graphs")
                    .profiles(new HashMap<>(Map.of("cycling-road", true)))
                    .build().toYAML(tempDir, "ors-config2.yml");
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_ETC);
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_USERCONF);
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_WORKDIR);
            container.withCopyFileToContainer(forHostPath(wrongTestConfig), CONFIG_FILE_PATH_TMP);
            container.withEnv(Map.of("ORS_CONFIG_LOCATION", CONFIG_FILE_PATH_TMP));
            container.withCopyFileToContainer(forHostPath(correctTestConfig), "/tmp/ors-config-arg.yml");
            container.waitingFor(healthyWaitStrategyWithLogMessage(List.of(
                    "Configuration lookup started.",
                    "Configuration file set by program argument.",
                    "Loaded file '" + CONFIG_FILE_PATH_ARG + "'.",
                    "Configuration lookup finished."
            ).toArray(new String[0])));
            container.start();
            OrsApiHelper.assertProfilesLoaded(container, Map.of("cycling-road", true));
            container.stop();
        }

        /**
         * ors-config-location-to-nonexisting-file.sh
         * The profile configured as run argument should be preferred over environment variable.
         * The default yml file should not be used when ORS_CONFIG_LOCATION is set,
         * even if the file does not exist. Fallback to default ors-config.yml is not desired!
         */
        @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
        @ParameterizedTest(name = "{0}")
        @Execution(ExecutionMode.CONCURRENT)
        void testOrsConfigLocationToNonExistingFile(ContainerInitializer.ContainerTestImageBare targetImage) {
            GenericContainer<?> container = initContainer(targetImage, false, "testOrsConfigLocationToNonExistingFile");
            container.setCommand(targetImage.getCommand("200M").toArray(new String[0]));
            container.addEnv("ORS_CONFIG_LOCATION", "/home/ors/openrouteservice/ors-config-that-does-not-exist.yml");
            container.waitingFor(waitStrategyWithLogMessage(List.of(
                    "Configuration lookup started.",
                    "Configuration file set by environment variable.",
                    "Config file '/home/ors/openrouteservice/ors-config-that-does-not-exist.yml' not found.",
                    "Configuration lookup finished.",
                    "No profiles configured. Exiting."
            ).toArray(new String[0])));
            container.start();
            container.stop();
        }
    }
}