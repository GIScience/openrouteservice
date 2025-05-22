package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import org.heigit.ors.config.ElevationProperties;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.BuildProperties;
import org.heigit.ors.config.profile.ExecutionProperties;
import org.heigit.ors.config.profile.PreparationProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import org.heigit.ors.util.ProfileTools;
import org.heigit.ors.util.StringUtility;

import java.nio.file.Path;
import java.util.*;

import org.apache.log4j.Logger;

public class ORSGraphHopperConfig extends GraphHopperConfig {
    private List<CHProfile> coreProfiles = new ArrayList<>();
    private List<LMProfile> coreLMProfiles = new ArrayList<>();
    private List<Profile> fastisochroneProfiles = new ArrayList<>();
    private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(ORSGraphHopperConfig.class.getName());

    public static ORSGraphHopperConfig createGHSettings(ProfileProperties profile, EngineProperties engineConfig, String graphLocation) {
        ORSGraphHopperConfig ghConfig = new ORSGraphHopperConfig();
        ghConfig.putObject("graph.dataaccess", engineConfig.getGraphsDataAccess().toString());
        ghConfig.putObject("datareader.file", Optional.ofNullable(profile).map(ProfileProperties::getBuild).map(BuildProperties::getSourceFile).map(Path::toString).orElse(null));
        ghConfig.putObject("graph.bytes_for_flags", profile.getBuild().getEncoderFlagsSize());
        ghConfig.putObject("graph.location", graphLocation);

        if (Boolean.FALSE.equals(profile.getBuild().getInstructions())) {
            ghConfig.putObject("instructions", false);
        }

        ElevationProperties elevationProps = engineConfig.getElevation();

        boolean addElevation = true;

    
        if (!profile.getBuild().getElevation()) {
            LOGGER.warn("Elevation is set to false.");
            addElevation = false;
        }
        if (elevationProps.getProvider() == null) {
            LOGGER.warn("Elevation provider is null.");
            addElevation = false;
        }
        if (elevationProps.getCachePath() == null) {
            LOGGER.warn("Elevation cache path is null.");
            addElevation = false;
        }
        
        if (!addElevation) {
             LOGGER.warn("Elevation deactivated.");
        } else {
            ghConfig.putObject("graph.elevation.provider", StringUtility.trimQuotes(elevationProps.getProvider()));
            ghConfig.putObject("graph.elevation.cache_dir", StringUtility.trimQuotes(elevationProps.getCachePath().toString()));
            ghConfig.putObject("graph.elevation.dataaccess", StringUtility.trimQuotes(elevationProps.getDataAccess().toString()));
            ghConfig.putObject("graph.elevation.clear", elevationProps.getCacheClear());
            if (Boolean.TRUE.equals(profile.getBuild().getInterpolateBridgesAndTunnels()))
                ghConfig.putObject("graph.encoded_values", "road_environment");
            if (Boolean.TRUE.equals(profile.getBuild().getElevationSmoothing()))
                ghConfig.putObject("graph.elevation.smoothing", true);
        }

        boolean prepareCH = false;
        boolean prepareLM = false;
        boolean prepareCore = false;
        boolean prepareFI = false;

        Integer[] profilesTypes = profile.getProfilesTypes();
        Map<String, Profile> profiles = new LinkedHashMap<>();

        // TODO Future improvement : Multiple profiles were used to share the graph  for several
        //       bike profiles. We don't use this feature now but it might be
        //       desireable in the future. However, this behavior is standard
        //       in original GH through an already existing mechanism.
        if (profilesTypes.length != 1)
            throw new IllegalStateException("Expected single profile in config");

        String vehicle = RoutingProfileType.getEncoderName(profilesTypes[0]);

        boolean hasTurnCosts = Boolean.TRUE.equals(profile.getBuild().getEncoderOptions().getTurnCosts());

        // TODO Future improvement : make this list of weightings configurable for each vehicle as in GH
        String[] weightings = {ProfileTools.VAL_FASTEST, ProfileTools.VAL_SHORTEST, ProfileTools.VAL_RECOMMENDED};
        for (String weighting : weightings) {
            if (hasTurnCosts) {
                String profileName = ProfileTools.makeProfileName(vehicle, weighting, true);
                profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(true));
            }
            String profileName = ProfileTools.makeProfileName(vehicle, weighting, false);
            profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(false));
        }

        if (Boolean.TRUE == profile.getBuild().getEncoderOptions().getEnableCustomModels()) {
            if (hasTurnCosts) {
                profiles.put(vehicle + "_custom_with_turn_costs", new CustomProfile(vehicle + "_custom_with_turn_costs").setCustomModel(new CustomModel().setDistanceInfluence(0)).setVehicle(vehicle).setTurnCosts(true));
            }
            profiles.put(vehicle + "_custom", new CustomProfile(vehicle + "_custom").setCustomModel(new CustomModel().setDistanceInfluence(0)).setVehicle(vehicle).setTurnCosts(false));
        }

        ghConfig.putObject(ProfileTools.KEY_PREPARE_CORE_WEIGHTINGS, "no");
        if (profile.getBuild().getPreparation() != null) {
            PreparationProperties preparations = profile.getBuild().getPreparation();


            if (preparations.getMinNetworkSize() != null)
                ghConfig.putObject("prepare.min_network_size", preparations.getMinNetworkSize());

            if (!preparations.getMethods().isEmpty()) {
                if (!preparations.getMethods().getCh().isEmpty()) {
                    PreparationProperties.MethodsProperties.CHProperties chOpts = preparations.getMethods().getCh();
                    prepareCH = Boolean.TRUE.equals(chOpts.getEnabled());
                    if (prepareCH) {
                        if (chOpts.getThreadsSave() != null)
                            ghConfig.putObject("prepare.ch.threads", chOpts.getThreadsSave());
                        if (chOpts.getWeightings() != null) {
                            List<CHProfile> chProfiles = new ArrayList<>();
                            String chWeightingsString = StringUtility.trimQuotes(chOpts.getWeightings());
                            for (String weighting : chWeightingsString.split(","))
                                chProfiles.add(new CHProfile(ProfileTools.makeProfileName(vehicle, weighting, hasTurnCosts)));
                            ghConfig.setCHProfiles(chProfiles);
                        }
                    }
                }

                if (!preparations.getMethods().getLm().isEmpty()) {
                    PreparationProperties.MethodsProperties.LMProperties lmOpts = preparations.getMethods().getLm();
                    prepareLM = Boolean.TRUE.equals(lmOpts.getEnabled());
                    if (prepareLM) {
                        if (lmOpts.getThreadsSave() != null)
                            ghConfig.putObject("prepare.lm.threads", lmOpts.getThreadsSave());
                        if (lmOpts.getWeightings() != null) {
                            List<LMProfile> lmProfiles = new ArrayList<>();
                            String lmWeightingsString = StringUtility.trimQuotes(lmOpts.getWeightings());
                            for (String weighting : lmWeightingsString.split(","))
                                lmProfiles.add(new LMProfile(ProfileTools.makeProfileName(vehicle, weighting, hasTurnCosts)));
                            ghConfig.setLMProfiles(lmProfiles);
                        }
                        if (lmOpts.getLandmarks() != null)
                            ghConfig.putObject("prepare.lm.landmarks", lmOpts.getLandmarks());
                    }
                }

                if (!preparations.getMethods().getCore().isEmpty()) {
                    PreparationProperties.MethodsProperties.CoreProperties coreOpts = preparations.getMethods().getCore();
                    prepareCore = Boolean.TRUE.equals(coreOpts.getEnabled());
                    if (prepareCore) {
                        if (coreOpts.getThreadsSave() != null) {
                            Integer threadsCore = coreOpts.getThreadsSave();
                            ghConfig.putObject("prepare.core.threads", threadsCore);
                            ghConfig.putObject("prepare.corelm.threads", threadsCore);
                        }
                        if (coreOpts.getWeightings() != null) {
                            List<CHProfile> coreProfiles = new ArrayList<>();
                            List<LMProfile> coreLMProfiles = new ArrayList<>();
                            String coreWeightingsString = StringUtility.trimQuotes(coreOpts.getWeightings());
                            for (String weighting : coreWeightingsString.split(",")) {
                                String configStr = "";
                                if (weighting.contains("|")) {
                                    configStr = weighting;
                                    weighting = weighting.split("\\|")[0];
                                }
                                PMap configMap = new PMap(configStr);
                                boolean considerTurnRestrictions = configMap.getBool("edge_based", hasTurnCosts);

                                String profileName = ProfileTools.makeProfileName(vehicle, weighting, considerTurnRestrictions);
                                profiles.put(profileName, new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(considerTurnRestrictions));
                                coreProfiles.add(new CHProfile(profileName));
                                coreLMProfiles.add(new LMProfile(profileName));
                            }
                            ghConfig.setCoreProfiles(coreProfiles);
                            ghConfig.setCoreLMProfiles(coreLMProfiles);
                        }
                        if (coreOpts.getLmsets() != null)
                            ghConfig.putObject("prepare.corelm.lmsets", StringUtility.trimQuotes(coreOpts.getLmsets()));
                        if (coreOpts.getLandmarks() != null)
                            ghConfig.putObject("prepare.corelm.landmarks", coreOpts.getLandmarks());
                    } else {
                        ghConfig.putObject(ProfileTools.KEY_PREPARE_CORE_WEIGHTINGS, "no");
                    }
                }

                if (!preparations.getMethods().getFastisochrones().isEmpty()) {
                    PreparationProperties.MethodsProperties.FastIsochroneProperties fastisochroneOpts = preparations.getMethods().getFastisochrones();
                    prepareFI = Boolean.TRUE.equals(fastisochroneOpts.getEnabled());
                    if (prepareFI) {
                        ghConfig.putObject(ORSParameters.FastIsochrone.PROFILE, profile.getEncoderName().toString());
                        //Copied from core
                        if (fastisochroneOpts.getThreadsSave() != null)
                            ghConfig.putObject("prepare.fastisochrone.threads", fastisochroneOpts.getThreadsSave());
                        if (fastisochroneOpts.getMaxcellnodes() != null)
                            ghConfig.putObject("prepare.fastisochrone.maxcellnodes", fastisochroneOpts.getMaxcellnodes());
                        if (fastisochroneOpts.getWeightings() != null) {
                            List<Profile> fastisochronesProfiles = new ArrayList<>();
                            String fastisochronesWeightingsString = StringUtility.trimQuotes(fastisochroneOpts.getWeightings());
                            for (String weighting : fastisochronesWeightingsString.split(",")) {
                                String configStr = "";
                                weighting = weighting.trim();
                                if (weighting.contains("|")) {
                                    configStr = weighting;
                                    weighting = weighting.split("\\|")[0];
                                }
                                PMap configMap = new PMap(configStr);
                                boolean considerTurnRestrictions = configMap.getBool("edge_based", hasTurnCosts);

                                String profileName = ProfileTools.makeProfileName(vehicle, weighting, considerTurnRestrictions);
                                Profile ghProfile = new Profile(profileName).setVehicle(vehicle).setWeighting(weighting).setTurnCosts(considerTurnRestrictions);
                                profiles.put(profileName, ghProfile);
                                fastisochronesProfiles.add(ghProfile);
                            }
                            ghConfig.setFastisochroneProfiles(fastisochronesProfiles);
                        }
                    } else {
                        ghConfig.putObject(ProfileTools.KEY_PREPARE_FASTISOCHRONE_WEIGHTINGS, "no");
                    }
                }
            }
        }

        if (profile.getService().getExecution() != null) {
            ExecutionProperties execution = profile.getService().getExecution();
            if (!execution.getMethods().getCore().isEmpty() && execution.getMethods().getCore().getActiveLandmarks() != null)
                ghConfig.putObject("routing.corelm.active_landmarks", execution.getMethods().getCore().getActiveLandmarks());

            if (!execution.getMethods().getLm().isEmpty() && execution.getMethods().getLm().getActiveLandmarks() != null)
                ghConfig.putObject("routing.lm.active_landmarks", execution.getMethods().getLm().getActiveLandmarks());
        }

        if (Boolean.TRUE.equals(profile.getBuild().getOptimize()) && !prepareCH)
            ghConfig.putObject("graph.do_sort", true);

        // Check if getGTFSFile exists
        if (profile.getBuild().getGtfsFile() != null && !profile.getBuild().getGtfsFile().toString().isEmpty())
            ghConfig.putObject("gtfs.file", profile.getBuild().getGtfsFile().toAbsolutePath().toString());

        String flagEncoder = vehicle;
        if (!Helper.isEmpty(profile.getBuild().getEncoderOptionsString()))
            flagEncoder += "|" + profile.getBuild().getEncoderOptionsString();

        ghConfig.putObject("graph.flag_encoders", flagEncoder.toLowerCase());
        ghConfig.putObject("index.high_resolution", profile.getBuild().getLocationIndexResolution());
        ghConfig.putObject("index.max_region_search", profile.getBuild().getLocationIndexSearchIterations());
        ghConfig.setProfiles(new ArrayList<>(profiles.values()));

        return ghConfig;
    }

    public List<CHProfile> getCoreProfiles() {
        return coreProfiles;
    }

    public GraphHopperConfig setCoreProfiles(List<CHProfile> coreProfiles) {
        this.coreProfiles = coreProfiles;
        return this;
    }

    public List<LMProfile> getCoreLMProfiles() {
        return coreLMProfiles;
    }

    public void setCoreLMProfiles(List<LMProfile> coreLMProfiles) {
        this.coreLMProfiles = coreLMProfiles;
        String coreLmThreadsKey = "prepare.corelm.threads";
        if (has(coreLmThreadsKey))
            putObject(coreLmThreadsKey, getInt(coreLmThreadsKey, 1));
    }

    public List<Profile> getFastisochroneProfiles() {
        return fastisochroneProfiles;
    }

    public GraphHopperConfig setFastisochroneProfiles(List<Profile> fastisochroneProfiles) {
        this.fastisochroneProfiles = fastisochroneProfiles;
        return this;
    }
}
