/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.services.matrix.requestprocessors.json;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.util.ArraysUtility;
import heigit.ors.util.CoordTools;
import heigit.ors.util.DistanceUnitUtil;
import heigit.ors.util.JsonUtility;
import heigit.ors.util.StreamUtility;

public class JsonMatrixRequestParser {

    public static MatrixRequest parseFromStream(InputStream stream) throws Exception {
        String body = StreamUtility.readStream(stream);

        if (Helper.isEmpty(body))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document.");

        JSONObject json = null;

        try {
            json = new JSONObject(body);
        } catch (Exception ex) {
            throw new StatusCodeException(StatusCode.BAD_REQUEST, MatrixErrorCodes.INVALID_JSON_FORMAT, "Unable to parse JSON document." + ex.getMessage());
        }

        MatrixRequest req = new MatrixRequest();

        String value = json.optString("profile");

        if (!Helper.isEmpty(value)) {
            int profileType = RoutingProfileType.getFromString(value);
            if (profileType == RoutingProfileType.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
            req.setProfileType(profileType);
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
        }

        // MARQ24 WHERE the heck are the 'preferences here???
        // -> so I add them!
        value = json.optString("preference");
        if (!Helper.isEmpty(value)) {
            int weightingMethod = WeightingMethod.getFromString(value);
            if (weightingMethod == WeightingMethod.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);
            req.setWeightingMethod(value);
        }


        JSONArray jLocations = json.optJSONArray("locations");
        Coordinate[] locations = null;

        if (jLocations != null) {
            try {
                int nLocations = jLocations.length();
                if (nLocations < 2)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "locations");

                locations = new Coordinate[nLocations];

                for (int i = 0; i < nLocations; i++) {
                    JSONArray jCoordinate = jLocations.getJSONArray(i);

                    if (jCoordinate.length() < 2)
                        throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "locations");

                    locations[i] = new Coordinate(jCoordinate.getDouble(0), jCoordinate.getDouble(1));
                }
            } catch (NumberFormatException nfex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
            } catch (JSONException jex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
            }
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "locations");
        }

        if (json.has("sources")) {
            JSONArray jSources = json.optJSONArray("sources");

            if (jSources != null) {
                int[] index = JsonUtility.parseIntArray(jSources, "sources", MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
                req.setSources(getLocations(locations, index, "sources"));
            } else
                req.setSources(getLocations(locations, json.getString("sources"), "sources"));
        } else
            req.setSources(locations);

        if (json.has("destinations")) {
            JSONArray jDestinations = json.optJSONArray("destinations");

            if (jDestinations != null) {
                int[] index = JsonUtility.parseIntArray(jDestinations, "destinations", MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
                req.setDestinations(getLocations(locations, index, "destinations"));
            } else
                req.setDestinations(getLocations(locations, json.getString("destinations"), "destinations"));
        } else
            req.setDestinations(locations);


        value = json.optString("metrics");
        if (!Helper.isEmpty(value)) {
            int metrics = MatrixMetricsType.getFromString(value);

            if (metrics == MatrixMetricsType.Unknown)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "metrics");

            req.setMetrics(metrics);
        }

        if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.Distance)) {
            value = json.optString("units");
            if (!Helper.isEmpty(value)) {
                DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);

                if (units == DistanceUnit.Unknown)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "units");

                req.setUnits(units);
            }
        }

        value = json.optString("resolve_locations");
        if (!Helper.isEmpty(value)) {
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false"))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
                req.setResolveLocations(b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
            }
        }

        value = json.optString("optimized");
        if (!Helper.isEmpty(value)) {
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false"))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
                req.setFlexibleMode(!b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
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

        String value = request.getParameter("profile");
        if (!Helper.isEmpty(value)) {
            int profileType = RoutingProfileType.getFromString(value);
            if (profileType == RoutingProfileType.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
            req.setProfileType(profileType);
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
        }

        value = request.getParameter("preference");
        if (!Helper.isEmpty(value)) {
            int weightingMethod = WeightingMethod.getFromString(value);
            if (weightingMethod == WeightingMethod.UNKNOWN)
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);

            req.setWeightingMethod(value);
        }

        Coordinate[] locations = null;
        value = request.getParameter("locations");
        if (!Helper.isEmpty(value)) {
            try {
                locations = CoordTools.parse(value, "\\|", false, false);
                if (locations.length < 2)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "locations");
            } catch (NumberFormatException nfex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
            }
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "locations");
        }

        req.setSources(getLocations(locations, request.getParameter("sources"), "sources"));
        req.setDestinations(getLocations(locations, request.getParameter("destinations"), "destinations"));

        value = request.getParameter("metrics");
        if (!Helper.isEmpty(value)) {
            int metrics = MatrixMetricsType.getFromString(value);

            if (metrics == MatrixMetricsType.Unknown)
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "metrics");

            req.setMetrics(metrics);
        }

        if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.Distance)) {
            value = request.getParameter("units");
            if (!Helper.isEmpty(value)) {
                DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);

                if (units == DistanceUnit.Unknown)
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "units");

                req.setUnits(units);
            }
        }

        value = request.getParameter("resolve_locations");
        if (!Helper.isEmpty(value)) {
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false"))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
                req.setResolveLocations(b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
            }
        }

        value = request.getParameter("optimized");
        if (!Helper.isEmpty(value)) {
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false"))
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
                req.setFlexibleMode(!b);
            } catch (Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
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

    /*
    public static MatrixRequest parseFromRequestParams(AbstractHttpRequestProcessor.MixedRequestParameters reqParams) throws Exception {
        MatrixRequest req = new MatrixRequest();

        String value = reqParams.getString("profile");
        if (!Helper.isEmpty(value)) {
            int profileType = RoutingProfileType.getFromString(value);
            if (profileType == RoutingProfileType.UNKNOWN) {
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "profile", value);
            }
            req.setProfileType(profileType);
        } else {
            throw new MissingParameterException(MatrixErrorCodes.MISSING_PARAMETER, "profile");
        }

        Coordinate[] locations = reqParams.getLocations("locations");
        req.setSources(reqParams.getSubLocations(locations, "sources"));
        req.setDestinations(reqParams.getSubLocations(locations, "destinations"));

        value = reqParams.getString("preference");
        if (!Helper.isEmpty(value)) {
            int weightingMethod = WeightingMethod.getFromString(value);
            if (weightingMethod == WeightingMethod.UNKNOWN) {
                throw new UnknownParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "preference", value);
            }
            req.setWeightingMethod(value);
        }

        value = reqParams.getString("metrics");
        if (!Helper.isEmpty(value)) {
            int metrics =  MatrixMetricsType.getFromString(value);
            if (metrics == MatrixMetricsType.Unknown) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "metrics");
            }
            req.setMetrics(metrics);
        }

        if (MatrixMetricsType.isSet(req.getMetrics(), MatrixMetricsType.Distance)) {
            value = reqParams.getString("units");
            if (!Helper.isEmpty(value)) {
                DistanceUnit units = DistanceUnitUtil.getFromString(value, DistanceUnit.Unknown);
                if (units == DistanceUnit.Unknown) {
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_VALUE, "units");
                }
                req.setUnits(units);
            }
        }

        value = reqParams.getString("resolve_locations");
        if (!Helper.isEmpty(value)){
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false")) {
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
                }
                req.setResolveLocations(b);
            } catch(Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "resolve_locations");
            }
        }

        value = reqParams.getString("optimized");
        if (!Helper.isEmpty(value)) {
            try {
                Boolean b = Boolean.parseBoolean(value);
                if (!b && !value.equalsIgnoreCase("false")) {
                    throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
                }
                req.setFlexibleMode(!b);
            } catch(Exception ex) {
                throw new ParameterValueException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, "optimized");
            }
        }

        value = reqParams.getString("id");
        if (!Helper.isEmpty(value)) {
            req.setId(value);
        }

        // REMOVE
        req.setAlgorithm(reqParams.getString("algorithm"));

        return req;
    }
    */

}
