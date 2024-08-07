package org.heigit.ors.config.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EnvDeserializer {


    public static ObjectNode envPropertiesToJson(List<Map.Entry<String, String>> envVars) {
        if (envVars == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        Properties properties = new Properties();
        envVars.forEach(entry -> properties.setProperty(entry.getKey(), entry.getValue()));

        properties.forEach((key, value) -> {
            String[] parts = ((String) key).split("\\.");
            ObjectNode current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                if (!current.has(parts[i])) {
                    current.set(parts[i], mapper.createObjectNode());
                }
                current = (ObjectNode) current.get(parts[i]);
            }
            current.put(parts[parts.length - 1], (String) value);
        });
        return root;
    }

}
