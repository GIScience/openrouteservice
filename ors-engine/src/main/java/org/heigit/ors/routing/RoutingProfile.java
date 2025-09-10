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
package org.heigit.ors.routing;

import com.graphhopper.config.CHProfile;
import com.graphhopper.routing.ev.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.StorableProperties;
import org.apache.log4j.Logger;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.graphhopper.extensions.*;
import org.heigit.ors.routing.graphhopper.extensions.manage.ORSGraphManager;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.BordersGraphStorageBuilder;
import org.heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;
import org.heigit.ors.routing.pathprocessors.ORSPathProcessorFactory;
import org.heigit.ors.util.TimeUtility;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

/**
 * This class generates {@link RoutingProfile} classes and is used by mostly all service classes e.g.
 * <p>
 * {@link RoutingProfileManager} etc.
 *
 * @author Openrouteserviceteam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingProfile {
    private static final Logger LOGGER = Logger.getLogger(RoutingProfile.class);
    private static final Object lockObj = new Object();
    private static int profileIdentifier = 0;
    private final Integer[] mRoutePrefs;

    private String profileName;
    private ProfileProperties profileProperties;
    private EngineProperties engineProperties;
    private String graphVersion;

    private final ORSGraphHopper mGraphHopper;
    private String astarApproximation;
    private Double astarEpsilon;

    private final List<String> dynamicDatasets = new ArrayList<>();

    public RoutingProfile(String profileName, ProfileProperties profile, EngineProperties engine, String graphVersion, RoutingProfileLoadContext loadCntx) throws Exception {

        this.profileName = profileName;
        this.profileProperties = profile;
        this.engineProperties = engine;
        this.graphVersion = graphVersion;

        mRoutePrefs = profile.getProfilesTypes();
        mGraphHopper = initGraphHopper(loadCntx);
        ExecutionProperties execution = profile.getService().getExecution();

        if (execution.getMethods().getAstar().getApproximation() != null)
            astarApproximation = execution.getMethods().getAstar().getApproximation();
        if (execution.getMethods().getAstar().getEpsilon() != null)
            astarEpsilon = execution.getMethods().getAstar().getEpsilon();
    }


    public ORSGraphHopper initGraphHopper(RoutingProfileLoadContext loadCntx) throws Exception {
        ORSGraphManager orsGraphManager = ORSGraphManager.initializeGraphManagement(graphVersion, engineProperties, profileProperties);
        profileProperties = orsGraphManager.loadProfilePropertiesFromActiveGraph(orsGraphManager, profileProperties);

        ORSGraphHopperConfig args = ORSGraphHopperConfig.createGHSettings(profileProperties, engineProperties, orsGraphManager.getActiveGraphDirAbsPath());

        int profileId;
        synchronized (lockObj) {
            profileIdentifier++;
            profileId = profileIdentifier;
        }

        long startTime = System.currentTimeMillis();

        GraphProcessContext gpc = new GraphProcessContext(profileProperties);
        gpc.setGetElevationFromPreprocessedData(engineProperties.getElevation().getPreprocessed());

        ORSGraphHopper gh = new ORSGraphHopper(gpc, engineProperties, profileProperties);
        gh.setOrsGraphManager(orsGraphManager);
        ORSDefaultFlagEncoderFactory flagEncoderFactory = new ORSDefaultFlagEncoderFactory();
        gh.setFlagEncoderFactory(flagEncoderFactory);

        ORSPathProcessorFactory pathProcessorFactory = new ORSPathProcessorFactory();
        gh.setPathProcessorFactory(pathProcessorFactory);

        gh.init(args);

        // MARQ24: make sure that we only use ONE instance of the ElevationProvider across the multiple vehicle profiles
        // so the caching for elevation data will/can be reused across different vehicles. [the loadCntx is a single
        // Object that will shared across the (potential) multiple running instances]
        if (loadCntx.getElevationProvider() != null) {
            if (args.has("graph.elevation.provider")) {
                gh.setElevationProvider(loadCntx.getElevationProvider());
            }
        } else {
            loadCntx.setElevationProvider(gh.getElevationProvider());
        }
        gh.setGraphStorageFactory(new ORSGraphStorageFactory(gpc.getStorageBuilders()));

        gh.importOrLoad();
        // store CountryBordersReader for later use
        for (GraphStorageBuilder builder : gpc.getStorageBuilders()) {
            if (builder.getName().equals(BordersGraphStorageBuilder.BUILDER_NAME)) {
                pathProcessorFactory.setCountryBordersReader(((BordersGraphStorageBuilder) builder).getCbReader());
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("[%d] Profile: '%s', encoder: '%s', location: '%s'.".formatted(profileId, profileProperties.getProfileName(), profileProperties.getEncoderName().toString(), gh.getOrsGraphManager().getActiveGraphDirAbsPath()));
            GraphHopperStorage ghStorage = gh.getGraphHopperStorage();
            LOGGER.info("[%d] Edges: %s - Nodes: %s.".formatted(profileId, ghStorage.getEdges(), ghStorage.getNodes()));
            LOGGER.info("[%d] Total time: %s.".formatted(profileId, TimeUtility.getElapsedTime(startTime, true)));
            LOGGER.info("[%d] Finished at: %s.".formatted(profileId, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        }

        // Make a stamp which help tracking any changes in the size of OSM file. TODO check if this is still in use
        if (profileProperties.getBuild().getSourceFile() != null) {
            File file = new File(profileProperties.getBuild().getSourceFile().toAbsolutePath().toString());
            Path pathTimestamp = Paths.get(gh.getOrsGraphManager().getActiveGraphDirAbsPath(), "stamp.txt");
            File file2 = pathTimestamp.toFile();
            if (!file2.exists())
                Files.write(pathTimestamp, Long.toString(file.length()).getBytes());
        }
        return gh;
    }

    public boolean hasCHProfile(String profileName) {
        boolean hasCHProfile = false;
        for (CHProfile chProfile : getGraphhopper().getCHPreparationHandler().getCHProfiles()) {
            if (profileName.equals(chProfile.getProfile()))
                hasCHProfile = true;
        }
        return hasCHProfile;
    }

    public long getMemoryUsage() {
        return mGraphHopper.getMemoryUsage();
    }

    public ORSGraphHopper getGraphhopper() {
        return mGraphHopper;
    }

    public StorableProperties getGraphProperties() {
        return mGraphHopper.getGraphHopperStorage().getProperties();
    }

    public ProfileProperties getProfileConfiguration() {
        return profileProperties;
    }

    public void close() {
        mGraphHopper.close();
    }

    public String getAstarApproximation() {
        return astarApproximation;
    }

    public Double getAstarEpsilon() {
        return astarEpsilon;
    }

    public boolean equals(Object o) {
        return o != null && o.getClass().equals(RoutingProfile.class) && this.hashCode() == o.hashCode();
    }

    public int hashCode() {
        return mGraphHopper.getGraphHopperStorage().getDirectory().getLocation().hashCode();
    }

    public String name() {
        return this.profileName;
    }

    public ProfileProperties getProfileProperties() {
        return this.profileProperties;
    }

    public void addDynamicData(String datasetName) {
        getGraphhopper().addSparseEncodedValue(datasetName);
        dynamicDatasets.add(datasetName);
    }

    public List<String> getDynamicDatasets() {
        return dynamicDatasets;
    }

    public void updateDynamicData(String key, int edgeID, String value) {
        Function<String,Object> stateFromString = null;
        switch (key) {
            case LogieBorders.KEY:
                stateFromString = s -> LogieBorders.valueOf(s);
                break;
            case LogieBridges.KEY:
                stateFromString = s -> LogieBridges.valueOf(s);
                break;
            case LogieRoads.KEY:
                stateFromString = s -> LogieRoads.valueOf(s);
                break;
            default:
                // do nothing
                break;
        }
        if (stateFromString == null) {
            LOGGER.error("No stateFromString function defined for key '" + key + "', cannot update dynamic data.");
            return;
        }
        SparseEncodedValue<String> sev = getGraphhopper().getEncodingManager().getEncodedValue(key, HashMapSparseEncodedValue.class);
        if (sev == null) {
            LOGGER.error("SparseEncodedValue for key '" + key + "' not found, cannot update dynamic data.");
        }
        sev.set(edgeID, stateFromString.apply(value));
    }
}
