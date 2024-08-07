package org.heigit.ors.config.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EnvDeserializer {


    public static String envPropertiesToJson(List<Map.Entry<String, String>> envVars) {
        Logger logger = LoggerFactory.getLogger(EnvDeserializer.class);
        if (envVars == null) {
            return "{}";
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        Properties properties = new Properties();
        envVars.forEach(entry -> properties.setProperty(entry.getKey().replace("_", "."), entry.getValue()));

        properties.forEach((key, value) -> {
            String[] parts = ((String) key).split("\\.");
            // if key is empty, ignore it and continue
            if (parts.length == 0 || parts[0].isEmpty()) {
                return;
            }
            ObjectNode current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                if (!current.has(parts[i])) {
                    current.set(parts[i], mapper.createObjectNode());
                }
                current = (ObjectNode) current.get(parts[i]);
            }
            current.put(parts[parts.length - 1], (String) value);
        });
        String jsonString = "{}";
        try {
            jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to convert properties to JSON", e);
        }
        return jsonString;
    }

}
