package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import org.heigit.ors.routing.configuration.RouteProfileConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ORSGraphHopperTest {

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
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfig();
        ORSGraphHopper gh = createORSGraphHopper(ghConfig);
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
    public void profileHashAddedToGraphHopperLocation() throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfig();
        ORSGraphHopper gh = createORSGraphHopper(ghConfig);

        String pathBefore = gh.getGraphHopperLocation();
        String profileHash = gh.createProfileHash();
        gh.importOrLoad();
        String pathAfter = gh.getGraphHopperLocation();

        assertNotEquals(pathAfter, pathBefore);
        assertEquals("%s/%s".formatted(pathBefore, profileHash), pathAfter);
    }

    static class CountingHashCollector {
        public int callCount = 0;
        public Set<String> hashes = new HashSet<>();
        void createHashAndCount(ORSGraphHopper gh){
            callCount++;
            String profileHash = gh.createProfileHash();
            hashes.add(profileHash);
            System.out.println(profileHash);
        }
    }

    @Test
    public void importOrLoad_orsGraphManagerCreated() throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfig();
        ORSGraphHopper gh = createORSGraphHopper(ghConfig);

        gh.importOrLoad();

        ORSGraphManager orsGraphManager = gh.getOrsGraphManager();
        assertNotNull(orsGraphManager);
    }

    @Test
    public void createProfileHash() throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfig();
        ORSGraphHopper gh = createORSGraphHopper(ghConfig);
        CountingHashCollector collector = new CountingHashCollector();
        collector.createHashAndCount(gh);

        gh.getConfig().getProfiles().get(0).setName("changed");
        collector.createHashAndCount(gh);

        gh.getConfig().getProfiles().get(0).setVehicle("changed");
        collector.createHashAndCount(gh);

        gh.getConfig().getProfiles().get(0).setTurnCosts(!gh.getConfig().getProfiles().get(0).isTurnCosts());
        collector.createHashAndCount(gh);

        gh.getConfig().getProfiles().get(0).setWeighting("changed");
        collector.createHashAndCount(gh);

        gh.getConfig().getCHProfiles().add(new CHProfile("added"));
        collector.createHashAndCount(gh);

        gh.getConfig().getLMProfiles().add(new LMProfile("added"));
        collector.createHashAndCount(gh);

        gh.getConfig().getLMProfiles().get(0).setPreparationProfile("changed");
        collector.createHashAndCount(gh);

        gh.getConfig().getLMProfiles().get(0).setPreparationProfile("this");
        gh.getConfig().getLMProfiles().get(0).setMaximumLMWeight(gh.getConfig().getLMProfiles().get(0).getMaximumLMWeight()-1);
        collector.createHashAndCount(gh);

        PMap pMap = gh.getConfig().asPMap();
        //TODO modify and createHashAndCount
        assertNotNull(pMap);

//        ORSGraphHopperConfig newConfig = createORSGraphHopperConfig();
//        gh = createORSGraphHopper(newConfig);
//        newConfig.putObject("prepare.min_one_way_network_size", gh.getMinNetworkSize()+1);
//        gh.init(newConfig);
//        collector.createHashAndCount(gh);
//
//        gh.setMinOneWayNetworkSize(gh.getMinOneWayNetworkSize()+1);
//        collector.createHashAndCount(gh);
//


        ORSGraphHopperConfig config = (ORSGraphHopperConfig) gh.getConfig();

        config.getFastisochroneProfiles().add(new Profile("added"));
        collector.createHashAndCount(gh);

        config.getFastisochroneProfiles().get(0).setVehicle("changed");
        collector.createHashAndCount(gh);

        config.getFastisochroneProfiles().get(0).setTurnCosts(!gh.getConfig().getProfiles().get(0).isTurnCosts());
        collector.createHashAndCount(gh);

        config.getFastisochroneProfiles().get(0).setWeighting("changed");
        collector.createHashAndCount(gh);

        config.getCoreProfiles().add(new CHProfile("added"));
        collector.createHashAndCount(gh);

        config.getCoreLMProfiles().add(new LMProfile("added"));
        collector.createHashAndCount(gh);

        config.getCoreLMProfiles().get(0).setPreparationProfile("changed");
        collector.createHashAndCount(gh);

        config.getCoreLMProfiles().get(0).setPreparationProfile("this");
        config.getCoreLMProfiles().get(0).setMaximumLMWeight(gh.getConfig().getLMProfiles().get(0).getMaximumLMWeight()-1);
        collector.createHashAndCount(gh);

        assertEquals(collector.hashes.size(), collector.callCount);
    }

    private static ORSGraphHopper createORSGraphHopper(ORSGraphHopperConfig ghConfig) throws Exception {
        RouteProfileConfiguration rpc = new RouteProfileConfiguration();
        rpc.setName("whocares");
        rpc.setEnabled(true);
        rpc.setProfiles("driving-car");
        GraphProcessContext gpc = new GraphProcessContext(rpc);
        gpc.setGetElevationFromPreprocessedData(true);
        ORSGraphHopper gh = new ORSGraphHopper(gpc);
        gh.init(ghConfig);
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        return gh;
    }

    private static ORSGraphHopperConfig createORSGraphHopperConfig() {
        ORSGraphHopperConfig ghConfig = new ORSGraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", "RAM");
        ghConfig.putObject("graph.location", "unittest.testgraph");
        ghConfig.putObject("datareader.file", "src/test/files/preprocessed_osm_data.pbf");
        ghConfig.setProfiles(List.of(new Profile("blah").setVehicle("car").setWeighting("fastest").setTurnCosts(true)));
        return ghConfig;
    }
}