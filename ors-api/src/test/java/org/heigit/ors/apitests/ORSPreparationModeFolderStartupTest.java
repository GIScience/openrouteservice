package org.heigit.ors.apitests;

import org.heigit.ors.api.Application;
import org.heigit.ors.api.services.EngineService;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;

import static org.heigit.ors.util.AppInfo.GRAPH_VERSION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Application.class)
@ActiveProfiles("prepmode-folder")
class ORSPreparationModeFolderStartupTest {
    private final EngineService engineService;

    @Autowired
    public ORSPreparationModeFolderStartupTest(EngineService engineService) {
        this.engineService = engineService;
    }

    @Test
    void testGraphFolderKeptExtracted() {
        String profileName = "driving-car-prepmode-folder";
        RoutingProfileManager rpm = engineService.waitForInitializedRoutingProfileManager();
        ProfileProperties profileProperties = rpm.getRoutingProfile(profileName).getProfileProperties();
        assertTrue(engineService.getRoutingProfileManager().isReady());
        assertTrue(engineService.getRoutingProfileManager().isShutdown());
        assertFalse(engineService.getRoutingProfileManager().hasFailed());
        String filename = RoutingProfile.getPreparationBaseFilename(profileProperties, GRAPH_VERSION);
        assertTrue(Files.isDirectory(profileProperties.getGraphPath().resolve(profileName)), "Graph folder should exist");
        assertTrue(Files.exists(profileProperties.getGraphPath().resolve(profileName).resolve("graph_build_info.yml")), "Graph build info should remain inside the graph folder");
        assertFalse(Files.exists(profileProperties.getGraphPath().resolve(filename + ".ghz")), "Packed graph file should not exist");
        assertFalse(Files.exists(profileProperties.getGraphPath().resolve(filename + ".yml")), "Sidecar graph info file should not exist");
    }
}
