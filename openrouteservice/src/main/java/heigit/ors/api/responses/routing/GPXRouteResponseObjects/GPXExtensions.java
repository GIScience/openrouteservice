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

import javax.xml.bind.annotation.XmlElement;

public class GPXExtensions {
    @XmlElement(name = "attribution")
    private String attribution;
    @XmlElement(name = "engine")
    private String engine;
    @XmlElement(name = "build_date")
    private String buildDate;
    @XmlElement(name = "profile")
    private String profile;
    @XmlElement(name="preference")
    private String preference;
    @XmlElement(name = "language")
    private String language;
    @XmlElement(name = "distance-units")
    private String units;
    @XmlElement(name = "instructions")
    private boolean includeInstructions;
    @XmlElement(name = "elevation")
    private boolean includeElevation;

    public GPXExtensions() {}

    public GPXExtensions(RouteRequest request) {

    }
}
