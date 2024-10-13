package integrationtests;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;
import static utils.TestContainersHelper.restartContainer;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class LookupTest {

    @TempDir
    Path tempDir;

    /*
     * lookup-yml-in-etc.sh
     * lookup-yml-in-userconf.sh
     * lookup-yml-in-workdir.sh
     * lookup-yml-pefer-userconf-over-etc.sh
     * lookup-yml-prefer-workdir-over-userconf.sh
     *
     * Test that openrouteservice correctly looks for the ors-config.yml file in the correct order.
     * The lookup sequence is as follows:
     * 0. Positional argument e.g. java -jar ors.jar /path/to/ors-config.yml are always prioritized, if set.
     * 1. If the environment variable `ORS_CONFIG_LOCATION` is set, the container looks for the file at the provided path.
     * 2. Implicit usage of ./ors-config.yml in the execution directory.
     * 3. /root/.config/openrouteservice/ors-config.yml
     * 4. /etc/openrouteservice/ors-config.yml
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testLookupYmlEscalation(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        // 4. Test that the bare container looks first in /etc/openrouteservice/ors-config.yml.
        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("cycling-regular", true));
        container.withCopyFileToContainer(forHostPath(testConfig), "/etc/openrouteservice/ors-config.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/etc/openrouteservice/ors-config.yml"));
        container.start();
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));

        // 3. Test that the bare container looks second for the user config in /root/.config/openrouteservice/ors-config.yml and overrides the previous config.
        container.withCopyFileToContainer(forHostPath(testConfig), "/root/.config/openrouteservice/ors-config.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/root/.config/openrouteservice/ors-config.yml"));
        restartContainer(container);
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));

        // 2. Test that the bare container looks third in the working directory in ./ors-config.yml and overrides the previous config.
        container.withCopyFileToContainer(forHostPath(testConfig), "/home/ors/openrouteservice/ors-config.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("./ors-config.yml"));
        restartContainer(container);
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));

        // 1. Test that the bare container looks for the ors-config.yml in the environment variable `ORS_CONFIG_LOCATION` and overrides the previous config.
        container.withEnv(Map.of("ORS_CONFIG_LOCATION", "/tmp/ors-config-env.yml"));
        container.withCopyFileToContainer(forHostPath(testConfig), "/tmp/ors-config-env.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/tmp/ors-config-env.yml"));
        restartContainer(container);
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));

        // 0. Test that the bare container looks for the ors-config.yml file in the path provided as a positional argument with the highest priority.
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            targetImage.getCommand().add("/tmp/ors-config-arg.yml");
        } else {
            targetImage.getCommand().add("-Dspring-boot.run.arguments=/tmp/ors-config-arg.yml");
        }
        container.withCommand(targetImage.getCommand().toArray(new String[0]));
        container.withCopyFileToContainer(forHostPath(testConfig), "/tmp/ors-config-arg.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/tmp/ors-config-arg.yml"));
        restartContainer(container);
        OrsApiHelper.assertProfiles(container, Map.of("cycling-regular", true));
    }
}
