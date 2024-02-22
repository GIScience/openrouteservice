package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.util.ConditionalSpeedCalculator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.ConditionalEdges;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.routing.ProfileWeighting;
import org.heigit.ors.routing.RouteRequestParameterNames;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.util.MaximumSpeedCalculator;
import org.heigit.ors.routing.graphhopper.extensions.weighting.*;
import org.heigit.ors.routing.traffic.RoutingTrafficSpeedCalculator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;
import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;
import static com.graphhopper.util.Helper.toLowerCase;

/**
 * This class is a preliminary adaptation of ORSWeightingFactory to the new
 * interface of GH's WeightingFactory. ORSWeightingFactory was copy-pasted
 * and modified from GH's DefaultWeightingFactory. OrsWeightingFactoryGh4
 * is meant to handle these extensions to DefaultWeightingFactory across the
 * two code bases more cleanly. The modifications in ORSWeightingFactory
 * should be transferred into OrsWeightingFactoryGh4 on need, in order
 * to figure out, which parts of ORSWeightingFactory are still needed and which
 * ones are remnants of unmaintained features.
 */
public class ORSWeightingFactory implements WeightingFactory {
    protected final GraphHopperStorage ghStorage;
    protected final EncodingManager encodingManager;

    public ORSWeightingFactory(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
        this.ghStorage = ghStorage;
        this.encodingManager = encodingManager;
    }

    @Override
    public Weighting createWeighting(Profile profile, PMap requestHints, boolean disableTurnCosts) {
        // Merge profile hints with request hints, the request hints take precedence.
        // Note that so far we do not check if overwriting the profile hints actually works with the preparation
        // for LM/CH. Later we should also limit the number of parameters that can be used to modify the profile.
        // todo: since we are not dealing with block_area here yet we cannot really apply any merging rules
        // for it, see discussion here: https://github.com/graphhopper/graphhopper/pull/1958#discussion_r395462901
        PMap hints = new PMap();
        hints.putAll(profile.getHints());
        hints.putAll(requestHints);

        FlagEncoder encoder = encodingManager.getEncoder(profile.getVehicle());
        TurnCostProvider turnCostProvider;
        if (profile.isTurnCosts() && !disableTurnCosts) {
            if (!encoder.supportsTurnCosts())
                throw new IllegalArgumentException("Encoder " + encoder + " does not support turn costs");
            int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, INFINITE_U_TURN_COSTS);
            turnCostProvider = new DefaultTurnCostProvider(encoder, ghStorage.getTurnCostStorage(), uTurnCosts);
        } else {
            turnCostProvider = NO_TURN_COST_PROVIDER;
        }

        // ORS-GH MOD START - use weighting method determined by ORS
        String weightingStr = hints.getString("weighting_method", "").toLowerCase();
        if (Helper.isEmpty(weightingStr))
            weightingStr = toLowerCase(profile.getWeighting());
        // ORS-GH MOD END
        if (weightingStr.isEmpty())
            throw new IllegalArgumentException("You need to specify a weighting");

