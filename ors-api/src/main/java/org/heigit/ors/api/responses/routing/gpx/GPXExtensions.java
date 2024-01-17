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

import com.graphhopper.util.Helper;
import jakarta.xml.bind.annotation.XmlElement;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.api.util.AppInfo;
import org.heigit.ors.api.APIEnums;

public class GPXExtensions {
    @XmlElement(name = "attribution")
    private String attribution;
    @XmlElement(name = "engine")
    private String engine;
    @XmlElement(name = "build_date")
    private String buildDate;
    @XmlElement(name = "profile")
    private String profile;
    @XmlElement(name = "preference")
    private String preference;
    @XmlElement(name = "language")
    private String language;
    @XmlElement(name = "distance-units")
    private String units;
    @XmlElement(name = "instructions")
    private boolean includeInstructions;
    @XmlElement(name = "elevation")
    private boolean includeElevation;

    public GPXExtensions() {
    }

    public GPXExtensions(RouteRequest request, String attribution) {
        if (!Helper.isEmpty(attribution))
            this.attribution = attribution;

        engine = AppInfo.getEngineInfo().getString("version");
        buildDate = AppInfo.getEngineInfo().getString("build_date");
        profile = request.getProfile().toString();
        if (request.hasRoutePreference())
            preference = request.getRoutePreference().toString();
        if (request.hasLanguage())
            language = request.getLanguage().toString();
        else
            language = APIEnums.Languages.EN.toString();

        if (request.hasUnits())
            units = request.getUnits().toString();
        else
            units = APIEnums.Units.METRES.toString();

        if (request.hasIncludeInstructions())
            includeInstructions = request.getIncludeInstructionsInResponse();
        else
            includeInstructions = true;

        if (request.hasUseElevation())
            includeElevation = request.getUseElevation();
        else
            includeElevation = false;
    }
}
