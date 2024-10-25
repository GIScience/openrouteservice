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

import java.util.ArrayList;
import java.util.Map;

import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.healthyOrsWaitStrategy;
import static utils.TestContainersHelper.noConfigHealthyWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigEnvTest {

    /**
     * missing-config-but-required-params-as-env-upper.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButRequiredParamsAsEnvUpperAndLower(ContainerInitializer.ContainerTestImageBare targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Config file './ors-config.yml' not found."));
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
    void testMissingConfigButRequiredParamsAsArg(ContainerInitializer.ContainerTestImageBare targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Config file './ors-config.yml' not found."));
        ArrayList<String> command = targetImage.getCommand("250M");
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            command.add("--ors.engine.profiles.driving-hgv.enabled=true");
            command.add("--ors.engine.profile_default.build.source_file=/home/ors/openrouteservice/files/heidelberg.test.pbf");
        } else {
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

    /**
     * missing-config-but-profile-enabled-as-env-dot-jar-only.sh
     * Even if no yml config file is present, the ors is runnable
     * if at least one routing profile is enabled with a environment variable.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButProfileEnabledAsEnvDot(ContainerInitializer.ContainerTestImageBare targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Config file './ors-config.yml' not found."));
        container.setCommand(targetImage.getCommand("250M").toArray(new String[0]));
        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        container.addEnv("ors.engine.profile_default.build.source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-hgv", true));
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
    void testConfigYmlPlusEnvPbfFilePath(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(healthyOrsWaitStrategy());
        // Start with fresh graphs
        container.addEnv("ors.engine.profile_default.graph_path", "/tmp/graphs");

        container.addEnv("PBF_FILE_PATH", "/home/ors/openrouteservice/i-do-not-exist.osm.pbf");
        container.start();
        // Assert pbf file does not exist
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/i-do-not-exist.osm.pbf", false);
        // Assert default profile is loaded
        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-car", true));
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) {
        GenericContainer<?> container = initContainer(targetImage, false);

        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

        container.start();

        OrsApiHelper.assertProfilesLoaded(container, Map.of("driving-hgv", true, "driving-car", true));
        container.stop();
    }
}