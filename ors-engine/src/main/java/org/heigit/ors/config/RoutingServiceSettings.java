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

import java.util.List;
import java.util.Map;

public class RoutingServiceSettings {
    private static final String SERVICE_NAME_ROUTING = "routing";
    private static boolean distanceApproximation = false;
    private static AppConfig config;

    static {
        config = AppConfig.getGlobal();
        init(config);
    }

    private RoutingServiceSettings() {
    }

    public static void loadFromFile(String path) {
        config = new AppConfig(path);
        init(config);
    }

    private static void init(AppConfig config) {
        String value = config.getServiceParameter(SERVICE_NAME_ROUTING, "distance_approximation");
        if (value != null)
            distanceApproximation = Boolean.parseBoolean(value);

    }

    public static boolean getDistanceApproximation() {
        return distanceApproximation;
    }

    public static String getParameter(String paramName) {
        return config.getServiceParameter(SERVICE_NAME_ROUTING, paramName);
    }

    public static String getParameter(String paramName, boolean notNull) {
        String value = config.getServiceParameter(SERVICE_NAME_ROUTING, paramName);
        if (notNull && Helper.isEmpty(value))
            throw new IllegalArgumentException("Parameter '" + paramName + "' must not be null or empty.");

        return value;
    }

    public static List<String> getParametersList(String paramName) {
        return config.getServiceParametersList(SERVICE_NAME_ROUTING, paramName);
    }

    public static Map<String, Object> getParametersMap(String paramName, boolean quotedStrings) {
        return config.getServiceParametersMap(SERVICE_NAME_ROUTING, paramName, quotedStrings);
    }

}
