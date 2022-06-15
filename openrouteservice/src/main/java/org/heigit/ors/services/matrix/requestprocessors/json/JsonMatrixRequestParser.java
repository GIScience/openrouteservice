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
package org.heigit.ors.services.matrix.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.exceptions.MissingParameterException;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.exceptions.UnknownParameterValueException;
import org.heigit.ors.matrix.MatrixErrorCodes;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.routing.WeightingMethod;
import org.heigit.ors.util.ArraysUtility;
import org.heigit.ors.util.CoordTools;
import org.heigit.ors.util.DistanceUnitUtil;
import org.heigit.ors.util.JsonUtility;
import org.heigit.ors.util.StreamUtility;

public class JsonMatrixRequestParser {

    public static final String KEY_PROFILE = "profile";
    public static final String KEY_PREFERENCE = "preference";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_SOURCES = "sources";
    public static final String KEY_DESTINATIONS = "destinations";
    public static final String KEY_METRICS = "metrics";
    public static final String KEY_UNITS = "units";
    public static final String KEY_RESOLVE_LOCATIONS = "resolve_locations";
    public static final String KEY_OPTIMIZED = "optimized";
    public static final String VAL_FALSE = "false";

    private JsonMatrixRequestParser() {}

    public static MatrixRequest parseFromStream(InputStream stream) throws Exception {
        String body = StreamUtility.readStream(stream);

        if (Helper.isEmpty(body))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");

        JSONObject json;

        try {
            json = new JSONObject(body);
        } catch (Exception ex) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document." + ex.getMessage());
        }

        MatrixRequest req = new MatrixRequest();

        String value = json.optString(KEY_PROFILE);

