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

import jakarta.xml.bind.annotation.XmlElement;
import org.heigit.ors.exceptions.InternalServerException;

public class GPXAuthor {
    @XmlElement(name = "name")
    private final String name;

    @XmlElement(name = "email")
    private final GPXEmail email;

    @XmlElement(name = "link")
    private final GPXLink link;

    // For JaxB compatibility
    public GPXAuthor() throws InternalServerException {
        this(null, null, null);
    }

    public GPXAuthor(String name, String email, String baseUrl) throws InternalServerException {
        this.name = name;
        this.email = new GPXEmail(email);
        this.link = new GPXLink(baseUrl);
    }
}
