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
package org.heigit.ors.config;

import com.graphhopper.util.Helper;
import com.typesafe.config.ConfigObject;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import org.heigit.ors.routing.RoutingProfileType;

import java.util.*;
import java.util.Map.Entry;

import static org.heigit.ors.common.TravelRangeType.DISTANCE;

public class IsochronesServiceSettings {
    public static final String SERVICE_NAME_ISOCHRONES = "isochrones";
    public static final String SERVICE_NAME_FASTISOCHRONES = "fastisochrones.";
    public static final String PARAM_STATISTICS_PROVIDERS = "statistics_providers.";
    public static final String KEY_ATTRIBUTION = "attribution";
    private static boolean enabled = true;
    private static int maximumLocations = 1;
    private static int maximumRangeDistance = 100000; //  in meters
    private static Map<Integer, Integer> profileMaxRangeDistances;
    private static int maximumRangeTime = 3600; // in seconds
    private static Map<Integer, Integer> profileMaxRangeTimes;
    private static int fastIsochronesMaximumRangeDistance = 100000; //  in meters
    private static Map<Integer, Integer> fastIsochronesProfileMaxRangeDistances;
    private static int fastIsochronesMaximumRangeTime = 3600; // in seconds
    private static Map<Integer, Integer> fastIsochronesProfileMaxRangeTimes;
    private static Set<Integer> fastIsochroneProfiles = new HashSet<>();
    private static int maximumIntervals = 1;
    private static boolean allowComputeArea = true;
    private static Map<String, StatisticsProviderConfiguration> statsProviders;
    private static String attribution = "";
    private static String weightings = "";
    private static AppConfig config;

