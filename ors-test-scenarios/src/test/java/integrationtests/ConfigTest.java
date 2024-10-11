package integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.*;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static utils.ContainerInitializer.initContainer;
import static utils.TestContainersHelper.noConfigWaitStrategy;

@ExtendWith(TestcontainersExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
public class ConfigTest {

    @TempDir
    File anotherTempDir;

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
    @Order(1)
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testActivateEachProfileWithConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        List<String> allProfiles = List.of("cycling-electric", "cycling-road", "cycling-mountain", "cycling-regular", "driving-car", "driving-hgv", "foot-hiking", "foot-walking");
        // Create another file in anotherTempDir called ors-config2.yml
        File testConfig = new File(anotherTempDir, "ors-config.yml");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        // Create the profile_default node
        ObjectNode profileDefaultNode = mapper.createObjectNode();
        profileDefaultNode.put("enabled", false);
        profileDefaultNode.put("source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        profileDefaultNode.put("graph_path", "/home/ors/openrouteservice/graphs");

        // Create the profiles node
        ObjectNode profilesNode = mapper.createObjectNode();
        profilesNode.put("wheelchair", mapper.createObjectNode().put("enabled", false));
        for (String profile : allProfiles) {
            // Add each profile to the profiles node
            profilesNode.put(profile, mapper.createObjectNode().put("enabled", true));
        }
        // Create the engine object
        ObjectNode engineNode = mapper.createObjectNode();
        engineNode.set("profile_default", profileDefaultNode);
        engineNode.set("profiles", profilesNode);
        // Add the engine object to the root node
        rootNode.set("ors", mapper.createObjectNode().set("engine", engineNode));
        // Write the JsonNode to a YAML file
        YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory());
        yamlMapper.writeValue(testConfig, rootNode);
        // Insert the same content as ors-config.yml
        GenericContainer<?> container = initContainer(targetImage, true, false);

        container.withCopyFileToContainer(forHostPath(testConfig.getPath()), "/home/ors/openrouteservice/ors-config.yml");
        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(8, profiles.size());

        for (JsonNode profile : profiles) {
            Assertions.assertTrue(allProfiles.contains(profile.get("profiles").asText()));
        }
    }

    /**
     * missing-config.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageNoConfigsImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithMissingConfigFile(ContainerInitializer.ContainerTestImageNoConfigs targetImage) {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        container.waitingFor(noConfigWaitStrategy());

        container.start();
        container.stop();
    }

    /**
     * profile-default-enabled-false.sh
     */
    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testFailStartupWithProfileDefaultEnabledFalse(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);
        // Wait for the log message when running container.start()
        container.waitingFor(noConfigWaitStrategy());

        // Setup the config file
        File testConfig = new File(anotherTempDir, "ors-config.yml");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        // Create the profile_default node
        ObjectNode profileDefaultNode = mapper.createObjectNode();
        profileDefaultNode.put("enabled", false);
        profileDefaultNode.put("source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        profileDefaultNode.put("graph_path", "/home/ors/openrouteservice/graphs");
        // Create the engine object
        ObjectNode engineNode = mapper.createObjectNode();
        engineNode.set("profile_default", profileDefaultNode);
        // Add the engine object to the root node
        rootNode.set("ors", mapper.createObjectNode().set("engine", engineNode));
        // Write the JsonNode to a YAML file
        YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory());
        yamlMapper.writeValue(testConfig, rootNode);

        // Add the config file to te container and overwrite the default config
        container.withCopyFileToContainer(forHostPath(testConfig.getPath()), "/home/ors/openrouteservice/ors-config.yml");

        // Start the container. Succeeds if the expected log message is found.
        container.start();

        // Shutdown the container
        container.stop();
    }

    @MethodSource("utils.ContainerInitializer#ContainerTestImageDefaultsImageStream")
    @ParameterizedTest(name = "{0}")
    void testPropertyOverridesDefaultConfig(ContainerInitializer.ContainerTestImageDefaults targetImage) throws IOException {
        GenericContainer<?> container = initContainer(targetImage, true, false);

        container.addEnv("ors.engine.profiles.driving-hgv.enabled", "true");

        container.start();

        JsonNode profiles = OrsApiRequests.getProfiles(container.getHost(), container.getFirstMappedPort());
        Assertions.assertEquals(2, profiles.size());

        List<String> expectedProfiles = List.of("driving-car", "driving-hgv");
        for (JsonNode profile : profiles) {
            Assertions.assertTrue(expectedProfiles.contains(profile.get("profiles").asText()));
        }
    }
}