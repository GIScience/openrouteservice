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

import com.graphhopper.util.Helper;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.exceptions.EmptyElementException;


import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.requestprocessors.geojson.GeoJsonResponseWriter;
import heigit.ors.services.routing.requestprocessors.gpx.GpxRoutingResponseWriter;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.util.ServletUtility;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.geojson.GeoJSON;
import org.geotools.geojson.feature.FeatureHandler;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.JSONArray;
import org.json.JSONObject;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.geojson.GeometryJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;


public class RoutingRequestProcessor extends AbstractHttpRequestProcessor {

    public RoutingRequestProcessor(HttpServletRequest request) throws Exception {
        super(request);
    }

    @Override
    public void process(HttpServletResponse response) throws Exception {
        RoutingRequest rreq = RoutingRequestParser.parseFromRequestParams(_request);

        RouteResult result = RoutingProfileManager.getInstance().computeRoute(rreq);

        JSONObject json = null;
        String gpx;
        String geojson;
        String respFormat = _request.getParameter("format");
        if (Helper.isEmpty(respFormat) || "json".equalsIgnoreCase(respFormat)) {
            json = JsonRoutingResponseWriter.toJson(rreq, new RouteResult[]{result});
            if (json != null) {
                ServletUtility.write(response, json, "UTF-8");

            } else {
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "JSON was empty and therefore could not be created.");
            }
        } else if ("geojson".equalsIgnoreCase(respFormat)) {
            // If the response format is set to geojson the geometry_format parameter from the api call must be manually
            // set to geojson. Else the response will eventually be an encoded polyline
            String geometryFormat = _request.getParameter("geometry_format").toLowerCase();
            if (Helper.isEmpty(geometryFormat) || !geometryFormat.equals("geojson")) {
                rreq.setGeometryFormat("geojson");
            }
            // TODO create a json response first and parse it to the related global export class method and let this return the export to this method
            // TODO use the logics from FeatureParser and GeoJsonResponseWriter!!!
            geojson = GeoJsonResponseWriter.toGeoJson(rreq, new RouteResult[]{result});


        } else if ("gpx".equalsIgnoreCase(respFormat)) {
            // TODO integrate gpx when the global export is done
            gpx = GpxRoutingResponseWriter.toGPX(rreq, new RouteResult[]{result});
            if (gpx != null) {
                ServletUtility.write(response, gpx);
            } else {
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "GPX was empty and therefore could not be created.");
            }
        }

    }
}
