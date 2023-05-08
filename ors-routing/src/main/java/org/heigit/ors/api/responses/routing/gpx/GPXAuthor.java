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
import org.heigit.ors.exceptions.InternalServerException;

import javax.xml.bind.annotation.XmlElement;

public class GPXAuthor {
    @XmlElement(name = "name")
    private final String name;

    @XmlElement(name = "email")
    private final GPXEmail email;

    @XmlElement(name = "link")
    private final GPXLink link;

    public GPXAuthor() throws InternalServerException  {
        this.name = AppConfig.getGlobal().getParameter("info", "author_tag");
        this.email = new GPXEmail();
        this.link = new GPXLink();
    }
}
