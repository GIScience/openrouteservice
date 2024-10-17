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

@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Builder(toBuilder = true, access = AccessLevel.PUBLIC)
public class GrcConfigBuilder {
    public String repositoryUri;
    public String repositoryName;
    public String repositoryProfileGroup;
    public String graphExtent;
    public String profileDefaultGraphPath;

    @Builder.Default
    private HashMap<String, Boolean> profiles = new HashMap<>();
    @Builder.Default
    private boolean graphManagementEnabled = true;
    @Builder.Default
    private boolean setRepoManagementPerProfile = false;

    public Path toYAML(Path tempDir, String fileName) {
        Path testConfig = tempDir.resolve(fileName);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        // Create the graph_management node
        ObjectNode graphManagementNode = mapper.createObjectNode();
        graphManagementNode.put("enabled", graphManagementEnabled);

        // Create the repo node
        ObjectNode repoNode = mapper.createObjectNode();
        repoNode.put("repository_uri", repositoryUri);
        repoNode.put("repository_name", repositoryName);
        repoNode.put("repository_profile_group", repositoryProfileGroup);
        repoNode.put("graph_extent", graphExtent);

        // Create the profiles node
        ObjectNode profilesNode = mapper.createObjectNode();
        ObjectNode drivingCarNode = mapper.createObjectNode();
        for (String profile : profiles.keySet()) {
            // Add each profile to the profiles node
            drivingCarNode.put("enabled", profiles.get(profile));
            if (setRepoManagementPerProfile) {
                drivingCarNode.set("repo", repoNode);
            }
            profilesNode.set(profile, drivingCarNode);
        }

        // Create the profile_default node
        ObjectNode profileDefaultNode = mapper.createObjectNode();
        profileDefaultNode.put("source_file", "");
        if (profileDefaultGraphPath != null) {
            profileDefaultNode.put("graph_path", profileDefaultGraphPath);
        }

        // If setGraphManagementPerProfile is false, set the graph management in profile_default
        if (!setRepoManagementPerProfile) {
            profileDefaultNode.set("repo", repoNode);
        }


        // Create the engine object
        ObjectNode engineNode = mapper.createObjectNode();
        engineNode.set("profile_default", profileDefaultNode);
        engineNode.set("graph_management", graphManagementNode);
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