        if (!Helper.isEmpty(value)) {
            int profileType = RoutingProfileType.getFromString(value);
            if (profileType == RoutingProfileType.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE, value);
            req.setProfileType(profileType);
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, KEY_PROFILE);
        }

        // MARQ24 WHERE the heck are the 'preferences here???
        // -> so I add them!
        value = json.optString(KEY_PREFERENCE);
        if (!Helper.isEmpty(value)) {
            int weightingMethod = WeightingMethod.getFromString(value);
            if (weightingMethod == WeightingMethod.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_PREFERENCE, value);
            req.setWeightingMethod(weightingMethod);
        }


        JSONArray jLocations = json.optJSONArray(KEY_LOCATIONS);
        Coordinate[] locations = null;

        if (jLocations != null) {
            try {
                int nLocations = jLocations.length();
                if (nLocations < 2)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_LOCATIONS);

                locations = new Coordinate[nLocations];

                for (int i = 0; i < nLocations; i++) {
                    JSONArray jCoordinate = jLocations.getJSONArray(i);

                    if (jCoordinate.length() < 2)
                        throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_LOCATIONS);

                    locations[i] = new Coordinate(jCoordinate.getDouble(0), jCoordinate.getDouble(1));
                }
            } catch (NumberFormatException|JSONException ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_LOCATIONS);
            }
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, KEY_LOCATIONS);
        }

        if (json.has(KEY_SOURCES)) {
            JSONArray jSources = json.optJSONArray(KEY_SOURCES);

            if (jSources != null) {
                int[] index = JsonUtility.parseIntArray(jSources, KEY_SOURCES, MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
                req.setSources(getLocations(locations, index, KEY_SOURCES));
            } else
                req.setSources(getLocations(locations, json.getString(KEY_SOURCES), KEY_SOURCES));
        } else
            req.setSources(locations);

        if (json.has(KEY_DESTINATIONS)) {
            JSONArray jDestinations = json.optJSONArray(KEY_DESTINATIONS);

            if (jDestinations != null) {
                int[] index = JsonUtility.parseIntArray(jDestinations, KEY_DESTINATIONS, MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
                req.setDestinations(getLocations(locations, index, KEY_DESTINATIONS));
            } else
                req.setDestinations(getLocations(locations, json.getString(KEY_DESTINATIONS), KEY_DESTINATIONS));
        } else
            req.setDestinations(locations);


        value = json.optString(KEY_METRICS);
        if (!Helper.isEmpty(value)) {
            int metrics = MatrixMetricsType.getFromString(value);

            if (metrics == MatrixMetricsType.UNKNOWN)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_METRICS);

            req.setMetrics(metrics);
        }

        if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.DISTANCE)) {
            value = json.optString(KEY_UNITS);
            if (!Helper.isEmpty(value)) {
                DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.UNKNOWN);

                if (units == DistanceUnit.UNKNOWN)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_UNITS);

                req.setUnits(units);
            }
        }

        value = json.optString(KEY_RESOLVE_LOCATIONS);
        if (!Helper.isEmpty(value)) {
            try {
                boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase(VAL_FALSE))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RESOLVE_LOCATIONS);
                req.setResolveLocations(b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RESOLVE_LOCATIONS);
            }
        }

        value = json.optString(KEY_OPTIMIZED);
        if (!Helper.isEmpty(value)) {
            try {
                boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase(VAL_FALSE))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_OPTIMIZED);
                req.setFlexibleMode(!b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_OPTIMIZED);
            }
        }

        value = json.optString("id");
        if (!Helper.isEmpty(value))
            req.setId(value);

        // MARQ24 - where is the 'algorithm' ?! [compare with parseFromRequestParams!]

        return req;
    }

    public static MatrixRequest parseFromRequestParams(HttpServletRequest request) throws Exception {
        MatrixRequest req = new MatrixRequest();

        String value = request.getParameter(KEY_PROFILE);
        if (!Helper.isEmpty(value)) {
            int profileType = RoutingProfileType.getFromString(value);
            if (profileType == RoutingProfileType.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_PROFILE, value);
            req.setProfileType(profileType);
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, KEY_PROFILE);
        }

        value = request.getParameter(KEY_PREFERENCE);
        if (!Helper.isEmpty(value)) {
            int weightingMethod = WeightingMethod.getFromString(value);
            if (weightingMethod == WeightingMethod.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_PREFERENCE, value);

            req.setWeightingMethod(weightingMethod);
        }

        Coordinate[] locations = null;
        value = request.getParameter(KEY_LOCATIONS);
        if (!Helper.isEmpty(value)) {
            try {
                locations = CoordTools.parse(value, "\\|", false, false);
                if (locations.length < 2)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_LOCATIONS);
            } catch (NumberFormatException nfex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_LOCATIONS);
            }
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, KEY_LOCATIONS);
        }

        req.setSources(getLocations(locations, request.getParameter(KEY_SOURCES), KEY_SOURCES));
        req.setDestinations(getLocations(locations, request.getParameter(KEY_DESTINATIONS), KEY_DESTINATIONS));

        value = request.getParameter(KEY_METRICS);
        if (!Helper.isEmpty(value)) {
            int metrics = MatrixMetricsType.getFromString(value);

            if (metrics == MatrixMetricsType.UNKNOWN)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_METRICS);

            req.setMetrics(metrics);
        }

        if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.DISTANCE)) {
            value = request.getParameter(KEY_UNITS);
            if (!Helper.isEmpty(value)) {
                DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.UNKNOWN);

                if (units == DistanceUnit.UNKNOWN)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, KEY_UNITS);

                req.setUnits(units);
            }
        }

        value = request.getParameter(KEY_RESOLVE_LOCATIONS);
        if (!Helper.isEmpty(value)) {
            try {
                boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase(VAL_FALSE))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RESOLVE_LOCATIONS);
                req.setResolveLocations(b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_RESOLVE_LOCATIONS);
            }
        }

        value = request.getParameter(KEY_OPTIMIZED);
        if (!Helper.isEmpty(value)) {
            try {
                boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase(VAL_FALSE))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_OPTIMIZED);
                req.setFlexibleMode(!b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, KEY_OPTIMIZED);
            }
        }

        value = request.getParameter("id");
        if (!Helper.isEmpty(value))
            req.setId(value);

        // REMOVE
        req.setAlgorithm(request.getParameter("algorithm"));

        return req;
    }

    private static Coordinate[] getLocations(Coordinate[] locations, String strIndex, String elemName) throws Exception {
        if (Helper.isEmpty(strIndex) || "all".equalsIgnoreCase(strIndex))
            return locations;

        int[] index = ArraysUtility.parseIntArray(strIndex, elemName, MatrixErrorCodes.INVALID_PARAMETER_FORMAT);

        return getLocations(locations, index, elemName);
    }

    private static Coordinate[] getLocations(Coordinate[] locations, int[] index, String elemName) throws Exception {
        Coordinate[] res = new Coordinate[index.length];
        for (int i = 0; i < index.length; i++) {
            int idx = index[i];
            if (idx < 0 || idx >= locations.length)
                throw new ParameterOutOfRangeException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, elemName, Integer.toString(idx), Integer.toString(locations.length - 1));

            res[i] = locations[idx];
        }

        return res;
    }
}
