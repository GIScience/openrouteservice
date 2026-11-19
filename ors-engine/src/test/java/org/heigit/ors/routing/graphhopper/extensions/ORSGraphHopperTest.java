package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Rest;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.GHPoint;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ExtendedStorageProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.GraphManagementRuntimeProperties;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.util.AppInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ORSGraphHopperTest {
    static final String GH_PROFILE_NAME = "car_ors";
    static final String ROUTE_PROFILE_NAME = "driving-car";
    static final String FOOT_PROFILE_NAME = "foot-walking";

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
        ORSGraphHopper gh = createORSGraphHopperWithOsmFile(true);

        GraphManagementRuntimeProperties managementProps = GraphManagementRuntimeProperties.Builder.empty()
                .withEnabled(true)
                .withLocalProfileName("buildGraphWithPreprocessedData")
                .withLocalGraphsRootAbsPath("target/test-output/graphs")
                .withRepoName("repoName")
                .withGraphVersion("6")
                .withRepoBaseUri("http://my.domain.com")
                .build();
        ORSGraphManager orsGraphManager = ORSGraphManager.initializeGraphManagement(managementProps);
        gh.setOrsGraphManager(orsGraphManager);

        gh.importOrLoad();

        ORSGraphHopperStorage storage = (ORSGraphHopperStorage) gh.getGraphHopperStorage();
        assertEquals(419, storage.getNodes());
    }

    @Test
    void restPointsAreImportedWhenTheGraphIsBuiltAndNotAgainWhenItIsLoaded(@TempDir Path dir) throws Exception {
        GHPoint builtWith = new GHPoint(49.3580784, 8.7439722);
        GHPoint loadedWith = new GHPoint(49.3699229, 8.7457273);

        ORSGraphHopper built = createFootGraphHopper(dir, writeRestPoints(dir, "built_with.csv", builtWith));
        built.importOrLoad();
        assertTrue(hasRestPoint(built, builtWith));
        built.close();

        ORSGraphHopper loaded = createFootGraphHopper(dir, writeRestPoints(dir, "loaded_with.csv", loadedWith));
        loaded.importOrLoad();
        assertTrue(hasRestPoint(loaded, builtWith));
        assertFalse(hasRestPoint(loaded, loadedWith));
        loaded.close();
    }

    private static Path writeRestPoints(Path dir, String fileName, GHPoint restPoint) throws IOException {
        return Files.write(dir.resolve(fileName), List.of("lat,lon", restPoint.getLat() + "," + restPoint.getLon()));
    }

    private static boolean hasRestPoint(ORSGraphHopper gh, GHPoint point) {
        DecimalEncodedValue restEnc = gh.getEncodingManager().getDecimalEncodedValue(Rest.KEY);
        Snap snap = gh.getLocationIndex().findClosest(point.getLat(), point.getLon(), EdgeFilter.ALL_EDGES);
        return snap.getClosestEdge().get(restEnc) != -1;
    }

    private static ORSGraphHopper createFootGraphHopper(Path dir, Path footRestData) throws Exception {
        ProfileProperties profile = new ProfileProperties();
        profile.setProfileName(FOOT_PROFILE_NAME);
        profile.setEncoderName(EncoderNameEnum.FOOT_WALKING);
        profile.getBuild().setSourceFile(Path.of("src/test/files/preprocessed_osm_data.pbf"));
        profile.getBuild().setFootRestData(footRestData);

        EngineProperties engineProperties = new EngineProperties();
        engineProperties.setGraphsDataAccess(DataAccessEnum.RAM_STORE);
        engineProperties.getElevation().setCachePath(Path.of(""));
        engineProperties.getProfiles().put(FOOT_PROFILE_NAME, profile);

        ORSGraphManager orsGraphManager = ORSGraphManager.initializeGraphManagement(GraphManagementRuntimeProperties.Builder.empty()
                .withLocalProfileName(FOOT_PROFILE_NAME)
                .withLocalGraphsRootAbsPath(dir.resolve("graphs").toString())
                .withGraphVersion(AppInfo.GRAPH_VERSION)
                .build());
        ORSGraphHopperConfig ghConfig = ORSGraphHopperConfig.createGHSettings(profile, engineProperties, orsGraphManager.getActiveGraphDirAbsPath());
        GraphProcessContext gpc = new GraphProcessContext(profile);
        ORSGraphHopper gh = new ORSGraphHopper(gpc, engineProperties, profile);
        gh.setOrsGraphManager(orsGraphManager);
        gh.setFlagEncoderFactory(new ORSDefaultFlagEncoderFactory());
        gh.init(ghConfig);
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
        return gh;
    }

    @Test
    void usePbfElevationProviderOnlyWithPreprocessedElevation() throws Exception {
        assertInstanceOf(PbfElevationProvider.class, createORSGraphHopperWithOsmFile(true).getElevationProvider());
        assertFalse(createORSGraphHopperWithOsmFile(false).getElevationProvider() instanceof PbfElevationProvider);
    }

    private static EngineProperties createEngineProperties(Path localGraphsRootPath,
                                                           String graphManagementRepositoryUrl,
                                                           String graphManagementRepositoryName,
                                                           String graphManagementRepositoryProfileGroup,
                                                           String graphManagementGraphExtent,
                                                           String profileName,
                                                           int graphManagementMaxBackups,
                                                           boolean preprocessed) {

        EngineProperties engineProperties = new EngineProperties();

        engineProperties.getGraphManagement().setEnabled(true);
        engineProperties.getGraphManagement().setDownloadSchedule("0 0 0 31 2 *");
        engineProperties.getGraphManagement().setActivationSchedule("0 0 0 31 2 *");
        engineProperties.getGraphManagement().setMaxBackups(graphManagementMaxBackups);

        engineProperties.getProfileDefault().setEnabled(false);
        engineProperties.getProfileDefault().setGraphPath(Path.of(localGraphsRootPath.toString()));
        engineProperties.getProfileDefault().getBuild().setSourceFile(Path.of("/path/to/source/file"));
        engineProperties.getProfileDefault().getBuild().getPreparation().setMinNetworkSize(300);
        engineProperties.getProfileDefault().getBuild().getPreparation().getMethods().getLm().setEnabled(false);
        engineProperties.getProfileDefault().getBuild().getPreparation().getMethods().getLm().setWeightings("shortest");
        engineProperties.getProfileDefault().getBuild().getPreparation().getMethods().getLm().setLandmarks(2);
        engineProperties.getProfileDefault().getService().getExecution().getMethods().getLm().setActiveLandmarks(2);

        ExtendedStorageProperties wayCategory = new ExtendedStorageProperties();
        wayCategory.setEnabled(true);
        engineProperties.getProfileDefault().getBuild().getExtStorages().put("WayCategory", wayCategory);

        ExtendedStorageProperties greenIndex = new ExtendedStorageProperties();
        greenIndex.setEnabled(true);
        greenIndex.setFilepath(Path.of("/path/to/file.csv"));
        engineProperties.getProfileDefault().getBuild().getExtStorages().put("GreenIndex", greenIndex);

        ProfileProperties carProperties = new ProfileProperties();
        engineProperties.getElevation().setPreprocessed(preprocessed);
        carProperties.setProfileName(profileName);
        carProperties.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        carProperties.getBuild().getPreparation().getMethods().getLm().setEnabled(true);
        carProperties.getBuild().getPreparation().getMethods().getLm().setThreads(5);
        carProperties.getService().getExecution().getMethods().getLm().setActiveLandmarks(2);
        carProperties.getService().setMaximumWayPoints(50);
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
        gpc.setGetElevationFromPreprocessedData(engineProperties.getElevation().getPreprocessed());
        gpc.setPreprocessedCountries(true);

        ORSGraphHopper gh = new ORSGraphHopper(gpc, engineProperties, profileProperties);
        gh.init(ghConfig);
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));
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

    private static ORSGraphHopper createORSGraphHopperWithOsmFile(boolean preprocessed) throws Exception {
        ORSGraphHopperConfig ghConfig = createORSGraphHopperConfigWithOsmFile();

        EngineProperties engineProperties = createEngineProperties(Path.of("repoDir"), "https://my.domain.com/",
                "repoName", "profileGroup", "graphExtent",
                ROUTE_PROFILE_NAME, 0, preprocessed
        );
        return createORSGraphHopper(ghConfig, engineProperties, engineProperties.getProfiles().get(ROUTE_PROFILE_NAME));
    }

}