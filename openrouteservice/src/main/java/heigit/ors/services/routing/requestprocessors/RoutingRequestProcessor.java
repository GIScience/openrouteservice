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
package heigit.ors.services.routing.requestprocessors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.exceptions.EmptyElementException;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.services.routing.requestprocessors.gpx.GpxRoutingResponseWriter;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingRequestParser;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import io.jenetics.jpx.GPX;
import org.json.JSONObject;

import com.graphhopper.util.Helper;

import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;

public class RoutingRequestProcessor extends AbstractHttpRequestProcessor {

    public RoutingRequestProcessor(HttpServletRequest request) throws Exception {
        super(request);
    }

    @Override
    public void process(HttpServletResponse response) throws Exception {
        RoutingRequest rreq = JsonRoutingRequestParser.parseFromRequestParams(_request);

        RouteResult result = RoutingProfileManager.getInstance().computeRoute(rreq);

        JSONObject json = null;
        String gpx;

        String respFormat = _request.getParameter("format");
        if (Helper.isEmpty(respFormat) || "json".equalsIgnoreCase(respFormat)) {
            json = JsonRoutingResponseWriter.toJson(rreq, new RouteResult[]{result});
            if (json != null){
                ServletUtility.write(response, json, "UTF-8");

            }else{
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "JSON was empty and therefore could not be created.");
            }
        } else if ("geojson".equalsIgnoreCase(respFormat)) {
            json = JsonRoutingResponseWriter.toGeoJson(rreq, new RouteResult[]{result});
            if (json != null){
            ServletUtility.write(response, json, "UTF-8");}
            else{
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "GEOJSON was empty and therefore could not be created.");
            }
        } else if ("gpx".equalsIgnoreCase(respFormat)) {
            gpx = GpxRoutingResponseWriter.toGPX(new RouteResult[] {result});
            if (gpx != null) {
                ServletUtility.write(response, gpx);
            } else{
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "GPX was empty and therefore could not be created.");
            }
        }
        // TODO: Decide how to handle two different write servlets. E.g. integrate them seperately in the if loop above.

    }
}
