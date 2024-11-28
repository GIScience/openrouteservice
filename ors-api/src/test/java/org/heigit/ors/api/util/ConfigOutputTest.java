package org.heigit.ors.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.heigit.ors.api.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class,
        properties = "ors.engine.config_output=./target/config_output.yml")
@ActiveProfiles("test")
class ConfigOutputTest {

    @Test
    void testConfigOutputFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
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