    static {
        config = AppConfig.getGlobal();
        String value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "enabled");
        if (value != null)
            enabled = Boolean.parseBoolean(value);
        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_locations");
        if (value != null)
            maximumLocations = Integer.parseInt(value);
        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "weightings");
        if (value != null)
            weightings = value;

        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_range_distance");
        if (value != null)
            maximumRangeDistance = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.getGlobal().getObjectList(SERVICE_NAME_ISOCHRONES, "maximum_range_distance");
            profileMaxRangeDistances = getParameters(params);
            if (profileMaxRangeDistances.containsKey(-1))
                maximumRangeDistance = profileMaxRangeDistances.get(-1);
        }

        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_range_time");
        if (value != null)
            maximumRangeTime = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.getGlobal().getObjectList(SERVICE_NAME_ISOCHRONES, "maximum_range_time");
            profileMaxRangeTimes = getParameters(params);
            if (profileMaxRangeTimes.containsKey(-1))
                maximumRangeTime = profileMaxRangeTimes.get(-1);
        }

        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "maximum_intervals");
        if (value != null)
            maximumIntervals = Integer.parseInt(value);
        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, "allow_compute_area");
        if (value != null)
            allowComputeArea = Boolean.parseBoolean(value);

        //Fast isochrones
        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_distance");
        if (value != null)
            fastIsochronesMaximumRangeDistance = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.getGlobal().getObjectList(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_distance");
            fastIsochronesProfileMaxRangeDistances = getParameters(params);
            if (fastIsochronesProfileMaxRangeDistances.containsKey(-1))
                fastIsochronesMaximumRangeDistance = fastIsochronesProfileMaxRangeDistances.get(-1);
        }

        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_time");
        if (value != null)
            fastIsochronesMaximumRangeTime = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.getGlobal().getObjectList(SERVICE_NAME_ISOCHRONES, SERVICE_NAME_FASTISOCHRONES + "maximum_range_time");
            fastIsochronesProfileMaxRangeTimes = getParameters(params);
            if (fastIsochronesProfileMaxRangeTimes.containsKey(-1))
                fastIsochronesMaximumRangeTime = fastIsochronesProfileMaxRangeTimes.get(-1);
        }

        statsProviders = new HashMap<>();

        Map<String, Object> providers = AppConfig.getGlobal().getServiceParametersMap(SERVICE_NAME_ISOCHRONES, "statistics_providers", false);
        if (providers != null) {
            int id = 0;
            for (Map.Entry<String, Object> entry : providers.entrySet()) {
                Map<String, Object> provider = AppConfig.getGlobal().getServiceParametersMap(SERVICE_NAME_ISOCHRONES, PARAM_STATISTICS_PROVIDERS + entry.getKey(), false);

                if (provider.containsKey("provider_name") && provider.containsKey("provider_parameters") && provider.containsKey("property_mapping")) {
                    String provName = provider.get("provider_name").toString();

                    Map<String, Object> providerParams = AppConfig.getGlobal().getServiceParametersMap(SERVICE_NAME_ISOCHRONES, PARAM_STATISTICS_PROVIDERS + entry.getKey() + ".provider_parameters", false);
                    Map<String, Object> map = AppConfig.getGlobal().getServiceParametersMap(SERVICE_NAME_ISOCHRONES, PARAM_STATISTICS_PROVIDERS + entry.getKey() + ".property_mapping", false);
                    Map<String, String> propMapping = new HashMap<>();

                    for (Map.Entry<String, Object> propEntry : map.entrySet())
                        propMapping.put(propEntry.getValue().toString(), propEntry.getKey());

                    if (propMapping.size() > 0) {
                        String attribution = null;
                        if (provider.containsKey(KEY_ATTRIBUTION))
                            attribution = provider.get(KEY_ATTRIBUTION).toString();

                        id++;
                        StatisticsProviderConfiguration provConfig = new StatisticsProviderConfiguration(id, provName, providerParams, propMapping, attribution);
                        for (Entry<String, String> property : propMapping.entrySet())
                            statsProviders.put(property.getKey().toLowerCase(), provConfig);
                    }
                }
            }
        }

        value = AppConfig.getGlobal().getServiceParameter(SERVICE_NAME_ISOCHRONES, KEY_ATTRIBUTION);
        if (value != null)
            attribution = value;
    }

    private IsochronesServiceSettings() {
    }

    public static void loadFromFile(String path) {
        config = new AppConfig(path);
    }

    private static Map<Integer, Integer> getParameters(List<? extends ConfigObject> params) {
        Map<Integer, Integer> result = new HashMap<>();

        for (ConfigObject cfgObj : params) {
            if (cfgObj.containsKey("profiles") && cfgObj.containsKey("value")) {
                String[] profiles = cfgObj.toConfig().getString("profiles").split(",");
                for (String profileStr : profiles) {
                    profileStr = profileStr.trim();
                    Integer profile = ("any".equalsIgnoreCase(profileStr)) ? -1 : RoutingProfileType.getFromString(profileStr);
                    if (profile != RoutingProfileType.UNKNOWN)
                        result.put(profile, cfgObj.toConfig().getInt("value"));
                }
            }
        }

        return result;
    }

    public static boolean getEnabled() {
        return enabled;
    }

    public static void setFastIsochronesActive(String profile) {
        int routingProfile = RoutingProfileType.getFromString(profile);
        fastIsochroneProfiles.add(routingProfile);
    }

	public static String getWeightings() {
		return weightings.equals("") ? "fastest" : weightings;
	}

    public static boolean getAllowComputeArea() {
        return allowComputeArea;
    }

    public static int getMaximumLocations() {
        return maximumLocations;
    }

    public static int getMaximumRange(int profileType, String calcMethod, TravelRangeType range) {
        Integer res;
        if (fastIsochroneProfiles.contains(profileType) && calcMethod.equalsIgnoreCase("fastisochrone"))
            return getMaximumRangeFastIsochrone(profileType, range);
        if (range == DISTANCE) {
            res = maximumRangeDistance;
            if (profileMaxRangeDistances != null && profileMaxRangeDistances.containsKey(profileType)) {
                res = profileMaxRangeDistances.get(profileType);
            }
        } else {
            res = maximumRangeTime;
            if (profileMaxRangeTimes != null && profileMaxRangeTimes.containsKey(profileType)) {
                res = profileMaxRangeTimes.get(profileType);
            }
        }

        return res;
    }

    private static int getMaximumRangeFastIsochrone(int profileType, TravelRangeType range) {
        Integer res;
        if (range == DISTANCE) {
            res = fastIsochronesMaximumRangeDistance;
            if (fastIsochronesProfileMaxRangeDistances != null && fastIsochronesProfileMaxRangeDistances.containsKey(profileType)) {
                res = fastIsochronesProfileMaxRangeDistances.get(profileType);
            }
        } else {
            res = fastIsochronesMaximumRangeTime;
            if (fastIsochronesProfileMaxRangeTimes != null && fastIsochronesProfileMaxRangeTimes.containsKey(profileType)) {
                res = fastIsochronesProfileMaxRangeTimes.get(profileType);
            }
        }

        return res;
    }

    public static int getMaximumIntervals() {
        return maximumIntervals;
    }

    public static Map<String, StatisticsProviderConfiguration> getStatsProviders() {
        return statsProviders;
    }

    public static boolean isStatsAttributeSupported(String attrName) {
        if (statsProviders == null || attrName == null)
            return false;

        return statsProviders.containsKey(attrName.toLowerCase());
    }

    public static String getAttribution() {
        return attribution;
    }

    public static String getParameter(String paramName) {
        return config.getServiceParameter(SERVICE_NAME_ISOCHRONES, paramName);
    }

	public static String getParameter(String paramName, boolean notNull) {
		String value = config.getServiceParameter(SERVICE_NAME_ISOCHRONES, paramName);
		if (notNull && Helper.isEmpty(value))
			throw new IllegalArgumentException("Parameter '" + paramName + "' must not be null or empty.");

        return value;
    }

    public static List<String> getParametersList(String paramName) {
        return config.getServiceParametersList(SERVICE_NAME_ISOCHRONES, paramName);
    }

    public static List<Double> getDoubleList(String paramName) {
        return config.getDoubleList(SERVICE_NAME_ISOCHRONES, paramName);
    }

    public static Map<String, Object> getParametersMap(String paramName, boolean quotedStrings) {
        return config.getServiceParametersMap(SERVICE_NAME_ISOCHRONES, paramName, quotedStrings);
    }
}
