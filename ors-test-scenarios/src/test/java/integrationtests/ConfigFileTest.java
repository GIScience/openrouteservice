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

import java.io.IOException;
import java.nio.file.Path;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.noConfigFailWaitStrategy;
import static utils.configs.OrsConfigHelper.setupConfigFileProfileDefaultFalse;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigFileTest {

    /**
     * profile-default-enabled-false.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithProfileDefaultEnabledFalse(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);
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