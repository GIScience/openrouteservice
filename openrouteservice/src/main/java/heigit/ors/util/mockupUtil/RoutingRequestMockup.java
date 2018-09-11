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

package heigit.ors.util.mockupUtil;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.common.DistanceUnit;
import heigit.ors.routing.RouteInstructionsFormat;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.routing.pathprocessors.BordersExtractor;

/**
 * This is a {@link heigit.ors.routing.RoutingRequest} Mockup-Class, used in junit tests and wherever needed.
 * The mockup is intended as generic as possible and is very flexible. Make sure you check the element out and understand it before integration!
 * The enum routeProfile is intended as a variable that makes the creation of new and personal mockups really easy and flexible.
 * For a new mockup routeProfile add it to the enum first and than to the create function as a new "else if()" choice.
 * Example call: "RoutingRequest routingRequestMockup = new RoutingRequestMockup().create(RoutingRequestMockup.routeProfile.standard2d)"
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingRequestMockup {

    private static RoutingRequest routingRequestMockup;


    // For now only two profiles are integrated. Both referring to a Heidelberg graph!
    public enum routeProfile {
        standardHeidelberg2d, standardHeidelberg3d
    }

    private enum searchParamProfile {
        standardCarSearchProfile
    }

    private Coordinate[] coords2d = new Coordinate[3];
    private Coordinate[] coords3d = new Coordinate[3];

    public RoutingRequestMockup() {
        routingRequestMockup = new RoutingRequest();

    }

    /**
     * This functions purpose is to fully initialize the RouteRequestMockup with standardized variables.
     * The coordinates use Heidelberg as a reference and are two-dimensional!!! so if Heidelberg isn't present in the Graph you need to adjust the coordinates.
     * Idea: generalize the choosing of the coordinates at one point.
     *
     * @return @{@link RoutingRequest} is being returned for mockup use.
     */
    public RoutingRequest create(routeProfile profile) throws Exception {

        if (profile == RoutingRequestMockup.routeProfile.standardHeidelberg2d) {
            routingRequestMockup.setGeometryFormat("geojson");
            // Fill the two-dimensional coordinate
            coords2d[0] = new Coordinate(Double.parseDouble("8.690614"), Double.parseDouble("49.38365"));
            coords2d[1] = new Coordinate(Double.parseDouble("8.7007"), Double.parseDouble("49.411699"));
            coords2d[2] = new Coordinate(Double.parseDouble("8.7107"), Double.parseDouble("49.4516"));
            routingRequestMockup.setCoordinates(coords2d);
            routingRequestMockup.setIncludeElevation(false);
            routingRequestMockup.setIncludeGeometry(true);
            routingRequestMockup.setAttributes(null);
            routingRequestMockup.setContinueStraight(false);
            routingRequestMockup.setExtraInfo(0);
            routingRequestMockup.setIncludeInstructions(true);
            routingRequestMockup.setIncludeManeuvers(false);
            routingRequestMockup.setIncludeRoundaboutExits(false);
            routingRequestMockup.setInstructionsFormat(RouteInstructionsFormat.TEXT);
            routingRequestMockup.setLanguage("DE");
            routingRequestMockup.setLocationIndex(-1);
            routingRequestMockup.setUnits(DistanceUnit.Meters);
            // the search parameters are only accessible through local access.
            setRouteSearchParameters(searchParamProfile.standardCarSearchProfile);
            routingRequestMockup.setId(null);
            return routingRequestMockup;
        } else if (profile == RoutingRequestMockup.routeProfile.standardHeidelberg3d) {
            routingRequestMockup.setGeometryFormat("geojson");
            // Fill the three-dimensional coordinate
            // TODO fill with third coordinate!!!
            coords3d[0] = new Coordinate(Double.parseDouble("8.690614"), Double.parseDouble("49.38365"), Double.parseDouble("NaN"));
            coords3d[1] = new Coordinate(Double.parseDouble("8.7007"), Double.parseDouble("49.411699"), Double.parseDouble("NaN"));
            coords3d[2] = new Coordinate(Double.parseDouble("8.7107"), Double.parseDouble("49.4516"), Double.parseDouble("NaN"));
            routingRequestMockup.setCoordinates(coords3d);
            routingRequestMockup.setIncludeElevation(false);
            routingRequestMockup.setIncludeGeometry(true);
            routingRequestMockup.setAttributes(null);
            routingRequestMockup.setContinueStraight(false);
            routingRequestMockup.setExtraInfo(0);
            routingRequestMockup.setIncludeInstructions(true);
            routingRequestMockup.setIncludeManeuvers(false);
            routingRequestMockup.setIncludeRoundaboutExits(false);
            routingRequestMockup.setInstructionsFormat(RouteInstructionsFormat.TEXT);
            routingRequestMockup.setLanguage("DE");
            routingRequestMockup.setLocationIndex(-1);
            routingRequestMockup.setUnits(DistanceUnit.Meters);
            // the search parameters are only accessible through local access.
            setRouteSearchParameters(searchParamProfile.standardCarSearchProfile);
            routingRequestMockup.setId(null);
            return routingRequestMockup;
        }
        return null;

    }

    /**
     * This function initializes the {@link heigit.ors.routing.RouteSearchParameters} from the {@link RoutingRequest} with standard variables from self-designed profiles.
     */
    private void setRouteSearchParameters(searchParamProfile profile) throws Exception {
        if (profile == searchParamProfile.standardCarSearchProfile) {
            routingRequestMockup.getSearchParameters().setAvoidAreas(null);
            routingRequestMockup.getSearchParameters().setAvoidBorders(BordersExtractor.Avoid.NONE);
            routingRequestMockup.getSearchParameters().setAvoidCountries(null);
            routingRequestMockup.getSearchParameters().setAvoidFeatureTypes(0);
            routingRequestMockup.getSearchParameters().setBearings(null);
            routingRequestMockup.getSearchParameters().setConsiderTraffic(false);
            routingRequestMockup.getSearchParameters().setConsiderTurnRestrictions(false);
            routingRequestMockup.getSearchParameters().setFlexibleMode(false);
            routingRequestMockup.getSearchParameters().setMaximumRadiuses(null);
            routingRequestMockup.getSearchParameters().setMaximumSpeed(-1.0);
            routingRequestMockup.getSearchParameters().setOptions(null);
            routingRequestMockup.getSearchParameters().setProfileType(1);
            routingRequestMockup.getSearchParameters().setVehicleType(0);
            routingRequestMockup.getSearchParameters().setWeightingMethod(1);
        }
    }
}