        Weighting weighting = null;
        if ("shortest".equalsIgnoreCase(weightingStr)) {
            weighting = new ShortestWeighting(encoder, turnCostProvider);
        } else if ("fastest".equalsIgnoreCase(weightingStr) || "recommended".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new ORSPriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new ORSFastestWeighting(encoder, hints, turnCostProvider);
            }
        } else {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new FastestSafeWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new ORSFastestWeighting(encoder, hints, turnCostProvider);
            }
        }

        weighting = applySoftWeightings(hints, encoder, weighting);

        setSpeedCalculator(weighting, hints);

        if (isRequestTimeDependent(hints))
            weighting = createTimeDependentAccessWeighting(weighting);

        weighting = new LimitedAccessWeighting(weighting, requestHints);

        return weighting;

    }

    /**
     * Potentially wraps the specified weighting into a TimeDependentAccessWeighting.
     */
    private Weighting createTimeDependentAccessWeighting(Weighting weighting) {
        FlagEncoder flagEncoder = weighting.getFlagEncoder();
        if (encodingManager.hasEncodedValue(EncodingManager.getKey(flagEncoder, ConditionalEdges.ACCESS)))
            return new TimeDependentAccessWeighting(weighting, ghStorage, flagEncoder);
        else
            return weighting;
    }

    public Weighting createIsochroneWeighting(Profile profile) {
        FlagEncoder encoder = this.encodingManager.getEncoder(profile.getVehicle());
        String weightingStr = toLowerCase(profile.getWeighting());
        Weighting result = null;

        //Isochrones only support fastest or shortest as no path is found.
        //CalcWeight must be directly comparable to the isochrone limit

        if ("shortest".equalsIgnoreCase(weightingStr)) {
            result = new ShortestWeighting(encoder);
        } else if ("fastest".equalsIgnoreCase(weightingStr)
                || "priority".equalsIgnoreCase(weightingStr)
                || "recommended_pref".equalsIgnoreCase(weightingStr)
                || "recommended".equalsIgnoreCase(weightingStr)) {
            result = new ORSFastestWeighting(encoder);
        }

        return result;
    }

    public static Weighting createIsochroneWeighting(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
        if (travelRangeType == TravelRangeType.TIME) {
            return new ORSFastestWeighting(searchContext.getEncoder());
        } else {
            return new ShortestWeighting(searchContext.getEncoder());
        }
    }

    protected void setSpeedCalculator(Weighting weighting, PMap requestHints) {
        if (isRequestTimeDependent(requestHints)) {
            // OSM conditionals
            FlagEncoder encoder = weighting.getFlagEncoder();
            if (encodingManager.hasEncodedValue(EncodingManager.getKey(encoder, ConditionalEdges.SPEED)))
                weighting.setSpeedCalculator(new ConditionalSpeedCalculator(weighting.getSpeedCalculator(), ghStorage, encoder));

            // traffic data
            Instant time = requestHints.getObject(requestHints.has(RouteRequestParameterNames.PARAM_DEPARTURE) ?
                    RouteRequestParameterNames.PARAM_DEPARTURE : RouteRequestParameterNames.PARAM_ARRIVAL, null);
            addTrafficSpeedCalculator(weighting, ghStorage, time);
        }

        if (requestHints.has("maximum_speed")) {
            double maximumSpeedLowerBound = requestHints.getDouble("maximum_speed_lower_bound", 0);
            double maximumSpeed = requestHints.getDouble("maximum_speed", maximumSpeedLowerBound);
            weighting.setSpeedCalculator(new MaximumSpeedCalculator(weighting.getSpeedCalculator(), maximumSpeed));
        }
    }

    protected boolean isRequestTimeDependent(PMap hints) {
        return hints.has(RouteRequestParameterNames.PARAM_DEPARTURE) || hints.has(RouteRequestParameterNames.PARAM_ARRIVAL);
    }

    protected Weighting applySoftWeightings(PMap hints, FlagEncoder encoder, Weighting weighting) {
        // TODO (cleanup): The term "custom_weighting" is easily confused with GH's custom
        //                 weighting and should be renamed.
        if (hints.getBool("custom_weightings", false)) {
            Map<String, Object> map = hints.toMap();

            List<String> weightingNames = new ArrayList<>();
            for (Map.Entry<String, Object> kv : map.entrySet()) {
                String name = ProfileWeighting.decodeName(kv.getKey());
                if (name != null && !weightingNames.contains(name))
                    weightingNames.add(name);
            }

            List<Weighting> softWeightings = new ArrayList<>();

            for (String weightingName : weightingNames) {
                switch (weightingName) {
                    case "steepness_difficulty" ->
                            softWeightings.add(new SteepnessDifficultyWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    case "avoid_hills" ->
                            softWeightings.add(new AvoidHillsWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    case "green" ->
                            softWeightings.add(new GreenWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    case "quiet" ->
                            softWeightings.add(new QuietWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    case "csv" ->
                            softWeightings.add(new HeatStressWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    case "shadow" ->
                            softWeightings.add(new ShadowWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                    default -> {
                    }
                }
            }

            if (!softWeightings.isEmpty()) {
                weighting = new AdditionWeighting(softWeightings, weighting);
            }
        }
        return weighting;
    }

    private PMap getWeightingProps(String weightingName, Map<String, Object> map) {
        PMap res = new PMap();

        String prefix = "weighting_#" + weightingName;
        int n = prefix.length();

        for (Map.Entry<String, Object> kv : map.entrySet()) {
            String name = kv.getKey();
            int p = name.indexOf(prefix);
            if (p >= 0)
                res.putObject(name.substring(p + n + 1), kv.getValue());
        }

        return res;
    }

    public static void addTrafficSpeedCalculator(List<Weighting> weightings, GraphHopperStorage ghStorage) {
        for (Weighting weighting : weightings)
            addTrafficSpeedCalculator(weighting, ghStorage, null);
    }

    private static void addTrafficSpeedCalculator(Weighting weighting, GraphHopperStorage ghStorage, Instant time) {
        TrafficGraphStorage trafficGraphStorage = GraphStorageUtils.getGraphExtension(ghStorage, TrafficGraphStorage.class);

        if (trafficGraphStorage != null) {
            RoutingTrafficSpeedCalculator routingTrafficSpeedCalculator = new RoutingTrafficSpeedCalculator(weighting.getSpeedCalculator(), ghStorage, weighting.getFlagEncoder());

            if (time != null) {
                //Use fixed time zone because original implementation was for German traffic data
                ZonedDateTime zdt = time.atZone(ZoneId.of("Europe/Berlin"));
                routingTrafficSpeedCalculator.setZonedDateTime(zdt);
            }

            weighting.setSpeedCalculator(routingTrafficSpeedCalculator);
        }
    }
}
