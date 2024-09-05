package org.heigit.ors.routing.graphhopper.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.GraphManagementProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.FlatORSGraphFolderStrategy;
import org.heigit.ors.routing.graphhopper.extensions.manage.local.ORSGraphFileManager;
import org.heigit.ors.routing.graphhopper.extensions.manage.remote.NamedGraphsRepoStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

        gh.initializeGraphManagement("0");
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

    @Test
    public void profileHashAddedToGraphHopperLocationWithDeepHashStrategy() throws Exception {
        ORSGraphHopper gh = createORSGraphHoopperWithoutOsmFile("graphs-apitests", "https://my.domain.com/");
        String pathBefore = gh.getGraphHopperLocation();
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.from(gh.getEngineProperties(), ROUTE_PROFILE_NAME, "0").build();

        gh.initializeGraphManagementWithDeepHashBasedStructure(managementProps);

        String pathAfter = gh.getGraphHopperLocation();
        assertNotEquals(pathAfter, pathBefore);
        assertTrue(pathAfter.endsWith("graphs-apitests/driving-car/6f779cc93716407f07c169054b12f307"));
    }

    @Test
    public void noProfileHashAddedToGraphHopperLocationWithFlatStrategy() throws Exception {
        ORSGraphHopper gh = createORSGraphHoopperWithOsmFile("graphs-apitests", "https://my.domain.com/");
        String pathBefore = gh.getGraphHopperLocation();
        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.from(gh.getEngineProperties(), ROUTE_PROFILE_NAME, "0").build();

        gh.initializeGraphManagementWithFlatStructure(managementProps);

        String pathAfter = gh.getGraphHopperLocation();
        assertNotEquals(pathAfter, pathBefore);
        assertTrue(pathAfter.endsWith("graphs-apitests/driving-car"));
    }

    @Test
    public void importOrLoad_orsGraphManagerCreated_usesRepo() throws Exception {
        ORSGraphHopper gh = createORSGraphHoopperWithoutOsmFile("repoDir", "https://my.domain.com/");

        gh.initializeGraphManagement("0");

        ORSGraphManager orsGraphManager = gh.getOrsGraphManager();
        assertNotNull(orsGraphManager);
        assertTrue(orsGraphManager.useGraphRepository());
    }

    @Test
    public void importOrLoad_orsGraphManagerCreated_notUsingRepo() throws Exception {
        ORSGraphHopper gh = createORSGraphHoopperWithoutOsmFile("repoDir", null);

        gh.initializeGraphManagement("0");

        ORSGraphManager orsGraphManager = gh.getOrsGraphManager();
        assertNotNull(orsGraphManager);
        assertFalse(orsGraphManager.useGraphRepository());
    }

    private static EngineProperties createEngineProperties(Path localGraphsRootPath,
                                                          String graphManagementRepositoryUrl,
                                                          String graphManagementRepositoryName,
                                                          String graphManagementRepositoryProfileGroup,
                                                          String graphManagementGraphExtent,
                                                          String profileName, int graphManagementMaxBackups) throws JsonProcessingException {

        String testJson = """
            {
              "graph_management": {
                "graph_extent": null,
                "repository_uri": null,
                "repository_name": null,
                "repository_profile_group": null,
                "download_schedule": "0 0 0 31 2 *",
                "activation_schedule": "0 0 0 31 2 *",
                "max_backups": 0
              },           
              "profile_default": {
                "enabled": false,
                "source_file": "/path/to/source/file",
                "graph_path": "/path/to/graphs",
                "preparation": {
                  "min_network_size": 300,
                  "methods": {
                    "lm": {
                      "enabled": false,
                      "weightings": "shortest",
                      "landmarks": 2
                    }
                  }
                },
                "execution": {
                  "methods": {
                    "lm": {
                      "active_landmarks": 2
                    }
                  }
                },
                "ext_storages": {
                  "WayCategory": {
                    "enabled": true
                  },
                  "GreenIndex": {
                    "enabled": true,
                    "filepath": "/path/to/file.csv"
                  }
                }
              },
              "profiles": {
                "driving-car": {
                  "encoder_name": "driving-car",
                  "encoder_options": {},
                  "preparation": {
                    "methods": {
                      "lm": {
                        "enabled": true,
                        "threads": 5
                      }
                    }
                  },
                  "execution": {
                    "methods": {
                      "lm": {
                        "active_landmarks": 2
                      }
                    }
                  },
                  "ext_storages": {}
                }
              }
            }""";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        EngineProperties engineProperties = mapper.readValue(testJson, EngineProperties.class);

        engineProperties.getProfileDefault().setGraphPath(localGraphsRootPath);
        engineProperties.getProfiles().get(profileName).setEnabled(true);
        engineProperties.getProfiles().get(profileName).setMaximumWayPoints(50);

        GraphManagementProperties graphManagementProperties = engineProperties.getGraphManagement();
        graphManagementProperties.setRepositoryUri(graphManagementRepositoryUrl);
        graphManagementProperties.setRepositoryName(graphManagementRepositoryName);
        graphManagementProperties.setRepositoryProfileGroup(graphManagementRepositoryProfileGroup);
        graphManagementProperties.setGraphExtent(graphManagementGraphExtent);
        graphManagementProperties.setMaxBackups(graphManagementMaxBackups);

        return engineProperties;
    }

    private static ORSGraphHopper createORSGraphHopper(ORSGraphHopperConfig ghConfig,
                                                       EngineProperties engineProperties) throws Exception {
        GraphProcessContext gpc = new GraphProcessContext(engineProperties.getProfiles().get(ROUTE_PROFILE_NAME));
        gpc.setGetElevationFromPreprocessedData(true);

        ORSGraphHopper gh = new ORSGraphHopper(gpc, engineProperties);
        gh.init(ghConfig);
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        gh.setRouteProfileName(ROUTE_PROFILE_NAME);
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

    private static ORSGraphHopper createORSGraphHoopperWithoutOsmFile(String repoDir, String repoUrl) throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithoutOsmFile();
        Path repoPath = repoDir.isEmpty() ? null : Path.of(repoDir);
        EngineProperties engineProperties = createEngineProperties(repoPath, repoUrl,
                "repoName", "profileGroup", "graphExtent",
                ROUTE_PROFILE_NAME, 0
        );
        return createORSGraphHopper(ghConfig, engineProperties);
    }

    private static ORSGraphHopper createORSGraphHoopperWithOsmFile(String repoDir, String repoUrl) throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithOsmFile();

        Path repoPath = repoDir.isEmpty() ? null : Path.of(repoDir);
        EngineProperties engineProperties = createEngineProperties(repoPath, repoUrl,
                "repoName", "profileGroup", "graphExtent",
                ROUTE_PROFILE_NAME, 0
        );
        return createORSGraphHopper(ghConfig, engineProperties);
    }

    @ParameterizedTest
    @CsvSource({
            "HttpRepoManager, http://my.domain.com",
            "HttpRepoManager, https://my.domain.com/",
            "NullRepoManager, file:relative/path",
            "NullRepoManager, file://relative/path",
            "NullRepoManager, file://relative/path.txt",
            "FileSystemRepoManager, file:///absolute/path",
            "FileSystemRepoManager, file:///absolute/path.txt",
            "FileSystemRepoManager, relative/path",
            "FileSystemRepoManager, relative/path.txt",
            "FileSystemRepoManager, /absolute/path",
            "FileSystemRepoManager, /absolute/path.txt",
            "FileSystemRepoManager, ~/absolute/path",
            "FileSystemRepoManager, ~/absolute/path.txt"
    })
    public void getOrsGraphRepoManager(String className, String repoUri) throws ClassNotFoundException {

        ORSGraphHopper orsGraphHopper = new ORSGraphHopper();
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.getProfileDefault().setGraphPath(Path.of("graphs"));
        engineProperties.getGraphManagement().setRepositoryUri(repoUri);

        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withLocalGraphsRootAbsPath("graphs")
                .withRepoBaseUri(repoUri)
                .withGraphVersion("1")
                .withLocalProfileName("driving-car")
                .build();
        FlatORSGraphFolderStrategy orsGraphFolderStrategy = new FlatORSGraphFolderStrategy(managementProps);
        ORSGraphFileManager orsGraphFileManager = new ORSGraphFileManager(managementProps, orsGraphFolderStrategy);
        NamedGraphsRepoStrategy orsGraphRepoStrategy = new NamedGraphsRepoStrategy(managementProps);

        assertEquals(className, orsGraphHopper.getOrsGraphRepoManager(managementProps, orsGraphRepoStrategy, orsGraphFileManager).getClass().getSimpleName());
    }

}