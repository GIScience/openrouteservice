package org.heigit.ors.api.util;

import org.heigit.ors.api.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class,
        properties = {
            "ors.engine.config_output=./target/config_output.yml",
            "ors.engine.dynamic_data.enabled=false"
        })
@ActiveProfiles("test")
class ConfigOutputTest {

    @Test
    void testConfigOutputFile() {
        YAMLMapper mapper = YAMLMapper.builder().build();
        JsonNode configOutput = mapper.readTree(new File("./target/config_output.yml"));
        assertTrue(configOutput.has("ors"));
        assertTrue(configOutput.has("logging"));
        assertTrue(configOutput.has("server"));
        assertTrue(configOutput.has("spring"));
        assertTrue(configOutput.has("springdoc"));
        assertTrue(configOutput.get("ors").has("engine"));
        assertTrue(configOutput.get("ors").has("cors"));
        assertTrue(configOutput.get("ors").has("messages"));
        assertTrue(configOutput.get("ors").has("endpoints"));
    }
}
