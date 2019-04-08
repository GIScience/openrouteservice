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

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.common.BoundingBox.BoundingBox;
import heigit.ors.api.responses.common.BoundingBox.BoundingBoxFactory;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.routing.RouteResult;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.GeomUtility;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "metadata")
public class GPXMetadata {
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "desc")
    private String description;
    @XmlElement(name = "author")
    private GPXAuthor author;
    @XmlElement(name = "copyright")
    private GPXCopyright copyright;
    @XmlElement(name = "time")
    private Date timeGenerated;
    @XmlElement(name = "bounds", type = GPXBounds.class)
    private BoundingBox bounds;

    public GPXMetadata() {}

    public GPXMetadata(RouteResult[] routeResults, RouteRequest request) throws StatusCodeException {
        this.name = RoutingServiceSettings.getRoutingName();
        this.description = "This is a directions instructions file as GPX, generated from openrouteservice";
        this.author = new GPXAuthor();
        this.copyright = new GPXCopyright();
        this.timeGenerated = new Date();

        BBox[] bboxes = new BBox[routeResults.length];
        for(int i=0; i<routeResults.length; i++) {
            bboxes[i] = routeResults[i].getSummary().getBBox();
        }

        this.bounds = BoundingBoxFactory.constructBoundingBox(GeomUtility.generateBoundingFromMultiple(bboxes), request);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public GPXAuthor getAuthor() {
        return author;
    }

    public GPXCopyright getCopyright() {
        return copyright;
    }

    public Date getTimeGenerated() {
        return timeGenerated;
    }

    public BoundingBox getBounds() {
        return bounds;
    }
}
