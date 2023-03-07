/*
 *
 *  *
 *  *  *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *  *  *
 *  *  *   http://www.giscience.uni-hd.de
 *  *  *   http://www.heigit.org
 *  *  *
 *  *  *  under one or more contributor license agreements. See the NOTICE file
 *  *  *  distributed with this work for additional information regarding copyright
 *  *  *  ownership. The GIScience licenses this file to you under the Apache License,
 *  *  *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  *  *  with the License. You may obtain a copy of the License at
 *  *  *
 *  *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  *  Unless required by applicable law or agreed to in writing, software
 *  *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  See the License for the specific language governing permissions and
 *  *  *  limitations under the License.
 *  *
 *
 */

package org.heigit.ors.util.mockuputil;

import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.routing.RouteInstructionsFormat;
import org.heigit.ors.routing.RoutingRequest;
import org.heigit.ors.routing.pathprocessors.BordersExtractor;

/**
 * This is a {@link org.heigit.ors.routing.RoutingRequest} Mockup-Class, used in junit tests and wherever needed.
 * The mockup is intended as generic as possible and is very flexible. Make sure you check the element out and understand it before integration!
 * The enum routeProfile is intended as a variable that makes the creation of new and personal mockups really easy and flexible.
 * For a new mockup routeProfile add it to the enum first and than to the create function as a new "else if()" choice.
 * Example call: "RoutingRequest routingRequestMockup = new RoutingRequestMockup().create(RoutingRequestMockup.routeProfile.standard2d)"
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingRequestMockup {

    private final RoutingRequest routingRequest;


    // For now only two profiles are integrated. Both referring to a Heidelberg graph!
    public enum routeProfile {
        STANDARD_HEIDELBERG_2D, STANDARD_HEIDELBERG_3D
    }

    private enum searchParamProfile {
        STANDARD_CAR_SEARCH_PROFILE
    }

    private final Coordinate[] coords2d = new Coordinate[3];
    private final Coordinate[] coords3d = new Coordinate[3];

    public RoutingRequestMockup() {
        routingRequest = new RoutingRequest();
    }

    /**
     * This functions purpose is to fully initialize the RouteRequestMockup with standardized variables.
     * The coordinates use Heidelberg as a reference and are two-dimensional!!! so if Heidelberg isn't present in the Graph you need to adjust the coordinates.
     * Idea: generalize the choosing of the coordinates at one point.
     *
     * @return @{@link RoutingRequest} is being returned for mockup use.
     */
    public RoutingRequest create(routeProfile profile) throws Exception {

        if (profile == RoutingRequestMockup.routeProfile.STANDARD_HEIDELBERG_2D) {
            routingRequest.setGeometryFormat("geojson");
            // Fill the two-dimensional coordinate
            coords2d[0] = new Coordinate(Double.parseDouble("8.690614"), Double.parseDouble("49.38365"));
            coords2d[1] = new Coordinate(Double.parseDouble("8.7007"), Double.parseDouble("49.411699"));
            coords2d[2] = new Coordinate(Double.parseDouble("8.7107"), Double.parseDouble("49.4516"));
            routingRequest.setCoordinates(coords2d);
            routingRequest.setIncludeElevation(false);
            routingRequest.setIncludeGeometry(true);
            routingRequest.setAttributes(null);
            routingRequest.setContinueStraight(false);
            routingRequest.setExtraInfo(0);
            routingRequest.setIncludeInstructions(true);
            routingRequest.setIncludeManeuvers(false);
            routingRequest.setIncludeRoundaboutExits(false);
            routingRequest.setInstructionsFormat(RouteInstructionsFormat.TEXT);
            routingRequest.setLanguage("DE");
            routingRequest.setLocationIndex(-1);
            routingRequest.setUnits(DistanceUnit.METERS);
            // the search parameters are only accessible through local access.
            setRouteSearchParameters(searchParamProfile.STANDARD_CAR_SEARCH_PROFILE);
            routingRequest.setId(null);
            return routingRequest;
        } else if (profile == RoutingRequestMockup.routeProfile.STANDARD_HEIDELBERG_3D) {
            routingRequest.setGeometryFormat("geojson");
            // Fill the three-dimensional coordinate
            // TODO fill with third coordinate!!!
            coords3d[0] = new Coordinate(Double.parseDouble("8.690614"), Double.parseDouble("49.38365"), Double.parseDouble("NaN"));
            coords3d[1] = new Coordinate(Double.parseDouble("8.7007"), Double.parseDouble("49.411699"), Double.parseDouble("NaN"));
            coords3d[2] = new Coordinate(Double.parseDouble("8.7107"), Double.parseDouble("49.4516"), Double.parseDouble("NaN"));
            routingRequest.setCoordinates(coords3d);
            routingRequest.setIncludeElevation(false);
            routingRequest.setIncludeGeometry(true);
            routingRequest.setAttributes(null);
            routingRequest.setContinueStraight(false);
            routingRequest.setExtraInfo(0);
            routingRequest.setIncludeInstructions(true);
            routingRequest.setIncludeManeuvers(false);
            routingRequest.setIncludeRoundaboutExits(false);
            routingRequest.setInstructionsFormat(RouteInstructionsFormat.TEXT);
            routingRequest.setLanguage("DE");
            routingRequest.setLocationIndex(-1);
            routingRequest.setUnits(DistanceUnit.METERS);
            // the search parameters are only accessible through local access.
            setRouteSearchParameters(searchParamProfile.STANDARD_CAR_SEARCH_PROFILE);
            routingRequest.setId(null);
            return routingRequest;
        }
        return null;

    }

    /**
     * This function initializes the {@link org.heigit.ors.routing.RouteSearchParameters} from the {@link RoutingRequest} with standard variables from self-designed profiles.
     */
    private void setRouteSearchParameters(searchParamProfile profile) throws Exception {
        if (profile == searchParamProfile.STANDARD_CAR_SEARCH_PROFILE) {
            routingRequest.getSearchParameters().setAvoidAreas(null);
            routingRequest.getSearchParameters().setAvoidBorders(BordersExtractor.Avoid.NONE);
            routingRequest.getSearchParameters().setAvoidCountries(null);
            routingRequest.getSearchParameters().setAvoidFeatureTypes(0);
            routingRequest.getSearchParameters().setBearings(null);
            routingRequest.getSearchParameters().setConsiderTurnRestrictions(false);
            routingRequest.getSearchParameters().setFlexibleMode(false);
            routingRequest.getSearchParameters().setMaximumRadiuses(null);
            routingRequest.getSearchParameters().setOptions(null);
            routingRequest.getSearchParameters().setProfileType(1);
            routingRequest.getSearchParameters().setVehicleType(0);
            routingRequest.getSearchParameters().setWeightingMethod(1);
        }
    }
}