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

package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.RouteResponse;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "gpx")
@ApiModel(value = "gpx")
public class GPXRouteResponse extends RouteResponse {

    @XmlAttribute(name = "version")
    private String gpxVersion = "1.0";

    @XmlAttribute(name = "creator")
    private String gpxCreator = "openrouteservice";

    @XmlElement(name = "metadata")
    private GPXMetadata metadata;

    @XmlElement(name = "rte")
    private List<GPXRouteElement> routes;

    @XmlElement(name = "extensions")
    private GPXExtensions extensions;

    public GPXRouteResponse() throws StatusCodeException {
        super(null);
        init(null, null);
    }

    public GPXRouteResponse(RouteResult[] routeResult, RouteRequest request) throws StatusCodeException {
        super(request);
        init(routeResult, request);
        for(RouteResult result : routeResult) {
            routes.add(new GPXRouteElement(result));
        }
    }

    private void init(RouteResult[] result, RouteRequest request) throws StatusCodeException {
        metadata = new GPXMetadata(result, request);
        routes = new ArrayList<>();
        extensions = new GPXExtensions(request);
    }

    public List<GPXRouteElement> getGpxRouteElements() {
        return routes;
    }
}
