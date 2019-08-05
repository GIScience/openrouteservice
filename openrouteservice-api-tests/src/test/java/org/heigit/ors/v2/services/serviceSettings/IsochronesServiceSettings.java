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
package heigit.ors.v2.services.serviceSettings;

import com.typesafe.config.ConfigObject;
import heigit.ors.v2.services.config.AppConfig;
import heigit.ors.v2.services.config.RoutingProfileType;
import heigit.ors.v2.services.config.StatisticsProviderConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class IsochronesServiceSettings {
    private static boolean enabled = true;
    private static int maximumLocations = 1;
    private static int maximumRangeDistance = 100000; //  in meters
    private static Map<Integer, Integer> profileMaxRangeDistances;
    private static int maximumRangeTime = 3600; // in seconds
    private static Map<Integer, Integer> profileMaxRangeTimes;
    private static int maximumIntervals = 1;
    private static boolean allowComputeArea = true;
    private static Map<String, StatisticsProviderConfiguration> statsProviders;
    private static String attribution = "";

    static {
        String value = AppConfig.Global().getServiceParameter("isochrones", "enabled");
        if (value != null)
            enabled = Boolean.parseBoolean(value);
        value = AppConfig.Global().getServiceParameter("isochrones", "maximum_locations");
        if (value != null)
            maximumLocations = Integer.parseInt(value);
        value = AppConfig.Global().getServiceParameter("isochrones", "maximum_range_distance");
        if (value != null)
            maximumRangeDistance = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.Global().getObjectList("isochrones", "maximum_range_distance");
            if (params != null) {
                profileMaxRangeDistances = getParameters(params);
                if (profileMaxRangeDistances.containsKey(-1))
                    maximumRangeDistance = profileMaxRangeDistances.get(-1);
            }
        }

        value = AppConfig.Global().getServiceParameter("isochrones", "maximum_range_time");
        if (value != null)
            maximumRangeTime = Integer.parseInt(value);
        else {
            List<? extends ConfigObject> params = AppConfig.Global().getObjectList("isochrones", "maximum_range_time");
            if (params != null) {
                profileMaxRangeTimes = getParameters(params);
                if (profileMaxRangeTimes.containsKey(-1))
                    maximumRangeTime = profileMaxRangeTimes.get(-1);
            }
        }

        value = AppConfig.Global().getServiceParameter("isochrones", "maximum_intervals");
        if (value != null)
            maximumIntervals = Integer.parseInt(value);
        value = AppConfig.Global().getServiceParameter("isochrones", "allow_compute_area");
        if (value != null)
            allowComputeArea = Boolean.parseBoolean(value);

        statsProviders = new HashMap<String, StatisticsProviderConfiguration>();

        Map<String, Object> providers = AppConfig.Global().getServiceParametersMap("isochrones", "statistics_providers", false);
        if (providers != null) {
            int id = 0;
            for (Entry<String, Object> entry : providers.entrySet()) {
                Map<String, Object> provider = AppConfig.Global().getServiceParametersMap("isochrones", "statistics_providers." + entry.getKey(), false);

                if (provider.containsKey("provider_name") && provider.containsKey("provider_parameters") && provider.containsKey("property_mapping")) {
                    String provName = provider.get("provider_name").toString();

                    Map<String, Object> providerParams = AppConfig.Global().getServiceParametersMap("isochrones", "statistics_providers." + entry.getKey() + ".provider_parameters", false);
                    Map<String, Object> map = AppConfig.Global().getServiceParametersMap("isochrones", "statistics_providers." + entry.getKey() + ".property_mapping", false);
                    Map<String, String> propMapping = new HashMap<String, String>();

                    for (Entry<String, Object> propEntry : map.entrySet())
                        propMapping.put(propEntry.getValue().toString(), propEntry.getKey());

                    if (propMapping.size() > 0) {
                        String attribution = null;
                        if (provider.containsKey("attribution"))
                            attribution = provider.get("attribution").toString();

                        id++;
                        StatisticsProviderConfiguration provConfig = new StatisticsProviderConfiguration(id, provName, providerParams, propMapping, attribution);
                        for (Entry<String, String> property : propMapping.entrySet())
                            statsProviders.put(property.getKey().toLowerCase(), provConfig);
                    }
                }
            }
        }

        value = AppConfig.Global().getServiceParameter("isochrones", "attribution");
        if (value != null)
            attribution = value;
    }

    private static Map<Integer, Integer> getParameters(List<? extends ConfigObject> params) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();

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

    public static boolean getAllowComputeArea() {
        return allowComputeArea;
    }

    public static int getMaximumLocations() {
        return maximumLocations;
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
}
