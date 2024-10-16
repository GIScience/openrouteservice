package integrationtests;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import utils.ContainerInitializer;
import utils.OrsApiHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;
import static utils.TestContainersHelper.noConfigFailWaitStrategy;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public class LookupTest {

    private static final String CONFIG_FILE_PATH_ETC = "/etc/openrouteservice/ors-config.yml";
    private static final String CONFIG_FILE_PATH_USERCONF = "/root/.config/openrouteservice/ors-config.yml";
    private static final String CONFIG_FILE_PATH_WORKDIR = "/home/ors/openrouteservice/ors-config.yml";
    private static final String CONFIG_FILE_PATH_TMP = "/tmp/ors-config-env.yml";
    private static final String CONFIG_FILE_PATH_ARG = "/tmp/ors-config-arg.yml";

    /**
     * missing-config.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithMissingConfigFile(ContainerInitializer.ContainerTestImageBare targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigFailWaitStrategy());
        container.setCommand(targetImage.getCommand("100M").toArray(new String[0]));
        container.start();
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void lookupYmlInEtc(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");
        container.setCommand(command.toArray(new String[0]));

        String testProfile = "cycling-regular";
        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of(testProfile, true));
        // Check that the etc. path is used as path that is used by default, if nothing else is specified.
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy(CONFIG_FILE_PATH_ETC));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of(testProfile, true));
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void lookupYmlInUserconfAndOverwriteUserConf(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");
        container.setCommand(command.toArray(new String[0]));

        Path testConfigCyclingRegular = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));
        // Check the hierarchy works.
        container.withCopyFileToContainer(forHostPath(testConfigCyclingRegular), CONFIG_FILE_PATH_ETC);
        // This should be the one that is loaded.
        container.withCopyFileToContainer(forHostPath(testConfigCyclingRegular), CONFIG_FILE_PATH_USERCONF);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy(CONFIG_FILE_PATH_USERCONF));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-in-workdir.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     * lookup-yml-prefer-workdir-over-userconf.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void lookupYmlInWorkdirOverUserConfAndEtc(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");
        container.setCommand(command.toArray(new String[0]));

        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));
        // Check the hierarchy works.
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
        // This should be the one that is loaded. It will be logged as ./ors-config.yml
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-in-workdir.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     * lookup-yml-prefer-workdir-over-userconf.sh
     * specify-yml-prefer-env-over-lookup.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void specifyYmlPreferEnvOverLookup(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");
        container.setCommand(command.toArray(new String[0]));

        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));
        // Check the hierarchy works.
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_TMP);
        // This should be the one that is loaded.
        container.withEnv(Map.of("ORS_CONFIG_LOCATION", CONFIG_FILE_PATH_TMP));
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy(CONFIG_FILE_PATH_TMP));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-in-workdir.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     * lookup-yml-prefer-workdir-over-userconf.sh
     * specify-yml-prefer-arg-over-lookup.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void specifyYmlPreferArgOverLookup(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");

        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));

        container.withCommand(command.toArray(new String[0]));
        Path testConfig2 = configWithCustomProfilesActivated(tempDir, "ors-config2.yml", Map.of("cycling-road", true));
        // Check the hierarchy works.
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
        // This should be the one that is loaded.
        container.withCopyFileToContainer(forHostPath(testConfig2), CONFIG_FILE_PATH_ARG);
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy(CONFIG_FILE_PATH_ARG));
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            command.add(CONFIG_FILE_PATH_ARG);
        } else {
            command.add("-Dspring-boot.run.arguments=" + CONFIG_FILE_PATH_ARG);
        }

        container.setCommand(command.toArray(new String[0]));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
        container.stop();
    }

    /**
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-in-workdir.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     * lookup-yml-prefer-workdir-over-userconf.sh
     * specify-yml-prefer-arg-over-env.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void specifyYmlPreferArgOverEnv(ContainerInitializer.ContainerTestImageBare targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
        ArrayList<String> command = targetImage.getCommand("200M");
        container.setCommand(command.toArray(new String[0]));

        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            command.add(CONFIG_FILE_PATH_TMP);
        } else {
            command.add("-Dspring-boot.run.arguments=" + CONFIG_FILE_PATH_TMP);
        }
        container.withCommand(command.toArray(new String[0]));
        Path testConfig2 = configWithCustomProfilesActivated(tempDir, "ors-config2.yml", Map.of("cycling-road", true));
        // Check the hierarchy works.
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_ETC);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_USERCONF);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_WORKDIR);
        container.withCopyFileToContainer(forHostPath(testConfig), CONFIG_FILE_PATH_TMP);
        container.withEnv(Map.of("ORS_CONFIG_LOCATION", CONFIG_FILE_PATH_TMP));
        // This should be the one that is loaded.
        container.withCopyFileToContainer(forHostPath(testConfig2), "/tmp/ors-config-arg.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/tmp/ors-config-arg.yml"));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
        container.stop();
    }
}
