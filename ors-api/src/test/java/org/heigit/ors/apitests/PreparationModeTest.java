package org.heigit.ors.apitests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.heigit.ors.api.Application;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphBuildInfo;
import org.heigit.ors.util.AppInfo;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("preparation-mode-test")
@DirtiesContext
@Order(value = Integer.MAX_VALUE)
class PreparationModeTest {

    @Autowired
    private EngineProperties engineProperties;

    @Test
    void testPrepMode() throws IOException {
        assertTrue(engineProperties.getPreparationMode());
        await().atMost(Duration.ofSeconds(60)).until(RoutingProfileManagerStatus::isReady);
        assertTrue(RoutingProfileManagerStatus.isReady());

        Path graphPath = RoutingProfileManager.getInstance().getRoutingProfile("driving-car-preparation").getProfileProperties().getGraphPath();
        String fileName = "test_heidelberg_%s_driving-car".formatted(AppInfo.GRAPH_VERSION);
        File yamlFile = Paths.get(graphPath.toString(), fileName + ".yml").toFile();
        assertTrue(yamlFile.exists(), "Graph info YAML should exist");
        assertTrue(Paths.get(graphPath.toString(), fileName + ".ghz").toFile().exists(), "Packed graph should exist");
        assertFalse(Paths.get(graphPath.toString(), "driving-car-preparation").toFile().exists(), "Build graph dir should not exist");

        // parse yaml file and check for expected values
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        PersistedGraphBuildInfo graphBuildInfo = mapper.readValue(yamlFile, PersistedGraphBuildInfo.class);
        assertNotNull(graphBuildInfo.getGraphBuildDate(), "Graph build date should be set");
        assertNotNull(graphBuildInfo.getOsmDate(), "OSM date should be set");
        assertNotNull(graphBuildInfo.getGraphVersion(), "Graph version should be set");
        assertNotNull(graphBuildInfo.getGraphSizeBytes(), "Graph size bytes should be set");
        assertNotNull(graphBuildInfo.getProfileProperties(), "Profile properties should be set");
    }
}


