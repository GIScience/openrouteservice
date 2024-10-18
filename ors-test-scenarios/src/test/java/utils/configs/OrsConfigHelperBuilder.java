package utils.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Builder(toBuilder = true, access = AccessLevel.PUBLIC)
public class OrsConfigHelperBuilder {
    private String ProfileDefaultBuildSourceFile;
    private String ProfileDefaultGraphPath;
    private boolean profileDefaultEnabled;

    @Builder.Default
    private Map<String, Boolean> profiles = new HashMap<>();

    public Path toYaml(Path tempDir, String fileName) {
        Path testConfig = tempDir.resolve(fileName);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        // Create the profile_default node
        ObjectNode profileDefaultNode = mapper.createObjectNode();
        profileDefaultNode.put("enabled", profileDefaultEnabled);
        profileDefaultNode.put("graph_path", ProfileDefaultGraphPath);
        // Create profile_default build node
        ObjectNode profileDefaultBuildNode = mapper.createObjectNode();
        profileDefaultBuildNode.put("source_file", ProfileDefaultBuildSourceFile);
        profileDefaultNode.set("build", profileDefaultBuildNode);

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