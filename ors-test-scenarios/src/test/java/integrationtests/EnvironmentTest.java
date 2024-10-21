package integrationtests;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
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
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers(disabledWithoutDocker = true)
public class EnvironmentTest {

    /**
     * build-all-graphs.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testBuildAllBareGraphsWithEnv(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profile_default.enabled", "true");
        container.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        container.setCommand(targetImage.getCommand("500M").toArray(new String[0]));
        // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

        container.start();

        List<String> expectedProfiles = List.of("foot-walking", "wheelchair", "foot-hiking", "cycling-electric", "cycling-mountain", "driving-car", "driving-hgv", "cycling-regular", "cycling-road"
                // "public-transport"
        );
        OrsApiHelper.assertProfilesLoaded(container, expectedProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));

        List<String> files = List.of("/home/ors/openrouteservice/logs/ors.log", "/home/ors/openrouteservice/files/heidelberg.test.pbf", "/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh");
        OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

        for (String profile : expectedProfiles) {
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
    void testBuildAllDefaultGraphsWithEnv(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.addEnv("ors.engine.profiles.public-transport.enabled", "false");
        container.addEnv("ors.engine.profile_default.enabled", "true");
        container.addEnv("JAVA_OPTS", "-Xmx500m");
        // sharedOrsTestContainer.addEnv("gtfs_file", "/home/ors/openrouteservice/ors-api/src/test/files/vrn_gtfs_cut.zip");

        container.start();

        List<String> expectedProfiles = List.of("foot-walking", "wheelchair", "foot-hiking", "cycling-electric", "cycling-mountain", "driving-car", "driving-hgv", "cycling-regular", "cycling-road"
                // "public-transport"
        );
        OrsApiHelper.assertProfilesLoaded(container, expectedProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));

        List<String> files = List.of("/home/ors/openrouteservice/ors-config.yml", "/home/ors/openrouteservice/logs/ors.log", "/home/ors/openrouteservice/files/heidelberg.test.pbf", "/home/ors/openrouteservice/elevation_cache/srtm_38_03.gh");
        OrsContainerFileSystemCheck.assertFilesExist(container, files.toArray(new String[0]));

        for (String profile : expectedProfiles) {
            OrsContainerFileSystemCheck.assertDirectoryExists(container, "/home/ors/openrouteservice/graphs/" + profile, true);
        }
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testDefaultProfileActivated(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        // Get a fresh container
        GenericContainer<?> container = initContainer(targetImage, true);

        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        container.stop();
    }

    /**
     * arg-overrides-default-prop.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testActivateEachProfileWithEnvAndOverwriteDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);

        List<String> allProfiles = List.of("cycling-electric", "cycling-road", "cycling-mountain", "cycling-regular", "driving-car", "driving-hgv", "foot-hiking", "foot-walking", "wheelchair");

        // Prepare the environment
        container.withEnv(Map.of());
        container.addEnv("ors.engine.profile_default.enabled", "false");
        container.addEnv("JAVA_OPTS", "-Xmx400m");
        allProfiles.forEach(profile -> container.addEnv("ors.engine.profiles." + profile + ".enabled", "true"));

        restartContainer(container);

        OrsApiHelper.assertProfilesLoaded(container, allProfiles.stream().collect(HashMap::new, (m, v) -> m.put(v, true), HashMap::putAll));
        container.stop();
    }
}