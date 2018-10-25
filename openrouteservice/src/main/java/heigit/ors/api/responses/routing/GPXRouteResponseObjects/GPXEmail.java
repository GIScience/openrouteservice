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
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.routing.RoutingErrorCodes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "email")
public class GPXEmail {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "domain")
    private String domain;

    public GPXEmail() throws InternalServerException {
        String email = AppConfig.Global().getParameter("info", "support_mail");
        try {
            String[] parts = email.split("@");

            if (parts.length == 2) {
                id = parts[0];
                domain = parts[1];
            }
        } catch (Exception e) {
            throw new InternalServerException(RoutingErrorCodes.UNKNOWN, "Error creating GPX Email attribute, has it been set in the app.config?");
        }
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }
}