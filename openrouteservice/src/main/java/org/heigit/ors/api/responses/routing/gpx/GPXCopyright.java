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

import org.heigit.ors.config.AppConfig;
import org.heigit.ors.config.RoutingServiceSettings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Calendar;

public class GPXCopyright {
    @XmlAttribute(name = "author")
    private final String author;
    @XmlElement(name = "year")
    private final int year;
    @XmlElement(name = "license")
    private final String license;

    public GPXCopyright() {
        this.author = RoutingServiceSettings.getAttribution();
        this.license = AppConfig.getGlobal().getParameter("info", "content_licence");
        this.year = Calendar.getInstance().get(Calendar.YEAR);
    }
}
