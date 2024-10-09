package org.heigit.ors.apitests;

import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.RoutingProfile;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ORSStartupTest extends ServiceTest {
    @Test
    void testGraphInfoFilesWrittenCorrectly() {
        RoutingProfileManager rpm = RoutingProfileManager.getInstance();
        RoutingProfile profile = rpm.getRoutingProfile(EncoderNameEnum.DRIVING_CAR.getName());
        GraphInfo graphInfo = profile.getGraphhopper().getOrsGraphManager().getActiveGraphInfo();
        ProfileProperties profileProperties = graphInfo.getPersistedGraphInfo().getProfileProperties();

        assertEquals("Sun Sep 08 22:21:00 CEST 2024", graphInfo.getPersistedGraphInfo().getOsmDate().toString(), "graph_info should contain OSM data timestamp");
        assertEquals(EncoderNameEnum.DRIVING_CAR, profileProperties.getEncoderName(), "Encoder name should be set in the graph_info");
        assertTrue(profileProperties.getBuild().getElevation(), "Elevation should be set in the graph_info");
        assertNull(profileProperties.getService().getMaximumDistance(), "Maximum distance should not be set in the graph_info");
        assertNull(profileProperties.getBuild().getGtfsFile(), "GTFS file path settings should not be set in the graph_info");
        assertTrue(profileProperties.getRepo().isEmpty(), "Repo settings should not be set in the graph_info");
        assertTrue(profileProperties.getService().getExecution().isEmpty(), "Execution settings should not be set in the graph_info");
        assertEquals("turn_costs=true|block_fords=false|use_acceleration=true|maximum_grade_level=1|conditional_access=true|conditional_speed=true", profileProperties.getBuild().getEncoderOptions().toString(), "Encoder options should be set in the graph_info");
        assertFalse(profileProperties.getBuild().getPreparation().isEmpty(), "Preparation settings should be set in the graph_info");
        assertTrue(profileProperties.getBuild().getPreparation().getMethods().getCore().getEnabled(), "Preparation settings should contain enabled core method");
        assertFalse(profileProperties.getBuild().getExtStorages().isEmpty(), "ExtStorages settings should be set in the graph_info");
        assertTrue(profileProperties.getBuild().getExtStorages().get("WayCategory").getEnabled(), "ExtStorages settings should contain enabled WayCategory storage");
    }
}
