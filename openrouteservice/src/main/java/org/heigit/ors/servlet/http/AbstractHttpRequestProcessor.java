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
package heigit.ors.servlet.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.StatusCode;
import heigit.ors.exceptions.*;
import heigit.ors.matrix.MatrixErrorCodes;
import heigit.ors.util.ArraysUtility;
import heigit.ors.util.CoordTools;
import heigit.ors.util.JsonUtility;
import heigit.ors.util.StreamUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpRequestProcessor implements HttpRequestProcessor {
    protected static Logger logger = LoggerFactory.getLogger(AbstractHttpRequestProcessor.class);

    protected HttpServletRequest _request;

    /*
    public abstract int getMethodNotSupportedErrorCode();

    public abstract int getInvalidJsonFormatErrorCode();

    public abstract int getInvalidParameterFormatErrorCode();

    public abstract int getMissingParameterErrorCode();

    public class MixedRequestParameters {
        private JSONObject jMap;
        private HttpServletRequest requestMap;

        public MixedRequestParameters(JSONObject jsonData) {
            this.jMap = jsonData;
        }

        public MixedRequestParameters(HttpServletRequest rMap) {
            this.requestMap = rMap;
        }

        public String getString(String key) {
            if (jMap != null && jMap.has(key)) {
                return jMap.getString(key);
            } else if (requestMap != null) {
                return requestMap.getParameter(key);
            }
            return null;
        }

        public Coordinate[] getSubLocations(Coordinate[] allLocations, String locationIndexKey) throws Exception {
            if (jMap != null && jMap.has(locationIndexKey)) {
                JSONArray jSources = jMap.optJSONArray(locationIndexKey);
                if (jSources != null) {
                    return getLocationsAtIndex(allLocations, JsonUtility.parseIntArray(jSources, locationIndexKey, getInvalidParameterFormatErrorCode()), locationIndexKey);
                } else {
                    return getLocationsAtIndex(allLocations, jMap.getString(locationIndexKey), locationIndexKey);
                }
            } else if (requestMap != null) {
                return getLocationsAtIndex(allLocations, requestMap.getParameter(locationIndexKey), locationIndexKey);
            }
            return null;
        }

        private Coordinate[] getLocationsAtIndex(Coordinate[] locations, String strIndex, String elemName) throws Exception {
            if (Helper.isEmpty(strIndex) || "all".equalsIgnoreCase(strIndex)) {
                return locations;
            }
            int[] index = ArraysUtility.parseIntArray(strIndex, elemName, MatrixErrorCodes.INVALID_PARAMETER_FORMAT);
            return getLocationsAtIndex(locations, index, elemName);
        }

        private Coordinate[] getLocationsAtIndex(Coordinate[] locations, int[] index, String elemName) throws Exception {
            Coordinate[] res = new Coordinate[index.length];
            for (int i = 0; i < index.length; i++) {
                int idx = index[i];
                if (idx < 0 || idx >= locations.length) {
                    throw new ParameterOutOfRangeException(MatrixErrorCodes.INVALID_PARAMETER_FORMAT, elemName, Integer.toString(idx), Integer.toString(locations.length - 1));
                }
                res[i] = locations[idx];
            }
            return res;
        }

        public Coordinate[] getLocations(String key) throws StatusCodeException {
            Coordinate[] locations = null;
            if (jMap != null && jMap.has(key)) {
                JSONArray jLocations = jMap.optJSONArray(key);
                if (jLocations != null) {
                    try {
                        int nLocations = jLocations.length();
                        if (nLocations < 2) {
                            throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                        }
                        locations = new Coordinate[nLocations];
                        for (int i = 0; i < nLocations; i++) {
                            JSONArray jCoordinate = jLocations.getJSONArray(i);
                            if (jCoordinate.length() < 2) {
                                throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                            }
                            locations[i] = new Coordinate(jCoordinate.getDouble(0), jCoordinate.getDouble(1));
                        }
                    } catch (NumberFormatException nfex) {
                        throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                    } catch (JSONException jex) {
                        throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                    }
                } else {
                    throw new MissingParameterException(getMissingParameterErrorCode(), key);
                }
            } else if (requestMap != null) {
                String value = requestMap.getParameter(key);
                if (!Helper.isEmpty(value)) {
                    try {
                        locations = CoordTools.parse(value, "\\|", false, false);
                        if (locations.length < 2) {
                            throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                        }
                    } catch (NumberFormatException nfex) {
                        throw new ParameterValueException(getInvalidParameterFormatErrorCode(), key);
                    }
                } else {
                    throw new MissingParameterException(getMissingParameterErrorCode(), key);
                }
            }

            return locations;
        }
    }

    public void process(HttpServletResponse response) throws Exception {
        switch (_request.getMethod()) {
            case "GET":
                process(new MixedRequestParameters(_request), response);
                break;

            case "POST":
                String body = StreamUtility.readStream(_request.getInputStream());
                if (Helper.isEmpty(body)) {
                    throw new StatusCodeException(StatusCode.BAD_REQUEST, getInvalidJsonFormatErrorCode(), "Unable to parse JSON document.");
                }
                JSONObject json = null;
                try {
                    json = new JSONObject(body);
                } catch (Exception ex) {
                    throw new StatusCodeException(StatusCode.BAD_REQUEST, getInvalidJsonFormatErrorCode(), "Unable to parse JSON document." + ex.getMessage());
                }
                if (json != null) {
                    process(new MixedRequestParameters(json), response);
                }
                break;

            default:
                int errorCode = getMethodNotSupportedErrorCode();
                if (errorCode != -1) {
                    throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED, errorCode);
                } else {
                    throw new StatusCodeException(StatusCode.METHOD_NOT_ALLOWED);
                }
        }
    }

    public abstract void process(MixedRequestParameters parameterMap, HttpServletResponse response) throws Exception;
    */
    public abstract void process(HttpServletResponse response) throws Exception;

    public AbstractHttpRequestProcessor(HttpServletRequest request) throws Exception {
        if (request == null)
            throw new InternalServerException();

        _request = request;
    }

    public void destroy() {

    }
}
