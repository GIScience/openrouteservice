/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.api.responses.routing.gpx;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.routing.RouteResponse;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteResult;

import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "gpx")
@Schema(name = "gpx")
public class GPXRouteResponse extends RouteResponse {

    @XmlAttribute(name = "version")
    private static final String GPX_VERSION = "1.0";

    @XmlAttribute(name = "creator")
    private static final String GPX_CREATOR = "openrouteservice";

    @XmlAttribute(name = "xmlns")
    private static final String XMLNS_LINK = "https://raw.githubusercontent.com/GIScience/openrouteservice-schema/main/gpx/v2/ors-gpx.xsd";

    @XmlElement(name = "metadata")
    private GPXMetadata metadata;

    @XmlElement(name = "rte")
    private List<GPXRouteElement> routes;

    @XmlElement(name = "extensions")
    private GPXExtensions extensions;

    public GPXRouteResponse() throws StatusCodeException {
        super(null, new SystemMessageProperties(), new EndpointsProperties());
        init(null, null, new SystemMessageProperties(), new EndpointsProperties());
    }

    public GPXRouteResponse(RouteResult[] routeResult, RouteRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) throws StatusCodeException {
        super(request, systemMessageProperties, endpointsProperties);
        init(routeResult, request, systemMessageProperties, endpointsProperties);
        for (RouteResult result : routeResult) {
            routes.add(new GPXRouteElement(result));
        }
    }

    private void init(RouteResult[] result, RouteRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) throws StatusCodeException {
        metadata = new GPXMetadata(result, request, systemMessageProperties, endpointsProperties);
        routes = new ArrayList<>();
        extensions = new GPXExtensions(request, endpointsProperties.getRouting().getAttribution());
    }

    public List<GPXRouteElement> getGpxRouteElements() {
        return routes;
    }
}
