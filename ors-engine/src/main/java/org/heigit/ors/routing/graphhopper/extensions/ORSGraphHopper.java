/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.*;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.gtfs.GraphHopperGtfs;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.routing.Router;
import com.graphhopper.routing.RouterConfig;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.ch.CHPreparationHandler;
import com.graphhopper.routing.lm.LMConfig;
import com.graphhopper.routing.lm.LMPreparationHandler;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHConfig;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.*;
import com.graphhopper.util.details.PathDetailsBuilderFactory;
import org.geotools.feature.SchemaException;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.fastisochrones.Contour;
import org.heigit.ors.fastisochrones.Eccentricity;
import org.heigit.ors.fastisochrones.partitioning.FastIsochroneFactory;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.AvoidFeatureFlags;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.core.*;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.HeavyVehicleEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.core.LMEdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.HereTrafficGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.routing.graphhopper.extensions.weighting.HgvAccessWeighting;
import org.heigit.ors.util.AppInfo;
import org.heigit.ors.util.CoordTools;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;


public class ORSGraphHopper extends GraphHopperGtfs {
    private static final Logger LOGGER = LoggerFactory.getLogger(ORSGraphHopper.class);

    private GraphProcessContext processContext;
    private EngineProperties engineProperties;
    private ProfileProperties profileProperties;
    private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
    private HashMap<Integer, Long> tmcEdges;
    private Eccentricity eccentricity;

    private int minNetworkSize = 200;

    private final CorePreparationHandler corePreparationHandler = new CorePreparationHandler();
    private final CoreLMPreparationHandler coreLMPreparationHandler = new CoreLMPreparationHandler();
    private final FastIsochroneFactory fastIsochroneFactory = new FastIsochroneFactory();

    private ORSGraphManager orsGraphManager;

    public ORSGraphManager getOrsGraphManager() {
        return this.orsGraphManager;
    }

    public void setOrsGraphManager(ORSGraphManager orsGraphManager) {
        this.orsGraphManager = orsGraphManager;
    }

    public GraphHopperConfig getConfig() {
        return config;
    }

    public ProfileProperties getProfileProperties() {
        return profileProperties;
    }

    private GraphHopperConfig config;

    public ORSGraphHopper(GraphProcessContext processContext, EngineProperties engineProperties, ProfileProperties profileProperties) {
        this.processContext = processContext;
        this.engineProperties = engineProperties;
        this.profileProperties = profileProperties;
    }

    public ORSGraphHopper() {
        // used to initialize tests more easily without the need to create GraphProcessContext etc. when they're anyway not used in the tested functions.
    }

    @Override
    public GraphHopper init(GraphHopperConfig ghConfig) {
        GraphHopper ret = super.init(ghConfig);

        if (ghConfig instanceof ORSGraphHopperConfig orsConfig) {
            corePreparationHandler.init(orsConfig);
            coreLMPreparationHandler.init(orsConfig);
        }

        fastIsochroneFactory.init(ghConfig);

        minNetworkSize = ghConfig.getInt("prepare.min_network_size", minNetworkSize);
        config = ghConfig;

        return ret;
    }

