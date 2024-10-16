package utils.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class OrsConfigHelper {

    public static Path setupConfigFileProfileDefaultFalse(Path tempDir, String fileName) throws IOException {
        Path testConfig = tempDir.resolve(fileName);
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
        yamlMapper.writeValue(testConfig.toFile(), rootNode);

        return testConfig;
    }

    public static Path configWithCustomProfilesActivated(Path tempDir, String fileName, Map<String, Boolean> profiles) {
        Path testConfig = tempDir.resolve(fileName);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        // Create the profile_default node
        ObjectNode profileDefaultNode = mapper.createObjectNode();
        profileDefaultNode.put("enabled", false);
        profileDefaultNode.put("source_file", "/home/ors/openrouteservice/files/heidelberg.test.pbf");
        profileDefaultNode.put("graph_path", "/home/ors/openrouteservice/graphs");


        // Create the profiles node
        ObjectNode profilesNode = mapper.createObjectNode();
        for (String profile : profiles.keySet()) {
            // Add each profile to the profiles node
            profilesNode.set(profile, mapper.createObjectNode().put("enabled", profiles.get(profile)));
        }

        // Create the engine object
        ObjectNode engineNode = mapper.createObjectNode();
        engineNode.set("profile_default", profileDefaultNode);
        engineNode.set("profiles", profilesNode);

        // Add the engine object to the root node
        rootNode.set("ors", mapper.createObjectNode().set("engine", engineNode));

        // Write the JsonNode to a YAML file
        YAMLMapper yamlMapper = new YAMLMapper(new YAMLFactory());
        try {
            yamlMapper.writeValue(testConfig.toFile(), rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return testConfig;
    }
}
