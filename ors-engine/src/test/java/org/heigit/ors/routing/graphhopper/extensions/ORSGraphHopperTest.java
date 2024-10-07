package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ORSGraphHopperTest {
    static final String GH_PROFILE_NAME = "car_ors";
    static final String ROUTE_PROFILE_NAME = "driving-car";

    @Test
    void directRouteTest() {
        GHRequest ghRequest = new GHRequest(49.41281601436809, 8.686215877532959, 49.410163456220076, 8.687160015106201);
        GHResponse ghResponse = new ORSGraphHopper().constructFreeHandRoute(ghRequest);

        assertTrue(ghResponse.getHints().has("skipped_segment"));
        assertTrue(ghResponse.getHints().getBool("skipped_segment", false));

        assertEquals(1, ghResponse.getAll().size());
        ResponsePath responsePath = ghResponse.getAll().get(0);

        assertEquals(0, responsePath.getErrors().size());
        assertEquals(0, responsePath.getDescription().size());
        assertEquals(309.892f, responsePath.getDistance(), 3);
        assertEquals(0.0, responsePath.getAscend(), 0);
        assertEquals(0.0, responsePath.getDescend(), 0);
        assertEquals(0.0, responsePath.getRouteWeight(), 0);
        assertEquals(0, responsePath.getTime());
        assertEquals("", responsePath.getDebugInfo());
        assertEquals(2, responsePath.getInstructions().size());
        assertEquals(1, responsePath.getInstructions().get(0).getPoints().size());
        assertEquals(0, responsePath.getNumChanges());
        assertEquals(0, responsePath.getLegs().size());
        assertEquals(0, responsePath.getPathDetails().size());
        assertNull(responsePath.getFare());
        assertFalse(responsePath.isImpossible());

        checkInstructions(responsePath.getInstructions());
        checkPointList(responsePath.getWaypoints());
        checkPointList(responsePath.getPoints());

    }

    /**
     * This tests loading an OSM dataset preprocessed with ors-preprocessor, as used on our production servers.
     * For testing purposes, a subset from the Heidelberg graph has been modified with several invalid ele tag
     * values to demonstrate that the graph will still build, albeit with incorrect but valid elevation values.
     * <node id="...">
     * <tag k="ele" v="invalid ele tag"/>
     * </node>
     * => NaN, elevation will be set to 0
     * <node id="...">
     * <tag k="ele" v="198.0.0.4"/>
     * </node>
     * => NaN, elevation will be set to 0
     * <node id="...">
     * <tag k="ele" v="1,912.1"/>
     * </node>
     * => 1912.1
     * <node id="...">
     * <tag k="ele" v="1.021,12"/>
     * </node>
     * => 1.02112
     */
    @Test
    void buildGraphWithPreprocessedData() throws Exception {
        ORSGraphHopper gh = createORSGraphHoopperWithOsmFile("repoDir", "https://my.domain.com/");
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withEnabled(true)
                .withRepoName("repoName")
                .withRepoBaseUri("http://my.domain.com")
                .build();
        ORSGraphManager orsGraphManager = ORSGraphManager.initializeGraphManagement(managementProps);
        gh.setOrsGraphManager(orsGraphManager);

        gh.importOrLoad();

        ORSGraphHopperStorage storage = (ORSGraphHopperStorage) gh.getGraphHopperStorage();
        assertEquals(419, storage.getNodes());
    }

    private void checkInstructions(InstructionList instructions) {
        for (Instruction instruction : instructions) {
            PointList points = instruction.getPoints();

            assertEquals(2, points.getDimension());
            assertFalse(points.isEmpty());
            assertFalse(points.is3D());
            assertFalse(points.isImmutable());
            assertEquals(0, instruction.getExtraInfoJSON().size());

            if (instruction.getName().equals("free hand route") && instruction.getSign() == Instruction.REACHED_VIA) {
                assertEquals(1, instruction.getPoints().size());
                assertEquals(49.41281601436809, instruction.getPoints().getLat(0), 0);
                assertEquals(8.686215877532959, instruction.getPoints().getLon(0), 0);
                assertEquals(309.892f, instruction.getDistance(), 3);
                assertEquals(0, instruction.getTime());
            } else if (instruction.getName().equals("end of free hand route") && instruction.getSign() == Instruction.FINISH) {
                assertEquals(1, instruction.getPoints().size());
                assertEquals(49.410163456220076, instruction.getPoints().getLat(0), 0);
                assertEquals(8.687160015106201, instruction.getPoints().getLon(0), 0);
                assertEquals(0.0, instruction.getDistance(), 0);
                assertEquals(0, instruction.getTime());
            } else {
                fail("The name or instruction sign of the skipped_segments instructions are wrong.");
            }
        }

    }

    private void checkPointList(PointList waypoints) {
        assertFalse(waypoints.is3D());
        assertFalse(waypoints.isImmutable());
        assertEquals(2, waypoints.size());
        assertEquals(49.41281601436809, waypoints.getLat(0), 0);
        assertEquals(49.410163456220076, waypoints.getLat(1), 0);
        assertEquals(8.686215877532959, waypoints.getLon(0), 0);
        assertEquals(8.687160015106201, waypoints.getLon(1), 0);
    }

    private static EngineProperties createEngineProperties(Path localGraphsRootPath,
                                                           String graphManagementRepositoryUrl,
                                                           String graphManagementRepositoryName,
                                                           String graphManagementRepositoryProfileGroup,
                                                           String graphManagementGraphExtent,
                                                           String profileName, int graphManagementMaxBackups) {

        EngineProperties engineProperties = new EngineProperties();

        engineProperties.getGraphManagement().setEnabled(true);
        engineProperties.getGraphManagement().setDownloadSchedule("0 0 0 31 2 *");
        engineProperties.getGraphManagement().setActivationSchedule("0 0 0 31 2 *");
        engineProperties.getGraphManagement().setMaxBackups(graphManagementMaxBackups);

        engineProperties.getProfileDefault().setEnabled(false);
        engineProperties.getProfileDefault().setGraphPath(Path.of(localGraphsRootPath.toString()));
        engineProperties.getProfileDefault().setSourceFile(Path.of("/path/to/source/file"));
        engineProperties.getProfileDefault().getPreparation().setMinNetworkSize(300);
        engineProperties.getProfileDefault().getPreparation().getMethods().getLm().setEnabled(false);
        engineProperties.getProfileDefault().getPreparation().getMethods().getLm().setWeightings("shortest");
        engineProperties.getProfileDefault().getPreparation().getMethods().getLm().setLandmarks(2);
        engineProperties.getProfileDefault().getExecution().getMethods().getLm().setActiveLandmarks(2);

        ExtendedStorageProperties wayCategory = new ExtendedStorageProperties();
        wayCategory.setEnabled(true);
        engineProperties.getProfileDefault().getExtStorages().put("WayCategory", wayCategory);

        ExtendedStorageProperties greenIndex = new ExtendedStorageProperties();
        greenIndex.setEnabled(true);
        greenIndex.setFilepath(Path.of("/path/to/file.csv"));
        engineProperties.getProfileDefault().getExtStorages().put("GreenIndex", greenIndex);

        ProfileProperties carProperties = new ProfileProperties();
        carProperties.setProfileName(profileName);
        carProperties.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        carProperties.getPreparation().getMethods().getLm().setEnabled(true);
        carProperties.getPreparation().getMethods().getLm().setThreads(5);
        carProperties.getExecution().getMethods().getLm().setActiveLandmarks(2);
        carProperties.setMaximumWayPoints(50);
        carProperties.getRepo().setRepositoryUri(graphManagementRepositoryUrl);
        carProperties.getRepo().setRepositoryName(graphManagementRepositoryName);
        carProperties.getRepo().setRepositoryProfileGroup(graphManagementRepositoryProfileGroup);
        carProperties.getRepo().setGraphExtent(graphManagementGraphExtent);
        engineProperties.getProfiles().put(profileName, carProperties);

        return engineProperties;
    }

    private static ORSGraphHopper createORSGraphHopper(ORSGraphHopperConfig ghConfig,
                                                       EngineProperties engineProperties, ProfileProperties profileProperties) throws Exception {
        GraphProcessContext gpc = new GraphProcessContext(engineProperties.getProfiles().get(ROUTE_PROFILE_NAME));
        gpc.setGetElevationFromPreprocessedData(true);

        ORSGraphHopper gh = new ORSGraphHopper(gpc, engineProperties, profileProperties);
        gh.init(ghConfig);
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        gh.setProfileName(ROUTE_PROFILE_NAME);
        return gh;
    }

    private static ORSGraphHopperConfig createORSGraphHopperConfigWithOsmFile() {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithoutOsmFile();
        ghConfig.putObject("datareader.file", "src/test/files/preprocessed_osm_data.pbf");
        return ghConfig;
    }

    private static ORSGraphHopperConfig createORSGraphHopperConfigWithoutOsmFile() {
        ORSGraphHopperConfig ghConfig = new ORSGraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", "RAM");
        ghConfig.putObject("graph.location", "unittest.testgraph");
        ghConfig.setProfiles(List.of(new Profile(GH_PROFILE_NAME).setVehicle("car").setWeighting("fastest").setTurnCosts(true)));
        return ghConfig;
    }

    private static ORSGraphHopper createORSGraphHoopperWithOsmFile(String repoDir, String repoUrl) throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithOsmFile();

        Path repoPath = repoDir.isEmpty() ? null : Path.of(repoDir);
        EngineProperties engineProperties = createEngineProperties(repoPath, repoUrl,
                "repoName", "profileGroup", "graphExtent",
                ROUTE_PROFILE_NAME, 0
        );
        return createORSGraphHopper(ghConfig, engineProperties, engineProperties.getProfiles().get(ROUTE_PROFILE_NAME));
    }

}