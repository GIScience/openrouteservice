package integrationtests;

import org.junit.jupiter.api.Assertions;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.ContainerInitializer;
import utils.OrsApiRequests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.OrsConfigHelper.configWithCustomProfilesActivated;
import static utils.TestContainersHelper.orsCorrectConfigLoadedWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class LookupTest {

    @TempDir
    Path tempDir;

    /*
     * lookup-yml-in-etc.sh
     *
     * Test that the container looks for the ors-config.yml file in /etc/openrouteservice/ors-config.yml,
     * if no config file is provided via the environment variable `CONFIG_FILE` or is present at ./ors-config.yml from the execution directory.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testLookupYmlInEtc(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        Path testConfig = configWithCustomProfilesActivated(tempDir, "ors-config.yml", Map.of("driving-hgv", true, "cycling-regular", true));
        container.withCopyFileToContainer(forHostPath(testConfig), "/etc/openrouteservice/ors-config.yml");
        container.waitingFor(orsCorrectConfigLoadedWaitStrategy("/etc/openrouteservice/ors-config.yml"));

        container.setCommand(targetImage.getCommand().toArray(new String[0]));

        container.start();

        // Assert profiles are loaded
        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(profile.get("profiles").asText().equals("driving-hgv") || profile.get("profiles").asText().equals("cycling-regular"));
        }
    }

}
