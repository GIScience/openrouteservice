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
import utils.OrsConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigProfileTest {

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
    void testActivateEachProfileWithConfig(ContainerInitializer.ContainerTestImageDefaults targetImage, @TempDir Path tempDir) throws IOException {
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

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, false);

        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

        container.start();

        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-hgv", true, "driving-car", true));
        container.stop();
    }
}