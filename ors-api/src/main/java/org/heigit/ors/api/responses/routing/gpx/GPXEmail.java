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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.routing.RoutingErrorCodes;

@XmlRootElement(name = "email")
public class GPXEmail {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "domain")
    private String domain;

    // For JaxB compatibility
    public GPXEmail() throws InternalServerException {
        this("");
    }

    public GPXEmail(String email) throws InternalServerException {
        try {
            String[] parts = email.split("@");

            if (parts.length == 2) {
                id = parts[0];
                domain = parts[1];
            }
        } catch (Exception e) {
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Error creating GPX Email attribute, has it been set in the ors-config.json?");
        }
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }
}
