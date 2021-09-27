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

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RouteSegment;
import org.heigit.ors.routing.RouteStep;

import java.math.BigDecimal;

/**
 * This is a {@link org.heigit.ors.routing.RouteResult} Mockup-Class, used in junit tests and wherever needed.
 * The mockup is intended as generic as possible. Make sure you check the element out using debug before integrating it.
 * For now the class only supports single routes!
 * <p>
 * !!!There is actually no other way to efficiently create a {@link RouteResult} mockup in another way!!!
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RouteResultMockup {

    // The enum holds the possible profiles
    public enum routeResultProfile {
        STANDARD_HEIDELBERG
    }


    /**
     * The function creates a {@link RouteResult} mockup with standardized values.
     * Add profiles as needed.
     *
     * @param profile Input must be an enum from routeResultProfile.* E.g. routeResultProfile.standardHeidelberg
     * @return Return value is a standardized and profile depending {@link RouteResult}.
     * @throws Exception An Exception is raised if something goes wrong.
     */
    public static RouteResult[] create(routeResultProfile profile) throws Exception {
        RouteResult routeResult = new RouteResult(0);
        if (profile == routeResultProfile.STANDARD_HEIDELBERG) {
            BBox bbox = new BBox(8.690603626917166, 8.690675487235653, 49.38366164068056, 49.38376283758349);
            routeResult.getSummary().setAscent(0.0);
            routeResult.getSummary().setAverageSpeed(0.0);
            routeResult.getSummary().setBBox(bbox);
            routeResult.getSummary().setDescent(0.0);
            routeResult.getSummary().setDistance(0.0);
            routeResult.getSummary().setDuration(0.0);
            PointList pointList = new PointList();
            pointList.add(8.690675487235653, 49.38366164068056);
            pointList.add(8.69063028001704, 49.38376283758349);
            pointList.add(8.690444946820548, 49.38375538700272);
            pointList.add(8.6904630144789, 49.384111711027735);
            pointList.add(8.690476425524274, 49.38459562624832);
            pointList.add(8.690506107646703, 49.38539990448134);
            routeResult.addPointsToGeometry(pointList, false, false);
            ResponsePath responsePath = new ResponsePath();
            responsePath.setDistance(0.0);
            responsePath.setInstructions(null);
            responsePath.setTime(0);
            responsePath.setAscend(0.0);
            responsePath.setDescend(0.0);
            responsePath.setDescription(null);
            responsePath.setFare(BigDecimal.ZERO);
            responsePath.setNumChanges(2);
            responsePath.setRouteWeight(0.0);
            responsePath.setPoints(pointList);
            RouteSegment routeSegment = new RouteSegment(responsePath, DistanceUnit.METERS);
            // Create first routeStep
            RouteStep routeStep1 = new RouteStep();
            routeStep1.setDistance(0.0);
            routeStep1.setDuration(0.0);
            routeStep1.setMessage(null);
            routeStep1.setMessageType(-1);
            routeStep1.setInstruction("");
            routeStep1.setName("");
            routeStep1.setExitNumber(-1);
            routeStep1.setType(11);
            routeStep1.setWayPoints(new int[]{0, 1});
            routeStep1.setManeuver(null);
            routeStep1.setRoundaboutExitBearings(null);
            routeSegment.addStep(routeStep1);

            // Create second routeStep
            RouteStep routeStep2 = new RouteStep();
            routeStep2.setDistance(0.0);
            routeStep2.setDuration(0.0);
            routeStep2.setMessage(null);
            routeStep2.setMessageType(-1);
            routeStep2.setInstruction("");
            routeStep2.setName("");
            routeStep2.setExitNumber(-1);
            routeStep2.setType(0);
            routeStep2.setWayPoints(new int[]{1, 2});
            routeStep2.setManeuver(null);
            routeStep2.setRoundaboutExitBearings(null);
            routeSegment.addStep(routeStep2);

            // Create third routeStep
            RouteStep routeStep3 = new RouteStep();
            routeStep3.setDistance(0.0);
            routeStep3.setDuration(0.0);
            routeStep3.setMessage(null);
            routeStep3.setMessageType(-1);
            routeStep3.setInstruction("");
            routeStep3.setName("");
            routeStep3.setExitNumber(-1);
            routeStep3.setType(10);
            routeStep3.setWayPoints(new int[]{2, 2});
            routeStep3.setManeuver(null);
            routeStep3.setRoundaboutExitBearings(null);
            routeSegment.addStep(routeStep3);
            // Add RouteSegment
            routeResult.addSegment(routeSegment);
            routeResult.addWayPointIndex(0);
            routeResult.addWayPointIndex(2);

            return new RouteResult[]{routeResult};
        } else {
            return new RouteResult[]{};
        }
    }
}
