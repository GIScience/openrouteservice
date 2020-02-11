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

import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.util.SystemMessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extensions")
public class GPXMetadataExtensions {
    @XmlElement(name = "system-message")
    private String systemMessage;

    public GPXMetadataExtensions() {}

    public GPXMetadataExtensions(RouteRequest request) {
        this.systemMessage = SystemMessage.getSystemMessage(request);
    }
}
