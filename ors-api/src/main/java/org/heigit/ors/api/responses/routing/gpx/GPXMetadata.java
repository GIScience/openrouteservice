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

import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.common.boundingbox.BoundingBoxFactory;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.util.GeomUtility;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
    @XmlElement(name = "extensions")
    private GPXMetadataExtensions extensions;

    public GPXMetadata() {
    }

    public GPXMetadata(RouteResult[] routeResults, RouteRequest request, SystemMessageProperties systemMessageProperties, EndpointsProperties endpointsProperties) throws StatusCodeException {
        EndpointsProperties.EndpointRoutingProperties props = endpointsProperties.getRouting();
        this.name = props.getGpxName();
        this.description = props.getGpxDescription();
        this.author = new GPXAuthor(props.getGpxAuthor(), props.getGpxSupportMail(), props.getGpxBaseUrl());
        this.copyright = new GPXCopyright(props.getGpxContentLicence(), endpointsProperties.getRouting().getAttribution());
        this.timeGenerated = new Date();

        BBox[] bboxes = new BBox[routeResults.length];
        for (int i = 0; i < routeResults.length; i++) {
            bboxes[i] = routeResults[i].getSummary().getBBox();
        }

        this.bounds = BoundingBoxFactory.constructBoundingBox(GeomUtility.generateBoundingFromMultiple(bboxes), request);

        this.extensions = new GPXMetadataExtensions(request, systemMessageProperties);
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

    public GPXMetadataExtensions getExtensions() {
        return extensions;
    }
}
