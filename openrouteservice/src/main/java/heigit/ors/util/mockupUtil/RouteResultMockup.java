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

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.common.DistanceUnit;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;

import java.math.BigDecimal;

/**
 * This is a {@link heigit.ors.routing.RouteResult} Mockup-Class, used in junit tests and wherever needed.
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
        standardHeidelberg
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
        if (profile == routeResultProfile.standardHeidelberg) {
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
            routeResult.addPoints(pointList, false, false);
            PathWrapper pathWrapper = new PathWrapper();
            pathWrapper.setDistance(0.0);
            pathWrapper.setInstructions(null);
            pathWrapper.setTime(0);
            pathWrapper.setAscend(0.0);
            pathWrapper.setDescend(0.0);
            pathWrapper.setDescription(null);
            pathWrapper.setFare(BigDecimal.ZERO);
            // MARQ24 UPDATE to gh 0.10.1 - method not present anylonger
            //pathWrapper.setFirstPtLegDeparture(0);
            pathWrapper.setNumChanges(2);
            pathWrapper.setRouteWeight(0.0);
            pathWrapper.setPoints(pointList);
            RouteSegment routeSegment = new RouteSegment(pathWrapper, DistanceUnit.Meters);
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
            routeResult.setWayPointsIndices(new int[]{0, 2});
            routeResult.setLocationIndex(0);

            return new RouteResult[]{routeResult};
        } else {
            return null;
        }
    }
}
