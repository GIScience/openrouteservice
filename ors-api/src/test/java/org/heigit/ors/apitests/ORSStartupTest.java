package org.heigit.ors.apitests;

import org.heigit.ors.api.services.EngineService;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.exceptions.ORSGraphFileManagerException;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphBuildInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.*;

class ORSStartupTest extends ServiceTest {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final EngineService engineService;

    @Autowired
    public ORSStartupTest(EngineService engineService) {
        this.engineService = engineService;
    }

    @Test
    void testGraphBuildInfoFilesWrittenCorrectly() throws ParseException, ORSGraphFileManagerException {
        RoutingProfileManager rpm = engineService.waitForActiveRoutingProfileManager();
        RoutingProfile profile = rpm.getRoutingProfile(EncoderNameEnum.DRIVING_CAR.getEncoderName());
        GraphBuildInfo graphBuildInfo = profile.getGraphhopper().getOrsGraphManager().getActiveGraphBuildInfo();
        ProfileProperties profileProperties = graphBuildInfo.getPersistedGraphBuildInfo().getProfileProperties();
        assertEquals(DATE_FORMAT.parse("2024-09-08T20:21:00+0000"), graphBuildInfo.getPersistedGraphBuildInfo().getOsmDate(), "graph_build_info should contain OSM data timestamp");
        assertEquals(EncoderNameEnum.DRIVING_CAR, profileProperties.getEncoderName(), "Encoder name should be set in the graph_build_info");
        assertTrue(profileProperties.getBuild().getElevation(), "Elevation should be set in the graph_build_info");
        assertNull(profileProperties.getService().getMaximumDistance(), "Maximum distance should not be set in the graph_build_info");
        assertNull(profileProperties.getBuild().getGtfsFile(), "GTFS file path settings should not be set in the graph_build_info");
        assertTrue(profileProperties.getRepo().isEmpty(), "Repo settings should not be set in the graph_build_info");
        assertTrue(profileProperties.getService().getExecution().isEmpty(), "Execution settings should not be set in the graph_build_info");
        assertEquals("turn_costs=true|block_fords=false|use_acceleration=true|maximum_grade_level=1|conditional_access=true|conditional_speed=true|enable_custom_models=true", profileProperties.getBuild().getEncoderOptions().toString(), "Encoder options should be set in the graph_build_info");
        assertFalse(profileProperties.getBuild().getPreparation().isEmpty(), "Preparation settings should be set in the graph_build_info");
        assertTrue(profileProperties.getBuild().getPreparation().getMethods().getCore().getEnabled(), "Preparation settings should contain enabled core method");
        assertFalse(profileProperties.getBuild().getExtStorages().isEmpty(), "ExtStorages settings should be set in the graph_build_info");
    }
}
