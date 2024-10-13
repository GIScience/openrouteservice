package integrationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import utils.ContainerInitializer;
import utils.OrsApiHelper;
import utils.OrsContainerFileSystemCheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    void testMissingConfigButRequiredParamsAsEnvUpperAndLower(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
        container.addEnv("ors.engine.profiles.driving-car.enabled", "true");
        container.addEnv("ORS_ENGINE_PROFILES_DRIVING_HGV_ENABLED", "true");
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(List.of("driving-car", "driving-hgv").contains(profile.get("profiles").asText()));
        }
        container.stop();
    }

    /**
     * missing-config-but-required-params-as-arg.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButRequiredParamsAsArg(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));
        ArrayList<String> command = targetImage.getCommand();
        if (targetImage.equals(ContainerInitializer.ContainerTestImageBare.JAR_CONTAINER_BARE)) {
            command.add("--ors.engine.profiles.driving-hgv.enabled=true");
        } else {
            command.add("-Dspring-boot.run.arguments=--ors.engine.profiles.driving-hgv.enabled=true");
        }
        container.setCommand(command.toArray(new String[0]));
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-hgv", profiles.get("profile 1").get("profiles").asText());
        container.stop();
    }

    /**
     * missing-config-but-profile-enabled-as-env-dot-jar-only.sh
     * Even if no yml config file is present, the ors is runnable
     * if at least one routing profile is enabled with a environment variable.
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageBareImageStream")
    @ParameterizedTest(name = "{0}")
    void testMissingConfigButProfileEnabledAsEnvDot(ContainerInitializer.ContainerTestImageBare targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(noConfigHealthyWaitStrategy("Log file './ors-config.yml' not found."));
        container.setCommand(targetImage.getCommand().toArray(new String[0]));
        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");
        container.start();

        // Assert ors-config.yml not present for a sane test.
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/ors-config.yml", false);
        // Get active profiles
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-hgv", profiles.get("profile 1").get("profiles").asText());
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
    void testConfigYmlPlusEnvPbfFilePath(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException, InterruptedException {
        GenericContainer<?> container = initContainer(targetImage, false);
        container.waitingFor(healthyOrsWaitStrategy());
        // Start with fresh graphs
        container.addEnv("ors.engine.profile_default.graph_path", "/tmp/graphs");

        container.addEnv("PBF_FILE_PATH", "/home/ors/openrouteservice/i-do-not-exist.osm.pbf");
        container.start();
        // Assert pbf file does not exist
        OrsContainerFileSystemCheck.assertFileExists(container, "/home/ors/openrouteservice/i-do-not-exist.osm.pbf", false);
        // Assert default profile is loaded
        JsonNode profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertEquals("driving-car", profiles.get("profile 1").get("profiles").asText());
        container.stop();
    }
}