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

import heigit.ors.config.AppConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class GPXLink {
    @XmlAttribute(name = "href")
    private String href;
    @XmlElement(name = "text")
    private String text;
    @XmlElement
    private String type;

    public GPXLink() {
        this.href = AppConfig.Global().getParameter("info", "base_url");
        this.text = AppConfig.Global().getParameter("info", "base_url");
        this.type = "text/html";
    }

}
