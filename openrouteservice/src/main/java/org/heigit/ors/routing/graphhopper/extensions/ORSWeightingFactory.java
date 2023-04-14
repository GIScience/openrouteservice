package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.DefaultWeightingFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.routing.ProfileWeighting;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.util.MaximumSpeedCalculator;
import org.heigit.ors.routing.graphhopper.extensions.weighting.*;
import org.heigit.ors.routing.traffic.RoutingTrafficSpeedCalculator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class ORSWeightingFactory extends DefaultWeightingFactory {
    public ORSWeightingFactory(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
        super(ghStorage, encodingManager);
    }

    @Override
    protected Weighting handleExternalOrsWeightings(String weightingStr, PMap hints, FlagEncoder encoder, TurnCostProvider turnCostProvider) {
        Weighting weighting = null;
        if ("priority".equalsIgnoreCase(weightingStr)) {
            weighting = new PreferencePriorityWeighting(encoder, hints);
        } else if ("recommended_pref".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new PreferencePriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        } else if ("recommended".equalsIgnoreCase(weightingStr)) {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new OptimizedPriorityWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        } else {
            if (encoder.supports(PriorityWeighting.class)) {
                weighting = new FastestSafeWeighting(encoder, hints, turnCostProvider);
            } else {
                weighting = new FastestWeighting(encoder, hints, turnCostProvider);
            }
        }

        return weighting;
    }

    public Weighting createIsochroneWeighting(Profile profile, PMap requestHints) {
        FlagEncoder encoder = this.encodingManager.getEncoder(profile.getVehicle());
        String weightingStr = toLowerCase(profile.getWeighting());
        Weighting result = null;

        //Isochrones only support fastest or shortest as no path is found.
        //CalcWeight must be directly comparable to the isochrone limit

        if ("shortest".equalsIgnoreCase(weightingStr))
        {
            result = new ShortestWeighting(encoder);
        }
        else if ("fastest".equalsIgnoreCase(weightingStr)
                || "priority".equalsIgnoreCase(weightingStr)
                || "recommended_pref".equalsIgnoreCase(weightingStr)
                || "recommended".equalsIgnoreCase(weightingStr))
        {
            result = new FastestWeighting(encoder, requestHints);
        }

        return result;
    }

    public static Weighting createIsochroneWeighting(RouteSearchContext searchContext, TravelRangeType travelRangeType) {
        if (travelRangeType == TravelRangeType.TIME) {
            return new FastestWeighting(searchContext.getEncoder());
        } else {
            return new ShortestWeighting(searchContext.getEncoder());
        }
    }

    @Override
    protected void setSpeedCalculator(Weighting weighting, PMap requestHints) {
        super.setSpeedCalculator(weighting, requestHints);

        if (isRequestTimeDependent(requestHints)) {
            String time = requestHints.getString(requestHints.has(RouteRequest.PARAM_DEPARTURE) ?
                    RouteRequest.PARAM_DEPARTURE : RouteRequest.PARAM_ARRIVAL, "");
            addTrafficSpeedCalculator(weighting, ghStorage, time);
        }

        if (requestHints.has("maximum_speed")) {
            double maximumSpeedLowerBound = requestHints.getDouble("maximum_speed_lower_bound", 0);
            double maximumSpeed = requestHints.getDouble("maximum_speed", maximumSpeedLowerBound);
            weighting.setSpeedCalculator(new MaximumSpeedCalculator(weighting.getSpeedCalculator(), maximumSpeed));
        }
    }

    @Override
    protected boolean isRequestTimeDependent(PMap hints) {
        return hints.has(RouteRequest.PARAM_DEPARTURE) || hints.has(RouteRequest.PARAM_ARRIVAL);
    }

    @Override
    protected Weighting applySoftWeightings(PMap hints, FlagEncoder encoder, Weighting weighting) {
        // TODO (cleanup): The term "custom_weighting" is easily confused with GH's custom
        //                 weighting and should be renamed.
        if (hints.getBool("custom_weightings", false))
        {
            Map<String, Object> map = hints.toMap();

            List<String> weightingNames = new ArrayList<>();
            for (Map.Entry<String, Object> kv : map.entrySet())
            {
                String name = ProfileWeighting.decodeName(kv.getKey());
                if (name != null && !weightingNames.contains(name))
                    weightingNames.add(name);
            }

            List<Weighting> softWeightings = new ArrayList<>();

            for (String weightingName : weightingNames) {
                switch (weightingName) {
                    case "steepness_difficulty":
                        softWeightings.add(new SteepnessDifficultyWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "avoid_hills":
                        softWeightings.add(new AvoidHillsWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "green":
                        softWeightings.add(new GreenWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "quiet":
                        softWeightings.add(new QuietWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "acceleration":
                        softWeightings.add(new AccelerationWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "csv":
                        softWeightings.add(new HeatStressWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    case "shadow":
                        softWeightings.add(new ShadowWeighting(encoder, getWeightingProps(weightingName, map), ghStorage));
                        break;
                    default:
                        break;
                }
            }

            if (!softWeightings.isEmpty()) {
                weighting = new AdditionWeighting(softWeightings, weighting);
            }
        }
        return weighting;
    }

    private PMap getWeightingProps(String weightingName, Map<String, Object> map)
    {
        PMap res = new PMap();

        String prefix = "weighting_#" + weightingName;
        int n = prefix.length();

        for (Map.Entry<String, Object> kv : map.entrySet())
        {
            String name = kv.getKey();
            int p = name.indexOf(prefix);
            if (p >= 0)
                res.putObject(name.substring(p + n + 1), kv.getValue());
        }

        return res;
    }

    public static void addTrafficSpeedCalculator(List<Weighting> weightings, GraphHopperStorage ghStorage) {
        for (Weighting weighting : weightings)
            addTrafficSpeedCalculator(weighting, ghStorage, "");
    }

    private static void addTrafficSpeedCalculator(Weighting weighting, GraphHopperStorage ghStorage, String time) {
        TrafficGraphStorage trafficGraphStorage = GraphStorageUtils.getGraphExtension(ghStorage, TrafficGraphStorage.class);

        if (trafficGraphStorage != null) {
            RoutingTrafficSpeedCalculator routingTrafficSpeedCalculator = new RoutingTrafficSpeedCalculator(weighting.getSpeedCalculator(), ghStorage, weighting.getFlagEncoder());

            if (!time.isEmpty()) {
                //Use fixed time zone because original implementation was for German traffic data
                ZonedDateTime zdt = LocalDateTime.parse(time).atZone(ZoneId.of("Europe/Berlin"));
                routingTrafficSpeedCalculator.setZonedDateTime(zdt);
            }

            weighting.setSpeedCalculator(routingTrafficSpeedCalculator);
        }
    }
}
