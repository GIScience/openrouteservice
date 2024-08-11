package org.heigit.ors.api.config.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.support.JsonMapFlattener;

import java.util.*;

public class EnvironmentVariableParser {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariableParser.class);

    public static List<Map.Entry<String, String>> validateEnvironmentVariablesAgainstClassJson(String classAsJson, List<String> relevantPrefixes, List<Map.Entry<String, String>> envVars) {
        if (envVars == null || envVars.isEmpty()) {
            return Collections.emptyList();
        }

        if (relevantPrefixes == null || relevantPrefixes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map.Entry<String, String>> envVarsFiltered = filterEnvironmentVariables(envVars, relevantPrefixes);
        Map<String, String> cleanedAndHarmonizedVariables = new HashMap<>();

        try {
            Map<String, String> engineProperties = new ObjectMapper().readValue(classAsJson, Map.class);
            Map<String, String> flattenedProperties = JsonMapFlattener.flattenToStringMap(engineProperties);
            Map<String, String> capitalizedLinkMap = convertToUpperCaseEnv(flattenedProperties);

            for (Map.Entry<String, String> entry : envVarsFiltered) {
                String key = entry.getKey();
                if (flattenedProperties.containsKey(key) && !cleanedAndHarmonizedVariables.containsKey(key)) {
                    cleanedAndHarmonizedVariables.put(key, entry.getValue());
                }
                if (capitalizedLinkMap.containsKey(key)) {
                    cleanedAndHarmonizedVariables.put(capitalizedLinkMap.get(key), entry.getValue());
                }
            }
        } catch (JsonProcessingException e) {
            logger.warn("Error parsing object to json: {}", e.getMessage());
        }
        return new ArrayList<>(cleanedAndHarmonizedVariables.entrySet());
    }

    private static List<Map.Entry<String, String>> filterEnvironmentVariables(List<Map.Entry<String, String>> envVars, List<String> relevantPrefixes) {
        List<Map.Entry<String, String>> filteredVars = new ArrayList<>();
        for (Map.Entry<String, String> entry : envVars) {
            String key = entry.getKey().strip();
            for (String prefix : relevantPrefixes) {
                if (key.startsWith(prefix)) {
                    filteredVars.add(Map.entry(key.replace(prefix, ""), entry.getValue()));
                }
            }
        }
        return filteredVars;
    }

    private static Map<String, String> convertToUpperCaseEnv(Map<String, String> stringStringMap) {
        Map<String, String> capitalizedLinkMap = new HashMap<>();
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            String newKey = entry.getKey().toUpperCase().replace(".", "_").replace("-", "_");
            capitalizedLinkMap.put(newKey, entry.getKey());
        }
        return capitalizedLinkMap;
    }
}