    @Override
    protected void cleanUp() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("call cleanUp for '%s' ".formatted(getGraphHopperLocation()));
        GraphHopperStorage ghs = getGraphHopperStorage();
        if (ghs != null) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("graph %s, details:%s".formatted(ghs, ghs.toDetailsString()));
            int prevNodeCount = ghs.getNodes();
            int ex = ghs.getAllEdges().length();
            List<FlagEncoder> list = getEncodingManager().fetchEdgeEncoders();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("will create PrepareRoutingSubnetworks with: NodeCountBefore: '%d' getAllEdges().getMaxId(): '%d' List<FlagEncoder>: '%s' minNetworkSize: '%d'".formatted(prevNodeCount, ex, list, minNetworkSize)
                );
            ghs.getProperties().put("elevation", hasElevation());
        } else {
            LOGGER.info("graph GraphHopperStorage is null?!");
        }
        super.cleanUp();
    }

    @Override
    protected OSMReader createOSMReader() {
        return new ORSOSMReader(getGraphHopperStorage(), processContext);
    }

    @Override
    public GraphHopper importOrLoad() {
        if (isFullyLoaded()) {
            throw new IllegalStateException("graph is already successfully loaded");
        }

        ORSGraphHopper gh = (ORSGraphHopper) super.importOrLoad();
        AppInfo.GRAPH_DATE = gh.getGraphHopperStorage().getProperties().get("datareader.import.date");

        writeOrsGraphBuildInfoFileIfNotExists(gh);

        if ((tmcEdges != null) && (osmId2EdgeIds != null)) {
            java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

            if ((tmcEdges.size() == 0) || (osmId2EdgeIds.size() == 0)) {
                // try to load TMC edges from file.

                File file = path.toFile();
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(path.toString());
                         ObjectInputStream ois = new ObjectInputStream(fis)) {
                        tmcEdges = (HashMap<Integer, Long>) ois.readObject();
                        osmId2EdgeIds = (HashMap<Long, ArrayList<Integer>>) ois.readObject();
                        LOGGER.info("Serialized HashMap data is saved in trafficEdges");
                    } catch (IOException ioe) {
                        LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                    } catch (ClassNotFoundException c) {
                        LOGGER.error("Class not found");
                        LOGGER.error(Arrays.toString(c.getStackTrace()));
                    }
                }
            } else {
                // save TMC edges if needed.
                try (FileOutputStream fos = new FileOutputStream(path.toString());
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(tmcEdges);
                    oos.writeObject(osmId2EdgeIds);
                    LOGGER.info("Serialized HashMap data is saved in trafficEdges");
                } catch (IOException ioe) {
                    LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                }
            }
        }

        return gh;
    }

    private void writeOrsGraphBuildInfoFileIfNotExists(ORSGraphHopper gh) {
        orsGraphManager.writeOrsGraphBuildInfoFileIfNotExists(gh);
    }

    @Override
    protected Router doCreateRouter(GraphHopperStorage ghStorage, LocationIndex locationIndex, Map<String, Profile> profilesByName,
                                    PathDetailsBuilderFactory pathBuilderFactory, TranslationMap trMap, RouterConfig routerConfig,
                                    WeightingFactory weightingFactory, Map<String, RoutingCHGraph> chGraphs, Map<String, LandmarkStorage> landmarks) {
        ORSRouter r = new ORSRouter(ghStorage, locationIndex, profilesByName, pathBuilderFactory, trMap, routerConfig, weightingFactory, chGraphs, landmarks);
        r.setEdgeFilterFactory(new ORSEdgeFilterFactory());
        r.setPathProcessorFactory(pathProcessorFactory);

        if (!(ghStorage instanceof ORSGraphHopperStorage))
            throw new IllegalStateException("Expected an instance of ORSGraphHopperStorage");

        Map<String, RoutingCHGraph> coreGraphs = new LinkedHashMap<>();
        for (com.graphhopper.config.CHProfile chProfile : corePreparationHandler.getCHProfiles()) {
            String chGraphName = corePreparationHandler.getPreparation(chProfile.getProfile()).getCHConfig().getName();
            coreGraphs.put(chProfile.getProfile(), ((ORSGraphHopperStorage) ghStorage).getCoreGraph(chGraphName));
        }
        r.setCoreGraphs(coreGraphs);

        Map<String, PrepareCoreLandmarks> coreLandmarks = new LinkedHashMap<>();
        for (PrepareLandmarks preparation : coreLMPreparationHandler.getPreparations()) {
            coreLandmarks.put(preparation.getLMConfig().getName(), (PrepareCoreLandmarks) preparation);
        }
        r.setCoreLandmarks(coreLandmarks);

        return r;
    }

    @Override
    protected WeightingFactory createWeightingFactory() {
        return new ORSWeightingFactory(getGraphHopperStorage(), getEncodingManager());
    }

    public GHResponse constructFreeHandRoute(GHRequest request) {
        LineString directRouteGeometry = constructFreeHandRouteGeometry(request);
        ResponsePath directRoutePathWrapper = constructFreeHandRoutePathWrapper(directRouteGeometry);
        GHResponse directRouteResponse = new GHResponse();
        directRouteResponse.add(directRoutePathWrapper);
        directRouteResponse.getHints().putObject("skipped_segment", true);
        return directRouteResponse;
    }

    private ResponsePath constructFreeHandRoutePathWrapper(LineString lineString) {
        ResponsePath responsePath = new ResponsePath();
        PointList pointList = new PointList();
        PointList startPointList = new PointList();
        PointList endPointList = new PointList();
        PointList wayPointList = new PointList();
        Coordinate startCoordinate = lineString.getCoordinateN(0);
        Coordinate endCoordinate = lineString.getCoordinateN(1);
        double distance = CoordTools.calcDistHaversine(startCoordinate.x, startCoordinate.y, endCoordinate.x, endCoordinate.y);
        pointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        pointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        wayPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        wayPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        startPointList.add(lineString.getCoordinateN(0).x, lineString.getCoordinateN(0).y);
        endPointList.add(lineString.getCoordinateN(1).x, lineString.getCoordinateN(1).y);
        Translation translation = new TranslationMap.TranslationHashMap(new Locale(""));
        InstructionList instructions = new InstructionList(translation);
        Instruction startInstruction = new Instruction(Instruction.REACHED_VIA, "free hand route", startPointList);
        Instruction endInstruction = new Instruction(Instruction.FINISH, "end of free hand route", endPointList);
        instructions.add(0, startInstruction);
        instructions.add(1, endInstruction);
        responsePath.setDistance(distance);
        responsePath.setAscend(0.0);
        responsePath.setDescend(0.0);
        responsePath.setTime(0);
        responsePath.setInstructions(instructions);
        responsePath.setWaypoints(wayPointList);
        responsePath.setPoints(pointList);
        responsePath.setRouteWeight(0.0);
        responsePath.setDescription(new ArrayList<>());
        responsePath.setImpossible(false);
        startInstruction.setDistance(distance);
        startInstruction.setTime(0);
        return responsePath;
    }

    private LineString constructFreeHandRouteGeometry(GHRequest request) {
        Coordinate start = new Coordinate();
        Coordinate end = new Coordinate();
        start.x = request.getPoints().get(0).getLat();
        start.y = request.getPoints().get(0).getLon();
        end.x = request.getPoints().get(1).getLat();
        end.y = request.getPoints().get(1).getLon();
        Coordinate[] coords = new Coordinate[]{start, end};
        return new GeometryFactory().createLineString(coords);
    }

    private void matchTraffic() {
        // Do the graph extension post-processing
        // Reserved for processes that need a fully initiated graph e.g. for match making
        if (getGraphHopperStorage() != null && processContext != null && processContext.getStorageBuilders() != null) {
            for (GraphStorageBuilder graphStorageBuilder : processContext.getStorageBuilders()) {
                if (graphStorageBuilder instanceof HereTrafficGraphStorageBuilder storageBuilder) {
                    try {
                        storageBuilder.postProcess(this);
                    } catch (SchemaException e) {
                        LOGGER.error("Error building the here traffic storage.");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void addTrafficSpeedCalculator(LMPreparationHandler lmPreparationHandler) {
        if (isTrafficEnabled())
            ORSWeightingFactory.addTrafficSpeedCalculator(lmPreparationHandler.getWeightings(), getGraphHopperStorage());
    }

    /**
     * Does the preparation and creates the location index
     *
     * @param closeEarly release resources as early as possible
     */
    @Override
    protected void postProcessing(boolean closeEarly) {
        super.postProcessing(closeEarly);

        //Create the core
        GraphHopperStorage gs = getGraphHopperStorage();
        if (corePreparationHandler.isEnabled())
            corePreparationHandler.setProcessContext(processContext).createPreparations(gs);
        if (isCorePrepared()) {
            // check loaded profiles
            for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
                if (!getProfileVersion(profile.getProfile()).isEmpty() && !getProfileVersion(profile.getProfile()).equals("" + profilesByName.get(profile.getProfile()).getVersion()))
                    throw new IllegalArgumentException("Core preparation of " + profile.getProfile() + " already exists in storage and doesn't match configuration");
            }
        } else {
            prepareCore(closeEarly);
        }

        //Create the landmarks in the core
        if (coreLMPreparationHandler.isEnabled()) {
            initCoreLMPreparationHandler();
            coreLMPreparationHandler.createPreparations(gs, super.getLocationIndex());
            addTrafficSpeedCalculator(coreLMPreparationHandler);
        }
        loadOrPrepareCoreLM();

        if (fastIsochroneFactory.isEnabled()) {
            EdgeFilterSequence partitioningEdgeFilter = new EdgeFilterSequence();
            try {
                partitioningEdgeFilter.add(new AvoidFeaturesEdgeFilter(AvoidFeatureFlags.FERRIES, getGraphHopperStorage()));
            } catch (Exception e) {
                LOGGER.debug(e.getLocalizedMessage());
            }
            fastIsochroneFactory.createPreparation(gs, partitioningEdgeFilter);

            if (!isPartitionPrepared())
                preparePartition();
            else {
                fastIsochroneFactory.setExistingStorages();
                fastIsochroneFactory.getCellStorage().loadExisting();
                fastIsochroneFactory.getIsochroneNodeStorage().loadExisting();
            }
            //No fast isochrones without partition
            if (isPartitionPrepared()) {
                // Initialize edge filter sequence for fast isochrones
                calculateContours();
                List<Profile> profiles = fastIsochroneFactory.getFastIsochroneProfiles();
                for (Profile profile : profiles) {
                    Weighting weighting = ((ORSWeightingFactory) createWeightingFactory()).createIsochroneWeighting(profile);
                    for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
                        calculateCellProperties(weighting, partitioningEdgeFilter, encoder, fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
                    }
                }
            }
        }
    }

    @Override
    protected void postProcessingHook() {
        matchTraffic();

        if (getLMPreparationHandler().isEnabled())
            addTrafficSpeedCalculator(getLMPreparationHandler());
    }

    //TODO Refactoring : This is a duplication with code in RoutingProfile and should probably be moved to a status keeping class.
    private boolean hasCHProfile(String profileName) {
        return contains(getGraphHopperStorage().getCHGraphNames(), profileName);
    }

    private boolean hasCoreProfile(String profileName) {
        if (getGraphHopperStorage() instanceof ORSGraphHopperStorage) {
            List<String> profiles = ((ORSGraphHopperStorage) getGraphHopperStorage()).getCoreGraphNames();
            return contains(profiles, profileName);
        }
        return false;
    }

    private boolean hasLMProfile(String profileName) {
        List<String> profiles = getLMPreparationHandler().getLMConfigs().stream().map(LMConfig::getName).collect(Collectors.toList());
        return contains(profiles, profileName);
    }

    private boolean contains(List<String> profiles, String profileName) {
        for (String profile : profiles) {
            if (profileName.equals(profile))
                return true;
        }
        return false;
    }

    public final boolean isCoreEnabled() {
        return corePreparationHandler.isEnabled();
    }

    public final CorePreparationHandler getCorePreparationHandler() {
        return corePreparationHandler;
    }

    @Override
    protected void initCHPreparationHandler() {
        CHPreparationHandler chPreparationHandler = getCHPreparationHandler();
        if (chPreparationHandler.hasCHConfigs()) {
            return;
        }

        for (CHProfile chProfile : chPreparationHandler.getCHProfiles()) {
            Profile profile = profilesByName.get(chProfile.getProfile());
            Weighting weighting = createWeighting(profile, new PMap());

            if (profile.getVehicle().equals(FlagEncoderNames.HEAVYVEHICLE)) {
                HeavyVehicleAttributesGraphStorage hgvStorage = GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), HeavyVehicleAttributesGraphStorage.class);
                EdgeFilter hgvEdgeFilter = new HeavyVehicleEdgeFilter(HeavyVehicleAttributes.HGV, null, hgvStorage);
                weighting = new HgvAccessWeighting(weighting, hgvEdgeFilter);
            }

            if (profile.isTurnCosts()) {
                chPreparationHandler.addCHConfig(CHConfig.edgeBased(profile.getName(), weighting));
            } else {
                chPreparationHandler.addCHConfig(CHConfig.nodeBased(profile.getName(), weighting));
            }
        }
    }

    protected void loadORS() {
        List<CHConfig> chConfigs;
        if (corePreparationHandler.isEnabled()) {
            initCorePreparationHandler();
            chConfigs = corePreparationHandler.getCHConfigs();
        } else {
            chConfigs = emptyList();
        }

        if (getGraphHopperStorage() instanceof ORSGraphHopperStorage)
            ((ORSGraphHopperStorage) getGraphHopperStorage()).addCoreGraphs(chConfigs);
        else
            throw new IllegalStateException("Expected an instance of ORSGraphHopperStorage");
    }

    private void initCorePreparationHandler() {
        if (corePreparationHandler.hasCHConfigs()) {
            return;
        }

        for (com.graphhopper.config.CHProfile chProfile : corePreparationHandler.getCHProfiles()) {
            Profile profile = profilesByName.get(chProfile.getProfile());
            corePreparationHandler.addCHConfig(new CHConfig(profile.getName(), createWeighting(profile, new PMap()), profile.isTurnCosts(), CHConfig.TYPE_CORE));
        }
    }

    private void initCoreLMPreparationHandler() {
        if (coreLMPreparationHandler.hasLMProfiles())
            return;

        CoreLMOptions coreLMOptions = coreLMPreparationHandler.getCoreLMOptions();
        coreLMOptions.createRestrictionFilters(getGraphHopperStorage());

        for (LMProfile lmProfile : coreLMPreparationHandler.getLMProfiles()) {
            if (lmProfile.usesOtherPreparation())
                continue;
            Profile profile = profilesByName.get(lmProfile.getProfile());
            Weighting weighting = createWeighting(profile, new PMap(), true);
            for (LMEdgeFilterSequence edgeFilter : coreLMOptions.getFilters()) {
                CoreLMConfig coreLMConfig = new CoreLMConfig(profile.getName(), weighting);
                coreLMConfig.setEdgeFilter(edgeFilter);
                coreLMPreparationHandler.addLMConfig(coreLMConfig);
            }
        }
    }

    protected void prepareCore(boolean closeEarly) {
        for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
            if (!getProfileVersion(profile.getProfile()).isEmpty()
                    && !getProfileVersion(profile.getProfile()).equals("" + profilesByName.get(profile.getProfile()).getVersion()))
                throw new IllegalArgumentException("Core preparation of " + profile.getProfile() + " already exists in storage and doesn't match configuration");
        }
        if (isCoreEnabled()) {
            ensureWriteAccess();
            GraphHopperStorage ghStorage = getGraphHopperStorage();
            ghStorage.freeze();
            corePreparationHandler.prepare(ghStorage.getProperties(), closeEarly);
            ghStorage.getProperties().put(ORSParameters.Core.PREPARE + "done", true);
            for (CHProfile profile : corePreparationHandler.getCHProfiles()) {
                // potentially overwrite existing keys from CH/LM
                setProfileVersion(profile.getProfile(), profilesByName.get(profile.getProfile()).getVersion());
            }
        }
    }

    private boolean isCorePrepared() {
        return "true".equals(getGraphHopperStorage().getProperties().get(ORSParameters.Core.PREPARE + "done"))
                // remove old property in >0.9
                || "true".equals(getGraphHopperStorage().getProperties().get("prepare.done"));
    }

    public final boolean isCoreLMEnabled() {
        return coreLMPreparationHandler.isEnabled();
    }

    /**
     * For landmarks it is required to always call this method: either it creates the landmark data or it loads it.
     */
    protected void loadOrPrepareCoreLM() {
        boolean tmpPrepare = coreLMPreparationHandler.isEnabled();
        if (tmpPrepare) {
            ensureWriteAccess();
            getGraphHopperStorage().freeze();
            if (coreLMPreparationHandler.loadOrDoWork(getGraphHopperStorage().getProperties(), false))
                getGraphHopperStorage().getProperties().put(ORSParameters.CoreLandmark.PREPARE + "done", true);
        }
    }

    //TODO Refactoring : This is a duplication with code in RoutingProfile and should probably be moved to a status keeping class.
    public final boolean isCHAvailable(String profileName) {
        return getCHPreparationHandler().isEnabled() && hasCHProfile(profileName);
    }

    public final boolean isLMAvailable(String profileName) {
        return getLMPreparationHandler().isEnabled() && hasLMProfile(profileName);
    }

    public final boolean isCoreAvailable(String profileName) {
        return getCorePreparationHandler().isEnabled() && hasCoreProfile(profileName);
    }

    public final boolean isFastIsochroneAvailable(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
        return eccentricity != null && eccentricity.isAvailable(ORSWeightingFactory.createIsochroneWeighting(searchContext, travelRangeType));
    }

    /**
     * Partitioning
     */
    public final FastIsochroneFactory getFastIsochroneFactory() {
        return fastIsochroneFactory;
    }

    protected void preparePartition() {
        if (fastIsochroneFactory.isEnabled()) {
            ensureWriteAccess();

            getGraphHopperStorage().freeze();
            fastIsochroneFactory.prepare(getGraphHopperStorage().getProperties());
            getGraphHopperStorage().getProperties().put(ORSParameters.FastIsochrone.PREPARE + "done", true);
        }
    }

    private boolean isPartitionPrepared() {
        return "true".equals(getGraphHopperStorage().getProperties().get(ORSParameters.FastIsochrone.PREPARE + "done"));
    }

    private void calculateContours() {
        if (fastIsochroneFactory.getCellStorage().isContourPrepared())
            return;
        Contour contour = new Contour(getGraphHopperStorage(), getGraphHopperStorage().getNodeAccess(), fastIsochroneFactory.getIsochroneNodeStorage(), fastIsochroneFactory.getCellStorage());
        contour.calculateContour();
    }

    private void calculateCellProperties(Weighting weighting, EdgeFilter edgeFilter, FlagEncoder flagEncoder, IsochroneNodeStorage isochroneNodeStorage, CellStorage cellStorage) {
        if (eccentricity == null)
            eccentricity = new Eccentricity(getGraphHopperStorage(), getLocationIndex(), isochroneNodeStorage, cellStorage);
        if (!eccentricity.loadExisting(weighting)) {
            eccentricity.calcEccentricities(weighting, edgeFilter, flagEncoder);
            eccentricity.calcBorderNodeDistances(weighting, edgeFilter, flagEncoder);
        }
    }

    public Eccentricity getEccentricity() {
        return eccentricity;
    }


    public boolean isTrafficEnabled() {
        return GraphStorageUtils.getGraphExtension(getGraphHopperStorage(), TrafficGraphStorage.class) != null;
    }

    public long getMemoryUsage() {
        long mem = 0;
        if (getLMPreparationHandler().isEnabled()) {
            mem += getLMPreparationHandler().getPreparations().stream().mapToLong(lm -> lm.getLandmarkStorage().getCapacity()).sum();
        }
        if (isCoreEnabled()) {
            // core CH preparations are handled in ORSGraphHopperStorage.getCapacity()
            mem += coreLMPreparationHandler.getPreparations().stream().mapToLong(lm -> lm.getLandmarkStorage().getCapacity()).sum();
        }
        if (fastIsochroneFactory.isEnabled()) {
            mem += fastIsochroneFactory.getCapacity();
        }
        return mem + getGraphHopperStorage().getCapacity();
    }

    public EngineProperties getEngineProperties() {
        return engineProperties;
    }
}
